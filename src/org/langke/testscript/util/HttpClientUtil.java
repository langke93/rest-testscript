package org.langke.testscript.util;

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
import org.langke.util.Strings;
import org.langke.util.logging.ESLogger;
import org.langke.util.logging.Loggers;
 
/**
 * 2012
 * @author langke
 *
 */
public class HttpClientUtil {

	private static ESLogger log = Loggers.getLogger(HttpClientUtil.class);
	private static HttpClient client = null;
	private static MultiThreadedHttpConnectionManager conn_manager = null;
	private static HttpConnectionManagerParams cmanager_params = null;
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
		cmanager_params.setConnectionTimeout(6000);
		cmanager_params.setSoTimeout(6000);
		conn_manager.setParams(cmanager_params);
		if (client == null)
			client = new HttpClient(conn_manager);
	}
	
	public static Response exec(String url,String body,String method){
		if(method.equalsIgnoreCase("get")){
			return get(url);
		}else if(method.equalsIgnoreCase("post")){
			return post(url,body);
		}else if(method.equalsIgnoreCase("put")){
			return put(url,body);
		}else if(method.equalsIgnoreCase("delete")){
			return delete(url);
		}else{
			Response res = new Response();
			res.setCode(400);
			res.setMsg("not found method :"+method);
			return res;
		}
	}
	public static Response get(String url){
		GetMethod get = null;
		Response res = new Response();
		try {
			if(url.indexOf("?")!=-1){
				try {
					url = new URI(url).toASCIIString();
				} catch (URISyntaxException e) {
					log.error("{}", Strings.throwableToString(e));
				}
			}
			get = new GetMethod(url);
			get.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			int status = client.executeMethod(get);
			String resp = get.getResponseBodyAsString();
			res.result= resp;
			res.msg = get.getStatusText();
			res.code = status;
		} catch (Exception e) {
			res.setCode(500);
			res.setMsg(Strings.throwableToString(e));
			log.error("{}", Strings.throwableToString(e));
		} finally{
			if(get != null){
				get.releaseConnection();
			}
		}
		return res;
	}
	public static Response post(String url, String requestBody){
		PostMethod post = null;
		Response res = new Response();
		try {
			post = new PostMethod(url);
			post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			if(requestBody != null){
				RequestEntity reqEntity = new StringRequestEntity(requestBody, "application/json", "UTF-8");
				post.setRequestEntity(reqEntity);
			}
			int status = client.executeMethod(post);
			String resp = post.getResponseBodyAsString();
			res.result= resp;
			res.msg = post.getStatusText();
			res.code = status;
			return res;
		} catch (Exception e) {
			res.setCode(500);
			res.setMsg(Strings.throwableToString(e));
			log.error("{}", Strings.throwableToString(e));
		} finally{
			if(post != null){
				post.releaseConnection();
			}
		}
		return res;
	}

	public static Response put(String url, String requestBody){
		PutMethod put = null;
		Response res = new Response();
		try {
			put = new PutMethod(url);
			put.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			if(requestBody != null){
				RequestEntity reqEntity = new StringRequestEntity(requestBody, "application/json", "UTF-8");
				put.setRequestEntity(reqEntity);
			}
			int status = client.executeMethod(put);
			String resp = put.getResponseBodyAsString();
			res.result= resp;
			res.msg = put.getStatusText();
			res.code = status;
			return res;
		} catch (Exception e) {
			res.setCode(500);
			res.setMsg(Strings.throwableToString(e));
			log.error("{}", Strings.throwableToString(e));
		} finally{
			if(put != null){
				put.releaseConnection();
			}
		}
		return res;
	}
	

	public static Response delete(String url){
		DeleteMethod delete = null;
		Response res = new Response();
		try {
			delete = new DeleteMethod(url);
			delete.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			int status = client.executeMethod(delete);
			String resp = delete.getResponseBodyAsString();
			res.result= resp;
			res.msg = delete.getStatusText();
			res.code = status;
			return res;
		} catch (Exception e) {
			res.setCode(500);
			res.setMsg(Strings.throwableToString(e));
			log.error("{}", Strings.throwableToString(e));
		} finally{
			if(delete != null){
				delete.releaseConnection();
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
	public static Response tryExecute(String uri, String json){
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
				log.error("{}", Strings.throwableToString(e));
			}
		}
		return res;
	}
	
	public static void main(String[] args) {
		Response resp = get("http://dfs1:9200/test//_search?q=tttext1:测试文本1");
		System.out.println(resp.code+resp.msg+resp.result);
	}
	
}
