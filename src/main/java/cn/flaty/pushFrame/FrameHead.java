package cn.flaty.pushFrame;


public interface  FrameHead {
	

	/**
	 * 用于切包的，包长度所占字节数
	 * @return
	 */
	int byteLength();

	/**
	 * 包最大支持的报文长度
	 * @return
	 */
	int maxLength();

	

	/**
	 * 包头部所占字节
	 * @return
	 */
	int headLength();
	
	/**
	 * 包长度转换
	 * @param b
	 * @return
	 */
	int bytesToInt(byte []b);
	
	/**
	 * 包长度转换
	 * @param length
	 * @return
	 */
	byte[] intToBytes(int length);
}
