package bots;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import bets.BetData;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class BotSaveFavoriteToFile extends Bot {

	private BufferedWriter out=null;
	private RunnersData favorite=null;
	
	private int frame=0;
	
	public BotSaveFavoriteToFile(MarketData md) {
		super(md);
	}
	
	private RunnersData selectFavorite (Vector<RunnersData> runners)
	{
		
		RunnersData ret=null;
		double max=1000;
		for (RunnersData rd:runners)
		{
			if (rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack()<max)
			{
				ret=rd;
				max=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
			}
		}
		
		return ret;
	}
	
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		// TODO Auto-generated method stub
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			if(out==null)
			{
				favorite = selectFavorite(md.getRunners());
				
				if(favorite==null) 
					return;
				else
				{
					System.out.println("SaveFav: new favarite Found:"+favorite.getName());
					
					try {
						out = new BufferedWriter(new FileWriter(favorite.getName()+".txt", true));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("SaveFav: Could not open file to save:"+  favorite.getName()+".txt");
						e.printStackTrace();
					}
				}
				
			}
			
			if(out!=null && favorite!=null)
			{
				String s=frame
						+" "+favorite.getDataFrames().get(favorite.getDataFrames().size()-1).getTimestamp().getTimeInMillis()
						+" "+Utils.oddToIndex(favorite.getDataFrames().get(favorite.getDataFrames().size()-1).getOddBack())
						+" "+favorite.getDataFrames().get(favorite.getDataFrames().size()-1).getAmountBack()
						+" "+Utils.oddToIndex(favorite.getDataFrames().get(favorite.getDataFrames().size()-1).getOddLay())
						+" "+favorite.getDataFrames().get(favorite.getDataFrames().size()-1).getAmountLay()
						+" "+favorite.getDataFrames().get(favorite.getDataFrames().size()-1).getMatchedAmount()
						+" "+favorite.getDataFrames().get(favorite.getDataFrames().size()-1).getLastMatchet()
						//+" "+favorite.getData().get(favorite.getData().size()-1).getWeightmoneyBack()
						//+" "+favorite.getData().get(favorite.getData().size()-1).getWeightmoneyLay()
						//+" "+(favorite.getData().get(favorite.getData().size()-1).getWeightmoneyBack()-favorite.getData().get(favorite.getData().size()-1).getWeightmoneyLay())
						;

				try {
					out.write(s);
					out.newLine();
					out.flush();
				} catch (IOException e) {
					System.out.println("SaveFav:Error wrtting data to log file");
					e.printStackTrace();
				}
				
				
				frame++;
			}
		
		}
		
		if(marketEventType==MarketChangeListener.MarketNew)
		{
			System.out.println("SaveFav:New Market");
			try {
				if(out!=null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("SaveFav:Error closing log file"+out.toString());
			}
			out=null;
			favorite=null;
			frame=0;
		}
		

	}

	@Override
	public void writeMsg(String s, Color c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tradeResults(RunnersData rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {
		// TODO Auto-generated method stub
		
	}

	

}
