package ar.com.kfgodel.webbyconvention.auth.impl;

import ar.com.kfgodel.webbyconvention.auth.api.WebCredential;

/**
 * This Type implements the web credentials as an immutable object
 * Created by kfgodel on 29/03/15.
 */
public class ImmutableCredential implements WebCredential {

    private String username;
    private String password;


    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public static ImmutableCredential create(String username, String password) {
        ImmutableCredential credential = new ImmutableCredential();
        credential.username = username;
        credential.password = password;
        return credential;
    }

}
