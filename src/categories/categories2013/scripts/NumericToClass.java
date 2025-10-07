package categories.categories2013.scripts;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;

import GUI.MyChart2D;
import categories.categories2011.Histogram;
import categories.categories2013.bots.DataWindowsSizes;
import demo.util.Display;

public class NumericToClass {

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
		
		System.out.println("min : "+min);
		System.out.println("max : "+max);
		
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
		//chart.setForceXRange(new Range(50.1, 50));
		chart.setDecimalsX(0);
		frame.add(chart);
		
		if(points!=null)
			for(double point:points)
			{
			
				chart.addValue("interval", (point)-0.0001, 0, Color.RED);
				chart.addValue("interval", (point), 100, Color.RED);
				chart.addValue("interval", (point)+0.0001, 0, Color.RED);
			}
		
		
		double axisx=histogram.min-0.5;
		int value =histogram.getIntervals()[0];
		for(int i=0;i<histogram.getIntervals().length;i++)
		{
			
			if(histogram.getIntervals()[i]!=0)
			{
				value =histogram.getIntervals()[i];
				//System.out.println("Value to chart : "+histogram.getIntervals()[i]);
				
				//System.out.println(histogram.getIntervals()[i]);
				
			}
			chart.addValue("histogram", axisx,value, Color.BLUE);
			axisx+=histogram.precision;
		}
		
		
		frame.setSize(1000, 500);
		
	
		frame.setVisible(true);
		
		return frame;
	}

	
	public static double[][] findSegments(Vector<Double> examples,int classes)
	{
		double minmax[][]=new double[classes][2];
		
		Histogram histogram=buildHistogram(examples);
		
		double segmentSizePercent=100/classes;
		double segmentSizePercentAcc=0;
		
		Vector<Double> points=new Vector<Double>();
		//points.add(0d);
		//points.add(histogram.getMaxFiltred(10));
		for(int i=0;i<classes;i++)
		{
			System.out.println(" Segment: " +i);
			minmax[i][0]=histogram.getMaxFiltred(segmentSizePercentAcc);
			segmentSizePercentAcc+=segmentSizePercent;
			System.out.println(" Segment: " +segmentSizePercentAcc);
			minmax[i][1]=histogram.getMaxFiltred(segmentSizePercentAcc);
			points.add(minmax[i][1]);
			
		}
		
		/*
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
				// this is to consider - no-move - small-move - big-move
				//System.out.println(" Max  "+minmax[i+1][0]+"[");
				
				
				//System.out.println(" no move  "+minmax[i+1][0]+"[");
				//System.out.println(" small move  "+minmax[i+1][1]+"[");
			}
			
			Vector<Double> points=new Vector<Double>();
			points.add(minmax[i][0]);
			points.add(minmax[i][1]);
			*/
			
			//if(i==DataWindowsSizes.INPUT_NEURONS)
			{
			//	points.add(minmax[i+1][0]);
			//	points.add(minmax[i+1][1]);
				
			//	points.add(-minmax[i+1][0]);
			//	points.add(-minmax[i+1][1]);
				
				
				JFrame f=showHistogramChart(histogram, points,"classes");
				f.setSize(800, 6000);
				f.setVisible(true);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					Display.getStringAnswer("pause - input : ");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				f.setVisible(false);
				f.dispose();
				
				
			}
			
			
//			JFrame f=showHistogramChart(histogram, points,""+i);
//			
//			try {
//				Display.getStringAnswer("pause - input : "+i);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			f.dispose();
	//	}
		
		
		//System.out.println(""+((int)4.9));
		
		return minmax;
	}
	
	public static Vector<Double> loadFileIntoMemory(String fileName)
	{
		
		Vector<Double> ret=new Vector<Double>();
		
		File ff=new File(fileName);
		BufferedReader inputFile=getBufferedReader(ff);
		
		if(inputFile == null)
			return null;
		
		String s;
		try {
			while ((s=inputFile.readLine()) != null)
			{
				//String saux[]=s.split(" ");
				double data=Double.parseDouble(s)*100; 
				
				//for (int i=0;i<DataWindowsSizes.INPUT_NEURONS+1;i++)
				//	dataExample[i]=Double.parseDouble(saux[i]);
				
				//System.out.println(fileName);
				//System.out.println(dataExample[35]);
				
				ret.add(data);

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
	
	public static void main(String[] args) {
		
		int nClasses=5;
		
		String fileName="foo.csv";
		Vector<Double> examples =loadFileIntoMemory(fileName);
		double[][] intevals=findSegments(examples,nClasses);
		for(int i=0; i<nClasses;i++)
			System.out.println("Class "+i+" start :"+intevals[i][0]/100+" end :"+intevals[i][1]/100);
	}	
}
