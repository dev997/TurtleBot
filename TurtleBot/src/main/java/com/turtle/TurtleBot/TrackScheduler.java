package com.turtle.TurtleBot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
  private final AudioPlayer player;
  private final BlockingQueue<AudioTrack> queue;
  private AudioTrack repeatTrack;
  private boolean repeat;

  /**
   * @param player The audio player this scheduler uses
   */
  public TrackScheduler(AudioPlayer player) {
	this.repeat = false;
	this.repeatTrack = null;
    this.player = player;
    this.queue = new LinkedBlockingQueue<>();
  }

  /**
   * Add the next track to queue or play right away if nothing is in the queue.
   *
   * @param track The track to play or add to queue.
   */
  public void queue(AudioTrack track) {
    // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
    // something is playing, it returns false and does nothing. In that case the player was already playing so this
    // track goes to the queue instead.
    if (!player.startTrack(track, true)) {
    	queue.offer(track);
    }else {
    	updateRepeatTrack(track);
    }
  }
  
  public void clearQueue() {
	  queue.clear();
	  Logger.getInstance().log("Queue Cleared");
  }
  
  public ArrayList<AudioTrackInfo> getQueue() {
	  ArrayList<AudioTrackInfo> queuelist = new ArrayList<AudioTrackInfo>();
	  queue.stream().forEach(s -> queuelist.add(s.getInfo()));
	  return queuelist;
  }
  
  public AudioTrack getNext() {
	  return queue.peek();
  }

  /**
   * Start the next track, stopping the current one if it is playing.
   */
  public void nextTrack() {
    // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
    // giving null to startTrack, which is a valid argument and will simply stop the player.
	if(repeat && repeatTrack!=null) {
		updateRepeatTrack(repeatTrack);
		player.startTrack(repeatTrack, false);
		Logger.getInstance().log("Starting track from repeat: "+repeatTrack);
	}else{
		if(queue.isEmpty()) {
			try {
				player.stopTrack();
			}catch(Exception e) {
				Logger logger = Logger.getInstance();
				logger.log("New Queue loaded: "+e.getMessage());
			}
		}else {
			updateRepeatTrack(queue.peek());
			player.startTrack(queue.poll(), false);
			Logger logger = Logger.getInstance();
			logger.log("Next Song started: "+player.getPlayingTrack().getInfo().title);
		}
	}
  }

  @Override
  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
    // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
    if (endReason.mayStartNext) {
      nextTrack();
    }else {
    	Logger logger = Logger.getInstance();
    	logger.log("Track end reason: "+endReason.toString());
    }
  }
  
  @Override
  public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
    Logger logger = Logger.getInstance();
	logger.log("Track Stuck");
  }
  
  @Override
  public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
	  Logger logger = Logger.getInstance();
	  logger.log("Track exception: "+exception.getMessage());
  }
  
  public boolean setRepeat() {
	  repeat = !repeat;
	  return repeat;
  }
  
  public void updateRepeatTrack(AudioTrack track) {
	  if(track!=null) {
		  this.repeatTrack = track.makeClone();
	  }
  }
}