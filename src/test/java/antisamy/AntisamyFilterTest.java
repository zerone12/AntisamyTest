package antisamy;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcf.data.GridData;
import jcf.sua.mvc.MciRequest;
import jcf.sua.ux.json.dataset.JsonDataSetReader;
import jcf.sua.ux.json.mvc.JsonRequest;
import jcf.sua.ux.webflow.dataset.WebFlowDataSetReader;
import jcf.sua.ux.webflow.mvc.WebFlowRequest;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import sample.model.TweetVO;
import sample.mvc.SecureRequest;

public class AntisamyFilterTest {

	private TestObjectBuilder builder = new TestObjectBuilder();
	private TestJsonObjectBuilder jsonBuilder = new TestJsonObjectBuilder();
	private Map<String, String[]> paramMap = new HashMap<String, String[]>();

	@Test
	public void test_Antisamy() throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException{
		//WebFlow getParam(), getParam(clazz)
		TweetVO expectedObject = builder.getTweetVO();
		SecureRequest secureRequest = new SecureRequest(getMciRequest(expectedObject));

		Map<String, Object> actualMap = secureRequest.getParam();
		assertThat(expectedObject.getId(), is(actualMap.get("id")));
		assertThat("", is(actualMap.get("tweets")));

		TweetVO actualObject = secureRequest.getParam(TweetVO.class);
		assertThat(expectedObject.getId(), is(actualObject.getId()));
		assertThat("", is(actualObject.getTweets()));

		//Json get(dataSetId, clazz), getGridData(dataSetId, clazz)
		TweetVO expectedJsonObject = jsonBuilder.buildSingleObject();
		SecureRequest secureJsonRequest = new SecureRequest(getJsonMciRequest(expectedJsonObject));

		TweetVO actualJsonObject = secureJsonRequest.get("tweet", TweetVO.class);
		assertThat(expectedJsonObject.getId(), is(actualJsonObject.getId()));
		assertThat("", is(actualJsonObject.getTweets()));

		List<TweetVO> expectedJsonListObject = jsonBuilder.buildCollectionObject();
		SecureRequest secureJsonListRequest = new SecureRequest(getJsonMciRequest(expectedJsonListObject));

		GridData<TweetVO> actualJsonListObject = secureJsonListRequest.getGridData("tweetList", TweetVO.class);
		for(int i = 0; i < actualJsonListObject.size(); i++){
			assertThat(expectedJsonListObject.get(i).getId(), is(actualJsonListObject.get(i).getId()));
			assertThat(expectedJsonListObject.get(i).getRegister(), is(actualJsonListObject.get(i).getRegister()));
			assertThat("", is(actualJsonListObject.get(i).getTweets()));
		}
	}

	private MciRequest getMciRequest(final Object object) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
		MockHttpServletRequest request = new MockHttpServletRequest();

		Class<?> type = object.getClass();
		Field[] fields = type.getDeclaredFields();

		for(Field field : fields){
			field.setAccessible(true);
			String key = field.getName();
			Object value = field.get(object);
			String[] retValue = null;

			if(value != null){
				if(field.get(object).getClass().isAssignableFrom(String[].class)){
					retValue = (String[])value;
				}else{
					retValue = new String[]{(String)value};
				}

				paramMap.put(key, retValue);
			}
		}

		request.addParameters(paramMap);

		return new WebFlowRequest(new WebFlowDataSetReader(request, null), null);
	}

	private MciRequest getJsonMciRequest(final Object object){
		return new JsonRequest(new JsonDataSetReader(new MockHttpServletRequest(){

			@Override
			public BufferedReader getReader() throws UnsupportedEncodingException {
				return new BufferedReader(new StringReader(new antisamy.JsonWriter().writeJsonData(object)));
			}

		}, null), null);
	}
}
