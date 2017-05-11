package categories.categories2013.bots;

import java.io.File;
import java.util.Vector;

import categories.categories2013.CategoryNode;
import categories.categories2013.Root;

public class PrintValidCategoriesDirs {

	public static void main(String[] args) {
			
			Root root=new Root(0);
			
			
			for(int i=0;i<648;i++)
			{
				
				Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
				String fileName=CategoryNode.getAncestorsStringPath(cat)+"NNNormalizeData.csv";
				
				
				
				File file = new File(fileName);
				if(file.exists()) {
					System.out.println(i+","+file.getParentFile().getAbsolutePath());
				}
			}
			
	}
	
}
