package cn.flaty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.flaty.PushFrameParser;
import cn.flaty.model.ClientInfo;
import cn.flaty.model.GenericMessage;

import com.alibaba.fastjson.JSON;

public class AcceptHandler {

	private Logger log = LoggerFactory.getLogger(AcceptHandler.class);

	public void connect(Selector selector, SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		// 如果正在连接，则完成连接
		if (channel.isConnectionPending()) {
			channel.finishConnect();
		}
		// 设置成非阻塞
		channel.configureBlocking(false);
		// 在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。
		channel.register(selector, SelectionKey.OP_READ);
	   // channel.register(selector, SelectionKey.OP_WRITE);

	}

}
