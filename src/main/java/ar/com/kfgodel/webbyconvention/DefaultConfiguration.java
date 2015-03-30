package ar.com.kfgodel.webbyconvention;

import ar.com.kfgodel.webbyconvention.auth.api.WebCredential;
import com.google.common.collect.Lists;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This type represents the defaul configuration with sensitive values for all the parameters.
 * You will usually use this as is
 * Created by kfgodel on 03/03/15.
 */
public class DefaultConfiguration implements WebServerConfiguration {
    
    private int httpPort = 80;
    
    private List<String> refreshableContent = Lists.newArrayList("src/main/html/web",
            "src/main/javascript/web");
    
    private String webFolderInClassPath = "/web";
    
    private String apiResourcesPackage = "web.api.resources";

    private Consumer<AbstractBinder> injectionConfiguration = this::noBinding;

    private Function<WebCredential, Optional<Object>> authenticatorFunction = this::authenticateAll;

    private Optional<Object> authenticateAll(WebCredential webCredential) {
        // We allow access to every login attempt
        return Optional.of(true);
    }

    private void noBinding(AbstractBinder abstractBinder) {
        // By default there are no bindings
    }

    @Override
    public int getHttpPort() {
        return httpPort;
    }

    @Override
    public List<String> getRefreshableContent() {
        return refreshableContent;
    }

    @Override
    public String getWebFolderInClasspath() {
        return webFolderInClassPath;
    }

    @Override
    public String getApiResourcesPackage() {
        return apiResourcesPackage;
    }

    @Override
    public Consumer<AbstractBinder> getInjectionConfiguration() {
        return injectionConfiguration;
    }

    @Override
    public Function<WebCredential, Optional<Object>> getAuthenticatorFunction() {
        return authenticatorFunction;
    }

    public static DefaultConfiguration create() {
        DefaultConfiguration config = new DefaultConfiguration();
        return config;
    }

    /**
     * Changes the default port
     * @param newHttpPort New port number
     * @return Ths config to chain calls
     */
    public DefaultConfiguration listeningHttpOn(int newHttpPort) {
        this.httpPort = newHttpPort;
        return this;
    }

    /**
     * Changes the default refreshable folders
     * @param newContent The list of folders to look for changes
     * @return This instance for call chaining
     */
    public DefaultConfiguration withRefreshableContentIn(List<String> newContent){
        this.refreshableContent = newContent;
        return this;
    }

    /**
     * Changes the default folder in the classpath that holds all the static web content
     * @param newFolder The new location to look into the classpath
     * @return This instance to chain method calls
     */
    public DefaultConfiguration usingClasspathWebFolder(String newFolder){
        this.webFolderInClassPath = newFolder;
        return this;
    }

    /**
     * Changes the default location in the classpath were annotated resource class are
     * @param annotatedResourcesPackage The package were jersey api resource classes are
     * @return This instance to chain calls
     */
    public DefaultConfiguration withResourcesFrom(String annotatedResourcesPackage){
        this.apiResourcesPackage = annotatedResourcesPackage;
        return this;
    }

    /**
     * Changes the default binding of injected instances for resources
     * @param binderConfig The binder code to configure the binder instance
     * @return This instance for method chaining
     */
    public DefaultConfiguration withInjections(Consumer<AbstractBinder> binderConfig){
        this.injectionConfiguration = binderConfig;
        return this;
    }

    /**
     * Changes the default "allow all" authentication to a custom app authenticator function.<br>
     *     The function will be called for every authentication attempt, and the non empty returned object
     *     will be used as the user id, available in a thread context through the WebAuthenticatedId class
     * @param authenticationFunction The authentication function
     * @return This isntance to allow method chaining
     */
    public WebServerConfiguration authenticatingWith(Function<WebCredential, Optional<Object>> authenticationFunction) {
        this.authenticatorFunction = authenticationFunction;
        return this;
    }
}

