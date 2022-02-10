package com.landray.cms.notify.core.utils;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * HTTP 客户端工具类
 * @version 1.0
 * @author 李世超
 */
public class HttpClientUtil {
	
	/**
	 * 参数放 Body 中进行传输
	 */
	public static final String TRANSMIS_TYPE_BODY = "Body";
	
	/**
	 * 参数放 QueryString 中进行传输
	 */
	public static final String TRANSMIS_TYPE_QUERY_STRING = "QueryString";
	
	/**
	 * 发送 Get 请求
	 * @param url 请求地址
	 * @param params 请求参数
	 * @return 响应数据
	 * @throws Exception
	 */
	public static String sendGetRequest(String url, Map<String, String> params) throws Exception {
		return sendGetRequest(url, params, null);
	}
	
	/**
	 * 发送 Get 请求（携带 Cookie）
	 * @param url 请求地址
	 * @param params 请求参数
	 * @param cookieStore 携带的 Cookie
	 * @return 响应数据
	 * @throws Exception
	 */
	public static String sendGetRequest(String url, Map<String, String> params, CookieStore cookieStore) throws Exception {
		URIBuilder builder = new URIBuilder(url);
		
		List<NameValuePair> nameValuePairs = HttpClientUtil.convertToNameValuePairs(params);
		if(!nameValuePairs.isEmpty()) {
			builder.setParameters(HttpClientUtil.convertToNameValuePairs(params));
		}
		
		// 构建 URI
		URI uri = builder.build();
		HttpGet httpGet = new HttpGet(uri);
		
		return sendRequest(httpGet, cookieStore);
	}
	
	/**
	 * 将 JSON 格式字符串作为请求体进行传输
	 * @param url 请求地址
	 * @param requestBody JSON 格式字符串
	 * @return 响应数据
	 * @throws Exception
	 */
	public static String sendPostRequest(String url, String requestBody) throws Exception {
		return sendPostRequest(url, requestBody, null);
	}
	
	/**
	 * 将 JSON 格式字符串作为请求体进行传输（携带 Cookie）
	 * @param url 请求地址
	 * @param requestBody JSON 格式字符串
	 * @param cookieStore 携带的 Cookie
	 * @return 响应数据
	 * @throws Exception
	 */
	public static String sendPostRequest(String url, String requestBody, CookieStore cookieStore) throws Exception {
		StringEntity stringEntity = new StringEntity(requestBody, Consts.UTF_8);
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
		httpPost.setEntity(stringEntity);
		
		return sendRequest(httpPost, cookieStore);
	}
	
	/**
	 * 发送 Post 请求（仅支持普通字段传输）
	 * @param url 请求地址
	 * @param params key-value 文本参数
	 * @return 响应数据
	 * @throws Exception
	 */
	public static String sendPostRequest(String url, Map<String, String> params) throws Exception {
		return sendPostRequest(url, params, (CookieStore) null, TRANSMIS_TYPE_BODY);
	}
	
	/**
	 * 发送 Post 请求（仅支持普通字段传输）
	 * @param url 请求地址
	 * @param params key-value 文本参数
	 * @param cookieStore 携带的 Cookie
	 * @return 响应数据
	 * @throws Exception
	 */
	public static String sendPostRequest(String url, Map<String, String> params, CookieStore cookieStore) throws Exception {
		return sendPostRequest(url, params, cookieStore, TRANSMIS_TYPE_BODY);
	}
	
	/**
	 * 发送 Post 请求（仅支持普通字段传输）（携带 Cookie）
	 * @param url 请求地址
	 * @param params key-value 文本参数
	 * @param cookieStore 携带的 Cookie
	 * @param transmisType 传输形式
	 * @return 响应数据
	 * @throws Exception
	 */
	public static String sendPostRequest(String url, Map<String, String> params, CookieStore cookieStore, String transmisType) throws Exception {
		HttpPost httpPost = null;
		if(TRANSMIS_TYPE_BODY.equals(transmisType)) {
			httpPost = new HttpPost(url);
			
			if(params != null && !params.isEmpty()) {
				UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(
						HttpClientUtil.convertToNameValuePairs(params), Consts.UTF_8);
				httpPost.setEntity(urlEncodedFormEntity);
			}
		} else {
			URIBuilder builder = new URIBuilder(url);
			
			List<NameValuePair> nameValuePairs = HttpClientUtil.convertToNameValuePairs(params);
			if(!nameValuePairs.isEmpty()) {
				builder.setParameters(HttpClientUtil.convertToNameValuePairs(params));
			}
			
			// 构建 URI
			URI uri = builder.build();
			httpPost = new HttpPost(uri);
		}
		
		return sendRequest(httpPost, cookieStore);
	}
	
	/**
	 * 发送 Post 请求（支持普通字段及文件传输）
	 * @param url 请求地址
	 * @param textParams key-value 文本参数
	 * @param binaryParams key-file[] 二进制参数
	 * @return 响应数据
	 * @throws Exception
	 */
	public static String sendPostRequest(String url, Map<String, String> textParams, 
			Map<String, File[]> binaryParams) throws Exception {
		return sendPostRequest(url, textParams, binaryParams, null);
	}
	
	/**
	 * 发送 Post 请求（支持普通字段及文件传输）（携带 Cookie）
	 * @param url 请求地址
	 * @param textParams key-value 文本参数
	 * @param binaryParams key-file[] 二进制参数
	 * @param cookieStore 携带的 Cookie
	 * @return 响应数据
	 * @throws Exception
	 */
	public static String sendPostRequest(String url, Map<String, String> textParams, 
			Map<String, File[]> binaryParams, CookieStore cookieStore) throws Exception {
		HttpEntity requestEntity = HttpClientUtil.buildMultipartEntity(textParams, binaryParams);
		
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(requestEntity);
		
		return sendRequest(httpPost, cookieStore);
	}
	
