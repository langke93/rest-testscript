package org.langke.testscript;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.JSONObject;

import org.langke.testscript.cmd.Cmd;
import org.langke.testscript.cmd.CmdFactory;
import org.langke.testscript.common.FileUtil;
import org.langke.testscript.common.HttpSupporter;
import org.langke.testscript.common.TestConfig;
import org.langke.testscript.tag.Querywd;
import org.langke.testscript.tag.Tag;
import org.langke.testscript.tag.TagFactory;
import org.langke.util.logging.ESLogger;
import org.langke.util.logging.Loggers;


/**
 * 读取运行测试用例脚本  Usage: Test <project> <operate>
 * 标签配置在每个脚本项目根目录下test.properties文件里，在运行时，会把脚本里标签替换成properties文件里key对应的值
 * 生成器标签：
 * 		SEQINT，自增整数 Usage:${SEQINT.0}
 *     RANDINT，随机整数 Usage:${RANDINT.0_1000}	0_1000表随机值范围
 *     QUERYWD，随机关键词 Usage:${QUERYWD.RAND}	properties文件配合QUERYWD.PATH=randkwd.txt配置关键词文件，如果没有则生成英文数字混合随机字符串
 *指令标签，（配置在测试脚本第五段，如果没有第四段需要留出空行）：
 *		SLEEP，等待指令，Usage:${SLEEP.10}	10表等10秒
 *		FOREACH,循环指令，Usage:${FOREACH.10} 10表循环10次
 * 测试脚本格式：
 * 		第一段URL，第二段METHOD，第三段请求BODY，第四段预计结果；第五段指令${FOREACH} ${SLEEP}
 * 		以空行区分每个段落
 * @author langke
 * @since JDK1.6
 * @version 1.0
 * 
 */
public class Test {
	public static volatile ConcurrentHashMap<String,String> current=new ConcurrentHashMap<String,String>();
	private static ESLogger log = Loggers.getLogger(Test.class);

	/**
	 * @param args[0] 脚本一级目录 测试脚本项目
	 * @param args[1] 脚本二级目录 测试步骤
	 * @param args[n] 脚本n级目录
	 */
	public static void main(String[] args) {
		String msg = null;
		//测试脚本项目
		String testProj;
		//测试步骤
		String operate;
		//执行指定目录下脚本
		String exec_script_dir = null;
		String workDir;
		TestConfig projConfig;
		String testBase = "rest-testscript";
		String home=System.getProperty("SEARCH.home", null);
		Test test = new Test();
		if(home!=null){
			testBase=home+File.separator+testBase;
			if(!new File(testBase).exists())
				testBase=home+File.separator+"script";
			if(!new File(testBase).exists()){
				log.error("script bse directory not exists :{}", testBase);
				return;
			}else
				log.info("Test base:{}", testBase);
		}
		File testBaseFile = new File(testBase);
		if(testBaseFile.list().length==0){
			log.error("test project is null");
			return;
		}
		msg = "\n Usage: Test <project> <operate>\n ";
		if(args.length==0){
			msg += "please choose test project:\n";
			msg += "or -c100 -t60 url \n";
			msg += "-c concurrent num \n";
			msg += "-t run time \n";
			msg += test.getSubDirectoryName(testBase);
			log.info(msg);
			return;
		}else{
			testProj = args[0];
		}
		
		if(testProj.startsWith("http://")){
			Test.webbench(testProj, null, null);
			return;	
		}else if(args.length==3 && (args.toString().indexOf("-c")!=-1 || args.toString().indexOf("-t")!=-1)){//直接测试URL,webbench
			Integer concurrent = null;
			Integer time = null;
			String url = null;
			for(int i=0;i<args.length;i++){
				if(args[i].startsWith("-c"))
					concurrent = Integer.parseInt(args[i].replace("-c", ""));
				else if(args[i].startsWith("-t"))
					time = Integer.parseInt(args[i].replace("-t", ""));
				else
					url = args[i];
			}
			Test.webbench(url, concurrent, time);
			return;	
		}else if(args.length==1){
			msg += "please choose operate:\n ";
			msg += "	all\n";
			msg += test.getSubDirectoryName(testBase+File.separator+testProj);
			log.info(msg);
			return;			
		}else{
			operate = args[1];
			if(args.length>=3){
				exec_script_dir = "";
				for(int i=3;i<=args.length;i++)
					exec_script_dir += args[i-1]+File.separator;
			}
			if(exec_script_dir!=null ){
				workDir = testBase+File.separator+testProj+File.separator+operate;
				projConfig=TestConfig.getInstance(testBase+File.separator+testProj+"/test.properties");
				test.execute(workDir,exec_script_dir,projConfig);
			}else if(operate.equals("all")){
				//顺序执行脚本
				String fileBase = testBase+File.separator+testProj+File.separator;
				FileFilter fileFilter =   new FileFilter() {
					public boolean accept(File pathname) {
						//过滤隐藏目录,report目录
						if(pathname.isDirectory() && !pathname.isHidden() && !pathname.getName().equals("report"))
							return true;
						else
						return false;
					}
				};
				File listFiles[] = new File(fileBase).listFiles(fileFilter);
				for(File operateFile:listFiles){
					if(operateFile.exists() && operateFile.isDirectory() && !operateFile.isHidden() && !operateFile.getName().startsWith(".")){
						workDir = testBase+File.separator+testProj;
						System.setProperty("TESTSCRIPT.WORK.DIR", workDir);
						projConfig=TestConfig.getInstance(workDir+"/test.properties");
						test.execute(workDir,operateFile.getName(),projConfig);
					}
				}
			}else{
				workDir = testBase+File.separator+testProj;
				System.setProperty("TESTSCRIPT.WORK.DIR", workDir);
				projConfig=TestConfig.getInstance(workDir+"/test.properties");
				test.execute(workDir,operate,projConfig);
			}
		}
	}
	
