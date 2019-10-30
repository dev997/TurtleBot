package com.turtle.TurtleBot;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.*;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Driver{
	
	public static String COMMAND_START = "!";
	public static JDA jda;
	public static JLabel status;
	public static ServerManager manager;
	public static Properties props;

    public static void main( String[] args ) throws Exception
    {
    	try {
    		configSetup();
    	}catch (Exception e) {
    		Logger.getInstance().log("Could not finish config setup");
    	}
    	
    	JFrame frame = new JFrame();
    	JPanel bottompanel = new JPanel(new FlowLayout());
    	frame.setLayout(new BorderLayout());
    	JPanel mainpanel = new JPanel(new FlowLayout());
    	frame.add(mainpanel, BorderLayout.CENTER);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setTitle("Turtle Bot");
    	frame.setSize(300,100);
    	frame.add(bottompanel, BorderLayout.SOUTH);
    	
    	status = new JLabel();
    	
    	JButton restartbutton = new JButton("Restart");
    	restartbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				restart();
			}
    	});
    	
    	mainpanel.add(status);
    	mainpanel.add(restartbutton);
    	frame.setVisible(true);
    	startUp();
    	setListeners(jda, status);
    }
    
    public static void startUp() {
    	try {
    		jda = new JDABuilder(AccountType.BOT).setToken(props.getProperty("token")).build();
    	}catch(Exception e) {
    		Logger logger = Logger.getInstance();
    		logger.log(e);
    		e.printStackTrace();
    	}
    }
    
    public static void setListeners(JDA jda, JLabel status) {
    	ArrayList<Object> removeset = new ArrayList<Object>();
    	for(Object al : jda.getRegisteredListeners()) {
    		removeset.add(al);
    	}
    	for(Object obj : removeset) {
    		jda.removeEventListener(obj);
    	}
    	jda.addEventListener(new ListenerAdapter() {
    		public void onStatusChange(StatusChangeEvent e) {
    			status.setText(jda.getStatus().toString());
    			if(jda.getStatus().toString().equals("CONNECTED")) {
    				manager = new ServerManager();
    				
    				for(Object al : jda.getRegisteredListeners()) {
    					if(al instanceof MsgListener) {
    						jda.removeEventListener(al);
    					}
    				}
    				jda.addEventListener(new MsgListener(manager));
    			}
    		}
    	});
    	Logger logger = Logger.getInstance();
    	logger.log("STARTUP: Set Listeners");
    }
    
    public static void restart() {
    	Logger logger = Logger.getInstance();
    	logger.log("Restarting");
    	jda.shutdownNow();
    	startUp();
    	setListeners(jda, status);
    }
    
    public static void configSetup() throws IOException{
    	try {
    		props = new Properties();
    		props.load(Driver.class.getClassLoader().getResourceAsStream("src/main/config.cfg"));
    	}catch(Exception e) {
    		Logger.getInstance().log("Writing new config file");
    		props = new Properties();
    		props.setProperty("token", "");
    		
    		props.store(new FileOutputStream("src/main/config.cfg"), null);
    	}
    }
}
