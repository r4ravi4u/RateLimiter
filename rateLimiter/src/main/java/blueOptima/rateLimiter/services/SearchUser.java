package blueOptima.rateLimiter.services;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import blueOptima.rateLimiter.profile.Profile;
import blueOptima.rateLimiter.utils.CommonUtils;
import blueOptima.rateLimiter.utils.Constants;
import blueOptima.rateLimiter.utils.HttpCollection;

public class SearchUser {
	private final static String CLASS = "SearchUser";

	/**
	 * Builds search query to be passed in <URL>?q=<query>
	 */
	private String getSearchQuery(Profile profile) {
		StringBuilder query = new StringBuilder(profile.getFirstName() + " ");
		if (profile.getLastName() != null && profile.getLastName().length() > 0) {
			query.append(profile.getLastName() + Constants.QUERY_FULL_NAME);
		}
		if (profile.getCity() != null && profile.getCity().length() > 0) {
			query.append(Constants.QUERY_LOCATION + profile.getCity());
		}
		return query.toString();
	}

	/**
	 * Checks if user exists or not If multiple users exists with same matching
	 * criteria, update Profiles List As per api.github.com rules, max 30 users list
	 * we can get in GET Response
	 */
	public CompletableFuture<Void> searchUser(Profile profile, List<Profile> list) throws InterruptedException {
		final String METHOD = "searchUser";

		String query = getSearchQuery(profile);

		HttpCollection reqRespClientObj = new HttpCollection();

		CommonUtils.buildRequest(Constants.URL_USER_SEARCH, reqRespClientObj, query);

		if (CommonUtils.getResponse(reqRespClientObj)) {
			JSONParser parser = new JSONParser();
			try {
				JSONObject responseObject = (JSONObject) parser
						.parse(EntityUtils.toString(reqRespClientObj.getEntity(), Charset.forName("UTF-8")));
				JSONArray item = (JSONArray) responseObject.get(Constants.ITEMS);
				int size = item.size();
				if (size == 0)
					Constants.LOGGER.logp(Level.WARNING, CLASS, METHOD, "User Not Found having first name :"
							+ profile.getFirstName() + ", last name : " + profile.getLastName());
				else {
					int count = 0;
					while (count < size) {
						JSONObject profileData = (JSONObject) item.get(count);
						profile.setProfileUrl((String) profileData.get(Constants.URL));
						profile.setLogin((String) profileData.get(Constants.LOGIN));
						profile.setId((long) profileData.get(Constants.ID));
						count++;
						if (count < size && size > 1) {
							profile = (Profile) profile.clone();
							list.add(profile);
						}
					}
				}
			} catch (ParseException e) {
				Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
			} catch (IOException e) {
				Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
			}
		} else {
			Constants.LOGGER.logp(Level.WARNING, CLASS, METHOD, "User Not Found having first name :"
					+ profile.getFirstName() + ", last name : " + profile.getLastName() + ": Reason is : " + reqRespClientObj.getErrorMsg());
			return CompletableFuture.completedFuture(null);
		}
		return CompletableFuture.completedFuture(null);
	}
}
