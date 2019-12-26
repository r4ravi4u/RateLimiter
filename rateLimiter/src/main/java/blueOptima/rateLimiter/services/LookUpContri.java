package blueOptima.rateLimiter.services;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import blueOptima.rateLimiter.repo.Repository;
import blueOptima.rateLimiter.utils.CommonUtils;
import blueOptima.rateLimiter.utils.Constants;
import blueOptima.rateLimiter.utils.HttpCollection;

public class LookUpContri {
	private static final String CLASS = "LookUpContri";

	/**
	 * Add contributions in repository for the user
	 */
	public CompletableFuture<Void> addContri(Repository repository, String login) throws InterruptedException {
		final String METHOD = "addContri";

		String contriUrl = Constants.URL_REPO_CONTRIBUTIONS + login + "/" + repository.getName() + "/"
				+ Constants.CONTRIBUTORS;

		HttpCollection reqRespClientObj = new HttpCollection();

		CommonUtils.buildRequest(contriUrl, reqRespClientObj);

		if (CommonUtils.getResponse(reqRespClientObj)) {
			long contributions = 0;
			JSONParser parser = new JSONParser();
			try {
				JSONArray contributionsList = (JSONArray) parser
						.parse(EntityUtils.toString(reqRespClientObj.getEntity(), Charset.forName(Constants.ENCODING)));
				for (int i = 0; i < contributionsList.size(); i++) {
					JSONObject contribution = (JSONObject) contributionsList.get(i);
					if (contribution.get(Constants.LOGIN).equals(login)) {
						contributions += (long) contribution.get(Constants.CONTRIBUTIONS);
					}
				}
				repository.setContribution(contributions);
			} catch (ParseException e) {
				Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
			} catch (IOException e) {
				Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
			}
		} else {
			Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD,
					"Unable to fetch contributions for user: " + login + " for repository : " + repository.getName() + " : Reason is : " + reqRespClientObj.getErrorMsg());
			repository.setContribution(0);
			return CompletableFuture.completedFuture(null);
		}

		return CompletableFuture.completedFuture(null);
	}
}
