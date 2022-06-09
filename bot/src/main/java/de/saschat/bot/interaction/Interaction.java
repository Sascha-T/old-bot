package de.saschat.bot.interaction;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.Timer;
import java.util.UUID;

public interface Interaction {
    UUID getId();

    long expiryTime();
    long lastInteraction();

    void onButtonClick(ButtonClickEvent event);
    void onSelectionMenu(SelectionMenuEvent event);

    void start(GenericInteractionCreateEvent event);
    void expire();
}
