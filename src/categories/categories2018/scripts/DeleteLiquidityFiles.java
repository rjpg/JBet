package categories.categories2018.scripts;

import java.io.File;
import java.util.Vector;

import categories.categories2013.CategoryNode;
import categories.categories2018.cattree.Root2018;

public class DeleteLiquidityFiles {

	public static void main(String[] args) {
		
		Root2018 root=new Root2018(0);
		
		System.out.println("root2018 ids : "+root.getIdStart()+"..."+root.getIdEnd());
		CategoryNode.printIDs(root);
		//CategoryNode.buildDirectories(root);
		
		for(int i=root.getIdStart();i<root.getIdEnd();i++)
		{
			Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
			
			String fileName=cat.get(0).getPath();
			for(int x=1;x<cat.size()-1;x++)
				fileName+="/"+cat.get(x).getPath();
			
			fileName+="/liquitidyFile.txt";
			
			File file = new File(fileName);
			if(file.exists()) { 
				System.out.println("File found in "+fileName);
				if(file.delete()){
	    			System.out.println(file.getName() + " is deleted!");
	    		}else{
	    			System.out.println("Delete operation is failed.");
	    		}
				System.gc();
			}
			else
			{
				System.out.println("File Not found in "+fileName);
			}
			
		}
		
		
	}
}
