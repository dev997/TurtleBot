package com.turtle.TurtleBot;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class MainPanel {
	
	JLabel statusLabel;
	JTextArea logArea;
	
	public MainPanel() {
		initializeComponents();
	}
	
	public void initializeComponents() {
		JFrame frame = new JFrame();
    	JPanel toppanel = new JPanel();
    	frame.setLayout(new BorderLayout());
    	JPanel mainpanel = new JPanel(new BorderLayout());
    	mainpanel.add(toppanel, BorderLayout.NORTH);
    	frame.add(mainpanel, BorderLayout.CENTER);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setTitle("Turtle Bot");
    	frame.setSize(400,200);
    	
    	logArea = new JTextArea();
    	logArea.setEditable(false);
    	JScrollPane logScroller = new JScrollPane(logArea);
    	mainpanel.add(logScroller);
    	
    	statusLabel = new JLabel();
    	
    	JButton restartbutton = new JButton("Restart");
    	restartbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Driver.restart();
			}
    	});
    	
    	toppanel.add(statusLabel);
    	toppanel.add(restartbutton);
    	frame.setVisible(true);
	}
	
	public JLabel getStatusLabel() {
		return statusLabel;
	}
	
	public void setStatusLabelText(String text) {
		statusLabel.setText(text);
	}
	
	public void setLogText(String text) {
		logArea.setText(logArea.getText()+text);
	}
	
}
