package cn.flaty.pushFrame;

import java.nio.ByteBuffer;
import java.util.Arrays;

import cn.flaty.PushConstant;

public class PushFrameDecoder {
	
	
	private static int FRAMEBUF = 256;
	
	private StringBuilder sb = null;
	
	private boolean isFinlsh = false;
	
	private byte[] frameBuf = new byte[FRAMEBUF];
	
	private byte[] frameLength = new byte[PushConstant.frame_header_bytes];
	
	private byte[] frameHeader = new byte[PushConstant.real_header_bytes];
	
	public void decode(ByteBuffer buf){
		
		
		
		//frameLength = Arrays.copyOf(frameBuf, 4);
		//frameHeader = Arrays.copyOf(frameBuf, 4);
		
		
		System.out.println(Arrays.toString(frameBuf));
	}

}
