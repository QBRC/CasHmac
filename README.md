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
