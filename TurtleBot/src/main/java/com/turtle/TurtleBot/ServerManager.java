package com.turtle.TurtleBot;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.*;

public class ServerManager {
	
	List<MusicHandler> musichandlerlist = new ArrayList<MusicHandler>();
	List<Guild> servers;
	List<AudioTrack> results = new ArrayList<AudioTrack>();
	List<QuoteHandler> quotehandlerList = new ArrayList<QuoteHandler>();
	private String NO_PERMISSION = "You do not have permission to use this command!";
	
	public ServerManager() {
		servers = Driver.jda.getGuilds();
		for(Guild server : servers) {
			musichandlerlist.add(new MusicHandler(server));
			quotehandlerList.add(new QuoteHandler(server));
		}
	}
	
	public void sendQuote(MessageReceivedEvent event) {
		Guild server = event.getGuild();
		event.getChannel().sendMessage(getServerQuoteHandler(server).getRandQuote()).queue();
	}
	
	public void addQuote(String content, MessageReceivedEvent event) {
		Guild server = event.getGuild();
		MessageChannel channel = event.getChannel();
		String quote = content.substring(10);
		if(quote!=null && quote!=" " && quote!="") {
			getServerQuoteHandler(server).addToList(quote);
			channel.sendMessage(quote+" has been added to quotes").queue();
		}else{
			channel.sendMessage("Can't add blank quote").queue();
		}
		getServerQuoteHandler(server).saveQuotes();
	}
	
	public void listQuotes(String content, MessageReceivedEvent event) {
		Guild server = event.getGuild();
		MessageChannel channel = event.getChannel();
		List<String> quotes = getServerQuoteHandler(server).getQuotes();
		StringBuilder sb = new StringBuilder();
		int i=0;
		while(i<quotes.size()) {
			sb = new StringBuilder();
			for(int index=0; index<10; index++) {
				if(i>=quotes.size()) {
					break;
				}
				sb.append(i+1+". ");
				sb.append(quotes.get(i));
				sb.append("\n");
				i++;
			}
			channel.sendMessage(sb.toString()).queue();
		}
	}
	
	public void removeQuote(String content, MessageReceivedEvent event) {
		Guild server = event.getGuild();
		QuoteHandler quotehandler = getServerQuoteHandler(server);
		MessageChannel channel = event.getChannel();
		String indexmsg = content.substring(14);
		List<String> quotes = quotehandler.getQuotes();
		try {
			int index = Integer.valueOf(indexmsg)-1;
			String quote = quotes.get(index);
			if(quotehandler.removeQuote(index)){
				channel.sendMessage(quote+"\n has been removed").queue();
			}else {
				channel.sendMessage("Message could not be removed at index: "+index).queue();
			}
		}catch(Exception e) {
			e.printStackTrace();
			channel.sendMessage("Invalid index: "+indexmsg).queue();
		}
		quotehandler.saveQuotes();
	}
	
	public void playTrack(String content, MessageReceivedEvent event) {
		boolean hashandler = false;
		MusicHandler musicHandler = null;
		Guild guild = event.getGuild();
		AudioManager audiomanager = guild.getAudioManager();
		for(MusicHandler handler : musichandlerlist) {
			if(handler.getServer() == guild) {
				if(!handler.hasManager()) {
					handler.setManager(audiomanager);
				}
				musicHandler = handler;
				hashandler = true;
				break;
			}
		}
		if(!hashandler) {
			musicHandler = new MusicHandler(event.getGuild());
			musicHandler.setManager(audiomanager);
			musichandlerlist.add(musicHandler);
		}
		
		boolean isSelection = false;
		int index = 0;
		AudioTrack addedtrack = null;
		String searchToken = content.substring(6);
		if(!results.isEmpty()) {
			try {
				index = Integer.valueOf(searchToken);
				isSelection = true;
			}catch(Exception e) {
				results.clear();
			}
		}
		if(searchToken==" " || searchToken=="" || searchToken==null) {
			return;
		}
		
		//for link
		if(content.substring(6).startsWith("http")) {
			Member member = event.getMember();
			VoiceChannel voicechannel = member.getVoiceState().getChannel();
			musicHandler.playTrack(content.substring(6));
			musicHandler.joinChannel(voicechannel);
			
		//for selection
		}else if(isSelection) {
			AudioTrack track = results.get(index-1);
			musicHandler.playTrack(track.getInfo().uri);
			Member member = event.getMember();
			VoiceChannel voicechannel = member.getVoiceState().getChannel();
			try {
				musicHandler.joinChannel(voicechannel);
			}catch(Exception e) {
				System.out.println("could not join channel");
				e.printStackTrace();
			}
			addedtrack = track;
			
		//for searching
		}else{
			List<AudioTrack> results = musicHandler.searchItem(searchToken);
			if(!results.isEmpty()) {
				this.results = results;
				ArrayList<String> sb = new ArrayList<String>();
				int i=1;
				for(AudioTrack song : results) {
					sb.add(i+". "+song.getInfo().title+" "+buildTimeString(event, song.getInfo(), false));
					i++;
				}
				event.getChannel().sendMessage(buildEmbed("Results", sb)).queue();
			}else {
				event.getChannel().sendMessage("> No results for: "+searchToken).queue();
			}
		}
		
		if(addedtrack!=null) {
			AudioTrackInfo info = addedtrack.getInfo();
			event.getChannel().sendMessage("> Added to queue: "+info.title).queue();
		}
	}
	
