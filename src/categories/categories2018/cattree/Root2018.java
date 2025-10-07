package categories.categories2018.cattree;

import java.util.Vector;

import DataRepository.RunnersData;
import categories.categories2013.CategoryNode;
import horses.HorsesUtils;

public class Root2018 extends CategoryNode{
	
	public Root2018(int startId) {
		super(new Vector<CategoryNode>());
		setPath("root2018");
		initialize();
		buildChilds(startId);  // run after 
	}
	
	public void initialize()
	{
		
		//addChild(new Length(getAncestors(),0, 120, "shortLenhgt"));
		//addChild(new Length(getAncestors(),121, 1000, "longLenght"));
		
		addChild(new Favorite(getAncestors(),1, 2.5, "favorite"));
		addChild(new Favorite(getAncestors(),2.52, 1000, "nofavorite"));
	}


	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) {
		if(HorsesUtils.getTimeRaceInSeconds(rd.getMarketData().getName())==-1)
			return false;
		else
			return true;
	}

	
	public static void main(String[] args) {
		Root2018 root=new Root2018(0);
		
		//CategoryNode.printIDs(root);
		//CategoryNode.buildDirectories(root);
		
		System.out.println("Cat max ID : "+root.getIdEnd());	
		//root.getAncestorsById(cat, id)
		int nCatActive=0;
		for(int i=0;i<root.getIdEnd();i++)
		{
			System.out.print("id="+i);
			Vector<CategoryNode> cnv=CategoryNode.getAncestorsById(root,i);
			
			if(((Liquidity)cnv.get(4)).active==true)
				nCatActive++;
			for(CategoryNode cn:cnv)
			{
				System.out.print(cn.getPath()+"/");
				
			}
			System.out.println();
		}
		
		System.out.println("active Cats : "+nCatActive);
		
		//System.out.println("Bulinding directories in file system");
		//CategoryNode.buildDirectories(root);
	}
	
	

}
