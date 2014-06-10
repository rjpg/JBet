package categories.categories2013.bots;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;

import GUI.MyChart2D;

import categories.categories2011.Histogram;
import categories.categories2013.CategoryNode;
import categories.categories2013.Root;
import demo.util.Display;

public class ProcessNNRawData {
	
	public static int[][] toNominalTable={
		{0,0,0}, //0
		{0,1,1}, //1
		{0,1,2}, //2
		{0,1,3}, //3
		{0,1,3}, //4
		{0,2,4}, //5
		{0,2,5}, //6
		{}, //7
		{}, //8
		{}, //9
		{}, //10
		{}, //11
		{}, //12
		{}, //13
		{}, //14
		{}, //15
		{}, //16
		{}, //17
		{}, //18
		{}, //19
		{}, //20
		};
	
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
		
		frame.setSize(1000, 1000);
		frame.setVisible(true);
		
		return frame;
	}
	
	public static double[][] findMinMax(Vector<double[]> examples)
	{
		double minmax[][]=new double[DataWindowsSizes.INPUT_NEURONS+1][2];
		
		
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
			
			Vector<Double> points=new Vector<Double>();
			points.add(minmax[i][0]);
			points.add(minmax[i][1]);
			
			
			if(i==DataWindowsSizes.INPUT_NEURONS)
			{
				JFrame f=showHistogramChart(histogram, points,""+i);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				f.setVisible(false);
				f.dispose();
				
			}
			
			
			/*
			try {
				Display.getStringAnswer("pause - input : "+i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			f.dispose();*/
		}
		
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
		System.out.println("Output min max :"+minmax[DataWindowsSizes.INPUT_NEURONS][0]+" "+minmax[DataWindowsSizes.INPUT_NEURONS][1]);
		
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
	
	public static void wtriteMinMaxValues(double[][] minmax,Vector<CategoryNode> cat)
	{
		String fileName=CategoryNode.getAncestorsStringPath(cat)+"NNMinMax.csv";
		for(int i=0;i<DataWindowsSizes.INPUT_NEURONS+1;i++)
		{
			
		}
	}
	
	public static void main(String[] args) {
			
			Root root=new Root(0);
			
			CategoryNode.printIDs(root);
			//CategoryNode.buildDirectories(root);
			
			//int i=203;
			for(int i=0;i<648;i++)
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
					
					wtriteMinMaxValues(minmax,cat);
					
					System.gc();
				}
				else
				{
					System.out.println("File Not found in "+fileName);
				}
				
			}
			
			
		}

}
