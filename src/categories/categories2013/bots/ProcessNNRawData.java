package categories.categories2013.bots;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import categories.categories2013.CategoryNode;
import categories.categories2013.Root;

public class ProcessNNRawData {
	
	public static BufferedReader getBufferedReader(File f)
	{
		BufferedReader input=null;
		try {
			input= new BufferedReader(new FileReader(f));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return input;
	}
	
	public static Vector<double[]> loadFileIntoMemory(String fileName)
	{
		
		Vector<double[]> ret=new Vector<double[]>();
		
		File ff=new File(fileName);
		BufferedReader inputFile=getBufferedReader(ff);
		
		if(inputFile == null)
			return null;
		
		String s;
		try {
			while ((s=inputFile.readLine()) != null)
			{
				String saux[]=s.split(" ");
				double dataExample[]=new double[DataWindowsSizes.INPUT_NEURONS+1]; 
				
				for (int i=0;i<DataWindowsSizes.INPUT_NEURONS+1;i++)
					dataExample[i]=Double.parseDouble(saux[i]);
				
				//System.out.println(fileName);
				//System.out.println(dataExample[35]);
				
				ret.add(dataExample);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		System.out.println("END OF FILE : "+ fileName );
		try {
			inputFile.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return ret;
	}
	
	
	public static Vector<double[]> removeToCollectExamples(Vector<double[]> examples)
	{
		
		Vector<double[]> ret=new Vector<double[]>();
		int size=examples.size();
		
		
		
		if(size<=DataWindowsSizes.COLLECT_EXAMPLES)
		{
			for(int i=0;i<examples.size();i++)
				ret.add(examples.get(i));
			return ret;
		}
			
		
		double step=(double)size/(double)DataWindowsSizes.COLLECT_EXAMPLES;
		double doubleIndex=0;
		for(int i =0;i<DataWindowsSizes.COLLECT_EXAMPLES;i++)
		{
			ret.add(examples.get((int)doubleIndex));
			doubleIndex+=step;
			
		}
		
		return ret;
		
		
	}
	
	public static void main(String[] args) {
			
			Root root=new Root(0);
			
			CategoryNode.printIDs(root);
			//CategoryNode.buildDirectories(root);
			
			int i=203;
			//for(int i=0;i<648;i++)
			{
				Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
				String fileName=CategoryNode.getAncestorsStringPath(cat)+"NNRawData.csv";
				
				
				File file = new File(fileName);
				if(file.exists()) { 
					System.out.println("File found in "+fileName);
					
					System.out.println("Loading "+fileName);
					Vector<double[]> examples =loadFileIntoMemory(fileName);
					System.out.println("Number of examples : "+examples.size() );
					
					Vector<double[]> collectExamples =removeToCollectExamples(examples);
					System.out.println("Removed - Number of examples : "+collectExamples.size() );
					
					System.gc();
				}
				else
				{
					System.out.println("File Not found in "+fileName);
				}
				
			}
			
			
		}

}
