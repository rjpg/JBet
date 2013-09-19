package categories.categories2013;

import java.io.File;
import java.util.Vector;

import categories.categories2011.CategoriesManager;

import DataRepository.RunnersData;

public abstract class CategoryNode {

	public Vector<CategoryNode> ancestors;
	
	public Vector<CategoryNode> childs=new Vector<CategoryNode>();

	public int idStart=0;
	public int idEnd=0;
	
	public String path="";
		
	public CategoryNode(Vector<CategoryNode> ancestorsA) {
		ancestors=(Vector<CategoryNode>) ancestorsA.clone();
		getAncestors().add(this);
	}
	
	public CategoryNode getNextLevel(RunnersData rd) {
		for(CategoryNode cn:getChilds())
			if(cn.isRunnerOnThisCategory(rd))
				return cn;
		return null;
	}
	
	public int buildChilds(int startId) {
		setIdStart(startId);
		
//		String s="\\";
//		for(CategoryNode cn:getAncestors())
//			s+="\\"+cn.getPath();
//		System.out.println(s);
		
		int id=startId;
		if(getChilds().size()==0)
			id++;
		
		for(CategoryNode cn:getChilds())
		{
			id=cn.buildChilds(id);
		}
		setIdEnd(id);
		
		
		return id;
	}
	
	public static void printIDs(CategoryNode cat)
	{
		if(cat.getChilds().size()==0)
		{
			String s="";
			for(CategoryNode cn:cat.getAncestors())
				s+="\\"+cn.getPath();
			System.out.println(s+" Start ID="+cat.getIdStart()+" End ID="+cat.getIdEnd());
			
		}
		for(CategoryNode cn:cat.getChilds())
		{
			printIDs(cn);
		}
	}
	
	public static void buildDirectories(CategoryNode cat)
	{
		
		  
		  if(cat.getChilds().size()==0)
			{
				String s="";
				for(CategoryNode cn:cat.getAncestors())
					s+="\\"+cn.getPath();
				//System.out.println(s+" Start ID="+cat.getIdStart()+" End ID="+cat.getIdEnd());
				s=s.substring(1);
				try{ 
					boolean success = new File(s).mkdirs();
					  if (success) {
						  System.out.println("Directories: " 
								  + s + " created");
					  }

				  }catch (Exception e){//Catch exception if any
					  System.err.println("Error: " + e.getMessage());
				  }
				
			}
			for(CategoryNode cn:cat.getChilds())
			{
				CategoryNode.buildDirectories(cn);
			}
	}
	
	public static Vector<CategoryNode> getAncestorsById(CategoryNode cat,int id)
	{
		
		if(cat.getIdStart()==id /*&& cat.getIdEnd()==id+1*/)
			return cat.getAncestors();
		
		for(CategoryNode cn:cat.getChilds())
		{
			System.out.println("cat path : "+cn.getPath()+" Id start : "+cn.getIdStart()+" Id end : "+cn.getIdEnd());
			if(cn.getIdStart()<=id && cn.getIdEnd()>id)
				return getAncestorsById(cn,id);
		}
			
		return null;
	}
	
	public static Vector<CategoryNode> getAncestorsByRunner(CategoryNode cat,RunnersData rd)
	{
	
		System.out.println("cat path : "+cat.getPath());
		if(cat.getChilds().size()==0)
			return cat.getAncestors();
		
		
		for(CategoryNode cn:cat.getChilds())
		{
			//System.out.println("cat path : "+cn.getPath()+" Id start : "+cn.getIdStart()+" Id end : "+cn.getIdEnd());
			if(cn.isRunnerOnThisCategory(rd))
				return getAncestorsByRunner(cn,rd);
		}
			
		return null;
	}
	
	
	public static String getAncestorsStringPath(Vector<CategoryNode> cat)
	{
		
		if(cat==null)
			return null;
		
		String ret="";
		for(CategoryNode cn:cat)
			ret+=(cn.getPath()+"\\");
	
		return ret;
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

	public Vector<CategoryNode> getAncestors() {
		return ancestors;
	}

	public void setAncestors(Vector<CategoryNode> ancestors) {
		this.ancestors = ancestors;
	}
	
	public static void main(String[] args) {
		Root root=new Root(0);
		
		CategoryNode.printIDs(root);
		//CategoryNode.buildDirectories(root);
		
		Vector<CategoryNode> cnv=CategoryNode.getAncestorsById(root,500);
		
		for(CategoryNode cn:cnv)
			System.out.print(cn.getPath()+"\\");
		
	}
}
