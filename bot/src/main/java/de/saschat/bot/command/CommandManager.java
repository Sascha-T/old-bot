package de.saschat.bot.command;

import java.util.*;

import de.saschat.bot.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class CommandManager extends ListenerAdapter {
    public List<Command> commands = new LinkedList<>();
    public String prefix = ">>";
    public JDA jda;
    public List<net.dv8tion.jda.api.interactions.commands.Command> jdaCommands;
    public HashMap<Command, net.dv8tion.jda.api.interactions.commands.Command> loadedCommands;

    public CommandManager(JDA jda) {
        this.jda = jda;
        jda.addEventListener(this);

        jdaCommands = jda.retrieveCommands().complete();
    }

    public void addCommand(Command command) {
        commands.add(command);
    }

    public void removeCommand(Command command) {
        commands.remove(command);
    }

    public void addCommands(Command... commands1) {
        commands.addAll(Arrays.stream(commands1).toList());
    }

    public void removeCommands(Command... commands1) {
        commands.removeAll(Arrays.stream(commands1).toList());
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        Optional<Command> cmd = commands.stream().filter(a -> a.getData().getName().equals(event.getName())).findFirst();
        cmd.ifPresent(command -> command.execute(event));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (Bot.instance.isAdmin(event.getAuthor().getIdLong()) && event.getMessage().getContentRaw().equals(">>update")) {
            event.getGuild().updateCommands().addCommands(commands.stream().map(Command::getData).collect(Collectors.toList())).queue(a -> {
                event.getMessage().reply("Commands force deployed.").queue();
            });
        }
    }

    public void finishLoad() {
        Set<net.dv8tion.jda.api.interactions.commands.Command> problemQueue = new HashSet<>();
        for (net.dv8tion.jda.api.interactions.commands.Command jcmd : jdaCommands) {
            boolean remove = true;
            for (Command cmd : commands) {
                if (cmd.matches(jcmd))
                    if (remove)
                        remove = false;
                    else
                        System.err.println("Double command...?");
            }
            if (remove)
                problemQueue.add(jcmd);
        }
        for (Command cmd : commands) {
            boolean matchFound = false;
            for (net.dv8tion.jda.api.interactions.commands.Command jcmd : jdaCommands) {
                if (cmd.matches(jcmd))
                    if (!matchFound)
                        matchFound = true;
                    else
                        problemQueue.add(jcmd); // Remove duplicates.
            }
        }
        if (problemQueue.size() != 0)
            jda.updateCommands().addCommands(commands.stream().map(a -> a.getData()).collect(Collectors.toList())).queue();
        for (Guild guild: jda.getGuilds())
            guild.updateCommands().queue();
    }
}
