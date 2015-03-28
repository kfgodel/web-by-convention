package ar.com.kfgodel.webbyconvention.auth;

import ar.com.kfgodel.webbyconvention.WebServerException;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.RunAsToken;
import org.eclipse.jetty.server.UserIdentity;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * Created by kfgodel on 27/03/15.
 */
public class IdentityServiceImpl implements IdentityService {

    private IdentityService delegate;

    @Override
    public Object associate(UserIdentity user) {
        // Link user to thread
        return delegate.associate(user);
    }

    @Override
    public void disassociate(Object previous) {
        // Unlik user from thread
        delegate.disassociate(previous);
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

    public static IdentityServiceImpl create() {
        IdentityServiceImpl identityService = new IdentityServiceImpl();
        identityService.delegate = new DefaultIdentityService();
        return identityService;
    }

}
