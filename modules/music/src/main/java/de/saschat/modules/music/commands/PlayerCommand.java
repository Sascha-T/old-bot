package de.saschat.modules.music.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.saschat.bot.command.Command;
import de.saschat.bot.util.Colors;
import de.saschat.modules.music.MusicModule;
import de.saschat.modules.music.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlayerCommand implements Command {
    public static final CommandData DATA = new CommandData("player", "Pausing, seeking, etc.").addSubcommands(
        new SubcommandData("pause", "Pauses the player."),
        new SubcommandData("unpause", "Unpauses the player."),
        new SubcommandData("status", "Gets player status."),
        new SubcommandData("seek", "Seeks the player.").addOption(
            OptionType.INTEGER, "offset", "Offset in seconds.", true
        )
    );

    MusicModule module;

    public PlayerCommand(MusicModule musicModule) {
        this.module = musicModule;
    }

    @Override
    public CommandData getData() {
        return DATA;
    }

    public String icon(boolean paused, boolean playing) {
        if (!playing)
            return ":stop_button:";
        if (paused)
            return ":pause_button:";
        return ":arrow_forward:";
    }

    @Override
    public void execute(SlashCommandEvent data) {
        if (!Utilities.isGuild(data)) {
            data.reply("You must use this command in a guild for it to work.").setEphemeral(true).queue();
            return;
        }
        data.deferReply().queue();
        long id = data.getGuild().getIdLong();
        switch (data.getSubcommandName()) {
            case "pause" -> {
                if (module.manager.pause(id))
                    data.getHook().sendMessage("Successfully paused the player. The player will disconnect in 5 minutes of idle time.").queue();
                else
                    data.getHook().sendMessage("Failed to pause the player.").queue();
            }
            case "resume" -> {
                if (module.manager.resume(id))
                    data.getHook().sendMessage("Successfully resumed the player.").queue();
                else
                    data.getHook().sendMessage("Failed to resume the player.").queue();
            }
            case "status" -> {
                boolean playing = module.manager.playing(id);
                boolean paused = module.manager.paused(id);
                long pos = module.manager.pos(id) / 1000;
                long len = module.manager.len(id) / 1000;
                AudioTrack track = module.manager.track(id);

                String bar[] = (Character.toString(0x25AC).repeat(12)).split("");
                if (playing) {
                    double percentage = (double) pos / len;
                    int segment = Math.max(((int) Math.ceil(percentage * 13)) - 1, 0);
                    bar[segment] = ":radio_button:";
                }
                String ret = icon(paused, playing) + Arrays.stream(bar).collect(Collectors.joining());
                ret += " `[" + format(pos) + "/" + format(len) + "]`";

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Colors.NEUTRAL);
                embedBuilder.setDescription(ret);
                if (playing) {
                    embedBuilder.setAuthor("Now playing...");
                    embedBuilder.setTitle(track.getInfo().title, track.getInfo().uri);
                    embedBuilder.setFooter("Author: " + track.getInfo().author);
                } else
                    embedBuilder.setTitle("Currently not playing anything.");
                data.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
            }
            case "seek" -> {
                long seek = (long) data.getOption("offset").getAsLong();
                boolean seeked = module.manager.seek(data.getGuild().getIdLong(), seek);
                if (seeked)
                    data.getHook().sendMessage("Successfully seeked to `" + seek + "`s.").queue();
                else data.getHook().sendMessage("Failed to seek.").queue();
            }
        }
    }

    public String format(long secs) {
        long min = (long) Math.floor((double) secs / 60);
        long sec = secs % 60;
        return String.format("%02d:%02d", min, sec);
    }
}
