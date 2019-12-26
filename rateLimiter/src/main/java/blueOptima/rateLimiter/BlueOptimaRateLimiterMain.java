package blueOptima.rateLimiter;

import java.util.logging.Level;

import blueOptima.rateLimiter.services.ProfileService;
import blueOptima.rateLimiter.utils.Constants;

/*
 * Main Driver Class which calls the real method to be executed and Generates Output File
 * Command Line Arguments : 
 * 0 -> Input File Path
 * 1 -> Output File Path (Will create File in case does not exist)
 * 2 -> (Limits are per hour)
 * a) If Null (Not given), means No Authorization hence Core Limit = 60, Search Limit = 10
 * b) If file path given, means it could be either of 2 types : 
 * 		2 Lines in file : Line 1 is Client Id, Line 2 is Client Secret
 * 		1 Line in file : Access Token is given
 * In both the case (if authenticated properly), Core Limit = 5000, Search Limit = 30, else same as Case a)
 * Atleast Command Line Arguments length shall be >= 2
 */

public class BlueOptimaRateLimiterMain {
	private final static String CLASS = "BlueOptimaRateLimiterMain";

	public static void main(String[] args) {
		final String METHOD = "mainDriver";

		if (args == null || args.length < 2) {
			Constants.LOGGER.logp(Level.SEVERE, CLASS, METHOD, Constants.CMD_LINE_ARGS_ERROR);
			return;
		}

		String str = null;
		if (args.length > 2)
			str = args[2];

		long time = System.currentTimeMillis();
		ProfileService.generateOutput(ProfileService.getProfiles(args[0], str), args[1]);
		System.out.println(System.currentTimeMillis() - time);
	}
}