package antisamy;

import java.io.UnsupportedEncodingException;
import java.util.List;

import jcf.sua.dataset.DataSetWriter;
import jcf.sua.mvc.MciDataSetAccessor;
import jcf.sua.mvc.MciResponse;
import jcf.sua.ux.json.dataset.JsonDataSetWriter;
import jcf.sua.ux.json.mvc.JsonResponse;

import org.springframework.mock.web.MockHttpServletResponse;

public class JsonWriter {

	public String writeJsonData(Object object) throws UnsupportedEncodingException{
		MockHttpServletResponse response = new MockHttpServletResponse();
		MciResponse jsonResponse = new JsonResponse();

		if(object instanceof List){
			jsonResponse.setList("tweetList", (List)object);
		}else{
			jsonResponse.set("tweet", object);
		}

		DataSetWriter writer = new JsonDataSetWriter(response, (MciDataSetAccessor) jsonResponse);

		writer.write();

		return response.getContentAsString();
	}
}
