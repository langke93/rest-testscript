package org.langke.testscript.cmd;

import org.langke.testscript.common.TestConfig;
import org.langke.util.logging.ESLogger;
import org.langke.util.logging.Loggers;

/**
 * 休眠指令
 * @author langke
 *
 */
public class Sleep  extends Cmd{
	private static ESLogger log = Loggers.getLogger(Sleep.class);
	
	@Override
	public Object exec(String command,String key,String URL,String BODY,String METHOD,TestConfig projConfig) {
		String result = postData(URL, BODY, METHOD, projConfig);//提交数据
		String tag = "${"+key+".";
		String temp ;
		Float time = 1.0f;//默认一秒
		temp = command.substring(command.indexOf(tag)+tag.length());
		temp = temp.substring(0,temp.indexOf('}'));
		if(!temp.equals("")) 
			time = Float.valueOf(temp);	
		log.info("sleep...{}s",time);
		try {
			Thread.sleep((long) (time*1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		return result;
	}
}
