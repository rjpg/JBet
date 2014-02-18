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
			System.out.println("varitaion frame Back "+(pastFrame+i)+" = "+getAmountOfferVariationBackDepthFrame(rd,pastFrame+i,depth));
			ret+=getAmountOfferVariationBackDepthFrame(rd,pastFrame+i,depth);
		}
		System.out.println("Total Back= "+ret);
		return ret;
	}
	
	public static double getAmountOfferVariationLayDepthWindow(RunnersData rd,int pastFrame,int size, int depth)
	{
		double ret=0;
		for (int i=0;i<size;i++)
		{
			System.out.println("varitaion frame Lay "+(pastFrame+i)+" = "+getAmountOfferVariationLayDepthFrame(rd,pastFrame+i,depth));
			ret+=getAmountOfferVariationLayDepthFrame(rd,pastFrame+i,depth);
		}
		
		System.out.println("Total Lay= "+ret);	
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
	
	public static double getAmountMatchedVariationFrame(RunnersData rd,int pastFrame, double odd)
	{
		double actual=Utils.getVolumeFrame(rd,pastFrame, odd);
		//System.out.println("Voluma actual on "+odd+" is :"+actual);
		double prev=Utils.getVolumeFrame(rd,(pastFrame+1), odd);
		//System.out.println("Volume prev on "+odd+" is :"+prev);
		
		double matchStake=(actual-prev)/2;
		
		double amountOfferPrev=Utils.getAmountOfferOddFrame(rd,pastFrame+1, odd);
		
		if(amountOfferPrev>0)
		{
			return matchStake*(-1.);
		}
		else if(amountOfferPrev<0)
		{
			return matchStake;
		}
		else
		{
			double amountOffer=Utils.getAmountOfferOddFrame(rd,pastFrame, odd);
			if(amountOffer>0)
				return matchStake;
			else if(amountOffer<0)
				return matchStake*(-1.);
			else
				return 0.;
		}
		
	}
	
	public static double getAmountMatchedVariationAxisFrame(RunnersData rd,int pastFrame, int depth)
	{
		
		double ret=0.;
		double odd=Utils.getOddBackFrame(rd, pastFrame);
		
		odd=Utils.indexToOdd(Utils.oddToIndex(odd)-(depth-1));
		
		for(int i=0;i<depth*2;i++)
		{
			//System.out.println("sum odd : "+odd+" var : "+ getAmountMatchedVariationFrame(rd,pastFrame,odd));
			
			ret+=getAmountMatchedVariationFrame(rd,pastFrame,odd);
			odd=Utils.indexToOdd(Utils.oddToIndex(odd)+1);
			
		}
		
		return ret;
	}
	
	public static double getAmountMatchedVariationAxisWindow(RunnersData rd,int pastFrame,int size, int depth)
	{
		double ret=0;
		for (int i=0;i<size;i++)
		{
			System.out.println("varitaion frame volume "+(pastFrame+i)+" = "+getAmountMatchedVariationAxisFrame(rd,pastFrame+i,depth));
			ret+=getAmountMatchedVariationAxisFrame(rd,pastFrame+i,depth);
		}
		
		System.out.println("Total Volume var= "+ret);	
		return ret;
	}
	
	public static double getAmountMatchedVariationAVGAxisWindow(RunnersData rd,int pastFrame,int size, int depth)
	{
		if(size!=0)
			return getAmountMatchedVariationAxisWindow(rd,pastFrame,size,depth)/size;
		else
			return getAmountMatchedVariationAxisWindow(rd,pastFrame,size,depth);
	}	
	
	
}
