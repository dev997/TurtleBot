package com.turtle.TurtleBot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import net.dv8tion.jda.core.entities.*;

public class CellHandler {
	
		JSONObject celldata = new JSONObject();
	
		public CellHandler(Guild server) {
			try {
				initCells(server);
			}catch(Exception e){
			}
		}
	
		public void initCells(Guild server) throws IOException{
			try {
				FileReader reader = new FileReader("src/main/celldata.json");
				JSONParser parser = new JSONParser();
				celldata = (JSONObject) parser.parse(reader);
			}catch(Exception e) {
			}
			
			List<Member> members = server.getMembers();
			FileWriter writer = new FileWriter("src/main/celldata.json");
			
			for(int i=0; i<members.size()-1; i++) {
				if(!isMember(members.get(i))) {
					addMember(members.get(i));
				}
			}
			
			if(!celldata.containsKey("ServerTotal")) {
				Long celltotal = new Long(0);
				for(Object cell : celldata.values()) {
					celltotal += (Long) cell;
				}
				celldata.put("ServerTotal", celltotal);
			}
			writer.write(celldata.toJSONString());
			writer.flush();
		}
		
		public boolean addMember(Member member) throws IOException{
			if(!isMember(member)) {
				celldata.put(member, new Long(0));
				try (FileWriter file = new FileWriter("src/main/celldata.json")){
					file.write(celldata.toJSONString());
					file.flush();
				}
				return true;
			}
			return false;
		}
			
		public boolean addCells(Member member, int cells) throws IOException{
			if(isMember(member)) {
				Long cellcount = (Long) celldata.get(member);
				cellcount += cells;
				celldata.put(member, new Long(cellcount));
				try (FileWriter file = new FileWriter("src/main/celldata.json")){
					file.write(celldata.toJSONString());
					file.flush();
				}
				return true;
			}
			return false;
		}
		
		public boolean removeCells(Member member, int cells) throws IOException {
			if(isMember(member)) {
				Long cellcount = (Long) celldata.get(member);
				cellcount -= cells;
				celldata.put(member, cells);
				try (FileWriter file = new FileWriter("src/main/celldata.json")){
					file.write(celldata.toJSONString());
					file.flush();
				}
				return true;
			}
			return false;
		}
		
		public boolean isMember(Member member) {
			if(celldata.containsKey(member)) {
				return true;
			}
			return true;
		}
		
}
