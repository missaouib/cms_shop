/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: localhost
 * License: localhost/license
 * FileId: QJSGdqYNWGjJpoBrkSdoLqaYfl0nRel7
 */
package com.igomall.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.igomall.Setting;

/**
 * Utils - Web
 * 
 * @author ?????? Team
 * @version 6.1
 */
public final class WebUtils {

	/**
	 * PoolingHttpClientConnectionManager
	 */
	private static final PoolingHttpClientConnectionManager HTTP_CLIENT_CONNECTION_MANAGER;

	/**
	 * CloseableHttpClient
	 */
	private static final CloseableHttpClient HTTP_CLIENT;

	static {
		HTTP_CLIENT_CONNECTION_MANAGER = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", SSLConnectionSocketFactory.getSocketFactory()).build());
		HTTP_CLIENT_CONNECTION_MANAGER.setDefaultMaxPerRoute(100);
		HTTP_CLIENT_CONNECTION_MANAGER.setMaxTotal(200);
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(60000).setConnectTimeout(60000).setSocketTimeout(60000).build();
		HTTP_CLIENT = HttpClientBuilder.create().setConnectionManager(HTTP_CLIENT_CONNECTION_MANAGER).setDefaultRequestConfig(requestConfig).build();
	}

	/**
	 * ???????????????
	 */
	private WebUtils() {
	}

