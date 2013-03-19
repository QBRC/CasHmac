package edu.swmed.qbrc.auth.cashmac.client;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;


public class GWTClientExecutor extends ApacheHttpClient4Executor {

  @Override
  public void loadHttpMethod(final ClientRequest request, HttpRequestBase httpMethod) throws Exception {
      if (httpMethod instanceof HttpGet && request.followRedirects()) {
         HttpClientParams.setRedirecting(httpMethod.getParams(), true);
      } else {
         HttpClientParams.setRedirecting(httpMethod.getParams(), false);
      }

      if (request.getBody() != null && !request.getFormParameters().isEmpty())
         throw new RuntimeException("You cannot send both form parameters and an entity body");

      commitHeaders(request, httpMethod);
   }
	  
}