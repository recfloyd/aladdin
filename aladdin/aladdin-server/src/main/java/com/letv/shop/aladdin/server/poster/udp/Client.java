package com.letv.shop.aladdin.server.poster.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;

public class Client {
	public static void main(String[] args) throws IOException {
		Selector selector = Selector.open();
		DatagramChannel dc = DatagramChannel.open();
		dc.configureBlocking(false);
		dc.connect(new InetSocketAddress("localhost", 65000));
		dc.register(selector, SelectionKey.OP_WRITE);

		Charset charset = Charset.forName("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate(10240);

		while (true) {
			selector.select();
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();
				if (key.isWritable()) {
					DatagramChannel channel = (DatagramChannel) key.channel();
					bb.clear();
					bb.put("Hello".getBytes(charset));
					bb.flip();
					channel.write(bb);
				}
			}
		}
	}
}
