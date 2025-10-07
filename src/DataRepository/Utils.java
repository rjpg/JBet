package DataRepository;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import main.Parameters;
import bfapi.handler.ExchangeAPI;
import bfapi.handler.GlobalAPI;
import bfapi.handler.ExchangeAPI.Exchange;
import bots.MecanicBot;
import bots.StudyBot;

import demo.util.APIContext;
import demo.util.Display;


public class Utils {
	
	
	private static int STEPS=350;
	private static double[] ladder =new double[STEPS];
	private static OddObj[] ladderOddObj=new OddObj[STEPS];
	
	public static double[] getLadder() {
		return ladder;
	}
	
	public static OddObj[] getLadderOddObj() {
		return ladderOddObj;
	}
	
	public static String getFormatedDouble(double d)
	{
		DecimalFormat df = new DecimalFormat("#.##");
	    DecimalFormatSymbols symbols=df.getDecimalFormatSymbols();
	    symbols.setDecimalSeparator('.');
	    df.setDecimalFormatSymbols(symbols);
	    return df.format(d);
	}
	
	public static double convertAmountToBF(double am)
	{
		
		double ret=0.0;
		DecimalFormat df = new DecimalFormat("#.##");
	    DecimalFormatSymbols symbols=df.getDecimalFormatSymbols();
	    symbols.setDecimalSeparator('.');
	    df.setDecimalFormatSymbols(symbols);
	    
	    ret=Double.parseDouble(df.format(am));
		return ret;
		//return am;
	}

	public static void init()
	{
		 DecimalFormat df = new DecimalFormat("#.##");
	     DecimalFormatSymbols symbols=df.getDecimalFormatSymbols();
	     symbols.setDecimalSeparator('.');
	     df.setDecimalFormatSymbols(symbols);
	        
	        
		
		
		ladder[0]=1.01;
		for(int i=1;i<1600;i++)
		{
			if(i>=1&&i<100)
			{
				ladder[i]=Double.parseDouble(df.format(ladder[i-1]+0.01));
			}
			if(i>=100&&i<150)
			{
				ladder[i]=Double.parseDouble(df.format(ladder[i-1]+0.02));
			}
			if(i>=150&&i<170)
			{
				ladder[i]=Double.parseDouble(df.format(ladder[i-1]+0.05));
			}
			if(i>=170&&i<190)
			{
				ladder[i]=Double.parseDouble(df.format(ladder[i-1]+0.1));
			}
			if(i>=190&&i<210)
			{
				ladder[i]=Double.parseDouble(df.format(ladder[i-1]+0.2));
			}
			
			if(i>=210&&i<230)
			{
				ladder[i]=Double.parseDouble(df.format(ladder[i-1]+0.5));
			}
			
			if(i>=230&&i<240)
			{
				ladder[i]=Double.parseDouble(df.format(ladder[i-1]+1));
			}
			if(i>=240&&i<250)
			{
				ladder[i]=Double.parseDouble(df.format(ladder[i-1]+2));
			}
			
			if(i>=250&&i<260)
			{
				ladder[i]=Double.parseDouble(df.format(ladder[i-1]+5));
			}
			if(i>=260&&i<350)
			{
				ladder[i]=Double.parseDouble(df.format(ladder[i-1]+10));
			}
			

			
			//double x=0.5;
			//ladder[i]=2.32+x;
		}
		
		for(int i=0;i<ladder.length;i++)
		{
			ladderOddObj[i]=new OddObj(ladder[i],i);
		}
		
	}
	
	public static OddObj getOddObjByOdd(double odd)
	{
		int index=Utils.oddToIndex(odd);
		if(index==-1)
			return null;
		else
			return ladderOddObj[index];
	}
	
	public static OddObj getOddObjByIndex(int index)
	{
		return ladderOddObj[index];
	}
	
	public static int oddToIndex(double odd)
	{
		int ret=0;
		
		for(ret=0;ret<STEPS;ret++)
			if(ladder [ret]==odd)
				return ret;
		return -1;
		/*if(odd>=1.01f && odd<2.f)
			ret=(int)(odd*100)-100;
		if(odd>=2.0f && odd<3.f)
		{System.out.println("entrei="+(2.34f*100.000f));
			ret=((int)(((int)(odd*100)-200))/2)+100;
		}*/
	
	}
	
	public static double indexToOdd(int index)
	{
		/*float ret=0.f;
		if(index>=1 && index<100)
			ret=(((float)index+100)/100);
		if(index>=100 && index<200)
			ret=((float)((float)((int)(index-100)*2))/100f)+2;
		return ret;*/
		if(index<ladder.length && index>=0)
			return ladder[index];
		else
			return -1;
	}
	
	public static int ladderSize()
	{
		return ladder.length;
	}
	
	public static boolean validOddIndex(int index)
	{
		if(index>=0 && index<ladder.length)
			return true;
		else
			return false;
	}
	
	public static double nearValidOdd(double odd)
	{
		double dist=10000;
		int index=-1;
		for(int i=0;i<ladder.length;i++)
		{
			if( Math.abs(ladder[i]-odd)<dist)
			{
				//System.out.println("Math.abs(ladder["+i+"]("+ladder[i]+")-"+odd+"="+ Math.abs(ladder[i]-odd)+"   <"+dist);
				dist= Math.abs(ladder[i]-odd);
				index=i;
			}		
		}
		if(index==-1)
		{
			System.err.println("invalid odd");
			return 0;
		}
		else
			return ladder[index];
	}
	
	public static double calculateOddAverage(double[] odds,double[] amounts)
	{
		//Bet 1 = Odds * Stake
		//Bet 2 = Odds * Stake

		//(Bet 1 + Bet 2 ) / Total Stake = Average Odds.
		
		//double bets[]=new double[odds.length];
		double totalStake=0;
		double totalBets=0;
		
		for(int i=0;i<odds.length;i++)
		{
			totalBets+=(odds[i]*amounts[i]);
			totalStake+=amounts[i];
		}
		//System.out.println("AVG ODD:"+(totalBets/totalStake));
	
		//return Utils.nearValidOdd(totalBets/totalStake); near valid odd d� muito edge
		// n�o fazer near mas sim erredondar para 2 casas
		if(totalStake==0)
			return 0.00;
		else
			return Utils.convertAmountToBF(totalBets/totalStake);
	}
	
