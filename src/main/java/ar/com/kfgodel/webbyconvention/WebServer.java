package ar.com.kfgodel.webbyconvention;

import ar.com.kfgodel.webbyconvention.auth.FormAuthenticator;
import ar.com.kfgodel.webbyconvention.auth.WebLoginService;
import ar.com.kfgodel.webbyconvention.auth.impl.Handlers;
import ar.com.kfgodel.webbyconvention.bugs.NonLockingResourceHandler;
import ar.com.kfgodel.webbyconvention.logging.RequestLoggerHandler;
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

        List<Handler> partialList = new ArrayList<>();

        partialList.add(RequestLoggerHandler.create());

        // Make web content refreshable if changed
        serveDynamicContent(partialList);

        // Serve as static content everything under classpath:/web/
        serveStaticContent(partialList);

        // Publish Jersey resources as API
        serveApi(partialList);

        HandlerList unsecuredHandlers = Handlers.asList(partialList);
        HandlerList securedHandlers = addAccessConstraints(unsecuredHandlers);
        jettyServer.setHandler(securedHandlers);

        // Prevents file locking in windows
        jettyServer.setAttribute("useFileMappedBuffer",false);
    }

    /**
     * Creates constrants to secure the api access frim unauthenticated access
     * @param unsecuredHandlers The list of handlers to secure
     * @return The secured handlers
     */
    private HandlerList addAccessConstraints(HandlerList unsecuredHandlers) {

        // This constraint requires authentication and in addition that an authenticated user be a member of a given
        // the "user" role.
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{"user"});

        // Binds a url pattern with the previously created constraint. The roles for this constraing mapping are
        // mined from the Constraint itself although methods exist to declare and bind roles separately as well.
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec( "/api/*" );
        mapping.setConstraint(constraint);

        // A security handler is a jetty handler that secures content behind a particular portion of a url space. The
        // ConstraintSecurityHandler is a more specialized handler that allows matching of urls to different
        // constraints. The session handler is needed by form authentication to create a session for a given
        // user
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.getSessionManager().setMaxInactiveInterval(config.getSessionTimeout());
        HandlerList securedHandlers = Handlers.asList(sessionHandler, security);

        // We add the mapping to restrict the secured urls. Next a form authenticator will look for certain
        // requests and parameters to authenticate a user and manage its session
        security.setConstraintMappings(Collections.singletonList(mapping));
        security.setAuthenticator(new FormAuthenticator("/#/login",null,false));

        // Finally a login service is used by the chosen authenticator to authenticate a user against the application code
        // This login service uses the function given by the webServer config
        LoginService loginService = WebLoginService.create(config.getAuthenticatorFunction());
        security.setLoginService(loginService);
        jettyServer.addBean(loginService);

        // Wrap the unsecure handlers into the secure handlers
        security.setHandler(unsecuredHandlers);
        return securedHandlers;
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
                Handler folderWebHandler = createDynamicContentHandler(refreshableSource);
                requestHandlers.add(folderWebHandler);
            }
        }
    }

    /**
     * Create the resource handler that we will use to serve static content that could be changed
     * while we are running. Due to the way Jetty opens files to buffer them, in windows that
     * generates a locked file, that cannot be changed once served. To prevent that we try to
     * detect the OS and use a non standard handler that doesn't lock files.
     * @return
     * @param refreshableSource
     */
    private Handler createDynamicContentHandler(String refreshableSource) {
        String currentOsName = System.getProperty("os.name");
        if(currentOsName.startsWith("Windows")){
            NonLockingResourceHandler nonLockingResourceHandler = new NonLockingResourceHandler();
            nonLockingResourceHandler.setResourceBase(refreshableSource);
            return nonLockingResourceHandler;
        }
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(refreshableSource);
        return resourceHandler;
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
