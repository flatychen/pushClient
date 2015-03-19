package cn.flaty.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.pushFrame.SimplePushHead;
import cn.flaty.pushFrame.SimplePushOutFrame;

public class ReadWriteHandler {

	private InputStream is;
	
	private OutputStream os;
	
	private SocketCallBack socketCallBack;
	
	private Logger log = LoggerFactory.getLogger(ReadWriteHandler.class);

	public ReadWriteHandler(InputStream is, OutputStream os,SocketCallBack scb) {
		super();
		this.is = is;
		this.os = os;
		this.socketCallBack = scb;
	}

	public void init() {
		
	}

	
	private void receive(String msg) throws IOException{
		os.write(msg.getBytes());
	}
	
	
	public void send(String msg) throws IOException{
		if(StringUtils.isBlank(msg)){
			throw new IOException("发送消息不能为空");
		}
		SimplePushOutFrame frame = new SimplePushOutFrame(SimplePushHead.getInstance(), msg);
		writeToSocket(frame);
	}

	private void writeToSocket(SimplePushOutFrame frame) {
		try {
			os.write(frame.getLength());
			os.write(frame.getHead());
			os.write(frame.getBody());
		} catch (IOException e) {
			socketCallBack.onError(e);
		}
		
	}

	
}
