package categories.categories2013.scripts;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;

import org.encog.ml.data.specific.CSVNeuralDataSet;
import org.encog.util.simple.EncogUtility;

import GUI.MyChart2D;
import categories.categories2011.Histogram;
import categories.categories2013.CategoryNode;
import categories.categories2013.Root;
import categories.categories2013.bots.DataWindowsSizes;
import demo.util.Display;

public class ProcessNNRawTestData {
	

	
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
	
	public static Vector<double[]> loadMinMaxFileIntoMemory(String fileName)
	{
		
		Vector<double[]> ret=new Vector<double[]>();
		
		File ff=new File(fileName);
		BufferedReader inputFile=getBufferedReader(ff);
		
		if(inputFile == null)
			return null;
		
		String s;
		try {
			for(int line=0;line<DataWindowsSizes.INPUT_NEURONS+2;line++)
			{
				s=inputFile.readLine();
				String saux[]=s.split(" ");
				double dataExample[]=new double[2]; 
				
				for (int i=0;i<2;i++)
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
		//chart.setForceXRange(new Range(50.1, 50));
		chart.setDecimalsX(0);
		frame.add(chart);
		
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
		
		if(points==null) return frame;
		
		for(double point:points)
		{
		
			chart.addValue("interval", point-1, 0, Color.RED);
			chart.addValue("interval", point, 100, Color.RED);
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
				// this is to consider - no-move - small-move - big-move
				//System.out.println(" Max  "+minmax[i+1][0]+"[");
				
				
				//System.out.println(" no move  "+minmax[i+1][0]+"[");
				//System.out.println(" small move  "+minmax[i+1][1]+"[");
			}
			
			Vector<Double> points=new Vector<Double>();
			points.add(minmax[i][0]);
			points.add(minmax[i][1]);
			
			
			
			//if(i==DataWindowsSizes.INPUT_NEURONS)
			{
			//	points.add(minmax[i+1][0]);
			//	points.add(minmax[i+1][1]);
				
			//	points.add(-minmax[i+1][0]);
			//	points.add(-minmax[i+1][1]);
				
				JFrame f=showHistogramChart(histogram, points,""+i);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					Display.getStringAnswer("pause - input : "+i);
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
		}
		
		
		//System.out.println(""+((int)4.9));
		
		return minmax;
	}
	
	public static void deleteFile(String fileName)
	{
		File file = new File(fileName);
		if(file.exists()) { 
			System.out.println("File to be deleted found in  "+fileName);
			if(file.delete()){
    			System.out.println(file.getName() + " is deleted!");
    		}else{
    			System.out.println("Delete operation fail.");
    		}
			System.gc();
		}
		else
		{
			System.out.println("File do be deleted Not found in "+fileName);
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
				lineExample+=value+",";
			}
			lineExample = lineExample.substring(0, lineExample.length() - 1);
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
	
	public static void writeEncogFile(Vector<CategoryNode> cat)
	{
		String encogFileName=CategoryNode.getAncestorsStringPath(cat)+"nn-train-data.egb";
		String normalFileName=CategoryNode.getAncestorsStringPath(cat)+"NNNormalizeData.csv";
		
		deleteFile(encogFileName);
		
		System.out.println("Writing binary Encog training to "+encogFileName);
		File normalizaedCVSFile=new File(normalFileName);
		File targetEGBFile=new File(encogFileName);
		CSVNeuralDataSet csvnds=new CSVNeuralDataSet(normalizaedCVSFile.getAbsolutePath(), DataWindowsSizes.INPUT_NEURONS, 1, false);
		EncogUtility.saveEGB(targetEGBFile, csvnds);
		System.out.println("Write Complete.");
	}

	public static void main(String[] args) {
			
			Root root=new Root(0);
			
			CategoryNode.printIDs(root);
			//CategoryNode.buildDirectories(root);
			
			//int i=36;//203;
			for(int i=0;i<648;i++)
			{
				System.out.println("-----------------------------------------------------");
				System.out.println("--- Processing Category "+i+" ---");
				Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
				String fileName=CategoryNode.getAncestorsStringPath(cat)+"NNRawDataTest.csv";
				String fileNameMinMax=CategoryNode.getAncestorsStringPath(cat)+"NNMinMax.csv";
				
				File file = new File(fileName);
				if(file.exists()) { 
					System.out.println("File found in "+fileName);
					
					System.out.println("Loading "+fileName);
					Vector<double[]> examples =loadFileIntoMemory(fileName);
					System.out.println("Number of examples : "+examples.size() );
					
					
					Vector<double[]> minmaxV =loadMinMaxFileIntoMemory(fileNameMinMax);
					double[][] minmax=new double[minmaxV.size()][2];
					minmaxV.toArray(minmax);
					
					// para deixar no original 
					// writeMinMaxValues(minmax,cat);
					
					Vector<double[]> normalizeExamples=normalize(examples,minmax);
					System.out.println("Normalized - Number of examples : "+normalizeExamples.size() );
					
					// para deixar no original
					writeTalbleFile(normalizeExamples,cat,"NNNTestNormalizeData.csv");
					
					System.gc();
					
					//para deixar no original
					//writeEncogFile(cat);
					
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
