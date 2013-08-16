package categories.categories2013;

import horses.HorsesUtils;
import DataRepository.RunnersData;

public class Length extends CategoryNode{

	int secsStartInterval;
	int secsEndInterval;
	
	public Length(int secsStartIntervalA,int secsEndIntervalA,String path) {
		secsStartInterval=secsStartIntervalA;
		secsEndInterval=secsEndIntervalA;
		setPath(path);
		initialize();
		
	}
	
	public void initialize()
	{
		addChild(new Favorite(1, 2.5, "favorite"));
		addChild(new Favorite(2.52, 1000, "nofavorite"));
	}
	
	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		if(HorsesUtils.getTimeRaceInSeconds(rd.getMarketData().getName())>=secsStartInterval &&
				HorsesUtils.getTimeRaceInSeconds(rd.getMarketData().getName())<=secsEndInterval)
			return true;
		else
			return false;
	}



}
