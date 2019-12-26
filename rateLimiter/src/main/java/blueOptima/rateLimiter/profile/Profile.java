package blueOptima.rateLimiter.profile;

import java.util.List;

import blueOptima.rateLimiter.repo.Repository;

/*
 * Simple POJO for a User profile
 */
public class Profile	{
	private String firstName;
	private String lastName;
	private String city;

	private String profileUrl;

	private String login;
	private long id;

	private List<Repository> repositories;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProfileUrl() {
		return profileUrl;
	}

	public void setProfileUrl(String profUrl) {
		this.profileUrl = profUrl;
	}

	public List<Repository> getRepositories() {
		return repositories;
	}

	public void setRepositories(List<Repository> repos) {
		this.repositories = repos;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	/*
	 * To be used when search query return more than 1 record with matching criteria
	*/
	@Override
	public Object clone()	{
		Profile p = new Profile();
		if(firstName != null)
			p.setFirstName(firstName);
		
		if(lastName != null)
			p.setLastName(lastName);
		
		if(city != null)
			p.setCity(city);
		
		return p;
	}
}
