package com.turtle.TurtleBot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;
import java.net.URL;

public class MusicHandler {
	private AudioPlayerManager playermanager;
	private AudioPlayer player;
	private TrackScheduler trackScheduler;
	private AudioManager audiomanager;
	private List<AudioTrack> results;
	private ScheduledExecutorService scheduler;
	private Guild server;
	private Logger logger = Logger.getInstance();
	
	public MusicHandler(Guild server) {
		this.server=server;
		playermanager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playermanager);
		createAudioPlayer();
	}
	
	@SuppressWarnings("unused")
	public void initTimer() {
		try {
			scheduler.shutdownNow();
		}catch(Exception e) {
			Logger.getInstance().log("Timer could not be cancelled, new timer created");
		}
		scheduler = Executors.newScheduledThreadPool(1);
    	Runnable cleantask = () -> {
    			if(player.getPlayingTrack()==null && audiomanager.isConnected()) {
	    				Logger.getInstance().log("leaving channel: IDLE Track: "+player.getPlayingTrack()+" Player: "+player);
	    				leaveChannel();
    			}
    	};
    	
    	ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(cleantask, 5, 5, TimeUnit.MINUTES);
	}
	
	public AudioPlayer getPlayer() {
		return player;
	}
	
	public void setManager(AudioManager audiomanager) {
		this.audiomanager=audiomanager;
	}
	
	public ArrayList<AudioTrackInfo> getQueue(){
		return trackScheduler.getQueue();
	}
	
	public void createAudioPlayer() {
		player = playermanager.createPlayer();
		this.trackScheduler = new TrackScheduler(player);
		player.addListener(trackScheduler);
	}
	
	public void joinChannel(VoiceChannel voicechannel) {
		if(!audiomanager.isConnected()) {
			initTimer();
			audiomanager.setSendingHandler(new AudioPlayerSendHandler(player));
			audiomanager.openAudioConnection(voicechannel);
		}
	}
	
	public void leaveChannel() {
		if(audiomanager.isConnected()) {
			audiomanager.closeAudioConnection();
		}
	}
	
	public void playTrack(String identifier) {
		playermanager.loadItem(identifier, new AudioLoadResultHandler() {
			  @Override
			  public void trackLoaded(AudioTrack track) {
			    trackScheduler.queue(track);
			  }

			  @Override
			  public void playlistLoaded(AudioPlaylist playlist) {
			    for (AudioTrack track : playlist.getTracks()) {
			      trackScheduler.queue(track);
			    }
			  }

			  @Override
			  public void noMatches() {
				  Logger.getInstance().log("No Matches");
				  leaveChannel();
			  }

			  @Override
			  public void loadFailed(FriendlyException throwable) {
				  Logger.getInstance().log(throwable.getMessage());
				  leaveChannel();
			  }
		});
	}
	
	public void playTrack(AudioTrack track) {
		trackScheduler.queue(track);
	}
	
	public void clear() {
		trackScheduler.clearQueue();
		player.stopTrack();
	}
	
	public void pause() {
		player.setPaused(true);
	}
	
	public void resume() {
		player.setPaused(false);
	}
	
	public boolean skip() {
		try {
			trackScheduler.updateRepeatTrack(trackScheduler.getNext());
		}catch(Exception e) {
			logger.log(e);
		}
		try {
			trackScheduler.nextTrack();
			return true;
		}catch(Exception e) {
			logger.log(e);
			return false;
		}
	}
	
	public List<AudioTrack> searchItem(String query) {
		YoutubeAudioSourceManager sourceManager = new YoutubeAudioSourceManager(true);
		Semaphore lock = new Semaphore(0);
		clearResults();
		try {
			new URL(query).toURI();
			playTrack(query);
			return results;
		}catch(Exception e) {
			logger.log("Searching for track: "+query);
			playermanager.loadItem("ytsearch: "+query, new FunctionalResultHandler(null, playlist -> {
				  results = playlist.getTracks();
				  lock.release();
		    }, null, null));
			try {
				lock.acquire();
			}catch(Exception x) {
			}
			List<AudioTrack> finalresults = new ArrayList<AudioTrack>();
			try {
				for(int i=0; i<10; i++) {
					finalresults.add(results.get(i));
				}
				return finalresults;
			}catch(Exception d) {
				logger.log(d.getMessage());
				return finalresults;
			}
		}
	}
	
	public void addToResults(AudioTrack track){
		results.add(track);
	}
	
	public List<AudioTrack> getResults() {
		return results;
	}
	
	public void clearResults() {
		results=new ArrayList<AudioTrack>();
	}
	
	public void setVolume(int level) {
		player.setVolume(level);
		System.out.println("Volume set to: "+player.getVolume());
	}
	
	public int getVolume() {
		int volume = player.getVolume();
		return volume;
	}
	
	public boolean isEarrape() {
		if(getVolume()==1000) {
			return true;
		}else {
			return false;
		}
	}
	
	public String setRepeat() {
		boolean toggle = trackScheduler.setRepeat();
		if(toggle) {
			return "Repeat turned on";
		}else {
			return "Repeat turned off";
		}
	}
	
	public Guild getServer() {
		return server;
	}
	
	public boolean hasManager() {
		if(audiomanager!=null) {
			return true;
		}
		return false;
	}
	
}
