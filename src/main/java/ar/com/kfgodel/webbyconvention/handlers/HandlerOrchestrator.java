package ar.com.kfgodel.webbyconvention.handlers;

import ar.com.kfgodel.webbyconvention.WebServerConfiguration;
import ar.com.kfgodel.webbyconvention.auth.impl.Handlers;
import ar.com.kfgodel.webbyconvention.logging.RequestLoggerHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;

/**
 * This type knows how to create the http handler that will handle the requests according to the server configuration.<br>
 *   The orchestrator collaborates with the content handler, and a securizer handler to put sensitive content under an
 *   authentication mechanism
 * Created by kfgodel on 14/02/16.
 */
public class HandlerOrchestrator {
  private WebServerConfiguration config;

  public static HandlerOrchestrator create(WebServerConfiguration config) {
    HandlerOrchestrator orchestrator = new HandlerOrchestrator();
    orchestrator.config = config;
    return orchestrator;
  }

  public Handler createHandler() {
    Handler contentHandler = createContentHandler();
    Handler securized = securitize(contentHandler);
    HandlerList mainHandler = wrapAdditionalHandlers(securized);
    return mainHandler;
  }

  private HandlerList wrapAdditionalHandlers(Handler securized) {
    RequestLoggerHandler loggerHandler = RequestLoggerHandler.create();
    return Handlers.asList(loggerHandler, securized);
  }

  private Handler securitize(Handler contentHandler) {
    return ContentSecurizer.create(config).securize(contentHandler);
  }

  private Handler createContentHandler() {
    return ContentHandlerConfigurator.create(config).createHandler();
  }
}
