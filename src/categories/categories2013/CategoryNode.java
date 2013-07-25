package categories.categories2013;

import java.util.Vector;

import DataRepository.RunnersData;

public abstract class CategoryNode {

	public Vector<CategoryNode> childs;
	
	public String path="";
		
	public abstract CategoryNode getNextLevel(RunnersData rd);
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
