package de.saschat.modules.music.management;

import de.saschat.bot.Bot;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

public class ConnectionTimer extends TimerTask {
    ConnectionManager manager;
    public Logger logger = LoggerFactory.getLogger("ConnectionTimer");
    public ConnectionTimer(ConnectionManager connectionManager) {
        this.manager = connectionManager;
    }

    @Override
    public void run() {
        List<Long> dc = new LinkedList<>();
        for (Long id: manager.voiceData.keySet()) {
            ConnectionManager.VoiceData data = manager.voiceData.get(id);
            if(data.idle)
                if(data.idleSince + (5 * 60 * 1000) > System.currentTimeMillis()) {
                    if(Bot.instance.config.getDebug())
                        logger.warn("Disconnecting from " + data.channel.getName());
                    dc.add(id);
                }
        }
        for (Long a: dc) {
            manager.leave(a);
        }
    }
}
