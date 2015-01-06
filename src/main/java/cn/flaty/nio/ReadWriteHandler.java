package cn.flaty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.nio.AcceptHandler.AfterAcceptListener;
import cn.flaty.pushFrame.FrameHead;
import cn.flaty.pushFrame.SimplePushFrame;
import cn.flaty.pushFrame.SimplePushHead;
import cn.flaty.services.PushService;
import cn.flaty.utils.CharsetUtil;

public class ReadWriteHandler implements Runnable{

	
	private Logger log = LoggerFactory.getLogger(ReadWriteHandler.class);

	private static int BUFFER_SIZE = 4096;

	private FrameHead frameHeader = null;

	private ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);

	private ByteBuffer writeBuf = ByteBuffer.allocate(BUFFER_SIZE);
	
	
	
	
	
	/**
	 * 连接监听器
	 */
	private AfterAcceptListener afterAcceptListener ;
	
	/**
	 * 读通道监听器
	 */
	private ChannelReadListener channelReadListener ;
	
	/**
	 * 写通道监听器
	 */
	private ChannelWriteListener channelWriteListener ;

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

	public ReadWriteHandler(PushService pushService) {
		this(pushService, new SimplePushHead());
	}

	public ReadWriteHandler(PushService pushService, FrameHead frameHeader) {
		super();
		this.pushService = pushService;
		this.frameHeader = frameHeader;

	}

	public void InitEventLoop(String host, int port){
		eventLoop = new SimpleEventLoop(new InetSocketAddress(host, port));
		eventLoop.setAccept(new AcceptHandler());
		eventLoop.setReadWrite(this);

	}


	public void doWrite(String msg) {
		writeBuf.clear();
		
		byte[] frame = encodeFrame(msg);
		
		writeBuf.put(frame);
		
		writeBuf.flip();
		try {
			int r = channel.write(writeBuf);
			channel.register(selector, SelectionKey.OP_READ);
		} catch (Exception e) {
			this.channelWriteListener.fail();
			log.error("---->"+e.getMessage());
			e.printStackTrace();
		}
		
	}

	private byte[] encodeFrame(String s) {
		byte[] body = s.getBytes();
		int bodyLength = body.length;
		byte[] frame = new byte[bodyLength + frameHeader.byteLength()
				+ frameHeader.headLength()];

		// 添加长度
		byte[] header_len = frameHeader.intToBytes(bodyLength);
		System.arraycopy(header_len, 0, frame, 0, frameHeader.byteLength());

		// 添加包头，4字节
		byte[] header = new byte[frameHeader.headLength()];
		// utf 编码
		header[1] = 1;
		System.arraycopy(header, 0, frame, frameHeader.byteLength(),
				frameHeader.headLength());

		// 添加内容
		System.arraycopy(body, 0, frame,
				frameHeader.byteLength() + frameHeader.headLength(), bodyLength);

		return frame;
	}

	public void doRead(SelectionKey key){
		readBuf.clear();
		
		try {
			channel.read(readBuf);
		} catch (IOException e) {
			this.channelReadListener.fail();
			log.error("---->"+e.getMessage());
			e.printStackTrace();
		}

		// 切包
		byte[] frameBytes = this.splitFrame();
		SimplePushFrame frame = new SimplePushFrame(frameHeader, frameBytes);

		// 解包，得到内容
		String s = this.dencodeFrame(frame);

		pushService.receiveMsg(s);

		try {
			channel.register(selector, SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			this.afterAcceptListener.fail();
			log.error("---->"+e.getMessage());
			e.printStackTrace();
		}
	}

	private String dencodeFrame(SimplePushFrame frame) {
		byte charsetType = frame.getCharsetType();
		String s = null;
		byte[] _b = frame.getBody();
		switch (charsetType) {
		case 0:
			s = new String(_b);
			break;
		case 1:
			s = new String(_b, CharsetUtil.UTF_8);
			break;
		case 2:
			s = new String(_b, CharsetUtil.US_ASCII);
			break;
		case 3:
			s = new String(_b, CharsetUtil.GBK);
			break;
		case 4:
			s = new String(_b, CharsetUtil.GB2312);
			break;
		}
		return s;
	}

	/**
	 * 
	 * FIXME 扩展buf
	 * 
	 * @return
	 */
	private byte[] splitFrame() {
		readBuf.flip();

		//
		// 拆包开始;
		// 如果读取的字节大于包头长度，则一直等到长度为止，
		// 否则，一直读等到包头长度大于为止;
		//
		int bytesToRead = 0;

		if (readBuf.remaining() > frameHeader.byteLength()) {
			// 必须读到包的长度字节

			byte[] frameLengthBytes = new byte[frameHeader.byteLength()];
			readBuf.get(frameLengthBytes);
			// 包长度大小
			bytesToRead = frameHeader.bytesToInt(frameLengthBytes) + frameHeader.headLength();

			if (readBuf.remaining() < bytesToRead) {
				// 没有完整读到所有包的内容,应继续读取

				if (readBuf.limit() == readBuf.capacity()) {
					// buffer 长度不够 ，需扩展buffer

					// 注意后期抽象
					int oldBufLength = readBuf.capacity();
					int newBufLength = bytesToRead + frameHeader.byteLength();
					ByteBuffer newBuffer = ByteBuffer
							.allocateDirect(newBufLength);

					readBuf.position(0);
					newBuffer.put(readBuf);

					readBuf = newBuffer;

					readBuf.position(oldBufLength);
					readBuf.limit(newBufLength);

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

		// 拆包完毕

		byte[] frame = new byte[bytesToRead];
		readBuf.get(frame);
		readBuf.clear();
		return frame;

	}

	public void setSelector(Selector selector) {
		this.selector = selector;
	}

	public void setChannel(SelectableChannel schannel) {
		this.channel = (SocketChannel) schannel;
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

	public ChannelReadListener getChannelReadListener() {
		return channelReadListener;
	}

	public void setChannelReadListener(ChannelReadListener channelReadListener) {
		this.channelReadListener = channelReadListener;
	}

	public ChannelWriteListener getChannelWriteListener() {
		return channelWriteListener;
	}

	public void setChannelWriteListener(ChannelWriteListener channelWriteListener) {
		this.channelWriteListener = channelWriteListener;
	}

	public AcceptHandler.AfterAcceptListener getAfterAcceptListener() {
		return afterAcceptListener;
	}

	public void setAfterAcceptListener(
			AcceptHandler.AfterAcceptListener afterAcceptListener) {
		this.afterAcceptListener = afterAcceptListener;
	}
	
	
	public static interface ChannelReadListener{
		void success();
		void fail();
	}

	
	public static interface ChannelWriteListener{
		void success();
		void fail();
	}
}
