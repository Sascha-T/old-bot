package de.saschat.modules.music.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchMusicProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.saschat.bot.Bot;
import de.saschat.bot.command.Command;
import de.saschat.modules.music.MusicModule;
import de.saschat.modules.music.Utilities;
import de.saschat.modules.music.interactions.SearchInteraction;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public class SearchCommand implements Command {
    MusicModule module;

    public SearchCommand(MusicModule module) {
        this.module = module;
    }

    public final static CommandData DATA = new CommandData("search", "Searches for a track. (YouTube Music)").addOption(
        OptionType.STRING, "query", "The search query."
    );

    @Override
    public CommandData getData() {
        return DATA;
    }

    @Override
    public void execute(SlashCommandEvent data) {
        if (!Utilities.isGuild(data)) {
            data.reply("You must use this command in a guild for it to work.").setEphemeral(true).queue();
            return;
        }
        data.deferReply().queue();
        Member a = data.getMember();
        if(a.getVoiceState() == null || !a.getVoiceState().inVoiceChannel()) {
            data.reply("You must be in a voice chat to use this command.").setEphemeral(true).queue();
            return;
        }
        VoiceChannel channel = a.getVoiceState().getChannel();
        String query = data.getOption("query").getAsString();

        module.playerManager.loadItem("ytsearch:" + query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(!module.manager.inVC(data.getGuild().getIdLong()))
                    module.manager.join(channel);
                module.manager.play(channel, List.of(track));
                data.getHook().sendMessage("Adding track `" + track.getInfo().title + "` (" + track.getInfo().uri + ") to queue.").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(playlist.getTracks().size() > 1) {
                    SearchInteraction interaction = new SearchInteraction(playlist, module, channel);
                    Bot.instance.interactionManager.addInteraction(interaction);
                    interaction.start(data);
                } else
                    trackLoaded(playlist.getTracks().get(0));
            }

            @Override
            public void noMatches() {
                data.getHook().sendMessage("Tracks not found.").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                data.getHook().sendMessage("Failed to load track.").queue();
                exception.printStackTrace();
            }
        });
    }
}
