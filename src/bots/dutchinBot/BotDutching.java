package bots.dutchinBot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import bots.Bot;

import main.Parameters;
import correctscore.MessageJFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.SwingFrontLine;
import DataRepository.Utils;
import GUI.MessagePanel;
import GUI.MyChart2D;

public class BotDutching extends Bot{

	boolean activated=false;
	
	// graphical interface 
	JFrame frame;
	Vector<DutchingRunnerOptionsPanel> vdrop;
	JPanel runnersOptionsPanel;
	MessagePanel msgPanel;
	
	
	public BotDutching(MarketData md) {
		super(md,"BotDutching - ");
		
		initialize();
	}
	
	public void initialize()
	{
		if(Parameters.graphicalInterfaceBots)
		{
			frame=new JFrame("Dutching Bot");
			vdrop=new Vector<DutchingRunnerOptionsPanel>();
			runnersOptionsPanel=new JPanel();
			msgPanel=new MessagePanel();
		}
	}
	
	
	
	public void clearActivation()
	{
	
	}
	
	public void activate()
	{
	
	}
	
	public void activateGraphicalInterface()
	{
		if(Parameters.graphicalInterfaceBots)
		{
	
		}
	}
	
	public void updateGraphicalInterface()
	{
		if(Parameters.graphicalInterfaceBots)
		{
		}
	}

	
	public void update()
	{
		writeMsg("Minutes to start :"+getMinutesToStart(), Color.BLUE);
		updateGraphicalInterface();
		
	}
		
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketNew)
		{
			clearActivation();
			setMd(md);
			
		}
		
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
				if(!activated)
				{
					activate();
				}
				else
				{
					update();
				}
				
			
		}
		
		
	}



	
	
	public static void main(String[] args)  throws Exception {
		
	}

	@Override
	public void tradeResults(RunnersData rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {
		
		
	}

	@Override
	public void writeMsg(String s, Color c) {
		
		
	}

}
