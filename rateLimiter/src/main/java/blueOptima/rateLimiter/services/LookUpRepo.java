package blueOptima.rateLimiter.services;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import blueOptima.rateLimiter.handler.RateLimiterHandler;
import blueOptima.rateLimiter.profile.Profile;
import blueOptima.rateLimiter.repo.Repository;
import blueOptima.rateLimiter.utils.CommonUtils;
import blueOptima.rateLimiter.utils.Constants;
import blueOptima.rateLimiter.utils.HttpCollection;

public class LookUpRepo {
	private static final String CLASS = "LookUpRepo";

	/*
	 * Add Repositories for the user
	 */
	public CompletableFuture<Void> addRepos(Profile profile) throws InterruptedException {
		final String METHOD = "addRepos";

		RateLimiterHandler handler = new RateLimiterHandler();
		String repoUrl = Constants.URL_USER_REPO + profile.getLogin() + "/" + Constants.REPOS;

		HttpCollection reqRespClientObj = new HttpCollection();

		CommonUtils.buildRequest(repoUrl, reqRespClientObj);

		if (CommonUtils.getResponse(reqRespClientObj)) {
			List<Repository> repositories = new ArrayList<>();
			try {
				JSONParser parser = new JSONParser();
				JSONArray repoArr = (JSONArray) parser
						.parse(EntityUtils.toString(reqRespClientObj.getEntity(), Charset.forName("UTF-8")));
				LookUpContri lookUpContri = new LookUpContri();
				CompletableFuture<Void>[] threads = new CompletableFuture[repoArr.size()];
				for (int i = 0; i < repoArr.size(); i++) {
					JSONObject repo = (JSONObject) repoArr.get(i);
					Repository tempRepo = new Repository();
					tempRepo.setName((String) repo.get(Constants.NAME));
					if (!handler.exceededCoreRateLimit()) {
						threads[i] = CompletableFuture.runAsync(() -> {
							try {
								lookUpContri.addContri(tempRepo, profile.getLogin());
								repositories.add(tempRepo);
							} catch (InterruptedException e) {
								Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
							}
						});
						RateLimiterHandler.coreRateLimit--;
					}
				}
				CompletableFuture.allOf(threads).join();
				
				profile.setRepositories(repositories);
			} catch (ParseException e) {
				Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
			} catch (IOException e) {
				Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
			}
		} else {
			Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD,
					"Unable to fetch repositories for user: " + profile.getLogin() + " : Reason is : " + reqRespClientObj.getErrorMsg());
		}

		return CompletableFuture.completedFuture(null);
	}
}
