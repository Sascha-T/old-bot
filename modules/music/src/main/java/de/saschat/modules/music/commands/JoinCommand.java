package de.saschat.modules.music.commands;

import de.saschat.bot.command.Command;
import de.saschat.modules.music.MusicModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class JoinCommand implements Command {
    public static final CommandData DATA = new CommandData("join", "Joins the current VC the user is in.");
    public MusicModule module;

    public JoinCommand(MusicModule musicModule) {
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
        if(a.getVoiceState() == null || !a.getVoiceState().inVoiceChannel()) {
            data.reply("You must be in a voice chat to use this command.").setEphemeral(true).queue();
            return;
        }
        module.manager.join(a.getVoiceState().getChannel());
        data.reply("Joining voice chat...").queue();
    }
}
