package ar.com.kfgodel.webbyconvention.impl.auth.adapters;

import ar.com.kfgodel.webbyconvention.api.exceptions.WebServerException;
import org.eclipse.jetty.server.UserIdentity;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * This type represents the user identity of a logged in user for the web server.<br>
 *  This instance is also used as principal to simplify the interactions.<br>
 *  This identity is responsible of validating roles for users, however its implementation allows
 *  any role (assuming only "user" role is needed)
 *
 * Created by kfgodel on 27/03/15.
 */
public class JettyIdentityAdapter implements UserIdentity, Principal {

    private Object appIdentification;

    @Override
    public Subject getSubject() {
        throw new WebServerException("Not implemented");
    }

    @Override
    public Principal getUserPrincipal() {
        return this;
    }

    @Override
    public boolean isUserInRole(String role, Scope scope) {
        //Validate permission
        return true;
    }

    @Override
    public String getName() {
        return "user_" + appIdentification;
    }

    /**
     * Returns the application generated identification of the current user
     * @param <T> Type of expected object
     * @return The user identification for the application
     */
    public <T> T getApplicationIdentification(){
        return (T) appIdentification;
    }

  public static JettyIdentityAdapter create(Object applicationIdentification) {
        JettyIdentityAdapter userIdentity = new JettyIdentityAdapter();
        userIdentity.appIdentification = applicationIdentification;
        return userIdentity;
    }

}
