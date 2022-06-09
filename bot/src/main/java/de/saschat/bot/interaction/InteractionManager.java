package de.saschat.bot.interaction;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InteractionManager extends ListenerAdapter {
    public List<Interaction> interactions = new LinkedList<>();
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    public InteractionTimer timer = new InteractionTimer(this);
    public JDA jda;


    public InteractionManager(JDA jda) {
        this.jda = jda;
        jda.addEventListener(this);
        executor.scheduleAtFixedRate(timer, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        String id = event.getButton().getId();
        Optional<Interaction> optional = interactions.stream().filter(a -> id.startsWith(a.getId().toString())).findFirst();
        optional.ifPresent(interaction -> interaction.onButtonClick(event));
    }

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        String id = event.getComponent().getId();
        Optional<Interaction> optional = interactions.stream().filter(a -> id.startsWith(a.getId().toString())).findFirst();
        optional.ifPresent(interaction -> interaction.onSelectionMenu(event));
    }

    public void addInteraction(Interaction ac) {
        this.interactions.add(ac);
    }

    public void removeInteraction(Interaction ac) {
        this.interactions.remove(ac);
    }
}
