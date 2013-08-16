package categories.categories2013;

import java.util.Calendar;

import horses.HorsesUtils;
import DataRepository.RunnersData;

public class DayPart extends CategoryNode{
	
	int hourStartInterval;
	int hourEndInterval;
	
	public DayPart(int hourStartIntervalA,int hourEndIntervalA,String path) {
		hourStartInterval=hourStartIntervalA;
		hourEndInterval=hourEndIntervalA;
		setPath(path);
		initialize();
		
	}
	
	public void initialize()
	{
		addChild(new NumberOfRunners(1, 5, "few"));
		addChild(new NumberOfRunners(5, 10, "medium"));
		addChild(new NumberOfRunners(11, 25, "many"));
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
