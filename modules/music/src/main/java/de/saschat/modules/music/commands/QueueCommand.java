package de.saschat.modules.music.commands;

import de.saschat.bot.Bot;
import de.saschat.bot.command.Command;
import de.saschat.modules.music.MusicModule;
import de.saschat.modules.music.Utilities;
import de.saschat.modules.music.interactions.QueueInteraction;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class QueueCommand implements Command {
    public static final CommandData DATA = new CommandData("queue", "Get current queue.");

    MusicModule module;
    public QueueCommand(MusicModule musicModule) {
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
        data.deferReply().queue();
        if(!module.manager.inVC(data.getGuild().getIdLong())) {
            data.getHook().sendMessage("The bot is not in a VC right now.").queue();
        } else {
            QueueInteraction interaction = new QueueInteraction(module);
            interaction.start(data);
            Bot.instance.interactionManager.addInteraction(interaction);
        }
    }
}
