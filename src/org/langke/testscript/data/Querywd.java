package org.langke.testscript.data;

import java.io.File;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.langke.testscript.util.Common;


public class Querywd  implements Tag{

	Random random = new Random(Integer.MAX_VALUE);//设置随机种子数
	private String querywdFilePath;
	public String getQuerywdFilePath() {
		return querywdFilePath;
	}
	public void setQuerywdFilePath(String querywdFilePath) {
		this.querywdFilePath = querywdFilePath;
	}
	private int randomLength = 10;
	public int getRandomLength() {
		return randomLength;
	}
	public void setRandomLength(int randomLength) {
		this.randomLength = randomLength;
	}
	public Object replaceTag(String str,String key) {
		String tag = "${"+key+".";
		String temp ;
		int min =0;
		int max = 0;
		String randomStr ;
		int randNumber ;
		
		Common common = new Common();
		File querywdFile = new File(querywdFilePath);
		String querywd[] = null;
		if(querywdFilePath!=null && querywdFile.exists())
			querywd = common.getFile(querywdFilePath).toString().split("\n");
		while(str.indexOf(tag)!=-1 ){
			temp = str.substring(str.indexOf(tag)+tag.length());
			temp = temp.substring(0,temp.indexOf('}'));
			if(temp.endsWith(".txt")){
				if(querywdFilePath.endsWith(".txt")){
					querywdFilePath = querywdFilePath.substring(0,querywdFilePath.lastIndexOf(File.separator)+1)+temp;
				}else{
					querywdFilePath +=temp;
				}
				if(querywdFilePath!=null && querywdFile.exists())
					querywd = common.getFile(querywdFilePath).toString().split("\n");
			}
				
			if(querywd!= null && querywd.length>0){
				max = querywd.length-1;
				min = 0;
				randNumber = random.nextInt(max - min + 1) + min;
				randomStr = querywd[randNumber];
			}else
				randomStr =  RandomStringUtils.randomAlphanumeric(randomLength);//如果没有指定关键词文件，随机生成英文字母与数字组合
			str = str.replaceFirst("\\$\\{"+key+"."+temp+"}",randomStr);
		}
		return str;
	}

}
