package com.letv.shop.aladdin.server.handler.impl;

import java.util.List;

import com.letv.shop.aladdin.server.handler.Handler;
import com.letv.shop.aladdin.server.message.MessagePack;
import com.letv.shop.aladdin.server.warner.Warner;

public abstract class AbstractHandler implements Handler {
	public static class Noticer {
		private List<String> receiver;
		private Warner warner;

		public Noticer(List<String> receiver, Warner warner) {
			this.receiver = receiver;
			this.warner = warner;
		}
	}

	protected volatile boolean enabled;
	protected String key;
	protected Integer id;
	protected List<Noticer> noticers;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Noticer> getNoticers() {
		return noticers;
	}

	public void setNoticers(List<Noticer> noticers) {
		this.noticers = noticers;
	}

	@Override
	public void handle(MessagePack messagePack) {
		List<String> noticeContent = getNoticeContents(messagePack);
		if (noticeContent != null && noticers != null)
			for (String content : noticeContent) {
				for (Noticer noticer : noticers) {
					noticer.warner.warn(noticer.receiver, content);
				}
			}
	}

	protected abstract List<String> getNoticeContents(MessagePack messagePack);
}
