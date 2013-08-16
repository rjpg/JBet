package categories.categories2013;

import DataRepository.RunnersData;
import DataRepository.Utils;

public class OddInterval extends CategoryNode{

	
	double oddStart;
	double oddEnd;
	
	public OddInterval(double oddStartA,double oddEndA,String path) {
		oddStart=oddStartA;
		oddEnd=oddEndA;
		setPath(path);
		initialize();
	}

	public void initialize()
	{
		
		addChild(new Liquidity(0, 2000, "lowLiquidity"));
		addChild(new Liquidity(2000.01, 10000, "lowLiquidity"));
		addChild(new Liquidity(10000.01, Double.MAX_VALUE, "lowLiquidity"));
	}

	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		if(Utils.getOddBackFrame(rd, 0)>=oddStart && Utils.getOddBackFrame(rd, 0)<=oddEnd)
			return true;
		else
			return false;
	}


	

}
