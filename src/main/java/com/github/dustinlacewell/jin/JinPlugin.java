package com.github.dustinlacewell.jin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.bukkit.plugin.java.JavaPlugin;


@Singleton
public abstract class JinPlugin extends JavaPlugin {

    protected Injector injector;

    public void enabled() { }
    public void disabled() { }

    @Override
    public final void onEnable() {
        var logger = getLogger();
        var loader = new ModuleLoader(this);
        var modules = loader.loadModules(logger);
        this.injector = Guice.createInjector(modules);
        this.injector.injectMembers(this);
        this.enabled();
    }

    @Override
    public final void onDisable() {
        this.disabled();
    }
}
