package categories.categories2013;

import java.util.Vector;

import DataRepository.RunnersData;
import DataRepository.Utils;

public class Liquidity extends CategoryNode{
	
	double valueStart;
	double valueEnd;
	
	public Liquidity(Vector<CategoryNode> ancestorsA,double valueStartA,double valueEndA,String path) {
		super(ancestorsA);
		valueStart=valueStartA;
		valueEnd=valueEndA;
		setPath(path);
		initialize();
	}

	public void initialize()
	{
		addChild(new LeafCategory(getAncestors()));
	}

	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		if(Utils.getMatchedAmount(rd, 0)>=valueStart && Utils.getMatchedAmount(rd, 0)<=valueEnd)
			return true;
		else
			return false;
	}



}
