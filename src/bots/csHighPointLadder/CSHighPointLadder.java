package bots.csHighPointLadder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import scrapers.GameScoreData;
import scrapers.xscores.ScraperGoals;

import bets.BetData;
import bots.Bot;
import bots.ManualPlaceBetBot;

import main.Parameters;
import correctscore.MessageJFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MessagePanel;
import GUI.MyChart2D;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.TradeMechanismUtils;
import TradeMechanisms.swing.Swing;
import TradeMechanisms.swing.SwingOptions;

public class CSHighPointLadder extends Bot implements TradeMechanismListener{

	// States
	public static final int PRE_LIVE = 0;
	public static final int WAIT_50_MINUTES = 1;
	public static final int WAIT_ODD_UNDER_3 = 2;
	public static final int PREPARING_SWING = 4;
	public static final int EXECUTING_SWING = 5;
	public static final int END = 6;
	
	protected int STATE=PRE_LIVE;
	
	public int TRIES_IN_PREPARING_SWING=600;

	public boolean end_runned=false;
	
	public Swing swing=null;
	
	//Visuals
	private JFrame frame;
	private MessagePanel msgPanel;
	
	public CSHighPointLadder(MarketData md) {
		super(md,"CSHighPointLadder - "+md.getEventName());
		
		
		initialize();
	}
	
	public void initialize()
	{
		
		frame=new JFrame(this.getName());
		frame.setSize(640,480);
		
		msgPanel=new MessagePanel();
		
		frame.setContentPane(msgPanel);
		
		frame.setVisible(true);
		
		writeMsg("Processing Game : "+getMd().getEventName(), Color.BLUE);
		
		/*
		writeMsg("Games in Scrapper : ", Color.BLACK);
		GameScoreData[] gameScoreData=sg.getGamesScoreData().toArray(new GameScoreData[]{});
				
		for(GameScoreData gsd: gameScoreData)
		{
			writeMsg("TeamA:"+gsd.getTeamA()+"-"+gsd.getActualGoalsA()+"("+gsd.getPrevGoalsA()+") - "+
					"TeamB:"+gsd.getTeamB()+"-"+gsd.getActualGoalsB()+"("+gsd.getPrevGoalsB()+")",Color.BLUE);
		}
		*/
	}
	
	
	
