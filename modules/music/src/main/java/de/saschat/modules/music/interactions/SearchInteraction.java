package de.saschat.modules.music.interactions;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.saschat.bot.interaction.Interaction;
import de.saschat.modules.music.MusicModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SearchInteraction implements Interaction {
    public long lastInteraction = System.currentTimeMillis();

    public void interact() {
        this.lastInteraction = System.currentTimeMillis();
    }

    InteractionHook hook;

    public AudioPlaylist results;
    public List<AudioTrack> tracks;
    public MusicModule module;
    public VoiceChannel channel;

    public SearchInteraction(AudioPlaylist playlist, MusicModule module, VoiceChannel channel) {
        this.results = playlist;
        this.tracks = results.getTracks().stream().limit(10).collect(Collectors.toList());
        this.module = module;
        this.channel = channel;
    }

    public UUID uuid = UUID.randomUUID();

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

    boolean expired = false;

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        interact();
        if (event.getButton().getId().endsWith("accept") && !expired) {
            AudioTrack track = tracks.get(selected);

            if (!module.manager.inVC(channel.getGuild().getIdLong()))
                module.manager.join(channel);
            module.manager.play(channel, List.of(track));

            event.editMessageEmbeds(builder().setFooter("This interaction has expired.").setTitle(
                "Queued __" + track.getInfo().title + "__",
                track.getInfo().uri
            ).build()).setActionRows(ActionRow.of(actionBar1(true, selected)), ActionRow.of(actionBar2(true))).queue();
            expired = true;
            hook.sendMessage("Queued [" + track.getInfo().title + "](" + track.getInfo().uri + ").").queue();
        }
    }

    int selected = 0;

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {
        interact();
        selected = Integer.parseInt(event.getInteraction().getSelectedOptions().get(0).getValue());
        event.editSelectionMenu(selection(false, selected)).queue();
    }

    @Override
    public void start(GenericInteractionCreateEvent event) {
        hook = event.getHook();
        event.getHook().sendMessageEmbeds(builder().build()).addActionRows(ActionRow.of(actionBar1(false, selected)), ActionRow.of(actionBar2(false))).queue();
    }

    public List<Component> actionBar1(boolean finished, int selected) {
        return List.of(
            selection(finished, selected)
        );
    }

    public List<Component> actionBar2(boolean finished) {
        return List.of(
            Button.primary(uuid.toString() + "-accept", "Queue.").withDisabled(finished)
        );
    }


    public SelectionMenu selection(boolean finished, int selected) {
        SelectionMenu.Builder a = SelectionMenu.create(uuid.toString() + "-song");
        a.setMinValues(1);
        a.setMaxValues(1);
        for (int i = 0; i < tracks.size(); i++) {
            AudioTrack b = tracks.get(i);
            String text = b.getInfo().title;
            if (text.length() > 25)
                text = text.substring(0, 25);
            a.addOption(text, Integer.toString(i));
        }
        a.setDefaultOptions(List.of(a.getOptions().get(selected)));
        a.setDisabled(finished);
        return a.build();
    }

    public EmbedBuilder builder() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Search results");
        for (int i = 0; i < tracks.size(); i++) {
            AudioTrack track = tracks.get(i);
            builder.appendDescription("*" + (i + 1) + "." + "* [" + track.getInfo().title + "](" + track.getInfo().uri + ")\n");
        }
        return builder;
    }

    @Override
    public void expire() {
        if (!expired)
            hook.editOriginalEmbeds(builder().setFooter("This interaction has expired.").build()).setActionRows(ActionRow.of(actionBar1(true, selected)), ActionRow.of(actionBar2(true))).queue();
    }
}
