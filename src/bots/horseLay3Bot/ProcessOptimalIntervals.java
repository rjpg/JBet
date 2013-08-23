package bots.horseLay3Bot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import demo.util.Display;

public class ProcessOptimalIntervals {
	public double[][] data;
	public int numberOfFields=0;
	
	public double min[];
	public double max[];
	
	public double currentMin[];
	public double currentMax[];
	
	public double step[];
	
	public double possibleValues[][];
	
	public double combinationOfPossibleValues[][];
	
	public int precision=10;
	
	public String fileName="HorseLay3BotAbove6.txt";
	
	public ProcessOptimalIntervals(String fname) {
		fileName=fname;
		readFile();
	}
	
	public void readFile()
	{
		File f=new File(fileName);
		BufferedReader input=null;
		try {
			input= new BufferedReader(new FileReader(f));
		} catch (Exception e) {
			e.printStackTrace();
		} 

		if(input==null)
		{
			System.err.println("File ("+f.getAbsolutePath()+") not processed");
			return;
		}
		
		Vector<Double[]> line=new Vector<Double[]>();
		String s;
	
		try {
			int h=0;
			while ((s=input.readLine()) != null)
			{
				
				String []aux=s.split(" ");
				Double[] fieldLine=new Double[aux.length];
				for(int i=0;i<aux.length;i++)
				{
					fieldLine[i]=Double.parseDouble(aux[i]);
				}
				line.add(fieldLine);
				System.out.println("h="+h++);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		min=new double [line.get(0).length];
		max=new double [line.get(0).length];
		
		currentMin=new double [line.get(0).length];
		currentMax=new double [line.get(0).length];
		
		for(int i=0;i<line.get(0).length;i++)
		{
			min[i]=line.get(0)[i];
			max[i]=line.get(0)[i];
			
			currentMin[i]=line.get(0)[i];
			currentMax[i]=line.get(0)[i];
		}
		
		step=new double [line.get(0).length];
		
		data=new double[line.size()][line.get(0).length];
		for(int i=0;i<line.size();i++)
		{
			for(int x=0;x<line.get(i).length;x++)
			{
				data[i][x]=line.get(i)[x];
				
				if(data[i][x]<min[x])
				{
					min[x]=data[i][x];
				}
				
				if(data[i][x]>max[x])
				{
					max[x]=data[i][x];
				}
			}
		}
		
		for(int i=0;i<data.length;i++)
		{
			System.out.print(i+" : ");
			for(int x=0;x<data[i].length;x++)
			{
				System.out.print(data[i][x]+" ");
			}
			System.out.println();
		}
		
		for(int i=0;i<min.length;i++)
		{
			//System.out.println("min["+i+"]="+min[i]+"  max["+i+"]="+max[i]);
			currentMin[i]=min[i];
			currentMax[i]=max[i];
			
			step[i]=(max[i]-min[i])/(double)(precision-1);
			
			System.out.println("min["+i+"]="+min[i]+"  max["+i+"]="+max[i]+"  step["+i+"]="+step[i]);
			
		}
		
		numberOfFields=data[0].length-1;

		
		possibleValues=new double[numberOfFields][precision];
		
		for(int i=0;i<numberOfFields;i++)
			possibleValues[i][0]=min[i+1];
		
		for(int i=0;i<numberOfFields;i++)
		{
			for(int x=1;x<precision;x++)
			{
				possibleValues[i][x]=possibleValues[i][x-1]+step[i+1];
			}
			
		}
		
		for(int i=0;i<numberOfFields;i++)
		{
			for(int x=0;x<precision;x++)
			{
				System.out.print(possibleValues[i][x]+" ");
			}
			System.out.println();
		}
		
		combinationOfPossibleValues=new double[(int)Math.pow(precision, numberOfFields)][numberOfFields];
		
		for(int i=0;i<combinationOfPossibleValues.length;i++)
		{
			//int x=0;
			for(int x=0;x< numberOfFields;x++)
			{
				int index=(i/(int)Math.pow(precision,x))%(int)(precision);
				combinationOfPossibleValues[i][x]=
					possibleValues[x][index];
				//System.out.print(combinationOfPossibleValues[i][x]+" ");
			}
			//System.out.println();
				
		}
		
		for(int x=0;x< numberOfFields;x++)
		{
			System.out.print(combinationOfPossibleValues[0][x]+" ");
		}
		System.out.println();
		
		System.out.println("data.lenght = "+data.length + " current pl ="+calculatePL(0,combinationOfPossibleValues.length-1)+ " numberOfFields="+numberOfFields);
		
		 printCurrentMinMax();
		System.out.println(combinationOfPossibleValues.length); 
		try {
			input.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		computeMaxPL();
		
	}
	
	public void computeMaxPL()
	{
		double max=calculatePL(0,combinationOfPossibleValues.length-1);
		
		
		for(int i=0;i<combinationOfPossibleValues.length;i++)
		{
			for(int x=0;x<combinationOfPossibleValues.length;x++)
			{
				double aux=calculatePL(i,x);
				if(aux>max)
				{
					System.out.println("******NEW MAX FOUND****************");
					max=aux;
					System.out.println(" NEW PL :"+max);
					System.out.print("Mins :");
					for(int h=0;h< numberOfFields;h++)
					{
						System.out.print(combinationOfPossibleValues[i][h]+" ");
					}
					System.out.println();
					
					System.out.print("Maxs :");
					for(int h=0;h< numberOfFields;h++)
					{
						System.out.print(combinationOfPossibleValues[x][h]+" ");
					}
					System.out.println();
					
				
				}
			}
		}
			
		
		
	}
	
	
	public double calculatePL(int start,int end)
	{
		double pl=0;
		for(int x=0;x< numberOfFields;x++)
		{
			currentMin[x+1]=combinationOfPossibleValues[start][x];
			currentMax[x+1]=combinationOfPossibleValues[end][x];
		}
		
		for(int i=0;i<data.length;i++)
		{
			boolean passed=true;
			for(int x=1;x<data[i].length;x++)
			{
				//System.out.println("data["+i+"]["+x+"]="+data[i][x]+">=currentMin["+x+"]="+currentMin[x]+" && data["+i+"]["+x+"]="+data[i][x]+"<=currentMax["+x+"]="+currentMax[x]);
//				try {
//					Display.getStringAnswer("pause");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					
//				}
				if(data[i][x]<currentMin[x] || data[i][x]>currentMax[x])
				{
					//System.out.println("not passed");
					passed=false;
				}
				//else
				//	System.out.println("passed");
			}
			
			if(passed)
				pl+=data[i][0];
		}
		return pl;
	}
	
	public void printCurrentMinMax()
	{
		System.out.print("current Mins :");
		for(int h=0;h< numberOfFields;h++)
		{
			System.out.print(currentMin[h+1]+" ");
		}
		System.out.println();
		
		System.out.print("current Maxs :");
		for(int h=0;h< numberOfFields;h++)
		{
			System.out.print(currentMax[h+1]+" ");
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		System.out.println("arg "+args[0]);
		//"HorseLay4.0BotAbove6.0.txt"
		new ProcessOptimalIntervals(args[0]);
	}
}
