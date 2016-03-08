package ar.com.kfgodel.webbyconvention.impl.config;

import ar.com.kfgodel.convention.api.Convention;
import ar.com.kfgodel.nary.api.Nary;
import ar.com.kfgodel.webbyconvention.api.auth.WebCredential;
import ar.com.kfgodel.webbyconvention.api.config.WebServerConfiguration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This type represents the defaul configuration with sensitive values for all the parameters.
 * You will usually use this as is
 * Created by kfgodel on 03/03/15.
 */
public class ConfigurationByConvention implements WebServerConfiguration {

  private int httpPort;
  private List<String> refreshableWebFolders;
  private String webFolderInClassPath;
  private List<String> apiResourcesPackage;
  private Consumer<AbstractBinder> injectionConfiguration;
  private Function<WebCredential, Optional<Object>> authenticatorFunction;
  private int sessionTimeout;
  private List<String> apiRootUrl;

  private Optional<Object> authenticateAll(WebCredential webCredential) {
    // We allow access to every login attempt
    return Optional.of(true);
  }

  private void noBinding(AbstractBinder abstractBinder) {
    // By default there are no bindings
  }

  @Override
  public int getHttpPort() {
    return httpPort;
  }

  public Nary<String> getRefreshableWebFolders() {
    return Nary.create(refreshableWebFolders);
  }

  @Override
  public String getWebFolderInClasspath() {
    return webFolderInClassPath;
  }

  @Override
  public Nary<String> getApiResourcesPackage() {
    return Nary.create(apiResourcesPackage);
  }

  @Override
  public Consumer<AbstractBinder> getInjectionConfigurator() {
    return injectionConfiguration;
  }

  @Override
  public Function<WebCredential, Optional<Object>> getAuthenticatorFunction() {
    return authenticatorFunction;
  }

  @Override
  public int getSessionTimeout() {
    return sessionTimeout;
  }

  @Override
  public Nary<String> getApiRootPath() {
    return Nary.create(apiRootUrl);
  }

  public static ConfigurationByConvention create() {
    ConfigurationByConvention config = new ConfigurationByConvention();
    config.initializeDefaults();
    return config;
  }

  private void initializeDefaults() {
    Convention convention = Convention.create();
    this.httpPort = convention.getHttpPort();
    this.apiRootUrl = convention.getRestApiRootUrl();
    this.apiResourcesPackage = convention.getRestApiRootPackageName();
    this.webFolderInClassPath = convention.getWebFolderInClasspath();
    this.refreshableWebFolders = convention.getWebFoldersInSources();
    this.injectionConfiguration = this::noBinding;
    this.authenticatorFunction = this::authenticateAll;
    this.sessionTimeout = (int) TimeUnit.MINUTES.toSeconds(30);
  }

  /**
   * Changes the default port
   *
   * @param newHttpPort New port number
   * @return Ths config to chain calls
   */
  public ConfigurationByConvention listeningHttpOn(int newHttpPort) {
    this.httpPort = newHttpPort;
    return this;
  }

  /**
   * Changes the default refreshable folders
   *
   * @param newContent The list of folders to look for changes
   * @return This instance for call chaining
   */
  public ConfigurationByConvention withRefreshableContentIn(Nary<String> newContent) {
    this.refreshableWebFolders = newContent.collect(Collectors.toList());
    return this;
  }

  /**
   * Changes the default folder in the classpath that holds all the static web content
   *
   * @param newFolder The new location to look into the classpath
   * @return This instance to chain method calls
   */
  public ConfigurationByConvention usingClasspathWebFolder(String newFolder) {
    this.webFolderInClassPath = newFolder;
    return this;
  }

  /**
   * Changes the default location in the classpath were annotated resource class are
   *
   * @param annotatedResourcesPackage The package were jersey api resource classes are
   * @return This instance to chain calls
   */
  public ConfigurationByConvention withResourcesFrom(Nary<String> annotatedResourcesPackage) {
    this.apiResourcesPackage = annotatedResourcesPackage.collect(Collectors.toList());
    return this;
  }

  /**
   * Changes the default binding of injected instances for resources
   *
   * @param binderConfig The binder code to configure the binder instance
   * @return This instance for method chaining
   */
  public ConfigurationByConvention withInjections(Consumer<AbstractBinder> binderConfig) {
    this.injectionConfiguration = binderConfig;
    return this;
  }

  /**
   * Sets the max interval in seconds between requests to keep a session alive.
   * After the session dies, the user will have to login again for access to authenticated resources
   *
   * @param seconds The amount of seconds to wait for next request
   * @return This instance for method chaining
   */
  public ConfigurationByConvention expiringSessionsAfter(int seconds) {
    this.sessionTimeout = seconds;
    return this;
  }

  /**
   * Sets which part of the route is dedicated to api requests
   *
   * @param parentPath The parent url
   * @return This instance for chaining methods
   */
  public ConfigurationByConvention withApiUnder(Nary<String> parentPath) {
    this.apiRootUrl = parentPath.collect(Collectors.toList());
    return this;
  }

  /**
   * Changes the default "allow all" authentication to a custom app authenticator function.<br>
   * The function will be called for every authentication attempt, and the non empty returned object
   * will be used as the user id, available in a thread context through the WebAuthenticatedId class
   *
   * @param authenticationFunction The authentication function
   * @return This isntance to allow method chaining
   */
  public WebServerConfiguration authenticatingWith(Function<WebCredential, Optional<Object>> authenticationFunction) {
    this.authenticatorFunction = authenticationFunction;
    return this;
  }
}

