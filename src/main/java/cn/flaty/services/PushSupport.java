package cn.flaty.services;

import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

	private Logger log = LoggerFactory.getLogger(PushSupport.class);

	private static int MAX_RECONNCNT = 3;

	private int reConnCnt = 0;

	private static int HEART_BEAT_TIME = 20;

	private static int HEART_BEAT_DEPLAY = 2;

	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

	private ExecutorService es = Executors.newFixedThreadPool(1);

	private ReadWriteHandler readWriteHandler;

	public PushSupport() {
		super();
	}

	private void StartHeartBeat() {
		ses.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				log.info(" ~~心跳~~");
				readWriteHandler.doWrite("心跳报文");
			}
		}, HEART_BEAT_DEPLAY, HEART_BEAT_TIME, TimeUnit.SECONDS);
	}

	public void startUp(final String host, final int port) {

		this.readWriteHandler = ReadWriteHandler.getInstance(this);
		readWriteHandler.InitEventLoop(host, port);
		readWriteHandler.setAfterConnectListener(simpleAfterConnectListener);
		readWriteHandler.setChannelReadListener(simpleChannelReadListener);
		readWriteHandler.setChannelWriteListener(simpleChannelWriteListener);
		if (SimpleEventLoop.state == STATE.stop) {
			readWriteHandler.connect(es);
		}

	}

	private String prepareDeviceInfo() {
		return "开始连接";
	}

	protected void sendMsg(String msg) {
	}

	private AfterConnectListener simpleAfterConnectListener = new AfterConnectListener() {
		@Override
		public void success() {
			readWriteHandler.doWrite(prepareDeviceInfo());
			// 连接成功，开始心跳
			//StartHeartBeat();
		}

		@Override
		public void fail() {
			if (reConnCnt++ < MAX_RECONNCNT
					&& SimpleEventLoop.STATE.connnected != SimpleEventLoop.state) {
				try {
					log.info(MessageFormat.format("建立连接失败，总共重试{0}次，现重试第{1}次",
							MAX_RECONNCNT, reConnCnt));
					Thread.sleep(20000 * reConnCnt);
					readWriteHandler.connect(es);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (reConnCnt >= MAX_RECONNCNT) {
				log.info(MessageFormat.format("{0}次连接均失败！ ", MAX_RECONNCNT));
				SimpleEventLoop.state = STATE.stop;
			}
		}
	};

	private ChannelReadListener simpleChannelReadListener = new ChannelReadListener() {

		@Override
		public void success() {
		}

		@Override
		public void fail() {
			ses.shutdownNow();
			es.shutdownNow();
		}
	};

	private ChannelWriteListener simpleChannelWriteListener = new ChannelWriteListener() {

		@Override
		public void success() {
		}

		@Override
		public void fail() {
			ses.shutdownNow();
			es.shutdownNow();
		}
	};

}
