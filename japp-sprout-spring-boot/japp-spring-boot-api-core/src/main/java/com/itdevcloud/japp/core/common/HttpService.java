/*
 * Copyright (c) 2018 the original author(s). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.itdevcloud.japp.core.common;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ResponseStatus;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.util.StringUtil;

/**
 *
 * @author Marvin Sun
 * @since 1.0.0
 * 
 * Sample header construction:
 * 
 * Map<String, String> headers = new LinkedHashMap<>();
 * headers.put("Authorization", "Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJsaW.5nIiwiZGlzdHJpY");
 * headers.put("Content-Type", "application/x-www-form-urlencoded");
 * 
 * Sample parameter construction:
 * 
 * Map<String,Object> params = new LinkedHashMap<>();
 * params.put("code", code);
 * params.put("redirect_uri", redirectUri");
 * 
 */

@Component
public class HttpService implements AppFactoryComponentI {

	private static final Logger log = LogManager.getLogger(HttpService.class);
	
	static {
	    //for localhost testing only
	    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
	    new javax.net.ssl.HostnameVerifier(){

	        public boolean verify(String hostname,
	                javax.net.ssl.SSLSession sslSession) {
	            if (hostname.equals("localhost")) {
	                return true;
	            }
	            return false;
	        }
	    });
	}
	

	@PostConstruct
	public void init() {
	}
	
	private Proxy getHttpProxy() {
		String httpProxyServer = ConfigFactory.appConfigService.getPropertyAsString(AppConfigKeys.JAPPCORE_HTTP_PROXY_SERVER);
		int httpProxyPort = ConfigFactory.appConfigService.getPropertyAsInteger(AppConfigKeys.JAPPCORE_HTTP_PROXY_PORT);
		if (StringUtil.isEmptyOrNull(httpProxyServer) || httpProxyPort == 0) {
			return null;
		}
		log.info("getHttpProxy() - httpProxyServer = " + httpProxyServer + ", httpProxyPort = " + httpProxyPort);
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyServer, httpProxyPort));
	}

	public HttpResponse doGet(String urlStr, Map<String, String> headers, boolean useProxy) {
		log.debug("HttpService.doGet() ==== " + urlStr + ", useProxy=" + useProxy);
		BufferedReader in = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlStr);
			Proxy proxy = getHttpProxy();

			if (proxy == null || !useProxy) {
				conn = (HttpURLConnection) url.openConnection();
			} else {
				conn = (HttpURLConnection) url.openConnection(proxy);
			}
			// optional default is GET
			conn.setRequestMethod("GET");

			// add request header
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					// log.info("Request Header : " + entry.getKey() + " = " + entry.getValue());
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			int responseCode = conn.getResponseCode();
			log.info("\nSending 'GET' request to URL : " + url);
			log.info("Response Code : " + responseCode);
			if (responseCode != 200) {
				throw new AppException(ResponseStatus.STATUS_CODE_ERROR_SYSTEM_ERROR,
						"doGet: " + urlStr + ", return Respone code: " + responseCode);
			}

			// print out all headers
			Map<String, List<String>> map = conn.getHeaderFields();
//			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
//				log.info("Header : " + entry.getKey() + " = " + entry.getValue());
//			}

			// body
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}

			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setHeaderMap(map);
			httpResponse.setResposebody(response.toString());
			return httpResponse;

		} catch (Throwable t) {
			log.error(AppUtil.getStackTrace(t));
			throw AppUtil.throwRuntimeException(t);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error(AppUtil.getStackTrace(e));
				}
			}
			if (conn != null) {
				conn.disconnect();
			}

		}
	}


	public HttpResponse doPost(String urlStr, Map<String, String> headers, Map<String, Object> params, boolean useProxy) {
		log.debug("HttpService.doPost() ==== " + urlStr + ", useProxy=" + useProxy);
		Reader in = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlStr);

			StringBuilder postData = new StringBuilder();
			if (params != null) {
				for (Map.Entry<String, Object> param : params.entrySet()) {
					if (postData.length() != 0) {
						postData.append('&');
					}
					postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
					postData.append('=');
					postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
				}
			}
			byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

			Proxy proxy = getHttpProxy();

			if (proxy == null || !useProxy) {
				conn = (HttpURLConnection) url.openConnection();
			} else {
				conn = (HttpURLConnection) url.openConnection(proxy);
			}
			conn.setRequestMethod("POST");

			// add request header
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					// log.info("Request Header : " + entry.getKey() + " = " + entry.getValue());
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

			// Send post request
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			// conn.getOutputStream().write(postDataBytes);

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.write(postDataBytes);
			wr.flush();
			wr.close();

			// print all Response headers
			Map<String, List<String>> map = conn.getHeaderFields();
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				// log.info("Response Header : " + entry.getKey() + " = " + entry.getValue());
			}

			// print body
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (int c; (c = in.read()) >= 0;) {
				sb.append((char) c);
			}
			// log.info("response = " + response);
			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setHeaderMap(map);
			httpResponse.setResposebody(sb.toString());
			return httpResponse;
		} catch (Throwable t) {
			log.error(AppUtil.getStackTrace(t));
			throw AppUtil.throwRuntimeException(t);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error(AppUtil.getStackTrace(e));
				}
			}
			if (conn != null) {
				conn.disconnect();
			}

		}
	}

}
