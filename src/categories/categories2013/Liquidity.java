package categories.categories2013;

import DataRepository.RunnersData;
import DataRepository.Utils;

public class Liquidity extends CategoryNode{
	
	double valueStart;
	double valueEnd;
	
	public Liquidity(double valueStartA,double valueEndA,String path) {
		valueStart=valueStartA;
		valueEnd=valueEndA;
		setPath(path);
		initialize();
	}

	public void initialize()
	{
		
	}

	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		if(Utils.getMatchedAmount(rd, 0)>=valueStart && Utils.getMatchedAmount(rd, 0)<=valueEnd)
			return true;
		else
			return false;
	}



}
