package de.saschat.modules.music.commands;

import de.saschat.bot.command.Command;
import de.saschat.modules.music.MusicModule;
import de.saschat.modules.music.Utilities;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class ShuffleCommand implements Command {
    public static final CommandData DATA = new CommandData("shuffle", "Shuffles the queue.");

    @Override
    public CommandData getData() {
        return DATA;
    }

    MusicModule module;

    public ShuffleCommand(MusicModule module) {
        this.module = module;
    }

    @Override
    public void execute(SlashCommandEvent data) {

        if(!Utilities.isGuild(data)) {
            data.reply("You must use this command in a guild for it to work.").setEphemeral(true).queue();
            return;
        }
        data.deferReply().queue();
        if (!module.manager.inVC(data.getGuild().getIdLong())) {
            data.getHook().sendMessage("The bot is not in a VC right now.").queue();
        } else {
            if (module.manager.shuffle(data.getGuild().getIdLong()))
                data.getHook().sendMessage("Successfully shuffled queue.").queue();
            else
                data.getHook().sendMessage("The queue is empty.").queue();
        }
    }
}
