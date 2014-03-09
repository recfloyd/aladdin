package com.letv.shop.aladdin.server.poster;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class Client {
	private static int port = 65000;

	private static void send() throws IOException {
		SocketChannel sc = SocketChannel.open();
		sc.configureBlocking(false);
		Selector selector = Selector.open();
		sc.register(selector, SelectionKey.OP_CONNECT);
		sc.connect(new InetSocketAddress("localhost", port));

		Charset charset = Charset.forName("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate(10240);
		while (true) {
			selector.select();
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();
				if (key.isConnectable()) {
					SocketChannel channel = (SocketChannel) key.channel();
					if (channel.isConnectionPending()) {
						channel.finishConnect();
						System.out.println("完成连接!");
						bb.clear();
						bb.put("Hello,Server".getBytes(charset));
						bb.flip();
						channel.write(bb);
					}
					channel.register(selector, SelectionKey.OP_READ);
				}

			}
		}
	}

	public static void main(String[] args) throws IOException {
		send();
	}
}
