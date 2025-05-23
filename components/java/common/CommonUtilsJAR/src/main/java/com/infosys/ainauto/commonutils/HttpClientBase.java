/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.HttpClientBase.Authentication.BasicAuthenticationConfig;
import com.infosys.ainauto.commonutils.HttpClientBase.Authentication.BearerAuthenticationConfig;

public abstract class HttpClientBase {

	private static Logger logger = LoggerFactory.getLogger(HttpClientBase.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");
	private static final int NUM_OF_RETRIES_FOR_AUTH_DEFAULT_VALUE = 1;
	private CloseableHttpClient httpClient;
	private RequestConfig requestConfig;
	private PoolingHttpClientConnectionManager connManager;
	private HttpClientConfig httpClientConfig;
	private int numOfTries;
	private BearerAuthenticationConfig bearerAuthConfig;
	private BasicAuthenticationConfig basicAuthConfig;
	private AuthenticationMode authenticationMode;

	private static final String APPLICATION_TYPE_JSON = "application/json";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String ACCEPT = "Accept";
	private static final String AUTHORIZATION = "Authorization";
	private static final String BASIC = "Basic ";
	private static final String AUTH_FAILURE_STATUS_CODE = "Authentication failure! HTTP status code =  ";
	private static final String UN_HANDLED_STATUS_FROM_API = "Unhandled response status from API ";
	private static final String EXISTING = "Exiting";
	private static final String ERROR_HTTP_RESPONSE_BODY = "Error occurred while reading httpResponseBody";
	private static final String AUTH = "auth";

	protected HttpClientBase() {
		// Create new instance of config with default values
		this.httpClientConfig = new HttpClientConfig();
		this.authenticationMode = AuthenticationMode.NONE;
		// If authentication fails, no point in retrying with same credentials
		this.numOfTries = NUM_OF_RETRIES_FOR_AUTH_DEFAULT_VALUE;
		initialize();
	}

	protected HttpClientBase(HttpClientConfig httpClientConfig) {
		this.httpClientConfig = httpClientConfig;
		this.authenticationMode = AuthenticationMode.NONE;
		// If authentication fails, no point in retrying with same credentials
		this.numOfTries = NUM_OF_RETRIES_FOR_AUTH_DEFAULT_VALUE;
		initialize();
	}

	protected HttpClientBase(HttpClientConfig httpClientConfig, BearerAuthenticationConfig bearerAuthConfig) {
		this.httpClientConfig = httpClientConfig;
		this.bearerAuthConfig = bearerAuthConfig;
		this.authenticationMode = AuthenticationMode.BEARER_AUTH;
		this.numOfTries = bearerAuthConfig.getNumOfRetries();
		initialize();
	}

	protected HttpClientBase(HttpClientConfig httpClientConfig, BasicAuthenticationConfig basicAuthConfig) {
		this.httpClientConfig = httpClientConfig;
		this.basicAuthConfig = basicAuthConfig;
		authenticationMode = AuthenticationMode.BASIC_AUTH;
		// If authentication fails, no point in retrying with same credentials
		numOfTries = NUM_OF_RETRIES_FOR_AUTH_DEFAULT_VALUE;
		initialize();
	}

	private void initialize() {
		// If null, then create a new instance of HttpClientConfig
		if (httpClientConfig == null) {
			httpClientConfig = new HttpClientConfig();
		}

		requestConfig = httpClientConfig.buildRequestConfig();
		httpClient = httpClientConfig.buildHttpClient(connManager);
		if (authenticationMode == AuthenticationMode.BEARER_AUTH) {
			// Call on initialize to keep auth token ready. Force = false
			updateAuthToken(false);
		}
	}

	protected enum HttpCallType {
		GET(1), POST(2), PUT(3);

		private int propertyValue;

		private HttpCallType(int s) {
			propertyValue = s;
		}

		public int getValue() {
			return propertyValue;
		}
	}

	protected JsonStructure executeHttpCall(HttpCallType httpCallType, String hostUrl) {
		return executeHttpCall(httpCallType, hostUrl, "", null);
	}

	protected JsonStructure executeHttpCall(HttpCallType httpCallType, String hostUrl,
			HashMap<String, String> headerPropertiesMap) {
		return executeHttpCall(httpCallType, hostUrl, "", headerPropertiesMap);
	}

	protected JsonStructure executeHttpCall(HttpCallType httpCallType, String hostUrl, JsonArray jsonRequest) {
		String httpRequestBody = "";
		if (jsonRequest != null) {
			httpRequestBody = jsonRequest.toString();
		}
		return executeHttpCall(httpCallType, hostUrl, httpRequestBody, null);
	}

	protected JsonStructure executeHttpCall(HttpCallType httpCallType, String hostUrl, JsonObject jsonRequest,
			HashMap<String, String> headerPropertiesMap) {
		String httpRequestBody = "";
		if (jsonRequest != null) {
			httpRequestBody = jsonRequest.toString();
		}
		return executeHttpCall(httpCallType, hostUrl, httpRequestBody, headerPropertiesMap);
	}

	protected JsonStructure executeHttpCall(HttpCallType httpCallType, String hostUrl, JsonObject jsonRequest) {
		String httpRequestBody = "";
		if (jsonRequest != null) {
			httpRequestBody = jsonRequest.toString();
		}
		return executeHttpCall(httpCallType, hostUrl, httpRequestBody, null);
	}

	protected JsonStructure executeHttpCall(HttpCallType httpCallType, String hostUrl, String httpRequestBody) {
		return executeHttpCall(httpCallType, hostUrl, httpRequestBody, null);
	}

	protected JsonStructure executeHttpCall(HttpCallType httpCallType, String hostUrl, String httpRequestBody,
			HashMap<String, String> headerPropertiesMap) {
		logger.debug("Entering");
		long startTime = System.nanoTime();
		HttpGet httpGet = null;
		HttpPost httpPost = null;
		HttpPut httpPut = null;
		JsonStructure jsonResponse = null;
		HttpResponse httpResponse = null;
		int httpStatusCode = 0;
		String correlationId = generateCorrelationId();
		String messageIdentity = correlationId + "|" + hostUrl + "|Method=" + httpCallType.toString() + "|";
		try {
			logger.info("{} |RequestContent= {}", StringUtility.sanitizeReqData(messageIdentity), httpRequestBody);
			Map<String, String> customHttpHeaderMap = new HashMap<>();
			if (headerPropertiesMap != null && !headerPropertiesMap.isEmpty()) {
				customHttpHeaderMap = headerPropertiesMap;
			}
			hostUrl = StringUtility.sanitizeReqData(hostUrl);
			for (int i = 1; i <= numOfTries; i++) {
				if (httpCallType.equals(HttpCallType.GET)) {
					httpGet = new HttpGet(hostUrl);
					httpGet.setHeader(CONTENT_TYPE, APPLICATION_TYPE_JSON);
					addAuthenticationToHeader(httpGet);
					httpGet.setHeader(ACCEPT, APPLICATION_TYPE_JSON);
					httpGet.setConfig(requestConfig);
					for (Map.Entry<String, String> entry : customHttpHeaderMap.entrySet()) {
						httpGet.setHeader(entry.getKey(), entry.getValue());
					}
					httpResponse = httpClient.execute(httpGet);
				} else if (httpCallType.equals(HttpCallType.POST)) {
					httpPost = new HttpPost(hostUrl);
					httpPost.setHeader(CONTENT_TYPE, APPLICATION_TYPE_JSON);
					addAuthenticationToHeader(httpPost);
					httpPost.setHeader(ACCEPT, APPLICATION_TYPE_JSON);
					httpPost.setConfig(requestConfig);
					for (Map.Entry<String, String> entry : customHttpHeaderMap.entrySet()) {
						httpPost.setHeader(entry.getKey(), entry.getValue());
					}
					httpPost.setEntity(new StringEntity(httpRequestBody, "UTF-8"));
					httpResponse = httpClient.execute(httpPost);
				} else if (httpCallType.equals(HttpCallType.PUT)) {
					httpPut = new HttpPut(hostUrl);
					httpPut.setHeader(CONTENT_TYPE, APPLICATION_TYPE_JSON);
					addAuthenticationToHeader(httpPut);
					httpPut.setHeader(ACCEPT, APPLICATION_TYPE_JSON);
					httpPut.setConfig(requestConfig);
					for (Map.Entry<String, String> entry : customHttpHeaderMap.entrySet()) {
						httpPut.setHeader(entry.getKey(), entry.getValue());
					}
					httpPut.setEntity(new StringEntity(httpRequestBody));
					httpResponse = httpClient.execute(httpPut);
				}
				if (httpResponse != null) {
					httpStatusCode = httpResponse.getStatusLine().getStatusCode();
					// 201 - created
					if (httpStatusCode == 200 || httpStatusCode == 201) {
						break; // No need for a second try
					} else if (httpStatusCode == 401) {

						if (authenticationMode == AuthenticationMode.BEARER_AUTH) {
							// Force = true
							Date currentDtm = new Date();
							releaseHttpConnection(httpGet, httpPost, httpPut);
							synchronized (this) {
								if (AuthTokenManager.retrieveAuthToken(bearerAuthConfig).getAuthTokenUpdatedDtm()
										.compareTo(currentDtm) < 0)
									updateAuthToken(true);
							}
						} else {
							throw new Exception(AUTH_FAILURE_STATUS_CODE + httpStatusCode);
						}
					} else {
						throw new Exception(UN_HANDLED_STATUS_FROM_API + httpStatusCode);
					}
				}
			}

			logger.info("{} |ResponseStatus= {} ", StringUtility.sanitizeReqData(messageIdentity), httpStatusCode);
			// Extract JSON response only if response status is NOT 401 UNAUTHORIZED
			if (httpStatusCode == 401) {
				JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
				jsonRequestBuilder.add("httpStatusCode", 401);
				jsonRequestBuilder.add("httpStatusMessage", "Unauthorized Error");
				jsonResponse = jsonRequestBuilder.build();
			} else {
				InputStream inputStream = null;
				if (httpResponse != null) {
					try {
						inputStream = httpResponse.getEntity().getContent();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1));
						String line;
						StringBuilder httpResponseBody = new StringBuilder();
						while ((line = reader.readLine()) != null) {
							httpResponseBody.append(line);
						}
						logger.info("{} |ResponseContent= {} ", StringUtility.sanitizeReqData(messageIdentity),
								httpResponseBody);

						JsonReader jsonReader = Json.createReader(new StringReader(httpResponseBody.toString()));
						// can be JsonObject or JsonArray
						jsonResponse = jsonReader.read();
						jsonReader.close();
					} finally {
						FileUtility.safeCloseInputStream(inputStream);
					}
				}
			}

		} catch (Exception ex) {
			logger.error("Error occurred while making call: " + StringUtility.sanitizeReqData(messageIdentity), ex);
		} finally {
			releaseHttpConnection(httpGet, httpPost, httpPut);
		}
		if (jsonResponse != null) {
			logger.info(jsonResponse.toString());
		}
		PERF_LOGGER.info("OUTBOUND,{},{},{},secs", httpCallType, hostUrl,
				(System.nanoTime() - startTime) / 1000000000.0);
		logger.debug(EXISTING);
		return jsonResponse;
	}

	protected HttpResponseData executePostAttachmentWithAuthCall(String hostUrl,
			List<HttpFileRequestData> httpFileDataList) {
		return executePostAttachmentWithAuthCall(hostUrl, httpFileDataList, null, null, true);
	}

	protected HttpResponseData executePostAttachmentWithAuthCall(String hostUrl,
			List<HttpFileRequestData> httpFileDataList, BasicAuthenticationConfig basicAuthConfig) {
		return executePostAttachmentWithAuthCall(hostUrl, httpFileDataList, null, basicAuthConfig, true);
	}

	protected HttpResponseData executePostAttachmentWithAuthCall(String hostUrl,
			List<HttpFileRequestData> httpFileDataList, HashMap<String, String> headerPropertiesMap) {
		return executePostAttachmentWithAuthCall(hostUrl, httpFileDataList, headerPropertiesMap, null, true);
	}

	protected HttpResponseData executePostAttachmentWithAuthCall(String hostUrl,
			List<HttpFileRequestData> httpFileDataList, HashMap<String, String> headerPropertiesMap,
			boolean isMultiFormEntity) {
		return executePostAttachmentWithAuthCall(hostUrl, httpFileDataList, headerPropertiesMap, null,
				isMultiFormEntity);
	}

	protected HttpResponseData executePostAttachmentWithAuthCall(String hostUrl,
			List<HttpFileRequestData> httpFileDataList, HashMap<String, String> headerPropertiesMap,
			BasicAuthenticationConfig basicAuthConfig, boolean isMultiFormEntity) {
		logger.debug("Entering executePostAttachmentCall");
		long startTime = System.nanoTime();
		HttpResponse httpResponse = null;
		int httpStatusCode = 0;
		HttpPost httpPost = new HttpPost(hostUrl);
		HttpResponseData httpResponseData = new HttpResponseData(null, null);
		String correlationId = generateCorrelationId();
		String messageIdentity = correlationId + "|" + hostUrl + "|Method=POST|";
		try {
			Map<String, String> customHttpHeaderMap = new HashMap<>();
			if (headerPropertiesMap != null && !headerPropertiesMap.isEmpty()) {
				customHttpHeaderMap = headerPropertiesMap;
			}
			logger.info("{} |RequestContent=", StringUtility.sanitizeReqData(messageIdentity));
			String boundary = "-" + System.currentTimeMillis();
			if (isMultiFormEntity) {
				MultipartEntityBuilder multiPartEntityBuilder = MultipartEntityBuilder.create();
				File file = null;
				FileBody fileBody = null;
				if (httpFileDataList != null) {
					for (HttpFileRequestData httpFileData : httpFileDataList) {
						file = new File(httpFileData.getFileAbsolutePath());
						fileBody = new FileBody(file, getFileContentType(httpFileData),
								httpFileData.getFileName().replace(" ", ""));
						multiPartEntityBuilder.setBoundary(boundary);
						multiPartEntityBuilder.addPart(httpFileData.getFileContentId(), fileBody);
						multiPartEntityBuilder.setBoundary(boundary);
					}
				}
				HttpEntity multipart = multiPartEntityBuilder.build();
				httpPost.setHeader(ACCEPT, APPLICATION_TYPE_JSON);
				httpPost.setHeader(CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);
				httpPost.setEntity(multipart);
			} else {
				HttpFileRequestData httpFileData = httpFileDataList.get(0);
				File file = new File(httpFileData.getFileAbsolutePath());
				InputStreamEntity inputStreamEntity = new InputStreamEntity(new FileInputStream(file), -1,
						getFileContentType(httpFileData));
				inputStreamEntity.setChunked(true);
				httpPost.setEntity(inputStreamEntity);
			}
			for (Map.Entry<String, String> entry : customHttpHeaderMap.entrySet()) {
				httpPost.setHeader(entry.getKey(), entry.getValue());
			}
			if (basicAuthConfig != null) {
				addAuthenticationToHeader(httpPost, basicAuthConfig);
			} else {
				addAuthenticationToHeader(httpPost);
			}
			httpPost.setConfig(requestConfig);
			for (int i = 1; i <= numOfTries; i++) {
				httpResponse = httpClient.execute(httpPost);
				httpStatusCode = httpResponse.getStatusLine().getStatusCode();
				if (httpStatusCode == 200 || httpStatusCode == 201 || httpStatusCode == 202) {
					break; // No need for a second try
				} else if (httpStatusCode == 401) {
					if (authenticationMode == AuthenticationMode.BEARER_AUTH) {
						// Force = true
						Date currentDtm = new Date();
						releaseHttpConnection(null, httpPost, null);
						synchronized (this) {
							if (AuthTokenManager.retrieveAuthToken(bearerAuthConfig).getAuthTokenUpdatedDtm()
									.compareTo(currentDtm) < 0)
								updateAuthToken(true);
						}
					} else {
						throw new Exception(AUTH_FAILURE_STATUS_CODE + httpStatusCode);
					}
				} else {
					throw new Exception(UN_HANDLED_STATUS_FROM_API + httpStatusCode);
				}
			}
			logger.info("{} |ResponseStatus= {} ", StringUtility.sanitizeReqData(messageIdentity), httpStatusCode);
			if (httpStatusCode == 401) {
				JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
				jsonRequestBuilder.add("httpStatusCode", 401);
				jsonRequestBuilder.add("httpStatusMessage", "Unauthorized Error");
				httpResponseData = new HttpResponseData(null, jsonRequestBuilder.build());
			} else {
				JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
				Map<String, String> jsonHeaders = new HashMap<>();
				if (httpResponse != null) {
					if (httpResponse.getAllHeaders().length > 0) {
						for (Header header : httpResponse.getAllHeaders()) {
							jsonHeaders.put(header.getName(), header.getValue());
						}
					}
					InputStream inputStream = null;
					try {
						inputStream = httpResponse.getEntity().getContent();
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
						String line;
						StringBuilder httpResponseBody = new StringBuilder();
						while ((line = reader.readLine()) != null) {
							httpResponseBody.append(line);
						}
						logger.info("{} |Response Body: {}", StringUtility.sanitizeReqData(messageIdentity),
								httpResponseBody);
						if (httpResponseBody.length() > 0) {
							try (JsonReader jsonReader = Json
									.createReader(new StringReader(httpResponseBody.toString()))) {
								jsonReader.readObject().entrySet()
										.forEach(s -> jsonRequestBuilder.add(s.getKey(), s.getValue()));
							} catch (Exception ex) {
								logger.error(ERROR_HTTP_RESPONSE_BODY, ex.getMessage());
							}
						}
						httpResponseData = new HttpResponseData(jsonHeaders, jsonRequestBuilder.build());
					} finally {
						FileUtility.safeCloseInputStream(inputStream);
					}
				}
			}

		} catch (Exception ex) {
			logger.error("Error occurred while invoking POST on ep: " + StringUtility.sanitizeReqData(hostUrl), ex);
		} finally {
			releaseHttpConnection(null, httpPost, null);
		}
		PERF_LOGGER.info("OUTBOUND,{},{},{},secs", "POST", hostUrl, (System.nanoTime() - startTime) / 1000000000.0);
		logger.debug(EXISTING);
		return httpResponseData;
	}

	protected HttpFileResponseData executeGetAttachmentCall(String hostUrl, String saveToFolder) {
		logger.debug("Entering executeGetAttachmentCall");
		long startTime = System.nanoTime();
		HttpGet httpGet = null;
		HttpResponse httpResponse = null;
		int httpStatusCode = 0;
		HttpFileResponseData httpFileResData = null;
		String correlationId = generateCorrelationId();
		String messageIdentity = correlationId + "|" + hostUrl + "|Method=GET" + "|";
		try {
			logger.info("{} |RequestContent=", messageIdentity);
			for (int i = 1; i <= numOfTries; i++) {
				httpGet = new HttpGet(hostUrl);
				httpGet.setHeader(CONTENT_TYPE, APPLICATION_TYPE_JSON);
				httpGet.setHeader(ACCEPT, "application/octet-stream");
				addAuthenticationToHeader(httpGet);
				httpGet.setConfig(requestConfig);
				httpResponse = httpClient.execute(httpGet);

				httpStatusCode = httpResponse.getStatusLine().getStatusCode();
				if (httpStatusCode == 200) {
					break; // No need for a second try
				} else if (httpStatusCode == 401) {
					if (authenticationMode == AuthenticationMode.BEARER_AUTH) {
						// Force = true
						Date currentDtm = new Date();
						releaseHttpConnection(httpGet, null, null);
						synchronized (this) {
							if (AuthTokenManager.retrieveAuthToken(bearerAuthConfig).getAuthTokenUpdatedDtm()
									.compareTo(currentDtm) < 0)
								updateAuthToken(true);
						}
					} else {
						throw new Exception(AUTH_FAILURE_STATUS_CODE + httpStatusCode);
					}
				} else {
					throw new Exception(UN_HANDLED_STATUS_FROM_API + httpStatusCode);
				}
			}

			logger.info("{} |ResponseStatus= {}", messageIdentity, httpStatusCode);
			if (httpResponse != null) {
				Header[] responseHeaders = httpResponse.getHeaders("content-disposition");
				String fileName = "";
				if (responseHeaders.length > 0) {
					String[] tokens = responseHeaders[0].getValue().split(";");
					for (String token : tokens) {
						String[] keyValuePair = token.split("=");
						if (keyValuePair[0].trim().equals("filename")) {
							fileName = keyValuePair[1].replace("\"", "");
							break;
						}
					}
				}

				responseHeaders = httpResponse.getHeaders("content-length");
				if (responseHeaders.length > 0) {
					int contentLength = Integer.parseInt(responseHeaders[0].getValue());

					InputStream in = null;
					try {
						in = httpResponse.getEntity().getContent();
						String filePhysicalName = generateUniqueFileName(fileName);
						String filePhysicalPath = getConcatenatedName(saveToFolder, filePhysicalName);
						try (FileOutputStream fos = new FileOutputStream(
								new File(FileUtility.cleanPath(filePhysicalPath)))) {

							byte[] buffer = new byte[contentLength];
							int length;
							while ((length = in.read(buffer)) > 0) {
								fos.write(buffer, 0, length);
							}
							httpFileResData = new HttpFileResponseData(fileName, filePhysicalName, filePhysicalPath);
							logger.info("{} |ResponseContent=Saved as new file--> {}", messageIdentity,
									filePhysicalPath);
						} catch (IOException ex) {
							logger.error("Error occurred while saving new file ", ex.getMessage());
						}
					} finally {
						FileUtility.safeCloseInputStream(in);
					}
				}
			}


		} catch (Exception ex) {
			logger.error("Error occurred while making call: " + messageIdentity, ex);
		} finally {
			releaseHttpConnection(httpGet, null, null);
		}
		PERF_LOGGER.info("OUTBOUND,{},{},{},secs", "GET", hostUrl, (System.nanoTime() - startTime) / 1000000000.0);
		logger.debug(EXISTING);
		return httpFileResData;
	}

	protected String generateCorrelationId() {
		return String.valueOf(StringUtility.getRangeOfRandomNumberInInt(100000000, 999999999));
	}

	private String generateUniqueFileName(String fileName) {
		// Split string into name and extension
		String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
		// Fix for defect #1 logged on issue #131
		String fileExtension = "";
		if (tokens.length == 2) {
			fileExtension = "." + tokens[1];
		}
		return UUID.randomUUID().toString() + fileExtension;
	}

	private String getConcatenatedName(String part1, String part2) {
		Path path1 = Paths.get(FileUtility.cleanPath(part1), FileUtility.cleanPath(part2));
		Path path2 = path1.normalize();
		return path2.toAbsolutePath().toString();
	}

	private ContentType getFileContentType(HttpFileRequestData httpFileData) {
		ContentType contentType = ContentType.DEFAULT_BINARY;
		if (httpFileData != null && httpFileData.getFileContentType() != null
				&& httpFileData.getFileContentType().length() > 0) {
			contentType = ContentType.create(httpFileData.getFileContentType());
		}
		return contentType;
	}

	private void addAuthenticationToHeader(HttpRequestBase httpRequestMethod) {
		if (basicAuthConfig != null && basicAuthConfig.isAuthenticate()) {
			httpRequestMethod.setHeader(AUTHORIZATION, AUTH + basicAuthConfig.getBase64UserAndPwd());
		} else if (bearerAuthConfig != null && bearerAuthConfig.isAuthenticate()) {
			httpRequestMethod.setHeader(AUTHORIZATION, "Bearer " + StringUtility
					.sanitizeReqData(AuthTokenManager.retrieveAuthToken(bearerAuthConfig).getAuthToken()));
		}
	}

	private void addAuthenticationToHeader(HttpRequestBase httpRequestMethod,
			BasicAuthenticationConfig basicAuthConfig) {
		if (basicAuthConfig != null && basicAuthConfig.isAuthenticate()) {
			httpRequestMethod.setHeader(AUTHORIZATION, AUTH + basicAuthConfig.getBase64UserAndPwd());
		}
	}

	private synchronized void updateAuthToken(boolean isForceGeneration) {
		AuthTokenManager.AuthTokenData authTokenData = AuthTokenManager.retrieveAuthToken(bearerAuthConfig);
		if (bearerAuthConfig.isAuthenticate() && (authTokenData.getAuthToken().length() == 0 || isForceGeneration)) {
			HttpPost httpPost = new HttpPost(bearerAuthConfig.getAuthUrl());
			try {
				httpPost.setHeader(CONTENT_TYPE, APPLICATION_TYPE_JSON);
				httpPost.setHeader(ACCEPT, APPLICATION_TYPE_JSON);
				// Credentials need to be send as Basic Auth(orization)
				JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
				jsonRequestBuilder.add("authorization", BASIC + bearerAuthConfig.getBase64UserAndPwd());

				if (bearerAuthConfig.getHeadersMap() != null && !bearerAuthConfig.getHeadersMap().isEmpty()) {
					for (Map.Entry<String, String> entry : bearerAuthConfig.getHeadersMap().entrySet()) {
						jsonRequestBuilder.add(entry.getKey(), entry.getValue());
					}
				}

				JsonObject jsonRequest = jsonRequestBuilder.build();
				httpPost.setEntity(new StringEntity(jsonRequest.toString()));
				httpPost.setConfig(requestConfig);

				HttpResponse httpResponse = httpClient.execute(httpPost);

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					InputStream inputStream = null;
					try {
						inputStream = httpResponse.getEntity().getContent();
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
						String line;
						StringBuilder httpResponseBody = new StringBuilder();
						while ((line = reader.readLine()) != null) {
							httpResponseBody.append(line);
						}

						try (JsonReader jsonReader = Json.createReader(new StringReader(httpResponseBody.toString()))) {
							JsonObject jsonResponseObj = jsonReader.readObject();
							String newAuthToken = null;
							if (!jsonResponseObj.isNull("response")) {
								JsonObject jsonResponse = jsonResponseObj.getJsonObject("response");
								if (!jsonResponse.isNull("token")) {
									newAuthToken = jsonResponse.getString("token");
									AuthTokenManager.storeAuthToken(bearerAuthConfig, newAuthToken);
								}
							}
							if (newAuthToken == null || newAuthToken.length() == 0) {
								throw new Exception("Error while procuring auth token");
							}
						} catch (Exception ex) {
							logger.error(ERROR_HTTP_RESPONSE_BODY, ex.getMessage());
						}
					} finally {
						FileUtility.safeCloseInputStream(inputStream);
					}

				} else {
					throw new Exception("Error while making REST API call to /auth.");
				}
			} catch (Exception ex) {
				logger.error("Error in updateAuthToken method", ex);
				// Do Nothing so that flow continues in parent method
			} finally {
				releaseHttpConnection(null, httpPost, null);
			}
		}
	}

	/**
	 * Method to release http connections
	 * 
	 * @param httpGet
	 * @param httpPost
	 * @param httpPut
	 */
	private void releaseHttpConnection(HttpGet httpGet, HttpPost httpPost, HttpPut httpPut) {
		if (httpGet != null)
			httpGet.releaseConnection();
		else if (httpPost != null)
			httpPost.releaseConnection();
		else if (httpPut != null)
			httpPut.releaseConnection();
	}

	/**
	 * Internal helper class for managing auth tokens
	 *
	 */
	private static class AuthTokenManager {
		// authConfigToTokenMap is a static variable shared by all instances of base
		// class. It is used to store auth token in a hash map using key as auth url
		// and credentials combination. So, if multiple service classes use the same
		// auth url and credentials, then the token will be fetched only once and
		// shared with other classes to optimize processing
		private static final HashMap<String, AuthTokenData> authConfigToTokenMap = new HashMap<>();

		public static AuthTokenData retrieveAuthToken(BearerAuthenticationConfig bearerAuthConfig) {
			String key = getKey(bearerAuthConfig);
			return authConfigToTokenMap.getOrDefault(key, new AuthTokenData());
		}

		public static void storeAuthToken(BearerAuthenticationConfig bearerAuthConfig, String authToken) {
			String key = getKey(bearerAuthConfig);
			AuthTokenData authTokenData = new AuthTokenData();
			authTokenData.setAuthToken(authToken);
			authConfigToTokenMap.put(key, authTokenData);
		}

		private static String getKey(BearerAuthenticationConfig bearerAuthConfig) {
			// Create key using the combination
			return bearerAuthConfig.getAuthUrl() + "=" + bearerAuthConfig.getBase64UserAndPwd();
		}

		private static class AuthTokenData {
			private String authToken = "";
			private Date authTokenUpdatedDtm;

			protected String getAuthToken() {
				return authToken;
			}

			protected void setAuthToken(String authToken) {
				this.authToken = authToken;
				this.authTokenUpdatedDtm = new Date();
			}

			protected Date getAuthTokenUpdatedDtm() {
				return authTokenUpdatedDtm;
			}
		}
	}

	/**
	 * Enumeration constants for different authentication modes
	 */
	private enum AuthenticationMode {
		NONE(0), BASIC_AUTH(1), BEARER_AUTH(2);

		private AuthenticationMode(int s) {
		}
	}

	protected class HttpFileRequestData {
		private String fileName;
		private String fileAbsolutePath;
		private String fileContentId;
		private String fileContentType;

		public HttpFileRequestData(String fileName, String fileAbsolutePath, String fileContentId,
				String fileContentType) {
			super();
			this.fileName = fileName;
			this.fileAbsolutePath = fileAbsolutePath;
			this.fileContentId = fileContentId;
			this.fileContentType = fileContentType;
		}

		public String getFileName() {
			return fileName;
		}

		public String getFileAbsolutePath() {
			return fileAbsolutePath;
		}

		public String getFileContentId() {
			return fileContentId;
		}

		public String getFileContentType() {
			return fileContentType;
		}
	}

	protected class HttpFileResponseData {
		private String fileName;
		private String filePhysicalName;
		private String filePhysicalPath;

		public HttpFileResponseData(String fileName, String filePhysicalName, String filePhysicalPath) {
			super();
			this.fileName = fileName;
			this.filePhysicalName = filePhysicalName;
			this.filePhysicalPath = filePhysicalPath;
		}

		public String getFileName() {
			return fileName;
		}

		public String getFilePhysicalName() {
			return filePhysicalName;
		}

		public String getFilePhysicalPath() {
			return filePhysicalPath;
		}
	}

	protected class HttpResponseData {
		private Map<String, String> headers;
		private JsonObject response;

		public HttpResponseData(Map<String, String> headers, JsonObject response) {
			super();
			this.headers = headers;
			this.response = response;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}

		public JsonObject getResponse() {
			return response;
		}
	}

	protected static class HttpClientConfig {
		private static final int CONN_REQ_TIMEOUT_SECS_DEFAULT_VALUE = 1800; // infinite
		private static final int CONN_TIMEOUT_SECS_DEFAULT_VALUE = 600; // infinite
		private static final int SOCKET_TIMEOUT_SECS_DEFAULT_VALUE = 60;
		private static final int DEFAULT_MAX_PER_ROUTE = 5;
		private String proxyIp;
		private String proxyPort;
		private String proxyUsername;
		private String proxyPassword;
		private String certificatePath;
		private int connectionRequestTimeoutInSecs;
		private int connectTimeoutInSecs;
		private int socketTimeoutInSecs;
		private int defaultMaxPerRoute;
		private boolean disableSSLValidation = false;

		public HttpClientConfig() {
			// Default values
			// the time to wait for a connection from the connection manager/pool
			this.connectionRequestTimeoutInSecs = CONN_REQ_TIMEOUT_SECS_DEFAULT_VALUE;
			// the time to establish the connection with the remote host
			this.connectTimeoutInSecs = CONN_TIMEOUT_SECS_DEFAULT_VALUE;
			// the time waiting for data – after establishing the connection
			// maximum time of inactivity between two data packets
			this.socketTimeoutInSecs = SOCKET_TIMEOUT_SECS_DEFAULT_VALUE;
			// the max requests which can be made to one route
			this.defaultMaxPerRoute = DEFAULT_MAX_PER_ROUTE;
		}

		public HttpClientConfig setProxyIp(final String proxyIp) {
			this.proxyIp = proxyIp;
			return this;
		}

		public HttpClientConfig setProxyPort(final String proxyPort) {
			this.proxyPort = proxyPort;
			return this;
		}

		public HttpClientConfig setProxyUsername(final String proxyUsername) {
			this.proxyUsername = proxyUsername;
			return this;
		}

		public HttpClientConfig setProxyPassword(final String proxyPassword) {
			this.proxyPassword = proxyPassword;
			return this;
		}

		public HttpClientConfig setCertificatePath(final String certificatePath) {
			this.certificatePath = certificatePath;
			return this;
		}
		
		/**
	     * IMPORTANT: NOT RECOMMENDED FOR PROD OR ANY EXTERNAL API.
	     * 
	     * NOTE: Used only for bypassing the internal API SSL validation.
	     * @return HttpClientConfig class reference.
	     */
		public HttpClientConfig setDisableSSLValidation(final boolean disableSSLValidation) {
			this.disableSSLValidation = disableSSLValidation;
			return this;
		}

		public HttpClientConfig setConnectionRequestTimeoutInSecs(final int connectionRequestTimeoutInSecs) {
			this.connectionRequestTimeoutInSecs = connectionRequestTimeoutInSecs;
			return this;
		}

		public HttpClientConfig setConnectTimeoutInSecs(final int connectTimeoutInSecs) {
			this.connectTimeoutInSecs = connectTimeoutInSecs;
			return this;
		}

		public HttpClientConfig setSocketTimeoutInSecs(final int socketTimeoutInSecs) {
			this.socketTimeoutInSecs = socketTimeoutInSecs;
			return this;
		}

		public HttpClientConfig setDefaultMaxPerRoute(final int defaultMaxPerRoute) {
			this.defaultMaxPerRoute = defaultMaxPerRoute;
			return this;
		}

		public RequestConfig buildRequestConfig() {
			RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
			if (StringUtility.hasTrimmedValue(proxyIp) && StringUtility.hasTrimmedValue(proxyPort)) {
				requestConfigBuilder.setProxy(new HttpHost(proxyIp, Integer.valueOf(proxyPort), "https"));
			}
			requestConfigBuilder.setSocketTimeout(socketTimeoutInSecs * 1_000);
			requestConfigBuilder.setConnectTimeout(connectTimeoutInSecs * 1_000);
			requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeoutInSecs * 1_000);
			return requestConfigBuilder.build();
		}

		public CloseableHttpClient buildHttpClient(PoolingHttpClientConnectionManager connManager) {
			HttpClientBuilder httpClientBuilder = HttpClients.custom();
			if (StringUtility.hasTrimmedValue(certificatePath)) {
				httpClientBuilder.setSSLContext(getSSLContext(certificatePath))
						.setSSLHostnameVerifier(SSLConnectionSocketFactory.getDefaultHostnameVerifier());
			}
			if (StringUtility.hasTrimmedValue(proxyUsername)) {
				CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(new AuthScope(proxyIp, Integer.valueOf(proxyPort)),
						new UsernamePasswordCredentials(proxyUsername, proxyPassword));
				httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			}
			if (disableSSLValidation) {
//				IMPORTANT: NOT RECOMMENDED FOR EXTERNAL API CALL.
//				---- Ref links ----
//				https://howtodoinjava.com/java/java-security/bypass-ssl-certificate-checking-java/
//				https://stackoverflow.com/questions/19517538/ignoring-ssl-certificate-in-apache-httpclient-4-3/25187223#25187223
				try {
					SSLConnectionSocketFactory sslsf = getAllTrustedSSLSockerFactory();
					Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).build();
					connManager = new PoolingHttpClientConnectionManager(registry);
					
				} catch (Exception e) {
					logger.error("Error occurred while SSL validation Bypass ", e);
				}
			} else {
				connManager = new PoolingHttpClientConnectionManager();
			}
			connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
			return httpClientBuilder.setConnectionManager(connManager).build();
		}

		private SSLContext getSSLContext(String certificatePath) {
			SSLContext sslContext = null;
			InputStream is = null;
			try {
				if (!certificatePath.startsWith("/")) {
					certificatePath = "/" + certificatePath;
				}
				is = HttpClientBase.class.getResourceAsStream(certificatePath);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);

				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				keyStore.load(null); // You don't need the KeyStore instance to come from a file.
				keyStore.setCertificateEntry("caCert", caCert);

				sslContext = SSLContexts.custom().loadKeyMaterial(keyStore, null).build();

				// Enable below lines when SSL setup needed for global.
				// TrustManagerFactory tmf =
				// TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				// tmf.init(ks);
				// sslContext = SSLContext.getInstance("TLS");
				// sslContext.init(null, tmf.getTrustManagers(), null);
			} catch (Exception e) {
				logger.error("Error occurred while reading certificate file", e);
			} finally {
				FileUtility.safeCloseInputStream(is);
			}
			return sslContext;
		}

		private SSLConnectionSocketFactory getAllTrustedSSLSockerFactory() throws Exception {
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					return true;
				}
			}).build();
			return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		}

	}

	public static class Authentication {
		private Authentication() {
		}

		public static class BasicAuthenticationConfig {
			private final String base64UserAndPwd;
			private final boolean isAuthenticate;

			public BasicAuthenticationConfig(String username, String authPass, boolean isAuthenticate) {
				super();
				base64UserAndPwd = generateBase64EncodedString(username, authPass);
				this.isAuthenticate = isAuthenticate;
			}

			public String getBase64UserAndPwd() {
				return base64UserAndPwd;
			}

			public boolean isAuthenticate() {
				return isAuthenticate;
			}
		}

		public static class BearerAuthenticationConfig {
			private final String authUrl;
			private final String base64UserAndPwd;
			private final Map<String, String> headersMap;
			private final boolean isAuthenticate;
			private final int numOfRetries;

			public BearerAuthenticationConfig(String authUrl, String username, String authPass,
					Map<String, String> headersMap, int numOfRetries, boolean isAuthenticate) {
				super();
				this.authUrl = authUrl;
				this.base64UserAndPwd = generateBase64EncodedString(username, authPass);
				this.headersMap = headersMap;
				this.numOfRetries = numOfRetries;
				this.isAuthenticate = isAuthenticate;
			}

			public String getAuthUrl() {
				return authUrl;
			}

			public String getBase64UserAndPwd() {
				return base64UserAndPwd;
			}

			public Map<String, String> getHeadersMap() {
				return headersMap;
			}

			public int getNumOfRetries() {
				return numOfRetries;
			}

			public boolean isAuthenticate() {
				return isAuthenticate;
			}
		}

		private static String generateBase64EncodedString(String username, String authPass) {
			String authString = username + ":" + authPass;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			return new String(authEncBytes);
		}
	}

	// For post call without authentication where request is file and response
	// either file or json(for validation errore).
	protected HttpFileResponseData executePostAttachmentCall(String hostUrl, List<HttpFileRequestData> httpFileDataList,
			String saveToFolder) {
		logger.debug("Entering executePostAttachmentWithoutAuthCall");
		long startTime = System.nanoTime();
		HttpResponse httpResponse = null;
		int httpStatusCode = 0;
		HttpPost httpPost = new HttpPost(hostUrl);
		HttpFileResponseData httpFileResData = null;
		String correlationId = generateCorrelationId();
		String messageIdentity = correlationId + "|" + hostUrl + "|Method=POST|";
		try {
			logger.info("{} |RequestContent=", StringUtility.sanitizeReqData(messageIdentity));
			String boundary = "-" + System.currentTimeMillis();
			MultipartEntityBuilder multiPartEntityBuilder = MultipartEntityBuilder.create();
			File file = null;
			FileBody fileBody = null;
			if (httpFileDataList != null) {
				for (HttpFileRequestData httpFileData : httpFileDataList) {
					file = new File(httpFileData.getFileAbsolutePath());
					fileBody = new FileBody(file, getFileContentType(httpFileData),
							httpFileData.getFileName().replace(" ", ""));
					multiPartEntityBuilder.setBoundary(boundary);
					multiPartEntityBuilder.addPart(httpFileData.getFileContentId(), fileBody);
					multiPartEntityBuilder.setBoundary(boundary);
				}
			}
			HttpEntity multipart = multiPartEntityBuilder.build();
			httpPost.setHeader(ACCEPT, "application/pdf");
			httpPost.setHeader(CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(multipart);
			httpResponse = httpClient.execute(httpPost);
			httpStatusCode = httpResponse.getStatusLine().getStatusCode();
			logger.info("{}|ResponseStatus={}", StringUtility.sanitizeReqData(messageIdentity), httpStatusCode);
			if (httpStatusCode == 200 || httpStatusCode == 201) {
				if (httpResponse.getHeaders("content-type")[0].getValue().equalsIgnoreCase(APPLICATION_TYPE_JSON)) {
					InputStream inputStream = null;
					try {
						inputStream = httpResponse.getEntity().getContent();
						BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
						String line;
						StringBuilder httpResponseBody = new StringBuilder();
						while ((line = reader.readLine()) != null) {
							httpResponseBody.append(line);
						}
						logger.info("{} |Response Body: {}", StringUtility.sanitizeReqData(messageIdentity),
								httpResponseBody);
						try (JsonReader jsonReader = Json.createReader(new StringReader(httpResponseBody.toString()))) {
							JsonObject jsonResponse = jsonReader.readObject();
							throw new Exception("Error thrown from API " + jsonResponse.getString("responseMsg"));
						} catch (Exception ex) {
							logger.error(ERROR_HTTP_RESPONSE_BODY, ex.getMessage());
						}
					} finally {
						FileUtility.safeCloseInputStream(inputStream);
					}
				} else {
					Header[] responseHeaders = httpResponse.getHeaders("content-disposition");
					String fileName = "";
					if (responseHeaders.length > 0) {
						String[] tokens = responseHeaders[0].getValue().split(";");
						for (String token : tokens) {
							String[] keyValuePair = token.split("=");
							if (keyValuePair[0].trim().equals("filename")) {
								fileName = keyValuePair[1].replace("\"", "");
								break;
							}
						}
					}
					responseHeaders = httpResponse.getHeaders("content-length");
					if (responseHeaders.length > 0) {
						int contentLength = Integer.parseInt(responseHeaders[0].getValue());
						InputStream in = null;
						try {
							in = httpResponse.getEntity().getContent();
							String filePhysicalName = generateUniqueFileName(fileName);
							String filePhysicalPath = getConcatenatedName(saveToFolder, filePhysicalName);
							try (FileOutputStream fos = new FileOutputStream(
									new File(FileUtility.cleanPath(filePhysicalPath)))) {
								byte[] buffer = new byte[contentLength];
								int length;
								while ((length = in.read(buffer)) > 0) {
									fos.write(buffer, 0, length);
								}
								httpFileResData = new HttpFileResponseData(fileName, filePhysicalName,
										filePhysicalPath);
								logger.info("{} |ResponseContent=Saved as new file--> {}",
										StringUtility.sanitizeReqData(messageIdentity), filePhysicalPath);
							} catch (IOException ex) {
								logger.error("Error occurred while saving new file ", ex.getMessage());
							}
						} finally {
							FileUtility.safeCloseInputStream(in);
						}
					}
				}
			} else {
				throw new Exception(UN_HANDLED_STATUS_FROM_API + httpStatusCode);
			}
		} catch (Exception ex) {
			logger.error("Error occurred while invoking POST on ep: " + StringUtility.sanitizeReqData(hostUrl), ex);
		} finally {
			releaseHttpConnection(null, httpPost, null);
		}
		PERF_LOGGER.info("OUTBOUND,{},{},{},secs", "POST", hostUrl, (System.nanoTime() - startTime) / 1000000000.0);
		logger.debug(EXISTING);
		return httpFileResData;
	}

}
