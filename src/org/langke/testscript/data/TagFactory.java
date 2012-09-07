package org.langke.testscript.data;

import java.util.HashMap;
import java.util.Map;
/**
 * 标签定义
 * @author langke_li
 *
 */
public class TagFactory {
	private static Map<String, Tag> tagClasses = new HashMap<String, Tag>();
	static {
		tagClasses.put("SEQINT",new SeqInt());
		tagClasses.put("RANDINT", new RandInt());
		tagClasses.put("QUERYWD", new Querywd());
	}
	public static Tag getTag(String tagName) throws InstantiationException,IllegalAccessException {
		return tagClasses.get(tagName);
	}
}
