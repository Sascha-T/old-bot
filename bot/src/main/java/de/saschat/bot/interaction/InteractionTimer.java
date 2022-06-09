package de.saschat.bot.interaction;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class InteractionTimer implements Runnable {
    InteractionManager manager;

    public InteractionTimer(InteractionManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        List<Interaction> remove = new LinkedList<>();
        for (Interaction ac: manager.interactions) {
            if(System.currentTimeMillis() > ac.expiryTime() + ac.lastInteraction()) {
                ac.expire();
                remove.add(ac);
            }
        }
        for (Interaction a: remove) {
            manager.removeInteraction(a);
        }
    }
}
