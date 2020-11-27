package com.ldlework.mc.jin;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ModuleLoader {

    protected JavaPlugin plugin;
    protected Logger log;

    public ModuleLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log = this.plugin.getLogger();
    }

    protected void logStackTrace(StackTraceElement[] elements) {
        for (var element : elements) {
            this.log.severe(element.toString());
        }
    }

    protected ClassInfoList getModules(String subpackage) {
        var abstractModuleName = AbstractModule.class.getCanonicalName();
        try (var result = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(subpackage)
                .scan()) {
            return result.getSubclasses(abstractModuleName);
        }
    }

    protected Constructor<?> getDefaultConstructor(Class<? extends Module> classObj) {
        var constructors = classObj.getConstructors();
        for (var c : constructors) {
            if (c.getParameterCount() == 0) {
                return c;
            }
        }
        return null;
    }

    protected Stream<? extends Constructor<?>> getConstructors(List<Class<Module>> moduleClasses) {
        return moduleClasses
                .stream()
                .map(this::getDefaultConstructor)
                .filter(Objects::nonNull);
    }

    protected List<Module> getModules(Stream<? extends Constructor<?>> moduleConstructors) {
        return moduleConstructors.map(c -> {
            try {
                return (Module) c.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                this.logStackTrace(e.getStackTrace());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<Module> loadModules(Logger log) {
        var subclass = this.getClass();
        var subpackage = subclass.getPackageName();
        var moduleInfos = this.getModules(subpackage);
        var moduleClasses = moduleInfos.loadClasses(Module.class);
        var moduleConstructors = this.getConstructors(moduleClasses);
        var modules = this.getModules(moduleConstructors);
        modules.add(new PluginModule(this.plugin));
        return modules;
    }

}
