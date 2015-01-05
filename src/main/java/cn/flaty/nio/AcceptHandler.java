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

	private Selector selector;

	private SocketChannel channel;

	private Logger log = LoggerFactory.getLogger(AcceptHandler.class);

	public void connect(Selector s, SelectionKey key) throws IOException {
		this.selector = s;
		this.channel = (SocketChannel) key.channel();

		// 如果正在连接，则完成连接
		if (channel.isConnectionPending()) {
			channel.finishConnect();
		}
		
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
//		channel.register(selector, SelectionKey.OP_WRITE);
		log.info("----> 连接已建立");

	}
	
	
	public static interface AfterAcceptListener{
		void success();
		void fail();
	}

}
