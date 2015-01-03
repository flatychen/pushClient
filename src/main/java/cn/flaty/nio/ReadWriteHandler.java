package cn.flaty.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.PushConstant;
import cn.flaty.PushFrameParser;
import cn.flaty.pushFrame.FrameHead;
import cn.flaty.pushFrame.SimplePushHead;
import cn.flaty.utils.ByteUtil;

public class ReadWriteHandler {
	
	private Logger log = LoggerFactory.getLogger(ReadWriteHandler.class);
	
	private static int BUFFER_SIZE = 4096;
	
	private FrameHead frameHeader = null;
	
	private ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
	
	private ByteBuffer writeBuf = ByteBuffer.allocate(BUFFER_SIZE);
	

	public ReadWriteHandler() {
		this(new SimplePushHead());
	}
	

	public ReadWriteHandler(FrameHead frameHeader) {
		super();
		this.frameHeader = frameHeader;
	}

	public void write(Selector selector, SelectionKey key) {
		SocketChannel socketChannel = this.getChannel(key);
		writeBuf.clear();
		String s = RandomStringUtils.randomAlphabetic(20);
		s = " 中国人 ";
		byte [] frame = encodeFrame(s);
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
	
	
	private byte[] encodeFrame(String s){
		byte[] body = s.getBytes();
		int bodyLength = body.length;
		byte[] frame = new byte[bodyLength + frameHeader.byteLength() + frameHeader.headLength()];
		
		
		// 添加长度
		byte [] header_len = ByteUtil.intToByteArray(bodyLength);
		System.arraycopy(header_len, 0, frame, 0, frameHeader.byteLength() );
		
		
		// 添加帧头，4字节
		byte [] header = new byte[frameHeader.headLength()];
		// utf 编码
		header[1] = 1; 
		System.arraycopy(header, 0, frame, frameHeader.byteLength() , frameHeader.headLength());
	
		// 添加内容
		System.arraycopy(body, 0, frame, frameHeader.byteLength() + frameHeader.headLength(), bodyLength);
		
		return frame;
	}
	
	public void read(Selector selector, SelectionKey key) throws IOException {
		SocketChannel channel = this.getChannel(key);
		
		readBuf.clear();
		if(channel.read(readBuf) == -1 ){
			throw new IOException("----> 读取失败");
		}
		
		// 切包
		byte [] frame = this.splitFrame();
		
		// 解析包头
		byte [] body = this.dencodeFrame(frame);
		
		
		
		channel.register(selector, SelectionKey.OP_READ);
	}



	private byte[] dencodeFrame(byte[] frame) {
		head = new byte[frameHead.headLength()];
		body = new byte[frame.length - frameHead.headLength() ];
		byte head[] = 
		return null;
	}


	private SocketChannel getChannel(SelectionKey key){
		return  (SocketChannel)key.channel();
	}
	
	
	
	/**
	 * 
	 * FIXME 扩展buf
	 * @return
	 */
	public byte[] splitFrame(){
		readBuf.flip();
		
		// 
		// 拆帧开始;
		// 如果读取的字节大于帧头长度，则一直等到长度为止，
		// 否则，一直读等到帧头长度大于为止;
		// 
		int bytesToRead = 0; 
		
		if( readBuf.remaining() > frameHeader.byteLength() ){
		// 	必须读到帧的长度字节
			
			byte [] frameLengthBytes = new byte[frameHeader.byteLength()];
			readBuf.get(frameLengthBytes);
			// 帧长度大小
			bytesToRead = frameHeader.byteLength();
			
			if( readBuf.remaining() < bytesToRead ){
			// 没有完整读到所有帧的内容,应继续读取	
				
				if(readBuf.limit() == readBuf.capacity() ){
				// buffer 长度不够 ，需扩展buffer
					
					// 注意后期抽象
					int oldBufLength = readBuf.capacity();
					int newBufLength = bytesToRead + frameHeader.byteLength() ;
					ByteBuffer newBuffer = ByteBuffer.allocateDirect(newBufLength);
					
					readBuf.position(0);
					newBuffer.put(readBuf);
					
					readBuf = newBuffer;
					
					readBuf.position(oldBufLength); 
					readBuf.limit(newBufLength); 
					
					return null;
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
			return null;
		}
		
		// 拆帧完毕
		
		byte [] frame  =  new byte[bytesToRead];
		readBuf.get(frame);
//		System.out.println(Arrays.toString(frame));
		return frame;
		
		
	}
	
}
