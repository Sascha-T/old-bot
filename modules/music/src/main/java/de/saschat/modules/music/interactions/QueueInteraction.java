package de.saschat.modules.music.interactions;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.saschat.bot.interaction.Interaction;
import de.saschat.bot.util.Colors;
import de.saschat.modules.music.MusicModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
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

import javax.sound.midi.Track;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;

public class QueueInteraction implements Interaction {
    public UUID uuid = UUID.randomUUID();
    public long lastInteraction = System.currentTimeMillis();
    public InteractionHook hook;
    public Guild guild;
    public MusicModule module;
    public List<MessageEmbed> embeds = new LinkedList<>();
    public int page = 0;

    public static final Logger LOGGER = LoggerFactory.getLogger("Music.QueueInteraction");
    public QueueInteraction(MusicModule mod) {
        this.module = mod;
    }

    @Override
    public UUID getId() {
        return uuid;
    }

    @Override
    public long expiryTime() {
        return 15 * 1000;
    }

    @Override
    public long lastInteraction() {
        return lastInteraction;
    }

    public void updateInteraction() {
        this.lastInteraction = System.currentTimeMillis();
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        updateInteraction();
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
        updateInteraction();
    }

    @Override
    public void start(GenericInteractionCreateEvent event) {
        this.hook = event.getHook();
        guild = event.getGuild();
        compileEmbeds();
        hook.sendMessageEmbeds(embeds.get(0)).addActionRow(
            getComponents(0)
        ).queue();
    }

    public EmbedBuilder builder() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Songs playing in **__" + guild.getName() + "__**" + getIcon());
        builder.setColor(Colors.NEUTRAL);
        return builder;
    }

    public String getIcon() {
        return switch (module.manager.loop(guild.getIdLong())) {
            case ON -> " :repeat:";
            case ON_ONE -> " :repeat_one:";
            case OFF -> "";
        };
    }

    public final int MAX_LENGTH = 3000;

    public void compileEmbeds() {
        List<AudioTrack> tracks = module.manager.queue(guild.getIdLong());
        int length = 1;
        EmbedBuilder builder = builder();
        tracks = tracks.stream().filter(a -> a != null).collect(Collectors.toList());
        if(tracks.size() > 0)
            builder.appendDescription("*Now playing: [" + tracks.get(0).getInfo().title + "]("+ tracks.get(0).getInfo().uri +") (" + tracks.get(0).getIdentifier() + ")*\n\n");
        if(tracks.size() > 1)
            for (int j = 1; j < tracks.size(); j++) {
                AudioTrack track = tracks.get(j);
                String a = "*" + j + ".* [" + track.getInfo().title + " ](" + track.getInfo().uri + ")\n";
                if (length + a.length() > MAX_LENGTH) {
                    embeds.add(builder.build());
                    builder = builder();
                    length = 0;
                }
                builder.appendDescription(a);
                length += a.length();
            }
        else
            builder.appendDescription("No tracks in queue.");
        if (length > 0) embeds.add(builder.build());
        for (int i = 0; i < embeds.size(); i++) {
            EmbedBuilder builder2 = new EmbedBuilder(embeds.get(i));
            builder2.setFooter("Page " + (i + 1) + " of " + embeds.size());
            embeds.set(i, builder2.build());
        }
    }

    public void sendPage(int pageId, ComponentInteraction interaction) {
        interaction.editMessageEmbeds(embeds.get(pageId)).setActionRow(
            getComponents(pageId)
        ).queue();
    }

    public List<Component> getComponents(boolean hasLast, boolean hasNext) {
        return
            List.of(
                Button.primary(uuid.toString() + "-back", "Back").withDisabled(!hasLast),
                Button.primary(uuid.toString() + "-next", "Next").withDisabled(!hasNext)
            );
    }

    public List<Component> getComponents(int pageId) {
        boolean hasLast = pageId > 0;
        boolean hasNext = pageId < (embeds.size() - 1);
        return getComponents(hasLast, hasNext);
    }

    @Override
    public void expire() {
        EmbedBuilder builder = new EmbedBuilder(embeds.get(page));
        builder.setFooter("This interaction has expired. | Page " + (page + 1) + " of " + embeds.size());

        hook.editOriginalEmbeds(builder.build()).setActionRow(
            getComponents(false, false)
        ).queue();
    }
}
