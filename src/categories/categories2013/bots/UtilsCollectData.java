package categories.categories2013.bots;

import java.util.Vector;

import DataRepository.RunnersData;
import DataRepository.Utils;
import akka.japi.pf.Match;
import categories.categories2018.DataWindowsSizes2018;

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
			//System.out.println("varitaion frame Back "+(pastFrame+i)+" = "+getAmountOfferVariationBackDepthFrame(rd,pastFrame+i,depth));
			ret+=getAmountOfferVariationBackDepthFrame(rd,pastFrame+i,depth);
		}
		//System.out.println("Total Back= "+ret);
		return ret;
	}
	
	public static double getAmountOfferVariationLayDepthWindow(RunnersData rd,int pastFrame,int size, int depth)
	{
		double ret=0;
		for (int i=0;i<size;i++)
		{
			//System.out.println("varitaion frame Lay "+(pastFrame+i)+" = "+getAmountOfferVariationLayDepthFrame(rd,pastFrame+i,depth));
			ret+=getAmountOfferVariationLayDepthFrame(rd,pastFrame+i,depth);
		}
		
		//System.out.println("Total Lay= "+ret);	
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
			//System.out.println("varitaion frame volume "+(pastFrame+i)+" = "+getAmountMatchedVariationAxisFrame(rd,pastFrame+i,depth));
			ret+=getAmountMatchedVariationAxisFrame(rd,pastFrame+i,depth);
		}
		
		//System.out.println("Total Volume var= "+ret);	
		//System.out.println("AVG Volume var= "+ret/size);	
		return ret;
	}
	
	public static double getAmountMatchedVariationAVGAxisWindow(RunnersData rd,int pastFrame,int size, int depth)
	{
		if(size!=0)
			return getAmountMatchedVariationAxisWindow(rd,pastFrame,size,depth)/size;
		else
			return getAmountMatchedVariationAxisWindow(rd,pastFrame,size,depth);
	}	
	
	public static int getOddLayTickVariation(RunnersData rd, int pastFrame,int windowSize)
	{
		
		System.out.println("Variation ["+pastFrame+"]["+windowSize+"]="+(Utils.oddToIndex(Utils.getOddLayFrame(rd,pastFrame))-Utils.oddToIndex(Utils.getOddLayFrame(rd, windowSize+pastFrame-1)))+ " Odd["+pastFrame+"]="+Utils.getOddLayFrame(rd,pastFrame)+" Odd["+(windowSize+pastFrame-1)+"]="+Utils.getOddLayFrame(rd, windowSize+pastFrame-1));
		return Utils.oddToIndex(Utils.getOddLayFrame(rd,pastFrame))-Utils.oddToIndex(Utils.getOddLayFrame(rd, windowSize+pastFrame-1));
	
	}
	
	public static double getOddLayTickVariationIntegral(RunnersData rd,int pastFrame, int windowSize)
	{
		
		double indexRef=0;
		for(int i=pastFrame+windowSize;i<windowSize+windowSize+pastFrame;i++)
		{
			indexRef+=Utils.oddToIndex(Utils.getOddLayFrame(rd, i));
			//System.out.println("Odd Lay "+Utils.getOddLayFrame(rd, i)+" index "+Utils.oddToIndex(Utils.getOddLayFrame(rd, i))+" on frame "+i);
		}
		indexRef/=windowSize;
		//System.out.println("Index Ref "+ indexRef);
		
		double ret=0;
		for(int i=pastFrame;i<windowSize+pastFrame;i++)
		{
			ret-=(indexRef-(double)Utils.oddToIndex(Utils.getOddLayFrame(rd,i)));
		}
		//System.out.println("Variation ["+pastFrame+"]["+windowSize+"]="+(Utils.oddToIndex(Utils.getOddLayFrame(rd,pastFrame))-Utils.oddToIndex(Utils.getOddLayFrame(rd, windowSize+pastFrame-1)))+ " Odd["+pastFrame+"]="+Utils.getOddLayFrame(rd,pastFrame)+" Odd["+(windowSize+pastFrame-1)+"]="+Utils.getOddLayFrame(rd, windowSize+pastFrame-1));
		//return Utils.oddToIndex(Utils.getOddLayFrame(rd,pastFrame))-Utils.oddToIndex(Utils.getOddLayFrame(rd, windowSize+pastFrame-1));
		return ret;
	}
	
	public static double getOddLayTickVariationIntegral(RunnersData rd,int pastFrame, int windowSize, int refFramesSize)
	{
		
		double indexRef=0;
		for(int i=pastFrame+windowSize;i<refFramesSize+windowSize+pastFrame;i++)
		{
			indexRef+=Utils.oddToIndex(Utils.getOddLayFrame(rd, i));
			//System.out.println("Odd Lay "+Utils.getOddLayFrame(rd, i)+" index "+Utils.oddToIndex(Utils.getOddLayFrame(rd, i))+" on frame "+i);
		}
		indexRef/=refFramesSize;
		//System.out.println("Index Ref "+ indexRef);
		
		double ret=0;
		for(int i=pastFrame;i<windowSize+pastFrame;i++)
		{
			ret-=(indexRef-(double)Utils.oddToIndex(Utils.getOddLayFrame(rd,i)));
		}
		//System.out.println("Variation ["+pastFrame+"]["+windowSize+"]="+(Utils.oddToIndex(Utils.getOddLayFrame(rd,pastFrame))-Utils.oddToIndex(Utils.getOddLayFrame(rd, windowSize+pastFrame-1)))+ " Odd["+pastFrame+"]="+Utils.getOddLayFrame(rd,pastFrame)+" Odd["+(windowSize+pastFrame-1)+"]="+Utils.getOddLayFrame(rd, windowSize+pastFrame-1));
		//return Utils.oddToIndex(Utils.getOddLayFrame(rd,pastFrame))-Utils.oddToIndex(Utils.getOddLayFrame(rd, windowSize+pastFrame-1));
		return ret;
	}
	
	public static double getOddLayTickVariationIntegralABSStep(RunnersData rd,int pastFrame, int windowSize, int refFramesSize)
	{
		
		double indexRef=0;
		for(int i=pastFrame+windowSize;i<refFramesSize+windowSize+pastFrame;i++)
		{
			indexRef+=Utils.oddToIndex(Utils.getOddLayFrame(rd, i));
			//System.out.println("Odd Lay "+Utils.getOddLayFrame(rd, i)+" index "+Utils.oddToIndex(Utils.getOddLayFrame(rd, i))+" on frame "+i);
		}
		indexRef/=refFramesSize;
		//System.out.println("Index Ref "+ indexRef);
		
		double ret=0;
		for(int i=pastFrame;i<windowSize+pastFrame;i++)
		{
			
			ret+=Math.abs((indexRef-(double)Utils.oddToIndex(Utils.getOddLayFrame(rd,i))));
		}
		//System.out.println("Variation ["+pastFrame+"]["+windowSize+"]="+(Utils.oddToIndex(Utils.getOddLayFrame(rd,pastFrame))-Utils.oddToIndex(Utils.getOddLayFrame(rd, windowSize+pastFrame-1)))+ " Odd["+pastFrame+"]="+Utils.getOddLayFrame(rd,pastFrame)+" Odd["+(windowSize+pastFrame-1)+"]="+Utils.getOddLayFrame(rd, windowSize+pastFrame-1));
		//return Utils.oddToIndex(Utils.getOddLayFrame(rd,pastFrame))-Utils.oddToIndex(Utils.getOddLayFrame(rd, windowSize+pastFrame-1));
		return ret;
	}

	
	
	public static double getWomAVGWindow (RunnersData rd,int pastFrame, int windowSize,int depth,boolean includeGaps)
	{
		double ac=0;
		for(int i=0;i<windowSize+1;i++)
		{
			ac+=Utils.getWomFrame(rd,depth,includeGaps,i+pastFrame);
		}
		
		return ac/windowSize+1;
	}
	
	public static double getWomOthersAVGWindow (RunnersData rd,int pastFrame, int windowSize,int depth,boolean includeGaps,double oddLimit)
	{
		
		
		Vector<RunnersData> usedRunners=new Vector<RunnersData>();
		for(RunnersData rdIteretor:rd.getMarketData().getRunners())
			if(rdIteretor!=rd && Utils.getOddBackFrame(rdIteretor, pastFrame)<oddLimit)
				usedRunners.add(rdIteretor);

		if(usedRunners.size()==0)
			return 0;
		
		double ac=0;
		for(RunnersData rdOther:usedRunners)
			ac+=getWomAVGWindow (rdOther, pastFrame, windowSize, depth, includeGaps);
			
		return ac/usedRunners.size();
	}
	
	public static double[] getMaxTickVariation(RunnersData rd,int pastFrame, int windowSize,int refFramesSize)
	{
		double ret[]=new double[2]; //  [0]-up [1]-down 
		
		if(!Utils.isValidWindow(rd, windowSize, pastFrame ))
		{
			System.out.println("Not Valid window at getMaxTickVariation()");
			return null;
		}
		
		double refOdd=Utils.getOddLayAVG(rd, refFramesSize, pastFrame+windowSize);
		
		for(int i=pastFrame;i<pastFrame+windowSize;i++)
		{
			double present=Utils.getOddLayFrame(rd, i);
			int var=Utils.oddToIndex(Utils.nearValidOdd(present))-Utils.oddToIndex(Utils.nearValidOdd(refOdd));
			if(ret[0]<var) ret[0]=var;
			if(ret[1]>var) ret[1]=var;
			
		}
		
		return ret;
	}
	
}
