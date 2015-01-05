package cn.flaty.services;

import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.nio.AcceptHandler.AfterAcceptListener;
import cn.flaty.nio.ReadWriteHandler;

public final class PushService {
	
	
	private Logger log = LoggerFactory.getLogger(PushService.class);

	private static int HEART_BEAT_TIME = 5;

	private static int HEART_BEAT_DEPLAY = 5;

	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

	private ExecutorService es = Executors.newFixedThreadPool(1);

	private static int MAX_RECONNCNT = 5;
	
	private int reConnCnt = 0;
	
	private ReadWriteHandler readWriteHandler;

	public PushService() {
		super();
	}

	public void receiveMsg(String msg) {
		System.out.println(msg);
	}

	private void heartBeat() {
		ses.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				System.out.println("心跳测试");
				//readWriteHandler.doWrite("心跳测试");
			}
		}, HEART_BEAT_DEPLAY, HEART_BEAT_TIME, TimeUnit.SECONDS);
	}

	public void connect(final String host, final int port) {

		this.readWriteHandler = new ReadWriteHandler(this);
		readWriteHandler.InitEventLoop(host, port);
		readWriteHandler.setAfterAcceptListener(new AfterAcceptListener() {

			@Override
			public void success() {
				System.out.println("success");
				readWriteHandler.doWrite(prepareDeviceInfo());;
				heartBeat();
			}

			@Override
			public void fail() {
				if( reConnCnt ++ < MAX_RECONNCNT){
					try {
						log.info(MessageFormat.format("---->建立连接失败，递递{0}次重试，现重试第{1}次",MAX_RECONNCNT,reConnCnt));
						Thread.sleep(2000 * reConnCnt);
						es.submit(readWriteHandler);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}else{
					log.info("---->5次连接均失败，关闭任务！ ");
					es.shutdown();
				}
			}
		});
		// 异步连接开始
		es.submit(readWriteHandler);

	}

	private String prepareDeviceInfo() {
		return "开始连接";
	}

	public void sendMsg(String msg) {
		System.out.println(msg);
	}
	
	
	
	
}
