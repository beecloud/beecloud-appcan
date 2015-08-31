package cn.beecloud.entity;

import java.util.Map;

public class JSBridgePayReqParams {
	private String channel;
	private String title;
	private Integer totalfee;
	private String billno;
	private Map<String, String> optional;
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getTotalfee() {
		return totalfee;
	}
	public void setTotalfee(Integer totalfee) {
		this.totalfee = totalfee;
	}
	public String getBillno() {
		return billno;
	}
	public void setBillno(String billno) {
		this.billno = billno;
	}
	public Map<String, String> getOptional() {
		return optional;
	}
	public void setOptional(Map<String, String> optional) {
		this.optional = optional;
	}
}