	public void update()
	{
		if (end_runned) return;
		switch (STATE) {
	        case  PRE_LIVE: preLive(); break;
	        case  WAIT_50_MINUTES: wait50minutes(); break;
	        case  WAIT_ODD_UNDER_3: waitOddUnder3(); break;
	        case  PREPARING_SWING: preparingSwing(); break;
	        case  EXECUTING_SWING: executingSwing(); break;
	        case  END: end(); break;
	        default: ; break;
		}
	
	}

	
	private void preLive()
	{
	
		if(Utils.getMarketMathedAmount(getMd(), 0)<2000)
		{
			writeMsg("No sufucient liquidity in market (<2000): "+Utils.getMarketMathedAmount(getMd(), 0), Color.RED);
			setSTATE(END);
			update();
			return;
		}
		else
		{
			writeMsg("Sufucient liquidity in market (>2000): "+Utils.getMarketMathedAmount(getMd(), 0), Color.GREEN);
		}
		
		
		if(!getMd().isInPlay())
		{
			RunnersData rd=getMd().getRunners().get(0);
			if(rd.getDataFrames().size()>20)
			{
				writeMsg("20 samples captured in pre-Live", Color.BLUE);
				for(RunnersData rdAux:getMd().getRunners())
				{
					if(rdAux.getName().contains("0 - 0"))
					{
						writeMsg("Runner 0 - 0 found ID :"+rdAux.getId(), Color.BLUE);
						
						double oddBackAvg=0;
						for(int i=0;i<20;i++)
						{
							oddBackAvg+=Utils.getOddBackFrame(rdAux, i);
						}
						
						oddBackAvg/=20;
						
						writeMsg("Runner 0 - 0 Odd AVG :"+oddBackAvg, Color.BLUE);
						
						if(oddBackAvg<20)
						{
							writeMsg("Odd AVG lower than 20.0 OK - goin to WAIT_50_MINUTES state", Color.GREEN);
							setSTATE(WAIT_50_MINUTES);
							update();
							return;
						}
						else
						{
							writeMsg("Odd AVG bigger than 20.0 OK - goin to END state", Color.RED);
							setSTATE(END);
							update();
							return;
						}
					}
				}
				
				return;
			}
			
			return;
		
		}
		else
		{
			writeMsg("No sufucient pre-live samples - goin to END state", Color.RED);
			setSTATE(END);
			update();
			return;
		}
		
		
	}
	
	
	private void wait50minutes()
	{
		writeMsg("Next sample will be recieved in 50 minutes - goin to WAIT_ODD_UNDER_3 state", Color.BLUE);
		getMd().setUpdateInterval(1000*60*60);
		
		setSTATE(WAIT_ODD_UNDER_3);
	}
	
	
	private void waitOddUnder3()
	{
		
		if(getMd().getState()==MarketData.CLOSED)
		{
			writeMsg("Market is CLOSED - going to END state", Color.RED);
			setSTATE(END);
			update();
			return;
		}
		
		if(getMd().getUpdateInterval()!=3000)
		{
			writeMsg("Seting Market Update to 3000", Color.BLUE);
			getMd().setUpdateInterval(3000);
		}
		writeMsg("Finding the lower result...", Color.BLUE);
		
		RunnersData rdLow=getMd().getRunners().get(0);
		
		for(RunnersData rdAux:getMd().getRunners())
		{
			if(!rdAux.getName().contains("Any")) // except Any Unquoted
			{
				if(Utils.getOddBackFrame(rdAux, 0)<Utils.getOddBackFrame(rdLow, 0))
					rdLow=rdAux;
			}
		}
		
		writeMsg("The lower runner found is "+rdLow.getName()+" with the odd : "+ Utils.getOddBackFrame(rdLow, 0), Color.BLUE);
		
		if(Utils.getOddBackFrame(rdLow, 0) > 2.20 && Utils.getOddBackFrame(rdLow, 0) < 2.28)
		{
			writeMsg("The lower runner found "+rdLow.getName()+" is between 2.20 and 2.28 - going to PREPARING_SWING state", Color.ORANGE);
			setSTATE(PREPARING_SWING);
		}
		
		//setSTATE(END);
	}
	
