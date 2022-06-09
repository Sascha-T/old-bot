package de.saschat.modules.base;

import de.saschat.modules.base.commands.HelpCommand;
import de.saschat.modules.base.commands.ModuleCommand;
import de.saschat.modules.base.commands.PingCommand;
import de.saschat.bot.command.Command;
import de.saschat.bot.module.Module;
import net.dv8tion.jda.api.JDA;

import java.util.List;

public class BaseModule implements Module {
    public List<Command> COMMANDS = List.of(
        new PingCommand(),
        new HelpCommand(),
        new ModuleCommand()
    );

    @Override
    public void load(JDA jda) {}

    @Override
    public void unload() {}

    @Override
    public String getName() {
        return "base";
    }

    @Override
    public String getFriendlyName() {
        return "Basic Commands";
    }

    @Override
    public Command[] getCommands() {
        return COMMANDS.toArray(new Command[0]);
    }
}
