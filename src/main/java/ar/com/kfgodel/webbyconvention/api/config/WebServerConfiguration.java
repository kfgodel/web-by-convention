package ar.com.kfgodel.webbyconvention.api.config;

import ar.com.kfgodel.nary.api.Nary;
import ar.com.kfgodel.webbyconvention.api.auth.WebCredential;
import ar.com.kfgodel.webbyconvention.impl.config.ConfigurationByConvention;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.Optional;
import java.util.Set;
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
   * Changes the default by convention port in which teh server, serves http content
   *
   * @param newHttpPort The new port to be used
   * @return This instance to chain methods
   */
  ConfigurationByConvention listeningHttpOn(int newHttpPort);

  /**
   * @return A code lambda that will set up the injection bindings for dependency injection
   * in the resources
   */
  Consumer<AbstractBinder> getInjectionConfigurator();


  /**
   * Changes the default by convention bindings used to inject dependencies in the resources.<br>
   * The given binder is the injection mechanism used by the server to manage dependencies
   *
   * @param binderConfig The code that defines how to define bindings
   * @return This instance to chain methods
   */
  ConfigurationByConvention withInjections(Consumer<AbstractBinder> binderConfig);

  /**
   * The function used to authenticate user credentials given to the web server.<br>
   * This function receives a credential object for every authentication attempt and returns
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
   * Changes the default authentication function that validates credential to a user identificator object
   *
   * @param authenticationFunction The function to use when authenticating users
   * @return This instance to chain methods
   */
  WebServerConfiguration authenticatingWith(Function<WebCredential, Optional<Object>> authenticationFunction);

  /**
   * Indicates that the server should not try to authenticate users (everything is anonymously accessible).
   * So an authentication function that accepts everything is used and no url is secured.<br>
   * This method is a shorthand version of defining an empty set of secured urls, and an always true
   * authenticator function
   *
   * @return Thi instance for method chaining
   */
  WebServerConfiguration withoutAuthentication();

  /**
   * @return The amount of seconds a session can remain inactive before invalidating it.<br>
   * After that user will have to re-login
   */
  int getSessionTimeout();

  /**
   * Changes the default by convention session expiration time
   *
   * @param seconds The new time to wait for sessions
   * @return This instance to chain methods
   */
  ConfigurationByConvention expiringSessionsAfter(int seconds);


  /**
   * @return The list of folders with resources that may change during development.
   * The server will serve the files directly allowing updates to be refreshed
   */
  Nary<String> getRefreshableWebFolders();

  /**
   * Changes the default by convention paths where static content is located while developing.<br>
   * The locations indicated in this method will be not cached by the server to always use the latests
   *
   * @param newContent The classpath relative location where static content is located on development
   * @return This instance to chain methods
   */
  ConfigurationByConvention withRefreshableWebFoldersIn(Nary<String> newContent);

  /**
   * @return The classpath folders that hold web content to be served
   */
  String getWebFolderInClasspath();

  /**
   * Changes the default by convention classpath folder where content is served from.<br>
   * This folder is used by the server to return static content to the http clients
   *
   * @param newFolder The folder that should be used
   * @return This instance to chain methods
   */
  ConfigurationByConvention usingClasspathWebFolder(String newFolder);

  /**
   * @return The package in the classpath that holds jersey annotated resources to expose as api
   */
  Nary<String> getApiResourcePackages();

  /**
   * Changes the default by convention packages where rest resources are defined.<br>
   * This packages will be explored to find classes annotated with @Path to declare http resources
   *
   * @param annotatedResourcesPackage The new package names
   * @return This instance to chain methods
   */
  ConfigurationByConvention withApiResourcesFrom(Nary<String> annotatedResourcesPackage);

  /**
   * @return The url root paths where api requests are served
   */
  Nary<String> getApiRootPaths();

  /**
   * Changes the default url location for api resources as published to the client.<br>
   *   This urls are where resources are published and where authentication may be needed (by default)
   *
   * @param parentPath The url paths where resources are located
   * @return This instance to chain methods
   */
  ConfigurationByConvention withApiRootPathsUnder(Nary<String> parentPath);

  /**
   * @return The url roots that will be restricted to authenticated users
   * Every url that start with any of this roots will need an authenticated user
   */
  Nary<String> geSecuredRootPaths();


  /**
   * Redefines the set of secured url paths that are going to be authenticated (to be accessed)
   * @param securedPaths The set of paths
   * @return This configuration
   */
  WebServerConfiguration withSecuredRootPaths(String... securedPaths);

  /**
   * @return The set of classes that will be used as http resources. If default is not changed,
   * api resource packages will be explored to look for classes annotated with {@link javax.ws.rs.Path}
   * recursively
   */
  Set<Class<?>> getApiResourceClasses();

  /**
   * Changes the default by convention resource classes that are discovered on runtime from
   * classes annotated with {@link javax.ws.rs.Path} from the api packages
   *
   * @param resourceClasses The set of classes to use as http resources
   * @return This instance to chain methods
   */
  WebServerConfiguration overridingResourceClassesWith(Nary<Class<?>> resourceClasses);

  /**
   * A path to redirect the client browser after authentication. This is useful when the authentication
   * is handled outside the app
   *
   * @return An optional path. If empty the server won't redirect (instead return a number)
   */
  Optional<String> getRedirectPath();

  /**
   * Changes the default behavior after authentication to make client broweser redirect to
   * indicated path after the authentication has been handled correctly.
   * This makes the server redirect a successful authentication request
   *
   * @param redirectPath Path to send the user to
   * @return This instance
   */
  WebServerConfiguration redirectingAfterAuthenticationTo(String redirectPath);

}
