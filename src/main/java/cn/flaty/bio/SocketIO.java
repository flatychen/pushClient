package cn.flaty.bio;

public interface SocketIO {

	public abstract void connect(SocketCallBack callBack);

	public abstract void send(String msg);

	public abstract void close();

}