	private void preparingSwing()
	{
		
		if(getMd().getState()==MarketData.CLOSED)
		{
			writeMsg("Market is CLOSED - going to END state", Color.RED);
			setSTATE(END);
			update();
			return;
		}
		
		if(getMd().getUpdateInterval()!=500)
		{
			writeMsg("Setting Market Update to 500", Color.BLUE);
			getMd().setUpdateInterval(500);	
		}
		writeMsg("In PREPARING_SWING state - tries left : "+TRIES_IN_PREPARING_SWING, Color.BLUE);
		
		TRIES_IN_PREPARING_SWING--;
		if(TRIES_IN_PREPARING_SWING<=0)
		{
			writeMsg("Tries left in PREPARING_SWING state are over - going to END state", Color.RED);
			setSTATE(END);
			update();
		}
		
		
		// finding the low runner except any unquoted
		RunnersData rdLow=getMd().getRunners().get(0);
		
		for(RunnersData rdAux:getMd().getRunners())
		{
			if(!rdAux.getName().contains("Any")) // except Any Unquoted
			{
				if(Utils.getOddBackFrame(rdAux, 0)<Utils.getOddBackFrame(rdLow, 0))
					rdLow=rdAux;
			}
		}
		
		writeMsg("The lower runner found is "+rdLow.getName()+" with the odd : "+ Utils.getOddBackFrame(rdLow, 0), Color.BLUE);
		
		
		//testing suspended in last 30 samples 
		writeMsg("Testing suspended in last 30 samples... ", Color.BLUE);
		boolean hasBeenSuspended=false;
		int vsize=rdLow.getDataFrames().size();
		int limit=30;
		if(vsize<30)
			limit=vsize;
		for(int i=0;i<limit;i++)
		{
			if(rdLow.getDataFrames().get(vsize-1-i).getState()==MarketData.SUSPENDED)
				hasBeenSuspended=true;
		}
		
		if(hasBeenSuspended)
		{
			writeMsg("Market has been suspended in the last 30 samples - ignoring this try ", Color.YELLOW);
			return;
		}
		writeMsg("Market has not been suspended in the last 30 samples", Color.GREEN);
		
		
		
		writeMsg("Testing odd Back AVG in last 15 samples (2.16 < oddBackAvg < 2.24)... ", Color.BLUE);
		double oddBackAvg=Utils.getOddBackFrame(rdLow, /*(int)(limit/2),*/ 0);
		if(oddBackAvg>2.16 && oddBackAvg<2.28)
		{
			writeMsg("Odd Back AVG is 2.16 < oddBackAvg("+oddBackAvg+") < 2.28 ", Color.GREEN);
		}
		else
		{
			writeMsg("Odd Back AVG does not meet condition 2.16 < oddBackAvg("+oddBackAvg+") < 2.28 - ignoring this try ", Color.ORANGE);
			return;
		}
		
//		//testing  at lest 5 variations in getLastMarketPrice()		
//		writeMsg("Testing at lest 5 variations in getLastMarketPrice() ... ", Color.BLUE);		
//		int matchedVariations=0;
//		double last=rdLow.getDataFrames().get(vsize-1).getLastMatchet();
//		for(int i=0;i<limit;i++)
//		{
//			if(last!=rdLow.getDataFrames().get(vsize-1-i).getLastMatchet())
//			{
//				matchedVariations++;
//				last=rdLow.getDataFrames().get(vsize-1-i).getLastMatchet();
//			}
//		}
//		
//		if(matchedVariations>5)
//		{
//			writeMsg("There was more than 5 variations on last Matched Pricer in the last 30 samples", Color.GREEN);
//		}
//		else
//		{
//			writeMsg("There was not more than 5 variations on last Matched Pricer in the last 30 samples - ignoring this try", Color.RED);
//			return;
//		}
		
		//Testing Amounts for Lay 
		writeMsg("Testing Odds Lay...", Color.BLUE);	
		double oddLay=Utils.getOddLayFrame(rdLow, 0);
		if(oddLay<2.30 && oddLay!=0)
		{
			writeMsg("Odd Lay under 2.30 - OK (Runner : "+rdLow.getName()+" @ "+oddLay+")", Color.GREEN);
		}
		else
		{
			writeMsg("Odd Lay is above 2.30- ignoring this try (Runner : "+rdLow.getName()+" @ "+oddLay+")", Color.GREEN);
			return;
		}
		
		writeMsg("Selecting entry Odd... ", Color.BLUE);		
		double entryOdd=Utils.getOddBackFrame(rdLow, 0);
		if(entryOdd<2.16)
			entryOdd=2.16;
		
		if(entryOdd>2.20)
			entryOdd=2.20;
		writeMsg("Entry Odd : "+entryOdd, Color.BLUE);
		
		writeMsg("Starting Swing ...", Color.BLUE);
		
		int stopProfit=Utils.getTicksDiff(entryOdd, 2.02);
		
		BetData betOpen=new BetData(rdLow,
				3.00,
				entryOdd,
				BetData.BACK,
				false);
		
		SwingOptions so=new SwingOptions(betOpen, this);
		so.setWaitFramesOpen(60*2);      // 1 minute
		so.setWaitFramesNormal(60*3*2);   // 3 minutes
		so.setWaitFramesBestPrice(60*2);  // 1 minute
		so.setTicksProfit(stopProfit);
		so.setTicksLoss(5);
		so.setForceCloseOnStopLoss(false);
		so.setInsistOpen(false);
		so.setGoOnfrontInBestPrice(false);
		so.setUseStopProfifInBestPrice(true);
		so.setPercentageOpen(0.80);   // if 80% is open go to close  
		so.setDelayBetweenOpenClose(-1);
		so.setDelayIgnoreStopLoss(50);
		so.setUpdateInterval(TradeMechanism.SYNC_MARKET_DATA_UPDATE);
		
			
		swing=new Swing(so);
		writeMsg("Swing Started - going to state EXECUTING_SWING", Color.BLUE);	
		setSTATE(EXECUTING_SWING);
		
		
	}
	
