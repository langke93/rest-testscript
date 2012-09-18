package org.langke.testscript.tag;

/**
 * 标签接口
 * 传入模板和标签，返回被替换后内容
 * @author langke
 *
 */
public interface Tag {
	public Object replaceTag(String str,String key);
}
