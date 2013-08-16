package categories.categories2013;

import DataRepository.RunnersData;

public class LeafCategory extends CategoryNode{

	public LeafCategory() {
		setPath("");
	}
	
	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		return true;
	}

	
}
