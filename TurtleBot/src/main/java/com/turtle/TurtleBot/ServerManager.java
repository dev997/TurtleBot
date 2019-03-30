package com.turtle.TurtleBot;

import java.util.List;

import net.dv8tion.jda.core.entities.*;

public class ServerManager {
	
	MusicHandler musichandler;
	List<Guild> servers;
	
	public ServerManager() {
		servers = Driver.jda.getGuilds();
		musichandler
	}
	
}
