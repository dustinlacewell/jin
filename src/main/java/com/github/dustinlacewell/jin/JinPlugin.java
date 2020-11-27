package com.github.dustinlacewell.jin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


@Singleton
public abstract class JinPlugin extends JavaPlugin {

    protected Injector injector;

    public void enabled() { }
    public void disabled() { }

    protected List<Module> loadModules() {
        var logger = getLogger();
        var loader = new ModuleLoader(this);
        return loader.loadModules(logger);
    }

    @Override
    public final void onEnable() {
        var modules = this.loadModules();
        this.injector = Guice.createInjector(modules);
        this.injector.injectMembers(this);
        this.enabled();
    }

    @Override
    public final void onDisable() {
        this.disabled();
    }
}
