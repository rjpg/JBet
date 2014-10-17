package categories.categories2013.bots;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class ProcessStats {
	
	public Hashtable<Integer, Double> stats=new Hashtable<Integer, Double>();
	
	public ProcessStats()
	{
		String fileName="root/stats.txt";
		File ff=new File(fileName);
		BufferedReader inputFile=ProcessNNRawData.getBufferedReader(ff);
		
		if(inputFile == null)
			return;
		
		String s;
		try {
			while ((s=inputFile.readLine()) != null)
			{
				String saux[]=s.split(" ");
				
				int catId=Integer.parseInt(saux[0]);
				double value=Double.parseDouble(saux[1]);
				if(stats.containsKey(catId))
				{
					stats.put(catId, (Double)stats.get(catId)+value);
				}
				else
				{
					stats.put(catId, value);
				}
				
				

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ;
		}

		System.out.println("END OF FILE : "+ fileName );
		try {
			inputFile.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		
		Enumeration<Integer> items = stats.keys();
		double profit=0;
		while(items.hasMoreElements())
		{	
			int id=items.nextElement();
		    System.out.println(id+ " "+stats.get(id));
		    if(stats.get(id)>0)
		    	profit+=stats.get(id);
		}
		
		System.out.println("Total Positive :"+profit);

	}
	
	public boolean isProfitableCat(int id)
	{
		if(stats.containsKey(id))
		{
			if(stats.get(id)>0.00)
				return true;
			else
				return false;
		}
		else
			return false;
	}
	
	public static void main(String[] args) {
		
		
		ProcessStats pc=new ProcessStats();
		
		System.out.println("id 192 is "+pc.isProfitableCat(192));
		
	}
}
