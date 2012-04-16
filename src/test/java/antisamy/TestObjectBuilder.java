package antisamy;

import sample.model.TweetVO;

public class TestObjectBuilder {

	public TweetVO getTweetVO() {
		TweetVO tweetVO = new TweetVO();
		tweetVO.setTweets("<IMG SRC=javascript:alert!('XSS')>");
		tweetVO.setId("mine");
		return tweetVO;
	}
}
