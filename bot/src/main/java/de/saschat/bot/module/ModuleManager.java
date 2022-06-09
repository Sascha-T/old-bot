package de.saschat.bot.module;

import de.saschat.bot.Bot;
import de.saschat.bot.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ModuleManager {
    public Logger logger = LoggerFactory.getLogger("ModuleManager");
    public CommandManager commandManager;
    public Map<String, ModuleWrapper> modules = new LinkedHashMap<>();
    public List<ModuleWrapper> loaded_modules = new ArrayList<>();

    public ModuleManager(CommandManager cmd) {
        this.commandManager = cmd;
    }

    public void addModule(ModuleWrapper... module) {
        for (ModuleWrapper wrapper: module) {
            if(Arrays.stream(Bot.instance.config.getDisabledModules()).anyMatch(a -> a.equals(wrapper.module.getName()))) {
                logger.debug("Loading module: " + wrapper.module.getFriendlyName() + " (" + wrapper.module.getName() + "v" + wrapper.version + ") [DISABLED]. ");
                addModule(false, wrapper);
            } else {
                logger.debug("Loading module: " + wrapper.module.getFriendlyName() + " (" + wrapper.module.getName() + "v" + wrapper.version + ").");
                addModule(true, wrapper);
            }
        }
    }
    public void addModule(boolean enable, ModuleWrapper... module) {
        for (ModuleWrapper a: module) {
            modules.put(a.module.getName(), a);
        }
        if(enable)
            loadModule(module);
    }

    public void loadModule(ModuleWrapper... module) {
        for (ModuleWrapper a : module) {
            loaded_modules.add(a);
            commandManager.addCommands(a.module.getCommands());
            a.module.load(commandManager.jda);
        }
    }

    public void unloadModule(ModuleWrapper... module) {
        for (ModuleWrapper a: module) {
            if(!loaded_modules.contains(a)) {
                logger.warn("Unloading module " + a.module.getName() + " failed: Not loaded.");
                continue;
            }
            a.module.unload();
            loaded_modules.remove(a);
            commandManager.removeCommands(a.module.getCommands());
            Bot.instance.config.addDisabledModule(a.module.getName());
        }
    }

    public void removeModules(ModuleWrapper... modules1) {
        unloadModule(modules1);
        for (ModuleWrapper a: modules1) {
            modules.remove(a);
        }
    }

    public Optional<ModuleWrapper> getModule(String name) {
        return modules.values().stream().filter(a -> a.module.getName().equals(name)).findFirst();
    }
    public Optional<ModuleWrapper> getModuleFriendly(String name) {
        return modules.values().stream().filter(a -> a.module.getFriendlyName().equals(name)).findFirst();
    }

    public boolean isLoaded(ModuleWrapper a) {
        return loaded_modules.contains(a);
    }


    public Collection<ModuleWrapper> getModules() {
        return modules.values();
    }
    public Collection<ModuleWrapper> getLoadedModules() {
        return loaded_modules;
    }

    public void loadCommands() {
        commandManager.finishLoad();
    }
}
