package cn.flaty;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cn.flaty.bio.SingleThreadSocketIO;
import cn.flaty.bio.SocketCallBackAdapator;
import cn.flaty.bio.SocketIO;
import cn.flaty.core.PushServiceImpl;

public class ClientMain {

	private String server;
	private int port;
	private int threads;
	private int connections;


	private ExecutorService es ;
	
	/**
	 * 启动客户端测试
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new ClientMain().init().startUp();
	}

	
	
	
	private ClientMain init() {
		Properties p = new Properties();
		try {
			p.load(this.getClass().getClassLoader()
					.getResourceAsStream("client.properties"));
			server = p.getProperty("local.server");
			port = Integer.parseInt(p.getProperty("local.port"));
			threads = Integer.parseInt(p.getProperty("local.threads"));
			connections = Integer.parseInt(p.getProperty("local.connections"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		es = Executors.newFixedThreadPool(threads);
		
		return this;

	}

	private void startUp() {
		for (int i = 0; i < connections; i++) {
			es.submit(new Runnable() {
				@Override
				public void run() {
					SocketIO boot = new SingleThreadSocketIO(server, port);
					boot.connect(new SocketCallBackAdapator(){});
					boot.send("test");
				}
			});
		}
	
		
		// wait
		try {
			Thread.currentThread().join();
			Thread.sleep(1000 * 60 * 3600 * 5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}

}
