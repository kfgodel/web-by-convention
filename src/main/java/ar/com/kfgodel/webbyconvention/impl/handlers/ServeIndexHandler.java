package ar.com.kfgodel.webbyconvention.impl.handlers;

import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * This type represents the handler that serves the index page for any url, as
 * the las resource handler. Delegating any wrong url handling to the frontend
 *
 * Created by kfgodel on 15/02/16.
 */
public class ServeIndexHandler extends ResourceHandler {
  public static Logger LOG = LoggerFactory.getLogger(ServeIndexHandler.class);

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    super.handle(target, baseRequest, request, response);
  }

  @Override
  protected Resource getResource(HttpServletRequest request) throws MalformedURLException {
    try{
      String pathInContext= URIUtil.addPaths("","/");
      Resource rootUrl = getResource(pathInContext);
      if(rootUrl == null){
        // Null represents absence for jetty (avoids NPE for favicon)
        LOG.debug("No tenemos recurso para responder el request [{} {}] ", request.getMethod(), request.getRequestURI());
        return null;
      }
      Resource indexResource = getWelcome(rootUrl);
      LOG.debug("Usando index como respuesta a [{} {}]: {}", request.getMethod(), request.getRequestURI(), indexResource);
      return indexResource;
    }catch (IOException e){
      throw new RuntimeIOException("Error accessing index page?",e);
    }
  }

  public static ServeIndexHandler create(String webFolderInClasspath) {
    ServeIndexHandler handler = new ServeIndexHandler();
    handler.setBaseResource(Resource.newClassPathResource(webFolderInClasspath));
    return handler;
  }

}
