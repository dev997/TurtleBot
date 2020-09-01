package com.turtle.TurtleBot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import net.dv8tion.jda.api.entities.Guild;

public class QuoteHandler {
	
	private List<String> quotes;
	private Guild server;
	private Path path=null;
	
	public QuoteHandler(Guild server) {
		this.server=server;
		try {
			//Once again eclipse is dumb and wants it without src/main
			path = FileSystems.getDefault().getPath("src/main/data/"+server.getName()+"_quotes.txt");
			if(!Files.exists(path)) {
				Files.createFile(path);
			}else {
				initQuoteList(path);
			}
		}catch(Exception e) {
			Logger.getInstance().log(e);
		}
	}
	
	public List<String> getQuotes(){
		return quotes;
	}
	
	public void initQuoteList(Path path) {
		quotes = new ArrayList<String>();
		try (Stream<String> stream = Files.lines(path)) {
	        stream.forEach(s -> addToList(s));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addToList(String s) {
		if(s!=null || s!="") {
			quotes.add(s);
		}
	}
	
	public void saveQuotes() {
		try {
			File fout = new File(path.toString());
			FileOutputStream fos = new FileOutputStream(fout);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			
			quotes.stream().forEach(s -> {try{
											osw.write(s+"\n");
										 }catch(Exception e) {
											 e.printStackTrace();
										 }});
			osw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getRandQuote() {
		Random rand = new Random();
		int index=0;
		try {
			index = rand.nextInt(quotes.size());
		}catch(Exception e) {
		}
		return quotes.get(index);
	}
	
	public boolean removeQuote(int index) {
		try {
			quotes.remove(index);
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Guild getServer() {
		return server;
	}
	
}
