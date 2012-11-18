package categories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import statistics.Statistics;
import DataRepository.Utils;

public class ProcessThreshold {

		int catIndex=-1;
		double percentFilter;
		
		double mins[];
		double maxs[];
		
		public static double HISTOGRAM_PRECISION=0.1;
		
		public ProcessThreshold(int catIndexA,double percentFilterA) {
			catIndex=catIndexA;
			percentFilter=percentFilterA;
			
			Category cat=CategoriesManager.getCategory(catIndex);
			
			mins=new double[cat.getNumberInputValues()];
			maxs=new double[cat.getNumberInputValues()];
			
			System.out.println("1st pass to find mins/maxs ...");
			File f=new File(CategoriesManager.getDirectory(catIndex)+"/nn-raw-data.csv");
			BufferedReader input=getBufferedReader(f);
			System.out.println("Log file ("+f.getAbsolutePath()+") in processing...");
			String s=null;
			try {
				s=input.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if(s==null)
			{
				System.out.println("File"+CategoriesManager.getDirectory(catIndex)+"/nn-raw-data.csv is empty");
			}
			
			String [] fields=s.split(",");
			
			for(int i=0;i<cat.getNumberInputValues();i++)
			{
				mins[i]=Double.parseDouble(fields[i]);
				maxs[i]=Double.parseDouble(fields[i]);
			}
			
			try {
				while ((s=input.readLine()) != null)
				{
					fields=s.split(",");
					for(int i=0;i<cat.getNumberInputValues();i++)
					{
						double value=Double.parseDouble(fields[i]);
						if(value>maxs[i])
							maxs[i]=value;
						
						if(value<mins[i])
							mins[i]=value;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				input.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			input=null;
			
			for(int i=0;i<cat.getNumberInputValues();i++)
			{
				System.out.println("Field:"+i+" Max:"+maxs[i]+" Min:"+mins[i]);
			}
			
			System.out.println("1st pass to find mins/maxs complete.");
			//----------------------------------------------------------------
			System.out.println("2nd pass to build histograms...");
			Histogram histograms[]=new Histogram[cat.getNumberInputValues()];
			
			for(int i=0;i<cat.getNumberInputValues();i++)
			{
				histograms[i]=new Histogram(mins[i], maxs[i], HISTOGRAM_PRECISION);
			}
			
			
			f=new File(CategoriesManager.getDirectory(catIndex)+"/nn-raw-data.csv");
			input=getBufferedReader(f);
			System.out.println("Log file ("+f.getAbsolutePath()+") in processing...");
			s=null;
			
			
			try {
				while ((s=input.readLine()) != null)
				{
					fields=s.split(",");
					for(int i=0;i<cat.getNumberInputValues();i++)
					{
						double value=Double.parseDouble(fields[i]);
						histograms[i].addValue(value);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				input.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			input=null;
			
			System.out.println("Histograms build.");
			System.out.println("Redifine mins/max with " +percentFilter+"% filtered values...");
			for(int i=0;i<cat.getNumberInputValues();i++)
			{
				//if(mins[i]<10 && mins[i]>-10)
				//	mins[i]=0;
				//else
					mins[i]=histograms[i].getMinFiltred(percentFilter);
				
				maxs[i]=histograms[i].getMaxFiltred(percentFilter);
				System.out.println("histo size["+i+"] :"+histograms[i].getIntervals().length+" Values"+histograms[i].nvalues );
			}
			
			
			for(int i=0;i<cat.getNumberInputValues();i++)
			{
				System.out.println("Field:"+i+" Max:"+maxs[i]+" Min:"+mins[i]);
			}
			//----------------------------------------------------------------
			/*
			 
			System.out.println("3rd pass Rewrite file to "+CategoriesManeger.getDirectory(cat)+"/nn-raw-data-threshold.csv with "+percentFilter+"% filtered values...");
			
			BufferedWriter out=null;
			try {
				out = new BufferedWriter(new FileWriter(CategoriesManeger.getDirectory(cat)+"/nn-raw-data-threshold.csv", true));
				} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error Open "+CategoriesManeger.getDirectory(cat)+"/nn-raw-data-threshold.csv for writing");
				}
			if(out==null)
			{
				System.err.println("could not open "+CategoriesManeger.getDirectory(cat)+"/nn-raw-data-threshold.csv" );
				return;
			}
			
			
			f=new File(CategoriesManeger.getDirectory(catIndex)+"/nn-raw-data.csv");
			input=getBufferedReader(f);
			System.out.println("Log file ("+f.getAbsolutePath()+") in processing...");
			s=null;
			
			
			try {
				while ((s=input.readLine()) != null)
				{
					fields=s.split(",");
					String sout="";
					for(int i=0;i<cat.getNumberInputValues();i++)
					{
						double value=Double.parseDouble(fields[i]);
						if(value>maxs[i])
							sout=sout+maxs[i]+",";
						else if(value<mins[i])
							sout=sout+maxs[i]+",";
						else
							sout=sout+value+",";
					}
					sout+=fields[cat.getNumberInputValues()];
					
					out.write(sout);
					out.newLine();
					out.flush();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				input.close();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Rewrite file to "+CategoriesManeger.getDirectory(cat)+"/nn-raw-data-threshold.csv with Complete.");
		*/
		}
		
		public BufferedReader getBufferedReader(File f)
		{
			BufferedReader input=null;
			try {
					input= new BufferedReader(new FileReader(f));
			} catch (Exception e) {
					e.printStackTrace();
			} 
			return input;
		}
		
		public static void main(String[] args)  throws Exception {
			
			Utils.init();
			Statistics.init();
			
			CategoriesManager.init();
			CategoriesManager.loadRawAMFromFile();
			CategoriesManager.processAMCatIntervals();

			ProcessThreshold pt=new ProcessThreshold(16, 85.0);
			
		}
}
