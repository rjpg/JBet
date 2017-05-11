package categories.categories2013.bots;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

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

		System.out.println("END OF FILE : "+ fileName );
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
			
			lineExample+=(int)(((ex[ex.length-1]+1)*2)+0.01);
			
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
		Vector<double[]> rawData=loadFileIntoMemory("NNNormalizeData.csv");
		writeTalbleFile(rawData,"NNNormalizeData-out.csv");
		
	}
	
}
