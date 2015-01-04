package cn.flaty.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.flaty.nio.ReadWriteHandler;
import cn.flaty.nio.SimpleEventLoop;

public class PushService {

	
	private static int HEARTBEATTIME = 30;
	
	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
	
	
	private ReadWriteHandler readWriteHandler;
	
	
	
	
	public PushService() {
		super();
	}


	public void receiveMsg(String msg){
		System.out.println(msg);
	}
	
	
	public void heartBeat(){
		final Runnable heartBeat = new Runnable() {
			@Override
			public void run() {
				readWriteHandler.writeMsg("中国人");
			}
		};
		ses.scheduleAtFixedRate(heartBeat, HEARTBEATTIME, HEARTBEATTIME, TimeUnit.SECONDS);
	}
	
	
	public void conn(String host,int port){
		this.readWriteHandler = new ReadWriteHandler(this);
		// 阻塞等待
		readWriteHandler.startConn(host, port);
		System.out.println("ddd");
		
	}
	
	public void sendMsg(String msg){
		System.out.println(msg);
	}
}
