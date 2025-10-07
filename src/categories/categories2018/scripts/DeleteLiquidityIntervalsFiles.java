package categories.categories2018.scripts;

import java.io.File;
import java.util.Vector;

import categories.categories2013.CategoryNode;
import categories.categories2018.cattree.Root2018;

public class DeleteLiquidityIntervalsFiles {

	
	public static void main(String[] args) {
		
		Root2018 root=new Root2018(0);
		
		CategoryNode.printIDs(root);
		//CategoryNode.buildDirectories(root);
		
		for(int i=root.getIdStart();i<root.getIdEnd();i++)
		{
			Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
			
			String fileName=cat.get(0).getPath();;
			for(int x=1;x<cat.size()-1;x++)
				fileName+="/"+cat.get(x).getPath();
			
			fileName+="/liquitidyIntervalsFile.txt";
			
			File file = new File(fileName);
			if(file.exists()) { 
				System.out.println("File found in "+fileName);
				//file.setWritable(true);
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
