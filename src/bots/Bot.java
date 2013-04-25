package bots;

import java.awt.Color;

import javax.swing.JFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;

public abstract class Bot implements MarketChangeListener{

	public Bot(MarketData md)
	{
		setMd(md);
	}
	
	public Bot(MarketData md,String nameA)
	{
		setMd(md);
		name=nameA;
	}
	
	public abstract void writeMsg(String s, Color c);
	
	public abstract void tradeResults(RunnersData rd,int redOrGreen, int entryUpDown, double entryOdd, double exitOdd, double stake,double exitStake,double amountMade,int ticksMoved);
	
	public boolean inTrade=false;
	
	// Bot Info
	public String name="Generic Bot";
	
	public int id=0;
	
	// Market info
	public MarketData md=null;
		
	//statistics
	public int greens=0;
	public int reds=0;
	
	public double amountGreen=0.00;
	public double amountRed=0.00;
	
	
	//Visuals
	public JFrame frame=new JFrame();
	public Color color=Color.BLACK;
	
	public int getGreens() {
		return greens;
	}

	public int getReds() {
		return reds;
	}

	public MarketData getMd() {
		return md;
	}

	public void setMd(MarketData mdA) {
		if(this.md!=null)
		{
			System.out.println("Remove listener");
			md.removeMarketChangeListener(this);
			md.removeBotTrading(this);
		}
		
		this.md = mdA;
		
		if(this.md!=null)
		{
			md.addMarketChangeListener(this);
			md.addBotTrading(this);
		}
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public JFrame getFrame() {
		return frame;
	}
	public boolean isInTrade() {
		return inTrade;
	}

	public void setInTrade(boolean inTrade) {
		this.inTrade = inTrade;
	}
	
	public void setGreens(int greens) {
		this.greens = greens;
	}

	public void setReds(int reds) {
		this.reds = reds;
	}
	
	public void setAmountGreen(double amountGreenA) {
		this.amountGreen = amountGreenA;
	}

	public void setAmountRed(double amountRedA) {
		this.amountRed = amountRedA;
	}
	
	public double getAmountGreen() {
		return amountGreen;
	}

	public double getAmountRed() {
		return amountRed;
	}
	
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getMinutesToStart()
	{
		if(getMd()!=null)
		{
			long nowMin=getMd().getCurrentTime().getTimeInMillis();
			long startMin=getMd().getStart().getTimeInMillis();
			long sub=startMin-nowMin;
		
			return (int)(sub/60000);
		}
		else
			return -1;
	}

	
}
