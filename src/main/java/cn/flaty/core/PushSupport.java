package cn.flaty.core;

import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.nio.ConnectHandler;
import cn.flaty.nio.ConnectHandler.AfterConnectListener;
import cn.flaty.nio.ReadWriteHandler;
import cn.flaty.nio.ReadWriteHandler.ChannelReadListener;
import cn.flaty.nio.ReadWriteHandler.ChannelWriteListener;
import cn.flaty.nio.SimpleEventLoop;
import cn.flaty.nio.SimpleEventLoop.STATE;

public abstract class PushSupport implements PushService {

	private static Logger log = LoggerFactory.getLogger(PushSupport.class);

	private static int MAX_RECONNCNT = 3;

	private AtomicInteger connCount = null;

	private ScheduledExecutorService ses = null;

	private ExecutorService es = null;

	private Future<Integer> nioEvent;

	private ReadWriteHandler readWriteHandler;

	public PushSupport() {
	}



	public void connect(final String host, final int port, int threads) {
		this.connCount = new AtomicInteger(0);
		this.readWriteHandler = new ReadWriteHandler(this);
		readWriteHandler.InitEventLoop(host, port);
		readWriteHandler.setAfterConnectListener(simpleAfterConnectListener);
		readWriteHandler.setChannelReadListener(simpleChannelReadListener);
		readWriteHandler.setChannelWriteListener(simpleChannelWriteListener);
		if (readWriteHandler.isStoped()) {
			nioEvent = readWriteHandler.connect(threads);
		}

	}


	private AfterConnectListener simpleAfterConnectListener = new AfterConnectListener() {
		@Override
		public void success() {
		}

		@Override
		public void fail() {
//			if (connCount.incrementAndGet() <= MAX_RECONNCNT
//					&& SimpleEventLoop.STATE.connnected != readWriteHandler.) {
//				try {
//
//					log.info("建立连接失败，{}秒后重试第{}次",
//							20 * connCount.get(), connCount.get());
//					Thread.sleep(20000 * connCount.get());
//					nioEvent = readWriteHandler.connect(es);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			if (connCount.get() > MAX_RECONNCNT) {
//				log.info(MessageFormat.format("{}次连接均失败！停止连接~~ ",
//						MAX_RECONNCNT));
//				SimpleEventLoop.state = STATE.stop;
//			}
		}
	};

	private ChannelReadListener simpleChannelReadListener = new ChannelReadListener() {

		@Override
		public void success() {
		}

		@Override
		public void fail() {
			nioEvent.cancel(true);
		}
	};

	private ChannelWriteListener simpleChannelWriteListener = new ChannelWriteListener() {

		@Override
		public void success() {
		}

		@Override
		public void fail() {
			nioEvent.cancel(true);
		}
	};
}
