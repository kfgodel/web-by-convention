package ar.com.kfgodel.webbyconvention.auth;

import ar.com.kfgodel.webbyconvention.WebServerException;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;

/**
 * Created by kfgodel on 27/03/15.
 */
public class LoginServiceImpl implements LoginService {

    private IdentityService identityService;

    @Override
    public String getName() {
        return "restricted";
    }

    @Override
    public UserIdentity login(String username, Object credentials) {
        if("pepe".equals(username) && "1234".equals(credentials)){
            return UserIdentityImpl.create();
        }
        return null;
    }

    @Override
    public boolean validate(UserIdentity user) {
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

    public static LoginServiceImpl create() {
        LoginServiceImpl service = new LoginServiceImpl();
        service.identityService = IdentityServiceImpl.create();
        return service;
    }

}
