package cn.flaty.pushFrame;

import cn.flaty.utils.ByteUtil;

public class SimpleFourBytesPushFrameLength implements PushFrameLength{

	private final int NUM_BYTES = 4;
	private final int MAX_LENGTH = Integer.MAX_VALUE;
	
	@Override
	public int byteLength() {
		return NUM_BYTES;
	}

	@Override
	public int maxLength() {
		return MAX_LENGTH;
	}

	@Override
	public int bytesToLength(byte[] bytes) {
		if (bytes.length != NUM_BYTES) {
			throw new IllegalStateException("Wrong number of bytes, must be "+NUM_BYTES);
		}
		return ByteUtil.byteArrayToInt(bytes);
	}

	@Override
	public byte[] lengthToBytes(int length) {
		return ByteUtil.intToByteArray(length);
	}


}
