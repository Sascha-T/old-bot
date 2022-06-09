package de.saschat.bot;

import de.saschat.bot.module.JarManager;
import de.saschat.bot.module.ModuleWrapper;
import de.saschat.modules.base.BaseModule;
import de.saschat.bot.command.CommandManager;
import de.saschat.bot.interaction.InteractionManager;
import de.saschat.bot.module.ModuleManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class Bot {
    public Config config;
    public JDA bot;

    public CommandManager commandManager;
    public ModuleManager moduleManager;
    public InteractionManager interactionManager;
    public JarManager jarManager;
    public Logger logger = LoggerFactory.getLogger("Bot");

    public Bot() throws IOException {
        config = new Config();
    }

    public void start() throws Exception {
        if (bot != null)
            throw new RuntimeException("Bot already constructed.");
        instance = this;
        bot = JDABuilder.createDefault(config.getToken()).build();
        if (config.getDebug())
            logger.warn("Debug mode is enabled. No commands will be registered by default.");
        commandManager = new CommandManager(bot);
        interactionManager = new InteractionManager(bot);
        moduleManager = new ModuleManager(commandManager);
        jarManager = new JarManager(moduleManager);

        moduleManager.addModule(new ModuleWrapper(new BaseModule()));
        jarManager.addFolder(config.getModulesFolder());
        Arrays.stream(config.getForceLoadedModules()).forEach(jarManager::addGeneric);

        moduleManager.loadCommands();

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    public class ShutdownHook extends Thread {
        @Override
        public void run() {
            moduleManager.unloadModule(moduleManager.modules.values().stream().toList().toArray(new ModuleWrapper[0]));
            bot.shutdownNow();
        }
    }

    public static Bot instance;

    public boolean isAdmin(long a) {
        return config.getAdmins().stream().anyMatch(b -> a == b);
    }
}
