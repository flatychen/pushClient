package base;

import java.util.Arrays;

import org.junit.Test;

import cn.flaty.PushFrameParser;

public class PushFrameParserTest {
	
	
	@Test
	public void test(){
		PushFrameParser p = new PushFrameParser();
		byte [] r = p.encode("abcd");
		System.out.println(Arrays.toString(r));
	
	}

}
