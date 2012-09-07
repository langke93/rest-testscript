package org.langke.testscript.cmd;

import org.langke.testscript.Test;
import org.langke.testscript.util.HttpSupporter;
import org.langke.testscript.util.Response;
import org.langke.testscript.util.TestConfig;

public abstract class Cmd {
	public abstract Object exec(String command,String key,String URL,String BODY,String METHOD,TestConfig projConfig);
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
	public Response postDataResp(String URL,String BODY,String METHOD,TestConfig projConfig){
		String seg="|||SEG|||";
		String all=URL+seg+BODY;
		all=Test.replaceTag(projConfig, all);
		
		int index=all.indexOf(seg);
		
		URL = all.substring(0,index);
		BODY=all.substring(index+seg.length());
		Response response = new HttpSupporter().getRequestToResponse(URL, BODY,METHOD);
		return response;
	}
}
