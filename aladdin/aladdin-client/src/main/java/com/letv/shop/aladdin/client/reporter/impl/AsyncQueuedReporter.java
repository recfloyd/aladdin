package com.letv.shop.aladdin.client.reporter.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象的异步队列发报器 这个类维护一个阻塞队列,以及一个单独的发送线程
 * 可以为此队列设置一个上限,如果堆积的报文超过上限则自动丢弃新的报文,避免大量堆积造成内存溢出
 * 
 * @author lijia
 * 
 */
public abstract class AsyncQueuedReporter extends
		MessageAssembleReporter {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AsyncQueuedReporter.class);

	/**
	 * 发报客户端
	 * 
	 * @author lijia
	 * 
	 */
	interface Sender {
		void init();

		void destroy();

		void send(String message);
	}

	/**
	 * 消息队列
	 */
	protected BlockingQueue<String> queue;
	/**
	 * 队列上限,当此值为正数时才有效
	 */
	protected int maxSize;
	/**
	 * 发送客户端
	 */
	protected Sender sender;

	protected AsyncQueuedReporter(int maxSize, final Sender sender) {
		this.maxSize = maxSize;
		this.queue = maxSize > 0 ? new ArrayBlockingQueue<String>(maxSize)
				: new LinkedBlockingQueue<String>();
		this.sender = sender;
		new Thread() {
			@Override
			public void run() {
				sender.init();
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						sender.destroy();
					}
				});
				while (!interrupted()) {
					String message = null;
					try {
						message = queue.take();
					} catch (InterruptedException e) {
						continue;
					}
					sender.send(message);
				}
			}
		}.start();
		LOGGER.info("a sender of {} with a {} limit queue reporter started",
				sender.getClass().getName(), maxSize);
	}

	protected AsyncQueuedReporter(Sender sender) {
		this(0, sender);
	}

	@Override
	protected void sendMessage(String message) {
		this.queue.offer(message);
	}
}