	/**
	 * ??????HttpServletRequest
	 * 
	 * @return HttpServletRequest
	 */
	public static HttpServletRequest getRequest() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		return requestAttributes != null && requestAttributes instanceof ServletRequestAttributes ? ((ServletRequestAttributes) requestAttributes).getRequest() : null;
	}

	/**
	 * ??????HttpServletResponse
	 * 
	 * @return HttpServletResponse
	 */
	public static HttpServletResponse getResponse() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		return requestAttributes != null && requestAttributes instanceof ServletRequestAttributes ? ((ServletRequestAttributes) requestAttributes).getResponse() : null;
	}

	/**
	 * ???????????????AJAX??????
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return ?????????AJAX??????
	 */
	public static boolean isAjaxRequest(HttpServletRequest request) {
		Assert.notNull(request, "[Assertion failed] - request is required; it must not be null");

		return StringUtils.equalsIgnoreCase(request.getHeader("X-Requested-With"), "XMLHttpRequest");
	}

	/**
	 * ??????cookie
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param name
	 *            Cookie??????
	 * @param value
	 *            Cookie???
	 * @param maxAge
	 *            ?????????(??????: ???)
	 * @param path
	 *            ??????
	 * @param domain
	 *            ???
	 * @param secure
	 *            ??????????????????
	 */
	public static void addCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, Integer maxAge, String path, String domain, Boolean secure) {
		Assert.notNull(request, "[Assertion failed] - request is required; it must not be null");
		Assert.notNull(response, "[Assertion failed] - response is required; it must not be null");
		Assert.hasText(name, "[Assertion failed] - name must have text; it must not be null, empty, or blank");
		Assert.hasText(value, "[Assertion failed] - value must have text; it must not be null, empty, or blank");

		try {
			name = URLEncoder.encode(name, "UTF-8");
			value = URLEncoder.encode(value, "UTF-8");
			Cookie cookie = new Cookie(name, value);
			if (maxAge != null) {
				cookie.setMaxAge(maxAge);
			}
			if (StringUtils.isNotEmpty(path)) {
				cookie.setPath(path);
			}
			if (StringUtils.isNotEmpty(domain)) {
				cookie.setDomain(domain);
			}
			if (secure != null) {
				cookie.setSecure(secure);
			}
			response.addCookie(cookie);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * ??????cookie
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param name
	 *            Cookie??????
	 * @param value
	 *            Cookie???
	 * @param maxAge
	 *            ?????????(??????: ???)
	 */
	public static void addCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, Integer maxAge) {
		Assert.notNull(request, "[Assertion failed] - request is required; it must not be null");
		Assert.notNull(response, "[Assertion failed] - response is required; it must not be null");
		Assert.hasText(name, "[Assertion failed] - name must have text; it must not be null, empty, or blank");
		Assert.hasText(value, "[Assertion failed] - value must have text; it must not be null, empty, or blank");

		Setting setting = SystemUtils.getSetting();
		addCookie(request, response, name, value, maxAge, setting.getCookiePath(), setting.getCookieDomain(), null);
	}

	/**
	 * ??????cookie
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param name
	 *            Cookie??????
	 * @param value
	 *            Cookie???
	 */
	public static void addCookie(HttpServletRequest request, HttpServletResponse response, String name, String value) {
		Assert.notNull(request, "[Assertion failed] - request is required; it must not be null");
		Assert.notNull(response, "[Assertion failed] - response is required; it must not be null");
		Assert.hasText(name, "[Assertion failed] - name must have text; it must not be null, empty, or blank");
		Assert.hasText(value, "[Assertion failed] - value must have text; it must not be null, empty, or blank");

		Setting setting = SystemUtils.getSetting();
		addCookie(request, response, name, value, null, setting.getCookiePath(), setting.getCookieDomain(), null);
	}

	/**
	 * ??????cookie
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param name
	 *            Cookie??????
	 * @return Cookie???????????????????????????null
	 */
	public static String getCookie(HttpServletRequest request, String name) {
		Assert.notNull(request, "[Assertion failed] - request is required; it must not be null");
		Assert.hasText(name, "[Assertion failed] - name must have text; it must not be null, empty, or blank");

		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			try {
				name = URLEncoder.encode(name, "UTF-8");
				for (Cookie cookie : cookies) {
					if (StringUtils.equals(name, cookie.getName())) {
						return URLDecoder.decode(cookie.getValue(), "UTF-8");
					}
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * ??????cookie
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param name
	 *            Cookie??????
	 * @param path
	 *            ??????
	 * @param domain
	 *            ???
	 */
	public static void removeCookie(HttpServletRequest request, HttpServletResponse response, String name, String path, String domain) {
		Assert.notNull(request, "[Assertion failed] - request is required; it must not be null");
		Assert.notNull(response, "[Assertion failed] - response is required; it must not be null");
		Assert.hasText(name, "[Assertion failed] - name must have text; it must not be null, empty, or blank");

		try {
			name = URLEncoder.encode(name, "UTF-8");
			Cookie cookie = new Cookie(name, null);
			cookie.setMaxAge(0);
			if (StringUtils.isNotEmpty(path)) {
				cookie.setPath(path);
			}
			if (StringUtils.isNotEmpty(domain)) {
				cookie.setDomain(domain);
			}
			response.addCookie(cookie);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * ??????cookie
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param name
	 *            Cookie??????
	 */
	public static void removeCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		Assert.notNull(request, "[Assertion failed] - request is required; it must not be null");
		Assert.notNull(response, "[Assertion failed] - response is required; it must not be null");
		Assert.hasText(name, "[Assertion failed] - name must have text; it must not be null, empty, or blank");

		Setting setting = SystemUtils.getSetting();
		removeCookie(request, response, name, setting.getCookiePath(), setting.getCookieDomain());
	}

	/**
	 * ????????????
	 * 
	 * @param query
	 *            ???????????????
	 * @param encoding
	 *            ????????????
	 * @return ??????
	 */
	public static Map<String, String> parse(String query, String encoding) {
		Assert.hasText(query, "[Assertion failed] - query must have text; it must not be null, empty, or blank");

		Charset charset;
		if (StringUtils.isNotEmpty(encoding)) {
			charset = Charset.forName(encoding);
		} else {
			charset = Charset.forName("UTF-8");
		}
		List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(query, charset);
		Map<String, String> parameterMap = new HashMap<>();
		for (NameValuePair nameValuePair : nameValuePairs) {
			parameterMap.put(nameValuePair.getName(), nameValuePair.getValue());
		}
		return parameterMap;
	}

	/**
	 * ????????????
	 * 
	 * @param query
	 *            ???????????????
	 * @return ??????
	 */
	public static Map<String, String> parse(String query) {
		Assert.hasText(query, "[Assertion failed] - query must have text; it must not be null, empty, or blank");

		return parse(query, null);
	}

	/**
	 * ?????????
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param url
	 *            URL
	 * @param contextRelative
	 *            ???????????????????????????
	 * @param http10Compatible
	 *            ????????????HTTP1.0
	 */
	public static void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url, boolean contextRelative, boolean http10Compatible) {
		Assert.notNull(request, "[Assertion failed] - request is required; it must not be null");
		Assert.notNull(response, "[Assertion failed] - response is required; it must not be null");
		Assert.hasText(url, "[Assertion failed] - url must have text; it must not be null, empty, or blank");

		StringBuilder targetUrl = new StringBuilder();
		if (contextRelative && url.startsWith("/")) {
			targetUrl.append(request.getContextPath());
		}
		targetUrl.append(url);
		String encodedRedirectURL = response.encodeRedirectURL(String.valueOf(targetUrl));
		if (http10Compatible) {
			try {
				response.sendRedirect(encodedRedirectURL);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} else {
			response.setStatus(303);
			response.setHeader("Location", encodedRedirectURL);
		}
	}

	/**
	 * ?????????
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param url
	 *            URL
	 */
	public static void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) {
		sendRedirect(request, response, url, true, true);
	}

	/**
	 * POST??????
	 * 
	 * @param url
	 *            URL
	 * @param xml
	 *            XML
	 * @return ????????????
	 */
	public static String post(String url, String xml) {
		Assert.hasText(url, "[Assertion failed] - url must have text; it must not be null, empty, or blank");

		return post(url, null, new StringEntity(xml, "UTF-8"));
	}

	/**
	 * POST??????
	 * 
	 * @param url
	 *            URL
	 * @param parameterMap
	 *            ????????????
	 * @return ????????????
	 */
	public static String post(String url, Map<String, Object> parameterMap) {
		Assert.hasText(url, "[Assertion failed] - url must have text; it must not be null, empty, or blank");

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<>();
			if (parameterMap != null) {
				for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
					String name = entry.getKey();
					String value = ConvertUtils.convert(entry.getValue());
					if (StringUtils.isNotEmpty(name)) {
						nameValuePairs.add(new BasicNameValuePair(name, value));
					}
				}
			}
			return post(url, null, new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * POST??????
	 * 
	 * @param url
	 *            URL
	 * @param header
	 *            Header
	 * @param entity
	 *            HttpEntity
	 * @return ????????????
	 */
	public static String post(String url, Header header, HttpEntity entity) {
		return post(url, header, entity, String.class);
	}

	/**
	 * POST??????
	 * 
	 * @param url
	 *            URL
	 * @param header
	 *            Header
	 * @param entity
	 *            HttpEntity
	 * @param resultType
	 *            ??????????????????
	 * @return ????????????
	 */
	@SuppressWarnings("unchecked")
	public static <T> T post(String url, Header header, HttpEntity entity, Class<T> resultType) {
		Assert.hasText(url, "[Assertion failed] - url must have text; it must not be null, empty, or blank");
		Assert.notNull(resultType, "[Assertion failed] - resultType is required; it must not be null");

		try {
			HttpPost httpPost = new HttpPost(url);
			if (header != null) {
				httpPost.setHeader(header);
			}
			if (entity != null) {
				httpPost.setEntity(entity);
			}
			CloseableHttpResponse httpResponse = HTTP_CLIENT.execute(httpPost);
			HttpEntity httpEntity = null;
			try {
				httpEntity = httpResponse.getEntity();
				if (httpEntity != null) {
					if (String.class.isAssignableFrom(resultType)) {
						return (T) EntityUtils.toString(httpEntity, "UTF-8");
					} else if (resultType.isArray() && byte.class.isAssignableFrom(resultType.getComponentType())) {
						return (T) EntityUtils.toByteArray(httpEntity);
					}
				}
			} finally {
				EntityUtils.consume(httpEntity);
				IOUtils.closeQuietly(httpResponse);
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * GET??????
	 * 
	 * @param url
	 *            URL
	 * @param parameterMap
	 *            ????????????
	 * @return ????????????
	 */
	public static String get(String url, Map<String, Object> parameterMap) {
		Assert.hasText(url, "[Assertion failed] - url must have text; it must not be null, empty, or blank");

		String result = null;
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<>();
			if (parameterMap != null) {
				for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
					String name = entry.getKey();
					String value = ConvertUtils.convert(entry.getValue());
					if (StringUtils.isNotEmpty(name)) {
						nameValuePairs.add(new BasicNameValuePair(name, value));
					}
				}
			}
			HttpGet httpGet = new HttpGet(url + (StringUtils.contains(url, "?") ? "&" : "?") + EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs, "UTF-8")));
			CloseableHttpResponse httpResponse = HTTP_CLIENT.execute(httpGet);
			try {
				HttpEntity httpEntity = httpResponse.getEntity();
				if (httpEntity != null) {
					result = EntityUtils.toString(httpEntity);
					EntityUtils.consume(httpEntity);
				}
			} finally {
				IOUtils.closeQuietly(httpResponse);
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return result;
	}

	public static String get(String url, Map<String, Object> parameterMap, Map<String, Object> headers) {
		Assert.hasText(url, "[Assertion failed] - url must have text; it must not be null, empty, or blank");

		String result = null;
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<>();
			if (parameterMap != null) {
				for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
					String name = entry.getKey();
					String value = ConvertUtils.convert(entry.getValue());
					if (StringUtils.isNotEmpty(name)) {
						nameValuePairs.add(new BasicNameValuePair(name, value));
					}
				}
			}
			HttpGet httpGet = new HttpGet(url + (StringUtils.contains(url, "?") ? "&" : "?") + EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs, "UTF-8")));
			if (headers != null) {
				for (Map.Entry<String, Object> entry : headers.entrySet()) {
					String name = entry.getKey();
					String value = ConvertUtils.convert(entry.getValue());
					if (StringUtils.isNotEmpty(name)) {
						httpGet.setHeader(name,value);
					}
				}
			}
			CloseableHttpResponse httpResponse = HTTP_CLIENT.execute(httpGet);
			try {
				HttpEntity httpEntity = httpResponse.getEntity();
				if (httpEntity != null) {
					result = EntityUtils.toString(httpEntity);
					EntityUtils.consume(httpEntity);
				}
			} finally {
				IOUtils.closeQuietly(httpResponse);
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return result;
	}
}