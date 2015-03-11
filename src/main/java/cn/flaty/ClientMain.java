package cn.flaty;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.flaty.core.PushServiceImpl;

public class ClientMain {

	private String host;
	private int port;
	private int threads;
	private int connections;


	/**
	 * 启动客户端测试
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new ClientMain().init().startUp();
	}

	public ClientMain init() {
		Properties p = new Properties();
		try {
			p.load(this.getClass().getClassLoader()
					.getResourceAsStream("client.properties"));
			host = p.getProperty("local.host");
			port = Integer.parseInt(p.getProperty("local.port"));
			threads = Integer.parseInt(p.getProperty("local.threads"));
			connections = Integer.parseInt(p.getProperty("local.connections"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;

	}

	public void startUp() {
		for (int i = 0; i < connections; i++) {
			PushServiceImpl pushService = new PushServiceImpl();
			pushService.connect(host, port, threads);
		}
	}

}
