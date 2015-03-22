package ar.com.kfgodel.webbyconvention;

/**
 * This type represents an error in the web server
 * Created by kfgodel on 22/03/15.
 */
public class WebServerException extends RuntimeException {

    public WebServerException(String message) {
        super(message);
    }

    public WebServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebServerException(Throwable cause) {
        super(cause);
    }
}
