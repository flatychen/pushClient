package cn.flaty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.nio.ConnectHandler.AfterConnectListener;
import cn.flaty.utils.AssertUtils;

public class SimpleEventLoop {

	private Logger log = LoggerFactory.getLogger(SimpleEventLoop.class);

	private int timeOut;

	private static int DEFAULTTIMEOUT = 3000;

	private InetSocketAddress socket;

	/**
	 * accept
	 */
	private ConnectHandler connect;

	/**
	 * readWrite handlers
	 */
	private ReadWriteHandler readWrite;

	private volatile Selector selector;

	private volatile SelectionKey key;

	private SocketChannel channel;

	public SimpleEventLoop(InetSocketAddress socket) {
		super();
		this.socket = socket;
	}

	private void openChannel() throws IOException {
		this.validate();
		// 获得一个Socket通道
		channel = SocketChannel.open();
		// 设置通道为非阻塞
		channel.configureBlocking(false);
		// 获得一个通道管理器
		this.selector = Selector.open();
		
	}

	public void setConnect(ConnectHandler connect) {
		this.connect = connect;
	}

	public boolean connect() throws IOException {

		this.openChannel();

		AfterConnectListener listener = readWrite.getAfterConnectListener();
		try {
			connect.connect(selector, channel, socket, DEFAULTTIMEOUT);
		} catch (Exception e) {
			log.error("---->" + e.getMessage());
			clear();
			listener.fail();
			return false;
		}
		this.initReadWriteHandler();
		listener.success();
		return true;
	}

	/**
	 * FIXME cancle write key bug?
	 * 
	 * @throws IOException
	 */
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
					// if (key.isConnectable()) {
					// // 连接事件
					// AfterAcceptListener listener = readWrite
					// .getAfterAcceptListener();
					// try {
					// accept.connect(selector, key,timeOut);
					// } catch (Exception e) {
					// log.error("---->" + e.getMessage());
					// clear();
					// listener.fail();
					// return;
					// }
					//
					// this.initReadWriteHandler();
					// listener.success();
					//
					// } else

					if (key.isReadable()) {
						// 可读事件
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
		AssertUtils.notNull(connect, "----> accept 属性不能主空");
		AssertUtils.notNull(readWrite, "----> readWrite 属性不能主空");

	}

	public void setReadWrite(ReadWriteHandler readWrite) {
		this.readWrite = readWrite;
	}

	private void clear() {
		if (key != null) {
			key.cancel();
		}
		try {
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
