package categories.categories2013;

import java.util.Vector;

import DataRepository.RunnersData;

public class NumberOfRunners extends CategoryNode{
	
	int numberStartInterval;
	int numberEndInterval;
	
	public NumberOfRunners(Vector<CategoryNode> ancestorsA,int numberStartIntervalA,int numberEndIntervalA,String path) {
		super(ancestorsA);
		numberStartInterval=numberStartIntervalA;
		numberEndInterval=numberEndIntervalA;
		setPath(path);
		initialize();
		
	}
	
	public void initialize()
	{
		addChild(new TimePreLive(getAncestors(),10, 5, "farFromBegining"));
		addChild(new TimePreLive(getAncestors(),4, 2, "mediumFromBegining"));
		addChild(new TimePreLive(getAncestors(),1, 0, "nearFromBegining"));
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
