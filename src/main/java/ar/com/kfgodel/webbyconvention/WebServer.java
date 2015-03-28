package ar.com.kfgodel.webbyconvention;

import ar.com.kfgodel.webbyconvention.auth.FormAuthenticator;
import ar.com.kfgodel.webbyconvention.auth.LoginServiceImpl;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.glassfish.jersey.jetty.JettyHttpContainer;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.io.File;
import java.util.*;

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
//        jettyServer.setHandler(handlers);


        // Since this example is for our test webapp, we need to setup a LoginService so this shows how to create a
        // very simple hashmap based one.  The name of the LoginService needs to correspond to what is configured a
        // webapp's web.xml and since it has a lifecycle of its own we register it as a bean with the Jetty server
        // object so it can be started and stopped according to the lifecycle of the server itself. In this example
        // the name can be whatever you like since we are not dealing with webapp realms.
        LoginService loginService = LoginServiceImpl.create();
        jettyServer.addBean(loginService);

        // A security handler is a jetty handler that secures content behind a particular portion of a url space. The
        // ConstraintSecurityHandler is a more specialized handler that allows matching of urls to different
        // constraints. The server sets this as the first handler in the chain,
        // effectively applying these constraints to all subsequent handlers in the chain.
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        HandlerList securityList = new HandlerList();
        securityList.setHandlers(new Handler[]{new SessionHandler(), security});
        jettyServer.setHandler(securityList);

        // This constraint requires authentication and in addition that an authenticated user be a member of a given
        // set of roles for authorization purposes.
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{"user", "admin"});

        // Binds a url pattern with the previously created constraint. The roles for this constraing mapping are
        // mined from the Constraint itself although methods exist to declare and bind roles separately as well.
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec( "/api/v1/*" );
        mapping.setConstraint( constraint );

        // First you see the constraint mapping being applied to the handler as a singleton list,
        // however you can passing in as many security constraint mappings as you like so long as they follow the
        // mapping requirements of the servlet api. Next we set a BasicAuthenticator instance which is the object
        // that actually checks the credentials followed by the LoginService which is the store of known users, etc.
        security.setConstraintMappings(Collections.singletonList(mapping));
        security.setAuthenticator(new FormAuthenticator("/#/login",null,false));
        security.setLoginService(loginService);

        // chain the hello handler into the security handler
        security.setHandler(handlers);
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

    public void stop() throws WebServerException{
        try {
            jettyServer.stop();
        } catch (Exception e) {
            throw new WebServerException("Error stopping web server", e);
        }
    }
}
