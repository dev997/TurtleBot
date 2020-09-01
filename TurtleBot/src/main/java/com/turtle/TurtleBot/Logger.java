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
			writer.write(getTimeStamp(false)+" "+s+"\n");
			writer.close();
			try {
				Driver.mainPanel.setLogText(getTimeStamp(false)+"	"+s+"\n");
			}catch(Exception x) {}
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
			PrintWriter pw = new PrintWriter(writer);
			e.printStackTrace(pw);
			pw.close();
			writer.close();
			try {
				Driver.mainPanel.setLogText(getTimeStamp(false)+" "+e.toString()+"\n");
			}catch(Exception x) {}
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
		java.text.SimpleDateFormat fileform = new java.text.SimpleDateFormat("MMddYYYY");
		java.text.SimpleDateFormat stampform = new java.text.SimpleDateFormat("MM/dd/YYYY-HH:mm");
		if(forFileName) {
			return fileform.format(cal.getTime());
		}else {
			return stampform.format(cal.getTime());
		}
	}
	
}
