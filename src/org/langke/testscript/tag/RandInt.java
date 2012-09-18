package org.langke.testscript.tag;

import java.util.Random;

/**
 * 标签替换为随机数 
 * 有设置随机种了
 * @author langke
 *
 */
public class RandInt  implements Tag{
	//设置随机种子数
	Random random = new Random(Integer.MAX_VALUE);
	public Object replaceTag(String str,String key) {
		String tag = "${"+key+".";
		String temp ;
		int min =0;
		int max = 0;
		//Random random = new Random();
		int randNumber ;
		
		temp = str.substring(str.indexOf(tag)+tag.length());
		temp = temp.substring(0,temp.indexOf('}'));
		min = Integer.valueOf(temp.substring(0, temp.indexOf("_")));
		max = Integer.valueOf(temp.substring(temp.indexOf("_")+1));
		// randNumber 将被赋值为一个 MIN 和 MAX 范围内的随机数
		randNumber = random.nextInt(max - min + 1) + min;
		str = str.replace(tag+temp+"}", String.valueOf(randNumber));
		return str;
	}

	public  static void main(String args[]) throws Exception{
		for(int i=0;i<10;i++){
			RandInt tag = (RandInt) TagFactory.getTag("RANDINT");
			System.out.println(tag.random.nextInt(10-0+1)+0);
		}
	}
}
