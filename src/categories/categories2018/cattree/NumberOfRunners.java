package categories.categories2018.cattree;

import java.util.Vector;

import DataRepository.RunnersData;
import categories.categories2013.CategoryNode;

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
		//addChild(new TimePreLive(getAncestors(),10, 5, "farFromBegining"));
		//addChild(new TimePreLive(getAncestors(),4, 2, "mediumFromBegining"));
		//addChild(new TimePreLive(getAncestors(),1, 0, "nearFromBegining"));
		
		addChild(new OddInterval(getAncestors(),1.01,4.0,"lowOdd"));
		addChild(new OddInterval(getAncestors(),4.1,6,"midleOdd"));
		addChild(new OddInterval(getAncestors(),6.2,12,"highOdd"));

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
