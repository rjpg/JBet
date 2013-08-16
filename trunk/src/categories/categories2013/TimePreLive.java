package categories.categories2013;

import java.util.Calendar;

import DataRepository.RunnersData;

public class TimePreLive extends CategoryNode{

	int minuteStartInterval;
	int minuteEndInterval;
	
	public TimePreLive(int minuteStartIntervalA,int minuteEndIntervalA,String path) {
		minuteStartInterval=minuteStartIntervalA;
		minuteEndInterval=minuteEndIntervalA;
		setPath(path);
		initialize();
		
	}
	
	public void initialize()
	{
		addChild(new OddInterval(1.01,4,"Low"));
		addChild(new OddInterval(4.1,6,"Midle"));
		addChild(new OddInterval(6.2,12,"high"));
	}
	
	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		
		long nowMin=rd.getMarketData().getCurrentTime().getTimeInMillis();
		long startMin=rd.getMarketData().getStart().getTimeInMillis();
		long sub=startMin-nowMin;
	
		int minuteToStart=(int)(sub/60000);
		
		if(minuteToStart>=minuteStartInterval &&
				minuteToStart<=minuteEndInterval)
			return true;
		else
			return false;
	}

}
