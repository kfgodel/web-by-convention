package ar.com.kfgodel.webbyconvention.logging;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This type represents the request handler that logs requests for debugging purposes
 * Created by kfgodel on 09/02/16.
 */
public class RequestLoggerHandler extends AbstractHandler {
  public static Logger LOG = LoggerFactory.getLogger(RequestLoggerHandler.class);

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    LOG.debug("{} {}", baseRequest.getMethod(), baseRequest.getUri());
  }

  public static RequestLoggerHandler create() {
    RequestLoggerHandler requestLoggerHandler = new RequestLoggerHandler();
    return requestLoggerHandler;
  }

}
