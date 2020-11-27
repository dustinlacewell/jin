package com.ldlework.mc.jin;

import com.google.inject.AbstractModule;
import org.bukkit.plugin.java.JavaPlugin;

class PluginModule extends AbstractModule {
    private final JavaPlugin plugin;

    public PluginModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void configure() {
        bind(JavaPlugin.class).toInstance(this.plugin);
    }
}
