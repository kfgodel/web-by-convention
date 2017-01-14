package ar.com.kfgodel.webbyconvention.impl.auth.credent;


import ar.com.kfgodel.webbyconvention.api.auth.WebCredential;
import org.eclipse.jetty.server.Request;

/**
 * This Type implements the web credentials as an immutable object
 * Created by kfgodel on 29/03/15.
 */
public class ImmutableCredential implements WebCredential {

    private String username;
    private String password;
    private Request request;


    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getRequestParameter(String parameterName) {
        return request.getParameter(parameterName);
    }

    public static ImmutableCredential create(String username, String password, Request request) {
        ImmutableCredential credential = new ImmutableCredential();
        credential.username = username;
        credential.password = password;
        credential.request = request;
        return credential;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("user: ");
        builder.append(username);
        builder.append(", pass: ");
        builder.append(password);
        return builder.toString();
    }
}
