package categories.categories2013;

import java.util.Vector;

import horses.HorsesUtils;
import DataRepository.RunnersData;

public class Root extends CategoryNode{

	public Root(int startId) {
		super(new Vector<CategoryNode>());
		setPath("root");
		initialize();
		buildChilds(startId);
	}
	
	public void initialize()
	{
		
		addChild(new Length(getAncestors(),0, 120, "shortLenhgt"));
		addChild(new Length(getAncestors(),121, 1000, "longLenght"));
	}

	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		if(HorsesUtils.getTimeRaceInSeconds(rd.getMarketData().getName())==-1)
			return false;
		else
			return true;
	}

	
	

}
