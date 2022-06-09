package de.saschat.modules.music.management;

import de.saschat.modules.music.MusicModule;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MusicDiscordEvents extends ListenerAdapter {
    MusicModule module;
    public MusicDiscordEvents(MusicModule musicModule) {
        this.module = musicModule;
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if(event.getMember().getId().equals(event.getJDA().getSelfUser().getId()))
            module.manager.leave(event.getGuild().getIdLong());
    }
}
