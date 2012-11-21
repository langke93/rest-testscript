package org.langke.testscript.cmd;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.langke.testscript.common.HttpSupporter;
import org.langke.testscript.common.Response;
import org.langke.testscript.common.TestConfig;
import org.langke.util.logging.ESLogger;
import org.langke.util.logging.Loggers;

import net.sf.json.JSONObject;

/**
 * 并发指令
 * @author langke
 * @since JDK1.6
 * @version 1.8
 *
 */
public class Concurrent extends Cmd{
	private ESLogger log = Loggers.getLogger(Concurrent.class);
	private String url;
	private String body;
	private String method;
	private TestConfig projConfig;
	volatile String result;
	AtomicInteger send=new AtomicInteger();
	AtomicInteger receive=new AtomicInteger();
	AtomicInteger qps=new AtomicInteger();//每秒执行次数
	AtomicInteger maxQps=new AtomicInteger();	
	AtomicInteger success = new AtomicInteger();
	AtomicInteger error=new AtomicInteger();
	AtomicInteger totalShowTime = new AtomicInteger();//显示查询时间
	AtomicInteger totalFactTime = new AtomicInteger();//实际查询时间
	AtomicInteger totalHits = new AtomicInteger();//结果记录数
	AtomicInteger affected = new AtomicInteger();//影响记录数
	volatile boolean stopThread = false;
	Integer total = 1;//并发数
	Integer taskNum = 1;//任务个数
	Long exec_time;
	Object lock = new Object();
	
	void setParm(String url,String body,String method,TestConfig projConfig){
		this.url = url;
		this.body = body;
		this.method = method;
		this.projConfig = projConfig;
	}
	

	public void success(int showTime, int factTime, int hits,Response response) {
		synchronized(lock) {
			success.incrementAndGet();
			totalShowTime.addAndGet(showTime);
			totalFactTime.addAndGet(factTime);
			totalHits.addAndGet(hits);
			receive.incrementAndGet();
			qps.incrementAndGet();
			result = response.getResult();
		}
	}
	public synchronized void failed(Response response) {
		error.incrementAndGet();
		receive.incrementAndGet();
		result = response.getResult();
		if(error.get()<10 && response !=null){
			log.error("{}", JSONObject.fromObject(response));
		}
	}
	
	@Override
	public Object exec(String command, String key, String URL, String BODY,
			String METHOD,  final TestConfig projConfig) {
		String tag = "${"+key+".";
		String temp ;
		temp = command.substring(command.indexOf(tag)+tag.length());
		temp = temp.substring(0,temp.indexOf('}'));
		if(!temp.equals("")) {
			if(temp.indexOf('_')!=-1){//10_100 并发任务格式，前面并发数，后面任务执行次数
				total = Integer.valueOf(temp.substring(0, temp.indexOf("_")));
				taskNum = Integer.valueOf(temp.substring(temp.indexOf("_")+1));
			}else
				total = Integer.valueOf(temp);
		}
		setParm(URL, BODY, METHOD,projConfig);		
		exec_time = System.currentTimeMillis();
		ExecutorService es = Executors.newFixedThreadPool(total);
		for(int i=0;i<total;i++){//并发循环			
			Runnable task = taskThread(i);
			es.submit(task);
		}
		monitorReportThread();	
		waitTaskReport();
		es.shutdownNow();
		es.shutdown();
		return result;
	}

	
	/**
	 * 执行任务线程
	 * @param i
	 */
	public Runnable taskThread(int i){
		return new Thread("test_main"+i){
				 public void run(){
					Response response = new Response();
					JSONObject json;
					String qtime;
					int showTime = 0;
					int factTime = 0;
					int hits = 0;
					String affectedCountTemp ;
					long timer = 0;
					 try{
						 for(int i=0;i<taskNum;i++){//重复任务
								 send.incrementAndGet();
								 timer = System.currentTimeMillis();
								 response = postDataResp(url, body,method,projConfig);
								 timer = System.currentTimeMillis() - timer;
								 //receive.incrementAndGet();
								 if(response.getCode()!=200)
									 failed(response);
									 //error.incrementAndGet();
								 //result = response.getResult();
								if(response.getHeader()!=null && response.getHeader().indexOf("json")!=-1){
									if(response.getResult() == null) continue;
									json = JSONObject.fromObject(response.getResult());
									if(json.isEmpty() || json.isNullObject()) continue;
									qtime =getJsonString(projConfig.get("COST_TIME", "qTime"),json);
									if(qtime!=null){
										if(qtime.endsWith("ms"))
											showTime = Integer.parseInt(qtime.substring(0,qtime.length()-2));
										else
											showTime = Integer.valueOf(qtime);
										//totalShowTime.addAndGet(showTime);
									}
									affectedCountTemp = getJsonString("affectedCount",json);
									if(affectedCountTemp!=null){
										affected.addAndGet(Integer.parseInt(affectedCountTemp));
									}
									json = json.getJSONObject("hits");
									if(json != null && !json.isEmpty() && !json.isNullObject()){
										hits = Integer.parseInt(json.getString("total"));
										//totalHits.addAndGet(total);
									}
								}
								factTime = (int)(timer);
								//totalFactTime.addAndGet(factTime);
								success(showTime, factTime, hits, response);
						 }
					 }catch(Exception e){
						 log.error("{}", e);
						 log.error("code{} msg{} result{}", response.getCode(),response.getMsg(),response.getResult());
						 failed(response);
					 }
				}
		};
	}
	
