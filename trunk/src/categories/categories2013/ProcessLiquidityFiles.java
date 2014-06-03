package categories.categories2013;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Vector;

import javax.swing.JFrame;

import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MyChart2D;
import categories.categories2011.Histogram;

public class ProcessLiquidityFiles {

	
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
	
	public static int recordsInFile(String fileName,String fileNameOut)
	{
		File file = new File(fileName);
		if(file.exists()) {
			File f=new File(fileName);
			BufferedReader input=getBufferedReader(f);
			String s=null;
			
			Vector<Double> values=new Vector<Double>();
			
			try {
				s=input.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			double value=Double.parseDouble(s);
			values.add(value);
			
			double min=value;
			double max=value;
			int lines=1;
			
			try {
				while ((s=input.readLine()) != null)
				{
					lines++;
				
					value=Double.parseDouble(s);
					values.add(value);
					
					if(value>max)
						max=value;
					
					if(value<min)
						min=value;
					
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			double precision=(max-min)/1000;
			Histogram histogram=new Histogram(min, max, precision);
			for(double d:values)
				histogram.addValue(d);
			
			JFrame frame=new JFrame();
			MyChart2D chart=new MyChart2D();
			frame.add(chart);
			
			double axisx=min;
			for(int i=0;i<histogram.getIntervals().length;i++)
			{
				chart.addValue("histogram", axisx, histogram.getIntervals()[i], Color.BLUE);
				axisx+=precision;
			}
			
			double firstInterval=histogram.getMinFiltred(100-(100/3));
			double secondInterval=histogram.getMaxFiltred(100-(100/3));
			
			writeIntervals(fileNameOut,lines,firstInterval,secondInterval);
			
			chart.addValue("interval", firstInterval-1, 0, Color.RED);
			chart.addValue("interval", firstInterval, 10, Color.RED);
			chart.addValue("interval", firstInterval+1, 0, Color.RED);
			
			chart.addValue("interval", secondInterval-1, 0, Color.RED);
			chart.addValue("interval", secondInterval, 10, Color.RED);
			chart.addValue("interval", secondInterval+1, 0, Color.RED);
	
			frame.setVisible(true);
			
			return lines;
			
		}
		else
		{
			System.out.println("**** File Not found in *** "+fileName);
		}
		return 0;
	}
	
	public static void writeIntervals(String fileName,int nrecords,double firstInterval,double secondInterval)
	{
		
		System.out.println("writing to file : "+fileName);
		
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
		
		try {
			out.write(nrecords+"");
			out.newLine();
			out.write(firstInterval+"");
			out.newLine();
			out.write(secondInterval+"");
			
			out.flush();
		} catch (IOException e) {
			System.out.println(fileName+":Error wrtting data to log file");
			e.printStackTrace();
		}
		
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
	}
	
	public static void main(String[] args) {
		
		Root root=new Root(0);
		
		CategoryNode.printIDs(root);
		//CategoryNode.buildDirectories(root);
		
		OddInterval oddInterval=null;
		
		for(int i=0;i<648;i++)
		//int i=555;
		{
			Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
			
			/*String fileName=cat.get(0).getPath();;
			for(int x=1;x<cat.size()-1;x++)
				fileName+="/"+cat.get(x).getPath();
			*/
			
			OddInterval oddIntervalAux=(OddInterval) cat.get(cat.size()-2); 
			if(oddInterval!=oddIntervalAux)
			{
				oddInterval=oddIntervalAux;
				
				String fileName=cat.get(0).getPath();
				
				String fileNameOut=cat.get(0).getPath();
				
				for(int x=1;x<cat.size()-1;x++)
				{
					fileName+="/"+cat.get(x).getPath();
					fileNameOut+="/"+cat.get(x).getPath();
				}
				fileName+="/liquitidyFile.txt";
				fileNameOut+="/liquitidyIntervalsFile.txt";
				
				System.out.println(+oddInterval.getIdStart()+" "+oddInterval.getIdEnd()+" "+fileName + " " + recordsInFile(fileName,fileNameOut));
				
				
				
				
			}
			
			
			
			
			
			
		}
		
		
	}
	
}
