package de.saschat.bot.module;

import de.saschat.bot.command.Command;
import net.dv8tion.jda.api.JDA;

public interface Module {
    void load(JDA jda);
    void unload();
    String getName();
    String getFriendlyName();
    Command[] getCommands();
}
