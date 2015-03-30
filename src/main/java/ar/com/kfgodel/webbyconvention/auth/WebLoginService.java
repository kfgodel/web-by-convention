package ar.com.kfgodel.webbyconvention.auth;

import ar.com.kfgodel.webbyconvention.WebServerException;
import ar.com.kfgodel.webbyconvention.auth.api.WebCredential;
import ar.com.kfgodel.webbyconvention.auth.impl.ImmutableCredential;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;

import java.util.Optional;
import java.util.function.Function;

/**
 * This type represents the web server login service, used to authenticate users.<br>
 *     This class acts as an adaptar between the web server authentication layer, and the application
 *     login behavior
 *
 * Created by kfgodel on 27/03/15.
 */
public class WebLoginService implements LoginService {

    private IdentityService identityService;

    private Function<WebCredential, Optional<Object>> appAuthenticator;


    @Override
    public String getName() {
        return "loginService";
    }

    @Override
    public UserIdentity login(String username, Object credentials) {
        if(!String.class.isInstance(credentials)){
            throw new WebServerException("This service is not preprared to receive non String credentials: " + credentials);
        }
        ImmutableCredential webCredential = ImmutableCredential.create(username, (String) credentials);
        Optional<Object> foundUserId = appAuthenticator.apply(webCredential);
        return foundUserId.map(WebUserIdentification::create).orElse(null);
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

    public static WebLoginService create(Function<WebCredential, Optional<Object>> appAuthenticator) {
        WebLoginService service = new WebLoginService();
        service.identityService = WebAuthenticatedIdManager.create();
        service.appAuthenticator = appAuthenticator;
        return service;
    }

}
