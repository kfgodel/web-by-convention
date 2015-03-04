package ar.com.kfgodel.webbyconvention;

import com.google.common.collect.Lists;

import java.util.List;

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

    public static DefaultConfiguration create() {
        DefaultConfiguration config = new DefaultConfiguration();
        return config;
    }

    /**
     * Changes the default port
     * @param newHttpPort New port number
     * @return Ths config to chain calls
     */
    public WebServerConfiguration listeningHttpOn(int newHttpPort) {
        this.httpPort = newHttpPort;
        return this;
    }

    /**
     * Changes the default refreshable folders
     * @param newContent The list of folders to look for changes
     * @return This instance for call chaining
     */
    public WebServerConfiguration withRefreshableContentIn(List<String> newContent){
        this.refreshableContent = newContent;
        return this;
    }

    /**
     * Changes the default folder in the classpath that holds all the static web content
     * @param newFolder The new location to look into the classpath
     * @return This instance to chain method calls
     */
    public WebServerConfiguration usingClasspathWebFolder(String newFolder){
        this.webFolderInClassPath = newFolder;
        return this;
    }

    /**
     * Changes the default location in the classpath were annotated resource class are
     * @param annotatedResourcesPackage The package were jersey api resource classes are
     * @return This instance to chain calls
     */
    public WebServerConfiguration withResourcesFrom(String annotatedResourcesPackage){
        this.apiResourcesPackage = annotatedResourcesPackage;
        return this;
    }
}
