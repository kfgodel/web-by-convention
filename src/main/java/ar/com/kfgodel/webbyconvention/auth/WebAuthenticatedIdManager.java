package ar.com.kfgodel.webbyconvention.auth;

import ar.com.kfgodel.webbyconvention.WebServerException;
import ar.com.kfgodel.webbyconvention.auth.api.WebAuthenticatedId;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.RunAsToken;
import org.eclipse.jetty.server.UserIdentity;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * This type allows the management of a web identification into the context of the executing thread.<br>
 *     Using this type the web server can make the current user identity available to the application through the
 *     current thread
 * Created by kfgodel on 27/03/15.
 */
public class WebAuthenticatedIdManager implements IdentityService {


    @Override
    public Object associate(UserIdentity user) {
        if(user == null){
            //Unlink
            WebAuthenticatedId.removeFromThread();
        }else{
            //We don't use other type of identity
            WebUserIdentification userId = (WebUserIdentification) user;
            // Make the id available
            WebAuthenticatedId.setInThread(userId);
        }
        return null;
    }

    @Override
    public void disassociate(Object previous) {
        // Unlink user from thread
        WebAuthenticatedId.removeFromThread();
    }

    @Override
    public Object setRunAs(UserIdentity user, RunAsToken token) {
        throw new WebServerException("Not implemented: " + user + " " + token);
    }

    @Override
    public void unsetRunAs(Object token) {
        throw new WebServerException("Not implemented: "+ token);
    }

    @Override
    public UserIdentity newUserIdentity(Subject subject, Principal userPrincipal, String[] roles) {
        throw new WebServerException("Not implemented: " + subject + " " + userPrincipal + " " + roles);
    }

    @Override
    public RunAsToken newRunAsToken(String runAsName) {
        throw new WebServerException("Not implemented: " + runAsName);
    }

    @Override
    public UserIdentity getSystemUserIdentity() {
        throw new WebServerException("Not implemented");
    }

    public static WebAuthenticatedIdManager create() {
        WebAuthenticatedIdManager identityService = new WebAuthenticatedIdManager();
        return identityService;
    }

}
