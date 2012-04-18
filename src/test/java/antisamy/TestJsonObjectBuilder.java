package antisamy;

import java.util.ArrayList;
import java.util.List;

import sample.model.TweetVO;

public class TestJsonObjectBuilder {

	public TweetVO buildSingleObject(){
		return new TweetVO("id1", "reg1", "<IMG SRC=javascript:alert!('XSS')>", null, null);
	}

	public List<TweetVO> buildCollectionObject(){
		List<TweetVO> list = new ArrayList<TweetVO>();
		list.add(new TweetVO("id1", "reg1", "<IMG SRC=javascript:alert!('XSS')>", null, null));
		list.add(new TweetVO("id2", "reg2", "<SCRIPT/XSS SRC='http://xxxx/xss.js'></SCRIPT>", null, null));
		list.add(new TweetVO("id3", "reg3", "<IFRAME SRC='javascript:alert!('XSS');'></IFRAME>", null, null));
		list.add(new TweetVO("id4", "reg4", "<IMG SRC=javascript:alert!('XSS')>", null, null));
		list.add(new TweetVO("id5", "reg5", "<IMG SRC=javascript:alert!('XSS')>", null, null));

		return list;
	}
}
