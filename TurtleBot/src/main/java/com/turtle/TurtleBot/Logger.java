package com.turtle.TurtleBot;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;

public class Logger {
	private static Logger logger_instance = null;
	
	private Logger() {
		
	}
	
	public static Logger getInstance() {
		if(logger_instance == null) {
			logger_instance = new Logger();
		}
		
		return logger_instance;
	}
	
	public void log(String s) {
		try {
			Path path = FileSystems.getDefault().getPath("log");
			if(!Files.exists(path)) {
				Files.createDirectory(path);
			}
			FileWriter writer = new FileWriter("log\\"+getLogFile()+".txt", true);
			writer.write(getTimeStamp(false)+"	"+s+"\n");
			writer.close();
		}catch(Exception x) {
			x.printStackTrace();
		}
	}
	
	public void log(Exception e) {
		try {
			Path path = FileSystems.getDefault().getPath("log");
			if(!Files.exists(path)) {
				Files.createDirectory(path);
			}
			FileWriter writer = new FileWriter("log\\"+getLogFile()+".txt", true);
			writer.write(getTimeStamp(false)+"	"+e.toString()+"\n");
			PrintWriter pw = new PrintWriter("log\\"+getLogFile()+".txt");
			e.printStackTrace(pw);
			writer.close();
		}catch(Exception x) {
			x.printStackTrace();
		}
	}
	
	
	private static String getLogFile() {
		String ds = "log-"+getTimeStamp(true);
		return ds;
	}
	
	private static String getTimeStamp(boolean forFileName) {
		Calendar cal = Calendar.getInstance();
		if(forFileName) {
			return Integer.toString(cal.get(Calendar.MONTH+1))+Integer.toString(cal.get(Calendar.DAY_OF_MONTH))+Integer.toString(cal.get(Calendar.YEAR));
		}else {
			return Integer.toString(cal.get(Calendar.MONTH+1))+Integer.toString(cal.get(Calendar.DAY_OF_MONTH))+Integer.toString(cal.get(Calendar.YEAR))+
					"-"+Integer.toString(cal.get(Calendar.HOUR))+":"+Integer.toString(cal.get(Calendar.MINUTE));
		}
	}
	
}
