package de.saschat.modules.music.management;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import de.saschat.modules.music.MusicModule;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ConnectionManager {
    MusicModule parent;
    JDA jda;

    Timer executor = new Timer();
    ConnectionTimer timer = new ConnectionTimer(this);

    HashMap<Long, VoiceChannel> connectedChannels = new HashMap<>();
    HashMap<Long, VoiceData> voiceData = new HashMap<>();

    boolean ready = false;

    public ConnectionManager(MusicModule module) {
        this.parent = module;
        this.jda = module.jda;
    }

    public void start() {
        executor.scheduleAtFixedRate(timer, 60 * 1000, 60 * 1000);
        ready = true;
    }

    public void leave(long id) {
        VoiceChannel vc = connectedChannels.get(id);
        if(vc != null)
            vc.getGuild().getAudioManager().closeAudioConnection();
        try {
            VoiceData a = voiceData.get(id);
            a.disconnect();
        } catch (Exception ex) {}

        connectedChannels.remove(id);
        voiceData.remove(id);
    }

    public void join(VoiceChannel channel) {
        if (!ready)
            return;
        VoiceData a;
        if (connectedChannels.containsKey(channel.getGuild().getIdLong()))
            a = this.voiceData.get(channel.getGuild().getIdLong());
        else
            a = new VoiceData(parent.playerManager.createPlayer(), channel);
        a.channel = channel;
        a.updateChannel(channel);

        connectedChannels.put(channel.getGuild().getIdLong(), channel);
        this.voiceData.put(channel.getGuild().getIdLong(), a);
    }

    public void stop() {
        ready = false;
        for (Long channelId : connectedChannels.keySet()) {
            VoiceChannel channel = connectedChannels.get(channelId);
            channel.getGuild().getAudioManager().closeAudioConnection();
            connectedChannels.remove(channelId);
            voiceData.remove(channelId);
        }
        executor.cancel();
    }

    public void play(VoiceChannel channel, List<AudioTrack> tracks) {
        VoiceData data = voiceData.get(channel.getGuild().getIdLong());
        for (AudioTrack track : tracks) {
            data.scheduler.queue(track);
        }
        setIdle(channel.getGuild().getIdLong(), false);
    }
    public void setIdle(long id, boolean idle) {
        VoiceData data = voiceData.get(id);
        data.idleSince = System.currentTimeMillis();
        data.idle = idle;
    }

    public TrackScheduler.SkipResult skip(long g, int n) {
        VoiceData data = voiceData.get(g);
        return data.scheduler.skip(n);
    }

    public TrackScheduler.LoopStatus loop(long g) {
        VoiceData data = voiceData.get(g);
        return data.scheduler.looping();
    }
    public void loop(long g, TrackScheduler.LoopStatus a) {
        VoiceData data = voiceData.get(g);
        data.scheduler.setLooping(a);
    }

    public List<AudioTrack> queue(long id) {
        List<AudioTrack> track = new LinkedList<>();
        VoiceData data = voiceData.get(id);
        if(data == null)
            return null;
        track.add(data.scheduler.playing);
        track.addAll(data.scheduler.getQueue());
        return track;
    }

    public boolean inVC(long idLong) {
        VoiceData data = voiceData.get(idLong);
        return data != null;
    }

    public boolean shuffle(long idLong) {
        VoiceData data = voiceData.get(idLong);
        if(data == null)
            return false;
        return data.scheduler.shuffle();
    }

    public boolean seek(long idLong, long seek) {
        VoiceData data = voiceData.get(idLong);
        if(data == null)
            return false;
        return data.scheduler.seek(seek);
    }

    public long pos(long idLong) {
        VoiceData data = voiceData.get(idLong);
        if(data == null)
            return 1;
        return data.scheduler.pos();
    }
    public long len(long idLong) {
        VoiceData data = voiceData.get(idLong);
        if(data == null)
            return 1;
        return data.scheduler.len();
    }

    public boolean paused(long id) {
        VoiceData data = voiceData.get(id);
        if(data == null)
            return false;
        return data.scheduler.paused();
    }

    public boolean pause(long id) {
        VoiceData data = voiceData.get(id);
        if(data == null)
            return false;
        return data.scheduler.pause();
    }

    public boolean resume(long id) {
        VoiceData data = voiceData.get(id);
        if(data == null)
            return false;
        return data.scheduler.resume();
    }

    public AudioTrack track(long id) {
        VoiceData data = voiceData.get(id);
        if(data == null)
            return null;
        return data.scheduler.playing;
    }

    public boolean playing(long id) {
        VoiceData data = voiceData.get(id);
        if(data == null)
            return false;
        return data.scheduler.playing();
    }

    public class VoiceData extends AudioEventAdapter {
        public boolean idle = true;
        public long idleSince = System.currentTimeMillis();

        AudioPlayer player;
        TrackScheduler scheduler;
        AudioPlayerSendHandler sender;
        VoiceChannel channel;

        public VoiceData(AudioPlayer player, VoiceChannel channel) {
            this.player = player;
            this.scheduler = new TrackScheduler(this);
            this.sender = new AudioPlayerSendHandler(player);
            player.addListener(scheduler);
            player.addListener(this);
        }


        public void updateChannel(VoiceChannel channel) {
            AudioManager a = channel.getGuild().getAudioManager();
            a.openAudioConnection(channel);
            a.setSendingHandler(sender);
            this.channel = channel;
        }

        public void disconnect() {
            try {
                AudioManager a = channel.getGuild().getAudioManager();
                a.closeAudioConnection();
            } catch (Exception ex) {}
            player.destroy();
            scheduler.destroy();
        }
        public void stop() {
            ConnectionManager.this.leave(
                channel.getGuild().getIdLong()
            );
        }

        @Override
        public void onPlayerPause(AudioPlayer player) {
            setIdle(channel.getGuild().getIdLong(), true);
        }

        @Override
        public void onPlayerResume(AudioPlayer player) {
            setIdle(channel.getGuild().getIdLong(), false);
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            setIdle(channel.getGuild().getIdLong(), false);
        }
    }
}
