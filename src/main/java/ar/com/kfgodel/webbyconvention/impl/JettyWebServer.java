package ar.com.kfgodel.webbyconvention.impl;

import ar.com.kfgodel.webbyconvention.api.WebServer;
import ar.com.kfgodel.webbyconvention.api.config.WebServerConfiguration;
import ar.com.kfgodel.webbyconvention.api.exceptions.WebServerException;
import ar.com.kfgodel.webbyconvention.impl.handlers.HandlerOrchestrator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

/**
 * This type represents the simplified by pre-definitions jetty server
 * Created by kfgodel on 19/02/15.
 */
public class JettyWebServer implements WebServer {

  private Server jettyServer;
  private WebServerConfiguration config;

  private void initialize() {
    this.jettyServer = new Server(config.getHttpPort());

    Handler requestHandler = HandlerOrchestrator.create(config).createHandler();
    jettyServer.setHandler(requestHandler);

    // Prevents file locking in windows
    jettyServer.setAttribute("useFileMappedBuffer", false);
  }


  @Override
  public void startAndJoin() throws WebServerException {
    try {
      jettyServer.start();
      jettyServer.join();
    } catch (Exception e) {
      throw new WebServerException("Error starting web server", e);
    }
  }

  @Override
  public void stop() throws WebServerException {
    try {
      jettyServer.stop();
    } catch (Exception e) {
      throw new WebServerException("Error stopping web server", e);
    }
  }

  public static JettyWebServer createFor(WebServerConfiguration config) {
    JettyWebServer webServer = new JettyWebServer();
    webServer.config = config;
    webServer.initialize();
    return webServer;
  }

}
