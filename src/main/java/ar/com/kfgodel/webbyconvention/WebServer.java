package ar.com.kfgodel.webbyconvention;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This type represents the simplified by pre-definitions jetty server 
 * Created by kfgodel on 19/02/15.
 */
public class WebServer {
    public static Logger LOG = LoggerFactory.getLogger(WebServer.class);

    private Server jettyServer;
    private WebServerConfiguration config;
    
    private void initialize() {
        this.jettyServer = new Server(config.getHttpPort());

        List<Handler> requestHandlers = new ArrayList<>();

        // Make web content refreshable if changed
        serveDynamicContent(requestHandlers);

        // Serve as static content everything under classpath:/web/
        serveStaticContent(requestHandlers);

        // Publish Jersey resources as API
        serveApi(requestHandlers);
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(requestHandlers.toArray(new Handler[requestHandlers.size()]));
        jettyServer.setHandler(handlers);
    }

    /**
     * Serve an API using jersey resources from certain classpath location
     * @param requestHandlers The handlers to add the new one on
     */
    private void serveApi(List<Handler> requestHandlers) {

        Reflections reflections = new Reflections(config.getApiResourcesPackage());
        Set<Class<?>> annotatedResources = reflections.getTypesAnnotatedWith(Path.class);
        if(annotatedResources.isEmpty()){
            LOG.info("No resources annotated with " + Path.class + " found in["+config.getApiResourcesPackage()+"]");
            return;
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
        requestHandlers.add(jerseyHandler);
    }

    /**
     * Serve static content from the classpath (bundled with the app)
     * @param requestHandlers The list of handlers to add the new one
     */
    private void serveStaticContent(List<Handler> requestHandlers) {
        ResourceHandler classpathWebHandler = new ResourceHandler();
        classpathWebHandler.setBaseResource(Resource.newClassPathResource(config.getWebFolderInClasspath()));
        requestHandlers.add(classpathWebHandler);
    }

    /**
     * Make certain locations served from source folders to be updated in development (useful while developing)
     * @param requestHandlers The handler list
     */
    private void serveDynamicContent(List<Handler> requestHandlers) {
        List<String> refreshableSources = config.getRefreshableContent();
        for (String refreshableSource : refreshableSources) {
            if(new File(refreshableSource).exists()){
                // For development, we serve from the sources to allow runtime changes of static content
                ResourceHandler folderWebHandler = new ResourceHandler();
                folderWebHandler.setDirectoriesListed(true);
                folderWebHandler.setResourceBase(refreshableSource);
                requestHandlers.add(folderWebHandler);
            }
        }
    }

    public void startAndJoin() throws WebServerException {
        try {
            jettyServer.start();
            jettyServer.join();
        } catch (Exception e) {
            throw new WebServerException("Error starting web server", e);
        }
    }

    public static WebServer createFor(WebServerConfiguration config) {
        WebServer webServer = new WebServer();
        webServer.config = config;
        webServer.initialize();
        return webServer;
    }

}
