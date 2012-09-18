package org.langke.testscript.tag;

/**
 * 自增数
 * @author langke 
 *
 */
public class SeqInt  implements Tag{
	public synchronized Object replaceTag(String str,String key) {
		String tag = "${"+key+".";
		String temp ;
		Integer seqint = null;
		String seqIntKey = System.getProperty("TESTSCRIPT.SEQINT.KEY");
		String sseqIntValue = System.getProperty(seqIntKey, null);
		if(null != sseqIntValue)
			seqint = Integer.valueOf(sseqIntValue);
		while(str.indexOf(tag)!=-1 ){
			temp = str.substring(str.indexOf(tag)+tag.length());
			temp = temp.substring(0,temp.indexOf('}'));
			if(temp.equals("")) temp = "0";
			if(seqint==null)
				seqint = Integer.valueOf(temp);
			str = str.replaceFirst("\\$\\{"+key+"."+temp+"}", String.valueOf(seqint));//$和{在replaceFirst,replaceAll作为正则表达式需要转义\\$\\{
		}
		//写在循环外表单个脚本里自增变量值相同
		seqint ++;
		System.setProperty(seqIntKey, String.valueOf(seqint));
		return str;
	}

}
