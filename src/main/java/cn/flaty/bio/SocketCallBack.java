package cn.flaty.bio;

import java.io.IOException;
import java.net.SocketAddress;

public interface SocketCallBack {

	void onError(IOException e);

	void onConnect(SocketAddress socketAddress);

	void onReceice(byte[] b);

	void disConnect(SocketAddress socketAddress);
}
