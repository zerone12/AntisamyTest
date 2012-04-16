package antisamy;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import jcf.sua.mvc.MciRequest;
import jcf.sua.ux.webflow.dataset.WebFlowDataSetReader;
import jcf.sua.ux.webflow.mvc.WebFlowRequest;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import sample.model.TweetVO;
import sample.mvc.SecureRequest;

public class AntisamyFilterTest {

	private TestObjectBuilder builder = new TestObjectBuilder();
	private Map<String, String[]> paramMap = new HashMap<String, String[]>();

	@Test
	public void test_Antisamy() throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException{
		TweetVO expected = builder.getTweetVO();
		SecureRequest secureRequest = new SecureRequest(getMciRequest(expected));
		Map actual= secureRequest.getParam();

		assertThat(expected.getId(), is(actual.get("id")));
		assertThat("", is(actual.get("tweets")));
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
}
