package bots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.swing.Swing;
import bets.BetData;

import statistics.Statistics;

import main.Parameters;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import correctscore.MessageJFrame;

public class MecanicBot extends Bot  implements TradeMechanismListener{

	static public int Ticks_to_Consider_Neighbour = 30; 
	
	static public double LOWER_BOUND_ODD = 1.0;
	static public double UPPER_BOUND_ODD = 20.0;
	
	static public int WINDOW_SIZE = 2;
	
	public double stake=2.50;
	
	//Bot data
	public int runnerPos=0;
	public RunnersData rd=null;
	public RunnersData rdNeighbour=null;
	public RunnersData rdFavorite=null;
	public boolean clearFavorite=false;
	
	//Bot Control
	public boolean activated=false;
	//public boolean nearEnd=false;
	
	public boolean pauseFlag=false;
	
	//visuals
	private JPanel actionsPanel=null;
	private JButton pauseButton=null;

	
	private JLabel greenLabel=null;
	private JLabel redLabel=null;
	
	private JLabel greenAmountLabel=null;
	private JLabel redAmountLabel=null;
	
	public MessageJFrame msgjf=null;
	
	
	//Statistics
	double entryRaceAmaunt=0.0;
	double horseEntryAmount=0.0;
	int entryMinute=0;
	int raceRunners=0;
	int neighbourDiff=300;
	
	//more Statistics
	Calendar scalpTimeStart;
	int entryUpDown=0;
	double entryOdd=0.0;
	double exitOdd=0.0;
	double volumeIncLay=0.0;
	double volumeIncBack=0.0;
	
	//swing
	public Swing swing;
	
	
	public MecanicBot(MarketData md,int runnerPosA) {
		super(md,"Mecanic Bot - "+runnerPosA+" - ");
		runnerPos=runnerPosA;
		initialize();
	}
	
	public void initialize()
	{
		//md.addMarketChangeListener(this);
		if(Parameters.graphicalInterfaceBots)
		{
			msgjf=new MessageJFrame("Mecanic Bot - "+runnerPos+" - ");
			getMsgFrame().setTitle("Mecanic Bot - "+runnerPos+" - ");
			msgjf.getBaseJpanel().add(getActionsPanel(),BorderLayout.SOUTH);
			msgjf.setSize(300,200);
			msgjf.setVisible(true);
		}
		//msgjf.setAlwaysOnTop(true);
	}
	
