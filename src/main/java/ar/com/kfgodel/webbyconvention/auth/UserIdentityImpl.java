package ar.com.kfgodel.webbyconvention.auth;

import ar.com.kfgodel.webbyconvention.WebServerException;
import org.eclipse.jetty.server.UserIdentity;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * Created by kfgodel on 27/03/15.
 */
public class UserIdentityImpl implements UserIdentity {

    private Principal principal;
    @Override
    public Subject getSubject() {
        throw new WebServerException("Not implemented");
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role, Scope scope) {
        //Validate permission
        return true;
    }

    public static UserIdentityImpl create() {
        UserIdentityImpl userIdentity = new UserIdentityImpl();
        userIdentity.principal = PrincipalImpl.create();
        return userIdentity;
    }

}
