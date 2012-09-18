package org.langke.testscript.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.langke.util.logging.ESLogger;
import org.langke.util.logging.Loggers;

/**
 * URLConnection 调用http方法封装
 * @author langke
 * @since JDK1.6
 * @version 1.0
 */
public class HttpSupporter {
	private static ESLogger log = Loggers.getLogger(HttpSupporter.class);
	
	public static String doRequest(String url,String body, String method) {
		String result = null;
		byte[] bodyByte ;
		try {
			if(body==null)
				bodyByte = null;
			else
				bodyByte = body.getBytes("UTF-8");
 			result = doRequest( url,bodyByte ,  method) ;
		} catch (UnsupportedEncodingException e) {
			result = e.toString();
			log.error("{}", e);
		}
		return result;
	}
	
	public static String doRequest(String url, byte[] body, String method) {
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		InputStream is = null;
		String result=null;
		URLConnection conn = null;
		HttpURLConnection httpconn = null;
		HttpsURLConnection httpsconn = null;
		try {
			if(body!=null && body.length==0) body = null;
			if(method.equals("GET") && url.indexOf("?")!=-1){
				//url = url.substring(0, url.indexOf('?')+1)+java.net.URLEncoder.encode(url.substring(url.indexOf('?')+1),"UTF-8");
				//url = url.replace("%3D", "=");
				try {
					url = new URI(url).toASCIIString();
				} catch (URISyntaxException e) {
					log.error("{}", e);
				}
			}
			conn = new URL(url).openConnection();
			if (conn instanceof HttpURLConnection) {
				httpconn = (HttpURLConnection) conn;
				httpconn.setRequestMethod(method);		
				if (body != null){
					httpconn.setDoOutput(true);
					httpconn.getOutputStream().write(body);
				}else{
					httpconn.setDoOutput(false);
				}
				int code=httpconn.getResponseCode();
				if(code!=200){
					is=httpconn.getErrorStream();
				}else{
					is=httpconn.getInputStream();
				}
			} else if (conn instanceof HttpsURLConnection) {
				httpsconn = (HttpsURLConnection) conn;
				httpsconn.setHostnameVerifier(new HostnameVerifier() {
			
					@Override
					public boolean verify(String arg0, SSLSession arg1) {
						return true;
					}
				});
				httpsconn.setRequestMethod(method);			
				if (body != null){
					httpsconn.setDoOutput(true);
					httpsconn.getOutputStream().write(body);
				}
				int code=httpsconn.getResponseCode();
				if(code!=200){
					is=httpsconn.getErrorStream();
				}else{
					is = httpsconn.getInputStream();
				}
			}
			byte[] buf = new byte[1024];
			int amount;
			if(is!=null){
				while ((amount = is.read(buf)) != -1) {
					content.write(buf, 0, amount);
				}
			}
		} catch (MalformedURLException e) {
			log.error("{}", e);
		} catch (IOException e) {
			log.error("{}", e);
		}
		try {
			 result=new String(content.toByteArray(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("{}", e);
		}
		try{
			if(content != null) content.close(); content = null;
			if(is != null) is.close(); is = null;
			if(httpconn!= null) httpconn.disconnect();httpconn = null;
			if(httpsconn != null)httpsconn.disconnect();
		}catch(Exception e){
			log.error("{}", e);
		}
		return result;
	}

	public Response getRequestToResponse(String url, String body, String method) {
		byte[] bodyByte  = null;
		try {
			if(body==null)
				bodyByte = null;
			else if(body.length()==0)
				body = null;
			else
				bodyByte = body.getBytes("UTF-8");
			if(method.equals("GET") && url.indexOf("?")!=-1){
				//url = url.substring(0, url.indexOf('?')+1)+java.net.URLEncoder.encode(url.substring(url.indexOf('?')+1),"UTF-8");
				//url = url.replace("%3D", "=");			
				try {
					url = new URI(url).toASCIIString();
				} catch (URISyntaxException e) {
					log.error("{}", e);
				}
			}
		} catch (UnsupportedEncodingException e) {
			log.error("{}", e);
		}
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		InputStream is = null;
		Response response= new Response();
		String result = null;
		URLConnection conn = null;
		HttpURLConnection httpconn = null;
		HttpsURLConnection httpsconn = null;
		try {
			conn = new URL(url).openConnection();
			if (conn instanceof HttpURLConnection) {
				httpconn = (HttpURLConnection) conn;
				httpconn.setRequestMethod(method);
				if (body != null){
					httpconn.setDoOutput(true);
					httpconn.getOutputStream().write(bodyByte);
				}else{
					httpconn.setDoOutput(false);
				}
				int code=httpconn.getResponseCode();
				if(code!=200){
					is=httpconn.getErrorStream();
				}else{
					is=httpconn.getInputStream();
				}
			} else if (conn instanceof HttpsURLConnection) {
				httpsconn = (HttpsURLConnection) conn;
				httpsconn.setHostnameVerifier(new HostnameVerifier() {
			
					@Override
					public boolean verify(String arg0, SSLSession arg1) {
						return true;
					}
				});
				httpsconn.setRequestMethod(method);			
				if (body != null){
					httpsconn.setDoOutput(true);
					httpsconn.getOutputStream().write(bodyByte);
				}
				int code=httpsconn.getResponseCode();
				if(code!=200){
					is=httpsconn.getErrorStream();
				}else{
					is = httpsconn.getInputStream();
				}
			}
			byte[] buf = new byte[1024];
			int amount;
			if(is!=null){
				while ((amount = is.read(buf)) != -1) {
					content.write(buf, 0, amount);
				}
				 result=new String(content.toByteArray(),"UTF-8");
			}
			response.setCode(httpconn.getResponseCode());
			response.setMsg(httpconn.getResponseMessage());
			response.setResult(result);
		} catch (MalformedURLException e) {
			log.error("{}", e);
		} catch (Exception e) {			
			log.error("{}", e);
		} finally{
			try{
				if(content != null) content.close();
				if(is != null) is.close();
				if(httpconn!= null) httpconn.disconnect();
				if(httpsconn != null)httpsconn.disconnect();
			}catch(Exception e){
				log.error("{}", e);
			}
		}
		return response;
	}
}