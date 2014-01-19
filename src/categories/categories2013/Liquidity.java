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
		
		//System.out.println(" active : " + active);
		if(!active)
			return false;
		//System.out.println("Matched Amount - "+ rd.getName()+" - "+ Utils.isValidWindow(rd, 120, 0) );
		
		if(CategoriesParameters.clollect)
		{
			if(Utils.isValidWindow(rd, CategoriesParameters.framesPrediction, 0))
			{
				System.out.println("Matched Amount - "+ rd.getName()+" - "+ Utils.getMatchedAmount(rd, 90) );
				if(Utils.getMatchedAmount(rd, CategoriesParameters.framesPrediction)>=valueStart && Utils.getMatchedAmount(rd, CategoriesParameters.framesPrediction)<=valueEnd)
					return true;
				else
					return false;
			}
			else
			{
				System.out.println("No Vald window to get Matched Amount - "+ rd.getName() );
				return false;
			}
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
