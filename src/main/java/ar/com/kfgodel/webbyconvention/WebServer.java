package ar.com.kfgodel.webbyconvention;

import ar.com.kfgodel.webbyconvention.handlers.HandlerOrchestrator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

/**
 * This type represents the simplified by pre-definitions jetty server
 * Created by kfgodel on 19/02/15.
 */
public class WebServer {

  private Server jettyServer;
  private WebServerConfiguration config;

  private void initialize() {
    this.jettyServer = new Server(config.getHttpPort());

    Handler requestHandler = HandlerOrchestrator.create(config).createHandler();
    jettyServer.setHandler(requestHandler);

    // Prevents file locking in windows
    jettyServer.setAttribute("useFileMappedBuffer", false);
  }


  public void startAndJoin() throws WebServerException {
    try {
      jettyServer.start();
      jettyServer.join();
    } catch (Exception e) {
      throw new WebServerException("Error starting web server", e);
    }
  }

  public void stop() throws WebServerException {
    try {
      jettyServer.stop();
    } catch (Exception e) {
      throw new WebServerException("Error stopping web server", e);
    }
  }

  public static WebServer createFor(WebServerConfiguration config) {
    WebServer webServer = new WebServer();
    webServer.config = config;
    webServer.initialize();
    return webServer;
  }

}
