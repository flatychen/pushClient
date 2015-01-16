package cn.flaty.nio;

import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.nio.ReadWriteHandler.STATE;

public class ConnectHandler {


	private Logger log = LoggerFactory.getLogger(ConnectHandler.class);

	private AfterConnectListener afterConnectListener;

	private volatile boolean connecting = true;

	private static ConnectHandler connect;

	public static ConnectHandler getInstance() {
		if (connect == null) {
			synchronized (ConnectHandler.class) {
				connect = new ConnectHandler();
			}
		}
		return connect;
	}

	private Selector selector;
	private SocketChannel channel;
	private InetSocketAddress socket;
	private int timeOut;

	public void connect(Selector s, SocketChannel channel,
			InetSocketAddress socket, int timeOut) {
		System.out.println(ReadWriteHandler.state);
		ReadWriteHandler.state = STATE.connecting;
		this.channel = channel;
		this.socket = socket;
		this.timeOut = timeOut;
		// 提供简单超时检查
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				connecting = false;
			}
		}, timeOut);
		try {
			// 开始连接
			boolean connected = false;
			channel.connect(socket);
			while (connecting && !connected) {
				try {
					Thread.sleep(200);
					connected = channel.finishConnect();
				} catch (Exception e) {
					continue;
				}

			}

			if (!connected) {
				throw new SocketTimeoutException(" socket 连接超时 ");
			}

			channel.configureBlocking(false);
			channel.register(selector, SelectionKey.OP_READ);
		} catch (Exception e) {
			log.error("---->" + e.getMessage());
			SimpleEventLoop.clearUp(s, channel, null);
			afterConnectListener.fail();
			return;
		}
		System.out.println("aa");
		afterConnectListener.success();
		ReadWriteHandler.state = STATE.connnected;
		log.info("----> 连接建立成功");

	}

	public void reConnect() {
		this.connect(selector, channel, socket, timeOut);
	}

	public AfterConnectListener getAfterConnectListener() {
		return afterConnectListener;
	}

	public void setAfterConnectListener(
			AfterConnectListener afterConnectListener) {
		this.afterConnectListener = afterConnectListener;
	}

	public static interface AfterConnectListener {
		void success();
		void fail();
	}

}
