package categories.categories2018.statspaper;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import bots.Bot;
import categories.categories2013.bots.UtilsCollectData;


public class StatsForPaperBot extends Bot{

	
	public static int TOTAL_SIZE_FRAMES=850;
	public static int VOLATILITY_SEGMENTS=10;
	// Time Windows 
	public static int WINDOWS[][]=new int[VOLATILITY_SEGMENTS][2];
		
	public boolean raceProcessed=false;
	
	public int racesProcesssed=0;
	
	public static int VALID_RACES_TO_PROCESS=4000;
	
	String fileNameVolume="volume.csv";
	String fileNameLiquidity="liquidity.csv";
	String fileNameVolatility="volatility.csv";
	
	
	public StatsForPaperBot(MarketData md) {
		super(md, "StatsPaperBot2018");
		initialize();
	}

	public void initialize()
	{
		
		int EXP_FACTOR=1;
		double maxNormal=Math.pow(VOLATILITY_SEGMENTS, EXP_FACTOR);
		
		int totalSizeFrames=(int) (TOTAL_SIZE_FRAMES);
		
		for(int i=1;i<VOLATILITY_SEGMENTS+1;i++)
		{
			double indexNormalA=Math.pow(i-1, EXP_FACTOR);
			double indexNormalB=Math.pow(i, EXP_FACTOR);
			
			int indexA=(int)((indexNormalA*totalSizeFrames)/maxNormal);
			int indexB=(int)((indexNormalB*totalSizeFrames)/maxNormal);
			int size=indexB-indexA;
						
			WINDOWS[i-1][0]=indexA;
			WINDOWS[i-1][1]=size;
		}
		
		for(int i=0;i<VOLATILITY_SEGMENTS;i++)
		{		
			System.out.println("window ["+i+"]=["+WINDOWS[i][0]+","+WINDOWS[i][1]+"]");
		}
		
		
		//CategoryNode.buildDirectories(root);
		
		//Vector<CategoryNode> cnv=CategoryNode.getAncestorsById(root,500);
		
		//for(CategoryNode cn:cnv)
		//	System.out.print(cn.getPath()+"\\");
		
	}
	
	public void update()
	{

		if(racesProcesssed>=VALID_RACES_TO_PROCESS)
		{
			printStats();
			getMd().pause=true;
		}
		
		if(getSecondsToStart()<=2 && !raceProcessed)
		{
			System.out.println("seconds to start : "+getSecondsToStart());
			System.out.println("frames Collected : "+getMd().getRunners().get(0).getDataFrames().size());
			
			raceProcessed=true;
			if(!Utils.isValidWindow(getMd().getRunners().get(0),  4, TOTAL_SIZE_FRAMES))
			{
				System.out.println("Not Valid window - Race not added to stats");
				return;
			}
			
			System.out.println("OK, Addind data of this race to statistics ...");
			racesProcesssed++;
			
			double volumeInfo[]=new double[TOTAL_SIZE_FRAMES];
			double liquidityInfo[]=new double[TOTAL_SIZE_FRAMES];
			double volatilityInfo[]=new double[VOLATILITY_SEGMENTS];
			
			
			for(int i=TOTAL_SIZE_FRAMES-1;i>=0;i--)
			{
				double globalVolume=0;
				double amountBack=0;
				double amountLay=0;
				for(RunnersData rdc:getMd().getRunners())
				{
					
					Utils.getAmountBackFrame(rdc, i);
					globalVolume+=rdc.getDataFrames().get(rdc.getDataFrames().size()-1-i).getMatchedAmount();
					amountBack+=rdc.getDataFrames().get(rdc.getDataFrames().size()-1-i).getAmountBack();
					amountLay+=rdc.getDataFrames().get(rdc.getDataFrames().size()-1-i).getAmountLay();
					
				}
				
				
				volumeInfo[i]=globalVolume;
				liquidityInfo[i]=amountBack+amountLay;
				
				
			
			}
			
			
			for(int i=VOLATILITY_SEGMENTS-1;i>=0;i--)
			{
				double totalVolatility=0;
				for(RunnersData rdc:getMd().getRunners())
				{
					totalVolatility+=(double)UtilsCollectData.getOddLayTickVariationIntegralABSStep(rdc, WINDOWS[i][0], WINDOWS[i][1]+1,4);
				}
				
				volatilityInfo[i]=totalVolatility;
				
				
				//System.out.println("Volatility ["+i+"] = "+volatilityInfo[i]);
			}
			
			HoldStatsPaper.addLiquidityInfo(liquidityInfo);
			HoldStatsPaper.addVolumeInfo(volumeInfo);
			HoldStatsPaper.addVolatilityInfo(volatilityInfo);
			
			
		}

	}
	
	
	public void reset()
	{
		 raceProcessed=false;
	}
	
