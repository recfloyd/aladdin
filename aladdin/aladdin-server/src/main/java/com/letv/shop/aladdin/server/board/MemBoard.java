package com.letv.shop.aladdin.server.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.letv.shop.aladdin.server.message.Message;
import com.letv.shop.aladdin.server.message.MessagePack;

/**
 * 以JVM内存实现的看板
 * 对于报文包,只做浅拷贝以防止线程安全问题
 * 
 * @author lijia
 * 
 */
public class MemBoard implements Board {
	private static final int DEFAULT_MESSAGE_QUEUE_MAX_SIZE = 20;
	private ConcurrentMap<String, ThreadSafeMessageBag> boardMap;

	/**
	 * 线程安全且具有固定大小的报文队列
	 * 
	 * @author lijia
	 * 
	 */
	private static class ThreadSafeMessageBag {
		private int maxSize;
		private LinkedList<Message> queue;
		private ReadWriteLock lock;

		ThreadSafeMessageBag(int maxSize) {
			this.maxSize = maxSize;
			this.queue = new LinkedList<Message>();
			this.lock = new ReentrantReadWriteLock();
		}

		void add(Message message) {
			lock.writeLock().lock();
			try {
				if (maxSize > 0 && queue.size() == maxSize) {
					queue.poll();
				}
				queue.add(message);
			} finally {
				lock.writeLock().unlock();
			}
		}

		MessagePack dump() {
			MessagePack mp = new MessagePack();
			List<Message> copy = null;
			lock.readLock().lock();
			try {
				copy = new ArrayList<Message>(queue);
			} finally {
				lock.readLock().unlock();
			}
			mp.setRecent(copy);
			int size = copy.size();
			mp.setSize(size);
			if (size > 0) {
				Message first = copy.get(0);
				Message last = copy.get(size - 1);
				mp.setFirstTime(first.getReceiveTime());
				mp.setLastTime(last.getReceiveTime());
				mp.setLatest(last);
			}
			return mp;
		}

		void clear() {
			lock.writeLock().lock();
			try {
				queue.clear();
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	@Override
	public void init() {
		boardMap = new ConcurrentHashMap<String, MemBoard.ThreadSafeMessageBag>();
	}

	@Override
	public void post(Message message) {
		String key = message.getKey();
		ThreadSafeMessageBag bag = boardMap.get(key);
		if (bag == null) {
			ThreadSafeMessageBag newBag = new ThreadSafeMessageBag(
					DEFAULT_MESSAGE_QUEUE_MAX_SIZE);
			bag = boardMap.putIfAbsent(key, newBag);
			if (bag == null)
				bag = newBag;
		}
		bag.add(message);
	}

	@Override
	public MessagePack getMessagePack(String key) {
		ThreadSafeMessageBag bag = boardMap.get(key);
		return bag == null ? new MessagePack() : bag.dump();
	}

	@Override
	public Map<String, MessagePack> dump() {
		Map<String, MessagePack> dump = new HashMap<String, MessagePack>();
		for (Map.Entry<String, ThreadSafeMessageBag> bagEntry : boardMap
				.entrySet()) {
			dump.put(bagEntry.getKey(), bagEntry.getValue().dump());
		}
		return dump;
	}

	@Override
	public void erase(String key) {
		ThreadSafeMessageBag bag = boardMap.get(key);
		if (bag != null)
			bag.clear();
		boardMap.remove(key);
	}

	@Override
	public void clear() {
		for (Map.Entry<String, ThreadSafeMessageBag> bagEntry : boardMap
				.entrySet()) {
			bagEntry.getValue().clear();
		}
		boardMap.clear();
	}
}
