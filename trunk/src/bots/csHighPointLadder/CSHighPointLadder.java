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
	public static final int WAIT_ODD_UNDER_3 = 1;
	public static final int EXECUTING_SWING = 2;
	public static final int END = 3;
	
	protected int STATE=PRE_LIVE;
	

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
	        case  WAIT_ODD_UNDER_3: waitOddUnder3(); break;
	        case  EXECUTING_SWING: executingSwing(); break;
	        case  END: end(); break;
	        default: ; break;
		}
	
	}

	private void preLive()
	{
	
		if(getMd().isInPlay())
		{
			writeMsg("Market turn In Play",Color.BLUE);
	
			writeMsg("Games in Scrapper : ", Color.BLACK);
			GameScoreData[] gameScoreData=sg.getGamesScoreData().toArray(new GameScoreData[]{});
					
			for(GameScoreData gsd: gameScoreData)
			{
				writeMsg("TeamA:"+gsd.getTeamA()+"-"+gsd.getActualGoalsA()+"("+gsd.getPrevGoalsA()+") - "+
						"TeamB:"+gsd.getTeamB()+"-"+gsd.getActualGoalsB()+"("+gsd.getPrevGoalsB()+")",Color.BLUE);
			}
			
			fingGameInScrapper();
			
			if(gsd==null)
				writeMsg("No Game Found in Scraper",Color.RED);
			else
				writeMsg("Game Found in Scraper : "+gsd.getTeamA()+" v "+gsd.getTeamB(),Color.GREEN);
			
			setSTATE(END);
			
		}
		
		
	}
	
	private void fingGameInScrapper()
	{
		
		String[] sarray=getMd().getEventName().split(" v ");
		
		String sMarket=sarray[0]+sarray[1];
		
		writeMsg("Teams Market Name together : "+sMarket,Color.BLUE);
	
		double mc = 0;
		
		GameScoreData[] gameScoreData=sg.getGamesScoreData().toArray(new GameScoreData[]{});
		
		for(GameScoreData gsda: gameScoreData)
		{
			String sScraper=gsda.getTeamA()+gsda.getTeamB();
			writeMsg("Teams Scrapper Name together : "+sScraper,Color.BLUE);
			
			double ma = matchedChars(sMarket.toLowerCase(), sScraper.toLowerCase());
			
			if(ma>90.0 && ma>mc)
			{
				writeMsg("New Game Score Data Found : "+gsda.getTeamA()+" v "+gsda.getTeamB(),Color.GREEN);
				gsd=gsda;
				mc=ma;
			}
		}
	}
	
	private void waitOddUnder3()
	{
		
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
