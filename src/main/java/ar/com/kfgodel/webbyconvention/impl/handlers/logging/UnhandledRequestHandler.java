package ar.com.kfgodel.webbyconvention.impl.handlers.logging;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * THis type represents the handler that takes action after no other handler handled the request.<br>
 * At least it logs the situation
 * Created by kfgodel on 14/01/17.
 */
public class UnhandledRequestHandler extends AbstractHandler {
  public static Logger LOG = LoggerFactory.getLogger(UnhandledRequestHandler.class);

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    if (!baseRequest.isHandled()) {
      LOG.error("Unhandled request [{} {}]", baseRequest.getMethod(), baseRequest.getUri());
    }
  }

  public static UnhandledRequestHandler create() {
    UnhandledRequestHandler handler = new UnhandledRequestHandler();
    return handler;
  }

}
