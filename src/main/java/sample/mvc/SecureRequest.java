package sample.mvc;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jcf.data.GridData;
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

	public <E> E get(String datasetId, Class<E> clazz) {
		E bean = request.get(datasetId, clazz);

		Class object = bean.getClass();
		Field[] fields = object.getDeclaredFields();

		for(Field field : fields){
			Object value = null;
			field.setAccessible(true);

			try {
				value = field.get(bean);
			} catch (Exception e) {

			}

			try {

				if(value != null){
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

					field.set(bean, value);
				}

			} catch (Exception e) {
				throw new MciException("[AbstractMciRequest] AbstractMciRequest - " + e.getMessage(), e);
			}
		}

		return bean;
	}


	public Map<String, Object> getParam() {
		return doFilter(request.getParam());
	}

	public <T> T getParam(Class<T> type) {
		T object = BeanUtils.instantiate(type);
		Field[] fields = type.getDeclaredFields();

		for(Field field : fields){
			Object value = null;

			if(String[].class.isAssignableFrom(field.getType())){
				value = getParamArray(field.getName());
			} else{
				value = getParam(field.getName());
			}

			field.setAccessible(true);

			try {

				if(value != null){
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

					field.set(object, value);
				}

			} catch (Exception e) {
				throw new MciException("[AbstractMciRequest] AbstractMciRequest - " + e.getMessage(), e);
			}
		}

		return object;
	}

	public <E> GridData<E> getGridData(String datasetId, Class<E> clazz) {
		GridData<E> gridData = request.getGridData(datasetId, clazz);
		for(int i = 0; i < gridData.size(); i++){
			E bean = gridData.get(i);
			Class object = bean.getClass();
			Field[] fields = object.getDeclaredFields();

			for(Field field : fields){
				Object value = null;
				field.setAccessible(true);

				try {
					value = field.get(bean);
				} catch (Exception e) {

				}

				try {

					if(value != null){
						if(value.getClass().isAssignableFrom(String[].class)){
							String[] temp = (String[])value;
							for(int j = 0; j < temp.length; j++){
								String clean = doFilter(temp[j]);
								temp[j] = clean;
							}

							value = temp;
						}else{
							value = doFilter((String)value);
						}

						field.set(bean, value);
					}

				} catch (Exception e) {
					throw new MciException("[AbstractMciRequest] AbstractMciRequest - " + e.getMessage(), e);
				}
			}

		}

		return gridData;
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
