package categories.categories2013;

import java.util.Calendar;
import java.util.Vector;

import horses.HorsesUtils;
import DataRepository.RunnersData;

public class DayPart extends CategoryNode{
	
	int hourStartInterval;
	int hourEndInterval;
	
	public DayPart(Vector<CategoryNode> ancestorsA,int hourStartIntervalA,int hourEndIntervalA,String path) {
		super( ancestorsA);
		hourStartInterval=hourStartIntervalA;
		hourEndInterval=hourEndIntervalA;
		setPath(path);
		initialize();
		
	}
	
	public void initialize()
	{
		addChild(new NumberOfRunners(getAncestors(),1, 5, "fewRunners"));
		addChild(new NumberOfRunners(getAncestors(),5, 10, "mediumRunners"));
		addChild(new NumberOfRunners(getAncestors(),11, 25, "manyRunners"));
	}
	
	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		if(rd.getMarketData().getStart().get(Calendar.HOUR_OF_DAY)>=hourStartInterval &&
				rd.getMarketData().getStart().get(Calendar.HOUR_OF_DAY)<=hourEndInterval)
			return true;
		else
			return false;
	}


}
