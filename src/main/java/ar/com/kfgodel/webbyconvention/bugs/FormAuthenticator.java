package ar.com.kfgodel.webbyconvention.bugs;

import ar.com.kfgodel.webbyconvention.api.config.WebServerConfiguration;
import ar.com.kfgodel.webbyconvention.impl.auth.adapters.AuthenticatorFunctionLoginService;
import ar.com.kfgodel.webbyconvention.impl.auth.adapters.JettyIdentityAdapter;
import org.eclipse.jetty.http.*;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.DeferredAuthentication;
import org.eclipse.jetty.security.authentication.LoginAuthenticator;
import org.eclipse.jetty.security.authentication.SessionAuthentication;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.security.Constraint;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Optional;

/**
 * Copied from jetty FormAuthenticator to fix a NPE bug while accessing a null contextPath.<br>
 *     Modified a little to respond 401 and 200 instead of redirect.
 *     Added logout
 * Created by kfgodel on 28/03/15.
 */
public class FormAuthenticator extends LoginAuthenticator
{
    private static final Logger LOG = Log.getLogger(FormAuthenticator.class);

    public final static String __FORM_LOGIN_PAGE="org.eclipse.jetty.security.form_login_page";
    public final static String __FORM_ERROR_PAGE="org.eclipse.jetty.security.form_error_page";
    public final static String __FORM_DISPATCH="org.eclipse.jetty.security.dispatch";
    public final static String __J_URI = "org.eclipse.jetty.security.form_URI";
    public final static String __J_POST = "org.eclipse.jetty.security.form_POST";
    public final static String __J_METHOD = "org.eclipse.jetty.security.form_METHOD";
    public final static String __J_SECURITY_CHECK = "/j_security_check";
    public final static String __J_USERNAME = "j_username";
    public final static String __J_PASSWORD = "j_password";

    public final static String __J_LOGOUT = "/j_logout";

    private String _formErrorPage;
    private String _formErrorPath;
    private String _formLoginPage;
    private String _formLoginPath;
    private boolean _dispatch;
    private boolean _alwaysSaveUri;

    private WebServerConfiguration _config;

    public FormAuthenticator()
    {
    }

    /* ------------------------------------------------------------ */
    public FormAuthenticator(String login, String error, boolean dispatch, WebServerConfiguration config)
    {
        this();
        this._config = config;
        if (login!=null)
            setLoginPage(login);
        if (error!=null)
            setErrorPage(error);
        _dispatch=dispatch;
    }

    /* ------------------------------------------------------------ */
    /**
     * If true, uris that cause a redirect to a login page will always
     * be remembered. If false, only the first uri that leads to a login
     * page redirect is remembered.
     * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=379909
     * @param alwaysSave
     */
    public void setAlwaysSaveUri (boolean alwaysSave)
    {
        _alwaysSaveUri = alwaysSave;
    }


    /* ------------------------------------------------------------ */
    public boolean getAlwaysSaveUri ()
    {
        return _alwaysSaveUri;
    }

    /* ------------------------------------------------------------ */
    /**
     * @see org.eclipse.jetty.security.authentication.LoginAuthenticator#setConfiguration(org.eclipse.jetty.security.Authenticator.AuthConfiguration)
     */
    @Override
    public void setConfiguration(AuthConfiguration configuration)
    {
        super.setConfiguration(configuration);
        String login=configuration.getInitParameter(FormAuthenticator.__FORM_LOGIN_PAGE);
        if (login!=null)
            setLoginPage(login);
        String error=configuration.getInitParameter(FormAuthenticator.__FORM_ERROR_PAGE);
        if (error!=null)
            setErrorPage(error);
        String dispatch=configuration.getInitParameter(FormAuthenticator.__FORM_DISPATCH);
        _dispatch = dispatch==null?_dispatch:Boolean.valueOf(dispatch);
    }

    /* ------------------------------------------------------------ */
    @Override
    public String getAuthMethod()
    {
        return Constraint.__FORM_AUTH;
    }