	public static double calculateOddAverage(Double[] odds,Double[] amounts)
	{
		//Bet 1 = Odds * Stake
		//Bet 2 = Odds * Stake

		//(Bet 1 + Bet 2 ) / Total Stake = Average Odds.
		
		//double bets[]=new double[odds.length];
		double totalStake=0;
		double totalBets=0;
		
		for(int i=0;i<odds.length;i++)
		{
			totalBets+=(odds[i]*amounts[i]);
			totalStake+=amounts[i];
		}
		//System.out.println("AVG ODD:"+(totalBets/totalStake));
	
		//return Utils.nearValidOdd(totalBets/totalStake); near valid odd d� muito edge
		// n�o fazer near mas sim erredondar para 2 casas
		if(totalStake==0)
			return 0.00;
		else
		//	return Utils.convertAmountToBF(totalBets/totalStake);
			return totalBets/totalStake;
	}
	
	public static boolean validOdd(double odd)
	{
		for(int i=0;i<ladder.length;i++)
		{
			if(ladder[i]==odd)
				return true;
		}
		return false;
	}
	
	public static double closeAmountLay(double initialOddBack,double AmountBack,double oddLay)
	{
		//valor a apostar em Lay (ODD BACK / ODD LAY)  X  Stake
		
		return (initialOddBack/oddLay)*AmountBack;
	}
	
	public static double closeAmountBack(double initialOddLay,double AmountLay,double oddBack)
	{
		 // Valor a Aposatr em Back = (ODD LAY / ODD BACK ) X Stake
		return (initialOddLay/oddBack)*AmountLay;
	}
	
	
	/////////// RUNNER UTILS /////////////
	public static RunnersData getFavorite(MarketData md)
	{
		RunnersData rdLow=md.getRunners().get(0);
		
		for(RunnersData rdAux:md.getRunners())
		{
			if(Utils.getOddBackFrame(rdAux, 0)<Utils.getOddBackFrame(rdLow, 0))
					rdLow=rdAux;
		}
		
		return rdLow;
		
	}
	
	public static RunnersData getFavorite(MarketData md,int pastFrame)
	{
		RunnersData rdLow=md.getRunners().get(0);
		
		for(RunnersData rdAux:md.getRunners())
		{
			if(Utils.getOddBackFrame(rdAux, pastFrame)<Utils.getOddBackFrame(rdLow, pastFrame))
					rdLow=rdAux;
		}
		
		return rdLow;
		
	}
	