	public void getQueue(String content, MessageReceivedEvent event) {
		
		MusicHandler musicHandler = null;
		for(MusicHandler handler : musichandlerlist) {
			if(handler.getServer() == event.getGuild()) {
				musicHandler = handler;
			}
		}
		
		List<AudioTrackInfo> queue = musicHandler.getQueue();
		MessageChannel channel = event.getChannel();
		if(queue.isEmpty()){
			channel.sendMessage("Queue is empty").queue();
			return;
		}
		ArrayList<String> sb = new ArrayList<String>();
		for(int i=1; i<=queue.size(); i++) {
			sb.add(i+". "+queue.get(i-1).title+buildTimeString(event, queue.get(i-1), false));
			if(i>=10) {
				int size = queue.size()-10;
				sb.add("--"+String.valueOf(size)+" more--");
				break;
			}
		}
		if(!queue.isEmpty()) {
			channel.sendMessage(buildEmbed("Queue", sb)).queue();
		}
	}
	
	public void stopTrack(String content, MessageReceivedEvent event) {
		MusicHandler musicHandler = null;
		for(MusicHandler handler : musichandlerlist) {
			if(handler.getServer() == event.getGuild()) {
				musicHandler = handler;
			}
		}
		musicHandler.clear();
		MessageChannel channel = event.getChannel();
		channel.sendMessage("Stopped and cleared queue").queue();
	}
	
	public void pauseTrack(String content, MessageReceivedEvent event) {
		MusicHandler musicHandler = null;
		for(MusicHandler handler : musichandlerlist) {
			if(handler.getServer() == event.getGuild()) {
				musicHandler = handler;
			}
		}
		musicHandler.pause();
		MessageChannel channel = event.getChannel();
		channel.sendMessage("Paused track: "+musicHandler.getPlayer().getPlayingTrack().getInfo().title).queue();
	}
	
	public void resumeTrack(String content, MessageReceivedEvent event) {
		MusicHandler musicHandler = null;
		for(MusicHandler handler : musichandlerlist) {
			if(handler.getServer() == event.getGuild()) {
				musicHandler = handler;
			}
		}
		musicHandler.resume();
		MessageChannel channel = event.getChannel();
		channel.sendMessage("Resumed track: "+musicHandler.getPlayer().getPlayingTrack().getInfo().title);
	}
	
	public void leaveVoice(MessageReceivedEvent event) {
		
		MusicHandler musicHandler = null;
		for(MusicHandler handler : musichandlerlist) {
			if(handler.getServer() == event.getGuild()) {
				musicHandler = handler;
			}
		}
		Logger.getInstance().log("Leaving voice: "+event.getMessage());
		
		musicHandler.leaveChannel();
	}
	
