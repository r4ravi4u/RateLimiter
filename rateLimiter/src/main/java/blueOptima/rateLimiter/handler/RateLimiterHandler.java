package blueOptima.rateLimiter.handler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import blueOptima.rateLimiter.utils.CommonUtils;
import blueOptima.rateLimiter.utils.Constants;
import blueOptima.rateLimiter.utils.HttpCollection;

public class RateLimiterHandler {
	private final static String CLASS = "RateLimiterHandler";

	public static long coreRateLimit = 0;
	public static long searchRateLimit = 0;

	private HttpCollection reqRespClientObj = new HttpCollection();

	// to be used for future calls which uses same config as initial Auth Level
	public RateLimiterHandler() {
	}

	/*
	 * Checks if Auth is enabled or not If enabled, then checks if its via Client
	 * way or Token way Sets auth specs for future calls as well
	 */
	public RateLimiterHandler(String authPath) {
		final String METHOD = "Constructor RateLimiterHandler";

		if (authPath == null || authPath.length() < 1) {
			CommonUtils.setAuthLevel(false, false);
			CommonUtils.buildRequest(Constants.URL_RATE_LIMIT, reqRespClientObj);
		} else {
			String content = null;
			try {
				content = new String(Files.readAllBytes(Paths.get(authPath)));
			} catch (IOException e) {
				Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
			}

			if (content != null) {
				String[] lines = content.split("\n");

				// Token Auth
				if (lines.length == 1) {
					CommonUtils.setAuthLevel(true, false);
					CommonUtils.buildRequest(Constants.URL_RATE_LIMIT, null, null, lines[0].trim(), reqRespClientObj,
							null);
				}

				// Client Auth
				else if (lines.length == 2) {
					CommonUtils.setAuthLevel(true, true);
					CommonUtils.buildRequest(Constants.URL_RATE_LIMIT, lines[0].trim(), lines[1].trim(), null,
							reqRespClientObj, null);
				}

				// Wrong Input
				else {
					CommonUtils.setAuthLevel(false, false);
					CommonUtils.buildRequest(Constants.URL_RATE_LIMIT, reqRespClientObj);
				}
			} else {
				CommonUtils.setAuthLevel(false, false);
				CommonUtils.buildRequest(Constants.URL_RATE_LIMIT, reqRespClientObj);
			}
		}
	}

	/*
	 * Refreshes Rate (Core & Search) for the first time and also in case when
	 * cached limit reaches to 0 Cached the values and decrementing at each
	 * subsequent calls
	 */
	private boolean refreshRate(boolean coreRate) {
		final String METHOD = "refreshRate";

		if (CommonUtils.getResponse(reqRespClientObj)) {
			try {
				JSONParser parser = new JSONParser();
				JSONObject responseObject = (JSONObject) parser
						.parse(EntityUtils.toString(reqRespClientObj.getEntity(), Charset.forName("UTF-8")));
				JSONObject resources = (JSONObject) responseObject.get(Constants.RESOURCES);
				JSONObject core = (JSONObject) resources.get(Constants.CORE);
				JSONObject search = (JSONObject) resources.get(Constants.SEARCH);
				// Normal Rate Limit Column has been deprecated, hence used Core Rate Limit
				coreRateLimit = (long) core.get(Constants.LIMIT); // taken as remaining part
				searchRateLimit = (long) search.get(Constants.LIMIT);
				Constants.LOGGER.logp(Level.INFO, CLASS, METHOD, "CoreRateLimit : " + coreRateLimit + ", SearchRateLimit : " + searchRateLimit);
				if (coreRate) {
					if (coreRateLimit <= 0)
						return true;
				} else {
					if (searchRateLimit <= 0)
						return true;
				}
				return false;
			} catch (ParseException e) {
				Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
			} catch (IOException e) {
				Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
			}
		}
		else
			Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, reqRespClientObj.getErrorMsg());
		
		return true;
	}

	/*
	 * Method to check whether Search Rate Limit exceeded or not
	 */
	public boolean exceededSearchRateLimit() {
		if (searchRateLimit <= 0)
			return refreshRate(false);
		else
			return false;
	}

	/*
	 * Method to check whether Core Rate Limit exceeded or not
	 */
	public boolean exceededCoreRateLimit() {
		if (coreRateLimit <= 0)
			return refreshRate(true);
		else
			return false;
	}
}
