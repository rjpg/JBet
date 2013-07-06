package bots.horseLay3Bot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import main.Manager;
import main.Parameters;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MessagePanel;
import GUI.MyChart2D;
import bets.BetData;
import bots.Bot;
import bots.dutchingChaseBot.DutchingChaseOptionsPanel;
import correctscore.MessageJFrame;

public class HorseLay3Bot extends Bot{

	//Visuals
	private JFrame frame;
	private MessagePanel msgPanel;
	
	public Manager manager=null;
	
	public boolean betsPlaced=false;
	
	public boolean betsCanceled=false;
	
	
	public Vector<BetData> bets=new Vector<BetData>();
	
	public HorseLay3Bot(MarketData md,Manager managerA) {
		super(md,"HorseLay3Bot - ");
		manager=managerA;
		initialize();
	}
	
	public void initialize()
	{
		frame=new JFrame(this.getName());
		frame.setSize(640,480);
		
		msgPanel=new MessagePanel();
		
		frame.setContentPane(msgPanel);
		
		frame.setVisible(true);
		
	}
	
	public void update()
	{
		writeMsg("MarketState :"+Utils.getMarketSateFrame(md,0)+" Market Live : "+Utils.isInPlayFrame(md,0)+ "  Minutes to start : "+getMinutesToStart(), Color.BLUE);
	
		if(Utils.getMarketSateFrame(md,0)==MarketData.SUSPENDED && Utils.isInPlayFrame(md,0)==true) //end
		{
			
			RunnersData rdLow=getMd().getRunners().get(0);
			
			for(RunnersData rdAux:getMd().getRunners())
				if(Utils.getOddBackFrame(rdAux, 0)<Utils.getOddBackFrame(rdLow, 0))
						rdLow=rdAux;
			
			writeMsg("The lower runner found is "+rdLow.getName()+" with the odd : "+ Utils.getOddBackFrame(rdLow, 0), Color.BLUE);
			
			writeMsg("Going to the next Race",Color.RED);
			manager.MarketLiveMode(getMd());
		}
		
		if(!betsPlaced)
		{
			if(getMinutesToStart()==0)
			{
				RunnersData rdLow=getMd().getRunners().get(0);
				
				for(RunnersData rdAux:getMd().getRunners())
					if(Utils.getOddBackFrame(rdAux, 0)<Utils.getOddBackFrame(rdLow, 0))
							rdLow=rdAux;
				
				RunnersData rdLow2=getMd().getRunners().get(0);
				
				if(rdLow2==rdLow)
					rdLow2=getMd().getRunners().get(1);
				
				for(RunnersData rdAux:getMd().getRunners())
					if(Utils.getOddBackFrame(rdAux, 0)<Utils.getOddBackFrame(rdLow2, 0) && rdAux!=rdLow)
							rdLow2=rdAux;
				
				writeMsg("At minute 0 the lower runner found is "+rdLow.getName()+" @ "+ Utils.getOddBackFrame(rdLow, 0)+" . The 2nd lower runner found is "+rdLow2.getName()+" @ "+ Utils.getOddBackFrame(rdLow2, 0), Color.BLUE);
				
				
			}
		}
	}
		
	public void newMarket(MarketData md)
	{
		writeMsg("************** NEW MARKET **************",Color.BLUE);
		setMd(md);
		
		betsPlaced=false;
		betsCanceled=false;
		bets.clear();
	}
	
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketNew)
			newMarket(md);
			
		if(marketEventType==MarketChangeListener.MarketUpdate)
			update();			
	}

	@Override
	public void writeMsg(String s, Color c) {
		
		msgPanel.writeMessageText(s, color);
	}

	


}
