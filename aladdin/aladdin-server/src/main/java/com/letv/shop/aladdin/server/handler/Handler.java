package com.letv.shop.aladdin.server.handler;

import com.letv.shop.aladdin.server.message.MessagePack;

/**
 * 处理器
 * 
 * @author lijia
 * 
 */
public interface Handler {
	boolean isEnabled();

	void setEnabled(boolean enabled);

	String getKey();

	Integer getId();

	void handle(MessagePack messagePack);
}
