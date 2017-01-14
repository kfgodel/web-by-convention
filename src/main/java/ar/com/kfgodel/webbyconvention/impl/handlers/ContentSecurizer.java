package ar.com.kfgodel.webbyconvention.impl.handlers;

import ar.com.kfgodel.webbyconvention.api.config.WebServerConfiguration;
import ar.com.kfgodel.webbyconvention.bugs.FormAuthenticator;
import ar.com.kfgodel.webbyconvention.impl.auth.adapters.AuthenticatorFunctionLoginService;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.security.Constraint;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This type represents the handler securizer that ensures that sensible requests are authenticated
 * Created by kfgodel on 15/02/16.
 */
public class ContentSecurizer {

  private WebServerConfiguration config;

  public static ContentSecurizer create(WebServerConfiguration config) {
    ContentSecurizer securizer = new ContentSecurizer();
    securizer.config = config;
    return securizer;
  }

  /**
   * Creates constraints to secure the content access
   *
   * @param contentHandler The handler that contains sensitive content
   * @return The secured handlers
   */
  public Handler securize(Handler contentHandler) {

    // This is needed (as a pre-requisite) by the form authenticator used by the security handler
    SessionHandler sessionHandler = createSessionHandler();

    // The url access restrictor to avoid unauthenticated access
    ConstraintSecurityHandler securityHandler = createConstraintedHandlerFor(contentHandler);

    return Handlers.asList(sessionHandler, securityHandler);
  }

  /**
   * Creates a session handler that manages logged in sessions according to requests.<br>
   * @return The session manager handler
   */
  private SessionHandler createSessionHandler() {
    SessionHandler sessionHandler = new SessionHandler();
    sessionHandler.getSessionManager().setMaxInactiveInterval(config.getSessionTimeout());
    return sessionHandler;
  }

  /**
   * Creates a url restricted access based on the configuration to prevent unauthenticated access
   * @param contentHandler The sensitive content to be restricted
   * @return The new security handler
   */
  private ConstraintSecurityHandler createConstraintedHandlerFor(Handler contentHandler) {
    // A security handler is a jetty handler that secures content behind a particular portion of a url space. The
    // ConstraintSecurityHandler is a more specialized handler that allows matching of urls to different
    // constraints
    ConstraintSecurityHandler security = new ConstraintSecurityHandler();

    // We add the mapping to restrict the secured urls. Next a form authenticator will look for certain
    // requests and parameters to authenticate a user and manage its session
    security.setConstraintMappings(getConfigConstraintedMappings());
    security.setAuthenticator(new FormAuthenticator(null, null, false));

    // Finally a login service is used by the chosen authenticator to authenticate a user against the application code
    // This login service uses the function given by the webServer config
    LoginService loginService = AuthenticatorFunctionLoginService.create(config.getAuthenticatorFunction());
    security.setLoginService(loginService);

    // Wrap the unsecure handlers into the secure handlers
    security.setHandler(contentHandler);
    return security;
  }

  private List<ConstraintMapping> getConfigConstraintedMappings() {
    // This constraint requires authentication and in addition that an authenticated user be a member of a given
    // the "user" role.
    Constraint constraint = new Constraint();
    constraint.setName("authenticatedUser");
    constraint.setAuthenticate(true);
    constraint.setRoles(new String[]{"user"});

    return createContraintedUrlMappings(constraint);
  }

  private List<ConstraintMapping> createContraintedUrlMappings(Constraint constraint) {
    return config.geSecuredRootPaths()
      .map((apiRootPath)-> createMappingFor(constraint, apiRootPath))
      .collect(Collectors.toList());
  }

  /**
   * Creates a contrainted url mapping that enforces security restrictions over the url root
   * @param constraint The type of restriction
   * @param rootPath The root url
   * @return The contrainted mapping
   */
  private ConstraintMapping createMappingFor(Constraint constraint, String rootPath) {
    // Binds a url pattern with the previously created constraint. The roles for this constraing mapping are
    // mined from the Constraint itself although methods exist to declare and bind roles separately as well.
    ConstraintMapping mapping = new ConstraintMapping();
    mapping.setPathSpec(rootPath + "/*");
    mapping.setConstraint(constraint);
    return mapping;
  }
}
