package categories.categories2013;

import java.io.File;
import java.util.Vector;

public class DeleteEncogTrainData {

public static void main(String[] args) {
		
		Root root=new Root(0);
		
		CategoryNode.printIDs(root);
		//CategoryNode.buildDirectories(root);
		//int i=201;
		for(int i=0;i<648;i++)
		{
			Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
			String fileName=CategoryNode.getAncestorsStringPath(cat)+"nn-train-data.egb";
			
			
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
