package com.turtle.TurtleBot;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.simple.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.tuple.Pair;

public class CellHandler {
	
		JSONObject celldata;
		HashMap<String, Pair<String, Integer>> targetlist = new HashMap<String, Pair<String, Integer>>();
		JSONObject targetdata;
		Guild server;
	
		public CellHandler(Guild server) {
			try {
				initCells(server);
			}catch(Exception e){
			}
		}
	
		public void initCells(Guild server) throws IOException{
			this.server = server;
			// Read data from celldata
			try {
				FileReader reader = new FileReader("src/main/celldata.json");
				JSONParser parser = new JSONParser();
				celldata = (JSONObject) parser.parse(reader);
			}catch(Exception e) {
				if(celldata==null) {
					celldata = new JSONObject();
				}
			}
			// Read data from targetdata
			try {
				FileReader reader = new FileReader("src/main/targetdata.json");
				JSONParser parser = new JSONParser();
				targetdata = (JSONObject) parser.parse(reader);
				targetlist = (HashMap) targetdata.get("Targets");
			}catch(Exception e) {
				if(targetdata==null) {
					targetdata = new JSONObject();
				}
			}
			//saves new files
			saveData();
			
			List<Member> members = server.getMembers();
			FileWriter writer = new FileWriter("src/main/celldata.json");
			
			for(Member member : members) {
				if(!isMember(member.getUser().getId())) {
					addMember(member.getUser().getId());
					System.out.println(isMember(member.getUser().getId()));
				}
			}
			saveData();
			
			/*
			 * Thread watches the voice channel for people in 1 hour intervals and removes cells
			 * from the ones that are still in the channel
			 * 
			 */
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						while(true) {
							
							for(Map.Entry<String, Pair<String, Integer>> pair : targetlist.entrySet()) {
								if(pair.getValue().getRight() < 2) {
									addCells(pair.getKey(), 20);
									removeCells(pair.getValue().getLeft(), 20);
									
									Pair<String, Integer> newpair = Pair.of(pair.getValue().getLeft(), pair.getValue().getRight()+1);
									targetlist.put(pair.getKey(), newpair);
								}else {
									targetlist.remove(pair.getKey());
								}
							}
							
							List<Member> members = server.getVoiceChannelById("530263582227693568").getMembers();
							Thread.sleep(TimeUnit.MINUTES.toMillis(30)); //Time in milliseconds
							List<Member> newmembers = server.getVoiceChannelById("530263582227693568").getMembers();
							for(Member member : newmembers) {
								if(members.contains(member)) {
									addCells(member.getUser().getId(), 5);
								}
							}
						}
					}catch(Exception e) {
					}
				}
			});
			thread.start();
		}
		
		public boolean addMember(String member) throws IOException{
			if(!isMember(member)) {
				celldata.put(member, new Long(100));
				try (FileWriter file = new FileWriter("src/main/celldata.json")){
					file.write(celldata.toJSONString());
					file.flush();
				}
				return true;
			}
			return false;
		}
			
		public boolean addCells(String member, int cells) throws IOException{
			if(isMember(member)) {
				Long cellcount = (Long) celldata.get(member);
				cellcount += cells;
				celldata.put(member, new Long(cellcount));
				saveData();
				return true;
			}
			return false;
		}
		
		public boolean removeCells(String member, int cells) throws IOException {
			if(isMember(member)) {
				Long cellcount = (Long) celldata.get(member);
				cellcount -= cells;
				celldata.put(member, new Long(cellcount));
				saveData();
				return true;
			}
			return false;
		}
		
		public boolean isMember(String member) {
			if(celldata.containsKey(member)) {
				return true;
			}
			return false;
		}
		
		public Long getCells(String member) {
			return (Long) celldata.get(member);
		}
		
		public boolean targetCells(String user, String target) {
			if(targetlist.containsKey(user)) {
				return false;
			}else {
				Pair<String, Integer> data = Pair.of(target, 0);
				if((Long) celldata.get(user) >= 30) {
					try {
						removeCells(user, 30);
						targetlist.put(user, data);
					}catch(Exception e) {
					}
				}
				try {
					saveData();
				}catch(Exception e) {
				}
				return true;
			}
		}
		
		public void saveData() throws IOException{
			targetdata.put("Targets", targetlist);
			try (FileWriter file = new FileWriter("src/main/targetdata.json")){
				file.write(targetdata.toJSONString());
				file.flush();
			}
			try (FileWriter file = new FileWriter("src/main/celldata.json")){
				file.write(celldata.toJSONString());
				file.flush();
			}
		}
		
}