	public static RunnersData getNeighbour(RunnersData rd)
	{
		MarketData md=rd.getMarketData();
		double rdOdd=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
		int neighbourOddDiff=Utils.oddToIndex(1000);
		RunnersData neighbourAux=null;
	
		
		for(RunnersData rdaux:md.getRunners())
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
		}
		return neighbourAux;
	}
	
	public static RunnersData getNeighbour(RunnersData rd, int pastFrame)
	{
		MarketData md=rd.getMarketData();
		double rdOdd=getOddBackFrame(rd, pastFrame);
				
				
		int neighbourOddDiff=Utils.oddToIndex(1000);
		RunnersData neighbourAux=null;
	
		
		for(RunnersData rdaux:md.getRunners())
		{
			  if(rdaux!=rd)
			  {
				  double auxOdd=getOddBackFrame(rdaux, pastFrame);
				  int diff=Math.abs(Utils.oddToIndex(rdOdd)-Utils.oddToIndex(auxOdd));
				  if(diff<neighbourOddDiff)
				  {
					  neighbourOddDiff=diff;
					  neighbourAux=rdaux;
				  }
			  }
		}
		return neighbourAux;
	}
	
	public static RunnersData getNeighbour(MarketData md,RunnersData rd)
	{
		double rdOdd=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
		int neighbourOddDiff=Utils.oddToIndex(1000);
		RunnersData neighbourAux=null;
	
		
		for(RunnersData rdaux:md.getRunners())
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
		}
		return neighbourAux;
	}
	
	public static RunnersData getNeighbour(MarketData md,RunnersData rd,int n)
	{
		double rdOdd=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
		
		int ticksDifAux=-1;
		RunnersData neighbourAux=null;
		for(int i=0;i<n+1;i++)
		{
			//System.out.println("Estou no cclo");
			RunnersData rdaux=getNeighbourMinDiff(md, rd,ticksDifAux);
			if(rdaux==null)
			{
				//System.out.println("� null");
				return null;
				
			}
			double auxOdd=rdaux.getDataFrames().get(rdaux.getDataFrames().size()-1).getOddBack();
			ticksDifAux=Math.abs(Utils.oddToIndex(rdOdd)-Utils.oddToIndex(auxOdd));
			neighbourAux=rdaux;
		}
		//System.out.println(neighbourAux);
		return neighbourAux;
	}
	
	public static RunnersData getNeighbourMinDiff(MarketData md,RunnersData rd,int ticksMinDiff)
	{
		double rdOdd=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
		int neighbourOddDiff=Utils.oddToIndex(1000);
		RunnersData neighbourAux=null;
	
		
		for(RunnersData rdaux:md.getRunners())
		{
			  if(rdaux!=rd)
			  {
				  double auxOdd=rdaux.getDataFrames().get(rdaux.getDataFrames().size()-1).getOddBack();
				  int diff=Math.abs(Utils.oddToIndex(rdOdd)-Utils.oddToIndex(auxOdd));
				  if(diff<neighbourOddDiff && diff<ticksMinDiff)
				  {
					  neighbourOddDiff=diff;
					  neighbourAux=rdaux;
				  }
			  }
		}
		return neighbourAux;
	}
	
	public static int getTicksDiff(double oddA,double oddB)
	{
		return Math.abs(Utils.oddToIndex(oddA)-Utils.oddToIndex(oddB));
	}
	
	public static int getTicksDiff(RunnersData rdA,RunnersData rdB)
	{
		double auxOddA=rdA.getDataFrames().get(rdA.getDataFrames().size()-1).getOddBack();
		double auxOddB=rdB.getDataFrames().get(rdB.getDataFrames().size()-1).getOddBack();
		int diff=Math.abs(Utils.oddToIndex(auxOddA)-Utils.oddToIndex(auxOddB));
		return diff;
	}
	
	public static int getTicksDiff(RunnersData rdA,RunnersData rdB,int pastFrame)
	{
		double auxOddA=rdA.getDataFrames().get(rdA.getDataFrames().size()-1-pastFrame).getOddBack();
		double auxOddB=rdB.getDataFrames().get(rdB.getDataFrames().size()-1-pastFrame).getOddBack();
		int diff=Math.abs(Utils.oddToIndex(auxOddA)-Utils.oddToIndex(auxOddB));
		return diff;
	}
	
	
	
	public static double getMatchedAmountAVG(RunnersData rd,int windowSize,int pastFrame)
	{
		double ret=0;
		if(Utils.isValidWindow(rd, windowSize,  pastFrame))
		{
			for(int i=0;i<windowSize;i++)
			{
				ret+=Utils.getMatchedAmount(rd, i+pastFrame);
			}
			ret/=windowSize;
			return ret;
		}
		else
		{
			return -1;
		}
	}
	
	
	public static double getMarketMathedAmount(MarketData md,int pastFrame)
	{
		double ret=0;
		for (RunnersData rd:md.getRunners())
		{
			ret+=Utils.getMatchedAmount(rd, pastFrame);
		}
		
		return ret;
		
	}
	
	public static double getMatchedAmount(RunnersData rd, int pastFrame)
	{
		return rd.getDataFrames().get(rd.getDataFrames().size()-1-pastFrame).getMatchedAmount();
	}
	
	public static boolean isRdConstantOddInWindow(RunnersData rd, int windowSize,int pastFrame)
	{
		
		
		double oddB=getOddBackFrame(rd, pastFrame);
		double oddL=getOddLayFrame(rd, pastFrame);
		for(int i=0;i<windowSize+1;i++)
		{
			if(oddB!=getOddBackFrame(rd, pastFrame+i))
				return false;
			if(oddL!=getOddLayFrame(rd, pastFrame+i))
				return false;
		}
		return true;
	}
	
	public static boolean isRdConstantOddLayInWindow(RunnersData rd, int windowSize,int pastFrame)
	{
		
		
		//double oddB=getOddBackFrame(rd, pastFrame);
		double oddL=getOddLayFrame(rd, pastFrame);
		for(int i=0;i<windowSize+1;i++)
		{
			//if(oddB!=getOddBackFrame(rd, pastFrame+i))
			//	return false;
			if(oddL!=getOddLayFrame(rd, pastFrame+i))
				return false;
		}
		return true;
	}
	
	public static boolean isRdConstantOddBackInWindow(RunnersData rd, int windowSize,int pastFrame)
	{
		
		
		double oddB=getOddBackFrame(rd, pastFrame);
		//double oddL=getOddLayFrame(rd, pastFrame);
		for(int i=0;i<windowSize+1;i++)
		{
			if(oddB!=getOddBackFrame(rd, pastFrame+i))
				return false;
			//if(oddL!=getOddLayFrame(rd, pastFrame+i))
			//	return false;
		}
		return true;
	}
	
	public static boolean isRdMovedOddInWindow(RunnersData rd, int windowSize,int pastFrame, int ticks)
	{
		
		if(rd.getDataFrames().size()-(pastFrame+windowSize)<0)
			return false;
		
		
		//System.out.println();
		double oddBn=getOddBackFrame(rd, pastFrame);
		double oddLn=getOddLayFrame(rd, pastFrame);
		
		double oddBp=getOddBackFrame(rd, pastFrame+windowSize);
		double oddLp=getOddLayFrame(rd, pastFrame+windowSize);
		
		int ticksMovedB=Math.abs(Utils.oddToIndex(oddBn)-Utils.oddToIndex(oddBp));
		int ticksMovedL=Math.abs(Utils.oddToIndex(oddLn)-Utils.oddToIndex(oddLp));
		
		if(ticksMovedB>=ticks && ticksMovedL>=ticks )
				return true;
		return false;
	}
	
	public static double getOddBackFrame(RunnersData rd,int pastFrame)
	{
		return rd.getDataFrames().get(rd.getDataFrames().size()-1-pastFrame).getOddBack();
	}
	
	public static double getOddBackAVG(RunnersData rd,int windowSize, int pastFrame)
	{
		double ret=0;
		for(int i=0;i<windowSize+1;i++)
		{
			ret+=getOddBackFrame(rd, i+pastFrame);
		}
		
		ret/=windowSize+1;
		
		return ret;
	}
	
	public static double getOddLayFrame(RunnersData rd,int pastFrame)
	{
		return rd.getDataFrames().get(rd.getDataFrames().size()-1-pastFrame).getOddLay();
	}
	
	public static double getOddLayAVG(RunnersData rd,int windowSize, int pastFrame)
	{
		double ret=0;
		for(int i=0;i<windowSize+1;i++)
		{
			ret+=getOddLayFrame(rd, i+pastFrame);
		}
		
		ret/=windowSize+1;
		
		return ret;
	}
	
	
	public static int getMarketSateFrame(MarketData md,int pastFrame)
	{
		return md.getRunners().get(0).getDataFrames().get(md.getRunners().get(0).getDataFrames().size()-1-pastFrame).getState();
	}
	
	public static boolean isInPlayFrame(MarketData md,int pastFrame)
	{
		return md.getRunners().get(0).getDataFrames().get(md.getRunners().get(0).getDataFrames().size()-1-pastFrame).isInPlay();
	}
	
	public static boolean isWomGoingDown(RunnersData rd, int windowSize,int pastFrame)
	{
		if(rd.getDataFrames().size()-(pastFrame+windowSize+1)<0)
			return false;
		
		
		for(int i=0;i<windowSize+1;i++)
		{
			double womBn=getWomBackFrame(rd, i+pastFrame);
			double womLn=getWomLayFrame(rd, i+pastFrame);
			
			double womn=womBn-womLn;
			
			double womBp=getWomBackFrame(rd, i+1+pastFrame);
			double womLp=getWomLayFrame(rd, i+1+pastFrame);
			
			double womp=womBp-womLp;
			
			if(womn>=womp)
				return false;
			
		}
		
		double womBn=getWomBackFrame(rd,pastFrame);
		double womLn=getWomLayFrame(rd,pastFrame);
		
		double womn=womBn-womLn;
		
		double womBp=getWomBackFrame(rd,windowSize+pastFrame);
		double womLp=getWomLayFrame(rd, windowSize+pastFrame);
		
		double womp=womBp-womLp;
			
		if(womn<womp)
			return true;
		else
			return false;
	}
	
	
	public static boolean isWomGoingUp(RunnersData rd,  int windowSize,int pastFrame)
	{
		
		double womBn=0;
		double womLn=0;
		
		double womn=0;
		
		double womBp=0;
		double womLp=0;
		
		double womp=0;
		
		for(int i=0;i<windowSize+1;i++)
		{
			womBn=getWomBackFrame(rd,i+pastFrame);
			womLn=getWomLayFrame(rd,i+pastFrame);
			
			womn=womBn-womLn;
			
			womBp=getWomBackFrame(rd,i+1+pastFrame);
			womLp=getWomLayFrame(rd,i+1+pastFrame);
			
			womp=womBp-womLp;
			
			if(womn<=womp)
				return false;
			
		}
		
		womBn=getWomBackFrame(rd,pastFrame);
		womLn=getWomLayFrame(rd,pastFrame);
		
		womn=womBn-womLn;
		
		womBp=getWomBackFrame(rd,windowSize+pastFrame);
		womLp=getWomLayFrame(rd,windowSize+pastFrame);
		
		womp=womBp-womLp;
		
		if(womn>womp)
			return true;
		else
			return false;
	}
	
	public static double getAmountBackFrameBackPivot(RunnersData rd,int frame, int depth)
	{
		double amBack=0;
		double oddBack=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getOddBack();
		Vector<OddData> oddsB=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getBackPrices();						 
		
		//System.out.println("B size:"+oddsB.size());
		for(int x=0;x<oddsB.size();x++)
		{
			if(oddsB.get(x).getOdd()==Utils.indexToOdd(Utils.oddToIndex(oddBack)-depth))
			{
				amBack=oddsB.get(x).getAmount();
			}
		}
				
		return amBack;
		
	}
	
	public static double getAmountBackFrameLayPivot(RunnersData rd,int frame, int depth)
	{
		double amBack=0;
		double oddBack=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getOddLay();
		Vector<OddData> oddsB=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getBackPrices();						 
		
		//System.out.println("B size:"+oddsB.size());
		for(int x=0;x<oddsB.size();x++)
		{
			if(oddsB.get(x).getOdd()==Utils.indexToOdd(Utils.oddToIndex(oddBack)-depth))
			{
				amBack=oddsB.get(x).getAmount();
			}
		}
				
		return amBack;
		
	}
	
	public static double getWomBackFrame(RunnersData rd,int frame)
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
	
	public static double getWomFrame(RunnersData rd,int depth,boolean includeGaps,int frame)
	{
		
		HistoryData hd=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame); // get info from frame (present to int frame..) received on this runner
		// index 0 is the first frame received and  (rd.getDataFrames().size()-1) is the last 
		
		double oddBack=hd.getOddBack();   //odd offer for Back bets
				
		Vector<OddData> bp=hd.getBackPrices();  // get all Back prices with amounts (OddData) 
			
		double womBack=0;
		// sum first "int depth" amounts of the vector that are the ones more near to best price on back prices
		int i=0;
		while (i<depth && Utils.indexToOdd(Utils.oddToIndex(oddBack)-i)!=-1)  //while depth and valid odd for depth
		{
			double amountValueB=0;
			
			for(int x=0;x<bp.size();x++)  // search for odd Utils.indexToOdd(Utils.oddToIndex(oddBack)-i) in ladder 
			{
				if(bp.get(x).getOdd()==Utils.indexToOdd(Utils.oddToIndex(oddBack)-i))
				{
					amountValueB=bp.get(x).getAmount();
				}
			}
			womBack+=amountValueB;
			i++;
			if(amountValueB==0 && !includeGaps) // if not includeGaps and there was a gap find next valid price 
				i--;
		}
		
		//System.out.println("wom amount back = "+womBack);
		
		double oddLay=hd.getOddLay();     //odd offer for Lay bets
		Vector<OddData> lp=hd.getLayPrices();  // get all Lay prices with amounts (OddData)
		
		double womLay=0;
		// sum first "int depth" amounts of the vector that are the ones more near to best price on back prices
		i=0;
		while (i<depth && Utils.indexToOdd(Utils.oddToIndex(oddLay)+i)!=-1)  //while depth and valid odd for depth
		{
			double amountValueL=0;
			
			for(int x=0;x<lp.size();x++)  // search for odd Utils.indexToOdd(Utils.oddToIndex(oddBack)-i) in ladder 
			{
				if(lp.get(x).getOdd()==Utils.indexToOdd(Utils.oddToIndex(oddLay)+i))
				{
					amountValueL=lp.get(x).getAmount();
				}
			}
			womLay+=amountValueL;
			i++;
			if(amountValueL==0 && !includeGaps) // if not includeGaps and there was a gap find next valid price 
				i--;
		}
		
		//System.out.println("wom amount Lay = "+womLay);
		
		double womTotal=womBack+womLay;
		
		double womDiff=womBack-womLay;
		
		return (womDiff*100.)/womTotal;
		
	}
	
	
	public static double getAmountLayFrameLayPivot(RunnersData rd,int frame, int depth)
	{
		double womLay=0;
		Vector<OddData> oddsL=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getLayPrices();
		double oddLay=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getOddLay();

		//System.out.println("l size:"+oddsL.size());
		for(int x=0;x<oddsL.size();x++)
		{
			if(oddsL.get(x).getOdd()==Utils.indexToOdd(Utils.oddToIndex(oddLay)+depth))
			{
				womLay=oddsL.get(x).getAmount();
			}
		}
			
		return womLay;
	}
	
	public static double getAmountLayFrameBackPivot(RunnersData rd,int frame, int depth)
	{
		double womLay=0;
		Vector<OddData> oddsL=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getLayPrices();
		double oddLay=rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getOddBack();

		//System.out.println("l size:"+oddsL.size());
		for(int x=0;x<oddsL.size();x++)
		{
			if(oddsL.get(x).getOdd()==Utils.indexToOdd(Utils.oddToIndex(oddLay)+depth))
			{
				womLay=oddsL.get(x).getAmount();
			}
		}
			
		return womLay;
	}
	
	
	public static double getWomLayFrame(RunnersData rd,int frame)
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
	
	public static double getWomFrame(RunnersData rd,int frame)
	{
		return getWomBackFrame(rd,frame)-getWomLayFrame(rd,frame);
	}
	
	public static boolean isValidWindow(RunnersData rd, int windowSize,int pastFrame)
	{
		return !(rd.getDataFrames().size()-(pastFrame+windowSize+1)<0);
		
	}
	
	public static double getWomAVGWindow(RunnersData rd, int windowSize,int pastFrame)
	{
		double ac=0;
		for(int i=0;i<windowSize+1;i++)
		{
			ac+=getWomFrame(rd,i+pastFrame);
		}
		
		return ac/windowSize;
	}
	
	public static boolean isAmountLayBiggerThanBack(RunnersData rd,int pastFrame)
	{
		return getAmountBackFrame(rd,pastFrame)<getAmountLayFrame(rd,pastFrame);
	}
	
	public static boolean isAmountBackBiggerThanLay(RunnersData rd,int pastFrame)
	{
		//System.out.println("L:"+rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountLay());
		//System.out.println("B:"+rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountBack());
		return getAmountBackFrame(rd,pastFrame)>getAmountLayFrame(rd,pastFrame);
		
	}
	
	public static boolean isAmountLayBiggerThanBack(RunnersData rd,int pastFrame,double percent)
	{
		double amB=getAmountBackFrame(rd,pastFrame);
		amB+=amB*percent;
		return amB<getAmountLayFrame(rd,pastFrame);
	}
	
	public static boolean isAmountBackBiggerThanLay(RunnersData rd,int pastFrame,double percent)
	{
		double amL=getAmountLayFrame(rd,pastFrame);
		amL+=amL*percent;
		return getAmountBackFrame(rd,pastFrame)>amL;	
	}
	
	public static boolean isAmountBackGoingDown(RunnersData rd,int windowSize,int pastFrame)
	{
		double amBn=0;
		double amBp=0;
		for(int i=0;i<windowSize+1;i++)
		{
			amBn=getAmountBackFrame(rd, i+pastFrame);
			amBp=getAmountBackFrame(rd, i+1+pastFrame);
			
			if(amBn>=amBp)
				return false;
		}
		
		amBn=getAmountBackFrame(rd, pastFrame );
		amBp=getAmountBackFrame(rd,pastFrame+windowSize);
		
		if(amBn<amBp)
			return true;
		return false;
	}
	
	public static double getAmountBackFrame(RunnersData rd, int frame)
	{
		return rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getAmountBack();
	}
	
	
	public static double getAmountLayFrame(RunnersData rd,int frame)
	{
		return rd.getDataFrames().get(rd.getDataFrames().size()-1-frame).getAmountLay();
	}
	
	// TESTAR !!!
	public static double getAmountBackOddFrame(RunnersData rd, double odd,int pastFrame)
	{
		double ret=0;
			
		if(getOddLayFrame(rd, pastFrame)>odd)
		{
			Vector<OddData> oddsB=rd.getDataFrames().get(rd.getDataFrames().size()-1-pastFrame).getBackPrices();
			for (OddData od:oddsB)
			{
				if(od.getOdd()==odd)
					return od.getAmount();
			}
		}
		else
		{
			
			Vector<OddData> oddsL=rd.getDataFrames().get(rd.getDataFrames().size()-1-pastFrame).getLayPrices();
			for (OddData od:oddsL)
			{
				if(od.getOdd()<=odd)
					ret+=od.getAmount();
			}
			ret*=-1;
		}
	
		return ret;
	}
	
	// TESTAR !!!
	public static double getAmountLayOddFrame(RunnersData rd, double odd,int pastFrame)
	{
		
		double ret=0;
		
		if(getOddBackFrame(rd, pastFrame)<odd)
		{
			Vector<OddData> oddsL=rd.getDataFrames().get(rd.getDataFrames().size()-1-pastFrame).getLayPrices();
			for (OddData od:oddsL)
			{
				if(od.getOdd()==odd)
					return od.getAmount();
			}
		}
		else
		{
			
			Vector<OddData> oddsB=rd.getDataFrames().get(rd.getDataFrames().size()-1-pastFrame).getBackPrices();
			for (OddData od:oddsB)
			{
				if(od.getOdd()>=odd)
					ret+=od.getAmount();
			}
			ret*=-1;
		}
	
		return ret;
	}
	
	public static boolean isAmountBackGoingUp(RunnersData rd,int windowSize,int pastFrame)
	{
		double amBn=0;
		double amBp=0;
		for(int i=0;i<windowSize+1;i++)
		{
			amBn=getAmountBackFrame(rd,i+pastFrame);
			amBp=getAmountBackFrame(rd,i+1+pastFrame);
			
			if(amBn<=amBp)
				return false;
		}
		
		amBn=getAmountBackFrame(rd, pastFrame );
		amBp=getAmountBackFrame(rd,pastFrame+windowSize);
		if(amBn>amBp)
			return true;
		return false;
	}
	
	public static boolean isAmountLayGoingDown(RunnersData rd, int windowSize,int pastFrame)
	{
		double amLn=0;
		double amLp=0;
		for(int i=0;i<windowSize+1;i++)
		{
			amLn=getAmountLayFrame(rd,i+pastFrame);
			amLp=getAmountLayFrame(rd,i+1+pastFrame);
			
			if(amLn>=amLp)
				return false;
		}
		amLn=getAmountLayFrame(rd, pastFrame );
		amLp=getAmountLayFrame(rd,pastFrame+windowSize);
		
		if(amLp > amLn)
			return true;
		return false;
	}
	
	public static boolean isAmountLayGoingUp(RunnersData rd, int windowSize,int pastFrame)
	{
		double amLn=0;
		double amLp=0;
		for(int i=0;i<windowSize+1;i++)
		{
			amLn=getAmountLayFrame(rd,i+pastFrame);
			amLp=getAmountLayFrame(rd,i+1+pastFrame);
			
			if(amLn<=amLp)
				return false;
		}
		
		amLn=getAmountLayFrame(rd, pastFrame );
		amLp=getAmountLayFrame(rd,pastFrame+windowSize);
		if(amLp < amLn)
			return true;
		return false;
	}
	
	public static boolean isRdBackWentDown(RunnersData rd, int windowSize,int pastFrame)
	{
		return getOddBackFrame(rd, windowSize+pastFrame)> getOddBackFrame(rd, pastFrame);
	}
	
	public static boolean isRdLayWentDown(RunnersData rd, int windowSize,int pastFrame)
	{
		return getOddLayFrame(rd, windowSize+pastFrame)> getOddLayFrame(rd, pastFrame);
	}
	
	public static boolean isRdBackWentUp(RunnersData rd, int windowSize,int pastFrame)
	{
		return getOddBackFrame(rd, windowSize+pastFrame)< getOddBackFrame(rd, pastFrame);
	}
	
	public static boolean isRdLayWentUp(RunnersData rd, int windowSize,int pastFrame)
	{
		return getOddLayFrame(rd, windowSize+pastFrame)< getOddLayFrame(rd, pastFrame);
	}
	
	public static int getOddBackLastFrameMove (RunnersData rd)
	{
		boolean notFound=true;
		
		
		int i=0;
		while (notFound)
		{
			if(rd.getDataFrames().size()<=i+1)
				return -1;
			if(getOddBackFrame(rd, i)!=getOddBackFrame(rd, i+1))
			{
				return i;
			}
			
			i++;
		}
		
		
		return -1;
	}
	
	public static int getOddBackDirection(RunnersData rd, int windowSize,int pastFrame)
	{
		int ret=0;
		//System.out.println("size : "+rd.getDataFrames().size());
		//System.out.println("index : "+ (pastFrame+windowSize));
		if(Utils.isValidWindow(rd,  pastFrame+windowSize, 0))	
			ret=oddToIndex(getOddBackFrame(rd, pastFrame))
			-oddToIndex(getOddBackFrame(rd, pastFrame+windowSize));
				
		return ret;
	}
	
	public static int[] getOddBackVariation(RunnersData rd, int windowSize,int pastFrame)
	{
		int ret[]=new int[windowSize];
		
		for(int i=0;i<windowSize;i++)
		{
			ret[i]=oddToIndex(getOddBackFrame(rd, i+pastFrame))-oddToIndex(getOddBackFrame(rd, i+1+pastFrame));
		}
		
		return ret;
	}
	
	public static int getOddBackTicksVariation(RunnersData rd, int windowSize,int pastFrame)
	{
		int ret=0;
		
		for(int i=0;i<windowSize;i++)
		{
			ret+=oddToIndex(getOddBackFrame(rd, i+pastFrame))-oddToIndex(getOddBackFrame(rd, i+1+pastFrame));
		}
		
		return ret;
	}
	
	public static int[] getOddBackVariation(RunnersData rd, int windowSize,int pastFrame,int ret[])
	{
		
		for(int i=0;i<windowSize;i++)
		{
			ret[i]=oddToIndex(getOddBackFrame(rd, i+pastFrame))-oddToIndex(getOddBackFrame(rd, i+1+pastFrame));
		}
		
		return ret;
	}
	
	
	public static int[] getOddLayVariation(RunnersData rd, int windowSize,int pastFrame)
	{
		int ret[]=new int[windowSize];
		
		for(int i=0;i<windowSize;i++)
		{
			ret[i]=oddToIndex(getOddLayFrame(rd, i+pastFrame))-oddToIndex(getOddLayFrame(rd, i+1+pastFrame));
		}
		
		return ret;
	}
	
	public static int getOddLayTickVariation(RunnersData rd, int windowSize,int pastFrame)
	{
		int ret=0;
		
		for(int i=0;i<windowSize;i++)
		{
			ret+=oddToIndex(getOddLayFrame(rd, i+pastFrame))-oddToIndex(getOddLayFrame(rd, i+1+pastFrame));
		}
		
		return ret;
	}
	
	public static double [][] getLadderAmountDiffWindow(RunnersData rd,int windowSize,int pastFrame, int axisSize,  double ladderWindow[][],int oddWindow[])
	{
		//ladderWindow
		double act=0;
		double past=0;
		for(int i=0;i<windowSize;i++)
		{
			for(int x =0;x<axisSize*2;x++)
			{
				if(x>=axisSize)
				{
					act=Utils.getAmountBackFrameBackPivot(rd, i+pastFrame, x-axisSize);
					past=Utils.getAmountBackFrameBackPivot(rd, i+pastFrame+1, x-axisSize - oddWindow[i]);
					ladderWindow[x][i]=act-past;
				}
				else
				{
					act=Utils.getAmountLayFrameBackPivot(rd, i+pastFrame, axisSize-x);
					past=Utils.getAmountLayFrameBackPivot(rd, i+pastFrame+1, axisSize-x + oddWindow[i]);
					//System.out.println(oddWindow[i]);
					ladderWindow[x][i]=act-past;
				}
				
				
			}
			
		}
		
		return ladderWindow;
	}
	
	public static double getAmountOfferOddFrame(RunnersData rd,int pastFrame, double odd)
	{
		
		Vector<OddData> odds;
		double oddBack=rd.getDataFrames().get(rd.getDataFrames().size()-1-pastFrame).getOddBack();
		double signal=1;
		if(oddBack>=odd)
		{
			odds=(rd.getDataFrames().get(rd.getDataFrames().size()-1-pastFrame).getBackPrices());
			signal = 1;
		}
		else
		{
			odds=rd.getDataFrames().get(rd.getDataFrames().size()-1-pastFrame).getLayPrices();
			signal = -1;
		}
								 
		for(int x=0;x<odds.size();x++)
		{
			if(odds.get(x).getOdd()==odd)
			{
				return odds.get(x).getAmount()*signal;
			}
		}
		
		return 0;
	}
	
	public static double [][] getLadderAmountOfferDiffWindow(RunnersData rd,int windowSize,int pastFrame, int axisSize,  double ladderWindow[][])
	{
		//ladderWindow
		double act=0;
		double past=0;
		double oddBack=rd.getDataFrames().get(rd.getDataFrames().size()-1-pastFrame).getOddBack();
		for(int i=0;i<windowSize;i++)
		{
			for(int x =0;x<axisSize*2;x++)
			{
				double odd=Utils.indexToOdd(Utils.oddToIndex(oddBack)+(axisSize-x));
				act=Utils.getAmountOfferOddFrame(rd, i+pastFrame, odd);
				//getAmountBackFrameBackPivot(rd, i+pastFrame, x-axisSize);
				past=Utils.getAmountOfferOddFrame(rd, i+pastFrame+1, odd);
					//Utils.getAmountBackFrameBackPivot(rd, i+pastFrame+1, x-axisSize - oddWindow[i]);
				ladderWindow[x][i]=act-past;
			}
			
		}
		
		return ladderWindow;
	}
	
	public static double[] getAmountLadderFrameBackPivot(RunnersData rd,int pastFrame, int axisSize)
	{
		double[] ret=new double [axisSize*2];
		
		for(int x =0;x<axisSize*2;x++)
		{
			if(x>=axisSize)
			{
				ret[x]=Utils.getAmountBackFrameBackPivot(rd, pastFrame, x-axisSize);
				
			}
			else
			{
				ret[x]=Utils.getAmountLayFrameBackPivot(rd, pastFrame, axisSize-x);
			}
		}
		return ret;
	}
	
	
	public static double getVolumeFrame(RunnersData rd, int pastFrame,double odd)
	{
		int pastFrameCalc=0;
		if(rd.getDataFrames() == null || rd.getDataFrames().size()==0)
			return 0;
		else
			pastFrameCalc=rd.getDataFrames().size()-1-pastFrame;
		
		if (rd.getDataFrames().get(pastFrameCalc)==null
					|| rd.getDataFrames().get(pastFrameCalc).getVolume() ==null
					|| !rd.getDataFrames().get(pastFrameCalc).getVolume().containsKey(odd)
					|| rd.getDataFrames().get(pastFrameCalc).getVolume().get(odd)==null )
			{
				return 0;
			}
	
		
		return rd.getDataFrames().get(pastFrameCalc).getVolume().get(odd);	
	
		
	}
	
	public static double[] getVolumeLadderFramePivot(RunnersData rd, int pastFrame,int axisSize, double oddPivot )
	{
		double[] ret=new double [axisSize*2];
		
		
		for(int i=-axisSize;i<axisSize;i++)
		{
			ret[i+axisSize]=Utils.getVolumeFrame(rd, pastFrame, Utils.indexToOdd(Utils.oddToIndex(oddPivot)-i));
		}
		
		return ret;
	}
	
	public static double [] processThreshold(double vec[],double min,double max)
	{
		double ret[]=new double[vec.length];
		for(int i=0;i<vec.length;i++)
		{
			if(vec[i]<min)
				ret[i]=min;
			else if(vec[i]>max)
				ret[i]=max;
			else
				ret[i]=vec[i];
		}
		
		return ret;
	}
	
	public static double [][] processThreshold(double vec[][],double min,double max)
	{
		for(int l=0;l<vec.length;l++)
		for(int i=0;i<vec[l].length;i++)
		{
			if(vec[l][i]<min)
				vec[l][i]=min;
			else if(vec[l][i]>max)
				vec[l][i]=max;
			else
				vec[l][i]=vec[l][i];
		}
		
		return vec;
	}
	
	public static double [] getVolumeLadderFramePivotdiff(RunnersData rd,int windowSize, int pastFrame,int axisSize, double oddPivot)
	{
		double[] ret=new double [axisSize*2];
		
		double[] present=getVolumeLadderFramePivot(rd,pastFrame,axisSize, oddPivot );
		double[] past=getVolumeLadderFramePivot(rd,pastFrame+windowSize,axisSize, oddPivot );
		
		for(int i=0;i<axisSize*2;i++)
		{
			ret[i]=present[i]-past[i];
		}
		return ret;
		
	}
	
	/////////// RUNNER UTILS END /////////////
	
	public static double[] getPercentage(double in[],double out[])
	{
		double total=0;
		for(int i=0;i<in.length;i++)
			total+=in[i];
		
		if(total < 0.01 )
		{
			for(int i=0;i<in.length;i++)
				out[i]=0;
		}
		else
		{
			for(int i=0;i<in.length;i++)
				out[i]=(in[i]*100)/total;
		}
		return out;
	}
	
	public static void main(String[] args)
	{
		Utils.init();
		System.out.println("ol�");
		for(int i=0;i<STEPS;i++)
		{
			System.out.println("odd="+ Utils.indexToOdd(i) + " index="+ Utils.oddToIndex(Utils.indexToOdd(i))+ " i="+i);
			System.out.println("ladder["+i+"]="+Utils.ladder[i]);
		}
		double d = 1.234567;
        DecimalFormat df = new DecimalFormat("#.##");
        System.out.print(df.format(d));
        DecimalFormatSymbols symbols=df.getDecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        double test=Double.parseDouble(df.format(d));
        System.out.println("teste="+test);
        
        double oddLay=4.2;
        

        System.out.println("OddBack:"+2+"\nAmount:"+100+"OddLayExit:"+1.90+"\nCalculated lay Amount:"+Utils.closeAmountLay(2., 100, 1.90));
        System.out.println("Oddlay:"+3+"\nAmount:"+50+"OddLayExit:"+2.90+"\nCalculated Back Amount:"+Utils.closeAmountBack(3., 50, 2.90));
        
        System.out.println("Near valid odd "+Utils.nearValidOdd(2.2345));
        
        System.out.println("Average ODD BETFAIR :"+Utils.calculateOddAverage(new double []{12,1.5},new double []{2,5}));
        
       
        System.out.println("amount"+convertAmountToBF(3.329525));
        
        System.out.println("%Utils.indexToOdd(Utils.oddToIndex(7.8)-1)  :"+Utils.indexToOdd(Utils.oddToIndex(7.8)-1));
        
        //>=getRunnerOddLay()
        System.out.println("---------------------------");
        int odd=oddToIndex(20.0);
        int step=odd/5;
        
        for(int i=0;i<odd;i+=step)
        {
        	System.out.println(indexToOdd(i)+" - "+indexToOdd(i+step));
        }
        
        System.out.println("Max index ladder:"+Utils.oddToIndex(1000));
        
        
        double matrix[][]=new double[2][3];
        
        for(int l=0;l<2;l++)
        	for(int c=0;c<3;c++)
        	{
        		matrix[l][c]=l+c;
        	}
        	
        
        for(int i = 0 ; i<matrix.length;i++)
        {
        	Utils.processThreshold(matrix, 1, 1);
        }
        
        for(int l=0;l<2;l++)
        	for(int c=0;c<3;c++)
        	{
        		System.out.println("matrix["+l+"]["+c+"]="+matrix[l][c]);
        		//matrix[l][c]=l+c;
        	}
        
        System.out.println(convertAmountToBF(0.049));
        
        System.out.println("AM:"+Utils.closeAmountLay(5.0, 100,   2.80));
        
       // System.out.println("AM:"+Utils.closeAmountLay(3.0,  100,  Utils.1.9  ));
        
       
        
        for (int i=0;i<STEPS-1;i++)
        {
        	
        	//if(Utils.indexToOdd(i)>2.2 && Utils.indexToOdd(i)<11)
        	//	System.out.println((100-Utils.closeAmountBack(Utils.indexToOdd(i),  100,  Utils.indexToOdd(i+1))));
        	//else
        		System.out.println(Utils.indexToOdd(i)+ " Gain: "+(100-Utils.closeAmountBack(Utils.indexToOdd(i),  100,  Utils.indexToOdd(i+1))));
        }
       
        
        System.out.println("AM:"+Utils.closeAmountLay(1.20,  100,  1.10  ));
        
        System.out.println("ticks dif:"+Utils.getTicksDiff(2.2, 2.02));
        
        double amount = 3;
        double oddActuation=3;
        for(int i=0;i<10;i++)
        	{
        		System.out.println("Amount ["+i+"]="+amount );
        		amount=(amount*(oddActuation-1.00))+amount;
        		
        	}
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Calendar c=Calendar.getInstance();
		//dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timeStart=dateFormat.format(c.getTimeInMillis());
		System.out.println("Time : "+timeStart);
        //10.32 @ 6.2 (LAY)  10.32258064516129
        //9.877342419080069 @ 5.87
        //890.3128 @ 9.884166553597792 (LAY)
        //487.5591 @ 9.844960334039504 (LAY)
        //1173.469387755102 @ 9.8 (Back)
        //988.2906482892269 @ 9.511372 (Back)
        //989.225648895193 @ 9.502381999999999 (Back)
        //1041.5109523809526 @ 12.481865860633793 
        //871.7170999999998 @ 9.865585979671618 (LAY)
        //922.7456382978723 @ 9.320011542795097
        //46.73913043478261 @ 9.2 (LAY)
        //1.0978260869565217 @ 9.2 (LAY)
        //1071.4285714285713 @ 9.8 (Back)
        //1091.7703125 @ 9.617407507588736 (Back)
        //934.1568860440698 @ 9.634356 (Back)
        //1133.132857142857 @ 12.79638120860875 (LAY)
        
        //1011.930476190476 @ 10.870312001483258 (LAY)
        //  1015.89519289074
        //1015.8937453042826 @ 10.827888621757614 (LAY)
        
        //895.7680952380954 @ 10.49378745455483
        
        // 1001.0785714285715 @ 9.98922590634387
        //10.217391304347828
        //21.299458558933775 @ 9.2 (LAY)
        //1003.8378146860575 @ 9.762525132784125 
        
        //891.5051086956521 @ 9.197928222752752 
        //909.0922865034646
        //1020.8333333333335
        // 10.652173913043478
        //10.77111111111111
        //8.91304347826087
        //825.19
     //  71385.0
        //109.40493571934792
       //986.3635326834435
       //1088.4895686155555
        //146.5742324137508
        
        //95338.99500000001
        
        //102020.41
        // 613.2200000000003
        //71464.82   
        //135.02038747294012
        // 134.33922894459704
        // 1022.8799999999999
		
	}
	
	// Select a market by the following process
	// * Select a type of event
	// * Recursively select an event of this type
	// * Select a market within this event if one exists.
	public static Market chooseMarket(APIContext apiContext,Exchange selectedExchange ) throws Exception {
		// Get available event types.
		EventType[] types = GlobalAPI.getActiveEventTypes(apiContext);
		
		//types[0].getName()
		int typeChoice = Display.getChoiceAnswer("Choose an event type:", types);

		// Get available events of this type
		Market selectedMarket = null;
		int eventId = types[typeChoice].getId();
		while (selectedMarket == null) {
			GetEventsResp resp = GlobalAPI.getEvents(apiContext, eventId);
			BFEvent[] events = resp.getEventItems().getBFEvent();
			if (events == null) {
				events = new BFEvent[] {};
			} else {
				// The API returns Coupons as event names, but Coupons don't contain markets so we remove any
				// events that are Coupons.
				ArrayList<BFEvent> nonCouponEvents = new ArrayList<BFEvent>(events.length);
				for(BFEvent e: events) {
					if(!e.getEventName().equals("Coupons")) {
						nonCouponEvents.add(e);
					}
				}
				events = (BFEvent[]) nonCouponEvents.toArray(new BFEvent[]{});
			}
			MarketSummary[] markets = resp.getMarketItems().getMarketSummary();
			if (markets == null) {
				markets = new MarketSummary[] {};
			}
			int choice = Display.getChoiceAnswer("Choose a Market or Event:", events, markets);

			// Exchange ID of 1 is the UK, 2 is AUS
			if (choice < events.length) {
				eventId = events[choice].getEventId(); 
			} else {
				choice -= events.length;
				selectedExchange = markets[choice].getExchangeId() == 1 ? Exchange.UK : Exchange.AUS;
				selectedMarket = ExchangeAPI.getMarket(selectedExchange, apiContext, markets[choice].getMarketId());
			}				
		}
		return selectedMarket;
		
		
	}

}
