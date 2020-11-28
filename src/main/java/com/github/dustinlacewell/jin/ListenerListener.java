package com.github.dustinlacewell.jin;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

class ListenerListener<T extends JavaPlugin> implements TypeListener {
    private final T plugin;

    public ListenerListener(T plugin) {
        this.plugin = plugin;
    }

    public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
        Class<?> clazz = typeLiteral.getRawType();
        var isListener = Listener.class.isAssignableFrom(clazz);
        var isPlugin = JinPlugin.class.isAssignableFrom(clazz);

        if (isListener && !isPlugin) {
            this.plugin.getLogger().info(String.format("Registering listener %s", clazz.getName()));
            typeEncounter.register(new InjectionListener<I>() {
                @Override
                public void afterInjection(I injectee) {
                    plugin.getServer().getPluginManager().registerEvents((Listener) injectee, plugin);
                }
            });
        }
    }
}