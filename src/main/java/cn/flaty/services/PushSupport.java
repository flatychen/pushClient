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

public abstract class PushSupport implements PushService {

	private Logger log = LoggerFactory.getLogger(PushSupport.class);

	private static int MAX_RECONNCNT = 5;

	private int reConnCnt = 0;

	private static int HEART_BEAT_TIME = 30;

	private static int HEART_BEAT_DEPLAY = 5;

	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

	private ExecutorService es = Executors.newFixedThreadPool(1);

	private ReadWriteHandler readWriteHandler;

	public PushSupport() {
		super();
	}

	private void heartBeat() {
		ses.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				log.info("----> 心跳~~");
				// readWriteHandler.doWrite("心跳测试");
			}
		}, HEART_BEAT_DEPLAY, HEART_BEAT_TIME, TimeUnit.SECONDS);
	}

	public void startUp(final String host, final int port) {

		this.readWriteHandler = ReadWriteHandler.getInstance(this);
		readWriteHandler.InitEventLoop(host, port);
		readWriteHandler.setAfterConnectListener(simpleAfterConnectListener);
		readWriteHandler.setChannelReadListener(simpleChannelReadListener);
		readWriteHandler.setChannelWriteListener(simpleChannelWriteListener);
		readWriteHandler.connect(es);
		

	}

	private String prepareDeviceInfo() {
		return "开始连接";
	}

	protected void sendMsg(String msg) {
		System.out.println(msg);
	}

	private  AfterConnectListener simpleAfterConnectListener = new AfterConnectListener() {
		@Override
		public void success() {
			readWriteHandler.doWrite(prepareDeviceInfo());
			// 连接成功，开始心跳
			heartBeat();
		}

		@Override
		public void fail() {
			if(reConnCnt++ < MAX_RECONNCNT && ReadWriteHandler.STATE.connnected != ReadWriteHandler.state  ){
				try {
					log.info(MessageFormat.format(
							"---->建立连接失败，总共重试{0}次，现重试第{1}次", MAX_RECONNCNT,
							reConnCnt));
					Thread.sleep(5000 * reConnCnt);
					readWriteHandler.connect(es);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (reConnCnt >= MAX_RECONNCNT){
				log.info(MessageFormat.format("---->{0}次连接均失败，关闭任务！ ",
						MAX_RECONNCNT));
				es.shutdown();
			}
		}
	};

	private ChannelReadListener simpleChannelReadListener = new ChannelReadListener() {

		@Override
		public void success() {}

		@Override
		public void fail() {
			ses.shutdownNow();
			es.shutdownNow();
		}
	};

	private ChannelWriteListener simpleChannelWriteListener = new ChannelWriteListener() {

		@Override
		public void success() {}

		@Override
		public void fail() {
			ses.shutdownNow();
			es.shutdownNow();
		}
	};


}
