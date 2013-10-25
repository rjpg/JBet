package categories.categories2013;

import java.io.File;
import java.util.Vector;

public class DeleteLiquidityIntervalsFiles {

	
	public static void main(String[] args) {
		
		Root root=new Root(0);
		
		CategoryNode.printIDs(root);
		//CategoryNode.buildDirectories(root);
		
		for(int i=0;i<648;i++)
		{
			Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
			
			String fileName=cat.get(0).getPath();;
			for(int x=1;x<cat.size()-1;x++)
				fileName+="/"+cat.get(x).getPath();
			
			fileName+="/liquitidyIntervalsFile.txt";
			
			File file = new File(fileName);
			if(file.exists()) { 
				System.out.println("File found in "+fileName);
				if(file.delete()){
	    			System.out.println(file.getName() + " is deleted!");
	    		}else{
	    			System.out.println("Delete operation is failed.");
	    		}
			}
			else
			{
				System.out.println("File Not found in "+fileName);
			}
			
		}
		
		
	}
}
