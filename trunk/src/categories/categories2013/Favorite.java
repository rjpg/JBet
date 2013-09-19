package categories.categories2013;

import java.util.Vector;

import DataRepository.RunnersData;
import DataRepository.Utils;

public class Favorite extends CategoryNode{

	double oddStart;
	double oddEnd;
	
	
	public Favorite(Vector<CategoryNode> ancestorsA, double oddStartA,double oddEndA,String path) {
		super(ancestorsA);
		oddStart=oddStartA;
		oddEnd=oddEndA;
		setPath(path);
		initialize();
	}
	
	public void initialize()
	{
		addChild(new DayPart(getAncestors(),12, 17, "beginingDay"));
		addChild(new DayPart(getAncestors(),18, 22, "endingDay"));
	}
	
	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		
		RunnersData rdLow = rd.getMarketData().getRunners().get(0);

		for (RunnersData rdAux : rd.getMarketData().getRunners())
			if (Utils.getOddBackFrame(rdAux, 0) < Utils.getOddBackFrame(
					rdLow, 0))
				rdLow = rdAux;
		
		if(Utils.getOddBackFrame(rdLow, 0)>=oddStart && Utils.getOddBackFrame(rdLow, 0)<=oddEnd)
			return true;
		else
			return false;
	}



}