    /* ------------------------------------------------------------ */
    private void setLoginPage(String path)
    {
        if (!path.startsWith("/"))
        {
            LOG.warn("form-login-page must start with /");
            path = "/" + path;
        }
        _formLoginPage = path;
        _formLoginPath = path;
        if (_formLoginPath.indexOf('?') > 0)
            _formLoginPath = _formLoginPath.substring(0, _formLoginPath.indexOf('?'));
    }

    /* ------------------------------------------------------------ */
    private void setErrorPage(String path)
    {
        if (path == null || path.trim().length() == 0)
        {
            _formErrorPath = null;
            _formErrorPage = null;
        }
        else
        {
            if (!path.startsWith("/"))
            {
                LOG.warn("form-error-page must start with /");
                path = "/" + path;
            }
            _formErrorPage = path;
            _formErrorPath = path;

            if (_formErrorPath.indexOf('?') > 0)
                _formErrorPath = _formErrorPath.substring(0, _formErrorPath.indexOf('?'));
        }
    }


    /* ------------------------------------------------------------ */
    @Override
    public UserIdentity login(String username, Object password, ServletRequest request)
    {

        UserIdentity user = superlogin(username, password, request);
        if (user!=null)
        {
            HttpSession session = ((HttpServletRequest)request).getSession(true);
            Authentication cached=new SessionAuthentication(getAuthMethod(),user,password);
            session.setAttribute(SessionAuthentication.__J_AUTHENTICATED, cached);
        }
        return user;
    }


    /* ------------------------------------------------------------ */
    public UserIdentity superlogin(String username, Object password, ServletRequest request) {
        UserIdentity user;
        if (_loginService instanceof AuthenticatorFunctionLoginService) {
            // Esta esl a version que permite acceder al request
            user = ((AuthenticatorFunctionLoginService) _loginService).login(username, password, (Request) request);
        } else {
            // Este es el codigo normal
            user = _loginService.login(username, password);
        }
        if (user != null) {
            renewSession((HttpServletRequest) request, (request instanceof Request ? ((Request) request).getResponse() : null));
            return user;
        }
        return null;
    }


    /* ------------------------------------------------------------ */
    @Override
    public void prepareRequest(ServletRequest request)
    {
        //if this is a request resulting from a redirect after auth is complete
        //(ie its from a redirect to the original request uri) then due to
        //browser handling of 302 redirects, the method may not be the same as
        //that of the original request. Replace the method and original post
        //params (if it was a post).
        //
        //See Servlet Spec 3.1 sec 13.6.3
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute(SessionAuthentication.__J_AUTHENTICATED) == null)
            return; //not authenticated yet

        String juri = (String)session.getAttribute(__J_URI);
        if (juri == null || juri.length() == 0)
            return; //no original uri saved

        String method = (String)session.getAttribute(__J_METHOD);
        if (method == null || method.length() == 0)
            return; //didn't save original request method

        StringBuffer buf = httpRequest.getRequestURL();
        if (httpRequest.getQueryString() != null)
            buf.append("?").append(httpRequest.getQueryString());

        if (!juri.equals(buf.toString()))
            return; //this request is not for the same url as the original

