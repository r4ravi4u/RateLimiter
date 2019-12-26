package blueOptima.rateLimiter.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import blueOptima.rateLimiter.handler.RateLimiterHandler;
import blueOptima.rateLimiter.profile.Profile;
import blueOptima.rateLimiter.utils.Constants;

public class ProfileService {
	private final static String CLASS = "ProfileService";

	/*
	 * Fetch Data from Input File and add to profile
	 */
	private boolean addInputData(List<Profile> profiles, String inputFilePath) {
		final String METHOD = "addInputData";

		File file = new File(inputFilePath);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				String[] input = line.split(" ");
				Profile prof = new Profile();
				prof.setFirstName(input[0]);
				if (input.length > 1)
					prof.setLastName(input[1]);

				//surrounded with quotes for proper search query for city
				if (input.length > 2) {	
					StringBuilder city = new StringBuilder("\"");
					for (int i = 2; i < input.length; i++) {
						city.append(input[i]);
						if (i != input.length - 1)
							city.append(" ");
					}
					prof.setCity(city.toString() + '"'); 
				}
				profiles.add(prof);
			}
			br.close();
		} catch (IOException e) {
			Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, "Error Reading data from input file" + e.toString());
		}
		
		if(profiles == null || profiles.size() < 1)
			return false;
		
		return true;
	}

	/**
	 * Add Profile Info of the user
	 */
	int usersNotFound = 0;

	private void addProfileDetail(RateLimiterHandler handler, List<Profile> profiles) {
		final String METHOD = "addProfileDetail";

		SearchUser searchUser = new SearchUser();
		CompletableFuture<Void>[] threads = new CompletableFuture[profiles.size()];

		for (int i = 0; i < profiles.size(); i++) {
			Profile p = profiles.get(i);
			if (!handler.exceededSearchRateLimit()) {
				try {
					threads[i] = CompletableFuture.runAsync(() -> {
						try {
							searchUser.searchUser(p, profiles);
							if (p.getLogin() == null)
								usersNotFound++;
						} catch (InterruptedException e) {
							Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
						}
					});
				} catch (Exception e) {
					Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
				}
				RateLimiterHandler.searchRateLimit--;
			}
		}
		CompletableFuture.allOf(threads).join();
		if (usersNotFound > 0)
			Constants.LOGGER.logp(Level.WARNING, CLASS, METHOD, "Total Users Not Found : " + usersNotFound);

		int size = profiles.size();
		Constants.LOGGER.logp(Level.INFO, CLASS, METHOD, "Final Total Profiles we have : " + size);
		int count = 0;
		threads = null;
		threads = new CompletableFuture[size - usersNotFound];
		LookUpRepo lookUpRepo = new LookUpRepo();
		for (int i = 0; i < profiles.size(); i++) {
			Profile p = profiles.get(i);
			if (!handler.exceededCoreRateLimit()) {
				if (p.getLogin() != null) {
					threads[count++] = CompletableFuture.runAsync(() -> {
						try {
							lookUpRepo.addRepos(p);
						} catch (InterruptedException e) {
							Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
						}
					});
					RateLimiterHandler.coreRateLimit--;
				} else {
					Constants.LOGGER.logp(Level.WARNING, CLASS, METHOD, "Repository Not found for user having first name : "
							+ profiles.get(i).getFirstName() + ", last name : " + profiles.get(i).getLastName());
				}
			}
		}
		CompletableFuture.allOf(threads).join();
	}

	/**
	 * Main Method from where Real Execution Starts. Fetches Rate Limits (Core &
	 * Search), Read Input File and return populated Profiles
	 */
	public static List<Profile> getProfiles(String inputFilePath, String auth) {
		RateLimiterHandler handler = new RateLimiterHandler(auth);
		ProfileService profsvc = new ProfileService();
		List<Profile> profiles = new ArrayList<>();
		
		if (!handler.exceededCoreRateLimit() && !handler.exceededSearchRateLimit()) {
			if(profsvc.addInputData(profiles, inputFilePath))
				profsvc.addProfileDetail(handler, profiles);
		}

		return profiles;
	}

	/*
	 * Generates Output File as Json Format Profile & Repositories Info
	 * In case result is null or empty, then no output file will be generated
	 */
	public static void generateOutput(List<Profile> result, String outputFilePath) {
		final String METHOD = "generateOutput";
		
		if(result == null || result.size() <= 0)
			return;
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFilePath), result);
		} catch (JsonGenerationException e) {
			Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
		} catch (JsonMappingException e) {
			Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
		} catch (IOException e) {
			Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, e.toString());
		}

		Constants.LOGGER.logp(Level.INFO, CLASS, METHOD, "Output File Generated");
	}
}
