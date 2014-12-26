package base;

import org.junit.Test;

public class ByteUtilsTest {
	
	
	@Test
	public void test(){
		byte[] b = new byte[4];
		
		b[0] = 0;
		b[1] = 0;
		b[2] = 0;
		b[3] = 1;
		
		int i = cn.flaty.utils.ByteUtil.byteArrayToInt(b);
		System.out.println(i);
	}

}
