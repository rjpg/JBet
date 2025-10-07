package categories.categories2018.cattree;

import java.util.Calendar;
import java.util.Vector;

import DataRepository.RunnersData;
import categories.categories2013.CategoryNode;

public class TimePreLive extends CategoryNode{

	int minuteStartInterval;
	int minuteEndInterval;
	
	public TimePreLive(Vector<CategoryNode> ancestorsA,int minuteStartIntervalA,int minuteEndIntervalA,String path) {
		super(ancestorsA);
		minuteStartInterval=minuteStartIntervalA;
		minuteEndInterval=minuteEndIntervalA;
		setPath(path);
		initialize();
		
	}
	
	public void initialize()
	{
		addChild(new OddInterval(getAncestors(),1.01,4.0,"lowOdd"));
		addChild(new OddInterval(getAncestors(),4.1,6,"midleOdd"));
		addChild(new OddInterval(getAncestors(),6.2,12,"highOdd"));
	}
	
	
	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		
		long nowMin=rd.getMarketData().getCurrentTime().getTimeInMillis();
		long startMin=rd.getMarketData().getStart().getTimeInMillis();
		long sub=startMin-nowMin;
	
		int minuteToStart=(int)(sub/60000);
		
		//System.out.println("minute to start "+minuteToStart);
		
		int timeOffSet=0;
		//if(CategoriesParameters.COLLECT)
		//	timeOffSet=CategoriesParameters.MINUTES_PREDICTION;
		
		if(minuteToStart<=(minuteStartInterval - timeOffSet) &&
				minuteToStart>=(minuteEndInterval - timeOffSet))
			return true;
		else
			return false;
	}

}
