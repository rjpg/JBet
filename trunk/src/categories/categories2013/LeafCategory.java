package categories.categories2013;

import java.util.Vector;

import DataRepository.RunnersData;

public class LeafCategory extends CategoryNode{

	public LeafCategory(Vector<CategoryNode> ancestorsA) {
		super(ancestorsA);
		setPath("");
	}
	
	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		return true;
	}

	
}
