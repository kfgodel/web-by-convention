package ar.com.kfgodel.webbyconvention.auth;

import java.security.Principal;

/**
 * Created by kfgodel on 28/03/15.
 */
public class PrincipalImpl implements Principal {

    public static PrincipalImpl create() {
        PrincipalImpl principal = new PrincipalImpl();
        return principal;
    }

    @Override
    public String getName() {
        return "pruebaPrincipal";
    }
}
