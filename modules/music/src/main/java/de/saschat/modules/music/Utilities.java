package de.saschat.modules.music;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class Utilities {
    public static boolean isGuild(SlashCommandEvent event) {
        return event.getGuild() != null;
    }
}
