package categories.categories2013.bots;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;

import DataRepository.Utils;
import GUI.MyChart2D;
import categories.categories2011.Histogram;
import categories.categories2013.CategoryNode;
import categories.categories2013.Root;
import demo.util.Display;

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
	
	public static Histogram buildHistogram(Vector<Double> data)
	{
		Histogram ret=null;
		
		if(data==null) return null;
		
		if(data.size()<=0) return null;
		
		double min=data.get(0);
		double max=data.get(0);
		for(double value:data)
		{
			if(value>max)
				max=value;
			
			if(value<min)
				min=value;
		}
		
		double precision=(max-min)/1000;
		
		ret=new Histogram(min, max, precision);
		for(double d:data)
			ret.addValue(d);
		
		return ret;
	}
	
	public static JFrame showHistogramChart(Histogram histogram,Vector<Double> points,String name)
	{
		JFrame frame=new JFrame(name);
		MyChart2D chart=new MyChart2D();
		frame.add(chart);
		
		double axisx=histogram.min;
		for(int i=0;i<histogram.getIntervals().length;i++)
		{
			chart.addValue("histogram", axisx, histogram.getIntervals()[i], Color.BLUE);
			//System.out.println(histogram.getIntervals()[i]);
			axisx+=histogram.precision;
		}
		
		if(points==null) return frame;
		
		for(double point:points)
		{
		
			chart.addValue("interval", point-1, 0, Color.RED);
			chart.addValue("interval", point, 10000, Color.RED);
			chart.addValue("interval", point+1, 0, Color.RED);
		}
		
		frame.setSize(1000, 500);
		frame.setVisible(true);
		
		return frame;
	}
	
	public static double[][] findMinMax(Vector<double[]> examples)
	{
		double minmax[][]=new double[DataWindowsSizes.INPUT_NEURONS+2][2];
		
		
		for(int i=0;i<DataWindowsSizes.INPUT_NEURONS+1;i++)
		{
			Vector<Double> column=new Vector<Double>();
			for(double columns[]:examples)
			{
				column.add(columns[i]);
			}
			
			Histogram histogram=buildHistogram(column);
			
			double filter=90;
			if(i==DataWindowsSizes.INPUT_NEURONS)
				filter=95;
			minmax[i][0]=histogram.getMinFiltred(filter);
			minmax[i][1]=histogram.getMaxFiltred(filter);
			
			if(i==DataWindowsSizes.INPUT_NEURONS)
			{
				
				/// special process to find min max for output train for symmetry in int
				double minOutput=(int)(Math.abs(minmax[DataWindowsSizes.INPUT_NEURONS][0])+0.5);
				if(minmax[DataWindowsSizes.INPUT_NEURONS][1]>minOutput)
				{
					minmax[DataWindowsSizes.INPUT_NEURONS][0]=-minOutput;
					minmax[DataWindowsSizes.INPUT_NEURONS][1]=minOutput;
				}
				else
				{
					minmax[DataWindowsSizes.INPUT_NEURONS][0]=-(int)(minmax[DataWindowsSizes.INPUT_NEURONS][1]+0.5);
					minmax[DataWindowsSizes.INPUT_NEURONS][1]=(int)(minmax[DataWindowsSizes.INPUT_NEURONS][1]+0.5);
				}
				//System.out.println("Output min max :"+minmax[DataWindowsSizes.INPUT_NEURONS][0]+" "+minmax[DataWindowsSizes.INPUT_NEURONS][1]);
				
				
				minmax[i+1][0]=Math.round(histogram.getMaxFiltred(65));
				minmax[i+1][1]=Math.round(histogram.getMaxFiltred(77));
				
				//System.out.println(" Max  "+minmax[i+1][0]+"[");
				
				
				//System.out.println(" no move  "+minmax[i+1][0]+"[");
				//System.out.println(" small move  "+minmax[i+1][1]+"[");
			}
			
			Vector<Double> points=new Vector<Double>();
			points.add(minmax[i][0]);
			points.add(minmax[i][1]);
			
			
//			if(i==DataWindowsSizes.INPUT_NEURONS)
//			{
//				JFrame f=showHistogramChart(histogram, points,""+i);
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				try {
//					Display.getStringAnswer("pause - input : "+i);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				f.setVisible(false);
//				f.dispose();
//				
//			}
			
			/*JFrame f=showHistogramChart(histogram, points,""+i);
			
			try {
				Display.getStringAnswer("pause - input : "+i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			f.dispose();*/
		}
		
		
		//System.out.println(""+((int)4.9));
		
		return minmax;
	}
	
	public static void deleteFile(String fileName)
	{
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
	
	public static void writeMinMaxValues(double[][] minmax,Vector<CategoryNode> cat)
	{
		String fileName=CategoryNode.getAncestorsStringPath(cat)+"NNMinMax.csv";
		
		deleteFile(fileName);
		
		String s="";
		for(int i=0;i<DataWindowsSizes.INPUT_NEURONS+2;i++)
		{
			s+=minmax[i][0]+" "+minmax[i][1]+"\n";
		}
		
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
			out.write(s);
			out.newLine();
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
	
	public static Vector<double[]> normalize(Vector<double[]> examples,double[][] minmax)
	{
		Vector<double[]> ret=new Vector<double[]>();
		
		for(double[] rawExample:examples)
		{
			double normalExample[]=new double[DataWindowsSizes.INPUT_NEURONS+1];
		
			for(int i=0;i<DataWindowsSizes.INPUT_NEURONS+1;i++)
			{	
				if(i==DataWindowsSizes.INPUT_NEURONS)
				{
					
					normalExample[i]=normalizeOutput(rawExample[i],minmax[i+1][0],minmax[i+1][1]);
					//System.out.println("Normalize["+i+"]="+rawExample[i]+" with ("+minmax[i+1][0]+","+minmax[i+1][1]+") = "+normalExample[i]);
				}
				else
				{
					
					normalExample[i]=normalizeValue(rawExample[i], minmax[i][0], minmax[i][1]);
					//System.out.println("Normalize["+i+"]="+rawExample[i]+" with ("+minmax[i][0]+","+minmax[i][1]+") = "+normalExample[i]);
				}
				
				//try {
				//	Display.getStringAnswer("pause - input : "+i);
				//} catch (IOException e) {
				//	// TODO Auto-generated catch block
				//	e.printStackTrace();
				//}
			}
			ret.add(normalExample);
		}
		
		return ret;
	}
	
	public static double normalizeValue(double value,double min,double max)
	{
		double ret=0;
		
		double total=max-min;
		double relative=value-min;
		
		ret=(relative*2)/total;
				
		ret-=1;	
		
		if(ret>1) ret=1.0;
		
		if(ret<-1) ret=-1.0;
		
		return ret;
	}

	public static double normalizeOutput(double value,double min,double max)
	{
		double ret=0;
		if(value>-min && value<min)
			ret=0.0;
		else if(value>=min && value<=max)
			ret=0.5;
		else if(value>max)
			ret=1.0;
		else if(value>=-max && value<=-min)
			ret=-0.5;
		else if(value<-max)
			ret=-1.0;
	
		return ret;
	}
	
	public static void writeTalbleFile(Vector<double[]> examples,Vector<CategoryNode> cat,String fileNameA)
	{
		String fileName=CategoryNode.getAncestorsStringPath(cat)+fileNameA;
		
		System.out.println("Writting data to file - "+fileName);
		
		deleteFile(fileName);
		
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
			for(double value:ex)
			{
				lineExample+=value+" ";
			}
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
					
					double minmax[][]=findMinMax(collectExamples);
					
					writeMinMaxValues(minmax,cat);
					
					Vector<double[]> normalizeExamples=normalize(collectExamples,minmax);
					System.out.println("Normalized - Number of examples : "+normalizeExamples.size() );
					
					writeTalbleFile(normalizeExamples,cat,"NNNormalizeData.csv");
					
					System.gc();
					
					
				}
				else
				{
					System.out.println("File Not found in "+fileName);
				}
				
			}
			
			//System.out.println(normalizeValue(-89, -100, 100));
			//System.out.println(normalizeOutput(-2.000000000000341, -55.12600000000009, 65.21000000000004));
		}

}
