package categories.categories2013;

import DataRepository.RunnersData;
import DataRepository.Utils;

public class Favorite extends CategoryNode{

	double oddStart;
	double oddEnd;
	
	
	public Favorite(double oddStartA,double oddEndA,String path) {
		oddStart=oddStartA;
		oddEnd=oddEndA;
		setPath(path);
	}
	
	public void initialize()
	{
		addChild(new DayPart(12, 17, "begining"));
		addChild(new DayPart(18, 22, "ending"));
	}
	
	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		if(Utils.getOddBackFrame(rd, 0)>=oddStart && Utils.getOddBackFrame(rd, 0)<=oddEnd)
			return true;
		else
			return false;
	}



}
