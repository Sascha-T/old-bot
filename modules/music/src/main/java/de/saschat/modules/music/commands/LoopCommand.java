package de.saschat.modules.music.commands;

import de.saschat.bot.command.Command;
import de.saschat.modules.music.MusicModule;
import de.saschat.modules.music.Utilities;
import de.saschat.modules.music.management.TrackScheduler;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

public class LoopCommand implements Command {
    public static final CommandData DATA = new CommandData("loop", "Gets loop status.").addSubcommandGroups(
        new SubcommandGroupData("options", "Loop options").addSubcommands(
            new SubcommandData("off", "Turns looping off."),
            new SubcommandData("on", "Loops entire queue."),
            new SubcommandData("one", "Loops current song.")
        )
    );

    @Override
    public CommandData getData() {
        return DATA;
    }

    MusicModule module;

    public LoopCommand(MusicModule module) {
        this.module = module;
    }

    @Override
    public void execute(SlashCommandEvent data) {
        if(!Utilities.isGuild(data)) {
            data.reply("You must use this command in a guild for it to work.").setEphemeral(true).queue();
            return;
        }
        data.deferReply().queue();

        if (!module.manager.inVC(data.getGuild().getIdLong())) {
            data.getHook().sendMessage("The bot is not in a VC right now.").queue();
        } else {
            if (data.getSubcommandName() == null) {
                switch (module.manager.loop(data.getGuild().getIdLong())) {
                    case OFF -> data.getHook().sendMessage("The queue is not being looped right now.").queue();
                    case ON -> data.getHook().sendMessage("The queue is being looped entirely right now.").queue();
                    case ON_ONE -> data.getHook().sendMessage("A single track is being looped right now.").queue();
                }
            } else {
                switch (data.getSubcommandName()) {
                    case "off" -> {
                        data.getHook().sendMessage("Looping has been disabled.").queue();
                        module.manager.loop(data.getGuild().getIdLong(), TrackScheduler.LoopStatus.OFF);
                    }
                    case "on" -> {
                        data.getHook().sendMessage("Entire queue looping has been enabled.").queue();
                        module.manager.loop(data.getGuild().getIdLong(), TrackScheduler.LoopStatus.ON);
                    }
                    case "one" -> {
                        data.getHook().sendMessage("Single track looping has been enabled.").queue();
                        module.manager.loop(data.getGuild().getIdLong(), TrackScheduler.LoopStatus.ON_ONE);
                    }
                }
            }
        }
    }
}
