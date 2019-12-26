package blueOptima.rateLimiter.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;

/*
 * Common Methods separated out for ease of extension
 * These methods are called from different Files and only input parameter value changes
 */
public class CommonUtils {
	private final static String CLASS = "CommonUtils";

	// to be set from RateLimiterHandler parameterized Constructor
	private static boolean auth, client;

	private static String clientID, clientSecret, accessToken;

	public static void setAuthLevel(boolean auth, boolean client) {
		CommonUtils.auth = auth;
		CommonUtils.client = client;
	}

	public static void buildRequest(String url, HttpCollection reqRespClientObj) {
		buildRequest(url, clientID, clientSecret, accessToken, reqRespClientObj, null);
	}

	public static void buildRequest(String url, HttpCollection reqRespClientObj, String query) {
		buildRequest(url, clientID, clientSecret, accessToken, reqRespClientObj, query);
	}

	public static void buildRequest(String url, String clientID, String clientSecret, String accessToken,
			HttpCollection reqRespClientObj, String query) {
		final String METHOD = "buildRequest";
		URI uri = null;
		try {
			if (!auth)
				uri = new URIBuilder(url).build();
			else {
				// Client Auth
				if (client)
					uri = new URIBuilder(url).setParameter("client_id", (CommonUtils.clientID = clientID))
							.setParameter("client_secret", (CommonUtils.clientSecret = clientSecret)).build();
				// Token Auth
				else
					uri = new URIBuilder(url).setParameter("access_token", (CommonUtils.accessToken = accessToken))
							.build();
			}

			if (query != null)
				uri = new URIBuilder(uri).addParameter("q", query).build();

			reqRespClientObj.setRequest(new HttpGet(uri));
			reqRespClientObj.getRequest().setHeader("User-Agent", Constants.USER_AGENT);

			reqRespClientObj.setHttpClient(HttpClients.createDefault());

		} catch (URISyntaxException e) {
			Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
		}
	}
	
	public static boolean getResponse(HttpCollection reqRespClientObj) {
		final String METHOD = "getResponse";
		if (reqRespClientObj.getHttpClient() == null || reqRespClientObj.getRequest() == null)	{
			reqRespClientObj.setErrorMsg("Either HTTPClient or Request Object is Invalid");
			return false;
		}
			
		try {
			CloseableHttpResponse response = reqRespClientObj.getHttpClient().execute(reqRespClientObj.getRequest());
			reqRespClientObj.setEntity(response.getEntity());

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				reqRespClientObj.setErrorMsg(response.getStatusLine().toString());
				return false;
			}
			
			return true;
		} catch (ClientProtocolException e) {
			Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
			reqRespClientObj.setErrorMsg(e.toString());
		} catch (IOException e) {
			Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
			reqRespClientObj.setErrorMsg(e.toString());
		}
		
		return false;
	}

}
