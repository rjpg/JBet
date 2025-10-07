package bot123;

import java.awt.Color;

import bets.BetData;
import bots.Bot;


import DataRepository.MarketData;
import DataRepository.RunnersData;
import GUI.MarketMainFrame;
import GUI.MyChart2D;

public class Bot123 extends Bot {

	public MarketMainFrame mmf;
	
	public RunnersData rd;
	
	public MyChart2D oddBackChart=null;
	
	public Bot123(MarketData mdA, MarketMainFrame mmfA)
	{
		super(mdA);
		//setMd(mdA);
		mmf=mmfA;
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeMsg(String s, Color c) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void tradeResults(RunnersData rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {
		// TODO Auto-generated method stub
		
	}



	
	




}
