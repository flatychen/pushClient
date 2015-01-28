package cn.flaty.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class PushServiceImpl extends PushSupport implements PushService {

	
	private Logger log = LoggerFactory.getLogger(PushServiceImpl.class);
	
	@Override
	public void receiveMsg(String msg) {
		log.info(msg);
	}
	
	
}
