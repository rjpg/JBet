package categories.categories2018.scripts;

import java.io.File;
import java.util.Vector;

import categories.categories2013.CategoryNode;
import categories.categories2018.cattree.Root2018;

public class DeleteRawData {

public static void main(String[] args) {
		
		Root2018 root=new Root2018(0);
		
		CategoryNode.printIDs(root);
		//CategoryNode.buildDirectories(root);
		
		System.out.println("--------------- Deleting NNRawDataIn2018.csv in all categories ---------------");
		
		for(int i=root.getIdStart();i<root.getIdEnd();i++)
		{
			Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
			String fileName=CategoryNode.getAncestorsStringPath(cat)+"NNRawDataIn2018.csv";
			
			
			File file = new File(fileName);
			if(file.exists()) { 
				System.out.println("File found in id=["+i+"] "+fileName);
				if(file.delete()){
	    			System.out.println(file.getName() + " is deleted!");
	    		}else{
	    			System.out.println("Delete operation is failed.");
	    		}
				System.gc();
			}
			else
			{
				System.out.println("File Not found in id=["+i+"] "+fileName);
			}
			
		}
		
		System.out.println("--------------- Deleting NNRawDataOut2018.csv in all categories ---------------");
		
		for(int i=root.getIdStart();i<root.getIdEnd();i++)
		{
			Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
			String fileName=CategoryNode.getAncestorsStringPath(cat)+"NNRawDataOut2018.csv";
			
			
			File file = new File(fileName);
			if(file.exists()) { 
				System.out.println("File found in id=["+i+"] "+fileName);
				if(file.delete()){
	    			System.out.println(file.getName() + " is deleted!");
	    		}else{
	    			System.out.println("Delete operation is failed.");
	    		}
				System.gc();
			}
			else
			{
				System.out.println("File Not found in id=["+i+"] "+fileName);
			}
			
		}
		
		
	}
	
	
}
