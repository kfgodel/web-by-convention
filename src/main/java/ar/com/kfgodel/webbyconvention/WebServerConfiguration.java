package ar.com.kfgodel.webbyconvention;

import ar.com.kfgodel.webbyconvention.auth.api.WebCredential;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This type represents the configuration used by a web-by-convention server to setup
 * its behavior
 * <p/>
 * Created by kfgodel on 03/03/15.
 */
public interface WebServerConfiguration {
  /**
   * @return The port to start listening for incoming http connections
   */
  int getHttpPort();

  /**
   * @return The list of folders with resources that may change during development.
   * The server will serve the files directly allowing updates to be refreshed
   */
  List<String> getRefreshableContent();

  /**
   * @return The classpath folder that holds all the web content
   */
  String getWebFolderInClasspath();

  /**
   * @return The package in the classpath that holds jersey annotated resources to expose as api
   */
  String getApiResourcesPackage();

  /**
   * @return The url segment that serves as root for all the api requests
   */
  String getApiRootPath();


  /**
   * @return The code to setup injection bindings for the jersey resources
   */
  Consumer<AbstractBinder> getInjectionConfiguration();

  /**
   * The function used to authenticate user credentials given to the web server.<br>
   * The function receive a credential object for every authentication attempt and returns
   * an empty optional for failed authentication, or an object for successful ones.<br>
   * <br>
   * The returned object can be used later to identify the authenticated user, and it's available
   * in a thread context from requests that comes from the same session.<br>
   * To access the generated object WebAuthenticatedId can be used
   *
   * @return An authenticator function
   */
  Function<WebCredential, Optional<Object>> getAuthenticatorFunction();

  /**
   * @return The amount of seconds a session can remain inactive before invalidating it.<br>
   * After that user will have to re-login
   */
  int getSessionTimeout();

}
