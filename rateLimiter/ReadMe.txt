1. Pre-requisites ?
	a) Any IDE - Eclipse, IntelliJ, etc.
	b) Java 8 Environment
	c) Maven Support
	d) Laptop Connected to Internet
	
2. How to Run ?
	a) Import Rate Limiter Project into your IDE
	b) Maven pom.xml is already updated - Dependencies will be downloaded (Required Internet)
	c) Once project gets imported -> Right Click on project -> Do Maven Clean -> Do Maven Test
	d) Right Click on Project -> Run as "Run Configurations" -> "Arguments" Tab -> Copy & Paste "input.txt output.txt auth_token.txt" over there
		i) input.txt -> Contains Sample Input to the program (Mandatory)
		ii) output.txt -> Name of the file you want to give output - Json Format (Mandatory)
		iii) authorization -> either "client id & client secret" OR "access token" mentioned in a file. In case not given, it treats as an unauthorize request
		** In case you want to update Input File or auth file and put it somewhere else in your system, you can do that and give path here as command line arguments
			-> If Null (Not given), means No Authorization hence Core Limit = 60, Search Limit = 10
			-> If file path given, means it could be either of 2 types : 
			-> Lines in file : Line 1 is Client Id, Line 2 is Client Secret
			-> 1 Line in file : Access Token is given
			-> In both the case (if authenticated properly), Core Limit = 5000, Search Limit = 30, else same as Case 1st
			-> Atleast Command Line Arguments length shall be >= 2
			
	e) Apply Configuration and Click on Run
	f) Output will be generated in the file given as 2nd Command Line Argument (args[1]) - Json Formatted

3. How its Done ?
	A) 	GitHub applies rate limits to the use their API, this rate limit differs between an anonymous user and a logged in user. 
		The rate limits are in place to avoid any type of D/DOS attacks at the same time to avoid malicious scanning of user data. 
		This rate limit needs to be honoured to avoid account lockouts, this could also lead to the IP getting flagged and blocked by the service provider
	
	B) 	Mainly 3 types of rate limits :
			i. Core Rate limit - Main rate Limit (Tag with Rate Limit has been deprecated). We consider this as our main Rate Limit Factor
				a) UnAuthorized : 60
				b) Authorized : 5000
			ii. Search Rate limit - Search Query Rate Limit Factor. To be considered while searching for a user (SearchUser)
				a) UnAuthorized : 10
				b) Authorized : 30
			iii. graphql rate limit - v4 GraphQL Node Limits (In case not using REST). This limit has not been used in our project
				a) UnAuthorized : 0
				b) Authorized : 5000
		
	C) 	To avoid account lockout, Remaining requests have been taken into consideration
		Cached CoreRateLimit and SearchRateLimit values from first REST API call and decremented values on each subsequent call. Refreshes the rate when counter becomes <= 0
		In case after Refresh, counter still <= 0, problematic situation and hence will come out of the program and generate output till fetched Data.
		
	D) Steps Followed : (For All Kind of API hits URL info check "Constants.java" file in the package "blueOptima.rateLimiter.utils")
		a) Authorization Checks happen and variables set for subsequent calls to HTTP GET from Rate Limiter Handler Constructor. Rate Limit Variables loaded (cached)
		
		b) Fetch Data from Input File -> Load Profiles - First Name, Last Name, City (if provided).
			For City - Can contain spaces which can fail queries, hence protected them into double quotes "".
		
		c) Build request with search query -> Search User (Each search request in Async Thread - returns Completable Future)
			i) In case more then 1 users found with same search query (max 30 is provided in response by Github API), load all found users into Profiles List
			ii) In case user not found, we won't be fetching any repo info for that user
		
		d) Once all user search requests happened - our List has now may be increased if Case c-i) happens. Profile Detail has also been loaded here
		
		e) Now time to load Repo Information (Repo Name with Contributions)
			i) Check Each Repo for its individual Contributions by respective Developer
			ii) Each Repo API is hit by Async Threads (Completable Future)
			iii) Each Contribution API is also hit by further Aysnc Threads
			iv) When All repo-Contri result came for a particular developer then add it into profile info
			v) When All Developers info (Repo - Contri) populated, then only proceed further
		
		f) Now once all info gets populated by All Threads (Fork Join Pool at backend by Completable Future), then Generate Output in formatted JSON

4. Assumptions	:
		a) Input Format - "FirstName LastName City" - City is Optional and may contain Spaces as well
		b) Authorization mechanisms taken into account - Either via ClientId & ClientSecret OR Via Access Token. Else meant Unauthorized

5. Improvements & Enhancements : 
		a) If login was provided then directly we can hit exact URL for the unique user. Search API avoidance and hence no use of SearchRateLimiter
		b) In case we find multiple users with same search criteria, then we need to include all of them (max 30), in our Output which results in manual checking of desired User Info
		c) Right now reading input from plain txt File. Can make it work for different File Formats viz. xls, xlsx, doc, docx, xml, json, etc. with different Parsers and 3rd Party Libraries
		d) Same we can do for Output as well as per above c) statement
		e) Can have Different ways to get Input from User - Command Line Args (already done), proper UI, Read from Console @ Runtime, via some network Stream, etc.