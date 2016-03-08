package ar.com.kfgodel.webbyconvention.impl.auth.identity;

import ar.com.kfgodel.nary.api.Nary;
import ar.com.kfgodel.nary.api.optionals.Optional;
import ar.com.kfgodel.webbyconvention.api.exceptions.WebServerException;
import ar.com.kfgodel.webbyconvention.impl.auth.adapters.JettyIdentityAdapter;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.RunAsToken;
import org.eclipse.jetty.server.UserIdentity;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * This type represents an identity manager that stores logged in identities in a thread local.<br>
 *  Using this type, the web server can make the current user identity available to the application through the
 *  current thread
 * Created by kfgodel on 27/03/15.
 */
public class ThreadLocalIdentityService implements IdentityService {


    /**
     * This variable changes by thread, storing the current authenticated user identification
     */
    private static final ThreadLocal<JettyIdentityAdapter> currentIdentification = new ThreadLocal<>();


    /**
     * Removes the id from teh current thread
     */
    private static void removeFromThread() {
        currentIdentification.remove();
    }

    /**
     * Sets the give id available in the current thread
     */
    private static void setInThread(JettyIdentityAdapter currentId) {
        currentIdentification.set(currentId);
    }

    @Override
    public Object associate(UserIdentity user) {
        if(user == null){
            //Unlink
            removeFromThread();
        }else{
            //We don't use other type of identity
            JettyIdentityAdapter userId = (JettyIdentityAdapter) user;
            // Make the id available
            setInThread(userId);
        }
        return null;
    }

    @Override
    public void disassociate(Object previous) {
        // Unlink user from thread
        removeFromThread();
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

    public static ThreadLocalIdentityService create() {
        ThreadLocalIdentityService identityService = new ThreadLocalIdentityService();
        return identityService;
    }

  /**
   * Gets the identity stored in the current thread if there's any
   * @return An empty optional if there's no identity yet
   */
  public static Optional<JettyIdentityAdapter> getFromThread(){
        return Nary.ofNullable(currentIdentification.get());
    }


}
