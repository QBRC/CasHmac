package edu.swmed.qbrc.auth.cashmac.shared.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.container.ContainerRequestContext;
import org.apache.commons.codec.binary.Base64;
import org.jboss.logging.Logger;

public class HMACUtils {
 
	private static final Logger log = Logger.getLogger(HMACUtils.class);
	
	private final static String HMAC_SHA1_ALGORITHM = "HmacSHA1";
 
	/**
	 * Appends HMAC, date, and client id to a ClientRequest
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static void createSignatureAndSignRequest(ClientRequestContext context, String host, String clientId, String secret) throws Exception {
		// Get current date
		String currentDate = Long.toString(System.currentTimeMillis());

		// Get string to sign with HMAC
		String toSign = createSignature(context, currentDate);
		
		// Get HMAC
		String signature = calculateRFC2104HMAC(toSign, secret);
		log.trace("HMAC to Sign on Client Filter:\n" + toSign);
		
		// Add headers to request
		context.getHeaders().add("Signature", signature);
		context.getHeaders().add("ClientId", clientId);
		context.getHeaders().add("Date", currentDate);
	}
	
	/**
	 * Creates the signature to be passed to the HMAC function.
	 * @param request
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String createSignature(ContainerRequestContext context, String date) throws Exception {
		StringBuilder s = new StringBuilder();
		
		// HTTP Verb
		s.append(context.getRequest().getMethod()).append("\n");
		
		// Host header
		String host = context.getUriInfo().getRequestUri().getHost() + ":" + context.getUriInfo().getRequestUri().getPort();
		s.append(host.toLowerCase()).append("\n");
		
		// URI
		s.append(baseUriOnly(context.getUriInfo().getPath())).append("\n");

		// Date
		s.append(date).append("\n");

		// Query String
		s.append(buildSortedQueryString(context));

		log.trace("HMAC to Sign: " + s.toString());
		
		// Return value
		return s.toString();
	}

	private static String baseUriOnly(String uri) {
		String toReturn = uri;
		
		if (uri.indexOf("?") > 0) {
			toReturn = toReturn.substring(0, uri.indexOf("?"));
		}
		
		return toReturn;
	}
	
	/**
	 * Creates the signature to be passed to the HMAC function.
	 * @param request
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String createSignature(ClientRequestContext context, String date) throws Exception {
		StringBuilder s = new StringBuilder();
		
		// HTTP Verb
		s.append(context.getMethod()).append("\n");
		
		// Host header
		String host = context.getUri().getHost() + ":" + context.getUri().getPort();
		s.append(host.toLowerCase()).append("\n");
		
		// URI
		s.append(context.getUri().getPath()).append("\n");

		// Date
		s.append(date).append("\n");

		// Query String
		s.append(buildSortedQueryString(context));

		log.trace("HMAC to Sign: " + s.toString());

		// Return value
		return s.toString();
	}
	
	/**
	 * Sorts the query string parameters, URL encodes them, and joins them in
	 * '&' delimited, '=' separated name/value pairs.
	 * @param request
	 * @return
	 * @throws URISyntaxException
	 */
	private static String buildSortedQueryString(ContainerRequestContext context) throws URISyntaxException {
		StringBuilder s = new StringBuilder();
		
		// Sort by adding all items to a TreeMap
		TreeMap<String, String> sortedMap = new TreeMap<String, String>();
		for (Entry<String, List<String>> param : context.getUriInfo().getQueryParameters().entrySet()) {
			String key = param.getKey();
			String value = param.getValue().iterator().next();
			sortedMap.put(encode(key), encode(value));
		}
		
		// Create name=value pairs.
		for (String key : sortedMap.keySet()) {
			s.append((s.length() > 0) ? "&" : "").append(key).append("=").append(sortedMap.get(key));
		}
		
		return s.toString();
	}

	/**
	 * Sorts the query string parameters, URL encodes them, and joins them in
	 * '&' delimited, '=' separated name/value pairs.
	 * @param request
	 * @return
	 * @throws URISyntaxException
	 */
	private static String buildSortedQueryString(ClientRequestContext context) throws URISyntaxException {
		StringBuilder s = new StringBuilder();
		
		// Sort by adding all items to a TreeMap
		if (context.getUri().getQuery() != null) {
			TreeMap<String, String> sortedMap = new TreeMap<String, String>();
			Map<String, String> queryParams = getQueryMap(context.getUri().getQuery());
			for (Entry<String, String> param : queryParams.entrySet()) {
				String key = param.getKey();
				String value = param.getValue();
				sortedMap.put(encode(key), encode(value));
			}
			
			// Create name=value pairs.
			for (String key : sortedMap.keySet()) {
				s.append((s.length() > 0) ? "&" : "").append(key).append("=").append(sortedMap.get(key));
			}
		}
		
		return s.toString();
	}	
	
	
	public static Map<String, String> getQueryMap(String query)  
	{  
		query = query.replace("+", " ");
		String[] params = query.split("&");  
	    Map<String, String> map = new HashMap<String, String>();  
	    for (String param : params)  
	    {
	    	String[] nameValue = param.split("=");
	    	if (nameValue.length == 2) {
		        String name = nameValue[0];  
		        String value = nameValue[1];
		        map.put(name, value);
	    	} else if (nameValue.length == 1) {
	    		map.put(nameValue[0], null);
	    	}
	    }  
	    return map;  
	}  

	/**
	 * URL encoding for a string
	 * @param string
	 * @return
	 * @throws URISyntaxException
	 */
	private static String encode(String string) throws URISyntaxException {
		URI uri = new URI(null, string, null);
		return uri.toASCIIString();
	}
	

	/**
	 * Computes RFC 2104-compliant HMAC signature.
	 * @param data
	 * The data to be signed.
	 * @param key
	 * The signing key.
	 * @return
	 * The Base64-encoded RFC 2104-compliant HMAC signature.
	 * @throws
	 * java.security.SignatureException when signature generation fails
	 */
	 public static String calculateRFC2104HMAC(String data, String key) throws java.security.SignatureException {
		String result;
		try {
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(data.getBytes());
			result = new String(Base64.encodeBase64(rawHmac));
	
		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
		return result;
	 }
 
}