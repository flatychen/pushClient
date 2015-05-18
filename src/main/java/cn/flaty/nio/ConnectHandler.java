package cn.flaty.nio;

import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectHandler {

	
	private static int DEFAULTSCHEDULETIMETHREADS =  2 ;
	
	private static ScheduledExecutorService ses = Executors.newScheduledThreadPool(DEFAULTSCHEDULETIMETHREADS);

	private Logger log = LoggerFactory.getLogger(ConnectHandler.class);

	private AfterConnectListener afterConnectListener;

	private volatile boolean connecting;


	public boolean connect(Selector selector, SocketChannel channel,
			InetSocketAddress socket, int timeOut) {
		// 提供简单超时检查
		this.connecting = true;
		
		
		ScheduledFuture<Boolean> sf = ses.schedule(new Callable<Boolean>() {
			@Override
			public Boolean call() {
				Thread.currentThread().setName("timeOut-thread");
				connecting = false;
				return true;
			}
		}, timeOut, TimeUnit.SECONDS);
		
		try {
			// 开始连接
			channel.configureBlocking(false);
			
			boolean connected = false;
			channel.connect(socket);
			while (connecting && !(connected)) {
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

			
			channel.register(selector, SelectionKey.OP_READ);
		} catch (Exception e) {
			log.error(e.toString());
			SimpleEventLoop.clearUp(selector, channel, null);
			afterConnectListener.fail();
			return false;
		}finally{
			sf.cancel(true);
		}
		afterConnectListener.success();
		log.info(" 连接建立成功");
		return true;

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
