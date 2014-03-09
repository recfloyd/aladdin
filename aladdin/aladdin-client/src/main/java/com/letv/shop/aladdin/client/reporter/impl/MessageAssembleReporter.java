package com.letv.shop.aladdin.client.reporter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.letv.shop.aladdin.client.reporter.Reporter;

/**
 * 一个抽象的发报器,他抽象了构建报文的方法 子类需要实现发送报文的方法
 * 
 * @author lijia
 * 
 */
public abstract class MessageAssembleReporter implements Reporter {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MessageAssembleReporter.class);
	/**
	 * 默认的信息分割符,这里使用报警符(ASCII码为7),因为是一个控制字符,不会出现在常规文本中
	 */
	protected static char SPLITER = 7;
	/**
	 * 发送信息最多的字符数,默认是5K
	 */
	protected static int MAX_CHAR_SIZE = 5120;

	/**
	 * 构建报文
	 * 
	 * @param key
	 *            键
	 * @param type
	 *            类型,0为心跳,1为警报
	 * @param info
	 *            信息,可以为null
	 * @return 键 spliter 当前时间戳 spliter 类型 spliter 信息(可选)
	 */
	protected String buildMessage(String key, byte type, String info) {
		if (key == null || key.isEmpty()) {
			LOGGER.error("cannot send message without a key");
			return null;
		}

		if (info != null && info.length() > MAX_CHAR_SIZE) {
			LOGGER.error("cannot send info exceeding {} chars", MAX_CHAR_SIZE);
			return null;
		}

		long timestamp = System.currentTimeMillis();

		StringBuilder sb = new StringBuilder();
		sb.append(key).append(SPLITER).append(timestamp).append(SPLITER)
				.append(type);
		if (info != null && !info.isEmpty())
			sb.append(SPLITER).append(info);
		return sb.toString();
	}

	protected abstract void sendMessage(String message);

	@Override
	public boolean heartbeat(String key) {
		String message = buildMessage(key, (byte) 0, null);
		if (message == null)
			return false;
		else {
			sendMessage(message);
			return true;
		}
	}

	@Override
	public boolean warn(String key, String info) {
		String message = buildMessage(key, (byte) 1, info);
		if (message == null)
			return false;
		else {
			sendMessage(message);
			return true;
		}
	}

}
