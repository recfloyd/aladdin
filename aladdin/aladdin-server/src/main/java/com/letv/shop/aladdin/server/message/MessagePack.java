package com.letv.shop.aladdin.server.message;

import java.util.List;

/**
 * 报文包
 * 
 * @author lijia
 * 
 */
public class MessagePack {
	/**
	 * 最后一条
	 */
	private Message latest;
	/**
	 * 最近的若干条
	 */
	private List<Message> recent;
	/**
	 * 报文数量:recent+1
	 */
	private int size;
	/**
	 * 第一条报文时间
	 */
	private Long firstTime;
	/**
	 * 最后一条报文的时间
	 */
	private Long lastTime;
	public Message getLatest() {
		return latest;
	}
	public void setLatest(Message latest) {
		this.latest = latest;
	}
	public List<Message> getRecent() {
		return recent;
	}
	public void setRecent(List<Message> recent) {
		this.recent = recent;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public Long getFirstTime() {
		return firstTime;
	}
	public void setFirstTime(Long firstTime) {
		this.firstTime = firstTime;
	}
	public Long getLastTime() {
		return lastTime;
	}
	public void setLastTime(Long lastTime) {
		this.lastTime = lastTime;
	}
}