	/**
	 * 执行中间汇报
	 */
    public void monitorReportThread(){
		new Thread("test_monitor"){
			public void run(){
				while(!stopThread){
					if(qps.get()>maxQps.get())
						maxQps.set(qps.get());
					synchronized(lock) {
						log.info("send:{}  receive:{} error:{} hits/req:{}  affected:{} showtime/req:{} facttime/req:{} qps:{} maxQps{}" 
							  ,send.get(),receive.get(),error.get(),getDiv(totalHits.get(),receive.get()), affected.get(),getDiv(totalShowTime.get(),qps.get()),getDiv(totalFactTime.get(),qps.get()),qps.get(),maxQps.get());
						totalShowTime.set(0);
						totalFactTime.set(0);
						totalHits.set(0);
						qps.set(0);
					}
					try{
						Thread.sleep(1000);
					}catch (InterruptedException e){
						Thread.currentThread().interrupt(); // re-assert interrupt 
					} 
				}
			}
		}.start();
    }

    public String getJsonString(String key,JSONObject json){
    	String res = null;
    	try{
    		 res = json.getString(key);
    	}catch(Exception e){    		
    	}
    	return res;
    }
    public int getDiv(int a,int b ){
    	if(b==0)
    		return b;
    	else 
    		return a/b;
    }
    
    public int getDiv(long a,int b ){
    	if(b==0)
    		return b;
    	else 
    		return (int) (a/b);
    }
    
	/**
	 * 等待任务完成,并汇总执行结果
	 */
	public void waitTaskReport(){
		while(!stopThread){
			if(receive.get()==total*taskNum){//执行结果汇总
				exec_time = System.currentTimeMillis()-exec_time;
				log.info("Thread exit !concurrent num:{} taskNum:{} send:{}  receive:{} error:{} exec_time:{} ms " +
						" hits/req:{}  affected:{} showtime/req:{} facttime/req:{} average/s:{}" ,
						total,taskNum,send.get(),receive.get(),error.get(),exec_time,
						getDiv(totalHits.get(),receive.get()), affected.get(),
						getDiv(totalShowTime.get(),receive.get()),
						getDiv(totalFactTime.get(),receive.get()),
						getDiv(receive.get(),getDiv(exec_time,1000))
						);
				stopThread = true;
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				log.error("{}", e);
			}
		}
	}
	
	public void concurrentTest(String URL){
		this.url = URL;
		
		for(int i=0;i<total;i++){//创建并发线程
			new Thread("test_main"+i){
				public void run(){
					for(int i=0;i<taskNum;i++){//重复线程任务
						send.incrementAndGet();
						Response response = new HttpSupporter().getRequestToResponse(url, null, "GET");
						 receive.incrementAndGet();
						 if(response.getCode()!=200){
							 error.incrementAndGet();
							 log.info("{}	{}	{}",response.getCode(),response.getMsg(),response.getResult());
						 }
					}				
				}
			}.start();
		}

		monitorReportThread();	
		waitTaskReport(); 
	}
	public static void main(String args[]){
		Concurrent co = new Concurrent();
		String url ="http://192.168.200.62"; //"http://hadoop3:9209/hashShard1//_search?q=_all:woyo.com";
		co.total =1;
		co.taskNum = 10;
		co.exec_time = System.currentTimeMillis();
		co.concurrentTest(url);
		
/*		FileUtil common = new FileUtil();
		for(int i=0;i<total;i++){
			log.info(common.getUrl(url));
		}*/
	}
		
}
