package de.saschat.modules.base.commands;

import de.saschat.bot.command.Command;
import de.saschat.bot.util.Colors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class PingCommand implements Command {
    public static final CommandData data = new CommandData("ping", "Gets the bot's ping to Discord Gateway and API");

    @Override
    public CommandData getData() {
        return data;
    }

    @Override
    public void execute(SlashCommandEvent data) {
        data.deferReply().queue();
        JDA jda = data.getJDA();
        long gt = jda.getGatewayPing();
        jda.getRestPing().queue((time) -> {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Colors.POSITIVE); // Material Green 800
            builder.setDescription("Gateway Ping: `" + gt + "`ms.\nAPI Ping: `" + time + "`ms.");
            data.getHook().sendMessageEmbeds(builder.build()).setEphemeral(true).queue();
        });
    }
}
