package de.saschat.modules.music.management;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// https://github.com/sedmelluq/lavaplayer/blob/84f0d36a6b32a40bf9ab290ca5590d578ddc5d24/demo-jda/src/main/java/com/sedmelluq/discord/lavaplayer/demo/jda/TrackScheduler.java#L11
public class TrackScheduler extends AudioEventAdapter {
    private final ConnectionManager.VoiceData player;
    private final BlockingQueue<AudioTrack> queue;
    public AudioTrack playing;
    public int retry = 0;
    public final static int RETRIES = 5;

    public List<AudioTrack> getQueue() {
        return queue.stream().toList();
    }

    public TrackScheduler(ConnectionManager.VoiceData player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!player.player.startTrack(track, true)) {
            queue.offer(track);
        } else
            playing = track;
    }

    public void nextTrack(boolean force) {
        retry = 0;
        if (status == LoopStatus.OFF || force) {
            playing = queue.poll();
        } else if (status == LoopStatus.ON) {
            queue.offer(playing);
            playing = queue.poll();
        }
        if (playing != null)
            player.player.startTrack(playing.makeClone(), false);
        else
            player.player.startTrack(null, false);
    }
    public void nextTrack() {
        nextTrack(false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        exception.printStackTrace();
        long pos = player.getPlayingTrack().getPosition();
        player.startTrack(playing.makeClone(), false);
        player.getPlayingTrack().setPosition(pos);
        retry++;
        if (retry >= 5)
            if (status != LoopStatus.ON_ONE)
                nextTrack();
            else
                this.player.stop();
    }
    //

    public void destroy() {
        this.queue.clear();
    }

    public SkipResult skip(int n) {
        if ((long) queue.size() == 0 && playing == null)
            return SkipResult.NO_SONGS;
        int skipped = 0;
        for (; skipped < (n - 1); skipped++) {
            if (queue.poll() == null)
                break;
        }
        nextTrack(true);
        if (skipped != (n - 1))
            return SkipResult.SKIPPED_WITH_OVERHEAD;
        return SkipResult.SKIPPED;
    }

    public boolean shuffle() {
        List<AudioTrack> tracks = new ArrayList<>();
        AudioTrack track;
        while ((track = queue.poll()) != null)
            tracks.add(track);
        Collections.shuffle(tracks);
        for (AudioTrack a : tracks)
            queue.offer(a);
        return true;
    }

    public LoopStatus status = LoopStatus.OFF;

    public LoopStatus looping() {
        return status;
    }

    public void setLooping(LoopStatus a) {
        status = a;
    }

    public boolean seek(long seek) {
        if(playing.isSeekable()) {
            playing.setPosition(seek * 1000);
            return true;
        }
        return false;
    }

    public boolean pause() {
        player.player.setPaused(true);
        return player.player.isPaused();
    }

    public boolean resume() {
        player.player.setPaused(false);
        return !player.player.isPaused();
    }

    public boolean paused() {
        return player.player.isPaused();
    }

    public long pos() {
        return playing.getPosition();
    }

    public long len() {
        return playing.getDuration();
    }

    public boolean playing() {
        return playing != null;
    }

    public enum LoopStatus {
        OFF,
        ON,
        ON_ONE
    }

    public enum SkipResult {
        SKIPPED,
        SKIPPED_WITH_OVERHEAD,
        NO_SONGS
    }
}
