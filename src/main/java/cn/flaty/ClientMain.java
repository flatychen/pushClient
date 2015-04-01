package cn.flaty;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.LoggerFactory;

import cn.flaty.bio.SingleThreadSocketIO;
import cn.flaty.bio.SocketCallBackAdapator;
import cn.flaty.bio.SocketIO;

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
		StopWatch sw = new StopWatch();
		sw.start();
		for (int i = 0; i < connections; i++) {
			es.submit(new Runnable() {
				@Override
				public void run() {
					SocketIO boot = new SingleThreadSocketIO(server, port);
					boot.connect(new SimpleSocketCallBackAdapator());
					boot.send("bio 连接测试");
				}
			});
		}
		sw.stop();
		
		System.out.println("时间:"+sw.toString());
		// wait
		try {
			//Thread.currentThread().join();
			Thread.sleep(1000 * 60 * 3600 * 5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}

	
	private static class SimpleSocketCallBackAdapator extends SocketCallBackAdapator{
		
		private static org.slf4j.Logger log = LoggerFactory.getLogger(SimpleSocketCallBackAdapator.class);
		
		@Override
		public void onError(IOException e) {
			log.error(e.getMessage());
		}

		@Override
		public void onReceice(byte b[]) {
			
		}
		
		@Override
		public void onConnect(SocketAddress socketAddress) {
			log.info("{}连接成功",socketAddress.toString());
		}


		@Override
		public void disConnect(SocketAddress socketAddress) {
			
		}

	}
	
	
}
