package com.turtle.TurtleBot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.GuildController;

import java.util.Random;

public class MsgListener extends ListenerAdapter{
	
	public QuoteHandler quotehandler = new QuoteHandler();
	public String quotecommand = Driver.COMMAND_START+"quote";
	public String audiocommand = Driver.COMMAND_START+"play";
	public MusicHandler musicHandler = new MusicHandler();
	public List<AudioTrack> results = new ArrayList<AudioTrack>();
	private String NO_PERMISSION = "You do not have permission to use this command!";
	private CellHandler cellhandler = new CellHandler(Driver.jda.getGuilds().get(0));

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		User user = event.getAuthor();
		if(user.isBot()) return;
		
		Message message = event.getMessage();
		String content = message.getContentRaw();
		if(content.equalsIgnoreCase(Driver.COMMAND_START+"help")) {
			help(content, event);
		}else if(content.equalsIgnoreCase(quotecommand)){
			MessageChannel channel = event.getChannel();
			channel.sendMessage(quotehandler.getRandQuote()).queue();
		}else if(content.startsWith(quotecommand+" add")){
			addQuote(content, event);
		}else if(content.startsWith(quotecommand+" list")){
			listQuotes(content, event);
		}else if(content.startsWith(quotecommand+" remove")) {
			removeQuote(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"np") || content.equalsIgnoreCase(Driver.COMMAND_START+"playing") || 
				content.equalsIgnoreCase(Driver.COMMAND_START+"nowplaying")) {
			nowPlaying(content, event);
		}else if(content.startsWith(audiocommand+" ")) {
			playTrack(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"queue")) {
			getQueue(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"stop")) {
			stopTrack(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"pause")) {
			pauseTrack(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"resume")) {
			resumeTrack(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"leave")) {
			leaveVoice();
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"skip")){
			skipSong(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"earrape")) {
			changeVolume(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"grape")) {
			File grape_file = new File("src/main/grape.png");
			sendContent(content, event, grape_file);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"repeat")) {
			toggleRepeat(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"mr bones")) {
			mrBones(event);
		}else if(content.startsWith(Driver.COMMAND_START+"move")) {
			if(!Driver.disable) {
				moveMember(event, content);
			}else {
				event.getChannel().sendMessage("Command is currently disabled").queue();
			}
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"scramble")) {
			if(!Driver.disable) {
				moveAll(event, content);
			}else {
				event.getChannel().sendMessage("Command is currently disabled").queue();
			}
		}else if(content.startsWith(Driver.COMMAND_START+"give")) {
			
		}
	}
	
	public void addQuote(String content, MessageReceivedEvent event) {
		MessageChannel channel = event.getChannel();
		String quote = content.substring(10);
		if(quote!=null && quote!=" " && quote!="") {
			quotehandler.addToList(quote);
			channel.sendMessage(quote+" has been added to quotes").queue();
		}else{
			channel.sendMessage("Can't add blank quote").queue();
		}
		quotehandler.saveQuotes();
	}
	
	public void listQuotes(String content, MessageReceivedEvent event) {
		MessageChannel channel = event.getChannel();
		List<String> quotes = quotehandler.getQuotes();
		StringBuilder sb = new StringBuilder();
		int i=1;
		while(i<quotes.size()) {
			sb = new StringBuilder();
			for(int index=0; index<10; index++) {
				if(i>=quotes.size()) {
					break;
				}
				sb.append(i+". ");
				sb.append(quotes.get(i));
				sb.append("\n");
				i++;
			}
			channel.sendMessage(sb.toString()).queue();
		}
		quotehandler.saveQuotes();
	}
	
	public void removeQuote(String content, MessageReceivedEvent event) {
		MessageChannel channel = event.getChannel();
		String indexmsg = content.substring(14);
		List<String> quotes = quotehandler.getQuotes();
		try {
			int index = Integer.valueOf(indexmsg);
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
		
		Guild guild = event.getGuild();
		AudioManager audiomanager = guild.getAudioManager();
		musicHandler.setManager(audiomanager);
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
				StringBuilder sb = new StringBuilder();
				int i=1;
				for(AudioTrack song : results) {
					sb.append(i+". ");
					sb.append(song.getInfo().title);
					sb.append(buildTimeString(song.getInfo(), false));
					sb.append("\n");
					i++;
				}
				event.getChannel().sendMessage(sb.toString()).queue();
			}
		}
		if(addedtrack!=null) {
			AudioTrackInfo info = addedtrack.getInfo();
			event.getChannel().sendMessage("Added to queue: "+info.title).queue();
		}
	}
	
	public void getQueue(String content, MessageReceivedEvent event) {
		List<AudioTrackInfo> queue = musicHandler.getQueue();
		MessageChannel channel = event.getChannel();
		if(queue.isEmpty()){
			channel.sendMessage("Queue is empty").queue();
			return;
		}
		StringBuilder sb = new StringBuilder();
		for(int i=1; i<=queue.size(); i++) {
			sb.append(i+". ");
			sb.append(queue.get(i-1).title);
			sb.append(buildTimeString(queue.get(i-1), false));
			sb.append("\n");
			if(i>=10) {
				int size = queue.size()-10;
				sb.append("--"+String.valueOf(size)+" more--");
				break;
			}
			i++;
		}
		if(!queue.isEmpty()) {
			channel.sendMessage(sb.toString()).queue();
		}
	}
	
	public void stopTrack(String content, MessageReceivedEvent event) {
		musicHandler.clear();
		MessageChannel channel = event.getChannel();
		channel.sendMessage("Stopped and cleared queue").queue();
	}
	
	public void pauseTrack(String content, MessageReceivedEvent event) {
		musicHandler.pause();
		MessageChannel channel = event.getChannel();
		channel.sendMessage("Paused track: "+musicHandler.getPlayer().getPlayingTrack().getInfo().title).queue();
	}
	
	public void resumeTrack(String content, MessageReceivedEvent event) {
		musicHandler.resume();
		MessageChannel channel = event.getChannel();
		channel.sendMessage("Resumed track: "+musicHandler.getPlayer().getPlayingTrack().getInfo().title);
	}
	
	public void leaveVoice() {
		musicHandler.leaveChannel();
	}
	
	public void nowPlaying(String content, MessageReceivedEvent event) {
		String title;
		AudioTrack track = musicHandler.getPlayer().getPlayingTrack();
		try {
			title = track.getInfo().title;
		}catch(Exception e) {
			title = null;
		}
		if(title!=null && title!=" " && title!="") {
			event.getChannel().sendMessage("Currently Playing: "+title+buildTimeString(track.getInfo(), true)).queue();
		}else {
			event.getChannel().sendMessage("Nothing is playing").queue();
		}
	}
	
	public void skipSong(String content, MessageReceivedEvent event) {
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
			System.out.println("BAD TOUCH: volume level not an integer");
		}
	}
	
	public String buildTimeString(AudioTrackInfo song, boolean isNp) {
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
	
	public void NoPerms(MessageChannel channel) {
		channel.sendMessage(NO_PERMISSION).queue();
	}
	
	public void sendContent(String content, MessageReceivedEvent event, File file) {
		event.getChannel().sendFile(file).queue();
	}
	
	public void toggleRepeat(String content, MessageReceivedEvent event) {
		event.getChannel().sendMessage(musicHandler.setRepeat()).queue();
	}
	
	public void mrBones(MessageReceivedEvent event) {
		Member member = event.getMember();
		VoiceChannel voicechannel = member.getVoiceState().getChannel();
		musicHandler.joinChannel(voicechannel);
		musicHandler.playTrack("https://youtu.be/_MkUCE8cMUY");
	}
	
	public void moveMember(MessageReceivedEvent event, String content) {
		Guild guild = event.getGuild();
		if(!event.getMember().hasPermission(Permission.VOICE_MOVE_OTHERS)) {
			return;
		}
		List<VoiceChannel> channels = guild.getVoiceChannels();
		GuildController control = new GuildController(guild);
		String name = content.substring(6);
		List<Member> members = guild.getMembersByEffectiveName(name, true);
		for(int i=0; i<10; i++) {
			control.moveVoiceMember(members.get(0), channels.get(new Random().nextInt(6))).submit();
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
		GuildController control = new GuildController(guild);
		List<Member> members = event.getMember().getVoiceState().getChannel().getMembers();
		for(int i=0; i<1; i++) {
			for(Member member : members) {
				VoiceChannel channel;
				do {
					channel = channels.get(new Random().nextInt(6));
				}while(channel==member.getVoiceState().getChannel());
				control.moveVoiceMember(member, channel).submit();
				try {
					TimeUnit.MILLISECONDS.sleep(250);
				}catch(Exception e) {
					
				}
			}
		}
	}

}
