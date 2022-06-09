package de.saschat.modules.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudDataReader;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudHtmlDataLoader;
import de.saschat.bot.command.Command;
import de.saschat.bot.module.Module;
import de.saschat.modules.music.commands.*;
import de.saschat.modules.music.management.ConnectionManager;
import de.saschat.modules.music.management.MusicDiscordEvents;
import net.dv8tion.jda.api.JDA;

import java.util.List;

public class MusicModule implements Module {
    // @TODO: MUSIC: Rewrite this entire mess of a module, like damn, this is some hot garbage.
    // @TODO: MUSIC: Save queue to file.
    // @TODO: GENERAL: Add guild-only, dm-only, permission-only, etc. commands.
    public List<Command> COMMANDS = List.of(
        new JoinCommand(this),
        new PlayCommand(this),
        new QueueCommand(this),
        new SkipCommand(this),
        new ShuffleCommand(this),
        new LoopCommand(this),
        new PlayerCommand(this),
        new SearchCommand(this),
        new LeaveCommand(this)
    );

    public ConnectionManager manager;
    public AudioPlayerManager playerManager;
    public MusicDiscordEvents events;
    public JDA jda;

    @Override
    public void load(JDA jda) {
        this.jda = jda;
        this.events = new MusicDiscordEvents(this);

        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);

        manager = new ConnectionManager(this);
        manager.start();
        jda.addEventListener(events);
    }

    @Override
    public void unload() {
        jda.removeEventListener(events);
        manager.stop();
    }

    @Override
    public String getName() {
        return "music";
    }

    @Override
    public String getFriendlyName() {
        return "Music";
    }

    @Override
    public Command[] getCommands() {
        return COMMANDS.toArray(new Command[0]);
    }
}
