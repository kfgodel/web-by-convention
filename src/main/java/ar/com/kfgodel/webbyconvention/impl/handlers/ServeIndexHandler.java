package ar.com.kfgodel.webbyconvention.impl.handlers;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
    URL indexFileUrl = getClass().getResource("/convention/web/index.html");
    if (indexFileUrl == null) {
      LOG.error("No podemos acceder al index en el classpath. [{} {}]", request.getMethod(), request.getRequestURI());
      // Null represents absence for jetty (avoids NPE for favicon)
      return null;
    }
    Resource indexResource = Resource.newResource(indexFileUrl);
    if (indexResource == null || !indexResource.exists()) {
      LOG.debug("No encontramos index para responder el request [{} {}]: {} ", request.getMethod(), request.getRequestURI(), indexResource);
      return null;
    }
    LOG.debug("Usando index como respuesta a [{} {}]: {}", request.getMethod(), request.getRequestURI(), indexResource);
    return indexResource;
  }

  public static ServeIndexHandler create(String webFolderInClasspath) {
    ServeIndexHandler handler = new ServeIndexHandler();
    handler.setBaseResource(Resource.newClassPathResource(webFolderInClasspath));
    return handler;
  }

}
