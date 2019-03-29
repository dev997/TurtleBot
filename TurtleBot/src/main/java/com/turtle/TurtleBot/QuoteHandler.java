package com.turtle.TurtleBot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class QuoteHandler {
	
	private List<String> quotes;
	
	public QuoteHandler() {
		initQuoteList("src/main/quotes.txt");
	}
	
	public List<String> getQuotes(){
		return quotes;
	}
	
	public void initQuoteList(String filename) {
		quotes = new ArrayList<String>();
		try (Stream<String> stream = Files.lines(Paths.get(filename))) {
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
			File fout = new File("src/main/quotes.txt");
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
		int index = rand.nextInt(quotes.size()-1);
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
	
}
