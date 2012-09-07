package org.langke.testscript.cmd;

import java.util.HashMap;
import java.util.Map;
/**
 * 指令定义
 * @author langke_li
 *
 */
public class CmdFactory {
	private static Map<String, Cmd> cmdClasses = new HashMap<String, Cmd>();
	static {
		cmdClasses.put("SLEEP", new Sleep());
		cmdClasses.put("FOREACH", new ForEach());
		cmdClasses.put("CONCURRENT", new Concurrent());
	}
	public static Cmd getCmd(String tagName)throws InstantiationException,IllegalAccessException {
		return cmdClasses.get(tagName);
	}
}
