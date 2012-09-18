package org.langke.testscript.common;

/**
 * Http请求响应体
 * @author langke
 * @since JDK1.6
 * @version 1.0
 */
public class Response {
	volatile int code;
	volatile String header;
	volatile String msg;
	volatile String result;
	public int getCode() {
		return code;
	}
	public void setCode(int i) {
		this.code = i;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	
}
