package com.letv.shop.aladdin.client.reporter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异步的TCP协议发报器
 * 
 * @author lijia
 * 
 */
public class TcpReporter extends AsyncQueuedReporter {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TcpReporter.class);

	/**
	 * TCP发报器
	 * 
	 * @author lijia
	 * 
	 */
	private static class TcpSender implements Sender {
		/**
		 * 配置文件的位置
		 */
		static final String CONFIG_FILE = "conf/aladdin_client_tcp.properties";
		/**
		 * 默认配置文件的位置
		 */
		static final String DEFAULT_CONFIG_FILE = "conf/aladdin_client_tcp_default.properties";
		/**
		 * 发报所用的socket
		 */
		Socket socket;
		/**
		 * 服务端ip
		 */
		String host;
		/**
		 * 服务端端口
		 */
		int port;
		/**
		 * 连接超时,只有正数有意义
		 */
		int coTimeout;
		/**
		 * 发送缓冲,只有正数有意义
		 */
		int sendBuffer;
		/**
		 * 关闭延时,只有正数有意义
		 */
		int soLinger;
		/**
		 * 连接和发报重试次数,只有正数有意义
		 */
		int retryTimes;
		/**
		 * 重试时的延时基础值,如果为1000,那么第一次重试延时1000毫秒,第二次2000毫秒,第三次4000毫秒
		 */
		long initRetryMillis;

		@Override
		public void init() {
			Properties prop = new Properties();
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			InputStream is = cl.getResourceAsStream(CONFIG_FILE);
			if (is != null) {
				LOGGER.info("a tcp config file {} found", CONFIG_FILE);
			} else {
				LOGGER.warn("no tcp config file {} found, use default {}",
						CONFIG_FILE, DEFAULT_CONFIG_FILE);
				is = cl.getResourceAsStream(DEFAULT_CONFIG_FILE);
			}
			if (is == null) {
				LOGGER.error("no tcp config file found, cannot init tcp sender");
				return;
			} else {
				try {
					prop.load(is);
				} catch (IOException e) {
					LOGGER.error("cannot loading tcp config file");
					return;
				} finally {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}

			host = prop.getProperty("host");
			port = Integer.parseInt(prop.getProperty("port"));
			coTimeout = Integer.parseInt(prop.getProperty("coTimeout", "-1"));
			sendBuffer = Integer.parseInt(prop.getProperty("sendBuffer", "-1"));
			soLinger = Integer.parseInt(prop.getProperty("soLinger", "-1"));
			retryTimes = Integer.parseInt(prop.getProperty("retryTimes", "-1"));
			initRetryMillis = Long.parseLong(prop.getProperty(
					"initRetryMillis", "1000"));
		}

		@Override
		public void destroy() {
			if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
				}
		}

		/**
		 * 建立TCP连接
		 * 
		 * @return
		 * @throws IOException
		 */
		Socket setupSocket() throws IOException {
			Socket socket = new Socket();
			socket.setKeepAlive(true);
			if (sendBuffer > 0)
				socket.setSendBufferSize(sendBuffer);
			if (soLinger > 0)
				socket.setSoLinger(true, soLinger);
			socket.setTcpNoDelay(true);
			socket.setReuseAddress(true);

			SocketAddress address = new InetSocketAddress(host, port);

			if (coTimeout > 0) {
				socket.connect(address, coTimeout);
			} else {
				socket.connect(address);
			}

			return socket;
		}

		/**
		 * 建立TCP连接,如果配置的话可以进行失败重连
		 * 
		 * @return
		 * @throws IOException
		 */
		Socket trySetupSocket() throws IOException {
			Socket socket = null;
			if (retryTimes > 0) {
				int retryCount = 0;
				boolean successful = false;
				IOException exception = null;
				while (!successful && retryCount < retryTimes)
					try {
						socket = setupSocket();
						successful = true;
					} catch (IOException e) {
						retryCount++;
						LOGGER.error(
								"try setup socket connection {}:{} failure with {} try",
								host, port, retryCount);
						exception = e;
						try {
							if (retryCount - 1 < retryTimes)
								Thread.sleep((2 << (retryCount - 1))
										* initRetryMillis);
						} catch (InterruptedException e1) {
						}
					}
				if (!successful)
					throw new IOException(
							"cannot setup socket connection after "
									+ retryTimes + " trys", exception);
				return socket;
			} else {
				return setupSocket();
			}
		}

		/**
		 * 封包操作,为每条消息多附加3个字节用于封包,目的是为了在长连接的情况下解决粘包问题
		 * 包头以2个字节表示消息体大小(最大支持消息体为65536字节),包尾以一个空白字节表示结束
		 * 
		 * @param message
		 * @return
		 */
		byte[] encode(String message) {
			byte[] messageBytes = message.getBytes(Charset.forName("UTF8"));
			int size = messageBytes.length;
			byte[] bag = new byte[size + 3];
			bag[0] = (byte) ((size >> 8) & 0xff);
			bag[1] = (byte) (size & 0xff);
			for (int i = 0; i < size; i++) {
				bag[i + 2] = messageBytes[i];
			}
			bag[bag.length - 1] = (byte) 0;
			return bag;
		}

		/**
		 * 发送报文,如果配置的话可以进行失败重连重发
		 * 
		 * @param socket
		 * @param message
		 * @return 成功发送报文后,将可用的socket返回
		 * @throws IOException
		 */
		Socket trySendMessage(Socket socket, String message) throws IOException {
			if (retryTimes > 0) {
				int retryCount = 0;
				boolean successful = false;
				IOException exception = null;
				while (!successful && retryCount < retryTimes) {
					try {
						if (socket == null)
							socket = setupSocket();

						socket.getOutputStream().write(encode(message));
						successful = true;
					} catch (IOException e) {
						retryCount++;
						LOGGER.error(
								"try send message within socket connection {}:{} failure with {} try",
								host, port, retryCount);
						exception = e;
						try {
							if (retryCount - 1 < retryTimes)
								Thread.sleep((2 << (retryCount - 1))
										* initRetryMillis);
						} catch (InterruptedException e1) {
						}

						if (socket != null)
							try {
								socket.close();
							} catch (IOException ex) {
							}
						socket = null;
					}
				}
				if (!successful)
					throw new IOException("cannot sent message after "
							+ retryTimes + " trys", exception);
				return socket;
			} else {
				socket.getOutputStream().write(encode(message));
				return socket;
			}
		}

		@Override
		public void send(String message) {
			if (socket == null) {
				try {
					socket = trySetupSocket();
				} catch (IOException e) {
					LOGGER.error("cannot setup socket connection", e);
					return;
				}
			}
			try {
				socket = trySendMessage(socket, message);
			} catch (IOException e) {
				LOGGER.error("cannot send message within socket connection", e);
			}
		}

	}

	public TcpReporter() {
		this(0);
	}

	protected TcpReporter(int maxSize) {
		super(maxSize, new TcpSender());
	}
}
