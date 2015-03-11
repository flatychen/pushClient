package cn.flaty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.utils.AssertUtils;

/**
 * 简单事件循环，主要用于连接与监听读事件
 * 
 * @author flatychen
 */
public class SimpleEventLoop {

	public enum STATE {
		stop, connecting, connnected
	}

	public volatile SimpleEventLoop.STATE state = STATE.stop;

	public SimpleEventLoop.STATE getState() {
		return state;
	}

	public void setState(SimpleEventLoop.STATE state) {
		this.state = state;
	}

	private Logger log = LoggerFactory.getLogger(SimpleEventLoop.class);

	private int timeOut;

	private static int DEFAULTTIMEOUT = 2;

	private InetSocketAddress socket;

	private ConnectHandler connect;

	private ReadWriteHandler readWrite;

	private volatile Selector selector;

	private volatile SelectionKey key;

	private volatile SocketChannel channel;

	public SimpleEventLoop(InetSocketAddress socket) {
		this(socket, DEFAULTTIMEOUT);
	}

	public SimpleEventLoop(InetSocketAddress socket, int timeOut) {
		super();
		this.socket = socket;
		this.timeOut = timeOut;
	}

	public void openChannel() throws IOException {
		this.validate();
		// 获得一个Socket通道
		channel = SocketChannel.open();
		// 设置通道为非阻塞
		// channel.configureBlocking(false);
		// 获得一个通道管理器
		this.selector = Selector.open();
		this.initReadWriteHandler();
	}

	public void setConnect(ConnectHandler connect) {
		this.connect = connect;
	}

	public void connect() {
		// 开始连接
		if (!this.connect.connect(selector, channel, socket, timeOut)) {
			return;
		}
	}

	public void eventLoop() throws IOException {
		// 轮询访问selector
		while (selector.select() > 0) {
			// 获得selector中选中的项的迭代器
			Iterator<SelectionKey> keys = this.selector.selectedKeys()
					.iterator();
			while (keys.hasNext()) {
				key = keys.next();
				// 删除已选的key,以防重复处理
				keys.remove();

				if (key.isValid()) {

					// 可读事件
					if (key.isReadable()) {

						readWrite.doRead(key);
					}

					// else if (key.isWritable()) {
					// key.cancel();
					//
					//
					// }

				}

			}

		}
	}

	private void initReadWriteHandler() {
		this.readWrite.setSelector(selector);
		this.readWrite.setChannel(channel);
	}

	private void validate() {
		AssertUtils.notNull(connect, " connect 属性不能为空");
		AssertUtils.notNull(readWrite, " readWrite 属性不能为空");

	}

	public void setReadWrite(ReadWriteHandler readWrite) {
		this.readWrite = readWrite;
	}

	/**
	 * 
	 * 清理资源
	 * 
	 * @param selector
	 * @param channel
	 * @param key
	 * @author flatychen
	 */
	public static void clearUp(Selector selector, SocketChannel channel,
			SelectionKey key) {
		if (key != null) {
			key.cancel();
		}
		try {
			channel.close();
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
