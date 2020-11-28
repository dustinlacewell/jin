package com.github.dustinlacewell.jin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public abstract class JinPlugin extends JavaPlugin implements Listener {

    protected Injector injector;

    public void enabled() { }
    public void disabled() { }

    protected List<Module> loadModules() {
        var loader = new ModuleLoader(this);
        return loader.loadModules();
    }

    @Override
    public final void onEnable() {
        try {
            var modules = this.loadModules();
            this.injector = Guice.createInjector(modules);
            this.enabled();
        } catch (Exception e) {
            var log = this.getLogger();
            log.severe("Unrecoverable error:");
            log.severe(e.getMessage());
            log.severe(e.getCause().toString());
            for (var s : e.getStackTrace()) {
                log.severe(s.toString());
            }
            this.setEnabled(false);
        }
    }

    @Override
    public final void onDisable() {
        this.injector = null;
        this.disabled();
    }
}
