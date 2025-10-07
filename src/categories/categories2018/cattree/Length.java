package categories.categories2018.cattree;

import java.util.Vector;

import horses.HorsesUtils;
import DataRepository.RunnersData;
import categories.categories2013.CategoryNode;

public class Length extends CategoryNode{

	int secsStartInterval;
	int secsEndInterval;
	
	public Length(Vector<CategoryNode> ancestorsA,int secsStartIntervalA,int secsEndIntervalA,String path) {
		super(ancestorsA);
		secsStartInterval=secsStartIntervalA;
		secsEndInterval=secsEndIntervalA;
		setPath(path);
		//initialize();
		
	}
	
	public void initialize()
	{
		addChild(new Favorite(getAncestors(),1, 2.5, "favorite"));
		addChild(new Favorite(getAncestors(),2.52, 1000, "nofavorite"));
	}
	
	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		//System.out.println("Lenght time = "+HorsesUtils.getTimeRaceInSeconds(rd.getMarketData().getName())); 
		if(HorsesUtils.getTimeRaceInSeconds(rd.getMarketData().getName())>=secsStartInterval &&
				HorsesUtils.getTimeRaceInSeconds(rd.getMarketData().getName())<=secsEndInterval)
			return true;
		else
			return false;
	}



}
