package categories.categories2013.bots;

import DataRepository.RunnersData;
import DataRepository.Utils;

public class UtilsCollectData {

	
	public static double getAmountOfferVariationOddFrame(RunnersData rd,int pastFrame, double odd)
	{
		double actual=Utils.getAmountOfferOddFrame(rd,pastFrame, odd);
		System.out.println("amount actual on "+odd+" is :"+actual);
		double prev=Utils.getAmountOfferOddFrame(rd,(pastFrame+1), odd);
		System.out.println("amount prev on "+odd+" is :"+prev);
		return actual-prev;
		
	}
	
}
