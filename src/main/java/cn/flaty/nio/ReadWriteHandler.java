package cn.flaty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.PushConstant;
import cn.flaty.PushFrameParser;
import cn.flaty.model.ClientInfo;
import cn.flaty.model.GenericMessage;
import cn.flaty.pushFrame.PushFrameDecoder;
import cn.flaty.pushFrame.PushFrameLength;
import cn.flaty.pushFrame.SimpleFourBytesPushFrameLength;

import com.alibaba.fastjson.JSON;

public class ReadWriteHandler {
	
	private Logger log = LoggerFactory.getLogger(ReadWriteHandler.class);
	
	private static int BUFFER_SIZE = 4096;
	
	private PushFrameLength pushFrameLength = null;
	
	private ByteBuffer readBuf = ByteBuffer.allocateDirect(BUFFER_SIZE);
	
	private ByteBuffer writeBuf = ByteBuffer.allocateDirect(BUFFER_SIZE);
	

	public ReadWriteHandler() {
		this(new SimpleFourBytesPushFrameLength());
	}
	

	public ReadWriteHandler(PushFrameLength pushFrameLength) {
		super();
		this.pushFrameLength = pushFrameLength;
	}

	public void write(Selector selector, SelectionKey key) {
		SocketChannel socketChannel = this.getChannel(key);
		writeBuf.clear();
		
		String s = RandomStringUtils.randomAlphabetic(2500);
		
		PushFrameParser frameParser = new PushFrameParser();
		byte [] frame = frameParser .encode(s);
		writeBuf.put(frame);
		writeBuf.flip();
		try {
			int r = socketChannel.write(writeBuf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(3000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void read(Selector selector, SelectionKey key) throws IOException {
		SocketChannel channel = this.getChannel(key);
		
		readBuf.clear();
		if(channel.read(readBuf) == -1 ){
			throw new IOException("----> 读取失败");
		}
		
		
		this.splitFrame();
		
		channel.register(selector, SelectionKey.OP_READ);
	}

	private SocketChannel getChannel(SelectionKey key){
		return  (SocketChannel)key.channel();
	}
	
	
	
	public void splitFrame(){
		readBuf.flip();
		
		// 
		// 拆帧开始;
		// 如果读取的字节大于帧头长度，则一直等到长度为止，
		// 否则，一直读等到帧头长度大于为止;
		// 
		int bytesToRead = 0; 
		
		if( readBuf.remaining() > pushFrameLength.byteLength() ){
		// 	必须读到帧的长度字节
			
			byte [] frameLengthBytes = new byte[pushFrameLength.byteLength()];
			readBuf.get(frameLengthBytes);
			// 帧长度大小
			bytesToRead = pushFrameLength.bytesToLength(frameLengthBytes);
			
			if( readBuf.remaining() < bytesToRead ){
			// 没有完整读到所有帧的内容,应继续读取	
				
				if(readBuf.limit() == readBuf.capacity() ){
				// buffer 长度不够 ，需扩展buffer
					int oldBufLength = readBuf.capacity();
					int newBufLength = bytesToRead + pushFrameLength.byteLength() ;
					ByteBuffer newBuffer = ByteBuffer.allocateDirect(newBufLength);
					
					readBuf.position(0);
					newBuffer.put(readBuf);
					
					readBuf = newBuffer;
					
					readBuf.position(oldBufLength); 
					readBuf.limit(newBufLength); 
					
					return ;
				}
				
			}
			
			
		}else{
			//
			// 没读到帧长度字节，调整buffer，继续读取
			// 注意postion 与 limit 设置
			// 接上次位置，继续读取
			// 
			readBuf.position(readBuf.limit());
			readBuf.limit(readBuf.capacity());
			return;
		}
		
		// 拆帧完毕
		
		byte [] frame  =  new byte[bytesToRead];
		readBuf.get(frame);
		
		System.out.println(Arrays.toString(frame));
		
		
		
	}
	
}
