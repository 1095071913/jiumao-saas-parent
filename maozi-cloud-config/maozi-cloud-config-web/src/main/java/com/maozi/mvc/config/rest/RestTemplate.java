package com.maozi.mvc.config.rest;

import com.maozi.common.BaseCommon;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Tags;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;

public class RestTemplate extends org.springframework.web.client.RestTemplate {
	
	@Nullable
	@Override
	protected <T> T doExecute(URI url, @Nullable HttpMethod method, @Nullable RequestCallback requestCallback,@Nullable ResponseExtractor<T> responseExtractor) throws RestClientException {

		Boolean error = false;

		Long startTime = System.currentTimeMillis();

		Map<String,String> logs = new LinkedHashMap<String, String>();
		
		ClientHttpResponse response = null;
		
		try {
			
			Assert.notNull(url, "URI is required");
			
			Assert.notNull(method, "HttpMethod is required");
			
			ClientHttpRequest request = createRequest(url, method);
	        
            logs.put("Type", "RestTemplate");
            
            logs.put("URI", request.getURI().toString());
            
            logs.put("Method", request.getMethod().toString());
	        
			if (requestCallback != null) {
				requestCallback.doWithRequest(request);
			}
			
			InputStream paramData = parse(request.getBody());
			
			String requestParam = new String(readStream(paramData));
			
			functionParam(requestParam);
			
			logs.put("Param", requestParam);
	        
			response = request.execute();
			
			handleResponse(url, method, response);
			
			T t=(responseExtractor != null ? responseExtractor.extractData(response) : null);
			
			logs.put("Code", response.getRawStatusCode()+"");
			
        	logs.put("RT", (System.currentTimeMillis() - startTime) + " ms");
        	
            logs.put("Data", t.toString());
	        
			return t;
		} 
		catch (Exception e) {

			error = true;
			
			String resource = url.toString();
			
			String query = url.getRawQuery();
			
			resource = (query != null ? resource.substring(0, resource.indexOf('?')) : resource);
			
			StackTraceElement stackTraceElement = stackTraceElement = e.getStackTrace()[0];
			
			String stackTrace = BaseCommon.getStackTrace(e);
			
			BaseCommon.log.error(stackTrace);
			
			functionError(stackTrace);
            
            logs.put("ErrorUser",BaseCommon.getCurrentUserName());
            logs.put("ErrorDesc", e.getLocalizedMessage());
            logs.put("ErrorLine", stackTraceElement.toString());
            
			return null;

		}finally {
			
			if (response != null) {
				response.close();
			}

			StringBuilder respSql = BaseCommon.sql.get();
			if (BaseCommon.isNotNull(respSql)) {
				logs.put("SQL", respSql.toString());
			}

			logs.put("RT", (System.currentTimeMillis() - startTime) + " ms");

			BaseCommon.log(error,logs);
			
		}

	}
	
	
	
	@Override
	public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables)
			throws RestClientException {

		RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables)
			throws RestClientException {

		RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) throws RestClientException {
		RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor);
	}


	// HEAD

	@Override
	public HttpHeaders headForHeaders(String url, Object... uriVariables) throws RestClientException {
		return execute(url, HttpMethod.HEAD, null, headersExtractor(), uriVariables);
	}

	@Override
	public HttpHeaders headForHeaders(String url, Map<String, ?> uriVariables) throws RestClientException {
		return execute(url, HttpMethod.HEAD, null, headersExtractor(), uriVariables);
	}

	@Override
	public HttpHeaders headForHeaders(URI url) throws RestClientException {
		return execute(url, HttpMethod.HEAD, null, headersExtractor());
	}
	
	@Override
	public <T> ResponseEntity<T> postForEntity(String url, @Nullable Object request,
			Class<T> responseType, Object... uriVariables) throws RestClientException {

		RequestCallback requestCallback = httpEntityCallback(request, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> postForEntity(String url, @Nullable Object request,
			Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {

		RequestCallback requestCallback = httpEntityCallback(request, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> postForEntity(URI url, @Nullable Object request, Class<T> responseType)
			throws RestClientException {

		RequestCallback requestCallback = httpEntityCallback(request, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor);
	}
	
	@Override
	public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
			@Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables)
			throws RestClientException {

		RequestCallback requestCallback = httpEntityCallback(requestEntity, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, method, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
			@Nullable HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables)
			throws RestClientException {

		RequestCallback requestCallback = httpEntityCallback(requestEntity, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, method, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
			Class<T> responseType) throws RestClientException {

		RequestCallback requestCallback = httpEntityCallback(requestEntity, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, method, requestCallback, responseExtractor);
	}

	@Override
	public <T> ResponseEntity<T> exchange(String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
			ParameterizedTypeReference<T> responseType, Object... uriVariables) throws RestClientException {

		Type type = responseType.getType();
		RequestCallback requestCallback = httpEntityCallback(requestEntity, type);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(type);
		return execute(url, method, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> exchange(String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
			ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) throws RestClientException {

		Type type = responseType.getType();
		RequestCallback requestCallback = httpEntityCallback(requestEntity, type);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(type);
		return execute(url, method, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
			ParameterizedTypeReference<T> responseType) throws RestClientException {

		Type type = responseType.getType();
		RequestCallback requestCallback = httpEntityCallback(requestEntity, type);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(type);
		return execute(url, method, requestCallback, responseExtractor);
	}

	@Override
	public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> responseType)
			throws RestClientException {

		RequestCallback requestCallback = httpEntityCallback(requestEntity, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return doExecute(requestEntity.getUrl(), requestEntity.getMethod(), requestCallback, responseExtractor);
	}

	@Override
	public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, ParameterizedTypeReference<T> responseType)
			throws RestClientException {

		Type type = responseType.getType();
		RequestCallback requestCallback = httpEntityCallback(requestEntity, type);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(type);
		return doExecute(requestEntity.getUrl(), requestEntity.getMethod(), requestCallback, responseExtractor);
	}
	
	public ByteArrayInputStream parse(final OutputStream out) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos = (ByteArrayOutputStream) out;
        final ByteArrayInputStream swapStream = new ByteArrayInputStream(baos.toByteArray());
        return swapStream;
    }
	
	public byte[] readStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		return outSteam.toByteArray();
	}
	
	@Trace
    @Tags({@Tag(key = "错误值", value = "arg[0]")})
    public void functionError(Object errorMessage) {}
	
	@Trace(operationName = "入参值")
    @Tags({@Tag(key = "入参值", value = "arg[0]")})
    public void functionParam(Object param) { }

}
