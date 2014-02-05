package categories.categories2013.bots;

import DataRepository.RunnersData;
import DataRepository.Utils;

public class UtilsCollectData {

	
	public static double getAmountOfferVariationOddFrame(RunnersData rd,int pastFrame, double odd)
	{
		double actual=Utils.getAmountOfferOddFrame(rd,pastFrame, odd);
		//System.out.println("amount actual on "+odd+" is :"+actual);
		double prev=Utils.getAmountOfferOddFrame(rd,(pastFrame+1), odd);
		//System.out.println("amount prev on "+odd+" is :"+prev);
		return actual-prev;
		
	}
	
	public static double getAmountOfferVariationBackDepthFrame(RunnersData rd,int pastFrame, int depth)
	{
		double odd=Utils.getOddBackFrame(rd, pastFrame);
		double ret=0;
		
		for (int i=0;i<depth;i++)
		{
			ret+=getAmountOfferVariationOddFrame(rd,pastFrame,odd);
			odd=Utils.indexToOdd(Utils.oddToIndex(odd)-1);
		}
		
		
		return ret;
	}
	
	public static double getAmountOfferVariationLayDepthFrame(RunnersData rd,int pastFrame, int depth)
	{
		double odd=Utils.getOddBackFrame(rd, pastFrame);
		odd=Utils.indexToOdd(Utils.oddToIndex(odd)+1);
		double ret=0;
		
		for (int i=0;i<depth;i++)
		{
			ret+=getAmountOfferVariationOddFrame(rd,pastFrame,odd);
			odd=Utils.indexToOdd(Utils.oddToIndex(odd)+1);
		}
		
		
		return ret;
	}
	
	
	public static double getAmountOfferVariationBackDepthWindow(RunnersData rd,int pastFrame,int size, int depth)
	{
		double ret=0;
		for (int i=0;i<size;i++)
		{
			ret+=getAmountOfferVariationBackDepthFrame(rd,pastFrame+i,depth);
		}
			
		return ret;
	}
	
	public static double getAmountOfferVariationLayDepthWindow(RunnersData rd,int pastFrame,int size, int depth)
	{
		double ret=0;
		for (int i=0;i<size;i++)
		{
			ret+=getAmountOfferVariationLayDepthFrame(rd,pastFrame+i,depth);
		}
			
		return ret;
	}
	
	public static double getAmountOfferVariationAVGBackDepthWindow(RunnersData rd,int pastFrame,int size, int depth)
	{
		if(size!=0)
			return getAmountOfferVariationBackDepthWindow(rd,pastFrame,size,depth)/size;
		else
			return getAmountOfferVariationBackDepthWindow(rd,pastFrame,size,depth);
	}
	
	public static double getAmountOfferVariationAVGLayDepthWindow(RunnersData rd,int pastFrame,int size, int depth)
	{
		if(size!=0)
			return getAmountOfferVariationLayDepthWindow(rd,pastFrame,size,depth)/size;
		else
			return getAmountOfferVariationLayDepthWindow(rd,pastFrame,size,depth);
	}	
	
}