	public void update()
	{
		if(isInTrade())
			return;
		if(isNearEnd())
		{
			writeMsg("Too close to the start of the race",Color.ORANGE);
			return;
		}
/*		
		if(isRdConstantOddInWindow())
		{
			writeMsg("Odd Constant last "+MecanicBot.WINDOW_SIZE+" frames for "+rd.getName() +": OK", Color.GREEN);
		}
		else
		{
			writeMsg("Odd Not Constant last "+MecanicBot.WINDOW_SIZE+" frames for "+rd.getName() , Color.RED);
			return;
		}
			
//		System.out.println("Odd Lay second    : "+getOddLaySecond(0));
//		System.out.println("Amount Lay second : "+getAmountLaySecond(0));
//		System.out.println("Volume Lay second : "+getVolumeLaySecond(0));
//		System.out.println("Volume Lay        : "+getVolumeLay(0));
//		System.out.println("Volume Lay evol   : "+volumeLayEvolutionInWindow());
//		
//		System.out.println("Odd Back second    : "+getOddBackSecond(0));
//		System.out.println("Amount Back second : "+getAmountBackSecond(0));
//		System.out.println("Volume Back second : "+getVolumeBackSecond(0));
//		System.out.println("Volume Back        : "+getVolumeBack(0));
//		System.out.println("Volume Back evol   : "+volumeBackEvolutionInWindow());
		
		if(amountLayBiggerThanBack())
		{
			
			writeMsg("More Money On LAY for "+rd.getName() +": OK", Color.GREEN);
			if(womOnethirdNegativeInWindow())
			{
				writeMsg("WOM more than double negative last "+MecanicBot.WINDOW_SIZE+" frames for "+rd.getName() +": OK", Color.GREEN);
				
				if(amountBackGoingDown())
				{
					writeMsg("Amount on Back is going down last "+MecanicBot.WINDOW_SIZE+" frames for "+rd.getName() +": OK", Color.GREEN);
					
					//if(!clearFavorite)
					//{
					//	writeMsg("Race does NOT has Clear Favorite : "+rdFavorite.getName(), Color.GRAY);
						if(neighbourWentUp())
						{
							writeMsg("Neighbour Went UP last "+MecanicBot.WINDOW_SIZE+" frames for "+rdNeighbour.getName() +": OK", Color.GREEN);
							scalpDown();
						}
						else
						{
							writeMsg("Neighbour did NOT Went UP last "+MecanicBot.WINDOW_SIZE+" frames for "+rdNeighbour.getName(), Color.RED);
						}
					//}
					//else
					//{
					//	writeMsg("Race has Clear Favorite : "+rdFavorite.getName(), Color.GRAY);
					//	if(neighbourWentDown())
					//	{
					//		writeMsg("Neighbour Went DOWN last "+MecanicBot.WINDOW_SIZE+" frames for "+rdNeighbour.getName() +": OK", Color.GREEN);
					//		scalpDown();
					//	}
					//	else
					//	{
					//		writeMsg("Neighbour did NOT Went DOWN last "+MecanicBot.WINDOW_SIZE+" frames for "+rdNeighbour.getName(), Color.RED);
					//	}
					//}
				
				}
					
				
				else
				{
					writeMsg("Amount on Back is NOT going down last "+MecanicBot.WINDOW_SIZE+" frames for "+rd.getName(), Color.RED);
				}
				
			}
			else
			{
				writeMsg("WOM NO more than double negative last "+MecanicBot.WINDOW_SIZE+" frames for "+rd.getName(), Color.RED);
			}
			return;
		}
			
		if(amountBackBiggerThanLay())
		{
			writeMsg("More Money On Back for "+rd.getName() +": OK", Color.GREEN);
			if(womOnethirdPositiveInWindow())
			{
				writeMsg("WOM more double positive last "+MecanicBot.WINDOW_SIZE+" frames for "+rd.getName() +": OK", Color.GREEN);
				if(amountLayGoingDown())
				{
					writeMsg("Amount on Lay is going down last "+MecanicBot.WINDOW_SIZE+" frames for "+rd.getName() +": OK", Color.GREEN);
					
//					if(!clearFavorite)
//			 		{
//						writeMsg("Race does NOT has Clear Favorite : "+rdFavorite.getName(), Color.GRAY);
						if(neighbourWentDown())
						{
							writeMsg("Neighbour Went DOWN last "+MecanicBot.WINDOW_SIZE+" frames for "+rdNeighbour.getName() +": OK", Color.GREEN);
							scalpUP();
						}
						else
						{
							writeMsg("Neighbour did NOT Went Up down last "+MecanicBot.WINDOW_SIZE+" frames for "+rdNeighbour.getName(), Color.RED);
						}
//					}
//					else
//					{
//						writeMsg("Race does NOT has Clear Favorite : "+rdFavorite.getName(), Color.GRAY);
//						if(neighbourWentUp())
//						{
//							writeMsg("Neighbour Went UP last "+MecanicBot.WINDOW_SIZE+" frames for "+rdNeighbour.getName() +": OK", Color.GREEN);
//							scalpUP();
//						}
//						else
//						{
//							writeMsg("Neighbour did NOT Went UP down last "+MecanicBot.WINDOW_SIZE+" frames for "+rdNeighbour.getName() , Color.RED);
//						}
//					}
				}
				else
				{
					writeMsg("Amount on Lay is NOT going down last "+MecanicBot.WINDOW_SIZE+" frames for "+rd.getName() , Color.RED);
				}
			}
			else
			{
				writeMsg("WOM NO more double positive last "+MecanicBot.WINDOW_SIZE+" frames for "+rd.getName() , Color.RED);
			}
			return;
		}
			
		writeMsg("Diference Of money on Back/Lay is not significative",Color.RED);
		*/
	
		
		if(		Utils.isValidWindow(rd,MecanicBot.WINDOW_SIZE+1,StudyBot.WINDOW_SIZE+1) &&
				Utils.isRdConstantOddLayInWindow(rd,MecanicBot.WINDOW_SIZE,0)  && 
				Math.abs(Utils.oddToIndex(Utils.getOddBackFrame(rd, 0))-Utils.oddToIndex(Utils.getOddLayFrame(rd, 0)))<=1 &&
				Utils.getOddBackFrame(rd, 0)<20 &&
				Utils.isWomGoingUp(rd,MecanicBot.WINDOW_SIZE-1,0)  &&
				Utils.isAmountLayGoingDown(rd,MecanicBot.WINDOW_SIZE-1,0) &&
				Utils.isAmountBackGoingUp(rd,MecanicBot.WINDOW_SIZE-1,0) &&
				Utils.isAmountBackBiggerThanLay(rd,0,0.30) &&
				//Utils.isAmountBackBiggerThanLay(rd,0) &&
				//Utils.getWomFrame(rd, 0) > 0 &&
				Utils.getWomAVGWindow(rd,MecanicBot.WINDOW_SIZE+1,MecanicBot.WINDOW_SIZE+1) < Utils.getWomAVGWindow(rd,MecanicBot.WINDOW_SIZE+1,0))
		{
			
			if(clearFavorite)
			{
				if(  Utils.isRdBackWentDown(rdFavorite, 5, 0))
				{
					writeMsg("Runner "+rd.getName()+" going Up (favorite down :"+rdFavorite.getName()+")", Color.BLACK);
					double odd=Utils.getOddBackFrame(rd, 0);
					if(odd>10)
						scalpUP(1,1,100,80);
					else if(odd>5)
						scalpUP(1,1,100,80);
					else if(odd>4)
						scalpUP(2,2,100,80);
					else if(odd>3)
						scalpUP(3,2,75,60);
					else if(odd>2)
						scalpUP(3,3,75,60);
					else 
						scalpUP(4,3,50,40);
				}
			}
			else
			{
				if(  Utils.isRdBackWentDown(rdNeighbour, 5, 0))
				{
					writeMsg("Runner "+rd.getName()+" going Up (neighbour down :"+rdNeighbour.getName()+")", Color.BLACK);
					double odd=Utils.getOddBackFrame(rd, 0);
					if(odd>10)
						scalpUP(1,1,100,80);
					else if(odd>5)
						scalpUP(1,1,100,80);
					else if(odd>4)
						scalpUP(2,2,100,80);
					else if(odd>3)
						scalpUP(3,2,75,60);
					else if(odd>2)
						scalpUP(3,3,75,60);
					else 
						scalpUP(4,3,50,40);
				}
			}	
		}
		
		if(		Utils.isValidWindow(rd,MecanicBot.WINDOW_SIZE+1,StudyBot.WINDOW_SIZE+1) &&
				Utils.isRdConstantOddBackInWindow(rd,MecanicBot.WINDOW_SIZE,0)  && 
				Math.abs(Utils.oddToIndex(Utils.getOddBackFrame(rd, 0))-Utils.oddToIndex(Utils.getOddLayFrame(rd, 0)))<=1 &&
				Utils.getOddBackFrame(rd, 0)<20 &&
				Utils.isWomGoingDown(rd,MecanicBot.WINDOW_SIZE-1,0) &&
				Utils.isAmountLayGoingUp(rd,MecanicBot.WINDOW_SIZE-1,0) &&
				Utils.isAmountBackGoingDown(rd,MecanicBot.WINDOW_SIZE-1,0) &&
				Utils.isAmountLayBiggerThanBack(rd,0,0.30)  &&
				//Utils.isAmountLayBiggerThanBack(rd,0)  &&
				//Utils.getWomFrame(rd, 0) < 0)
				Utils.getWomAVGWindow(rd,MecanicBot.WINDOW_SIZE+1,MecanicBot.WINDOW_SIZE+1) > Utils.getWomAVGWindow(rd,MecanicBot.WINDOW_SIZE+1,0))
		{
			if(clearFavorite)
			{
				if(  Utils.isRdBackWentUp(rdFavorite, 5, 0))
				{
					writeMsg("Runner "+rd.getName()+" Going Down (favorite up :"+rdFavorite.getName()+")", Color.BLACK);
					//scalpDown(2,3);
					double odd=Utils.getOddBackFrame(rd, 0);
					if(odd>10)
						scalpDown(1,1,100,80);
					else if(odd>5)
						scalpDown(1,1,100,80);
					else if(odd>4)
						scalpDown(2,2,100,80);
					else if(odd>3)
						scalpDown(2,3,75,60);
					else if(odd>2)
						scalpDown(3,3,75,60);
					else 
						scalpDown(3,4,50,40);
				}
			}
			else
			{
				if(  Utils.isRdBackWentUp(rdNeighbour, 5, 0))
				{
					writeMsg("Runner "+rd.getName()+" Going Down (Neighbour up :"+rdNeighbour.getName()+")", Color.BLACK);
					//scalpDown(2,3);
					double odd=Utils.getOddBackFrame(rd, 0);
					if(odd>10)
						scalpDown(1,1,100,80);
					else if(odd>5)
						scalpDown(1,1,100,80);
					else if(odd>4)
						scalpDown(2,2,100,80);
					else if(odd>3)
						scalpDown(2,3,75,60);
					else if(odd>2)
						scalpDown(3,3,75,60);
					else 
						scalpDown(3,4,50,40);
				}
					
			}
			
		}
	}
	
