package com.letv.shop.aladdin.server.board;

import java.util.List;
import java.util.Map;

import com.letv.shop.aladdin.server.handler.Handler;
import com.letv.shop.aladdin.server.handler.HandlerRegistrar;
import com.letv.shop.aladdin.server.message.MessagePack;

public class Checker implements Runnable {
	private Board board;
	private HandlerRegistrar handlerRegistrar;

	public void setBoard(Board board) {
		this.board = board;
	}

	public void setHandlerRegistrar(HandlerRegistrar handlerRegistrar) {
		this.handlerRegistrar = handlerRegistrar;
	}

	@Override
	public void run() {
		Map<String, List<Handler>> allRegistered = handlerRegistrar
				.getAllRegistered();
		for (Map.Entry<String, List<Handler>> entry : allRegistered.entrySet()) {
			String key = entry.getKey();
			MessagePack messagePack = board.getMessagePack(key);
			for (Handler handler : entry.getValue()) {
				if (handler.isEnabled())
					handler.handle(messagePack);
			}
		}
	}
}
