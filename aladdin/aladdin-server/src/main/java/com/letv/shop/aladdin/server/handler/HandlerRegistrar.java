package com.letv.shop.aladdin.server.handler;

import java.util.List;
import java.util.Map;

/**
 * Handler注册器
 * 
 * @author lijia
 * 
 */
public interface HandlerRegistrar {
	void init();

	void addHandler(Handler handler);

	void setHandlers(List<Handler> handlers);

	void removeHandler(String key, Integer id);

	void enableHandler(String key, Integer id);

	void disableHandler(String key, Integer id);

	List<String> getKeys();

	List<Handler> getHandlersByKey(String key);

	Map<String, List<Handler>> getAllRegistered();
}
