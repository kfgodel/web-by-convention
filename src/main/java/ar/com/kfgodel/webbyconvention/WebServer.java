package ar.com.kfgodel.webbyconvention;

/**
 * This type represents an integrated web server
 *
 * Created by kfgodel on 08/03/16.
 */
public interface WebServer {
  /**
   * Starts the server and makes the current thread wait for
   * the server thread to finish. This method seems to hang due to the
   * current thread joining the server thread
   * @throws WebServerException If a configuration or start error happens
   */
  void startAndJoin() throws WebServerException;

  /**
   * Stops the server and returns the original thread to its conclusion
   * @throws WebServerException If an error happens trying to stop
   */
  void stop() throws WebServerException;
}
