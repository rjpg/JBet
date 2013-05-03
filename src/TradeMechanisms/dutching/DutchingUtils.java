package TradeMechanisms.dutching;

import java.util.Vector;

import DataRepository.OddData;

public class DutchingUtils {

	public static double calculateMargin(Vector<OddData> vod)
	{
		double ret=0;
		
		for(OddData od:vod)
		{
			ret+=(1/od.getOdd());
		}
		
		return ret;
	}
	
	public static void main(String[] args) {
		
		
	}
}
