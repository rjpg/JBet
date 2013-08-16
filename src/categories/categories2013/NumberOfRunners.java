package categories.categories2013;

import DataRepository.RunnersData;

public class NumberOfRunners extends CategoryNode{
	
	int numberStartInterval;
	int numberEndInterval;
	
	public NumberOfRunners(int numberStartIntervalA,int numberEndIntervalA,String path) {
		numberStartInterval=numberStartIntervalA;
		numberEndInterval=numberEndIntervalA;
		setPath(path);
		initialize();
		
	}
	
	public void initialize()
	{
		addChild(new TimePreLive(10, 5, "farFromBegining"));
		addChild(new Favorite(6, 2, "mediumFromBegining"));
		addChild(new Favorite(1, 0, "nearFromBegining"));
	}
	
	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		if(rd.getMarketData().getRunners().size()>=numberStartInterval &&
				rd.getMarketData().getRunners().size()<=numberEndInterval)
			return true;
		else
			return false;
	}



}