	//----------- TEST CONDITIONS -------------
	public double getAmountLaySecond(int backFrame)
	{
		
		return rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).layPrices.get(1).getAmount();
	
	}
	
	public double getAmountBackSecond(int backFrame)
	{
		int index=rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).backPrices.size()-2;
		return rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).backPrices.get(index).getAmount();
	
	}
	
	public double getOddLaySecond(int backFrame)
	{
		
		return rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).layPrices.get(1).getOdd();
	
	}
	
	public double getOddBackSecond(int backFrame)
	{
		int index=rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).backPrices.size()-2;
		return rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).backPrices.get(index).getOdd();
	
	}
	
	public double getVolumeLaySecond(int backFrame)
	{
		double odd=getOddLaySecond(backFrame);
		Double ret=rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).getVolume().get(odd);
		if(ret==null)
			return -1;
		else
			return ret;
	}
	
	public double getVolumeBackSecond(int backFrame)
	{
		double odd=getOddBackSecond(backFrame);
		Double ret=rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).getVolume().get(odd);
		if(ret==null)
			return -1;
		else
			return ret;
	}
	
	public double getVolumeLay(int backFrame)
	{
		double odd=rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).getOddLay();
		Double ret=rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).getVolume().get(odd);
		
		if(ret==null)
			return -1;
		else
			return ret;
	}
	
	public double getVolumeBack(int backFrame)
	{
		double odd=rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).getOddBack();
		Double ret=rd.getDataFrames().get(rd.getDataFrames().size()-1- backFrame).getVolume().get(odd);
		if(ret==null)
			return -1;
		else
			return ret;
	}
	
	public double volumeBackEvolutionInWindow()
	{
		if(rd.getDataFrames().size()<MecanicBot.WINDOW_SIZE*2)
			return -1;
		
		double pastVol=getVolumeBack(MecanicBot.WINDOW_SIZE);
		double nowVol=getVolumeBack(0);
		
		
		if(pastVol==-1)
		{
			pastVol=0;
		}
		
		if(nowVol==-1)
		{
			nowVol=0;
		}
		
		if(nowVol-pastVol<0 )
		{
			System.out.println("Volume negative");
		}
		if(rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack()!=rd.getDataFrames().get(rd.getDataFrames().size()-1-MecanicBot.WINDOW_SIZE).getOddBack())
		{
			System.out.println("Odd are direrent");
		}
		
		return nowVol-pastVol;
	}
	
	public double volumeLayEvolutionInWindow()
	{
		if(rd.getDataFrames().size()<MecanicBot.WINDOW_SIZE*2)
			return -1;
		
		double pastVol=getVolumeLay(MecanicBot.WINDOW_SIZE);
		double nowVol=getVolumeLay(0);
		
		if(pastVol==-1 || nowVol==-1)
		{
			return -1;
		}
		
		if(pastVol==-1)
		{
			pastVol=0;
		}
		
		if(nowVol==-1)
		{
			nowVol=0;
		}
		
		if(nowVol-pastVol<0 )
		{
			System.out.println("Volume negative");
		}
		if(rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddLay()!=rd.getDataFrames().get(rd.getDataFrames().size()-1-MecanicBot.WINDOW_SIZE).getOddLay())
		{
			System.out.println("Odd are direrent");
		}
		return nowVol-pastVol;
	}
	
	public boolean isRdConstantOddInWindow()
	{
		if(rd.getDataFrames().size()<MecanicBot.WINDOW_SIZE*2)
			return false;
		
		double oddB=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
		double oddL=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddLay();
		for(int i=0;i<MecanicBot.WINDOW_SIZE+1;i++)
		{
			if(oddB!=rd.getDataFrames().get(rd.getDataFrames().size()-i-1).getOddBack())
				return false;
			if(oddL!=rd.getDataFrames().get(rd.getDataFrames().size()-i-1).getOddLay())
				return false;
		}
		return true;
	}
	
	public boolean womOnethirdNegativeInWindow()
	{
		if(rd.getDataFrames().size()<MecanicBot.WINDOW_SIZE*2)
			return false;
		
		for(int i=0;i<MecanicBot.WINDOW_SIZE;i++)
		{
			double womB=womBackPastFrame(i);
			double womL=womLayPastFrame(i);
			//System.out.println("B:"+womB);
			//System.out.println("L:"+womL);
			
			if(womL<womB*1.5)
				return false;	
		}
		return true;
	}
	
	public boolean womOnethirdPositiveInWindow()
	{
		if(rd.getDataFrames().size()<MecanicBot.WINDOW_SIZE*2)
			return false;
		
		for(int i=0;i<MecanicBot.WINDOW_SIZE;i++)
		{
			double womB=womBackPastFrame(i);
			double womL=womLayPastFrame(i);
			if(womB<womL*1.5)
				return false;
		}
		return true;
	}
	
	public boolean isWomGoingDown()
	{
		if(rd.getDataFrames().size()<MecanicBot.WINDOW_SIZE*2)
			return false;
		double womBn=womBackPastFrame(0);
		double womLn=womLayPastFrame(0);
		
		double womn=womBn-womLn;
		
		double womBp=womBackPastFrame(MecanicBot.WINDOW_SIZE);
		double womLp=womLayPastFrame(MecanicBot.WINDOW_SIZE);
		
		double womp=womBp-womLp;
		
		if(womn<womp)
			return true;
		else
			return false;
	}
	
	public boolean isWomGoingUp()
	{
		if(rd.getDataFrames().size()<MecanicBot.WINDOW_SIZE*2)
			return false;
		double womBn=womBackPastFrame(0);
		double womLn=womLayPastFrame(0);
		
		double womn=womBn-womLn;
		
		double womBp=womBackPastFrame(MecanicBot.WINDOW_SIZE);
		double womLp=womLayPastFrame(MecanicBot.WINDOW_SIZE);
		
		double womp=womBp-womLp;
		
		if(womn>womp)
			return true;
		else
			return false;
	}
	
	public double womBackPastFrame(int frame)
	{
		double womBack=0;
		double oddBack=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getOddBack();
		Vector<OddData> oddsB=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getBackPrices();						 
		for(int i=0;i<=Parameters.WOM_DIST_CENTER;i++)
		{
			double amountValueB=0;
			//System.out.println("B size:"+oddsB.size());
			for(int x=0;x<oddsB.size();x++)
			{
				if(oddsB.get(x).getOdd()==Utils.indexToOdd(Utils.oddToIndex(oddBack)-i))
				{
					amountValueB=oddsB.get(x).getAmount();
				}
			}
			womBack+=amountValueB;
		}
		return womBack;
	}
	
	public double womLayPastFrame(int frame)
	{
		double womLay=0;
		Vector<OddData> oddsL=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getLayPrices();
		double oddLay=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getOddLay();
		
		for(int i=0;i<=Parameters.WOM_DIST_CENTER;i++)
		{
			double amountValueL=0;
			//System.out.println("l size:"+oddsL.size());
			for(int x=0;x<oddsL.size();x++)
			{
				if(oddsL.get(x).getOdd()==Utils.indexToOdd(Utils.oddToIndex(oddLay)+i))
				{
					amountValueL=oddsL.get(x).getAmount();
				}
			}
			womLay+=amountValueL;
		}
		return womLay;
	}
	
	
	public boolean amountLayBiggerThanBack()
	{
		if(rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountLay() > 
		(rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountBack() * 1.5))
			return true;
		return false;
	}
	
	public boolean amountBackBiggerThanLay()
	{
		//System.out.println("L:"+rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountLay());
		//System.out.println("B:"+rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountBack());
		
		if(rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountBack() > 
		(rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountLay() * 1.5))
			return true;
		return false;
	}
	
	public boolean amountBackGoingDown()
	{
		if(rd.getDataFrames().get(rd.getDataFrames().size()-1-MecanicBot.WINDOW_SIZE).getAmountBack() > 
		  (rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountBack()))
			return true;
		return false;
	}
	
	public boolean amountBackGoingUp()
	{
		if(rd.getDataFrames().get(rd.getDataFrames().size()-1-MecanicBot.WINDOW_SIZE).getAmountBack() < 
		  (rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountBack()))
			return true;
		return false;
	}
	
	public boolean amountLayGoingDown()
	{
		if(rd.getDataFrames().get(rd.getDataFrames().size()-1-MecanicBot.WINDOW_SIZE).getAmountLay() > 
		  (rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountLay()))
			return true;
		return false;
	}
	
	public boolean amountLayGoingUp()
	{
		if(rd.getDataFrames().get(rd.getDataFrames().size()-1-MecanicBot.WINDOW_SIZE).getAmountLay() < 
		  (rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountLay()))
			return true;
		return false;
	}
	
	public boolean neighbourWentDown()
	{
		if(rdNeighbour.getDataFrames().size()<MecanicBot.WINDOW_SIZE*2)
			return false;
		/*
		double odd=rdNeighbour.getDataFrames().get(rdNeighbour.getDataFrames().size()-1).getOddBack();
		for(int i=0;i<MecanicBot.WINDOW_SIZE;i++)
		{
			if(odd>rd.getDataFrames().get(rd.getDataFrames().size()-i-1).getOddBack())
				return false;
			odd=rd.getDataFrames().get(rd.getDataFrames().size()-i-1).getOddBack();
		}
		*/
		if(rdNeighbour.getDataFrames().get(rdNeighbour.getDataFrames().size()-1-MecanicBot.WINDOW_SIZE).getOddBack() > 
		  (rdNeighbour.getDataFrames().get(rdNeighbour.getDataFrames().size()-1).getOddBack()))
			return true;
		
		return false;
	}
	
	public boolean neighbourWentUp()
	{
		if(rdNeighbour.getDataFrames().size()<MecanicBot.WINDOW_SIZE*2)
			return false;
		
		/*double odd=rdNeighbour.getDataFrames().get(rdNeighbour.getDataFrames().size()-1).getOddBack();
		for(int i=0;i<MecanicBot.WINDOW_SIZE;i++)
		{
			if(odd<rd.getDataFrames().get(rd.getDataFrames().size()-i-1).getOddBack())
				return false;
			odd=rd.getDataFrames().get(rd.getDataFrames().size()-i-1).getOddBack();
		}
		*/
		if(rdNeighbour.getDataFrames().get(rdNeighbour.getDataFrames().size()-1-MecanicBot.WINDOW_SIZE).getOddBack() < 
		  (rdNeighbour.getDataFrames().get(rdNeighbour.getDataFrames().size()-1).getOddBack()))
			return true;
		
		return false;
	}
	
	//----------- END TEST CONDITIONS -------------
	
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
	
	public void selectNeighbourAndFavorite()
	{
		double rdOdd=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
		int neighbourOddDiff=300;
		RunnersData neighbourAux=null;
		double oddMin=1000.00;
		
		for(RunnersData rdaux:getMd().getRunners())
		{
			  if(rdaux!=rd)
			  {
				  double auxOdd=rdaux.getDataFrames().get(rdaux.getDataFrames().size()-1).getOddBack();
				  int diff=Math.abs(Utils.oddToIndex(rdOdd)-Utils.oddToIndex(auxOdd));
				  if(diff<neighbourOddDiff)
				  {
					  neighbourOddDiff=diff;
					  neighbourAux=rdaux;
				  }
			  }
			  if(rdaux.getDataFrames().get(rdaux.getDataFrames().size()-1).getOddBack()<oddMin)
			  {
				  oddMin=rdaux.getDataFrames().get(rdaux.getDataFrames().size()-1).getOddBack();
				  rdFavorite=rdaux;
			  }
		}
		rdNeighbour=neighbourAux;
	}
	
	boolean activatedDataEnv=false;
	public void activateDataEvn()
	{
		if(!activatedDataEnv && getMd().getRunners().size()>0) //initialize as soon we get a valid time
		{
			horseEntryAmount=rd.getDataFrames().get(rd.getDataFrames().size()-1).getMatchedAmount();
			entryRaceAmaunt=getAmountRace();
			entryMinute=getMinutesToStart();
			raceRunners=getMd().getRunners().size();
			activatedDataEnv=true;
		}

	}
	
	public void activate()
	{
		if(getMd().getCurrentTime()==null)
		{
			return;
		}
		Calendar currentTime=(Calendar) getMd().getCurrentTime().clone();
		Calendar startTime=getMd().getStart();
		
		currentTime.add(Calendar.MINUTE, 10);
		
		if(startTime.compareTo(currentTime)<0)
		{
			writeMsg("We are inside the 10m : OK", Color.GREEN);
			
			
			
		}
		else
		{
			writeMsg("We are not inside the 10m : Not initialized", Color.RED);
			return;
		}
		
		
		
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
		
		if(rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack()<MecanicBot.LOWER_BOUND_ODD
				|| rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack()>MecanicBot.UPPER_BOUND_ODD)
		{
			writeMsg("Selected Runner("+rd.getName()+") is outside ODD bounds ", Color.RED);
			return;
		}
		else
		{
			writeMsg("Selected Runner("+rd.getName()+") is inside ODD bounds : OK", Color.GREEN);
		}
		
		
		
		if(getMsgFrame()!=null)
		{
			getMsgFrame().setTitle(this.getName()+" "+rd.getName());
		}
		
		
		
		selectNeighbourAndFavorite();
		if(rdNeighbour==null)
		{
			writeMsg("No Neighbour Found", Color.RED);
			return;
		}
		else
		{
			writeMsg("Neighbour Found : "+ rdNeighbour.getName() +": OK", Color.GREEN);
		}
		
		double rdOdd=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
		double rdNeighbourOdd=rdNeighbour.getDataFrames().get(rdNeighbour.getDataFrames().size()-1).getOddBack();
		neighbourDiff=Math.abs(Utils.oddToIndex(rdOdd)-Utils.oddToIndex(rdNeighbourOdd));
		
		if(neighbourDiff>MecanicBot.Ticks_to_Consider_Neighbour)
		{
			writeMsg("Neighbour odd is too far:"+rd.getName()+":"+rdOdd+" "+rdNeighbour.getName()+":"+rdNeighbourOdd+" Ticks Distance:"+neighbourDiff, Color.RED);
			return;
		}
		else
		{
			writeMsg("Neighbour odd is near:"+rd.getName()+":"+rdOdd+" "+rdNeighbour.getName()+":"+rdNeighbourOdd+" Ticks Distance:"+neighbourDiff+" : OK", Color.GREEN);
		}
		
		if(rdFavorite==null)
		{
			writeMsg("Favorite not fount", Color.RED);
			return;
		}
		
		double favoriteOdd=rdFavorite.getDataFrames().get(rdFavorite.getDataFrames().size()-1).getOddBack();
		if(favoriteOdd<Parameters.ODD_FAVORITE)
		{
			writeMsg("Favorite ("+rdFavorite.getName()+") is Clear Favorite", Color.ORANGE);
			clearFavorite=true;
		}
		else
		{
			writeMsg("Favorite ("+rdFavorite.getName()+") is NOT Clear Favorite: OK", Color.GREEN);
			clearFavorite=false;
		}
		
		
		
		
		activated=true;
	}
	
	public void clearActivation()
	{
		writeMsg("Clear Activation", Color.BLUE);
		activated=false;
		rd=null;
		rdNeighbour=null;
		rdFavorite=null;
		clearFavorite=false;
		
		///////// DATA ENV for logging Statistics
		activatedDataEnv=false;
		entryRaceAmaunt=0.0;
		entryMinute=0;
		raceRunners=0;
		//nearEnd=false;
		if(getMsgFrame()!=null)
		{
			getMsgFrame().setTitle(this.getName());
		}
	}
	
	public double getAmountRace()
	{
		double am=0;
		for(RunnersData rdc:getMd().getRunners())
		{
			am+=rdc.getDataFrames().get(rdc.getDataFrames().size()-1).getMatchedAmount();
		}
		return am;
	}
	
	public boolean isNearEnd()
	{
		//if(nearEnd==true)
		//	return true;
		
		
		Calendar currentTime = (Calendar) getMd().getCurrentTime().clone();
		// System.out.println("currentTime:"+currentTime);
		Calendar startTime = getMd().getStart();

		currentTime.add(Calendar.MINUTE, 1);
		//System.out.println("currentTime:"+currentTime);
		if (startTime.compareTo(currentTime) < 0) {
			writeMsg("We are near end", Color.GREEN);
			// nearEnd=true;
			return true;

		}

		return false;
	}
	
	public JPanel getActionsPanel()
	{
		if(actionsPanel==null)
		{
			actionsPanel=new JPanel();
			actionsPanel.setLayout(new BorderLayout());
			JPanel auxPanel=new JPanel();
			auxPanel.setLayout(new BorderLayout());
			auxPanel.add(getPauseButton(),BorderLayout.CENTER);
			actionsPanel.add(auxPanel,BorderLayout.SOUTH);
			
			JPanel auxPanel2=new JPanel();
			auxPanel2.setLayout(new GridLayout(2,2));
			auxPanel2.add(getGreenLabel());
			auxPanel2.add(getGreenAmountLabel());
			auxPanel2.add(getRedLabel());
			auxPanel2.add(getRedAmountLabel());
			actionsPanel.add(auxPanel2,BorderLayout.CENTER);
		}
		return actionsPanel;
	}
	

	public void scalpUP(int ticksUpA,int ticksDownA, int closeTime, int emergencyTime)
	{
		if(!isInTrade())
		{
			writeMsg("Start Processing Scalping UP",Color.BLUE);
			double oddLay=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
			writeMsg("Odd Lay ("+rd.getName()+"):"+oddLay, Color.BLACK);
			scalpTimeStart=rd.getDataFrames().get(rd.getDataFrames().size()-1).getTimestamp();
			volumeIncLay = volumeLayEvolutionInWindow();
			volumeIncBack = volumeBackEvolutionInWindow();
			setInTrade(true);
			swing=new Swing(MecanicBot.this,rd, stake, oddLay, 50,closeTime+50, emergencyTime+50 ,BetData.LAY,ticksUpA+1,ticksDownA+2,true);
			
			//swing=new SwingFrontLine(md,rd, stake, oddLay, closeTime,emergencyTime, MecanicBot.this,1,ticksUpA, ticksDownA);
			
			//swing=new Swing(md,rd, 0.20, oddLay,  100,80, MecanicBot.this,1,2, 1);  60,30
			//scalping=new Scalping(md,rd, 0.20, oddLay, 100,80, MecanicBot.this,1);
		}
		else
			writeMsg("Processing Last Trade",Color.RED);
	}
	

	
	public void scalpDown(int ticksUpA,int ticksDownA, int closeTime, int emergencyTime)
	{
		if(!isInTrade())
		{
			writeMsg("Start Processing Scalping DOWN",Color.BLUE);
			double oddBak=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddLay();
			writeMsg("Odd back ("+rd.getName()+"):"+oddBak, Color.BLACK);
			scalpTimeStart=rd.getDataFrames().get(rd.getDataFrames().size()-1).getTimestamp();
			volumeIncLay = volumeLayEvolutionInWindow();
			volumeIncBack = volumeBackEvolutionInWindow();
			setInTrade(true);
			swing=new Swing(MecanicBot.this,rd, stake, oddBak, 50,closeTime+50, emergencyTime+50 ,BetData.BACK,ticksDownA+1,ticksUpA+2,true);
			
			//swing=new SwingFrontLine(md,rd, stake, oddBak, closeTime,emergencyTime, MecanicBot.this,-1,ticksUpA,ticksDownA);
			
			//swing=new Swing(md,rd, 0.20, oddBak, 100,80, MecanicBot.this,-1,1,2);
			
			//scalping=new Scalping(md,rd, 0.20, oddBak, 100,80, MecanicBot.this,-1);
		}
		else
			writeMsg("Processing Last Trade",Color.RED);
	}
	
	
	public JLabel getGreenLabel()
	{
		if (greenLabel==null)
		{
			greenLabel=new javax.swing.JLabel("Greens:0",JLabel.CENTER);
			greenLabel.setForeground(Color.WHITE);
			greenLabel.setBackground(Color.GREEN);
			greenLabel.setOpaque(true);
			greenLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return greenLabel;
	}
	
	public JLabel getGreenAmountLabel()
	{
		if (greenAmountLabel==null)
		{
			greenAmountLabel=new javax.swing.JLabel("(0.00)",JLabel.CENTER);
			greenAmountLabel.setForeground(Color.GREEN);
			//greenAmountLabel.setBackground(Color.GREEN);
			//greenAmountLabel.setOpaque(true);
			greenAmountLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return greenAmountLabel;
	}
	
	public JLabel getRedLabel()
	{
		if (redLabel==null)
		{
			redLabel=new javax.swing.JLabel("Reds:0",JLabel.CENTER);
			redLabel.setForeground(Color.WHITE);
			redLabel.setBackground(Color.RED);
			redLabel.setOpaque(true);
			redLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return redLabel;
	}
	
	public JLabel getRedAmountLabel()
	{
		if (redAmountLabel==null)
		{
			redAmountLabel=new javax.swing.JLabel("(0.00)",JLabel.CENTER);
			redAmountLabel.setForeground(Color.RED);
			//greenAmountLabel.setBackground(Color.GREEN);
			//greenAmountLabel.setOpaque(true);
			redAmountLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return redAmountLabel;
	}
	
	public JButton getPauseButton()
	{
		if(pauseButton==null)
		{
			if(pauseFlag==true)
				pauseButton=new JButton("Start");
			else
				pauseButton=new JButton("Pause");
			pauseButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(!pauseFlag)
					{
						pauseFlag=true;
						pauseButton.setText("Continue");
					}
					else
					{
						pauseFlag=false;
						pauseButton.setText("Pause");
					}	
					
				}
			});
		}
		return pauseButton;
	}
	
	public MessageJFrame getMsgFrame() {
		return msgjf;
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
	
		if( marketEventType==MarketChangeListener.MarketNew)
		{
			setMd(md);
			clearActivation();
			
			
		}
		
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			if(!pauseFlag)
				if(!activated)
				{
					activate();
				}
				else
				{
					if(!activatedDataEnv)
							activateDataEvn();
					
					update();
				}
			
		
		}
		
	}

	@Override
	public void setInTrade(boolean inTrade) {
		super.setInTrade(inTrade);
		
	}
	@Override
	public void writeMsg(String s, Color c) {
		if(getMsgFrame()!=null)
			getMsgFrame().writeMessageText(s, c);
	//	else
	//		System.out.println(s);
	}
	
	@Override
	public void setGreens(int greens) {
		super.setGreens(greens);
		if(getMsgFrame()!=null)
			greenLabel.setText("Greens:"+greens);
		
	}

	@Override
	public void setReds(int reds) {
		super.setReds(reds);
		if(getMsgFrame()!=null)
			redLabel.setText("reds:"+reds);
	}
	
	@Override
	public void setAmountGreen(double amountGreenA) {
		super.setAmountGreen(amountGreenA);
		if(getMsgFrame()!=null)
			greenAmountLabel.setText("("+getAmountGreen()+")");
	}

	@Override
	public void  setAmountRed(double amountRedA) {
		super.setAmountRed(amountRedA);
		if(getMsgFrame()!=null)
			redAmountLabel.setText("("+getAmountRed()+")");
	}

	public void tradeResults(String rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {
		
		Statistics.writeStatistics(entryMinute, entryRaceAmaunt, horseEntryAmount, md.getRunners().size(), clearFavorite, rdFavorite.getName(), scalpTimeStart.getTimeInMillis(), redOrGreen, entryUpDown, entryOdd, exitOdd, ticksMoved, stake, exitStake , amountMade, getMinutesToStart(), rd, neighbourDiff, rdNeighbour.getName(),volumeIncBack,volumeIncLay,scalpTimeStart.get(Calendar.DAY_OF_WEEK));
		
	}

	@Override
	public void tradeMechanismChangeState(TradeMechanism tm, int state) {
		System.out.println("Tm state : "+tm.getState());
		
	}

	@Override
	public void tradeMechanismEnded(TradeMechanism tm, int state) {
		if(tm instanceof Swing)
		{
			setInTrade(false);
			String[] fields=tm.getStatisticsFields().split(" ");
			String[] values=tm.getStatisticsValues().split(" ");
			           
			String msg="----- Swing Statistics -----\n";
			
			for(int i=0;i<fields.length;i++)
			{
				msg+="["+i+"] "+fields[i]+" : "+values[i]+"\n";
			}
			
			msg+="------------ || ------------";
			writeMsg(msg,Color.BLUE);
			
			if(values[0].equals("CLOSED"))
			{
				
				int ticksMoved=Integer.parseInt(values[17]);
				double pl=Double.parseDouble(values[16]);
				int direction=1;
				if(values[10].equals("BL"))
					direction=-1;
				int redOrGreen=1;
				if(pl<0)
				{
					redOrGreen=-1;
					
					setReds(getReds()+1);
					setAmountRed(getAmountRed()+pl);
				}
				else
				{
					setGreens(getGreens()+1);
					setAmountGreen(getAmountGreen()+pl);
				}
					
				
				double entryOdd=Double.parseDouble(values[18]);
				double exitOdd=Double.parseDouble(values[19]);
				
				double entryStake=Double.parseDouble(values[20]);
				double exitStake=Double.parseDouble(values[21]);
				
				tradeResults(values[3], redOrGreen, direction,entryOdd, exitOdd, entryStake, exitStake, pl, ticksMoved);
				
			}
				
			
		}
		
		tm.removeTradeMechanismListener(this);
		
	}

	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {
		writeMsg(msg,color);
		
	}

	
	
}
