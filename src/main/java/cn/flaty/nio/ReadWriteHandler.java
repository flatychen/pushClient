package cn.flaty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.PushConstant;
import cn.flaty.PushFrameParser;
import cn.flaty.model.ClientInfo;
import cn.flaty.model.GenericMessage;
import cn.flaty.pushFrame.PushFrameDecoder;

import com.alibaba.fastjson.JSON;

public class ReadWriteHandler {
	
	private Logger log = LoggerFactory.getLogger(ReadWriteHandler.class);
	
	private static int BUFFER_SIZE = 5;
	
	private volatile boolean readFlag = false;
	
	
	
	private ByteBuffer readBuf = ByteBuffer.allocateDirect(BUFFER_SIZE);
	
	private ByteBuffer writeBuf = ByteBuffer.allocate(BUFFER_SIZE);
	

	private PushFrameDecoder frameDecoder = new PushFrameDecoder();
	
	public void write(Selector selector, SelectionKey key) {
//		SocketChannel socketChannel = this.getChannel(key);
//		writeBuf.clear();
//		ClientInfo client = new ClientInfo();
//		client.setCid((new Date().getTime())+"");
//		GenericMessage msg = new GenericMessage();
//		msg.setCommond(100);
//		msg.setMessage(JSON.toJSONString(client));
//		
//		byte [] frame = frameParser.encode(JSON.toJSONString(msg));
//		
//		writeBuf.put(frame);
//		
//		
//		writeBuf.flip();
//		
//		try {
//			int r = socketChannel.write(writeBuf);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		try {
//			Thread.sleep(30000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public void read(Selector selector, SelectionKey key) throws IOException {
		System.out.println("read");
		SocketChannel channel = this.getChannel(key);
		// 清空buffer，使可读
		readBuf.clear();
		int readLen = channel.read(readBuf);
		// 无读取到字节，直接返回
		if(readLen <= 0 ){
			log.warn("----> nio reader 没有读到任何数据 ！");
			return ;
		}else{
			frameDecoder.decode(readBuf);
		}
		readBuf.clear();
		channel.register(selector, SelectionKey.OP_READ);
	}

	private SocketChannel getChannel(SelectionKey key){
		return  (SocketChannel)key.channel();
	}
	
	
}
