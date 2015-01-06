package cn.flaty.nio;

import java.nio.ByteBuffer;


public interface ByteBuf {


	public  ByteBuf clear() ;

	public  ByteBuf flip();

	public ByteBuf  position(int i) ;

	public int  position() ;
	
	public ByteBuf  limit(int i) ;

	public int  limit() ;
	
	public int  remaining() ;

	public int  capacity();

	public  ByteBuf put(byte b[]);
	
	public  ByteBuf get(byte b[]);
	
	
	public int nioBufferSize();
	
	public ByteBuffer nioBuffer();
	
	public ByteBuffer[] nioBuffers();
	

}
