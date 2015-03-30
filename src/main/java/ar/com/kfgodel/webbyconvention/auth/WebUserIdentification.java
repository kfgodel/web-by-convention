package ar.com.kfgodel.webbyconvention.auth;

import ar.com.kfgodel.webbyconvention.WebServerException;
import org.eclipse.jetty.server.UserIdentity;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * This type represents the user identity of a logged user for the web server.<br>
 *     This instance is also used as principal to simplify the amount of objects.<br>
 *     This identity is responsible of validating roles for users, but it allows any role (assuming only "user" role is needed)
 *
 * Created by kfgodel on 27/03/15.
 */
public class WebUserIdentification implements UserIdentity, Principal {

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

    public static WebUserIdentification create(Object applicationIdentification) {
        WebUserIdentification userIdentity = new WebUserIdentification();
        userIdentity.appIdentification = applicationIdentification;
        return userIdentity;
    }
}
