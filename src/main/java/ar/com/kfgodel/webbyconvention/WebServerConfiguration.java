package ar.com.kfgodel.webbyconvention;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.List;
import java.util.function.Consumer;

/**
 * This type represents the configuration used by a web-by-convention server to setup 
 * its behavior
 *
 * Created by kfgodel on 03/03/15.
 */
public interface WebServerConfiguration {
    /**
     * @return The port to start listening for incoming http connections
     */
    int getHttpPort();

    /**
     * @return The list of folders with resources that may change during development. 
     * The server will serve the files directly allowing updates to be refreshed
     */
    List<String> getRefreshableContent();

    /**
     * @return The classpath folder that holds all the web content
     */
    String getWebFolderInClasspath();

    /**
     * @return The package in the classpath that holds jersey annotated resources to expose as api
     */
    String getApiResourcesPackage();

    /**
     * @return The code to setup injection bindings for the jersey resources
     */
    Consumer<AbstractBinder> getInjectionConfiguration();
}
