package bots.horseLay3Bot;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.JFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MessagePanel;
import bets.BetData;
import bets.BetUtils;
import bots.Bot;

public class HorseLayEnd extends Bot{

	//Visuals
	private JFrame frame;
	private MessagePanel msgPanel;
	
	public boolean betsPlaced=false;
	
	public boolean betsCanceled=false;
	
	public Vector<BetData> bets=new Vector<BetData>();
	
	public BetData betMatched=null;
	
	public BetData betCloseMatched=null;
	
	public boolean win=false;
	
	// parameters 
	public int martingaleTries=1;
	public double oddActuation=1.01;
	public double oddExit=1.05;
	public double initialAmount=3.00;
	//
	
	public double amount=initialAmount;
	
	public int misses=0;
	
	public boolean useVisualInterface=false;
	
	public double volumeDiff=0;
	
	
	public HorseLayEnd(MarketData md,double initStake) {
		super(md,"HorseLayEnd - ");
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
		
		if(Utils.isValidWindow(getMd().getRunners().get(0), 0, 0) && Utils.getMarketSateFrame(md,0)==MarketData.SUSPENDED && Utils.isInPlayFrame(md,0)==true && isInTrade()) //end
		{
			
			
			writeMsg("End of race", Color.BLUE);
			
			
			if(betMatched!=null)
			{
				if(betCloseMatched.getState()!=BetData.MATCHED)
				{
					writeMsg("The close bet was not matched  - Martingale for the nest race", Color.RED);
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
					writeMsg("The close bet was mached", Color.GREEN);
					writeMsg("Reset Martingale ", Color.BLUE);
					misses=0;
					amount=initialAmount;
					double volumeDiffaux=0;
					for(int i=0;i<20;i++)
					{
						volumeDiffaux+=Utils.getVolumeFrame(betMatched.getRd(), 0, Utils.indexToOdd(Utils.oddToIndex(oddExit+i)));
					}
					volumeDiff=volumeDiffaux-volumeDiff;
					
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
			
			writeMsg("Going to the next Race",Color.RED);
			writeStatisticsToFile();
			setInTrade(false);
			//manager.MarketLiveMode(getMd());
		}
		
		if(!betsPlaced)
		{
			if(getMinutesToStart()==0)
			{
							
				
				for(RunnersData rdAux:getMd().getRunners())
				{
					BetData bd=new BetData(rdAux,amount, oddActuation,BetData.LAY,true);
					bets.add(bd);
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
				{
					if(getMd().getBetManager().cancelBets(betsToCancel)!=0);
					{
						betsCanceled=true;
						writeMsg("Bets were Canceled", Color.BLUE);
					}
				}
				betCloseMatched=new BetData(betMatched.getRd(),amount, oddExit,BetData.BACK,true);
				getMd().getBetManager().placeBet(betCloseMatched);
				writeMsg("Close bet was placed :"+BetUtils.printBet(betCloseMatched), Color.BLUE);
				for(int i=0;i<20;i++)
				{
					volumeDiff+=Utils.getVolumeFrame(betMatched.getRd(), 0, Utils.indexToOdd(Utils.oddToIndex(oddExit+i)));
				}
				
			}
			
			
		}
	}
	
	public void writeStatisticsToFile()
	{
		String fileName="HorseLayEnd.txt";
		writeMsg("Writing Stat file "+fileName,Color.RED);
		 BufferedWriter out=null;
			
				try {
					out = new BufferedWriter(new FileWriter(fileName, true));
					} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Error Open "+fileName+" for writing");
					}
				if(out==null)
				{
					System.err.println("could not open "+fileName );
					return;
				}
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
				Calendar c=getMd().getStart();
				//dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				String timeStart=dateFormat.format(c.getTimeInMillis());
				
				String s="";
				
				if( betCloseMatched==null)
				{
					s+="0.00 "+getMd().getRunners().size()+" "+timeStart+" \"NO_Name\" \""+getMd().getEventName()+"\" \""+getMd().getName()+"\""; 
				}
				else
				{
					if(win)
						s+=(betCloseMatched.getMatchedAmount()*(betCloseMatched.getOddMached()-1.01))+" "+getMd().getRunners().size()+" "+timeStart+" \""+betMatched.getRd().getName()+"\" \""+getMd().getEventName()+"\" \""+getMd().getName()+"\"";
					else
						s+=((betMatched.getMatchedAmount()*(betMatched.getOddMached()-1))*-1)+" "+getMd().getRunners().size()+" "+timeStart+" \""+betMatched.getRd().getName()+"\" \""+getMd().getEventName()+"\" \""+getMd().getName()+"\"";
				}
				
				s+=" "+volumeDiff;
				
				try {
					out.write(s);
					out.newLine();
					out.flush();
				} catch (IOException e) {
					System.out.println( fileName +" :Error wrtting data to log file");
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
		betCloseMatched=null;
		
		volumeDiff=0;
		
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
