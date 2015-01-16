package cn.flaty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.nio.ConnectHandler.AfterConnectListener;
import cn.flaty.nio.SimpleEventLoop.STATE;
import cn.flaty.pushFrame.FrameHead;
import cn.flaty.pushFrame.SimplePushHead;
import cn.flaty.pushFrame.SimplePushInFrame;
import cn.flaty.pushFrame.SimplePushOutFrame;
import cn.flaty.services.PushService;
import cn.flaty.utils.ByteBufUtil;

public class ReadWriteHandler implements Runnable {
	
	
	private Logger log = LoggerFactory.getLogger(ReadWriteHandler.class);

	private FrameHead frameHeader;

	private ByteBuf readBuf;

	private ByteBuf writeBuf;

	private ConnectHandler connectHandler;

	/**
	 * 读通道监听器
	 */
	private ChannelReadListener channelReadListener;

	/**
	 * 写通道监听器
	 */
	private ChannelWriteListener channelWriteListener;

	/**
	 * 选择器，用于注册
	 */
	private Selector selector;

	/**
	 * socke通道
	 */
	private SocketChannel channel;

	/**
	 * 业务逻辑处理
	 */
	private PushService pushService;

	/**
	 * nio 事件循环
	 */
	private SimpleEventLoop eventLoop;

	private static volatile ReadWriteHandler readWriteHandler = null;

	public static ReadWriteHandler getInstance(PushService pushService) {
		if (readWriteHandler == null) {
			synchronized (ReadWriteHandler.class) {
				readWriteHandler = new ReadWriteHandler(pushService,
						new SimplePushHead());
			}
		}
		return readWriteHandler;
	}

	private ReadWriteHandler(PushService pushService) {
		this(pushService, new SimplePushHead());
	}

	private ReadWriteHandler(PushService pushService, FrameHead frameHeader) {
		super();
		this.pushService = pushService;
		this.frameHeader = frameHeader;

	}

	public void InitEventLoop(String host, int port) {
		this.connectHandler = new ConnectHandler();
		eventLoop = new SimpleEventLoop(new InetSocketAddress(host, port));
		eventLoop.setConnect(connectHandler);
		eventLoop.setReadWrite(this);
		this.readBuf = ByteBufUtil.ByteBuf();
		this.writeBuf = ByteBufUtil.compositBuf();
	}

	public void doWrite(String msg) {
		SimplePushOutFrame frame = new SimplePushOutFrame(frameHeader,
				msg.getBytes());
		writeBuf.put(frame.getLength());
		writeBuf.put(frame.getHead());
		writeBuf.put(frame.getBody());
		try {
			this.write();
		} catch (Exception e) {
			SimpleEventLoop.state = STATE.stop;
			this.channelWriteListener.fail();
			log.error(e.toString());
			return;
		}

	}

	private void write() throws IOException {
		writeBuf.flip();
		int byteToWrite = 0;
		if (writeBuf.isCompositBuf()) {
			ByteBuffer buffers[] = writeBuf.nioBuffers();
			for (ByteBuffer byteBuffer : buffers) {
				byteToWrite = channel.write(byteBuffer);
				writeBuf.position(writeBuf.position() + byteToWrite);
			}
		} else {
			channel.write(writeBuf.nioBuffer());
		}

		writeBuf.resetBuf().clear();
	}

	private void read() throws IOException {
		int byteToRead = 0;
		if (readBuf.isCompositBuf()) {
			ByteBuffer buffers[] = readBuf.nioBuffers();
			for (ByteBuffer byteBuffer : buffers) {
				byteToRead = channel.read(byteBuffer);
				readBuf.position(readBuf.position() + byteToRead);
			}
		} else {
			byteToRead = channel.read(readBuf.nioBuffer());
		}

	}

	public void doRead(SelectionKey key) {
		try {
			this.read();
		} catch (IOException e) {
			SimpleEventLoop.state = STATE.stop;
			this.channelReadListener.fail();
			log.error(e.getMessage());
			return;
		}

		// 切包，直到拿到完整的包再纪续执行
		byte[] frameBytes = this.splitFrame();
		if (frameBytes == null) {
			return;
		}

		SimplePushInFrame frame = new SimplePushInFrame(frameHeader, frameBytes);
		String s = frame.getBody();

		// 业务逻辑处理
		pushService.receiveMsg(s);

		try {
			channel.register(selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			log.error("" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return
	 */
	private byte[] splitFrame() {

		//
		// 拆包开始;
		// 如果读取的字节大于包头长度，则一直等到长度为止，
		// 否则，一直读等到包头长度大于为止;
		//
		int bytesToRead = 0;
		// 反转buf
		readBuf.flip();
		if (readBuf.remaining() > frameHeader.byteLength()) {
			// 必须读到包的长度字节

			byte[] frameLengthBytes = new byte[frameHeader.byteLength()];
			readBuf.get(frameLengthBytes);
			// 包长度大小
			bytesToRead = frameHeader.bytesToInt(frameLengthBytes)
					+ frameHeader.headLength();

			if (readBuf.remaining() < bytesToRead) {
				// 没有完整读到所有包的内容,应继续读取

				if (readBuf.limit() == readBuf.capacity()) {
					// buffer 长度不够 ，需扩展buffer

					int oldBufLength = readBuf.capacity();
					int newBufLength = bytesToRead + frameHeader.byteLength();

					readBuf.position(readBuf.limit());
					// 扩展原包大小
					ByteBuf newBuffer = ByteBufUtil.wrapByteBuf(newBufLength
							- oldBufLength, readBuf);
					readBuf = newBuffer;
					return null;
				}

			}

		} else {
			//
			// 没读到包长度字节，调整buffer，继续读取
			// 注意postion 与 limit 设置
			// 接上次位置，继续读取
			//
			readBuf.position(readBuf.limit());
			readBuf.limit(readBuf.capacity());
			return null;
		}

		// 拆包完毕,已读一个完整的frame

		byte[] frame = new byte[bytesToRead];
		readBuf.get(frame);
		readBuf = readBuf.resetBuf().clear();
		return frame;

	}

	public void setSelector(Selector selector) {
		this.selector = selector;
	}

	public void setChannel(SelectableChannel schannel) {
		this.channel = (SocketChannel) schannel;
	}

	public void setChannelReadListener(ChannelReadListener channelReadListener) {
		this.channelReadListener = channelReadListener;
	}

	public void setChannelWriteListener(
			ChannelWriteListener channelWriteListener) {
		this.channelWriteListener = channelWriteListener;
	}

	public void setAfterConnectListener(
			AfterConnectListener afterConnectListener) {
		this.connectHandler.setAfterConnectListener(afterConnectListener);
	}


	public static interface ChannelReadListener {
		void success();
		void fail();
	}

	public static interface ChannelWriteListener {
		void success();
		void fail();
	}

	public void connect(ExecutorService es) {
		es.submit(this);
	}

	
	
	@Override
	public void run() {
		try {
			eventLoop.openChannel();
			eventLoop.eventLoop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
