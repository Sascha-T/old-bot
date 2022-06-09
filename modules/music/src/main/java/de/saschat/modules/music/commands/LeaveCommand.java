package de.saschat.modules.music.commands;

import de.saschat.bot.command.Command;
import de.saschat.modules.music.MusicModule;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class LeaveCommand implements Command {
    public static final CommandData DATA = new CommandData("leave", "Disconnects from the current VC.");
    public MusicModule module;

    public LeaveCommand(MusicModule musicModule) {
        this.module = musicModule;
    }


    @Override
    public CommandData getData() {
        return DATA;
    }

    @Override
    public void execute(SlashCommandEvent data) {
        Member a = data.getMember();
        if(a == null) {
            data.reply("You must use this command in a guild for it to work.").setEphemeral(true).queue();
            return;
        }
        if(module.manager.inVC(a.getGuild().getIdLong())) {
            data.reply("Disconnected.").queue();
            module.manager.leave(a.getGuild().getIdLong());
        } else {
            data.reply("The bot is not in a VC.").queue();
        }
    }
}
