package cn.flaty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;

import cn.flaty.core.PushService;
import cn.flaty.core.PushServiceImpl;
import cn.flaty.core.PushSupport;
import cn.flaty.nio.ReadWriteHandler;
import cn.flaty.nio.SimpleEventLoop;

public class ClientMain {
	
	
	private String host;
	private int port;
	private int threads;
	
	/**
	 * 启动客户端测试
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new ClientMain().start();
	}

	
	public void start(){
		Properties p = new Properties();
		try {
			p.load(this.getClass().getClassLoader().getResourceAsStream("client.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		host = p.getProperty("local.host");
		port = Integer.parseInt(p.getProperty("local.port"));
		threads = Integer.parseInt(p.getProperty("local.threads"));
		
		
		
		PushServiceImpl pushService = new PushServiceImpl();
		pushService.startUp(host, port);
		
	}
}
