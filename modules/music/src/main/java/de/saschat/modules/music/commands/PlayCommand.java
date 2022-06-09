package de.saschat.modules.music.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.saschat.bot.command.Command;
import de.saschat.modules.music.MusicModule;
import de.saschat.modules.music.Utilities;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public class PlayCommand implements Command {
    public static final CommandData DATA = new CommandData("play", "Play the piece of music.").addOption(OptionType.STRING, "id", "Music Identifier.", true);
    public MusicModule module;

    public PlayCommand(MusicModule musicModule) {
        this.module = musicModule;
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
        String name = data.getOptions().stream().filter(a -> a.getName().equals("id")).findFirst().get().getAsString();
        Member a = data.getMember();
        if(!Utilities.isGuild(data)) {
            data.reply("You must use this command in a guild for it to work.").setEphemeral(true).queue();
            return;
        }
        if(a.getVoiceState() == null || !a.getVoiceState().inVoiceChannel()) {
            data.reply("You must be in a voice chat to use this command.").setEphemeral(true).queue();
            return;
        }
        VoiceChannel channel = a.getVoiceState().getChannel();
        data.deferReply().queue();
        module.playerManager.loadItem(name, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                data.getHook().sendMessage("Adding track `" + track.getInfo().title + "` (" + track.getInfo().uri + ") to queue.").queue();
                addTracks(List.of(track));
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                data.getHook().sendMessage("Adding playlist `" + playlist.getName() + "` to queue.").queue();
                addTracks(playlist.getTracks());
            }

            public void addTracks(List<AudioTrack> track) {
                module.manager.join(channel);
                module.manager.play(channel, track);
            }

            @Override
            public void noMatches() {
                data.getHook().sendMessage("Track not found.").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                data.getHook().sendMessage("Failed to load track.").queue();
                exception.printStackTrace();
            }
        });
    }
}
