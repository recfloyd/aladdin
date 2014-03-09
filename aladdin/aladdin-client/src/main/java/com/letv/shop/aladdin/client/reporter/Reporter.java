package com.letv.shop.aladdin.client.reporter;

/**
 * 发报器
 * 
 * @author lijia
 * 
 */
public interface Reporter {
	/**
	 * 发送心跳
	 * 
	 * @param key
	 * @return
	 */
	boolean heartbeat(String key);

	/**
	 * 发送警报
	 * 
	 * @param key
	 * @param info
	 * @return
	 */
	boolean warn(String key, String info);
}
