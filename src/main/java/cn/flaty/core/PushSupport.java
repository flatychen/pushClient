package cn.flaty.core;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.nio.ConnectHandler.AfterConnectListener;
import cn.flaty.nio.ReadWriteHandler;
import cn.flaty.nio.ReadWriteHandler.ChannelReadListener;
import cn.flaty.nio.ReadWriteHandler.ChannelWriteListener;

public abstract class PushSupport implements PushService {

	private static Logger log = LoggerFactory.getLogger(PushSupport.class);


	private Future<Integer> nioEvent;

	private ReadWriteHandler readWriteHandler;

	public PushSupport() {
	}



	public void connect(final String host, final int port, int threads) {
		this.readWriteHandler = new ReadWriteHandler(this);
		readWriteHandler.InitEventLoop(host, port);
		readWriteHandler.setAfterConnectListener(simpleAfterConnectListener);
		readWriteHandler.setChannelReadListener(simpleChannelReadListener);
		readWriteHandler.setChannelWriteListener(simpleChannelWriteListener);
		if (readWriteHandler.isStoped()) {
			nioEvent = readWriteHandler.connect(threads);
		}

	}

	private String prepareNewConnMsg(){
		return "连接成功包测试";
	}
	

	private AfterConnectListener simpleAfterConnectListener = new AfterConnectListener() {
		@Override
		public void success() {
			readWriteHandler.doWrite(prepareNewConnMsg());
		}

		@Override
		public void fail() {
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
