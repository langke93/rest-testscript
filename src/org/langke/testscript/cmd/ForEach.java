package org.langke.testscript.cmd;

import java.util.concurrent.atomic.AtomicInteger;

import org.langke.testscript.util.Response;
import org.langke.testscript.util.TestConfig;
import org.langke.util.logging.ESLogger;
import org.langke.util.logging.Loggers;


/**
 * 循环提交数据
 */
public class ForEach extends Cmd{
	static ESLogger log = Loggers.getLogger(ForEach.class);
	public Object exec(String command,String key,String URL,String BODY,String METHOD,TestConfig projConfig) {
		AtomicInteger errorCount=new AtomicInteger();
		Long exec_time;
		String tag = "${"+key+".";
		String temp ;
		Integer total = 1;//循环次数	，默认1	
		Response res = null;
		temp = command.substring(command.indexOf(tag)+tag.length());
		temp = temp.substring(0,temp.indexOf('}'));
		if(!temp.equals("")) 
			total = Integer.valueOf(temp);	
		exec_time = System.currentTimeMillis();
		for(int i=0;i<total;i++){
			res = postDataResp(URL, BODY, METHOD, projConfig);//提交数据
			 if(res.getCode()!=200){
				 errorCount.incrementAndGet();
				 log.error("send:{} errorCount:{} code:{} msg:{} result:{}", total,errorCount.get(),res.getCode(),res.getMsg(),res.getResult());
				 try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			 }
		}
		exec_time = System.currentTimeMillis()-exec_time;
		 log.info("count:{} errorCount:{} exec_time:{} ms", total,errorCount.get(),exec_time);
		return res.getResult();
	}

}
