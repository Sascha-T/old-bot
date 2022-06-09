package de.saschat.modules.base.commands;

import de.saschat.modules.base.interactions.HelpInteraction;
import de.saschat.bot.Bot;
import de.saschat.bot.command.Command;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class HelpCommand implements Command {
    public static final CommandData DATA = new CommandData("help", "Get information on the bot's currently loaded commands and modules.");

    @Override
    public CommandData getData() {
        return DATA;
    }

    @Override
    public void execute(SlashCommandEvent data) {
        /*for (Module module: Bot.instance.moduleManager.getModules()) {
            List<String> lines = new LinkedList<>();
        }*/
        HelpInteraction interaction = new HelpInteraction();
        interaction.start(data);
        Bot.instance.interactionManager.addInteraction(interaction);
    }
}
