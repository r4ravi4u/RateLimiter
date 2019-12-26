package blueOptima.rateLimiter.utils;

import java.util.logging.Logger;

import blueOptima.rateLimiter.services.ProfileService;

/**
 * Constants to be declared at a common place for ease of understanding and code
 * maintenance
 */
public interface Constants {

	Logger LOGGER = Logger.getLogger(ProfileService.class.getName());

	String CMD_LINE_ARGS_ERROR = "Pls provide proper Input / Output File Paths as Command Line Arguments";

	String USER_AGENT = "BlueOptimaGithub";

	String URL_RATE_LIMIT = "https://api.github.com/rate_limit";
	String URL_REPO_CONTRIBUTIONS = "https://api.github.com/repos/";
	String URL_USER_REPO = "https://api.github.com/users/";
	String URL_USER_SEARCH = "https://api.github.com/search/users";

	String RESOURCES = "resources";
	String CONTRIBUTORS = "contributors";
	String REPOS = "repos";
	String CONTRIBUTIONS = "contributions";

	String CORE = "core";
	String SEARCH = "search";

	String LIMIT = "remaining";

	String QUERY_FULL_NAME = " in:fullname";
	String QUERY_LOCATION = " location:";

	String ITEMS = "items";
	String LOGIN = "login";
	String ID = "id";
	String URL = "url";
	String NAME = "name";

	String ENCODING = "UTF-8";
}
