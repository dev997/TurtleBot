package com.turtle.TurtleBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;
import java.net.URL;

public class MusicHandler {
	public AudioPlayerManager playermanager;
	public AudioPlayer player;
	public TrackScheduler trackScheduler;
	public AudioManager audiomanager;
	public List<AudioTrack> results;
	public Timer timer;
	private Guild server;
	
	public MusicHandler(Guild server) {
		this.server=server;
		playermanager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playermanager);
		createAudioPlayer();
	}
	
	public void initTimer() {
		try {
			timer.cancel();
		}catch(Exception e) {
			System.out.println("Timer could not be cancelled, new timer created");
		}
		timer = new Timer(true);
    	TimerTask timertask = new TimerTask() {
    		public void run() {
    			if(getPlayer().getPlayingTrack()==null && audiomanager!=null) {
    				leaveChannel();
    			}
    		}
    	};
    	
    	timer.scheduleAtFixedRate(timertask, TimeUnit.MINUTES.toMillis(10), TimeUnit.MINUTES.toMillis(10));
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
		initTimer();
		audiomanager.setSendingHandler(new AudioPlayerSendHandler(player));
		audiomanager.openAudioConnection(voicechannel);
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
				  System.out.println("No Matches");
				  leaveChannel();
			  }

			  @Override
			  public void loadFailed(FriendlyException throwable) {
				  System.out.println("Load Failed");
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
			
		}
		try {
			trackScheduler.nextTrack();
			return true;
		}catch(Exception e) {
			return false;
		}
		
	}
	
	public List<AudioTrack> searchItem(String query) {
		YoutubeAudioSourceManager sourceManager = new YoutubeAudioSourceManager();
		YoutubeSearchProvider ytsearchProvider = new YoutubeSearchProvider(sourceManager);
		List<AudioTrack> results = new ArrayList<AudioTrack>();
		try {
			new URL(query).toURI();
			playTrack(query);
			return results;
		}catch(Exception e) {
			AudioItem result = ytsearchProvider.loadSearchResult(query);
			if(result instanceof BasicAudioPlaylist) {
				results = ((BasicAudioPlaylist)result).getTracks();
			}
			List<AudioTrack> finalresults = new ArrayList<AudioTrack>();
			try {
				for(int i=0; i<10; i++) {
					finalresults.add(results.get(i));
				}
				return finalresults;
			}catch(Exception d) {
				return finalresults;
			}
		}
	}
	
	public void addToResults(AudioTrack track){
		results = new ArrayList<AudioTrack>();
		results.add(track);
	}
	
	public List<AudioTrack> getResults() {
		return results;
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
}
