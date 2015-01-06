package cn.flaty.nio;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CompositByteBuf {

	private static int buffer_size = 256;

	private List<ByteBuffer> buffers ;

	
	
	public CompositByteBuf() {
		super();
		this.buffers = new ArrayList<ByteBuffer>(5);
	}

	public final CompositByteBuf clear() {
		return this;
	}

	public final CompositByteBuf flip() {
		return this;
	}

	public final CompositByteBuf position() {
		return this;
	}

	public final CompositByteBuf limit() {
		return this;
	}

	public final CompositByteBuf remaining() {
		return this;
	}

	public final CompositByteBuf capacity() {
		return this;
	}

	public final CompositByteBuf put(byte b[]) {
		return this;
	}

	public final CompositByteBuf get(byte b[]) {
		return this;
	}

}
