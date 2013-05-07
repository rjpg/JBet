package TradeMechanisms.dutching;

import java.util.Vector;

import bets.BetData;

import DataRepository.OddData;

public class DutchingUtils {

	public static double calculateMargin(Vector<OddData> vod)
	{
		double ret=0.;
		
		for(OddData od:vod)
		{
			ret+=(1./od.getOdd());
		}
		
		return ret;
	}
	
	public static void calculateAmounts(Vector<OddData> vod,double globalStake)
	{
		double margin=calculateMargin(vod);
		
				
		for(OddData od:vod)
		{
			od.setAmount(globalStake*((1./od.getOdd()/margin)));
		}
		
	
	}
	
	public static double calculateGlobalStake(OddData od,double margin)
	{
		return (margin/(1./od.getOdd()))*od.getAmount();
	}
	
	public static double[] calculateNetProfitLoss(Vector<OddData> vod)
	{
		double ret[]=new double[vod.size()];
		
		OddData odArray[]=vod.toArray(new OddData[]{});
		
		for(int i=0;i<ret.length;i++)
		{
			double reduction=0;
			for(int x=0;x<odArray.length;x++)
				if(x!=i)
					reduction+=odArray[x].getAmount();
			
			ret[i]=(odArray[i].getAmount()*(odArray[i].getOdd()-1))-reduction;
		}
		
		return ret;
	}
	
	public static OddData getTheBigestEntry(Vector<OddData> vod)
	{
		OddData odret=vod.get(0);
		
		for(OddData od:vod)
			if(od.getAmount()*od.getOdd()>odret.getAmount()*odret.getOdd())
				odret=od;
		
		return odret;
	}
	
	public static void main(String[] args) {
		
		OddData od1=new OddData(3.5, 0, BetData.BACK);
		OddData od2=new OddData(3.4, 0, BetData.BACK);
		OddData od3=new OddData(4.5, 0, BetData.BACK);
		OddData od4=new OddData(7.5, 0, BetData.BACK);
		OddData od5=new OddData(17, 0, BetData.BACK);
		
		Vector<OddData> odv=new Vector<OddData>();
				
		odv.add(od1);
		odv.add(od2);
		odv.add(od3);
		odv.add(od4);
		odv.add(od5);
		
		System.out.println(calculateMargin(odv));
		
		calculateAmounts(odv, 100);
		
		//od5.setAmount(0);
		double netProfit[]=calculateNetProfitLoss(odv);
		
		int i=0;
		for(OddData od:odv)
			System.out.println(od+" NET Profit/Loss : "+ netProfit[i++]);
		
			
		od5.setOdd(od5.getOdd()-1);
		
		System.out.println(calculateMargin(odv));
		
		calculateAmounts(odv, 100);
		
		//od5.setAmount(0);
		netProfit=calculateNetProfitLoss(odv);

		i=0;
		for(OddData od:odv)
			System.out.println(od+" NET Profit/Loss : "+ netProfit[i++]);

		
		
		od1.setAmount(od1.getAmount());
		System.out.println(calculateGlobalStake(od1, calculateMargin(odv)));
		
		
		
		od4.setAmount(od4.getAmount()+2);
		System.out.println(od4);
		System.out.println(getTheBigestEntry(odv));
		
	}
}
