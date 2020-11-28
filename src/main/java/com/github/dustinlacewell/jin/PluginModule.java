package com.github.dustinlacewell.jin;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;

class PluginModule<T extends JinPlugin> extends AbstractModule {
    private final T plugin;

    public PluginModule(T plugin) {
        this.plugin = plugin;
    }

    @Override
    public void configure() {
        var clazz = (Class<T>) plugin.getClass();
        bind(clazz).toInstance(this.plugin);
        bind(PaperPluginLogger.class).toInstance((PaperPluginLogger) this.plugin.getLogger());
        bind(FileConfiguration.class).toInstance(this.plugin.getConfig());

        var server = this.plugin.getServer();
        bind(Server.class).toInstance(server);
        bind(BukkitScheduler.class).toInstance(server.getScheduler());
        bind(ScoreboardManager.class).toInstance(server.getScoreboardManager());

        var pluginManager = server.getPluginManager();
        pluginManager.registerEvents(this.plugin, this.plugin);
        bind(PluginManager.class).toInstance(pluginManager);

        var listener = new ListenerListener<T>(plugin);
        bindListener(Matchers.any(), listener);
    }
}
