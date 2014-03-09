package com.letv.shop.aladdin.server.poster.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;

public class Server {
	public static void main(String[] args) throws IOException {
		Selector selector = Selector.open();
		DatagramChannel dc = DatagramChannel.open();
		dc.configureBlocking(false);
		dc.bind(new InetSocketAddress(65000));
		System.out.println("服务端已经启动");
		dc.register(selector, SelectionKey.OP_READ);

		Charset charset = Charset.forName("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate(10240);

		while (true) {
			selector.select();
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();
				if (key.isReadable()) {
					DatagramChannel channel = (DatagramChannel) key.channel();
					bb.clear();
					SocketAddress address=channel.receive(bb);
					if(address==null){
						System.out.println("空");
					}else{
						String msg = new String(bb.array(), 0, bb.position(), charset);
						System.out.println(msg);
					}
				}
			}
		}
	}
}
