package org.walkframework.base.tools.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.walkframework.base.system.exception.ClientProtocolRuntimeException;

import com.alibaba.fastjson.JSON;

/**
 * @author shf675
 * 
 */
public class HttpClientUtil {

	private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);

	/**
	 * Socket读数据的超时时间
	 */
	private static final int DEFAULT_SOCKET_TIMEOUT = 10000;

	/**
	 * 通过网络与服务器建立连接的超时时间
	 */
	private static final int DEFAULT_CONNECT_TIMEOUT = 10000;

	/** GBK编码 */
	public static final String GBK = "GBK";

	/** UTF-8编码 */
	public static final String UTF8 = "UTF-8";

	/**
	 * 自定义配置
	 */
	private static RequestConfig config = RequestConfig.custom().setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).setConnectTimeout(DEFAULT_CONNECT_TIMEOUT).build();

	/**
	 * get方式请求url，返回结果
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String get(String url) throws Exception {
		return get(url, UTF8, null, null);
	}

	/**
	 * get方式请求url，返回结果
	 * 
	 * @param url
	 * @param charset
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public static String get(String url, String charset, Map<String, String> headers, RequestConfig config) {
		return execute(url, null, new HttpGet(url), charset, headers, config);
	}

	/**
	 * post方式请求url，返回结果
	 * 
	 * @param url
	 * @param loginParam
	 * @param params
	 * @param charset
	 *            指定返回字符编码
	 * @return
	 * @throws Exception
	 */
	public static String post(String url, Object params) {
		return post(url, params, UTF8, null, null);
	}

	/**
	 * post方式请求url，返回结果
	 * 
	 * @param url
	 * @param loginParam
	 * @param params
	 * @param charset
	 *            指定返回字符编码
	 * @return
	 * @throws Exception
	 */
	public static String post(String url, Object params, String charset, Map<String, String> headers, RequestConfig config) {
		return execute(url, params, new HttpPost(url), charset, headers, config);
	}

	/**
	 * 执行请求
	 * 
	 * @param url
	 * @param params
	 * @param request
	 * @param charset
	 * @param headers
	 * @return
	 */
	private static String execute(String url, Object params, HttpRequestBase request, String charset, Map<String, String> headers, RequestConfig config) {
		long t1 = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug("Start======================================================================");
			log.debug("request url:{}", url);
			log.debug("request params:{}", params);
			log.debug("request headers:{}", headers);
		}

		String responseBody;
		CloseableHttpClient httpclient = getHttpClient(url);

		// 处理请求头
		if (!CollectionUtils.isEmpty(headers)) {
			if (!headers.containsKey("Accept-Language")) {
				request.addHeader("Accept-Language", "zh-cn");
			}
			if (!headers.containsKey("Cache-Control")) {
				request.addHeader("Cache-Control", "no-cache");
			}
			if (!headers.containsKey("Connection")) {
				request.addHeader("Connection", "Keep-Alive");
			}
			if (!headers.containsKey("User-Agent")) {
				request.addHeader("User-Agent", "Mozilla/4.0 (compat ible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E");
			}

			// 循环添加其他
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				request.addHeader(entry.getKey(), entry.getValue());
			}
		} else {
			//添加默认
			request.addHeader("Accept-Language", "zh-cn");
			request.addHeader("Cache-Control", "no-cache");
			request.addHeader("Connection", "Keep-Alive");
			request.addHeader("User-Agent", "Mozilla/4.0 (compat ible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E");
		}
		try {
			//自定义配置设置
			request.setConfig(config == null ? HttpClientUtil.config : config);
			//请求参数处理
			if (request instanceof HttpPost && params != null) {
				HttpPost httppost = ((HttpPost) request);
				if (params instanceof JSON) {
					request.addHeader("Accept", "application/json");
					request.addHeader("Content-Type", "application/json");
					httppost.setEntity(new StringEntity(JSON.toJSONString(params), charset));
				} else if(params instanceof String){
					httppost.setEntity(new StringEntity(params.toString(), charset));
				} else {
					httppost.setEntity(new UrlEncodedFormEntity(getPostParam(params), charset));
				}
			}
			//获取相应内容
			responseBody = httpclient.execute(request, getResponseHandler(charset));
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
			throw new ClientProtocolRuntimeException(e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (httpclient != null) {
					httpclient.close();
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("response body:{}", responseBody);
			log.debug("cost time {}s", ((System.currentTimeMillis() - t1) / 1000.0D));
			log.debug("End======================================================================");
		}
		return responseBody;
	}

	/**
	 * 获取httpClient
	 * 
	 * @param url
	 * @return
	 */
	private static CloseableHttpClient getHttpClient(String url) {
		if (StringUtil.isEmpty(url)) {
			throw new RuntimeException("request url cannot be empty!");
		}
		if (url.toLowerCase().startsWith("https://")) {
			return HttpClientUtil.createSSLClientDefault();
		}
		return HttpClients.createDefault();
	}

	/**
	 * 创建ssl client
	 * 
	 * @return
	 */
	private static CloseableHttpClient createSSLClientDefault() {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();

			X509TrustManager tm = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			sslContext.init(null, new TrustManager[] { tm }, null);
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
			
			//忽略域名。解决问题：www.xxx.com does not match the certificate subject provided by the peer
//			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1.2" }, null, new HostnameVerifier() {
//				@Override
//				public boolean verify(String hostname, SSLSession session) {
//					hostname = "*.chinaunicom.cn";
//					return SSLConnectionSocketFactory.getDefaultHostnameVerifier().verify(hostname, session);
//				}
//			});
			
			
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (KeyManagementException e) {
			log.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage(), e);
		} catch (KeyStoreException e) {
			log.error(e.getMessage(), e);
		}
		return HttpClients.createDefault();
	}

	/**
	 * 获取请求参数
	 * 
	 * 根据参数类型区分获取参数的结果
	 * 
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List<NameValuePair> getPostParam(Object params) {
		if (params instanceof List) {
			return (List<NameValuePair>) params;
		} else if (params instanceof Map) {
			return getPostParam((Map<String, Object>) params);
		}
		log.warn("Unrecognized param type {}, use empty param list.", params);
		return new ArrayList<NameValuePair>();
	}

	/**
	 * 获取请求参数
	 * 
	 * @param params
	 * @return
	 */
	private static List<NameValuePair> getPostParam(Map<String, Object> params) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (params != null) {
			Object value = null;
			for (String key : params.keySet()) {
				value = params.get(key);
				if (value instanceof Object[]) {
					for (Object obj : (Object[]) value) {
						nvps.add(new BasicNameValuePair(key, getString(obj)));
					}
				} else if (value instanceof List) {
					for (Object obj : (List<?>) value) {
						nvps.add(new BasicNameValuePair(key, getString(obj)));
					}
				} else {
					nvps.add(new BasicNameValuePair(key, getString(value)));
				}
			}
		}
		return nvps;
	}

	private static String getString(Object obj) {
		return null == obj ? "" : obj.toString();
	}

	/**
	 * 获取字符编码的handler
	 * 
	 * @param charset
	 * @return
	 */
	private static ResponseHandler<String> getResponseHandler(String charset) {
		ResponseHandler<String> handler = ResponseHandlerSet.CHARSET_HANDLER.get(charset);
		handler = null == handler ? new CharsetResponseHandler(charset) : handler;
		return handler;
	}

	/**
	 * 响应处理器集合
	 * 
	 * @author MQK
	 * 
	 */
	@SuppressWarnings("serial")
	private interface ResponseHandlerSet {

		/** GBK响应处理器 */
		ResponseHandler<String> GBK_HANDLER = new CharsetResponseHandler(GBK);

		/** UTF-8响应处理器 */
		ResponseHandler<String> UTF8_HANDLER = new CharsetResponseHandler(UTF8);

		/**
		 * 响应处理器与字符集对应关系
		 */
		Map<String, ResponseHandler<String>> CHARSET_HANDLER = new HashMap<String, ResponseHandler<String>>() {
			{
				put(GBK, GBK_HANDLER);
				put(UTF8, UTF8_HANDLER);
			}
		};

	}

	private static class CharsetResponseHandler implements ResponseHandler<String> {

		private String charset;

		public CharsetResponseHandler(String charset) {
			this.charset = charset;
		}

		@Override
		public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

			HttpEntity entity = response.getEntity();
			String responseBody = entity != null ? EntityUtils.toString(entity, this.charset) : null;
			int status = response.getStatusLine().getStatusCode();
			if (status >= 200 && status <= 300) {
				return responseBody;
			} else {
				throw new ClientProtocolException("Unexpected response status: " + status + "\nresponseBody:" + responseBody);
			}
		}

	}
	
	public static void main(String[] args) {
		post("https://10.124.147.36:8000/api/naturePersonCenter/faceRecognition/faceCompare/v1", new HashMap());
	}
}
