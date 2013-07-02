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

import bots.Bot;

import main.Parameters;
import correctscore.MessageJFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MessagePanel;
import GUI.MyChart2D;

public class CSHighPointLadder extends Bot{

	// States
	public static final int PRE_LIVE = 0;
	public static final int WAIT_50_MINUTES = 1;
	public static final int WAIT_ODD_UNDER_3 = 2;
	public static final int PREPARING_SWING = 4;
	public static final int EXECUTING_SWING = 5;
	public static final int END = 6;
	
	protected int STATE=PRE_LIVE;
	
	public int TRIES_IN_PREPARING_SWING=200;

	ScraperGoals sg=null;
	GameScoreData gsd=null;
	
	//Visuals
	private JFrame frame;
	private MessagePanel msgPanel;
	
	public CSHighPointLadder(MarketData md,ScraperGoals sgA) {
		super(md,"CSHighPointLadder - "+md.getEventName());
		sg=sgA;
		
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
		
		writeMsg("Games in Scrapper : ", Color.BLACK);
		GameScoreData[] gameScoreData=sg.getGamesScoreData().toArray(new GameScoreData[]{});
				
		for(GameScoreData gsd: gameScoreData)
		{
			writeMsg("TeamA:"+gsd.getTeamA()+"-"+gsd.getActualGoalsA()+"("+gsd.getPrevGoalsA()+") - "+
					"TeamB:"+gsd.getTeamB()+"-"+gsd.getActualGoalsB()+"("+gsd.getPrevGoalsB()+")",Color.BLUE);
		}
		
	}
	
	
	
	public void update()
	{
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
						
						if(oddBackAvg<15)
						{
							writeMsg("Odd AVG lower than 15.0 OK - goin to WAIT_50_MINUTES state", Color.GREEN);
							setSTATE(WAIT_50_MINUTES);
							update();
							return;
						}
						else
						{
							writeMsg("Odd AVG bigger than 15.0 OK - goin to END state", Color.RED);
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
		getMd().setUpdateInterval(1000*60*50);
		setSTATE(WAIT_ODD_UNDER_3);
	}
	
	
	private void waitOddUnder3()
	{
		
		if(getMd().getState()==MarketData.CLOSED)
		{
			writeMsg("Market is CLOSED - going to END state", Color.RED);
			setSTATE(END);
			update();
		}
		
		writeMsg("Seting Market Update to 2000", Color.BLUE);
		getMd().setUpdateInterval(2000);
		
		writeMsg("Finding the lower result...", Color.BLUE);
		
		RunnersData rdLow=getMd().getRunners().get(0);
		
		for(RunnersData rdAux:getMd().getRunners())
		{
			if(!rdAux.getName().contains("Any")) // exept Any Unquoted
			{
				if(Utils.getOddBackFrame(rdAux, 0)<Utils.getOddBackFrame(rdLow, 0))
					rdLow=rdAux;
			}
		}
		
		writeMsg("The lower runner found is "+rdLow.getName()+" with the odd : "+ Utils.getOddBackFrame(rdLow, 0), Color.BLUE);
		
		if(Utils.getOddBackFrame(rdLow, 0)>2.20 && Utils.getOddBackFrame(rdLow, 0) < 2.70)
		{
			writeMsg("The lower runner found "+rdLow.getName()+" is between 2.20 and 2.70 - going to PREPARING_SWING state", Color.BLUE);
			setSTATE(PREPARING_SWING);
		}
		
		//setSTATE(END);
	}
	
	private void preparingSwing()
	{
		if(getMd().getUpdateInterval()!=500)
		{
			writeMsg("Seting Market Update to 500", Color.BLUE);
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
		
		//testing suspended in last 30 samples 
		writeMsg("Testing suspended in last 30 samples... ", Color.BLUE);
		boolean hasBeenSuspended=false;
		int vsize=getMd().getRunners().get(0).getDataFrames().size();
		int limit=30;
		if(vsize<30)
			limit=vsize;
		for(int i=0;i<limit;i++)
		{
			if(getMd().getRunners().get(0).getDataFrames().get(vsize-1-i).getState()==MarketData.SUSPENDED)
				hasBeenSuspended=true;
		}
		
		if(hasBeenSuspended)
		{
			writeMsg("Market has been suspended in the last 30 samples - ignoring this try ", Color.YELLOW);
			return;
		}
		writeMsg("Market has not been suspended in the last 30 samples", Color.GREEN);
		
		//testing  at lest 5 variations in getLastMarketPrice()
		writeMsg("Testing at lest 5 variations in getLastMarketPrice() ... ", Color.BLUE);
		int matchedVariations=0;
		for(int i=0;i<limit;i++)
		{
			
		}
		
	}
	
	private void executingSwing()
	{
		
	}
	
	
	private void end()
	{
		writeMsg("Closing Market : "+md.getName()+" from :"+md.getEventName(),Color.BLUE);
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
			for(RunnersData rd:md.getRunners())
			{
				writeMsg(rd.getName()+" Odd Back:"+Utils.getAmountBackFrame(rd, 0)+" @ "+Utils.getOddBackFrame(rd, 0),Color.BLUE);
			}
			
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
}
