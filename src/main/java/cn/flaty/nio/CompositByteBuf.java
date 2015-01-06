package cn.flaty.nio;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositByteBuf implements ByteBuf{

	private int capacity = 0;
	
	private int buffersSize = 0;
	
	private static int BUFFER_SIZE = 128;

	private List<ByteBuffer> buffers ;

	
	
	public CompositByteBuf() {
		super();
		this.buffers = new ArrayList<ByteBuffer>(5);
		this.buffers.add(ByteBuffer.allocate(BUFFER_SIZE));
		this.buffersSize = 1;
		this.capacity = BUFFER_SIZE;
	}
	
	

	public CompositByteBuf allocateBuf(){
		return new CompositByteBuf();
	}
	
	public final CompositByteBuf clear() {
		for (ByteBuffer buffer : buffers) {
			buffer.clear();
		}
		return this;
	}

	public final CompositByteBuf flip() {
		return this;
	}

	public CompositByteBuf  position(int i) {
		return this;
	}

	public int  position() {
		return 0;
	}
	
	public CompositByteBuf  limit(int i) {
		return this;
	}

	public int  limit() {
		return 0;
	}
	
	public int  remaining() {
		return this.limit() - this.position();
	}

	public int  capacity() {
		return this.capacity;
	}

	public final CompositByteBuf put(byte b[]) {
		int needToExtend = this.ensureBufferSize(b.length);
		
		// 空量不够 扩容buf，并填充
		if(needToExtend > 0){
			Arrays.copyOf(original, newLength)
			this.buffers.get(buffersSize).put();
			
			for (int i = 0; i < needToExtend; i++) {
				this.buffers.add(ByteBuffer.allocate(BUFFER_SIZE));
			}
		}
		
		this.buffers.get(buffersSize).put(b);
		return this;
	}

	public final CompositByteBuf get(byte b[]) {
		return this;
	}
	
	
	private int ensureBufferSize(int byteLength){
		int _remain = this.remaining();
		// 容量足够
		if(byteLength <= _remain){
			return 0;
		}
		int _needLength = byteLength - _remain;
		int sizeToExtend = 0 ;
		if(byteLength % BUFFER_SIZE == 0){
			sizeToExtend = _needLength / BUFFER_SIZE;
		}else{
			sizeToExtend = (_needLength / BUFFER_SIZE) + 1;
		}
		return sizeToExtend;
	}



	@Override
	public int nioBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	public ByteBuffer nioBuffer() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public ByteBuffer[] nioBuffers() {
		// TODO Auto-generated method stub
		return null;
	}

}
