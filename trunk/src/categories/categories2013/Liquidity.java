package categories.categories2013;

import java.util.Vector;

import DataRepository.RunnersData;
import DataRepository.Utils;

public class Liquidity extends CategoryNode{
	
	double valueStart;
	double valueEnd;
	
	boolean active=false;
	
	public Liquidity(Vector<CategoryNode> ancestorsA,double valueStartA,double valueEndA,String path,boolean activeA) {
		super(ancestorsA);
		valueStart=valueStartA;
		valueEnd=valueEndA;
		active=activeA;
		setPath(path);
		initialize();
	}

	public void initialize()
	{
		addChild(new LeafCategory(getAncestors()));
	}

	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		
		if(!active)
			return false;
		
		if(CategoriesParameters.clollect && Utils.isValidWindow(rd, 120, 0))
		{
			if(Utils.getMatchedAmount(rd, 120)>=valueStart && Utils.getMatchedAmount(rd, 120)<=valueEnd)
				return true;
			else
				return false;
		}
		else
		{
			if(Utils.getMatchedAmount(rd, 0)>=valueStart && Utils.getMatchedAmount(rd, 0)<=valueEnd)
				return true;
			else
				return false;

		}
	}



}
