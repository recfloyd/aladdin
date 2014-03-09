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

public class CopyOfServer {
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

	private static class SocketReader {
		private static int counter = 0;
		ByteBuffer receiver;
		ByteBuffer messageBuffer;
		Byte high;
		Byte low;
		byte status;
		int toBeRead;
		int hasRead;
		String client;
		int port;
		SocketChannel socketChannel;
		Charset charset;

		SocketReader(SocketChannel socketChannel) {
			this.socketChannel = socketChannel;
			client = socketChannel.socket().getInetAddress().getHostAddress();
			port = socketChannel.socket().getPort();
			receiver = ByteBuffer.allocate(8192);
			messageBuffer = ByteBuffer.allocate(8192);
			status = 0;
			toBeRead = 0;
			hasRead = 0;
			charset = Charset.forName("UTF-8");
		}

		void reset() {
			status = 0;
			messageBuffer.clear();
			high = null;
			low = null;
			toBeRead = 0;
			hasRead = 0;
		}

		boolean process() throws IOException {
			if (!socketChannel.isConnected())
				throw new IOException("socket通道已经关闭了");
			receiver.clear();
			int count = socketChannel.read(receiver);
			if (count > 0) {
				receiver.flip();
				while (receiver.position() < count) {
					// 1.判断当前出于什么状态
					// 2.如果出于初始状态,那么读取high和low,确定消息体的长度
					// 3.如果出于就绪状态,那么读取消息体,并存入消息缓存中
					// 4.如果处于完毕状态,那么校验最后一个空字节,然后重新回到初始状态
					switch (status) {
					case 0:
						if (high == null)
							high = receiver.get();
						else if (low == null)
							low = receiver.get();
						else {
							toBeRead += (high & 0xff) << 8;
							toBeRead += (low & 0xff);
							status = 1;
						}
						break;
					case 1:
						int maxRead = Math.min(toBeRead - hasRead,
								receiver.limit() - receiver.position());
						for (int i = 0; i < maxRead; i++) {
							messageBuffer.put(receiver.get());
							hasRead++;
						}

						if (hasRead == toBeRead) {
							String msg = client
									+ ":"
									+ port
									+ "\t"
									+ new String(messageBuffer.array(), 0,
											hasRead, charset);
							System.out.println((++counter) + ":" + msg);
							status = 2;
						}
						break;
					case 2:
						if (receiver.get() != 0) {
							throw new IOException("说好的空白结束符呢");
						} else {
							reset();
						}
						break;
					}
				}
				return true;
			} else {
				// System.out.println("socket通道应该关闭了");
				reset();
				return false;
			}
			// else if (count == 0) {
			// System.out.println("socket通道已读完 0,重置");
			// reset();
			// return true;
			// } else {
			// socketChannel.close();
			// System.out.println("socket通道已读完 -1,重置");
			// reset();
			// return false;
			// }
		}
	}

	private static void startup() throws IOException {
		Selector selector = getSelector();
		while (true) {
			if (selector.select() == 0)
				continue;
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();

				if (key.isAcceptable()) {
					SocketChannel sc = ((ServerSocketChannel) key.channel())
							.accept();
					sc.configureBlocking(false);
					SocketReader sr = new SocketReader(sc);
					sc.register(selector, SelectionKey.OP_READ, sr);
				} else if (key.isReadable()) {
					SocketReader sr = (SocketReader) key.attachment();
					if (!sr.process()) {
						SocketChannel sc = (SocketChannel) key.channel();
						Socket socket = sc.socket();
						socket.close();
						sc.close();
					}
				} else
					System.out.println("############################");
			}
		}
	}

	public static void main(String[] args) throws IOException {
		startup();
	}
}