	public void newMarket(MarketData md)
	{
	
		setMd(md);
		
	}
	
	
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketNew)
		{
			newMarket(md);
			reset();
		}
		
		if(marketEventType==MarketChangeListener.MarketUpdate)
			update();			
		
	}


	@Override
	public void writeMsg(String s, Color c) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void printStats()
	{
		System.out.println("---------------------- STATS -------------------------");
		System.out.println("############################## Volatility:");
		for(int i=VOLATILITY_SEGMENTS-1;i>=0;i--)
		{
			System.out.println(HoldStatsPaper.getTOTAL_VOLATILITY()[i]/VALID_RACES_TO_PROCESS);
					
		}
		
		System.out.println("############################### Liquidity:");
		for(int i=0;i<TOTAL_SIZE_FRAMES;i++)
		{
			System.out.println(HoldStatsPaper.getTOTAL_LIQUIDITY()[i]/VALID_RACES_TO_PROCESS);
		}
		
		System.out.println("################################ Volume :");
		for(int i=0;i<TOTAL_SIZE_FRAMES;i++)
		{
			System.out.println(HoldStatsPaper.getTOTAL_VOLUME()[i]/VALID_RACES_TO_PROCESS);
		}
		
		System.out.println("---------------------- END STATS -------------------------");
		
		System.out.println("---------------------- Saving minute precision to files-----");
		
		//############################################# VOLUME STATS 
		System.out.println("Tryint to write "+fileNameVolume);
		BufferedWriter inputsBuffer=null;
		
		
		try {
			inputsBuffer = new BufferedWriter(new FileWriter(fileNameVolume, true));
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open "+fileNameVolume+" for writing");
			}
		
		if(inputsBuffer==null)
		{
			System.err.println("could not open "+fileNameVolume);
			return;
		}
		
		String lines="";
		for(Double[] record:HoldStatsPaper.TOTAL_VOLUME_SEGMENTS)
		{
			for(int i=0;i<record.length;i++)
			{
				lines+= record[i]+" ";
			}
			lines+="\n";

		}
		
		try {
			inputsBuffer.write(lines);
			//System.out.println("writeen "+ filePath);
			//inputsBuffer.newLine();
			inputsBuffer.flush();
		} catch (IOException e) {
			System.out.println(fileNameVolume+":Error writting data to file "+fileNameVolume);
			e.printStackTrace();
		}

		try {
			inputsBuffer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Write complete on "+fileNameVolume);
		//######################################## END VOLUME STATS
		
		//############################################# Liquidity STATS 
		System.out.println("Tryint to write "+fileNameLiquidity);
		inputsBuffer=null;
		
		
		try {
			inputsBuffer = new BufferedWriter(new FileWriter(fileNameLiquidity, true));
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open "+fileNameLiquidity+" for writing");
			}
		
		if(inputsBuffer==null)
		{
			System.err.println("could not open "+fileNameLiquidity);
			return;
		}
		
		lines="";
		for(Double[] record:HoldStatsPaper.TOTAL_LIQUIDITY_SEGMENTS)
		{
			for(int i=0;i<record.length;i++)
			{
				lines+= record[i]+" ";
			}
			lines+="\n";

		}
		
		try {
			inputsBuffer.write(lines);
			//System.out.println("writeen "+ filePath);
			//inputsBuffer.newLine();
			inputsBuffer.flush();
		} catch (IOException e) {
			System.out.println(fileNameLiquidity+":Error writting data to file "+fileNameLiquidity);
			e.printStackTrace();
		}

		try {
			inputsBuffer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Write complete on "+fileNameLiquidity);
		//######################################## END LIQUIDITY STATS

		//############################################# Volatility STATS 
				System.out.println("Tryint to write "+fileNameVolatility);
				inputsBuffer=null;
				
				
				try {
					inputsBuffer = new BufferedWriter(new FileWriter(fileNameVolatility, true));
					} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Error Open "+fileNameVolatility+" for writing");
					}
				
				if(inputsBuffer==null)
				{
					System.err.println("could not open "+fileNameVolatility);
					return;
				}
				
				lines="";
				for(Double[] record:HoldStatsPaper.TOTAL_VOLATILITY_SEGMENTS)
				{
					for(int i=0;i<record.length;i++)
					{
						lines+= record[i]+" ";
					}
					lines+="\n";

				}
				
				try {
					inputsBuffer.write(lines);
					//System.out.println("writeen "+ filePath);
					//inputsBuffer.newLine();
					inputsBuffer.flush();
				} catch (IOException e) {
					System.out.println(fileNameVolatility+":Error writting data to file "+fileNameVolatility);
					e.printStackTrace();
				}

				try {
					inputsBuffer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Write complete on "+fileNameVolatility);
				//######################################## END Volatility STATS
		
		System.out.println("---------------------- END - Saving minute precision to files-----");
	}

}
