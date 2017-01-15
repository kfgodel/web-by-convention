package ar.com.kfgodel.webbyconvention.impl.config;

import ar.com.kfgodel.convention.api.Convention;
import ar.com.kfgodel.nary.api.Nary;
import ar.com.kfgodel.webbyconvention.api.auth.WebCredential;
import ar.com.kfgodel.webbyconvention.api.config.WebServerConfiguration;
import ar.com.kfgodel.webbyconvention.api.exceptions.WebServerException;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.reflections.Reflections;

import javax.ws.rs.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * This type represents the default configuration with sensitive values for all the parameters.
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
  private Set<String> apiRootPaths;
  private Set<String> securedRoots;
  private Set<Class<?>> apiResourceClasses;
  private Optional<String> redirectPath;


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
  public Nary<String> getApiResourcePackages() {
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
  public Nary<String> getApiRootPaths() {
    if (apiRootPaths == null) {
      apiRootPaths = discoverApiRootPaths();
    }
    return Nary.create(apiRootPaths);
  }

  /**
   * Finds the used api roots for the existing resource classes
   *
   * @return The different paths used for resources
   */
  private Set<String> discoverApiRootPaths() {
    return this.getApiResourceClasses().stream()
      .map(this::getPathFrom)
      .collect(toSet());
  }

  private String getPathFrom(Class<?> resourceClass) {
    Path annotation = resourceClass.getAnnotation(Path.class);
    if (annotation == null) {
      throw new WebServerException("A resource class doesn't have a @Path annotation, even after checking: " + resourceClass);
    }
    return annotation.value();
  }


  @Override
  public Nary<String> geSecuredRootPaths() {
    if (securedRoots == null) {
      // Use the api urls by defautl
      return getApiRootPaths();
    }
    return Nary.create(securedRoots);
  }

  @Override
  public WebServerConfiguration withoutAuthentication() {
    this.authenticatorFunction = this::authenticateAll;
    this.securedRoots = Collections.emptySet();
    return this;
  }

  public static ConfigurationByConvention create() {
    ConfigurationByConvention config = new ConfigurationByConvention();
    config.initializeDefaults();
    return config;
  }

  private void initializeDefaults() {
    Convention convention = Convention.create();
    this.httpPort = convention.getHttpPort();
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
  @Override
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
  @Override
  public ConfigurationByConvention withRefreshableWebFoldersIn(Nary<String> newContent) {
    this.refreshableWebFolders = newContent.toList();
    return this;
  }

  /**
   * Changes the default folder in the classpath that holds all the static web content
   *
   * @param newFolder The new location to look into the classpath
   * @return This instance to chain method calls
   */
  @Override
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
  @Override
  public ConfigurationByConvention withApiResourcesFrom(Nary<String> annotatedResourcesPackage) {
    this.apiResourcesPackage = annotatedResourcesPackage.toList();
    return this;
  }

  /**
   * Changes the default binding of injected instances for resources
   *
   * @param binderConfig The binder code to configure the binder instance
   * @return This instance for method chaining
   */
  @Override
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
  @Override
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
  @Override
  public ConfigurationByConvention withApiRootPathsUnder(Nary<String> parentPath) {
    this.apiRootPaths = parentPath.toSet();
    return this;
  }

  /**
   * Changes the default "allow all" authentication to a custom app authenticator function.<br>
   * The function will be called for every authentication attempt, and the non empty returned object
   * will be used as the user id, available in a thread context through the WebAuthenticatedId class
   *
   * @param authenticationFunction The authentication function
   * @return This instance to allow method chaining
   */
  @Override
  public WebServerConfiguration authenticatingWith(Function<WebCredential, Optional<Object>> authenticationFunction) {
    this.authenticatorFunction = authenticationFunction;
    return this;
  }

  @Override
  public Set<Class<?>> getApiResourceClasses() {
    if (apiResourceClasses == null) {
      // Was not user defined use default
      apiResourceClasses = discoverResourceClasses();
    }
    return apiResourceClasses;
  }

  @Override
  public WebServerConfiguration withSecuredRootPaths(String... securedPaths) {
    this.securedRoots = Arrays.stream(securedPaths)
      .collect(toSet());
    return this;
  }

  /**
   * Explores the api packages for classes usable as resources
   *
   * @return The set of resource classes
   */
  private Set<Class<?>> discoverResourceClasses() {
    Nary<String> resourcePackages = this.getApiResourcePackages();
    return resourcePackages
      .flatMap(this::getAnnotatedResourcesIn)
      .collect(toSet());
  }

  /**
   * Explores the types inside the given package to discover @Path annotated types
   *
   * @param resourcePackage The root package to explore
   * @return The stream of annotated types inside the package or subpackages
   */
  private Stream<? extends Class<?>> getAnnotatedResourcesIn(String resourcePackage) {
    Reflections reflections = new Reflections(resourcePackage);
    Set<Class<?>> typesAnnotatedWithPath = reflections.getTypesAnnotatedWith(Path.class);
    return typesAnnotatedWithPath.stream();
  }

  @Override
  public WebServerConfiguration overridingResourceClassesWith(Nary<Class<?>> resourceClasses) {
    this.apiResourceClasses = resourceClasses.toSet();
    return this;
  }

  @Override
  public Optional<String> getRedirectPath() {
    return redirectPath;
  }

  @Override
  public WebServerConfiguration redirectingAfterAuthenticationTo(String redirectPath) {
    this.redirectPath = Optional.of(redirectPath);
    return this;
  }
}

