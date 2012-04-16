package sample.model;

public class TweetVO {

	private String id;
	private String register;
	private String tweets;
	private String regDate;
	private String[] test;

	public TweetVO() {
		super();
	}
	public TweetVO(String id, String register, String tweets, String regDate, String[] test) {
		super();
		this.id = id;
		this.register = register;
		this.tweets = tweets;
		this.regDate = regDate;
		this.test = test;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRegister() {
		return register;
	}
	public void setRegister(String register) {
		this.register = register;
	}
	public String getTweets() {
		return tweets;
	}
	public void setTweets(String tweets) {
		this.tweets = tweets;
	}
	public String getRegDate() {
		return regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
	public String[] getTest() {
		return test;
	}
	public void setTest(String[] test) {
		this.test = test;
	}
}
