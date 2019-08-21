package com.turtle.TurtleBot;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MsgListener extends ListenerAdapter{
	
	ServerManager manager;
	public String quotecommand = Driver.COMMAND_START+"quote";
	public String audiocommand = Driver.COMMAND_START+"play";
	
	public MsgListener(ServerManager manager) {
		this.manager=manager;
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		User user = event.getAuthor();
		if(user.isBot()) return;
		
		Message message = event.getMessage();
		String content = message.getContentRaw();
		if(content.equalsIgnoreCase(Driver.COMMAND_START+"help")) {
			manager.help(content, event);
		}else if(content.equalsIgnoreCase(quotecommand)){
			manager.sendQuote(event);
		}else if(content.startsWith(quotecommand+" add")){
			manager.addQuote(content, event);
		}else if(content.startsWith(quotecommand+" list")){
			manager.listQuotes(content, event);
		}else if(content.startsWith(quotecommand+" remove")) {
			manager.removeQuote(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"np") || content.equalsIgnoreCase(Driver.COMMAND_START+"playing") || 
				content.equalsIgnoreCase(Driver.COMMAND_START+"nowplaying")) {
			manager.nowPlaying(content, event);
		}else if(content.startsWith(audiocommand+" ")) {
			manager.playTrack(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"queue")) {
			manager.getQueue(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"stop")) {
			manager.stopTrack(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"pause")) {
			manager.pauseTrack(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"resume")) {
			manager.resumeTrack(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"leave")) {
			manager.leaveVoice(event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"skip")){
			manager.skipSong(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"earrape")) {
			manager.changeVolume(content, event);
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"repeat")) {
			manager.toggleRepeat(content, event);
		}else if(content.startsWith(Driver.COMMAND_START+"move")) {
			if(!Driver.disable) {
				manager.moveMember(event, content);
			}else {
				event.getChannel().sendMessage("Command is currently disabled").queue();
			}
		}else if(content.equalsIgnoreCase(Driver.COMMAND_START+"cell count")) {
			manager.getCells(event);
		}else if(content.startsWith(Driver.COMMAND_START+"target")) {
			manager.targetCells(event, content);
		}else if(content.startsWith(Driver.COMMAND_START+"check")){
			manager.checkCells(event, content);
		}else if(content.startsWith(Driver.COMMAND_START+"give")) {
			manager.giveCells(event, content);
		}else if(content.startsWith(Driver.COMMAND_START+"take")){
			manager.takeCells(event, content);
		}else if(content.startsWith(Driver.COMMAND_START+"restart")) {
			manager.restartBot(event);
		}
	}

}
