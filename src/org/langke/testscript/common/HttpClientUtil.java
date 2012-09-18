package org.langke.testscript.common;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.langke.util.logging.ESLogger;
import org.langke.util.logging.Loggers;
 
/**
 * httpclient 公共方法
 * 提供get put post delete 几种方式调用
 * @author langke
 * @since httpclient4.0.1
 * @version 1.0
 */
public class HttpClientUtil {

	private static ESLogger log = Loggers.getLogger(HttpClientUtil.class);
	private static HttpClient client = null;
	private static MultiThreadedHttpConnectionManager conn_manager = null;
	private static HttpConnectionManagerParams cmanager_params = null;
	private static final HttpClientUtil instance = new HttpClientUtil();
	
	private HttpClientUtil() {
	}
	
	public static HttpClientUtil getInstance(){
		return instance;
	}
	
	static{
		if(client==null) init();
	}
	
	private static void init(){
		if (conn_manager == null)
			conn_manager = new MultiThreadedHttpConnectionManager();
		if (cmanager_params == null)
			cmanager_params = new HttpConnectionManagerParams();
		// config the HTTP client visit performance.
		cmanager_params.setDefaultMaxConnectionsPerHost(10240);
		cmanager_params.setMaxTotalConnections(10240);
		cmanager_params.setParameter(HttpMethodParams.USE_EXPECT_CONTINUE, false);
		//cmanager_params.setConnectionTimeout(6000);
		//cmanager_params.setSoTimeout(6000);
		conn_manager.setParams(cmanager_params);
		if (client == null)
			client = new HttpClient(conn_manager);
	}
	
	/**
	 * 内部类，封装http调用方式
	 * HttpClientUtil.getInstance().new Execute().exec("url", null, "GET")
	 * @author langke
	 * @since httpclient4.0.1
	 * @version 1.0
	 */
	public class Execute {
		public Response exec(String url,String body,String method){
			if(method.equalsIgnoreCase("get")){
				return new Execute().get(url);
			}else if(method.equalsIgnoreCase("post")){
				return new Execute().post(url,body);
			}else if(method.equalsIgnoreCase("put")){
				return new Execute().put(url,body);
			}else if(method.equalsIgnoreCase("delete")){
				return new Execute().delete(url);
			}else{
				Response res = new Response();
				res.setCode(400);
				res.setMsg("not found method :"+method);
				return res;
			}
		}
		
		public Response get(String url){
			GetMethod method = null;
			Response res = new Response();
			try {
				if(url.indexOf("?")!=-1){
					try {
						url = new URI(url).toASCIIString();
					} catch (URISyntaxException e) {
						log.error("{}", e.toString());
					}
				}
				method = new GetMethod(url);
				method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
				int status = client.executeMethod(method);
				String resp = method.getResponseBodyAsString();
				res.result= resp;
				res.msg = method.getStatusText();
				res.header = method.getResponseHeader("Content-Type").getValue();
				res.code = status;
			} catch (Exception e) {
				res.setCode(500);
				res.setMsg(e.toString());
				log.error("{}", e.toString());
			} finally{
				if(method != null){
					method.releaseConnection();
				}
			}
			return res;
		}
		
		public Response post(String url, String requestBody){
			PostMethod method = null;
			Response res = new Response();
			try {
				method = new PostMethod(url);
				method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
				if(requestBody != null){
					RequestEntity reqEntity = new StringRequestEntity(requestBody, "application/json", "UTF-8");
					method.setRequestEntity(reqEntity);
				}
				int status = client.executeMethod(method);
				String resp = method.getResponseBodyAsString();
				res.result= resp;
				res.msg = method.getStatusText();
				res.header = method.getResponseHeader("Content-Type").getValue();
				res.code = status;
				return res;
			} catch (Exception e) {
				res.setCode(500);
				res.setMsg(e.toString());
				log.error("{}", e.toString());
			} finally{
				if(method != null){
					method.releaseConnection();
				}
			}
			return res;
		}

		public Response put(String url, String requestBody){
			PutMethod method = null;
			Response res = new Response();
			try {
				method = new PutMethod(url);
				method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
				if(requestBody != null){
					RequestEntity reqEntity = new StringRequestEntity(requestBody, "application/json", "UTF-8");
					method.setRequestEntity(reqEntity);
				}
				int status = client.executeMethod(method);
				String resp = method.getResponseBodyAsString();
				res.result= resp;
				res.msg = method.getStatusText();
				res.header = method.getResponseHeader("Content-Type").getValue();
				res.code = status;
				return res;
			} catch (Exception e) {
				res.setCode(500);
				res.setMsg(e.toString());
				log.error("{}", e.toString());
			} finally{
				if(method != null){
					method.releaseConnection();
				}
			}
			return res;
		}
		

		public Response delete(String url){
			DeleteMethod method = null;
			Response res = new Response();
			try {
				method = new DeleteMethod(url);
				method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
				int status = client.executeMethod(method);
				String resp = method.getResponseBodyAsString();
				res.result= resp;
				res.msg = method.getStatusText();
				res.header = method.getResponseHeader("Content-Type").getValue();
				res.code = status;
				return res;
			} catch (Exception e) {
				res.setCode(500);
				res.setMsg(e.toString());
				log.error("{}", e.toString());
			} finally{
				if(method != null){
					method.releaseConnection();
				}
			}
			return res;
		}
		/**
		 * 失败后，重试3次
		 * @param uri
		 * @param json
		 * @return
		 */
		public Response tryExecute(String uri, String json){
			Response res = post(uri, json);
			if(res != null){
				return res;
			}
			int tryCount = 3;
			while(res == null && (tryCount--) > 0 ){
				res = post(uri, json);
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					log.error("{}", e.toString());
				}
			}
			return res;
		}
	}
	
	public static void main(String[] args) {
		Response resp =  HttpClientUtil.getInstance().new Execute().exec("http://dfs1:9200/test_mem//_search?q=tttext0:functional1", null, "GET");
		System.out.println(resp.code+resp.msg+resp.result);
	}
	
}
