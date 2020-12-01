package com.github.dustinlacewell.jin;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import io.github.classgraph.ClassGraph;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ModuleLoader {

    protected JinPlugin plugin;

    public ModuleLoader(JinPlugin plugin) {
        this.plugin = plugin;
    }

    protected Logger getLog() {
        return this.plugin.getLogger();
    }

    protected void logStackTrace(StackTraceElement[] elements) {
        for (var element : elements) {
            this.getLog().severe(element.toString());
        }
    }

    protected List<Module> getModules(String subpackage) {
        var abstractModuleName = AbstractModule.class.getCanonicalName();
        try (var result = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(subpackage)
                .scan()) {
            var subclasses = result.getSubclasses(abstractModuleName);
            var classes = subclasses.loadClasses(Module.class);
            var moduleConstructors = this.getConstructors(classes);
            return this.getModules(moduleConstructors);
        }
    }

    protected Constructor<?> getPluginConstructor(Class<? extends Module> classObj) {
        var pluginClass = this.plugin.getClass();
        var constructors = classObj.getConstructors();
        for (var c : constructors) {
            if (c.getParameterCount() == 1) {
                var paramTypes = c.getParameterTypes();
                if (paramTypes[0] == pluginClass) {
                    return c;
                }
            }
        }
        return null;
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

    protected Constructor<?> getConstructor(Class<? extends Module> classObj) {
        var pluginConstructor = this.getPluginConstructor(classObj);
        var defaultConstructor = this.getDefaultConstructor(classObj);
        return pluginConstructor != null ? pluginConstructor : defaultConstructor;
    }

    protected Stream<? extends Constructor<?>> getConstructors(List<Class<Module>> moduleClasses) {
        return moduleClasses
                .stream()
                .map(this::getConstructor)
                .filter(Objects::nonNull);
    }

    protected List<Module> getModules(Stream<? extends Constructor<?>> moduleConstructors) {
        return moduleConstructors.map(c -> {
            try {
                var paramCount = c.getParameterCount();
                if (paramCount == 0) {
                    return (Module) c.newInstance();
                } else if (paramCount == 1){
                    return (Module) c.newInstance(this.plugin);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                this.logStackTrace(e.getStackTrace());
                return null;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    protected void logModules(List<Module> modules) {
        var log = this.plugin.getLogger();
        for (var module : modules) {
            log.info(String.format("Loading module: %s", module.getClass().getName()));
        }
    }

    public List<Module> loadModules() {
        var subclass = this.plugin.getClass();
        var subpackage = subclass.getPackageName();
        var modules = this.getModules(subpackage);
        modules.add(new PluginModule(this.plugin));
        this.logModules(modules);
        return modules;
    }

}
