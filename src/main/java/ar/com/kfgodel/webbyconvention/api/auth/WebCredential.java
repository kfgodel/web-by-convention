package ar.com.kfgodel.webbyconvention.api.auth;

/**
 * This type represents the credentials presented by users through the web server to
 * authenticate
 *
 * Created by kfgodel on 29/03/15.
 */
public interface WebCredential {

    /**
     * @return The username sent by the user
     */
    String getUsername();

    /**
     * @return The accompanying password
     */
    String getPassword();
}
