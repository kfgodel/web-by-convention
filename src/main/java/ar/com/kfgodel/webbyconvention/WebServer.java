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

import javax.ws.rs.Path;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This type represents the simplified by pre-definitions jetty server 
 * Created by kfgodel on 19/02/15.
 */
public class WebServer {
    
    private Server jettyServer;
    
    private void initialize(Optional<Integer> predefinedPort) {
        this.jettyServer = new Server(predefinedPort.orElse(80));

        List<Handler> requestHandlers = new ArrayList<>();

        if(new File("src/main/resources/web").exists()){
            // For development, we serve from the sources to allow runtime changes of static content
            ResourceHandler folderWebHandler = new ResourceHandler();
            folderWebHandler.setDirectoriesListed(true);
            folderWebHandler.setResourceBase("src/main/resources/web");
            requestHandlers.add(folderWebHandler);
        }

        // Serve as static content everything under classpath:/web/
        ResourceHandler classpathWebHandler = new ResourceHandler();
        classpathWebHandler.setBaseResource(Resource.newClassPathResource("/web"));
        requestHandlers.add(classpathWebHandler);

        // Use as resource definitions everything inside that package
        Reflections reflections = new Reflections("ar.com.kfgodel.web.resources");
        Set<Class<?>> annotatedResources = reflections.getTypesAnnotatedWith(Path.class);

        ResourceConfig config = new ResourceConfig(annotatedResources);
        final JettyHttpContainer jerseyHandler = ContainerFactory.createContainer(JettyHttpContainer.class, config);
        requestHandlers.add(jerseyHandler);
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(requestHandlers.toArray(new Handler[requestHandlers.size()]));
        jettyServer.setHandler(handlers);
    }

    public void startAndJoin() throws Exception {
        jettyServer.start();
        jettyServer.join();
    }

    public static WebServer create(Optional<Integer> predefinedPort) {
        WebServer webServer = new WebServer();
        webServer.initialize(predefinedPort);
        return webServer;
    }

}
