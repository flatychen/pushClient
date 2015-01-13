package cn.flaty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectHandler {
	
	private Logger log = LoggerFactory.getLogger(ConnectHandler.class);

	private volatile boolean connecting = true;
	

	private Selector selector;


	public void connect(Selector s, SocketChannel channel, InetSocketAddress socket, int timeOut) throws IOException {
		this.selector = s;
		// 提供简单超时检查
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				connecting = false;
			}
		}, timeOut);

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
		// channel.register(selector, SelectionKey.OP_WRITE);
		log.info("----> 连接建立成功");

	}


	public static interface AfterConnectListener {
		void success();
		void fail();
	}

}
