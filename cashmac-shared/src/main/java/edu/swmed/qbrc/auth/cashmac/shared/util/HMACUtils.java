package edu.swmed.qbrc.auth.cashmac.shared.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SignatureException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
//import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.spi.HttpRequest;

public class HMACUtils {
 
	private final static String HMAC_SHA1_ALGORITHM = "HmacSHA1";
 
	/**
	 * Appends HMAC, date, and client id to a ClientRequest
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static void createSignatureAndSignRequest(ClientRequest request, String host, String clientId, String secret) throws Exception {
		// Get current date
		String currentDate = Long.toString(System.currentTimeMillis());

		// Get string to sign with HMAC
		String toSign = createSignature(request, currentDate, host);
		
		// Get HMAC
		String signature = calculateRFC2104HMAC(toSign, secret);
		
		// Add headers to request
		request.header("Signature", signature);
		request.header("ClientId", clientId);
		request.header("Date", currentDate);
	}
	
	/**
	 * Creates the signature to be passed to the HMAC function.
	 * @param request
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String createSignature(ClientRequest request, String date, String host) throws Exception {
		StringBuilder s = new StringBuilder();
		
		// HTTP Verb
		s.append(request.getHttpMethod()).append("\n");
		
		// Host header
		s.append(host.toLowerCase()).append("\n");
		
		// URI
		s.append(baseUriOnly(request.getUri())).append("\n");

		// Date
		s.append(date).append("\n");

		// Query String
		s.append(buildSortedQueryString(request));

		System.out.println("HMAC to Sign: " + s.toString());
		
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
	/*
	public static String createSignature(ContainerRequestContext request, String date) throws Exception {
		StringBuilder s = new StringBuilder();
		
		// HTTP Verb
		s.append(request.getMethod()).append("\n");
		
		// Host header
		String host = request.getHeaders().getFirst("HOST");
		if (host == null) {
			host = "/";
		}
		s.append(host.toLowerCase()).append("\n");
		
		// URI
		s.append(request.getUriInfo().getAbsolutePath().toASCIIString()).append("\n");

		// Date
		s.append(date).append("\n");

		// Query String
		s.append(buildSortedQueryString(request));

		System.out.println("HMAC to Sign: " + s.toString());

		// Return value
		return s.toString();
	}*/
	
	/**
	 * Creates the signature to be passed to the HMAC function.
	 * @param request
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String createSignature(HttpRequest request, String date) throws Exception {
		StringBuilder s = new StringBuilder();
		
		// HTTP Verb
		s.append(request.getHttpMethod()).append("\n");
		
		// Host header
		String host = request.getHttpHeaders().getRequestHeaders().getFirst("HOST");
		if (host == null) {
			host = "/";
		}
		s.append(host.toLowerCase()).append("\n");
		
		// URI
		s.append(request.getUri().getAbsolutePath().toASCIIString()).append("\n");

		// Date
		s.append(date).append("\n");

		// Query String
		s.append(buildSortedQueryString(request));

		System.out.println("HMAC to Sign: " + s.toString());

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
	private static String buildSortedQueryString(ClientRequest request) throws URISyntaxException {
		StringBuilder s = new StringBuilder();
		
		// Sort by adding all items to a TreeMap
		TreeMap<String, String> sortedMap = new TreeMap<String, String>();
		for (Entry<String, List<String>> param : request.getQueryParameters().entrySet()) {
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
	/*
	private static String buildSortedQueryString(ContainerRequestContext request) throws URISyntaxException {
		StringBuilder s = new StringBuilder();
		
		// Sort by adding all items to a TreeMap
		TreeMap<String, String> sortedMap = new TreeMap<String, String>();
		for (Entry<String, List<String>> param : request.getUriInfo().getQueryParameters().entrySet()) {
			String key = param.getKey();
			String value = param.getValue().iterator().next();
			sortedMap.put(encode(key), encode(value));
		}
		
		// Create name=value pairs.
		for (String key : sortedMap.keySet()) {
			s.append((s.length() > 0) ? "&" : "").append(key).append("=").append(sortedMap.get(key));
		}
		
		return s.toString();
	}	*/

	/**
	 * Sorts the query string parameters, URL encodes them, and joins them in
	 * '&' delimited, '=' separated name/value pairs.
	 * @param request
	 * @return
	 * @throws URISyntaxException
	 */
	private static String buildSortedQueryString(HttpRequest request) throws URISyntaxException {
		StringBuilder s = new StringBuilder();
		
		// Sort by adding all items to a TreeMap
		TreeMap<String, String> sortedMap = new TreeMap<String, String>();
		for (Entry<String, List<String>> param : request.getUri().getQueryParameters().entrySet()) {
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