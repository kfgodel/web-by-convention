package ar.com.kfgodel.webbyconvention;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.function.Consumer;

/**
 * This type represents the configuration of injection dependency for jersey h2k container.<br>
 *     It uses the configuration code, to configure bindings at the correct moment
 * Created by kfgodel on 22/03/15.
 */
public class ConfigurableInjectionBinder extends AbstractBinder {

    private Consumer<AbstractBinder> configurationCode;

    @Override
    protected void configure() {
        configurationCode.accept(this);
    }

    public static ConfigurableInjectionBinder create(Consumer<AbstractBinder> configurationCode) {
        ConfigurableInjectionBinder binder = new ConfigurableInjectionBinder();
        binder.configurationCode = configurationCode;
        return binder;
    }

}
