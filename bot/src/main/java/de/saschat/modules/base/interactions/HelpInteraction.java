package de.saschat.modules.base.interactions;

import de.saschat.bot.Bot;
import de.saschat.bot.command.Command;
import de.saschat.bot.interaction.Interaction;
import de.saschat.bot.module.Module;
import de.saschat.bot.module.ModuleWrapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class HelpInteraction implements Interaction {
    public static final Logger LOGGER = LoggerFactory.getLogger("Base.HelpInteraction");
    public UUID interaction = UUID.randomUUID();

    public static int max_length = 3000;
    public List<MessageEmbed> embeds = new LinkedList<>();

    public InteractionHook hook;
    public int page = 0;

    public long lastInteraction = System.currentTimeMillis();


    @Override
    public UUID getId() {
        return interaction;
    }

    @Override
    public long expiryTime() {
        return 15 * 1000; // 15s
    }

    @Override
    public long lastInteraction() {
        return lastInteraction;
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        lastInteraction = System.currentTimeMillis();
        if (event.getButton().getId().endsWith("next")) {
            if (page + 1 >= embeds.size()) {
                LOGGER.warn("Next button pressed even though there is no next page.");
                return;
            }
            page++;
            sendPage(page, event);
        } else if (event.getButton().getId().endsWith("back")) {
            if (page - 1 < 0) {
                LOGGER.warn("Back button pressed even though there is no previous page.");
                return;
            }
            page--;
            sendPage(page, event);
        }
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {

    }

    @Override
    public void start(GenericInteractionCreateEvent event) {
        event.deferReply().queue();
        hook = event.getHook();

        compileEmbeds();
        hook.sendMessageEmbeds(embeds.get(0)).addActionRow(
            getComponents(0)
        ).queue();
    }

    @Override
    public void expire() {
        EmbedBuilder builder = new EmbedBuilder(embeds.get(page));
        builder.setFooter("This interaction has expired. | Page " + (page + 1) + " of " + embeds.size());

        hook.editOriginalEmbeds(builder.build()).setActionRow(
            getComponents(false, false)
        ).queue();
    }

    public void compileEmbeds() {
        for (ModuleWrapper module : Bot.instance.moduleManager.getLoadedModules()) {
            int length = 0;
            EmbedBuilder builder = builder(module);
            for (Command cmd : module.module.getCommands()) {
                if (length < max_length) {
                    String line = "**" + cmd.getData().getName() + "** - " + cmd.getData().getDescription() + "\n";
                    length += line.length();
                    builder.appendDescription(line);
                } else {
                    embeds.add(builder.build());
                    builder = builder(module);
                    length = 0;
                }
            }
            embeds.add(builder.build());
        }
        for (int i = 0; i < embeds.size(); i++) {
            MessageEmbed embed = embeds.get(i);
            EmbedBuilder builder = new EmbedBuilder(embed);
            builder.setFooter("Page " + (i + 1) + " of " + embeds.size());
            embeds.set(i, builder.build());
        }
    }

    public EmbedBuilder builder(ModuleWrapper module) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(module.module.getFriendlyName());
        return builder;
    }

    public void sendPage(int pageId, ComponentInteraction interaction) {
        interaction.editMessageEmbeds(embeds.get(pageId)).setActionRow(
            getComponents(pageId)
        ).queue();
    }

    public List<Component> getComponents(boolean hasLast, boolean hasNext) {
        return
            List.of(
                Button.primary(interaction.toString() + "-back", "Back").withDisabled(!hasLast),
                Button.primary(interaction.toString() + "-next", "Next").withDisabled(!hasNext)
            );
    }

    public List<Component> getComponents(int pageId) {
        boolean hasLast = pageId > 0;
        boolean hasNext = pageId < (embeds.size() - 1);
        return getComponents(hasLast, hasNext);
    }

}
