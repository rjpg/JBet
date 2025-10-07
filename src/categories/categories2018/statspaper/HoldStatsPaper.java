package categories.categories2018.statspaper;

import java.util.Vector;

public class HoldStatsPaper {

	public static double TOTAL_VOLUME[]=new double[StatsForPaperBot.TOTAL_SIZE_FRAMES];
	public static double TOTAL_LIQUIDITY[]=new double[StatsForPaperBot.TOTAL_SIZE_FRAMES];
	public static double TOTAL_VOLATILITY[]=new double[StatsForPaperBot.VOLATILITY_SEGMENTS];
	
	public static Vector<Double []> TOTAL_VOLUME_SEGMENTS= new Vector<Double []>();
	public static Vector<Double []> TOTAL_LIQUIDITY_SEGMENTS= new Vector<Double []>();
	public static Vector<Double []> TOTAL_VOLATILITY_SEGMENTS= new Vector<Double []>();
	
			
	public static void addVolumeInfo(double volumeInfo[])
	{
		Double totalVolumeSegments[]=new Double[StatsForPaperBot.VOLATILITY_SEGMENTS];
		
		int x=0;
		int index=0;
		
		double acum=0;
		
		for(int i=0;i<StatsForPaperBot.TOTAL_SIZE_FRAMES;i++)
		{
			TOTAL_VOLUME[i]+=volumeInfo[i];
			
			acum+=volumeInfo[i];
			if(x==StatsForPaperBot.TOTAL_SIZE_FRAMES/10-1)
			{
				x=0;
				totalVolumeSegments[index]=acum/(StatsForPaperBot.TOTAL_SIZE_FRAMES/10-1);
				acum=0;
				index++;
			}
			x++;
			
		}
		
		TOTAL_VOLUME_SEGMENTS.add(totalVolumeSegments);
		
		//debug prints
		//System.out.println("add rece sample volume evolution");
		//for(int i=0;i<StatsForPaperBot.VOLATILITY_SEGMENTS;i++)
		//	System.out.println("Volme evol ["+i+"] = "+totalVolumeSegments[i]);
		
	}
	
	public static void addLiquidityInfo(double liquidityInfo[])
	{
		
		Double totalLiquiditySegments[]=new Double[StatsForPaperBot.VOLATILITY_SEGMENTS];
		
		int x=0;
		int index=0;
		
		double acum=0;
		
		for(int i=0;i<StatsForPaperBot.TOTAL_SIZE_FRAMES;i++)
		{
			TOTAL_LIQUIDITY[i]+=liquidityInfo[i];
			
			acum+=liquidityInfo[i];
			if(x==StatsForPaperBot.TOTAL_SIZE_FRAMES/10-1)
			{
				x=0;
				totalLiquiditySegments[index]=acum/(StatsForPaperBot.TOTAL_SIZE_FRAMES/10-1);
				acum=0;
				index++;
			}
			x++;
			
		}
		
		TOTAL_LIQUIDITY_SEGMENTS.add(totalLiquiditySegments);
		
		//debug prints
		System.out.println("add rece sample Liduidity evolution");
		for(int i=0;i<StatsForPaperBot.VOLATILITY_SEGMENTS;i++)
			System.out.println("Volme evol ["+i+"] = "+totalLiquiditySegments[i]);
		
	}
	
	public static void addVolatilityInfo(double volatilityInfo[])
	{
		// used to work easy for the cast from double to Double
		Double volatilityInfoSegments[]=new Double[StatsForPaperBot.VOLATILITY_SEGMENTS];
		
		for(int i=0;i<StatsForPaperBot.VOLATILITY_SEGMENTS;i++)
		{
			TOTAL_VOLATILITY[i]+=volatilityInfo[i];
			volatilityInfoSegments[i]=volatilityInfo[i];
		}
		
		TOTAL_VOLATILITY_SEGMENTS.add(volatilityInfoSegments);
	}

	public static double[] getTOTAL_VOLUME() {
		return TOTAL_VOLUME;
	}

	public static double[] getTOTAL_LIQUIDITY() {
		return TOTAL_LIQUIDITY;
	}

	public static double[] getTOTAL_VOLATILITY() {
		return TOTAL_VOLATILITY;
	}
	
	
}
