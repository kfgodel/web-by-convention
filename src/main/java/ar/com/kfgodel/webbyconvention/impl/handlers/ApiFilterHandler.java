package ar.com.kfgodel.webbyconvention.impl.handlers;

import ar.com.kfgodel.webbyconvention.api.config.WebServerConfiguration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This type represents the handler that filters requests for api ahndler, only if they conform to the expected url.<br>
 *   Any other request is ignores thus preventing a 404 for anything that is not an api resource
 * Created by kfgodel on 15/02/16.
 */
public class ApiFilterHandler extends AbstractHandler {

  private Handler apiHandler;
  private WebServerConfiguration config;

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    if (isNotAnApiRequest(target)) {
      // Ignore any non api request
      return;
    }
    apiHandler.handle(target,baseRequest,request,response);
  }

  private boolean isNotAnApiRequest(String target) {
    return config.getApiRootPaths()
      .noneMatch(target::startsWith);
  }

  public static ApiFilterHandler create(Handler apiHandler, WebServerConfiguration config) {
    ApiFilterHandler filterHandler = new ApiFilterHandler();
    filterHandler.apiHandler = apiHandler;
    filterHandler.config = config;
    return filterHandler;
  }

}
