package sample.mvc;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jcf.sua.exception.MciException;
import jcf.sua.mvc.MciRequest;
import jcf.sua.mvc.MciRequestAdapter;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

public class SecureRequest extends MciRequestAdapter {

	private String policyPath;
	private Map<String, Object> cleanMap;
	private AntiSamy as;

	public SecureRequest(MciRequest request) {
		super(request);
		this.cleanMap = new HashMap<String, Object>();
		this.policyPath = "antisamy-ebay-1.4.4.xml";
	}

	public <T> T getParam(Class<T> type) {
		return this.getParam(type, null);
	}

	public Map<String, Object> getParam() {
		return doFilter(request.getParam());
	}

	public <T> T getParam(Class<T> type, String filter) {
		T object = BeanUtils.instantiate(type);
		Field[] fields = type.getDeclaredFields();

		for(Field field : fields){
			Object value = null;

			if(String[].class.isAssignableFrom(field.getType())){
				value = getParamArray(field.getName());
			} else{
				value = getParam(field.getName());
			}

			if(StringUtils.hasText(filter))	{
				if(!isSelectedProperty(filter, field.getName()))	{
					continue;
				}

				if(isRequiredProperty(filter, field.getName()))	{
					if(value == null || (value instanceof String && !StringUtils.hasText((String) value)))	{
						throw new MciException("Column[" + field.getName() + "] 는 요청[" + filter + "] 에 의해 필수값으로 설정되었습니다.");
					}
				}
			}

			field.setAccessible(true);

			try {
				if(value.getClass().isAssignableFrom(String[].class)){
					String[] temp = (String[])value;
					for(int i = 0; i < temp.length; i++){
						String clean = doFilter(temp[i]);
						temp[i] = clean;
					}

					value = temp;
				}else{
					value = doFilter((String)value);
				}

				if (value != null) {
					field.set(object, value);
				}
			} catch (Exception e) {
				throw new MciException("[AbstractMciRequest] AbstractMciRequest - " + e.getMessage(), e);
			}
		}

		return object;
	}


	/**
	 * dirty string을 antisamy를 통해 필터링 후 리턴
	 * @param dirty
	 * @return
	 */
	private String doFilter(String dirty){
		try {
			URL url=SecureRequest.class.getClassLoader().getResource(policyPath);
			Policy policy = Policy.getInstance(url);
			as = new AntiSamy(); // Create AntiSamy object
			CleanResults cr = as.scan(dirty, policy, AntiSamy.SAX);
			return cr.getCleanHTML();
		} catch (PolicyException e) {
			e.getStackTrace();
		} catch (ScanException e) {
			e.getStackTrace();
		}

		return dirty;
	}


	/**
	 * map에 담긴 dirty string 필터링 후 반환
	 * @param map
	 * @return
	 */
	private Map<String, Object> doFilter(Map<String, Object> map) {
		Enumeration<String> keys = Collections.enumeration(map.keySet());
		while(keys.hasMoreElements()){
			String key = keys.nextElement().toString();
			Object value = map.get(key);

			if(value.getClass().isAssignableFrom(String[].class)){
				String[] temp = (String[])value;
				for(int i = 0; i < temp.length; i++){
					String clean = doFilter(temp[i]);
					temp[i] = clean;
				}
				value = temp;
			}else{
				value = doFilter((String)map.get(key));
			}

			map.put(key, value);
		}
		cleanMap.putAll(map);

		return cleanMap;
	}

	protected boolean isSelectedProperty(String expr, String property)	{
		boolean result = true;

		if(expr != null)	{
			result = expr.contains(property);
		}

		return result;
	}

	protected boolean isRequiredProperty(String expr, String property)	{
		return expr.contains(String.format("%s+", property));
	}
}
