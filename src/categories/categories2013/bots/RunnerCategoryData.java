package categories.categories2013.bots;

import java.util.Vector;

import DataRepository.RunnersData;
import categories.categories2013.CategoryNode;

public class RunnerCategoryData {

	Vector<CategoryNode> cat=null;
	
	RunnersData rd;
	
	public RunnerCategoryData(RunnersData rdA,Vector<CategoryNode> catA) {
		rd=rdA;
		cat=catA;
	}
}
