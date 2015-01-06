package cn.flaty.nio;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import cn.flaty.utils.ByteBufUtil;

public class CompositByteBuf implements ByteBuf {

	private int capacity = 0;

	private int position = 0;

	private int limit = 0;

	private int currentBufferIndex = 0;

	private int buffersSize = 0;


	private List<ByteBuf> buffers;

	
	public CompositByteBuf() {
		this(1, ByteBufUtil.ByteBuf());
	}
	
	
	public CompositByteBuf(ByteBuf buf) {
		this(1, buf);
	}

	public CompositByteBuf(int compontSize, ByteBuf buf) {
		super();
		if (compontSize < 1) {
			throw new IllegalArgumentException("----> size非法，至少大于1 ");
		}
		this.buffers = new ArrayList<ByteBuf>();
		buffers.add(buf);
		this.addCompont(compontSize);
		this.buffersSize = compontSize + 1;
		this.capacity = this.limit = BUFFER_SIZE * compontSize + buf.capacity();
		this.position = buf.position();
		this.currentBufferIndex = 1;
	}

	private void addCompont(int size) {
		for (int i = 0; i < size; i++) {
			this.buffers.add(new SimpleByteBuf(BUFFER_SIZE));
		}
	}

	public final CompositByteBuf clear() {
		for (ByteBuf buffer : buffers) {
			buffer.clear();
		}
		return this;
	}

	public final CompositByteBuf flip() {
		limit = position;
		position = 0;
		this.currentBufferIndex = 0;
		for (ByteBuf buf : this.buffers) {
			buf.flip();
		}
		return this;
	}

	public final ByteBuf position(int i) {
		if ((i > limit) || (i < 0)) {
			throw new IllegalArgumentException("----> position非法");
		}
		int _needToSize = i / BUFFER_SIZE;
		int _offset = i % BUFFER_SIZE;
		if ( _needToSize != 0 ){
			this.currentBufferIndex = _offset == 0 ? _needToSize : _needToSize + 1;
		}else{
			this.currentBufferIndex = 0;
		}
		this.position = i;

		return this;
	}

	public final int position() {
		return this.position;
	}

	public final CompositByteBuf limit(int i) {
		if ((i > capacity) || (i < 0))
			throw new IllegalArgumentException();
		this.limit = i;
		return this;
	}

	public final int limit() {
		return this.limit;
	}

	public final int remaining() {
		return this.limit() - this.position();
	}

	public final int capacity() {
		return this.capacity;
	}

	public final CompositByteBuf put(byte b[]) {


		this.buffers.get(buffersSize).put(b);
		return this;
	}

	public final CompositByteBuf get(byte b[]) {
		this.get(b, 0, b.length);
		return this;
	}

	@Override
	public final int nioBufferSize() {
		return buffersSize;
	}

	@Override
	public final ByteBuffer nioBuffer() {
		return null;
	}

	@Override
	public final ByteBuffer[] nioBuffers() {
		ByteBuffer[] bufs = new ByteBuffer[buffersSize - currentBufferIndex];
		for (int i = currentBufferIndex,j = 0; i < buffersSize; i++,j++) {
			bufs[j] = buffers.get(i).nioBuffer();
		}
		return bufs;
	}

	@Override
	public final ByteBuf get(byte[] dst, int offset, int length) {
		if(length > this.remaining()){
			throw new BufferOverflowException();
		}
		
		int _remaining = 0;
		int _offset = 0;
		int _lastLength = length;
		
		int i = this.currentBufferIndex;
		for(; i < this.buffersSize ; i++ ){
			ByteBuf buf = buffers.get(i);
			_remaining = buf.remaining();
			
			// 
			//判断当前所处buffer含有dst 长度所有数据，否则遍历其它buffer继续get
			//
			if(_lastLength < _remaining){
				buf.get(dst,_offset,_lastLength);
				break;
			}
				
			buf.get(dst,_offset,_remaining);
			
			_offset = _offset + _remaining;
			_lastLength = _lastLength - _remaining;
		}
		
		
		this.position(length);
		return this;
	}
	
	

}
