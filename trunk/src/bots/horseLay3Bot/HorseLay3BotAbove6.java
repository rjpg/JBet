package bots.horseLay3Bot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
import bets.BetUtils;
import bots.Bot;
import bots.dutchingChaseBot.DutchingChaseOptionsPanel;
import correctscore.MessageJFrame;

public class HorseLay3BotAbove6 extends Bot{

	//Visuals
	private JFrame frame;
	private MessagePanel msgPanel;
	
	public Manager manager=null;
	
	public boolean betsPlaced=false;
	
	public boolean betsCanceled=false;
	
	public boolean finish=false;
	public boolean win=false;
	
	public Vector<BetData> bets=new Vector<BetData>();
	
	public BetData betMatched=null;
	
	// parameters 
	public int martingaleTries=40;
	public double oddActuation=3.00;
	public double initialAmount=3.00;
	//
	
	public double amount=initialAmount;
	
	public int misses=0;
	
	public HorseLay3BotAbove6(MarketData md,Manager managerA,double initStake) {
		super(md,"HorseLay3BotAbove6 - ");
		manager=managerA;
		amount=initStake;
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
		//writeMsg("MarketState :"+Utils.getMarketSateFrame(md,0)+" Market Live : "+Utils.isInPlayFrame(md,0)+ "  Minutes to start : "+getMinutesToStart(), Color.BLUE);
	
		if(Utils.getMarketSateFrame(md,0)==MarketData.SUSPENDED && Utils.isInPlayFrame(md,0)==true && !finish) //end
		{
			
			RunnersData rdLow=getMd().getRunners().get(0);
			
			for(RunnersData rdAux:getMd().getRunners())
				if(Utils.getOddBackFrame(rdAux, 0)<Utils.getOddBackFrame(rdLow, 0))
						rdLow=rdAux;
			
			writeMsg("The lower runner (winner) found is "+rdLow.getName()+" with the odd : "+ Utils.getOddBackFrame(rdLow, 0), Color.BLUE);
			
			
			if(betMatched!=null)
			{
				if(betMatched.getRd()==rdLow)
				{
					writeMsg("The mached Lay Bet was on the winner - Martingale for the nest race", Color.RED);
					misses++;
					win=false;
					
					if(misses>=martingaleTries)
					{
						writeMsg("Reset Martingale - More than "+ martingaleTries+" consecutives misses", Color.BLUE);
						misses=0;
						amount=initialAmount;
						writeMsg("Reset amount to :"+ amount, Color.BLUE);
					}
					else
					{
						writeMsg("Executing Martingale - try number "+ misses, Color.ORANGE);
						amount=(amount*(oddActuation-1.00))+amount;
						writeMsg("Seting amount to :"+ amount, Color.ORANGE);
					}
				
				}
				else
				{
					writeMsg("The mached Lay Bet was NOT on the winner", Color.GREEN);
					writeMsg("Reset Martingale ", Color.BLUE);
					misses=0;
					amount=initialAmount;
					win=true;
				}
				
			}
			else
			{
				writeMsg("No Lay Bet was Macthed ", Color.BLUE);
				writeMsg("Canceling all ", Color.BLUE);
				Vector<BetData> betsToCancel=new Vector<BetData>();
				for(BetData bdAux:bets)
					if(bdAux.getState()==BetData.UNMATCHED)
						betsToCancel.add(bdAux);
				
				if(!betsToCancel.isEmpty())
					if(getMd().getBetManager().cancelBets(betsToCancel)!=0);
					{
						betsCanceled=true;
						writeMsg("Bets were Canceled", Color.BLUE);
					}
				
			}
			
			writeMsg("Going to the next Race (finish)",Color.RED);
			writeStatisticsToFile();
			finish=true;
			// manager.MarketLiveMode(getMd());
		}
		
		if(!betsPlaced)
		{
			if(getMinutesToStart()==0)
			{
				for(RunnersData rdAux:getMd().getRunners())
				{
					if(Utils.getOddBackFrame(rdAux,0)>6.0)
					{
						writeMsg("Adding Runner to placeBet : "+rdAux.getName(), Color.BLUE);
						BetData bd=new BetData(rdAux,amount, oddActuation,BetData.LAY,true);
						bets.add(bd);
					}
				}
				
				if(!bets.isEmpty())
				{
					getMd().getBetManager().placeBets(bets);
					writeMsg("Bets were Placed", Color.BLUE);
					betsPlaced=true;
				}
			}
		}
		else if(betsCanceled==false )//betsPlaced==true
		{
			boolean someMatched=false;
			for(BetData bdAux:bets)
			{
				if(bdAux.getState()==BetData.MATCHED)
				{
					writeMsg("Bet was Matched :"+BetUtils.printBet(bdAux), Color.BLUE);
					betMatched=bdAux;
					someMatched=true;
				}
			}
			
			if(someMatched)
			{
				Vector<BetData> betsToCancel=new Vector<BetData>();
				for(BetData bdAux:bets)
					if(bdAux.getState()==BetData.UNMATCHED)
						betsToCancel.add(bdAux);
				
				if(!betsToCancel.isEmpty())
					if(getMd().getBetManager().cancelBets(betsToCancel)!=0);
					{
						betsCanceled=true;
						writeMsg("Bets were Canceled", Color.BLUE);
					}
			}
		}
	}
	
	public void writeStatisticsToFile()
	{
		 BufferedWriter out=null;
			
				try {
					out = new BufferedWriter(new FileWriter("HorseLay3BotAbove6.txt", true));
					} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Error Open HorseLay3BotAbove6.txt for writing");
					}
				if(out==null)
				{
					System.err.println("could not open HorseLay3BotAbove6.txt" );
					return;
				}
				
				String s="";
				
				if( betMatched==null)
				{
					s+="0.00 \"NO_Name\" \""+getMd().getEventName()+"\" \""+getMd().getName()+"\""; 
				}
				else
				{
					if(win)
						s+=betMatched.getAmount()+" \""+betMatched.getRd().getName()+"\" \""+getMd().getEventName()+"\" \""+getMd().getName()+"\"";
					else
						s+=((betMatched.getAmount()*(betMatched.getOddRequested()-1))*-1)+" \""+betMatched.getRd().getName()+"\" \""+getMd().getEventName()+"\" \""+getMd().getName()+"\"";
				}
				
				try {
					out.write(s);
					out.newLine();
					out.flush();
				} catch (IOException e) {
					System.out.println("HorseLay3BotAbove6:Error wrtting data to log file");
					e.printStackTrace();
				}
				
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
		
	public void newMarket(MarketData md)
	{
		writeMsg("************** NEW MARKET **************",Color.BLUE);
		setMd(md);
		
		finish=false;
		win=false;
		
		betsPlaced=false;
		betsCanceled=false;
		bets.clear();
		betMatched=null;
		
		
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
		
		msgPanel.writeMessageText(s, c);
	}

	


}
