package cn.flaty;

import java.io.IOException;
import java.util.Properties;

import cn.flaty.bio.SingleThreadSocketIO;
import cn.flaty.bio.SocketIO;
import cn.flaty.core.PushServiceImpl;

public class ClientMain {

	private String server;
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
			server = p.getProperty("local.server");
			port = Integer.parseInt(p.getProperty("local.port"));
			threads = Integer.parseInt(p.getProperty("local.threads"));
			connections = threads;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;

	}

	public void startUp() {
		for (int i = 0; i < connections; i++) {
			SocketIO boot = new SingleThreadSocketIO(server, port);
		}
	}

}
