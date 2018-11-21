package org.walkframework.restful.converter;

import java.io.Serializable;

import org.springframework.http.HttpHeaders;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"requestURL", "requestBody", "responseBody", "requestHeaders", "sourceIP"})
public class RestfulLog implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@JsonProperty("REQUEST_URL")
	private String requestURL;
	
	@JsonProperty("REQUEST_BODY")
	private Object requestBody;
	
	@JsonProperty("REQUEST_HEADERS")
	private HttpHeaders requestHeaders;
	
	@JsonProperty("SOURCE_IP")
	private String sourceIP;
	
	@JsonProperty("RESPONSE_BODY")
	private Object responseBody;

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public Object getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(Object requestBody) {
		this.requestBody = requestBody;
	}

	public HttpHeaders getRequestHeaders() {
		return requestHeaders;
	}

	public void setRequestHeaders(HttpHeaders requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	public String getSourceIP() {
		return sourceIP;
	}

	public void setSourceIP(String sourceIP) {
		this.sourceIP = sourceIP;
	}
	
	public Object getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(Object responseBody) {
		this.responseBody = responseBody;
	}
	
}
