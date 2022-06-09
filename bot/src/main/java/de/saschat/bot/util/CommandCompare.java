package de.saschat.bot.util;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandCompare {
    public static boolean compare_command(Command command, CommandData data) {
        if (!data.getName().equals(command.getName()))
            return false;
        if (!data.getDescription().equals(command.getDescription()))
            return false;
        if (!compare_options(command.getOptions(), data.getOptions()))
            return false;
        if(!compare_subcommands(command.getSubcommands(), data.getSubcommands()))
            return false;
        for(Command.SubcommandGroup subcommandGroupMain : command.getSubcommandGroups()) {
            boolean matches = false;
            for (SubcommandGroupData subcommandGroupData : data.getSubcommandGroups()) {
                if(!subcommandGroupData.getName().equals(subcommandGroupMain.getName()))
                    continue;
                if(!subcommandGroupData.getDescription().equals(subcommandGroupMain.getDescription()))
                    continue;
                matches = compare_subcommands(subcommandGroupMain.getSubcommands(), subcommandGroupData.getSubcommands());
            }
            if(!matches)
                return false;
        }
        return true;
    }

    public static boolean compare_subcommands(List<Command.Subcommand> option1, List<SubcommandData> option2) {
        for (Command.Subcommand subcommandsMain : option1) {
            boolean matches = false;
            for (SubcommandData subcommandsData : option2) {
                if(!subcommandsData.getName().equals(subcommandsMain.getName()))
                    continue;
                if(!subcommandsData.getDescription().equals(subcommandsMain.getDescription()))
                    continue;
                matches = compare_subcommand(subcommandsMain, subcommandsData);
            }
            if(!matches)
                return false;
        }
        return true;
    }

    public static boolean compare_subcommand(Command.Subcommand option1, SubcommandData option2) {
        if(!option1.getName().equals(option2.getName()))
            return false;
        if(!option1.getDescription().equals(option2.getDescription()))
            return false;
        return compare_options(option1.getOptions(), option2.getOptions());
    }

    public static boolean compare_options(List<Command.Option> option1, List<OptionData> option2) {
        for (Command.Option a : option1) {
            for (OptionData b : option2) {
                if (a.getName().equals(b.getName())) {
                    if (!a.getDescription().equals(b.getName()))
                        return false;
                    if (a.getType().getKey() != b.getType().getKey())
                        return false;
                    return compare_types(a.getChannelTypes(), b.getChannelTypes());
                }
            }
        }
        return false;
    }

    public static boolean compare_types(Set<ChannelType> type1, Set<ChannelType> type2) {
        if (type1.size() != type2.size())
            return false;
        Set<Integer> type = new HashSet<>();
        for (ChannelType typeA : type1) {
            for (ChannelType typeB : type2) {
                if (typeA.getId() == typeB.getId())
                    type.add(typeA.getId());
            }
        }
        return type.size() == type1.size();
    }
}