        //restore the original request's method on this request
        if (LOG.isDebugEnabled()) LOG.debug("Restoring original method {} for {} with method {}", method, juri,httpRequest.getMethod());
        Request base_request = HttpChannel.getCurrentHttpChannel().getRequest();
        HttpMethod m = HttpMethod.fromString(method);
        LOG.info("Ignorando restore de metodo: {} en request {} {}", m, base_request.getMethod(), base_request.getUri());
        // Esto me cambia el meodo de algunos requests erroneamente. No sé si es un bug o lo estoy usando mal
//        base_request.setMethod(m,m.asString());
    }

    /* ------------------------------------------------------------ */
    @Override
    public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        String uri = request.getRequestURI();
        if (uri==null)
            uri= URIUtil.SLASH;

        if(isJLogout(uri)){
            HttpSession currentSession = request.getSession(false);
            if(currentSession != null){
                currentSession.invalidate();
            }

            response.setContentLength(0);
            response.setContentType("text/plain");
            return Authentication.SEND_SUCCESS;
        }

        mandatory|=isJSecurityCheck(uri);
        if (!mandatory)
            return new DeferredAuthentication(this);

        if (isLoginOrErrorPage(URIUtil.addPaths(request.getServletPath(),request.getPathInfo())) &&!DeferredAuthentication.isDeferred(response))
            return new DeferredAuthentication(this);


        HttpSession session = request.getSession(true);

        try
        {
            // Handle a request for authentication.
            if (isJSecurityCheck(uri))
            {
                final String username = request.getParameter(__J_USERNAME);
                final String password = request.getParameter(__J_PASSWORD);

                UserIdentity user = login(username, password, request);
                LOG.debug("jsecuritycheck {} {}",username,user);
                session = request.getSession(true);
                if (user!=null)
                {
                    // Redirect to original request
                    String nuri;
                    FormAuthentication form_auth;
                    synchronized(session)
                    {
                        nuri = (String) session.getAttribute(__J_URI);

                        if (nuri == null || nuri.length() == 0)
                        {
                            nuri = request.getContextPath();
                            if(nuri == null){
                                // Try to fix NPE from contextPath undefined (due to non servlet environment)
                                nuri = "/";
                            }
                            if (nuri.length() == 0)
                                nuri = URIUtil.SLASH;
                        }
                        form_auth = new FormAuthentication(getAuthMethod(),user);
                    }
                    LOG.debug("authenticated {}->{}",form_auth,nuri);

                    Optional<String> successPath = _config.getSuccessfulAuthenticationRedirectPath();
                    if (successPath.isPresent()) {
                        doRedirectTo(successPath.get(), response);
                        return form_auth;
                    } else {
                        // This is the only type we use
                        JettyIdentityAdapter identification = (JettyIdentityAdapter) form_auth.getUserIdentity();
                        Object applicationIdentification = (Object) identification.getApplicationIdentification();

                        // Do a 200 OK instead of 303 redirect to main page
                        response.getWriter().write(applicationIdentification.toString());
                        response.setContentType("text/plain");
                        return form_auth;
                    }
                }

                // not authenticated
                if (LOG.isDebugEnabled())
                    LOG.debug("Form authentication FAILED for " + StringUtil.printable(username));

                Optional<String> failedPath = _config.getFailedAuthenticationRedirectPath();
                if (failedPath.isPresent()) {
                    doRedirectTo(failedPath.get(), response);
                    return Authentication.SEND_FAILURE;
                }

                if (_formErrorPage == null)
                {
                    LOG.debug("auth failed {}->401",username);
                    if (response != null)
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                }
                else if (_dispatch)
                {
                    LOG.debug("auth failed {}=={}",username,_formErrorPage);
                    RequestDispatcher dispatcher = request.getRequestDispatcher(_formErrorPage);
                    response.setHeader(HttpHeader.CACHE_CONTROL.asString(), HttpHeaderValue.NO_CACHE.asString());
                    response.setDateHeader(HttpHeader.EXPIRES.asString(),1);
                    dispatcher.forward(new FormRequest(request), new FormResponse(response));
                }
                else
                {
                    LOG.debug("auth failed {}->{}", username, _formErrorPage);
                    Response base_response = HttpChannel.getCurrentHttpChannel().getResponse();
                    Request base_request = HttpChannel.getCurrentHttpChannel().getRequest();
                    int redirectCode = (base_request.getHttpVersion().getVersion() < HttpVersion.HTTP_1_1.getVersion() ? HttpServletResponse.SC_MOVED_TEMPORARILY : HttpServletResponse.SC_SEE_OTHER);
                    base_response.sendRedirect(redirectCode, response.encodeRedirectURL(URIUtil.addPaths(request.getContextPath(), _formErrorPage)));
                }

                return Authentication.SEND_FAILURE;
            }

            // Look for cached authentication
            Authentication authentication = (Authentication) session.getAttribute(SessionAuthentication.__J_AUTHENTICATED);
            if (authentication != null)
            {
                // Has authentication been revoked?
                if (authentication instanceof Authentication.User &&
                        _loginService!=null &&
                        !_loginService.validate(((Authentication.User)authentication).getUserIdentity()))
                {
                    LOG.debug("auth revoked {}",authentication);
                    session.removeAttribute(SessionAuthentication.__J_AUTHENTICATED);
                }
                else
                {
                    synchronized (session)
                    {
                        String j_uri=(String)session.getAttribute(__J_URI);
                        if (j_uri!=null)
                        {
                            //check if the request is for the same url as the original and restore
                            //params if it was a post
                            LOG.debug("auth retry {}->{}",authentication,j_uri);
                            StringBuffer buf = request.getRequestURL();
                            if (request.getQueryString() != null)
                                buf.append("?").append(request.getQueryString());

                            if (j_uri.equals(buf.toString()))
                            {
                                MultiMap<String> j_post = (MultiMap<String>)session.getAttribute(__J_POST);
                                if (j_post!=null)
                                {
                                    LOG.debug("auth rePOST {}->{}",authentication,j_uri);
                                    Request base_request = HttpChannel.getCurrentHttpChannel().getRequest();
                                    base_request.setContentParameters(j_post);
                                }
                                session.removeAttribute(__J_URI);
                                session.removeAttribute(__J_METHOD);
                                session.removeAttribute(__J_POST);
                            }
                        }
                    }
                    LOG.debug("auth {}",authentication);
                    return authentication;
                }
            }

            // if we can't send challenge
            if (DeferredAuthentication.isDeferred(response))
            {
                LOG.debug("auth deferred {}",session.getId());
                return Authentication.UNAUTHENTICATED;
            }

            // remember the current URI
            synchronized (session)
            {
                // But only if it is not set already, or we save every uri that leads to a login form redirect
                if (session.getAttribute(__J_URI)==null || _alwaysSaveUri)
                {
                    StringBuffer buf = request.getRequestURL();
                    if (request.getQueryString() != null)
                        buf.append("?").append(request.getQueryString());
                    session.setAttribute(__J_URI, buf.toString());
                    session.setAttribute(__J_METHOD, request.getMethod());

                    if (MimeTypes.Type.FORM_ENCODED.is(req.getContentType()) && HttpMethod.POST.is(request.getMethod()))
                    {
                        Request base_request = (req instanceof Request)?(Request)req:HttpChannel.getCurrentHttpChannel().getRequest();
                        MultiMap<String> formParameters = new MultiMap<>();
                        base_request.extractFormParameters(formParameters);
                        session.setAttribute(__J_POST, formParameters);
                    }
                }
            }

            // send the the challenge
            if (_dispatch)
            {
                LOG.debug("challenge {}=={}",session.getId(),_formLoginPage);
                RequestDispatcher dispatcher = request.getRequestDispatcher(_formLoginPage);
                response.setHeader(HttpHeader.CACHE_CONTROL.asString(),HttpHeaderValue.NO_CACHE.asString());
                response.setDateHeader(HttpHeader.EXPIRES.asString(),1);
                dispatcher.forward(new FormRequest(request), new FormResponse(response));
            }
            else
            {
                LOG.debug("challenge {}->{}", session.getId(), _formLoginPage);
                //Do a 401 instead of a 303 redirect to login page
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return Authentication.SEND_FAILURE;
            }
            return Authentication.SEND_CONTINUE;
        }
        catch (IOException | ServletException e)
        {
            throw new ServerAuthException(e);
        }
    }

    private void doRedirectTo(String redirectPath, HttpServletResponse response) throws IOException {
        response.setContentLength(0);
        Response base_response = HttpChannel.getCurrentHttpChannel().getResponse();
        Request base_request = HttpChannel.getCurrentHttpChannel().getRequest();
        int redirectCode = (base_request.getHttpVersion().getVersion() < HttpVersion.HTTP_1_1.getVersion() ? HttpServletResponse.SC_MOVED_TEMPORARILY : HttpServletResponse.SC_SEE_OTHER);
        base_response.sendRedirect(redirectCode, response.encodeRedirectURL(redirectPath));
    }

    /* ------------------------------------------------------------ */
    public boolean isJSecurityCheck(String uri)
    {
        return hasUriSegment(uri, __J_SECURITY_CHECK);
    }
    public boolean isJLogout(String uri)
    {
        return hasUriSegment(uri, __J_LOGOUT);
    }

    private boolean hasUriSegment(String uri, String urlSegment) {
        int jsc = uri.indexOf(urlSegment);

        if (jsc<0)
            return false;
        int e=jsc+ urlSegment.length();
        if (e==uri.length())
            return true;
        char c = uri.charAt(e);
        return c==';'||c=='#'||c=='/'||c=='?';
    }

    /* ------------------------------------------------------------ */
    public boolean isLoginOrErrorPage(String pathInContext)
    {
        return pathInContext != null && (pathInContext.equals(_formErrorPath) || pathInContext.equals(_formLoginPath));
    }

    /* ------------------------------------------------------------ */
    @Override
    public boolean secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, Authentication.User validatedUser) throws ServerAuthException
    {
        return true;
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected static class FormRequest extends HttpServletRequestWrapper
    {
        public FormRequest(HttpServletRequest request)
        {
            super(request);
        }

        @Override
        public long getDateHeader(String name)
        {
            if (name.toLowerCase(Locale.ENGLISH).startsWith("if-"))
                return -1;
            return super.getDateHeader(name);
        }

        @Override
        public String getHeader(String name)
        {
            if (name.toLowerCase(Locale.ENGLISH).startsWith("if-"))
                return null;
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames()
        {
            return Collections.enumeration(Collections.list(super.getHeaderNames()));
        }

        @Override
        public Enumeration<String> getHeaders(String name)
        {
            if (name.toLowerCase(Locale.ENGLISH).startsWith("if-"))
                return Collections.<String>enumeration(Collections.<String>emptyList());
            return super.getHeaders(name);
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected static class FormResponse extends HttpServletResponseWrapper
    {
        public FormResponse(HttpServletResponse response)
        {
            super(response);
        }

        @Override
        public void addDateHeader(String name, long date)
        {
            if (notIgnored(name))
                super.addDateHeader(name,date);
        }

        @Override
        public void addHeader(String name, String value)
        {
            if (notIgnored(name))
                super.addHeader(name,value);
        }

        @Override
        public void setDateHeader(String name, long date)
        {
            if (notIgnored(name))
                super.setDateHeader(name,date);
        }

        @Override
        public void setHeader(String name, String value)
        {
            if (notIgnored(name))
                super.setHeader(name,value);
        }

        private boolean notIgnored(String name)
        {
            if (HttpHeader.CACHE_CONTROL.is(name) ||
                    HttpHeader.PRAGMA.is(name) ||
                    HttpHeader.ETAG.is(name) ||
                    HttpHeader.EXPIRES.is(name) ||
                    HttpHeader.LAST_MODIFIED.is(name) ||
                    HttpHeader.AGE.is(name))
                return false;
            return true;
        }
    }

    /* ------------------------------------------------------------ */
    /** This Authentication represents a just completed Form authentication.
     * Subsequent requests from the same user are authenticated by the presents
     * of a {@link SessionAuthentication} instance in their session.
     */
    public static class FormAuthentication extends UserAuthentication implements Authentication.ResponseSent
    {
        public FormAuthentication(String method, UserIdentity userIdentity)
        {
            super(method,userIdentity);
        }

        @Override
        public String toString()
        {
            return "Form"+super.toString();
        }
    }
}
