package bots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Vector;

import javax.swing.text.DefaultEditorKit.PasteAction;

import main.Parameters;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;

import correctscore.MessageJFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class StudyBot extends Bot{

	static public int WINDOW_SIZE = 3;
	
	static public int CONSTAT_PAST_SIZE = 3;
	
	static public int TICKS_MOVE = 2;
	
	public MessageJFrame msgjf=new MessageJFrame("Study Bot");
	
	public StudyBot(MarketData md) {
		super(md, "Study Bot");
		initialize();
		// TODO Auto-generated constructor stub
	}
	
	public void initialize()
	{
		
		//md.addMarketChangeListener(this);
		//msgjf.getBaseJpanel().add(getActionsPanel(),BorderLayout.EAST);
		msgjf.setVisible(true);
		//msgjf.setAlwaysOnTop(true);
		
		
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		// TODO Auto-generated method stub
		if(MarketChangeListener.MarketUpdate==marketEventType)
		{
			for(RunnersData rd:md.getRunners())
			{
//				if(isRdMovedOddInWindow(rd) && isRdConstantOddInPastWindow(rd) && rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack()<20)
//				{
//					writeMsg("Runner "+rd.getName()+" moved "+StudyBot.TICKS_MOVE+" ticks", Color.BLACK);
//					md.pause=true;
//				}
				//double womAvgP=getWomAVGWindow(rd,StudyBot.WINDOW_SIZE,StudyBot.WINDOW_SIZE);
				//double womAvgN=getWomAVGWindow(rd,StudyBot.WINDOW_SIZE,0);
				
				
//				if(		Utils.isValidWindow(rd,StudyBot.WINDOW_SIZE,StudyBot.WINDOW_SIZE) &&
//						Utils.isRdConstantOddInWindow(rd,StudyBot.WINDOW_SIZE,0)  && 
//						Utils.getOddBackFrame(rd, 0)<20 &&
//						Utils.isWomGoingUp(rd,StudyBot.WINDOW_SIZE,0)  &&
//						Utils.isAmountLayGoingDown(rd,StudyBot.WINDOW_SIZE,0) &&
//						Utils.isAmountBackGoingUp(rd,StudyBot.WINDOW_SIZE,0) &&
//						Utils.isAmountBackBiggerThanLay(rd,0) &&
//						Utils.getWomAVGWindow(rd,StudyBot.WINDOW_SIZE,StudyBot.WINDOW_SIZE) < Utils.getWomAVGWindow(rd,StudyBot.WINDOW_SIZE,0))
//				{
//					writeMsg("Runner "+rd.getName()+" going Up", Color.BLACK);
//					
//					//writeMsg("womAvgP: "+womAvgP+" womAvgN: "+womAvgN, Color.BLACK);
//					md.pause=true;
//				}
				
//				if(		Utils.isValidWindow(rd,StudyBot.WINDOW_SIZE,StudyBot.WINDOW_SIZE) &&
//						Utils.isRdConstantOddInWindow(rd,StudyBot.WINDOW_SIZE,0) && 
//						Utils.getOddBackFrame(rd, 0)<20 &&
//						Utils.isWomGoingDown(rd,StudyBot.WINDOW_SIZE,0) &&
//						Utils.isAmountLayGoingUp(rd,StudyBot.WINDOW_SIZE,0) &&
//						Utils.isAmountBackGoingDown(rd,StudyBot.WINDOW_SIZE,0) &&
//						Utils.isAmountLayBiggerThanBack(rd,0) &&
//						Utils.getWomBackFrame(rd,0) - Utils.getWomLayFrame(rd,0)<0)
//				{
					if(		Utils.isValidWindow(rd,MecanicBot.WINDOW_SIZE,StudyBot.WINDOW_SIZE) &&
							Utils.isRdConstantOddInWindow(rd,MecanicBot.WINDOW_SIZE,0) && 
							Utils.getOddBackFrame(rd, 0)<20 &&
							Utils.isWomGoingDown(rd,MecanicBot.WINDOW_SIZE-1,0) &&
							Utils.isAmountLayGoingUp(rd,MecanicBot.WINDOW_SIZE-1,0) &&
							Utils.isAmountBackGoingDown(rd,MecanicBot.WINDOW_SIZE-1,0) &&
							Utils.isAmountLayBiggerThanBack(rd,0) &&
							Utils.getWomFrame(rd, 0) < 0)
							//Utils.getWomAVGWindow(rd,MecanicBot.WINDOW_SIZE,MecanicBot.WINDOW_SIZE) > Utils.getWomAVGWindow(rd,MecanicBot.WINDOW_SIZE,0))
					{
					writeMsg("Runner "+rd.getName()+" Going Down", Color.BLACK);
					md.pause=true;
				}
				
			}
		}
		
	}

	public MessageJFrame getMsgFrame() {
		return msgjf;
	}
	
	@Override
	public void writeMsg(String s, Color c) {
		if(getMsgFrame()!=null)
			getMsgFrame().writeMessageText(s, c);
		else
			System.out.println(s);
		
	}

	@Override
	public void tradeResults(RunnersData rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {
		// TODO Auto-generated method stub
		
	}

}
