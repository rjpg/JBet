package bots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import categories.CategoriesManager;

import main.Parameters;
import correctscore.MessageJFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class BotAmountCat extends Bot{

	static public double LOWER_BOUND_ODD = 1.0;
	static public double UPPER_BOUND_ODD = CategoriesManager.oddCat[CategoriesManager.oddCat.length-1];
	public boolean clearFavorite=false;
	
	public int oddinterval=0;
	public int runnerPos=0;
	public RunnersData rd=null;
	
	public boolean processed10=false;
	public boolean processed2=false;
	
	//Bot Control
	public boolean activated=false;
		
	public MessageJFrame msgjf=null;
	
	public BotAmountCat(MarketData md,int runnerPosA) {
		super(md,"Amount Bot - "+runnerPosA+" - ");
		runnerPos=runnerPosA;
		initialize();
	}
	
	public void initialize()
	{
		//md.addMarketChangeListener(this);
		if(Parameters.graphicalInterfaceBots)
		{
			msgjf=new MessageJFrame("Amount Bot - "+runnerPos+" - ");
			getMsgFrame().setTitle("Amount Bot - "+runnerPos+" - ");
			msgjf.setSize(300,200);
			msgjf.setVisible(true);
		}
		//msgjf.setAlwaysOnTop(true);
	}
	
	public MessageJFrame getMsgFrame() {
		return msgjf;
	}
	
	public void clearActivation()
	{
		activated=false;
		processed10=false;
		processed2=false;
		rd=null;
	}
	
	public void activate()
	{
		
		selectRunner();
		if(rd==null)
		{
			writeMsg("No Runner Selected : Race has "+getMd().getRunners().size()+" Runners", Color.RED);
			return ;
		}
		else
		{
			writeMsg("Runner Selected :"+rd.getName() +": OK", Color.GREEN);
		}
		
		activated=true;
	}
	
	public boolean isRaceFavorite()
	{
		RunnersData rdf = Utils.getFavorite(getMd());
		if(Utils.getOddBackFrame(rdf, 0)<Parameters.ODD_FAVORITE)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void writeFile10m(int minute,int oddCat,boolean fav, double am)
	{
		BufferedWriter out=null;
		try {
				out = new BufferedWriter(new FileWriter("10m.txt", true));
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error Open statistics.txt for writing");
			}
		if(out==null)
		{
			System.err.println("could not open statistics.txt" );
			return;
		}
		
		String s=minute+" "+oddCat+" "+fav+" "+am; 
		try {
			out.write(s);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			System.out.println("SaveFav:Error wrtting data to log file");
			e.printStackTrace();
		}
		
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeFile2m(int minute,int oddCat,boolean fav, double am)
	{
		BufferedWriter out=null;
		try {
				out = new BufferedWriter(new FileWriter("2m.txt", true));
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error Open statistics.txt for writing");
			}
		if(out==null)
		{
			System.err.println("could not open statistics.txt" );
			return;
		}
		
		String s=minute+" "+oddCat+" "+fav+" "+am; 
		try {
			out.write(s);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			System.out.println("SaveFav:Error wrtting data to log file");
			e.printStackTrace();
		}
		
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void update()
	{
		
		if(rd!=null && Utils.getOddBackFrame(rd, 0)<UPPER_BOUND_ODD)
		{
			if(getMinutesToStart()>8 && getMinutesToStart()<=9 && !processed10)
			{
				double amtAvg=Utils.getMatchedAmountAVG(rd, CategoriesManager.AVGAmountFrames, 0);
				if(amtAvg>0)
				{	
					processed10=true;
					writeMsg("Amount AVG in "+ CategoriesManager.AVGAmountFrames+" frames at "+ getMinutesToStart()+"m :"+amtAvg+" Odd Cat:"+CategoriesManager.getOddCat(rd)+" Favorite:"+isRaceFavorite(), Color.BLUE);
					writeFile10m(getMinutesToStart(),CategoriesManager.getOddCat(rd),isRaceFavorite(),amtAvg);
				}
			}
			
			if(getMinutesToStart()>1 && getMinutesToStart()<=2 && !processed2)
			{
				double amtAvg=Utils.getMatchedAmountAVG(rd,  CategoriesManager.AVGAmountFrames, 0);
				if(amtAvg>0)
				{	
					processed2=true;
					//writeMsg("Amount AVG in"+  CategoriesManeger.AVGAmountFrames+" frames at "+ getMinutesToStart()+"m :"+amtAvg, Color.BLUE);
					writeMsg("Amount AVG in "+ CategoriesManager.AVGAmountFrames+" frames at "+ getMinutesToStart()+"m :"+amtAvg+" Odd Cat:"+CategoriesManager.getOddCat(rd)+" Favorite:"+isRaceFavorite(), Color.BLUE);
					writeFile2m(getMinutesToStart(),CategoriesManager.getOddCat(rd),isRaceFavorite(),amtAvg);
				}
			}
		}
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

	@Override
	public void writeMsg(String s, Color c) {
		if(msgjf==null)
		{
		//	System.out.println(getName()+": "+s);
		}
		else
		{
			msgjf.writeMessageText(s, c);
		}
		
	}

	//@Override
	public void tradeResults(RunnersData rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {
		// TODO Auto-generated method stub
		
	}

	public void selectRunner()
	{
		if(getMd().getRunners().size()>runnerPos)
		{
			rd=getMd().getRunners().get(runnerPos);
		}
		else
		{
			rd=null;
		}
	}

}
