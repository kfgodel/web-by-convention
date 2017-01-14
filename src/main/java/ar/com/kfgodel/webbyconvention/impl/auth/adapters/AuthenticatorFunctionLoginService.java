package ar.com.kfgodel.webbyconvention.impl.auth.adapters;

import ar.com.kfgodel.webbyconvention.api.auth.WebCredential;
import ar.com.kfgodel.webbyconvention.api.exceptions.WebServerException;
import ar.com.kfgodel.webbyconvention.impl.auth.credent.ImmutableCredential;
import ar.com.kfgodel.webbyconvention.impl.auth.identity.ThreadLocalIdentityService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;

import java.util.Optional;
import java.util.function.Function;

/**
 * This type represents the web server login service, used by the web server to authenticate users.<br>
 * It uses an authenticator function to log in users from credentials. This class acts as an adapter
 * between the web server authentication layer, and the application login behavior
 * <p>
 * Created by kfgodel on 27/03/15.
 */
public class AuthenticatorFunctionLoginService implements LoginService {

  private IdentityService identityService;

  private Function<WebCredential, Optional<Object>> appAuthenticator;

  @Override
  public String getName() {
    return "loginService";
  }

  @Override
  public UserIdentity login(String username, Object credentials) {
    return login(username, credentials, null);
  }

  public UserIdentity login(String username, Object credentials, Request request) {
    ImmutableCredential webCredential = ImmutableCredential.create(username, (String) credentials, request);
    Optional<Object> foundUserId = appAuthenticator.apply(webCredential);
    return foundUserId
      .map(JettyIdentityAdapter::create)
      .orElse(null);
  }

  @Override
  public boolean validate(UserIdentity user) {
    //Check if the user is still a user
    return true;
  }

  @Override
  public IdentityService getIdentityService() {
    return identityService;
  }

  @Override
  public void setIdentityService(IdentityService service) {
    this.identityService = service;
  }

  @Override
  public void logout(UserIdentity user) {
    throw new WebServerException("Not implemented");
  }

  public static AuthenticatorFunctionLoginService create(Function<WebCredential, Optional<Object>> appAuthenticator) {
    AuthenticatorFunctionLoginService service = new AuthenticatorFunctionLoginService();
    service.identityService = ThreadLocalIdentityService.create();
    service.appAuthenticator = appAuthenticator;
    return service;
  }

}
