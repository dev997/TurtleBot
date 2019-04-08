package com.turtle.TurtleBot;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Driver extends ListenerAdapter{
	
	public static String COMMAND_START = "!";
	public static JDA jda;
	public static JLabel status;
	public static boolean disable;
	public static ServerManager manager;

    public static void main( String[] args ) throws Exception
    {
    	JFrame frame = new JFrame();
    	JPanel bottompanel = new JPanel(new FlowLayout());
    	frame.setLayout(new BorderLayout());
    	JPanel mainpanel = new JPanel(new FlowLayout());
    	frame.add(mainpanel, BorderLayout.CENTER);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setTitle("Turtle Bot");
    	frame.setSize(400,200);
    	frame.add(bottompanel, BorderLayout.SOUTH);
    	
    	status = new JLabel();
    	disable = false;
    	JLabel disable_text = new JLabel("Enabled");
    	
    	JButton Disable_Move = new JButton("Toggle Move Command");
    	Disable_Move.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			disable = !disable;
    			if(disable) {
    				disable_text.setText("Disabled");
    			}else {
    				disable_text.setText("Enabled");
    			}
    		}
    	});
    	
    	JButton restartbutton = new JButton("Restart");
    	restartbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				jda.shutdown();
				startUp();
				setListeners(jda, status);
			}
    	});
    	
    	mainpanel.add(status);
    	mainpanel.add(restartbutton);
    	bottompanel.add(disable_text);
    	bottompanel.add(Disable_Move);
    	frame.setVisible(true);
    	startUp();
    	setListeners(jda, status);
    }
    
    @SuppressWarnings("deprecation")
	public static void startUp() {
    	try {
    		jda = new JDABuilder(AccountType.BOT).setToken("NDc3MzYxMjIyNzkwOTM4NjI0.DlzmRA.hf_szqxGc_6xHeNTmevBAGOdq2E").buildAsync();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public static void setListeners(JDA jda, JLabel status) {
    	jda.addEventListener(new ListenerAdapter() {
    		public void onStatusChange(StatusChangeEvent e) {
    			status.setText(jda.getStatus().toString());
    			if(jda.getStatus().toString().equals("CONNECTED")) {
    				try {
    					manager.stopCellThread();
    				}catch(Exception d) {
    					d.printStackTrace();
    				}
    				manager = new ServerManager();
    				jda.addEventListener(new MsgListener(manager));
    			}
    		}
    	});
    }
}
