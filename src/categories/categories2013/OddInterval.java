package categories.categories2013;

import java.util.Vector;

import DataRepository.RunnersData;
import DataRepository.Utils;

public class OddInterval extends CategoryNode{

	
	double oddStart;
	double oddEnd;
	
	public OddInterval(Vector<CategoryNode> ancestorsA,double oddStartA,double oddEndA,String path) {
		super(ancestorsA);
		oddStart=oddStartA;
		oddEnd=oddEndA;
		setPath(path);
		initialize();
	}

	public void initialize()
	{
		
		addChild(new Liquidity(getAncestors(),0, 2000, "lowLiquidity"));
		addChild(new Liquidity(getAncestors(),2000.01, 10000, "mediumLiquidity"));
		addChild(new Liquidity(getAncestors(),10000.01, Double.MAX_VALUE, "highLiquidity"));
	}

	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		if(Utils.getOddBackFrame(rd, 0)>=oddStart && Utils.getOddBackFrame(rd, 0)<=oddEnd)
			return true;
		else
			return false;
	}


	

}
