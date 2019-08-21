package com.turtle.TurtleBot;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class CellHandler {
		
		private boolean exit;
		HashMap<String, Integer> celldata;
		HashMap<String, Pair<String, Integer>> targetlist;
		Guild server;
	
		public CellHandler(Guild server) {
			exit = false;
			try {
				initCells(server);
			}catch(Exception e){
			}
		}
		
		@SuppressWarnings("unchecked")
		public void initCells(Guild server) throws IOException{
			this.server = server;
			// Read data from celldata
			try {
				FileInputStream file = new FileInputStream("src/main/celldata.ser");
	            ObjectInputStream in = new ObjectInputStream(file);
	            
	            celldata = (HashMap<String, Integer>) in.readObject();
	            in.close();
	            file.close();
			}catch(Exception e) {
				Logger.getInstance().log(e);
				if(celldata==null) {
					celldata = new HashMap<String, Integer>();
				}
			}
			// Read data from targetdata
			try {
				FileInputStream file = new FileInputStream("src/main/targetdata.ser"); 
	            ObjectInputStream in = new ObjectInputStream(file);
				
				targetlist = (HashMap<String, Pair<String, Integer>>) in.readObject();
				in.close();
				file.close();
			}catch(Exception e) {
				e.printStackTrace();
				if(targetlist==null) {
					targetlist = new HashMap<String, Pair<String, Integer>>();
				}
			}
			//saves new files
			saveData();
			
			List<Member> members = server.getMembers();
			
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
						int count=0;
						while(!exit) {
							saveData();
							ArrayList<String> removeset = new ArrayList<String>();
							for(Map.Entry<String, Pair<String, Integer>> pair : targetlist.entrySet()) {
								if(pair.getValue().getRight() < 30) {
									addCells(pair.getKey(), 2);
									removeCells(pair.getValue().getLeft(), 1);
									
									Pair<String, Integer> newpair = Pair.of(pair.getValue().getLeft(), pair.getValue().getRight()+1);
									targetlist.put(pair.getKey(), newpair);
								}else {
									removeset.add(pair.getKey());
								}
							}
							for(String obj : removeset) {
								targetlist.remove(obj);
							}
							
							saveData();
							List<Member> members = server.getVoiceChannelById("530263582227693568").getMembers();
							Thread.sleep(TimeUnit.MINUTES.toMillis(2)); //Time in milliseconds
							List<Member> newmembers = server.getVoiceChannelById("530263582227693568").getMembers();
							for(Member member : newmembers) {
								if(members.contains(member) && count==15) {
									addCells(member.getUser().getId(), 5);
									count = 0;
								}
							}
							count++;
						}
					}catch(Exception e) {
					}
				}
			});
			thread.start();
		}
		
		public void stop() {
			exit = true;
		}
		
		public boolean addMember(String member) throws IOException{
			if(!isMember(member)) {
				celldata.put(member, 100);
				return true;
			}
			return false;
		}
			
		public boolean addCells(String member, int cells) throws IOException{
			if(isMember(member)) {
				int cellcount = (Integer) celldata.get(member);
				cellcount += cells;
				celldata.put(member, cellcount);
				return true;
			}
			return false;
		}
		
		public boolean removeCells(String member, int cells) throws IOException {
			if(isMember(member)) {
				int cellcount = celldata.get(member);
				cellcount -= cells;
				celldata.put(member, cellcount);
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
		
		public int getCells(String member) {
			return celldata.get(member);
		}
		
		public boolean targetCells(String user, String target) {
			if(targetlist.containsKey(user)) {
				return false;
			}else {
				Pair<String, Integer> data = Pair.of(target, 0);
				if(celldata.get(user) >= 30) {
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
			FileOutputStream file = new FileOutputStream("src/main/targetdata.ser");
			ObjectOutputStream out = new ObjectOutputStream(file);
			
			out.writeObject(targetlist);
			out.close();
			file.close();
			
			file = new FileOutputStream("src/main/celldata.ser");
			out = new ObjectOutputStream(file);
			
			out.writeObject(celldata);
			out.close();
			file.close();
		}
		
}
