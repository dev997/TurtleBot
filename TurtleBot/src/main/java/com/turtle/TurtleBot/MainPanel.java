package com.turtle.TurtleBot;

import java.awt.BorderLayout;
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
    	JPanel bottompanel = new JPanel();
    	frame.setLayout(new BorderLayout());
    	JPanel mainpanel = new JPanel();
    	frame.add(mainpanel, BorderLayout.CENTER);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setTitle("Turtle Bot");
    	frame.setSize(400,200);
    	
    	logArea = new JTextArea(7,32);
    	logArea.setEditable(false);
    	JScrollPane logScroller = new JScrollPane(logArea);
    	bottompanel.add(logScroller);
    	frame.add(bottompanel, BorderLayout.SOUTH);
    	
    	statusLabel = new JLabel();
    	
    	JButton restartbutton = new JButton("Restart");
    	restartbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Driver.restart();
			}
    	});
    	
    	mainpanel.add(statusLabel);
    	mainpanel.add(restartbutton);
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
