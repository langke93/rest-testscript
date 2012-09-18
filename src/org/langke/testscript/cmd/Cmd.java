package org.langke.testscript.cmd;

import org.langke.testscript.Test;
import org.langke.testscript.common.HttpClientUtil;
import org.langke.testscript.common.HttpSupporter;
import org.langke.testscript.common.Response;
import org.langke.testscript.common.TestConfig;

/**
 * 指令抽象类
 * @author langke
 *
 */
public abstract class Cmd {
	public abstract Object exec(String command,String key,String URL,String BODY,String METHOD,TestConfig projConfig);

	/**
	 * 提交数据返回字符串结果
	 * @param URL
	 * @param BODY
	 * @param METHOD
	 * @param projConfig
	 * @return String
	 */
	public String postData(String URL,String BODY,String METHOD,TestConfig projConfig){
		String seg="|||SEG|||";
		String all=URL+seg+BODY;
		all=Test.replaceTag(projConfig, all);
		
		int index=all.indexOf(seg);
		
		URL = all.substring(0,index);
		BODY=all.substring(index+seg.length());
		 
		String result = HttpSupporter.doRequest(URL, BODY,METHOD);
		return result;
	}	
	
	/**
	 * 提交数据返回Response对象
	 * @param URL
	 * @param BODY
	 * @param METHOD
	 * @param projConfig
	 * @return Response
	 */
	public Response postDataResp(String URL,String BODY,String METHOD,TestConfig projConfig){
		String seg="|||SEG|||";
		String all=URL+seg+BODY;
		all=Test.replaceTag(projConfig, all);
		
		int index=all.indexOf(seg);
		
		URL = all.substring(0,index);
		BODY=all.substring(index+seg.length());
		Response response = HttpClientUtil.getInstance().new Execute().exec(URL, BODY, METHOD);//new HttpSupporter().getRequestToResponse(URL, BODY,METHOD);
		return response;
	}
}