	private void executingSwing()
	{
		if(getMd().getState()==MarketData.CLOSED)
		{
			writeMsg("Market is CLOSED - going to END state", Color.RED);
			setSTATE(END);
			update();
			return;
		}
		
		if(swing==null)
		{
			writeMsg("Swing is null - going to END state", Color.RED);
			setSTATE(END);
			update();
			return;
		}
		
		if(swing.isEnded())
		{
			String[] fields=swing.getStatisticsFields().split(" ");
			String[] values=swing.getStatisticsValues().split(" ");
			           
			String msg="----- Swing Statistics -----\n";
			
			for(int i=0;i<fields.length;i++)
			{
				msg+="["+i+"] "+fields[i]+" : "+values[i]+"\n";
			}
			
			msg+="------------ || ------------";
			writeMsg(msg,Color.BLUE);
			writeMsg("Swing has ended - going to END state", Color.BLUE);
			setSTATE(END);
			update();
			return;
		}
		else
		{
			writeMsg("Swing has not ended - Swing State: "+ TradeMechanismUtils.getStateString(swing.getState()), Color.BLUE);
		}
		
		
	}
	
	
	private void end()
	{
		writeMsg("Closing Market : "+md.getName()+" from :"+md.getEventName(),Color.BLUE);
		end_runned=true;
		md.removeMarketChangeListener(this);
		md.stopPolling();
		md.clean();
		md=null;
	}

	public int getSTATE() {
		return STATE;
	}

	public void setSTATE(int sTATE) {
		STATE = sTATE;
	}


	@Override
	public void writeMsg(String s, Color c) {
	
		msgPanel.writeMessageText(s, c);
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			/*for(RunnersData rd:md.getRunners())
			{
				writeMsg(rd.getName()+" Odd Back:"+Utils.getAmountBackFrame(rd, 0)+" @ "+Utils.getOddBackFrame(rd, 0),Color.BLUE);
			}*/
			
			update();
			
		}
		
	}

	
	
    /* ------------------------ String manipulation ... Start ---------------------*/
    public int contain(char c,String ss)
    {
    	int x=0;
    	char[] s=ss.toCharArray();
    	
    	
    	for(int i=0;i<s.length;i++)
    	{
    		if(s[i]==c)
    			x++;
    	}
    	return x;
    }
    
  
    /**
     * The Strings must not have repeted chars (use removeRepitedChar(String ss))
     * @param a
     * @param b
     * @return
     */
    public int numberMatchedChars(String a,String b)
    {
    	int ret=0;
    	char[] s=a.toCharArray();
    	
    	for(int i=0;i<s.length;i++)
    	{
    		if(contain(s[i],b)!=0)
    		{
    			ret++;
    		}
    	}
    	
    	return ret;
    }
    
    /**
     * The Strings must not have repeted chars (use removeRepitedChar(String ss))
     * @param a
     * @param b
     * @return
     */
    public double matchedChars(String a,String b)
    {
    	double ret=0.0;
    	String sa=null;
    	String sb=null;
    	if(a.length()<b.length())
    	{
    		sa=a;
    		sb=b;
    	}
    	else
    	{
    		sa=b;
    		sb=a;
    	}
    	
    	int smallsize=sa.length();
    	
    	int matched=numberMatchedChars(sa,sb);
    	
    	ret=((double)matched*100.)/(double)smallsize;
    	
    	
    	return ret;
    	
    }
    
    public String removeRepitedChar(String ss)
    {
    	String ret="";
    	
    	char[] s=ss.toCharArray();
    	
    	for(int i=0;i<s.length;i++)
    	{
    		if(contain(s[i],ret)==0)
    		{
    			//System.out.println(contain(s[i],ret)+"ret:"+ret+":char:"+s[i]+":");
    			ret+=s[i]+"";
    		}
    	}
    	
    	
    	return ret;
    }
    
    public String removeChar(String ss,char c)
    {
    	String ret="";
    	
    	char[] s=ss.toLowerCase().toCharArray();
    	
    	for(int i=0;i<s.length;i++)
    	{
    		if(s[i]!=c)
    		{
    			//System.out.println(contain(s[i],ret)+"ret:"+ret+":char:"+s[i]+":");
    			ret+=s[i]+"";
    		}
    	}
    	
    	return ret;
    }

    /* ------------------------ String manipulation ... END ---------------------*/ 
    
	@Override
	public void tradeMechanismChangeState(TradeMechanism tm, int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tradeMechanismEnded(TradeMechanism tm, int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {
		writeMsg("[Swing msg] "+msg, color);
		
	}
    

}
