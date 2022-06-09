package de.saschat.bot.command;

import de.saschat.bot.util.CommandCompare;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface Command {
    CommandData getData();

    void execute(SlashCommandEvent data);

    default boolean matches(net.dv8tion.jda.api.interactions.commands.Command command) {
        return CommandCompare.compare_command(command, getData());
    }
}
