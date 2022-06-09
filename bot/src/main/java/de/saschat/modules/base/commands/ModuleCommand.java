package de.saschat.modules.base.commands;

import de.saschat.bot.Bot;
import de.saschat.bot.command.Command;
import de.saschat.bot.module.ModuleWrapper;
import de.saschat.bot.util.Colors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class ModuleCommand implements Command {
    public static final CommandData DATA = new CommandData("module", "Manages the bot's loaded modules.").addSubcommands(
        new SubcommandData("unload", "Unloads a module").addOption(
            OptionType.STRING, "name", "Module name", true
        ),
        new SubcommandData("reload", "Reloads a module.").addOption(
            OptionType.STRING, "name", "Module name", true
        ).addOption(
            OptionType.BOOLEAN, "hard", "Reload module from disk?", true
        ),
        new SubcommandData("load", "Loads a module").addOption(
            OptionType.STRING, "name", "Module name", true
        ),
        new SubcommandData("info", "Prints the currently loaded and unloaded modules.")
    );

    @Override
    public CommandData getData() {
        return DATA;
    }

    public Optional<ModuleWrapper> getModule(String name) {
        Optional<ModuleWrapper> module = Bot.instance.moduleManager.getModule(name);
        if (!module.isPresent())
            return Bot.instance.moduleManager.getModuleFriendly(name);
        return module;
    }

    @Override
    public void execute(SlashCommandEvent data) {
        if (Bot.instance.isAdmin(data.getMember().getIdLong())) {
            switch (data.getSubcommandName()) {
                case "unload" -> {
                    // dup1
                    String name = data.getOptions().stream().filter(a -> a.getName().equals("name")).findFirst().get().getAsString();
                    Optional<ModuleWrapper> module = getModule(name);
                    if (!module.isPresent()) {
                        data.replyEmbeds(new EmbedBuilder().setColor(Colors.NEGATIVE).setDescription("Module not found.").build()).queue();
                        break;
                    }
                    if (!Bot.instance.moduleManager.isLoaded(module.get())) {
                        data.replyEmbeds(new EmbedBuilder().setColor(Colors.NEGATIVE).setDescription("Module not loaded.").build()).queue();
                        break;
                    }
                    // uniq
                    data.replyEmbeds(new EmbedBuilder().setColor(Colors.POSITIVE).setDescription("Module successfully unloaded.").build()).queue();
                    Bot.instance.moduleManager.unloadModule(module.get());
                    Bot.instance.moduleManager.loadCommands();
                }
                case "load" -> {
                    // dup2
                    String name = data.getOptions().stream().filter(a -> a.getName().equals("name")).findFirst().get().getAsString();
                    Optional<ModuleWrapper> module = getModule(name);
                    if (!module.isPresent()) {
                        data.replyEmbeds(new EmbedBuilder().setColor(Colors.NEGATIVE).setDescription("Module not found.").build()).queue();
                        break;
                    }
                    if (Bot.instance.moduleManager.isLoaded(module.get())) {
                        data.replyEmbeds(new EmbedBuilder().setColor(Colors.NEGATIVE).setDescription("Module already loaded.").build()).queue();
                        break;
                    }
                    // uniq
                    data.replyEmbeds(new EmbedBuilder().setColor(Colors.POSITIVE).setDescription("Module successfully loaded.").build()).queue();
                    Bot.instance.moduleManager.loadModule(module.get());
                    Bot.instance.moduleManager.loadCommands();
                }
                case "reload" -> {
                    String name = data.getOptions().stream().filter(a -> a.getName().equals("name")).findFirst().get().getAsString();
                    boolean hard = data.getOptions().stream().filter(a -> a.getName().equals("hard")).findFirst().get().getAsBoolean();
                    Optional<ModuleWrapper> module1 = getModule(name);
                    if (!module1.isPresent()) {
                        data.replyEmbeds(new EmbedBuilder().setColor(Colors.NEGATIVE).setDescription("Module not found.").build()).queue();
                        break;
                    }
                    ModuleWrapper module = module1.get();
                    if (!hard) {
                        if (Bot.instance.moduleManager.isLoaded(module))
                            Bot.instance.moduleManager.unloadModule(module);
                        Bot.instance.moduleManager.loadModule(module);
                        Bot.instance.moduleManager.loadCommands();
                        data.replyEmbeds(new EmbedBuilder().setColor(Colors.POSITIVE).setDescription("Module successfully soft-reloaded.").build()).queue();
                    } else {
                        if (module.type == ModuleWrapper.SourceType.BUILTIN) {
                            data.replyEmbeds(new EmbedBuilder().setColor(Colors.NEGATIVE).setDescription("Cannot hard-reload builtin module.").build()).queue();
                            break;
                        }
                        Bot.instance.moduleManager.removeModules(module);
                        if (Bot.instance.jarManager.reloadJar(module))
                            data.replyEmbeds(new EmbedBuilder().setColor(Colors.POSITIVE).setDescription("Module successfully hard-reloaded. Be aware this may have reloaded other modules aswell.").build()).queue();
                        else
                            data.replyEmbeds(new EmbedBuilder().setColor(Colors.NEGATIVE).setDescription("Failed to hard-reload module.").build()).queue();
                        Bot.instance.moduleManager.loadCommands();
                    }
                }
                case "info" -> {
                    data.deferReply().queue();
                    List<MessageEmbed> embeds = new LinkedList<>();

                    int max_len = 3000;
                    int len = 0;
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("Loaded Modules.");
                    Path cwd = new File(".").toPath().toAbsolutePath();
                    for (Map.Entry<String, ModuleWrapper> entry : Bot.instance.moduleManager.modules.entrySet()) {
                        ModuleWrapper wrapper = entry.getValue();
                        StringBuilder toAppend = new StringBuilder();
                        toAppend.append(wrapper.module.getFriendlyName())
                            .append(" (*").append(wrapper.module.getName()).append("-v").append(wrapper.version).append("*)");
                        if(Bot.instance.moduleManager.loaded_modules.contains(wrapper))
                            toAppend.append(" [LOADED]");
                        switch (wrapper.type) {
                            case ADDON -> toAppend.append(" loaded from ").append("[").append(cwd.relativize(Path.of(wrapper.source)).toString()).append("](https://example.org)");
                            case BUILTIN -> toAppend.append(" is built-in.");
                        }
                        toAppend.append("\n");
                        String append = toAppend.toString();
                        if(append.length() + len > max_len) {
                            embeds.add(builder.build());
                            builder = new EmbedBuilder();
                            builder.setTitle("Loaded Modules.");
                            len = 0;
                        }
                        builder.appendDescription(toAppend);
                        len += append.length();
                    }
                    embeds.add(builder.build());
                    for (int i = 0; i < embeds.size(); i++) {
                        EmbedBuilder builder2 = new EmbedBuilder(embeds.get(i));
                        builder2.setFooter("Page " + (i + 1) + " of " + embeds.size());
                        embeds.set(i, builder2.build());
                    }
                    data.getHook().sendMessageEmbeds(embeds).queue();
                }
                default -> {
                    data.replyEmbeds(new EmbedBuilder().setColor(0xc62828).setDescription("Invalid command?").build()).queue();
                    System.out.println("discord got haxed?");
                }
            }
        } else {
            data.reply("You are not authorized to use this command.").setEphemeral(true).queue();
        }
    }
}