	/**
	 * 根据 HttpEntity 发送 Post 请求
	 * @param httpUriRequest 请求 HttpEntity
	 * @return 响应数据
	 * @throws Exception
	 */
	public static String sendRequest(HttpUriRequest httpUriRequest) throws Exception {
		return sendRequest(httpUriRequest, null);
	}
	
	/**
	 * 根据 HttpEntity 发送 Post 请求（携带 cookies）
	 * @param httpUriRequest 请求 HttpEntity
	 * @param cookieStore 携带的 cookies
	 * @return 响应数据
	 * @throws Exception
	 */
	public static String sendRequest(HttpUriRequest httpUriRequest, CookieStore cookieStore) throws Exception {
		String result = null;
		
		CloseableHttpClient httpclient = null;
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
//		httpClientBuilder.setProxy(new HttpHost("127.0.0.1", 8888));	// 设置代理
		
		if(cookieStore != null && !cookieStore.getCookies().isEmpty()) {
			// 设置 Cookie
			httpClientBuilder.setDefaultCookieStore(cookieStore);
		}
		
		setSSLSocketFactory(httpClientBuilder);	// 忽略证书
		
		httpclient = httpClientBuilder.build();
		
		try {
			// 发送请求
			CloseableHttpResponse response = httpclient.execute(httpUriRequest);
			try {
				HttpEntity responseEntity = response.getEntity();
				if (responseEntity != null) {
					result = EntityUtils.toString(responseEntity, Consts.UTF_8);
				}
			} finally {
				response.close();
			}
		} catch(Exception e) {
			throw e;
		} finally {
			try {
				// 关闭连接, 释放资源
				httpclient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 忽略证书
	 * @param httpClientBuilder
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	private static void setSSLSocketFactory(HttpClientBuilder httpClientBuilder) throws Exception {
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
			// 信任所有
			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		}).build();
		// ALLOW_ALL_HOSTNAME_VERIFIER:这个主机名验证器基本上是关闭主机名验证的,实现的是一个空操作，并且不会抛出javax.net.ssl.SSLException异常。
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslContext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		httpClientBuilder.setSSLSocketFactory(sslsf);
    }
	
	/**
	 * 构建 MultipartEntity
	 * @param textParams key-value 文本参数
	 * @param binaryParams key-file[] 二进制参数
	 * @return HttpEntity
	 */
	public static HttpEntity buildMultipartEntity(Map<String, String> textParams, Map<String, File[]> binaryParams) {
		
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		multipartEntityBuilder.setMode(HttpMultipartMode.RFC6532);	// 处理因文件名为中文时导致乱码或取不到文件
		multipartEntityBuilder.setCharset(Consts.UTF_8);
		
		// 设置文本参数
		HttpClientUtil.setTextParams(multipartEntityBuilder, textParams);
		
		// 设置二进制参数
		HttpClientUtil.setBinaryParams(multipartEntityBuilder, binaryParams);
		
		return multipartEntityBuilder.build();
	}
	
	/**
	 * 为 MultipartEntityBuilder 设置文本参数
	 * @param multipartEntityBuilder 
	 * @param textParams 文本参数
	 * @return multipartEntityBuilder
	 */
	public static MultipartEntityBuilder setTextParams(MultipartEntityBuilder multipartEntityBuilder, Map<String, String> textParams) {
		// 设置文本参数
		if(multipartEntityBuilder != null && textParams != null && textParams.size() > 0) {
			for(Map.Entry<String, String> entrySet : textParams.entrySet()) {
				multipartEntityBuilder.addTextBody(entrySet.getKey(), entrySet.getValue(), ContentType.APPLICATION_JSON);
			}
		}
		return multipartEntityBuilder;
	}
	
	/**
	 * 为 MultipartEntityBuilder 设置二进制参数
	 * @param multipartEntityBuilder
	 * @param binaryParams 二进制参数
	 * @return multipartEntityBuilder
	 */
	public static MultipartEntityBuilder setBinaryParams(MultipartEntityBuilder multipartEntityBuilder, Map<String, File[]> binaryParams) {
		// 设置二进制参数
		if(multipartEntityBuilder != null && binaryParams != null && binaryParams.size() > 0) {
			for(Map.Entry<String, File[]> entrySet : binaryParams.entrySet()) {
				String key = entrySet.getKey();
				File[] fileArray = entrySet.getValue();
				
				if(fileArray == null || fileArray.length == 0) 
					continue;
					
				for(File file : fileArray) {
					multipartEntityBuilder.addBinaryBody(key, file, ContentType.DEFAULT_BINARY, file.getName());
				}
			}
		}
		return multipartEntityBuilder;
	}
	
	/**
	 * 将参数转换为 NameValuePair
	 * @param params
	 * @return List<NameValuePair>
	 */
	public static List<NameValuePair> convertToNameValuePairs(Map<String, String> params) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		if(params != null && params.size() > 0) {
			for(Map.Entry<String,String> entrySet : params.entrySet()) {
				nameValuePairs.add(new BasicNameValuePair(entrySet.getKey(), entrySet.getValue()));
			}
		}
		return nameValuePairs;
	}
	
	public static void main(String[] args) throws Exception {
		String sendPostRequest = sendPostRequest("https://localhost:8443/ekp/qd/review/qd_review_anonymous/qdReviewAnonymous.do?method=test", "");
		System.out.println(sendPostRequest);
	}
	
}
