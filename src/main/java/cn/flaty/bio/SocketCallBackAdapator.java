package cn.flaty.bio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class SocketCallBackAdapator  implements SocketCallBack{

	@Override
	public void onError(IOException e) {
		e.printStackTrace();
	}


	@Override
	public void onReceice(byte b[]) {
		
	}


	@Override
	public void onConnect(SocketAddress socketAddress) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void disConnect(SocketAddress socketAddress) {
		// TODO Auto-generated method stub
		
	}




}
