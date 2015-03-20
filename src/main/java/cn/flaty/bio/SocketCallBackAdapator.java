package cn.flaty.bio;

import java.io.IOException;

public class SocketCallBackAdapator  implements SocketCallBack{

	@Override
	public void onError(IOException e) {
		e.printStackTrace();
	}

	@Override
	public void onConnect() {
	}

	@Override
	public void onReceice(byte b[]) {
		
	}

	@Override
	public void disConnect() {
	}

}
