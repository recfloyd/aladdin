package com.letv.shop.aladdin.server.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * JVM内存实现的Handler注册器 对于已注册的Handler,在向外提供查询服务时,只提供浅拷贝以防止线程安全问题
 * 
 * @author lijia
 * 
 */
public class MemHandlerRegistrar implements HandlerRegistrar {
	private ConcurrentMap<String, ConcurrentMap<Integer, Handler>> handlers;

	@Override
	public void init() {
		handlers = new ConcurrentHashMap<String, ConcurrentMap<Integer, Handler>>();
	}

	@Override
	public void addHandler(Handler handler) {
		Integer id = handler.getId();
		String key = handler.getKey();
		ConcurrentMap<Integer, Handler> handlersByKey = handlers.get(key);
		if (handlersByKey == null) {
			ConcurrentMap<Integer, Handler> newMap = new ConcurrentHashMap<Integer, Handler>();
			handlersByKey = handlers.putIfAbsent(key, newMap);
			if (handlersByKey == null)
				handlersByKey = newMap;
		}
		handlersByKey.put(id, handler);
	}

	@Override
	public void setHandlers(List<Handler> handlers) {
		handlers.clear();
		for (Handler handler : handlers) {
			addHandler(handler);
		}
	}

	@Override
	public void removeHandler(String key, Integer id) {
		ConcurrentMap<Integer, Handler> handlersByKey = handlers.get(key);
		if (handlersByKey != null)
			handlersByKey.remove(id);
	}

	@Override
	public void enableHandler(String key, Integer id) {
		ConcurrentMap<Integer, Handler> handlersByKey = handlers.get(key);
		if (handlersByKey != null) {
			Handler handler = handlersByKey.get(id);
			if (handler != null)
				handler.setEnabled(true);
		}
	}

	@Override
	public void disableHandler(String key, Integer id) {
		ConcurrentMap<Integer, Handler> handlersByKey = handlers.get(key);
		if (handlersByKey != null) {
			Handler handler = handlersByKey.get(id);
			if (handler != null)
				handler.setEnabled(false);
		}
	}

	@Override
	public List<String> getKeys() {
		return new ArrayList<String>(handlers.keySet());
	}

	@Override
	public List<Handler> getHandlersByKey(String key) {
		List<Handler> result = new ArrayList<Handler>();
		ConcurrentMap<Integer, Handler> handlersByKey = handlers.get(key);
		if (handlersByKey != null) {
			result.addAll(handlersByKey.values());
		}
		return result;
	}

	@Override
	public Map<String, List<Handler>> getAllRegistered() {
		Map<String, List<Handler>> result = new HashMap<String, List<Handler>>();
		for (Map.Entry<String, ConcurrentMap<Integer, Handler>> keyEntry : handlers
				.entrySet()) {
			result.put(keyEntry.getKey(), new ArrayList<Handler>(keyEntry
					.getValue().values()));
		}
		return result;
	}
}
