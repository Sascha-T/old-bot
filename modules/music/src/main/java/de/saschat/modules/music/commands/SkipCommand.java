package de.saschat.modules.music.commands;

import de.saschat.bot.command.Command;
import de.saschat.modules.music.MusicModule;
import de.saschat.modules.music.Utilities;
import de.saschat.modules.music.management.TrackScheduler;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class SkipCommand implements Command {
    public static final CommandData DATA = new CommandData("skip", "Skips a song.").addOption(
        OptionType.INTEGER, "songs", "The amount of songs to skip.", false
    );

    MusicModule mod;
    public SkipCommand(MusicModule musicModule) {
        mod = musicModule;
    }

    @Override
    public CommandData getData() {
        return DATA;
    }

    @Override
    public void execute(SlashCommandEvent data) {

        if(!Utilities.isGuild(data)) {
            data.reply("You must use this command in a guild for it to work.").setEphemeral(true).queue();
            return;
        }
        data.deferReply().queue();
        int skip = 1;
        OptionMapping opt = data.getOption("songs");
        if(opt != null)
            skip = (int) opt.getAsLong();
        long id = data.getGuild().getIdLong();

        if(mod.manager.queue(id) == null) {
            data.getHook().sendMessage("The bot is not in a VC right now.").queue();
            return;
        }
        TrackScheduler.SkipResult skip1 = mod.manager.skip(id, skip);
        switch (skip1) {
            case NO_SONGS -> data.getHook().sendMessage("There are no tracks in the queue right now.").queue();
            case SKIPPED_WITH_OVERHEAD -> data.getHook().sendMessage("All tracks in the queue have successfully been skipped.").queue();
            case SKIPPED -> data.getHook().sendMessage("The track(s) have successfully been skipped.").queue();
        }
    }
}