	public void nowPlaying(String content, MessageReceivedEvent event) {
		
		MusicHandler musicHandler = null;
		for(MusicHandler handler : musichandlerlist) {
			if(handler.getServer() == event.getGuild()) {
				musicHandler = handler;
			}
		}
		
		String title;
		AudioTrack track = musicHandler.getPlayer().getPlayingTrack();
		try {
			title = track.getInfo().title;
		}catch(Exception e) {
			title = null;
		}
		ArrayList<String> returnstring = new ArrayList<String>();
		if(title!=null && title!=" " && title!="") {
			returnstring.add(title+buildTimeString(event, track.getInfo(), true));
			event.getChannel().sendMessage(buildEmbed("Now Playing", returnstring)).queue();
		}else {
			event.getChannel().sendMessage("Nothing is playing").queue();
		}
	}
	
	public void skipSong(String content, MessageReceivedEvent event) {
		
		MusicHandler musicHandler = null;
		for(MusicHandler handler : musichandlerlist) {
			if(handler.getServer() == event.getGuild()) {
				musicHandler = handler;
			}
		}
		
		AudioTrack song = musicHandler.getPlayer().getPlayingTrack();
		musicHandler.skip();
		AudioTrack newsong = musicHandler.getPlayer().getPlayingTrack();
		event.getChannel().sendMessage("Song Skipped: "+song.getInfo().title).queue();
		try {
			event.getChannel().sendMessage("Now Playing: "+newsong.getInfo().title).queue();
		}catch(Exception e) {
			
		}
	}
	
	public void help(String content, MessageReceivedEvent event) {
		MessageChannel channel = event.getChannel();
		//uncomment to make the bot send a private message instead of to the server channel
		//User user = event.getAuthor();
		//user.openPrivateChannel().queue((channel) ->
        //{
            channel.sendMessage(Driver.COMMAND_START+"quote - gets a random quote\n"+
            					Driver.COMMAND_START+"quote list - displays a list of all quotes\n"+
            					Driver.COMMAND_START+"quote add (quote) - adds a quote to the list\n"+
            					Driver.COMMAND_START+"quote remove (#) - removes quote from list at specified number\n"+
            					Driver.COMMAND_START+"play (search) - searches youtube for videos based on search\n"+
            					Driver.COMMAND_START+"np/playing - shows currently playing song\n"+
            					Driver.COMMAND_START+"queue - displays queued songs\n"+
            					Driver.COMMAND_START+"stop - stops currently playing video and clears queue\n"+
            					Driver.COMMAND_START+"pause - pauses the currently palying song\n"+
            					Driver.COMMAND_START+"resume - resumes the paused song\n"+
            					Driver.COMMAND_START+"skip - skips the currently playing song\n"+
            					Driver.COMMAND_START+"earrape - toggles earrape mode").queue();
        //});
	}
	
	public void changeVolume(String content, MessageReceivedEvent event) {
		
		MusicHandler musicHandler = null;
		for(MusicHandler handler : musichandlerlist) {
			if(handler.getServer() == event.getGuild()) {
				musicHandler = handler;
			}
		}
		
		try {
			String message = "**EARRAPE MODE ACTIVATED**";
			int level = 1000;
			if(musicHandler.getVolume()==1000) {
				level=100;
				message = "**NORMIE MODE ACTIVATED**";
			}
			musicHandler.setVolume(level);
			event.getChannel().sendMessage(message).queue();
		}catch(Exception e) {
			Logger.getInstance().log("BAD TOUCH: volume level not an integer");
		}
	}
	
	public String buildTimeString(MessageReceivedEvent event, AudioTrackInfo song, boolean isNp) {
		
		MusicHandler musicHandler = null;
		for(MusicHandler handler : musichandlerlist) {
			if(handler.getServer() == event.getGuild()) {
				musicHandler = handler;
			}
		}
		
		String songtime = "";
		String playtime = "(";
		Long length = song.length;
		
		Long hours = TimeUnit.MILLISECONDS.toHours(length);
		Long minutes = TimeUnit.MILLISECONDS.toMinutes(length) - TimeUnit.HOURS.toMinutes(hours);
		Long seconds = TimeUnit.MILLISECONDS.toSeconds(length) - (TimeUnit.HOURS.toSeconds(hours)+TimeUnit.MINUTES.toSeconds(minutes));
		String sec="";
		if(seconds<10) {
			sec = "0"+Long.toString(seconds);
		}else {
			sec = Long.toString(seconds);
		}
		
		if(isNp) {
			Long position = musicHandler.getPlayer().getPlayingTrack().getPosition();
			
			Long poshours = TimeUnit.MILLISECONDS.toHours(position);
			Long posminutes = TimeUnit.MILLISECONDS.toMinutes(position) - TimeUnit.HOURS.toMinutes(poshours);
			Long posseconds = TimeUnit.MILLISECONDS.toSeconds(position) - (TimeUnit.HOURS.toSeconds(poshours)+TimeUnit.MINUTES.toSeconds(posminutes));
			
			String possec = "";
			if(posseconds<10) {
				possec = "0"+Long.toString(posseconds);
			}else {
				possec = Long.toString(posseconds);
			}
			
			if(hours>0) {
				playtime = " ("+poshours+":"+posminutes+":"+possec+"/";
			}else {
				playtime = " ("+posminutes+":"+possec+"/";
			}
		}
		
		if(hours>0) {
			songtime = playtime+hours+":"+minutes+":"+sec+")";
		}else {
			songtime = playtime+minutes+":"+sec+")";
		}
	
		return songtime;
		
	}
	
