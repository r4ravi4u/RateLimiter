package blueOptima.rateLimiter.repo;

/*
 * POJO to save Repository Data in a User Profile for contributions aspect repo wise
 */
public class Repository {

	private String name;
	private long contribution;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getContribution() {
		return contribution;
	}

	public void setContribution(long contribution) {
		this.contribution = contribution;
	}

	@Override
	public String toString() {
		return "Repository [contribution=" + contribution + ", name=" + name + "]";
	}
}
