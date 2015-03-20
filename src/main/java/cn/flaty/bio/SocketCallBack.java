package cn.flaty.bio;

import java.io.IOException;

public interface SocketCallBack {

	void onError(IOException e);

	void onConnect();

	void onReceice(byte[] b);

	void disConnect();
}
