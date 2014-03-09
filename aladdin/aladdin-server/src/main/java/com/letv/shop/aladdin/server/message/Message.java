package com.letv.shop.aladdin.server.message;

/**
 * 报文
 * 
 * @author lijia
 * 
 */
public class Message {
	/**
	 * 键值
	 */
	private String key;
	/**
	 * 报文类型,0:心跳信息;1:异常信息
	 */
	private byte type;
	/**
	 * 报文信息
	 */
	private String info;
	/**
	 * 报文发送时间
	 */
	private Long sendTime;
	/**
	 * 报文接收时间
	 */
	private Long receiveTime;
	/**
	 * 发报者IP
	 */
	private String senderIP;
	/**
	 * 收报者IP
	 */
	private String receiverIP;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public Long getSendTime() {
		return sendTime;
	}

	public void setSendTime(Long sendTime) {
		this.sendTime = sendTime;
	}

	public Long getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Long receiveTime) {
		this.receiveTime = receiveTime;
	}

	public String getSenderIP() {
		return senderIP;
	}

	public void setSenderIP(String senderIP) {
		this.senderIP = senderIP;
	}

	public String getReceiverIP() {
		return receiverIP;
	}

	public void setReceiverIP(String receiverIP) {
		this.receiverIP = receiverIP;
	}
}
