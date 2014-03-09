package com.letv.shop.aladdin.server.poster;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
	private static int port = 65000;

	private static Selector getSelector() throws IOException {
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress(port));
		Selector selector = Selector.open();
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("已开启监听");
		return selector;
	}

	private static void startup() throws IOException {
		Selector selector = getSelector();
		Charset charset = Charset.forName("UTF-8");
		ByteBuffer receiver = ByteBuffer.allocate(8192);
		ByteBuffer message = ByteBuffer.allocate(8192);
		int count = 0;
		while (true) {
			selector.select();
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();

				if (key.isAcceptable()) {
					SocketChannel sc = ((ServerSocketChannel) key.channel())
							.accept();
					sc.configureBlocking(false);
					sc.register(selector, SelectionKey.OP_READ);
				} else if (key.isReadable()) {
					SocketChannel sc = (SocketChannel) key.channel();
					Socket socket = sc.socket();
					String client = socket.getInetAddress().getHostAddress();
					int port = socket.getPort();
					receiver.clear();
					count = sc.read(receiver);
					if (count > 0) {
						String msg = client
								+ ":"
								+ port
								+ "\t"
								+ new String(receiver.array(), 0, count,
										charset);

						System.out.println(msg);
						sc.register(selector, SelectionKey.OP_READ);
					} else
						System.out.println("############################");
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {

		startup();
	}
}
