package categories.categories2013;

import java.util.Vector;

import DataRepository.RunnersData;

public abstract class CategoryNode {

	public Vector<CategoryNode> childs;

	public int idStart=0;
	public int idEnd=0;
	
	public String path="";
		
	public CategoryNode getNextLevel(RunnersData rd) {
		for(CategoryNode cn:getChilds())
			if(cn.isRunnerOnThisCategory(rd))
				return cn;
		return null;
	}
	
	public int buildChilds(int startId) {
		setIdStart(startId);
				
		int id=startId;
		for(CategoryNode cn:getChilds())
		{
			id=cn.buildChilds(id);
		}
		setIdEnd(id);
		return id;
	}
	public abstract boolean isRunnerOnThisCategory(RunnersData rd);
	
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public Vector<CategoryNode> getChilds() {
		return childs;
	}

	public void setChilds(Vector<CategoryNode> childs) {
		this.childs = childs;
	}

	public int getIdStart() {
		return idStart;
	}

	public void setIdStart(int idStart) {
		this.idStart = idStart;
	}

	public int getIdEnd() {
		return idEnd;
	}

	public void setIdEnd(int idEnd) {
		this.idEnd = idEnd;
	}

	public void addChild(CategoryNode child)
	{
		childs.add(child);
	}

}
