package cn.flaty;

import java.util.Arrays;

import cn.flaty.utils.ByteUtil;


public class PushFrameParser {
	
	
	
	public byte[] encode(String frame){
//		return null;
		if(frame == null || frame.length() == 0 ){
			new IllegalArgumentException("---->消息不能为空");
			return null;
		}
		int len = frame.getBytes().length;
		if(len > Integer.MAX_VALUE){
			new IllegalArgumentException("---->消息内容太长"); 
		}
		
		byte [] newFrame = new byte[len + PushConstant.header_Len_bytes];
	
		// 帧头，4字节
		byte [] header = new byte[PushConstant.header_Len_bytes];
		// 添加长度
		byte [] header_len = ByteUtil.intToByteArray(len);
		
		System.arraycopy(header_len, 0, header, 0, header_len.length);
		
		// utf 编码
	//	header[header_len.length +1] = 1; 
		
		
		System.arraycopy(header, 0, newFrame, 0, PushConstant.header_Len_bytes);
	
		// 添加内容
		System.arraycopy(frame.getBytes(), 0, newFrame, PushConstant.header_Len_bytes, len);
		
		return newFrame;
				
	} 

}
