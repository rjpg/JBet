package categories.categories2013;

import horses.HorsesUtils;
import DataRepository.RunnersData;

public class Root extends CategoryNode{

	public Root(int startId) {
		setPath("root");
		initialize();
		buildChilds(startId);
	}
	
	public void initialize()
	{
		
		addChild(new Length(0, 120, "short"));
		addChild(new Length(121, 1000, "long"));
	}

	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		if(HorsesUtils.getTimeRaceInSeconds(rd.getMarketData().getName())==-1)
			return false;
		else
			return true;
	}

	
	

}
