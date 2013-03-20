CasHmac Libraries
===============

CasHmac Goals
-------------
The CasHmac Java library provides both HMAC-based and CAS authentication for RESTful services. 

About CasHmac
-------------
CasHmac includes the following features:

1. CAS authentication for a RESTful Web service.  This is useful for when a user access your service directly by entering the URL in a Web browser, since HMAC is difficult to implement in this situation.
2. HMAC-based authentication for consumers of the RESTful Web service.  See the end of this document for the specifications for generating the HMAC expected by this library.
3. A Sample Application (https://github.com/QBRC/CasHmac-Sample) that includes a RESTful service (RESTEasy), along with sample console and web clients.
4. Simple role-based validation.
5. Integrated H2 database (in sample application) for quick testing.
6. Database settings (driver, url, user information, tables and key columns) configured in containing application's web.xml.
7. A client library that makes it easy to use HMAC-based authentication in Java apps that consume your RESTful service.

Getting Started
---------------

1. Clone this repository: ``git clone https://github.com/QBRC/CasHmac.git``
2. Compile the libraries with ``mvn clean install``


Sample Application
------------------
Our sample application provides the best way to become familiar with CasHmac.  In fact, it includes a fully-function RESTful Web service that implements JBoss's RESTEasy
You can use the sample application as a starting point for your next RESTful service. Please find it at https://github.com/QBRC/CasHmac-Sample
and follow the instructions in its README.md file.

Potential Gotchas
-----------------
- The CasHmac library was not properly determining the base URL (URL without the query string) on the client side.  This was resulting in an HMAC mismatch.  I fixed this by simply stripping the URI of any text after the first "?".  There may be a better way to do this.
- For now, because of the issues with form parameters and the client interceptor, I simply configured the client interceptor in the CasHmac client library to ignore any form parameters and only consider the query string parameters.  If you design your RESTful Web service to ignore any FORM parameters, this is fine.  However, if your RESTful Web service could be compromised by FORM parameters, then you'll need to modify CasHmac's shared HMACUtils class to include FORM parameters in the HMAC.
- CasHmac currently supports CAS authentication, but it doesn't consider roles yet. If the CAS server authenticates a user successfully, the user will be able to use any CasHmac-protected methods.  Role validation is only performed for clients authenticating with HMAC.

Detailed Configuration
======================

Server Configuration
--------------------

First, you'll want to configure your RESTful service to require authentication.  We assume that you're using JBoss's RESTEasy, but it should be relatively easy to modify CasHmac to work with other REST frameworks.

1. Include the cashmac-server and cashmac-shared JAR as dependencies for your REST Service project.  They're probably names something like ``cashmac-server-1.0.0-20130314.155633-55.jar`` and ``cashmac-shared-1.0.0-SNAPSHOT.jar``, and you can find them after you compile the CasHmac application.
2. Annotate any RESTful methods that you wish to secure with the `@Securable` annotation.  You can include a comma-delimited list of roles, if you wish.  In our sample application, you'll find these annotations in `edu.swmed.qbrc.resprirnate.shared.rest.MessageRestService.java`. Depending on your architecture, you may declare your RESTful methods in a shared library (like we do), or in your server application.  Here are two examples:

      ```java        
      @Securable("")  // This method is secured, but not validated by any roles.
      @GET
      @Path("/user/{param}")
      @Produces("application/json")
      public User get(@PathParam("param") String id);
      
      @Securable("admin,manager") // This method is secured and limited to users in the "admin" or "manager" roles.
      @GET
      @Path("/{param}")
      public Response printMessage(@PathParam("param") String msg);
      ```
      
3. To tie the HMAC-based authentication mechanism into your JDBC-compliant database, modify your RESTful service's web.xml as follows.  The values provided in our sample application (/src/main/webapp/WEB-INF/web.xml) are the defaults, so you only need to include parameters that vary from the defaults.

      ```xml
      <!-- Configure the interceptor for authentication -->
      <!-- Interceptor seems to work fine without this. When defined here, we end up with two interceptors.
           Don't include this unless you need it for some reason.
      <context-param>
        <param-name>resteasy.providers</param-name>
      	<param-value>edu.swmed.qbrc.auth.cashmac.server.ValidationInterceptorCasHmac</param-value>
      </context-param>
      -->
      
      <!-- The URL where users go to log in to your CAS server -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.cas.serverLoginUrl</param-name>
      	<param-value>https://cas.jon.swmed.edu:8443/cas-server-webapp-3.5.1/login</param-value>
      </context-param>
      <!-- The base URL of your RESTful service.  This URL is used by your CAS server to
           identify your application. -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.cas.serviceName</param-name>
      	<param-value>http://127.0.0.1:9090</param-value>
      </context-param>
      
      <!-- Context parameters for HMAC server -->
      <!-- The name of the table that stores Role information -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.hmac.table.Role</param-name>
      	<param-value>roles</param-value>
      </context-param>
      <!-- The name of the column that uniquely identifies the role in the Roles table -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.hmac.table.keycol.Role</param-name>
      	<param-value>id</param-value>
      </context-param>	
      <!-- The name of the column that identifies the user in the Roles table -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.hmac.table.usercol.Role</param-name>
      	<param-value>username</param-value>
      </context-param>
      <!-- The name of the column that names the role in the Roles table
           NOTE: There can be multiple rows in the Roles table for each user-->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.hmac.table.rolecol.Role</param-name>
      	<param-value>role</param-value>
      </context-param>
      <!-- The name of the table that stores user information -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.hmac.table.User</param-name>
      	<param-value>users</param-value>
      </context-param>
      <!-- The name of the column that uniquely identifies each user in the Users table -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.hmac.table.keycol.User</param-name>
      	<param-value>id</param-value>
      </context-param>
      <!-- The name of the column that contains the secret HMAC key for the user in the Users table -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.hmac.table.secretCol.User</param-name>
      	<param-value>secret</param-value>
      </context-param>
      <!-- The class of the JDBC driver to use -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.hmac.jdbcdriver</param-name>
      	<param-value>org.h2.Driver</param-value>
      </context-param>
      <!-- The URL for the JDBC driver to use to connect to your database. -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.hmac.jdbcurl</param-name>
      	<param-value>jdbc:h2:~/cashmacSampleServerDB</param-value>
      </context-param>
      <!-- The user name to use to access your database. -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.hmac.username</param-name>
      	<param-value>sa</param-value>
      </context-param>
      <!-- The password to use to access your database. -->
      <context-param>
      	<param-name>edu.swmed.qbrc.auth.cashmac.hmac.password</param-name>
      	<param-value></param-value>
      </context-param>
      ```

4. Restart your RESTful Web service (i.e., restart Tomcat).
5. If you attempt to access a secured method your RESTful service in a browser (by typing a direct URL), you should be redirected to your CAS server.  If you don't have a CAS server configured, you'll get a 404 error at this point, as the browser will be redirected to a location that doesn't exist.

Client Configuration
---------------------
Secure your clients (consumers of the RESTful service) with the CasHmac-client library as follows.

1. Include the cashmac-client and cashmac-shared JAR as dependencies for your REST Service project. 
2. Create a static function that returns a ClientRequestFactory, from which you can grab a reference to your REST client.  The "client" subproject in the CasHmac-Sample project shows you how to do this for a simple console application.  The "web-client" subproject shows you a more elegant implementation with Guice for dependency injection.

      ```java
      private static ClientRequestFactory initializeRequests() {
    		ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
    		RegisterBuiltin.register(instance);
    		instance.registerProvider(MessageRestService.class);
    	 
    		ClientRequestFactory clientRequestFactory = new ClientRequestFactory();
    		ClientAuthInterceptor interceptor = new ClientAuthInterceptor();
    		interceptor.setClientId("thomas");
    		interceptor.setSecret("123456789");
    		interceptor.setHostName("127.0.0.1:9090");
    		clientRequestFactory.getPrefixInterceptors().registerInterceptor(interceptor);
    		return clientRequestFactory;
    	}
      ```

3. Grab your REST client object from the ClientRequestFactory, and issue a REST request:

      ```java
      ClientRequestFactory client = initializeRequests();
      MessageRestService messageRestService = client.createProxy(MessageRestService.class, "http://127.0.0.1:9090/rest");
      
      StringBuilder out = new StringBuilder();
      
      // REST Request 1
      @SuppressWarnings("unchecked")
      ClientResponse<String> anonResponse = (ClientResponse<String>)messageRestService.printMessageAnon("Anonymous Message");
      out.append(anonResponse.getEntity(String.class)).append("\n");
      
      // REST Request 2
      @SuppressWarnings("unchecked")
      ClientResponse<String> authResponse = (ClientResponse<String>)messageRestService.printMessage("Authenticated Message");
      out.append(authResponse.getEntity(String.class)).append("\n");
      
      // REST Request 3
      User user = messageRestService.get("thomas");
    	out.append(user.toString()).append("\n");
      ```

Client Notes
-------------

- The RESTEasy library's default ClientExecutor, which is used by the Client Interceptor, was not working with a GWT application.  I think that GWT must append form parameters to all requests automatically.  When the ClientExecutor code sees the form parameters, it assumes that it's dealing with a POST request, and attempts to cast the request to an HttpPost request, which fails since it's actually a GET request.  I created a custom ClientExecutor that extends the implementation used by RESTEasy, and configured it to simply assume that we're dealing with a GET request.  I added the extended ClientExecutor (named GWTClientExecutor) to the CasHmac client library--it's up to the developer using the library whether to go with the custom GWTClientExecutor or the default one provided by RESTEasy.  The CasHmac-Sample's "web-client" sample client application provides an example of using GWTClientExecutor.

- Because the default client connection manager used by RESTEasy is not thread safe, and since some applications may fire off several REST requests, I started having issues with secondary REST requests failing due to a previous connection that was still open.  Based on the JBoss documentation, I figured out how to set up a thread-safe client connection manager, which is also used in the "web-client" sample of the CasHmac-Sample project.


HMAC Specifications
====================
If you wish to access a RESTful service protected by CasHmac from a client that doesn't support Java, or if you simply wish to write your own client-side implementation of HMAC, here is the specification you'll need:

Request Headers
---------------

- Include the current date in milliseconds (UTC) in a request header named "Date". We get this value as follows: ``String currentDate = Long.toString(System.currentTimeMillis());`` Be sure that your client and server times are reasonably in sync. By default, cashmac-server enforces a maximum difference of 5 minutes between the client and server.
- Include your client ID (user name) in a request header named "ClientId".  Be sure NOT to include your client secret!!
- Include an HMAC in a request header named "Signature".  See below for information on generating the HMAC.

HMAC Signature
--------------

- Start the string with the http method (probably GET), followed by a new line ("\n").
- Append the host header in lower case, followed by a new line.
- Append the URL-encoded request URI, followed by a new line (the URL minus any query string parameters).  If it's blank, use "/".  Do not URL encode any of the unreserved characters that RFC 3986 defines (reference: http://docs.aws.amazon.com/AmazonSimpleDB/latest/DeveloperGuide/HMACAuth.html)
- Append the date in milliseconds (UTC), followed by a new line.  This date must exactly match the one included in the "Date" request header.
- Append a sorted list of query string name=value pairs, each pair separated with "&".
- Encode the entire string with the HMAC-SHA1 algorithm.
- Convert the string to Base 64.

HMAC Signature Creation Example
-------------------------------

Below is the Java code (taken from cashmac-client) used to add the necessary HTTP headers, along with creating a valid HMAC, to an HTTP Request to a RESTful service protected by CasHmac.

```java
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

	// Return value
	return s.toString();
}

/**
 * Sorts the query string and form parameters, URL encodes them, and joins them in
 * '&' delimited, '=' separated name/value pairs.
 * @param request
 * @return
 * @throws URISyntaxException
 */
private static String buildSortedQueryString(HttpRequest request) throws URISyntaxException {
	StringBuilder s = new StringBuilder();
	
	// Get a single map of all form and query string parameters
	MultivaluedMap<String, String> combinedMap = request.getFormParameters();
	combinedMap.putAll(request.getUri().getQueryParameters());
	
	// Sort by adding all items to a TreeMap
	TreeMap<String, String> sortedMap = new TreeMap<String, String>();
	Iterator<String> it = combinedMap.keySet().iterator();
	while (it.hasNext()) { // Loop through keys
		String key = it.next();
		sortedMap.put(encode(key), encode(combinedMap.getFirst(key)));
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
```
      

