package org.langke.testscript;

import java.util.concurrent.atomic.AtomicInteger;

import net.sf.json.JSONObject;

import org.langke.testscript.common.HttpClientUtil;
import org.langke.testscript.common.Response;
import org.langke.util.logging.ESLogger;
import org.langke.util.logging.Loggers;

/**
 * 指定时间并发测试
 * @author langke
 * @since JDK1.6
 * @version 1.0
 *
 */ 
public class WebBench {

	private ESLogger log = Loggers.getLogger(WebBench.class);
	private String url;
	private String body;
	private String method;
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
	Long runTime;//需要执行时间
	Long startTime;//开始执行时间
	Long exec_time;
	Object lock = new Object();
	
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

	/**
	 * 并发执行指定时长
	 * @param url
	 * @param concurrent 并发数
	 * @param time 执行时间/秒
	 * @return
	 */
	public String runInTime(String url,Integer concurrent,Integer time){
		if(concurrent == null)
			concurrent = 1;
		if(time == null)
			time = 30;
		this.url = url;
		this.method = "GET";
		this.startTime = System.currentTimeMillis();
		this.exec_time = this.startTime;
		this.runTime = (long) (time * 1000);
		this.total = concurrent;
		for(int i=0;i<total;i++){//并发循环
			taskThread(i);
		}
		monitorReportThread();	
		waitTaskReport();
		return result;
	}
	
	/**
	 * 执行任务线程
	 * @param i
	 */
	public void taskThread(int i){
		new Thread("test_main"+i){
				 public void run(){
					 Response response = new Response();
					int showTime = 0;
					int factTime = 0;
					int hits = 0;
					long timer = 0;
					 try{
						 for(;;){//重复任务
								send.incrementAndGet();
								timer = System.currentTimeMillis();
								response =  HttpClientUtil.getInstance().new Execute().exec(url, body,method);
								timer = System.currentTimeMillis() - timer;
								if(response.getCode()!=200)
									failed(response);
								if(response.getHeader()!=null && response.getHeader().indexOf("json")!=-1){
									if(response.getResult() == null) continue;
								}
								factTime = (int)(timer);
								success(showTime, factTime, hits, response);
								if(System.currentTimeMillis() - startTime >= runTime){
									break;
								}
						 }
					 }catch(Exception e){
						 log.error("{}", e);
						 log.error("code{} msg{} result{}", response.getCode(),response.getMsg(),response.getResult());
						 failed(response);
					 }
				}
		}.start();
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
			if(System.currentTimeMillis() - startTime >= runTime){//执行结果汇总
				exec_time = System.currentTimeMillis()-exec_time;
				log.info("Thread exit !concurrent num:{} exec_time:{} send:{}  receive:{} error:{} exec_time:{} ms " +
						" hits/req:{}  affected:{} showtime/req:{} facttime/req:{} average/s:{}" ,
						total,exec_time,send.get(),receive.get(),error.get(),exec_time,
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
}
