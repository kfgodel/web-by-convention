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

        // Serve as static content everything under classpath:/web/
        ResourceHandler webHandler = new ResourceHandler();
        webHandler.setBaseResource(Resource.newClassPathResource("/web"));

        // Use as resource definitions everything inside that package
        Reflections reflections = new Reflections("ar.com.kfgodel.web.resources");
        Set<Class<?>> annotatedResources = reflections.getTypesAnnotatedWith(Path.class);
        
        ResourceConfig config = new ResourceConfig(annotatedResources);
        final JettyHttpContainer jerseyHandler = ContainerFactory.createContainer(JettyHttpContainer.class, config);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { webHandler, jerseyHandler });
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
