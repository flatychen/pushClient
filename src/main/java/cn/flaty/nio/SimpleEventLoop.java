package cn.flaty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SimpleEventLoop {
	
	private InetSocketAddress socket;

	private AcceptHandler accept;
	
	private ReadWriteHandler readWrite;
	
	private Selector selector;
	
	
	public SimpleEventLoop(String host, int port) {
		super();
		this.socket = new InetSocketAddress(host,port);
		this.accept = new AcceptHandler();
		this.readWrite = new ReadWriteHandler();
	}

	

	private void openChannel() throws IOException {
		// 获得一个Socket通道
		SocketChannel channel = SocketChannel.open();
		// 设置通道为非阻塞
		channel.configureBlocking(false);
		// 获得一个通道管理器
		this.selector = Selector.open();
		// 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调
		// 用channel.finishConnect();才能完成连接
		channel.connect(this.socket);
		// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。
		channel.register(selector, SelectionKey.OP_CONNECT);
	}

	public void connect() throws IOException {
		this.openChannel();
		// 轮询访问selector
		while (true) {
			selector.select();
			// 获得selector中选中的项的迭代器
			Iterator<SelectionKey> ite = this.selector.selectedKeys().iterator();
			while (ite.hasNext()) {
				SelectionKey key = (SelectionKey) ite.next();
				// 删除已选的key,以防重复处理
				ite.remove();
				if (key.isConnectable()) {
					accept.connect(selector, key);
				} else if (key.isReadable()) {
					readWrite.read(selector ,key);
				} else if (key.isWritable()) {
					readWrite.write(selector,key);
				}

			}

		}
	}



	
	
	
}
