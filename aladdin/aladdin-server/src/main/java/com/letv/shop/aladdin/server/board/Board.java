package com.letv.shop.aladdin.server.board;

import java.util.Map;

import com.letv.shop.aladdin.server.message.Message;
import com.letv.shop.aladdin.server.message.MessagePack;

/**
 * 看板
 * 
 * @author lijia
 * 
 */
public interface Board {
	/**
	 * 看板初始化
	 */
	void init();

	/**
	 * 张贴一条报文
	 * 
	 * @param message
	 */
	void post(Message message);

	/**
	 * 
	 * 获取某个键的报文包
	 * 
	 * @param key
	 * @return
	 */
	MessagePack getMessagePack(String key);

	/**
	 * 获取所有键的报文包
	 * 
	 * @return
	 */
	Map<String, MessagePack> dump();

	/**
	 * 擦除某个键的报文包
	 * 
	 * @param key
	 */
	void erase(String key);

	/**
	 * 清除看板
	 */
	void clear();
}
