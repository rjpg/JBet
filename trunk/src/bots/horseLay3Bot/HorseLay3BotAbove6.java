package bots.horseLay3Bot;

import generated.exchange.BFExchangeServiceStub.RacingSilkV2;
import horses.HorsesUtils;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.JFrame;

import main.Parameters;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MessagePanel;
import bets.BetData;
import bets.BetUtils;
import bots.Bot;

public class HorseLay3BotAbove6 extends Bot{

	//Visuals
	private JFrame frame;
	private MessagePanel msgPanel;
	
	public boolean betsPlaced=false;
	
	public boolean betsCanceled=false;
	
	public boolean win=false;
	
	public Vector<BetData> bets=new Vector<BetData>();
	
	public BetData betMatched=null;
	
	// parameters 
	public int martingaleTries=1;
	public double oddActuation=4.00;
	public double initialAmount=3.00;
	//
	
	public boolean useVisualInterface=false;
	
	
	public double amount=initialAmount;
	
	public int misses=0;
	
	public double raceMatchedAmount=0;
	
	public HorseLay3BotAbove6(MarketData md,double initStake) {
		super(md,"HorseLay3BotAbove6 - ");
		amount=initStake;
		initialize();
	}
	
	public void initialize()
	{
		
		setInTrade(true);
		
		if(useVisualInterface)
		{
			frame=new JFrame(this.getName());
			frame.setSize(640,480);
			
			msgPanel=new MessagePanel();
			
			frame.setContentPane(msgPanel);
			
			frame.setVisible(true);
		}
	}
	
	public void update()
	{
		//writeMsg("MarketState :"+Utils.getMarketSateFrame(md,0)+" Market Live : "+Utils.isInPlayFrame(md,0)+ "  Minutes to start : "+getMinutesToStart(), Color.BLUE);
		//if(md.getStart().get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)
		//	return;
		
		if(md.getRunners()==null
				|| md.getRunners().size()==0
				|| md.getRunners().get(0).getDataFrames().size()==0)
			return;
		
		if(Utils.isValidWindow(getMd().getRunners().get(0), 0, 0) && Utils.getMarketSateFrame(md,0)==MarketData.SUSPENDED && Utils.isInPlayFrame(md,0)==true && isInTrade()) //end
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
					writeMsg("The mached Lay Bet was on the winner - Martingale for the next race", Color.RED);
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
						
						if(amount==9) amount=6;
						
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
			setInTrade(false);
			// manager.MarketLiveMode(getMd());
		}
		
		if(!betsPlaced)
		{
			if(getSecondsToStart()<20 && getSecondsToStart()>-20)
			{
				//System.out.println("Seconds tostart :" + getSecondsToStart());
					
				raceMatchedAmount=0;
				for(RunnersData rdAux:getMd().getRunners())
				{
					raceMatchedAmount+=Utils.getMatchedAmount(rdAux, 0);
				}
				
				
				if(getMd().getRunners().size()<=3 || getMd().getRunners().size()>8)
					return;
				
				if(md.getStart().get(Calendar.HOUR_OF_DAY)<13 || md.getStart().get(Calendar.HOUR_OF_DAY)>=20.111111111111114)
					return;
				
				if(md.getStart().get(Calendar.DAY_OF_WEEK)<=1 || md.getStart().get(Calendar.DAY_OF_WEEK)>7.000000000000001)
					return;
				
				if(HorsesUtils.getTimeRaceInSeconds(getMd().getName())<60 || HorsesUtils.getTimeRaceInSeconds(getMd().getName())>=281.3333)
					return;
				                                                //  1128191,72  
				if(raceMatchedAmount<45100.2 || raceMatchedAmount>795344.4555555556)
					return;
				
				//System.out.println("minutes to start "+getMinutesToStart());
				for(RunnersData rdAux:getMd().getRunners())
				{
					
					//raceMatchedAmount+=Utils.getMatchedAmount(rdAux, 0);
					
					if(Utils.getOddBackFrame(rdAux,0)>6.0)
					{
						//if(Utils.isValidWindow(rdAux, 200, 0))
						//{
							//System.out.println("valid window");
						//	if( Utils.oddToIndex((Utils.getOddBackFrame(rdAux, 0))-Utils.oddToIndex(Utils.getOddBackFrame(rdAux, 200)))>0)
						//	{
								writeMsg("Adding Runner to placeBet : "+rdAux.getName(), Color.BLUE);
								BetData bd=new BetData(rdAux,amount, oddActuation,BetData.LAY,true);
								bets.add(bd);
						//	}
						//}
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
		writeMsg("Writing Stat file HorseLay3BotAbove6.txt",Color.RED);
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
				
				//SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
				//Calendar c=getMd().getStart();
				//dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				//String timeStart=dateFormat.format(c.getTimeInMillis());
				
				
				if( betMatched==null)
				{
					s+="0.00"; 
				}
				else
				{
					if(win)
						s+=betMatched.getMatchedAmount();
					else
						s+=((betMatched.getMatchedAmount()*(betMatched.getOddMached()-1))*-1);
				}
				
				s+=" "+getMd().getRunners().size()+" "+md.getStart().get(Calendar.HOUR_OF_DAY)+" "+md.getStart().get(Calendar.DAY_OF_WEEK)+" "+HorsesUtils.getTimeRaceInSeconds(getMd().getName())+" "+raceMatchedAmount;
				
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
		
		setInTrade(true);
		win=false;
		
		betsPlaced=false;
		betsCanceled=false;
		bets.clear();
		betMatched=null;
		
		raceMatchedAmount=0;
		
		
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
		if(useVisualInterface)
		{
			msgPanel.writeMessageText(s, c);
		}
	}

	

}
