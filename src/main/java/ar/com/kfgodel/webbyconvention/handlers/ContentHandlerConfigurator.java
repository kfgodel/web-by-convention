package ar.com.kfgodel.webbyconvention.handlers;

import ar.com.kfgodel.webbyconvention.ConfigurableInjectionBinder;
import ar.com.kfgodel.webbyconvention.WebServerConfiguration;
import ar.com.kfgodel.webbyconvention.auth.impl.Handlers;
import ar.com.kfgodel.webbyconvention.bugs.NonLockingResourceHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.jetty.JettyHttpContainer;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.io.File;
import java.util.*;
import java.util.stream.Stream;

/**
 * This type represents the configurator instance to create handlers for web content
 * Created by kfgodel on 15/02/16.
 */
public class ContentHandlerConfigurator {
  public static Logger LOG = LoggerFactory.getLogger(ContentHandlerConfigurator.class);

  private WebServerConfiguration config;

  public static ContentHandlerConfigurator create(WebServerConfiguration config) {
    ContentHandlerConfigurator configurator = new ContentHandlerConfigurator();
    configurator.config = config;
    return configurator;
  }

  /**
   * Creates the list of handlers that need to be secured under an authorized login
   * @return The handler list as a set of handlers
   */
  public Handler createHandler() {
    List<Handler> partialList = new ArrayList<>();

    // Publish Jersey resources as API
    Optional<Handler> apiHandler = createApiHandler();
    apiHandler.ifPresent(partialList::add);

    // Make web content refreshable if changed
    Stream<Handler> dynamicHandlers = createDynamicContentHandlers();
    dynamicHandlers.forEach(partialList::add);

    // Serve as static content everything under classpath:/web/
    Handler staticHandler = createStaticContentHandler();
    partialList.add(staticHandler);

    // If no other handler can respond, answer the index page. The frontend should take care of wrong urls
    Handler indexHandler = createAnyToIndexHandler();
    partialList.add(indexHandler);

    return Handlers.asList(partialList);
  }

  /**
   * Creates the handler that serves the index page for any url (only for GET or POST)
   * @return The created handler
   */
  private Handler createAnyToIndexHandler() {
    return ServeIndexHandler.create(config.getWebFolderInClasspath());
  }

  /**
   * Creates a request handler for the json api requests
   */
  private Optional<Handler> createApiHandler() {

    Reflections reflections = new Reflections(config.getApiResourcesPackage());
    Set<Class<?>> annotatedResources = reflections.getTypesAnnotatedWith(Path.class);
    if (annotatedResources.isEmpty()) {
      LOG.info("No resources annotated with " + Path.class + " found in[" + config.getApiResourcesPackage() + "]");
      return Optional.empty();
    }

    ResourceConfig jerseyConfig = new ResourceConfig(annotatedResources);
    //Configure dependency injection for resources
    jerseyConfig.register(ConfigurableInjectionBinder.create(this.config.getInjectionConfiguration()));
    // Activate tracing log on requests
//        headers: {
//            "X-Jersey-Tracing-Accept": 'true', // Any value is ok
//                    "X-Jersey-Tracing-Threshold": 'TRACE'
//        }

    HashMap<String, Object> properties = new HashMap<>();
    properties.put("jersey.config.server.tracing.type", "ON_DEMAND");
    properties.put("jersey.config.server.tracing.threshold", "SUMMARY");
    jerseyConfig.addProperties(properties);

    final JettyHttpContainer jerseyHandler = ContainerFactory.createContainer(JettyHttpContainer.class, jerseyConfig);
    ApiFilterHandler filterHandler = ApiFilterHandler.create(jerseyHandler, config);
    return Optional.of(filterHandler);
  }

  /**
   * Serve static content from the classpath (bundled with the app)
   */
  private Handler createStaticContentHandler() {
    String webFolderInClasspath = config.getWebFolderInClasspath();
    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setBaseResource(Resource.newClassPathResource(webFolderInClasspath));
    return resourceHandler;
  }

  /**
   * Make certain locations served from source folders to be updated in development (useful while developing)
   */
  private Stream<Handler> createDynamicContentHandlers() {
    List<String> refreshableSources = config.getRefreshableContent();
    return refreshableSources.stream()
      .filter((refreshableSource)-> new File(refreshableSource).exists())
      .map(this::createDynamicContentHandler);
  }

  /**
   * Create the resource handler that we will use to serve static content that could be changed
   * while we are running. Due to the way Jetty opens files to buffer them, in windows that
   * generates a locked file, that cannot be changed once served. To prevent that we try to
   * detect the OS and use a non standard handler that doesn't lock files.
   *
   * @param refreshableSource
   * @return
   */
  private Handler createDynamicContentHandler(String refreshableSource) {
    String currentOsName = System.getProperty("os.name");
    if (currentOsName.startsWith("Windows")) {
      NonLockingResourceHandler nonLockingResourceHandler = new NonLockingResourceHandler();
      nonLockingResourceHandler.setResourceBase(refreshableSource);
      return nonLockingResourceHandler;
    }
    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setResourceBase(refreshableSource);
    return resourceHandler;
  }

}
