package cn.flaty.core;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.bio.ReadWriteHandler;

public abstract class PushSupport implements PushService {

	private static Logger log = LoggerFactory.getLogger(PushSupport.class);


	private Future<Integer> nioEvent;

	private ReadWriteHandler readWriteHandler;

	public PushSupport() {
	}


}
