package cn.flaty.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.utils.AssertUtils;

public class SingleThreadSocketIO implements SocketIO {
	
	private Logger log = LoggerFactory.getLogger(SingleThreadSocketIO.class);
	
	private SocketAddress socketAddress;

	private static int DEFAULTTIMEOUT = 3000;
	
	
	private SocketCallBack socketCallBack;
	
	private int timeOut;
	
	private Socket socket;
	
	private ReadWriteHandler readWriteHandler;
	
	public SingleThreadSocketIO(String host,int port,int timeOut) {
		super();
		this.timeOut = timeOut;
		this.socketAddress = new InetSocketAddress(host, port);
	}
	
	public SingleThreadSocketIO(String host,int port) {
		this(host, port,DEFAULTTIMEOUT);
	}
	
	
	@Override
	public void connect(SocketCallBack callBack){
		
		AssertUtils.notNull(callBack," callBack对象不能为空  ");
		this.socketCallBack = callBack;
		
		try {
			socket.connect(socketAddress, timeOut);
		} catch (IOException e) {
			callBack.onError(e);
			return ;
		}
		socketCallBack.onConnect();
		
		try {
			readWriteHandler = new ReadWriteHandler(socket.getInputStream(), socket.getOutputStream(),callBack);
			readWriteHandler.init();
		} catch (IOException e) {
			socketCallBack.onError(e);
		}
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see cn.flaty.bio.SocketIO#send(java.lang.String)
	 */
	@Override
	public void send(String msg){
		try {
			readWriteHandler.send(msg);
		} catch (IOException e) {
			socketCallBack.onError(e);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see cn.flaty.bio.SocketIO#close()
	 */
	@Override
	public void close(){
		try {
			this.socket.close();
		} catch (IOException e) {
			socketCallBack.onError(e);
		}
		socketCallBack.disConnect();
	}
	
	
	
}
