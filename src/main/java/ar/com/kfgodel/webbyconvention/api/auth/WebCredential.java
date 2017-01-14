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

  /**
   * Returns the parameter available in the request where this credential comes from
   *
   * @param parameterName name of the request parameter
   * @return The parameter value (may be null if not defined)
   */
  String getRequestParameter(String parameterName);
}