	public void noPerms(MessageChannel channel) {
		channel.sendMessage(NO_PERMISSION).queue();
	}
	
	public void sendContent(String content, MessageReceivedEvent event, File file) {
		event.getChannel().sendFile(file).queue();
	}
	
	public void toggleRepeat(String content, MessageReceivedEvent event) {
		
		MusicHandler musicHandler = null;
		for(MusicHandler handler : musichandlerlist) {
			if(handler.getServer() == event.getGuild()) {
				musicHandler = handler;
			}
		}
		
		event.getChannel().sendMessage(musicHandler.setRepeat()).queue();
	}
	
	public void moveMember(MessageReceivedEvent event, String content) {
		Guild guild = event.getGuild();
		if(!event.getMember().hasPermission(Permission.VOICE_MOVE_OTHERS)) {
			return;
		}
		List<VoiceChannel> channels = guild.getVoiceChannels();
		String name = content.substring(6);
		List<Member> members = guild.getMembersByEffectiveName(name, true);
		for(int i=0; i<10; i++) {
			guild.moveVoiceMember(members.get(0), channels.get(new Random().nextInt(6))).submit();
			try {
				TimeUnit.SECONDS.sleep(1);
			}catch(Exception e) {
				
			}
		}
	}
	
	public void moveAll(MessageReceivedEvent event, String content) {
		Guild guild = event.getGuild();
		if(!event.getMember().hasPermission(Permission.VOICE_MOVE_OTHERS)) {
			return;
		}
		List<VoiceChannel> channels = guild.getVoiceChannels();
		List<Member> members = event.getMember().getVoiceState().getChannel().getMembers();
		for(int i=0; i<1; i++) {
			for(Member member : members) {
				VoiceChannel channel;
				do {
					channel = channels.get(new Random().nextInt(6));
				}while(channel==member.getVoiceState().getChannel());
				guild.moveVoiceMember(member, channel).submit();
				try {
					TimeUnit.MILLISECONDS.sleep(250);
				}catch(Exception e) {
					
				}
			}
		}
	}
	
	public void restartBot(MessageReceivedEvent event) {
		if(event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			Driver.restart();
		}
	}
	
	public MessageEmbed buildEmbed(String title, ArrayList<String> messages) {
		
		EmbedBuilder eb = new EmbedBuilder();
		
		if(title!=null) {
			eb.setTitle(title);
		}
		
		eb.setColor(Color.green);
		for(String line : messages) {
			eb.addField("", line, false);
		}
		
		return eb.build();
	}
	
	public void godWordsHappy(MessageReceivedEvent event) {
		Random rand = new Random();
		String s="";
		String temp="";
		int lines=0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader("src/main/happy.txt"));
			while(reader.readLine() != null) lines++;
			reader.close();
			
			for(int i=0; i<33; i++) {
				reader = new BufferedReader(new FileReader("src/main/happy.txt"));
				for(int j=0; j<rand.nextInt(lines-1)+1; j++) {
					temp = reader.readLine();
				}
				s = s+temp+" ";
			}
			reader.close();
			event.getChannel().sendMessage(s).queue();
		}catch(Exception e) {
			Logger.getInstance().log(e);
		}
	}
	
	public QuoteHandler getServerQuoteHandler(Guild server) {
		for(QuoteHandler handler : quotehandlerList) {
			if(handler.getServer()==server) {
				return handler;
			}
		}
		return null;
	}
	
}
