package blueOptima.rateLimiter.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

/*
 * POJO to have a single place to store HTTP req, response and entity objects
 * Also used so that it can be passed in method and hence member variables (objects) gets
 * updated from the called method itself
*/
public class HttpCollection {

	private CloseableHttpClient httpClient;
	private HttpGet request;
	private HttpEntity entity;
	private String errorMsg;

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public HttpGet getRequest() {
		return request;
	}

	public void setRequest(HttpGet request) {
		this.request = request;
	}

	public HttpEntity getEntity() {
		return entity;
	}

	public void setEntity(HttpEntity entity) {
		this.entity = entity;
	}	

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	@Override
	public String toString() {
		return "HttpCollection [httpClient=" + httpClient + ", request=" + request + ", entity=" + entity + "]";
	}
}