	/**
	 * url并发测试
	 * @param url 
	 * @param concurrent 并发数
	 * @param time 执行时间
	 * @since 1.0
	 * @out 输出执行结果
	 */
	public static void webbench(String url,Integer concurrent,Integer time){
		WebBench webBench = new WebBench();
		String result = webBench.runInTime(url, concurrent, time);
		log.info("{}", result);
	}
	private Object execute(String workDir, String operate,TestConfig projConfig) {
		log.info("execute workDir:"+workDir+"	operate:"+operate, workDir,operate);

		BufferedReader br = null;
		FileReader fileReader = null;
		String line;
		String file_encoding = null;
		if(!new File(workDir+File.separator+operate).exists()){
			log.error("not exists:"+workDir+File.separator+operate );
			return null;
		}
		File reportDir  = new File(workDir+File.separator+"report");
		if(reportDir.exists()){//初始化report目录
			FileUtil.deleteFile(reportDir);
		}
		File files = new File(workDir+File.separator+operate);
		for(File scriptFiles:files.listFiles()){
			int count=0;//读取行数记录，忽略空行
			String URL = null;//请求地址
			String METHOD = null;//请求方法
			String BODY = null;//请求主体
			String expected = null;//预计结果
			String command = null;//指令 
			String result = null;
			StringBuffer stringBuffer = new StringBuffer();
			if(scriptFiles.isDirectory()&& !scriptFiles.isHidden() && !scriptFiles.getName().startsWith(".") ){//执行子目录
				execute(workDir+File.separator+operate+File.separator+scriptFiles.getName(),"",projConfig);
			}
			if(scriptFiles.isFile() && !scriptFiles.isHidden() && !scriptFiles.getName().startsWith(".") && scriptFiles.getName().endsWith(".txt")){//只执行.txt格式脚本
				try {
					fileReader = new FileReader(scriptFiles);
					file_encoding = FileUtil.getCharset(scriptFiles);//fileReader.getEncoding();
					//log.info("file_encoding:{}", file_encoding);
					br = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFiles),file_encoding));
					while ((line = br.readLine()) != null) {
						if(line.startsWith("#"))
							continue;
						if(line.trim().length()!=0){
							stringBuffer.append(line);
							if(count==3){
								stringBuffer.append("\n");
							}
						}else{
							switch(count){
								case 0:
									URL = stringBuffer.toString();
									break;
								case 1:
									METHOD = stringBuffer.toString();
									break;
								case 2:
									BODY = stringBuffer.toString();
									break;
								case 3:
									expected = stringBuffer.toString();
									break;
								default :
									command = stringBuffer.toString(); 
							}
							stringBuffer.delete(0, stringBuffer.length());
							count++;
						}
						
					}
				} catch(Exception e){
					e.printStackTrace();
				}finally{
					try {
						fileReader.close();
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				//文件名hashCode作为自增环境变量KEY
				System.setProperty("TESTSCRIPT.SEQINT.KEY", new String(workDir+File.separator+operate+File.separator+ scriptFiles.getName()).hashCode()+"");
				
				if(null==URL || null==METHOD || URL.length()==0 || METHOD.length()==0)
					continue;//忽略无效文件
				if(BODY==null){
					BODY =  stringBuffer.toString();
					stringBuffer.delete(0, stringBuffer.length());
				}
				if(count==3){
					if(expected==null || expected.length()==0){
						expected = stringBuffer.toString();
						stringBuffer.delete(0, stringBuffer.length());
					}
				}else if(count>=4){
					if(command==null || command.length()==0){
						command = stringBuffer.toString();
						stringBuffer.delete(0, stringBuffer.length());
					}
				}
				//带命令标签，处理
				if(count>=4 && command!=null && command.length()!=0){
					result = processCommand(command,URL,BODY,METHOD,projConfig);
				}else{
					URL = replaceTag(projConfig, URL);
					BODY = replaceTag(projConfig, BODY);
					result = HttpSupporter.doRequest(URL, BODY,METHOD);
				}
				if(count>=3 && expected!=null && expected.length()!=0){
					reportFile(workDir, scriptFiles.getName(), result, expected, URL,BODY,METHOD);
				}
				log.info(URL+"\n"+METHOD+"\n"+result);
			}//end if execute File
		}//end for
		log.info(operate+" execute complete!");
		return null;
	}
	/**
	 * 配合指令提交数据
	 * @param command
	 * @param URL
	 * @param BODY
	 * @param METHOD
	 * @param projConfig
	 * @return String 处理结果字符串
	 */
	public  String processCommand(String command,String URL,String BODY,String METHOD, TestConfig projConfig) {
		String result = null;
		String key ;
		String tag;
		Cmd cmdClass;
		Object obj;
		if(command==null || command.equals(""))
			return null;
		try{
			for(Enumeration<?>  enume= projConfig.properties.propertyNames();enume.hasMoreElements();){
				key = (String)enume.nextElement();
				tag = "${"+key+".";
				if(command.indexOf(tag)!=-1){
						cmdClass= CmdFactory.getCmd(key);
						if(cmdClass!=null){
							obj = cmdClass.exec(command, key, URL, BODY, METHOD, projConfig);
							if(obj!=null) result = obj.toString();
						}
				}
			}//end for
		}catch(Exception e){
			log.error("{}", e);
		}
		return result;
		
	}
	
	/**
	 * 输出报告
	 * @param workDir
	 * @param fileName
	 * @param result
	 * @param expected
	 * @param URL
	 * @param BODY
	 * @param METHOD
	 */
	public void reportFile(String workDir,String fileName,String result,String expected,String URL,String BODY,String METHOD){
		if(result!=null) 
			result = result.trim();
		else	
			result = "";
		if(expected!=null) expected = expected.trim();
		int totalExpected = getTotal(expected);
		int totalResult = getTotal(result);
		boolean contentNotEqual = true;
		//结果相符再需判断内容
		if(totalExpected==totalResult){
			if(ignore(result).equals(ignore(expected)))
				contentNotEqual = false;
		}
		//预计结果不符
		if((totalExpected!=totalResult) || (totalExpected==-1) || contentNotEqual){
			BufferedWriter writer = null;
			String dirName = "report";
			try {//写入report.txt
				File reportDir  = new File(workDir+File.separator+dirName);
				if(!reportDir.exists())
					reportDir.mkdir();
				log.info("report file:{}", workDir+File.separator+dirName+File.separator+"report_"+fileName);
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(workDir+File.separator+dirName+File.separator+"report_"+fileName)),"UTF-8"));
				writer.write(fileName+ "\n");
				writer.write(URL+ "\n");
				writer.write(METHOD);
				writer.write(BODY+ "\n");
				writer.write("\r\n------预计结果------:\r\n  ");
				writer.write(expected);
				writer.write("\r\n------实际结果------:\r\n  ");
				writer.write(result);
				writer.write("\r\n\r\n\r\n");						
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 处理导数据XML
	 * @param url
	 * @param xmlString
	 * @param common
	 */
	/*
	private void importData(String url, String xmlString,FileUtil common) {
	    Element element = FileUtil.getJdomElement(xmlString);
		Element testdata ;
		List<?> testdataList ;
		String method ;
		String type,id,result=null;
		try {
			testdataList = element.getChildren("testdata");
			for(Iterator<?> it = testdataList.iterator();it.hasNext();){
				testdata = (Element) it.next();
				method = testdata.getAttributeValue("method");
				if(method!=null && method.equals("docs")){//添加多个文档
					type = testdata.getChildTextTrim("type");
					result = common.postUrl(url + type + "/_ma", testdata.getChildTextTrim("data"));
				}else{
					type = testdata.getChildTextTrim("type");
					id = testdata.getChildTextTrim("id");
					result = common.postUrl( url + type +"/"+id, testdata.getChildTextTrim("data"));				
				}
				log.info(result);
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}

	}
	 */
	
	/**
	 * 标签替换
	 * @param projConfig
	 * @param str
	 * @return 替换后标签
	 */
	public static String replaceTag(TestConfig projConfig,String str){
		String key ;
		String tag;
		Tag tagClass; 
		if(str==null || str.equals(""))
			return null;
		try{
			for(Enumeration<?>  enume= projConfig.properties.propertyNames();enume.hasMoreElements();){
				key = (String)enume.nextElement();
				tag = "${"+key+"}";
				if(str.indexOf(tag)!=-1){//处理简单标签
					str = str.replace(tag,projConfig.get(key));
				}else{//处理预定义
					tag = "${"+key+".";
					if(str.indexOf(tag)!=-1){
							tagClass= TagFactory.getTag(key);
							if(tagClass!=null){
								if(tagClass instanceof Querywd){//初始化随机关键字文件路径
									Querywd querywd = (Querywd) tagClass;
									querywd.setQuerywdFilePath(System.getProperty("TESTSCRIPT.WORK.DIR")+File.separator+projConfig.get("QUERYWD.PATH"));
								}
								str = tagClass.replaceTag(str, key).toString();
							}
					}
				}//end else
			}//end for
			//Log.info(str);
		}catch(Exception e){
			e.printStackTrace();
		}
		return str;
	}
	
	/**
	 * 取子目录列表
	 * @param dirName
	 * @return String 子目录树字符串
	 */
	public String getSubDirectoryName(String dirName){
		String fileNameList = "" ;
		for(File operateFile:new File(dirName).listFiles()){
			if(!operateFile.isHidden() && !operateFile.getName().startsWith(".") && operateFile.isDirectory())
				fileNameList +="	" + operateFile.getName()+"\n";
		}
		return fileNameList;
	}
	
	/**
	 * 取返回结果记录数
	 * @param str
	 * @return int 结果记录数
	 */
	public int getTotal(String str) {
		int total = -1;
		if(str==null){
			return total;
		}
		str = str.replaceAll("null", "NULL");
		if (str.startsWith("{")) {
			try {
				JSONObject json = JSONObject.fromObject(str);
				if (str.startsWith("{\"qTime\":")) {
					json = json.getJSONObject("hits");
					total = json.getInt("total");
				} else if (str.startsWith("{\"ok\":true")) {
					total = json.getInt("affectedCount");
					if(total>0)total=1;//affectedCount>0 则此字段忽略
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return total;
	}
	
	/**
	 * 忽略返回json串中某个值对，用于输出报告时比对
	 * @param str
	 * @return String
	 */
	public String ignore(String str){
		str = str.replaceAll("null", "NULL");
		//需要忽略的字段
		String keys[] = {"qTime", "affectedCount"};
		if (str.startsWith("{")) {
			try{
				JSONObject json = JSONObject.fromObject(str);
				for(String key:keys){
					if(json.get(key)!=null){
						if(key.equals("affectedCount") && json.getInt(key)<=0){
							//affectedCount<=0 则此字段不忽略
						}else
							json.put(key, Integer.valueOf(0));
					}
				}
				str = json.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return str;
	}
}
