package categories.categories2013.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import categories.categories2013.CategoryNode;
import categories.categories2013.Root;

public class ChangeOutputToInt {

	
	
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

	
	public static Vector<double[]> loadFileIntoMemory(File file)
	{
		
		Vector<double[]> ret=new Vector<double[]>();
		
		File ff=file;
		BufferedReader inputFile=getBufferedReader(ff);
		
		if(inputFile == null)
			return null;
		
		String s;
		try {
			while ((s=inputFile.readLine()) != null)
			{
				String saux[]=s.split(",");
				double dataExample[]=new double[saux.length]; 
				
				for (int i=0;i<saux.length;i++)
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

		System.out.println("END OF FILE : "+ file.getPath() );
		try {
			inputFile.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return ret;
	}
	
	
	public static Vector<double[]> changeOutput(Vector<double[]> in)
	{
		
		Vector<double[]> ret=new Vector<double[]>();
		
		
		
		for(double[] example:in)
		{
			System.out.println(example[example.length-1]);
			System.out.println((int)(((example[example.length-1]+1)*2)+0.01));
			
		}
		
		return ret;
	}

	public static void writeTalbleFile(Vector<double[]> examples,String fileNameA)
	{
		String fileName=fileNameA;
		
		
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
		
		System.out.println("Writting data to file - "+fileName);
		
		//deleteFile(fileName);
		
		BufferedWriter out=null;
		
		try {
			out = new BufferedWriter(new FileWriter(fileName, true));
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open "+fileName+" for writing");
			}
		if(out==null)
		{
			System.err.println("could not open "+fileName);
			return;
		}
		
		
		for(double[] ex:examples)
		{
			String lineExample="";
			
			for(int i=0;i<ex.length-1;i++)
			{
				lineExample+=ex[i]+",";
			}
			
			int outputClass=(int)(((ex[ex.length-1]+1)*2)+0.01);  //0,2,3,4
			
			///////////////////////////////////////////////////////////////////
			if(outputClass==0 || outputClass==1)
				outputClass=0;
			else if(outputClass==2)
				outputClass=1;
			else outputClass = 2;                     // transform  to  0,1,2
			///////////////////////////////////////////////////////////////////
			
			lineExample+=outputClass;
			
			//lineExample = lineExample.substring(0, lineExample.length() - 1);
			//lineExample+="\n";
			
			
			try {
				out.write(lineExample);
				out.newLine();
				out.flush();
			} catch (IOException e) {
				System.out.println("Error wrtting data to file - "+fileName);
				e.printStackTrace();
			}
		}
		
		System.out.println("Writting data to file over and OK - closing - "+fileName);
		
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) {
		
		Root root=new Root(0);
		
		int i=607;
		//for(int i=0;i<648;i++)
		{
			
			Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
			String fileName=CategoryNode.getAncestorsStringPath(cat)+"NNNormalizeData.csv";
			
			
			
			File file = new File(fileName);
			if(file.exists()) {
				System.out.println("Category ID:"+i+"  "+file.getParentFile().getAbsolutePath());
				
				Vector<double[]> rawData=loadFileIntoMemory(file);
				writeTalbleFile(rawData,file.getParentFile().getAbsolutePath()+"/NNNormalizeData-out-2.csv");
				
				//rawData=null;
			}
			else
			{
				System.out.println("Category ID:"+i+"  "+file.getParentFile().getAbsolutePath()+"NNNormalizeData.csv");
				System.out.println("Category ID:"+i+"  file does not exist" );
			}
		}
		
		
		
		
		
	}
	
}
