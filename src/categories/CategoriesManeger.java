package categories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import org.encog.app.analyst.EncogAnalyst;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;

import main.Parameters;

import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class CategoriesManeger {
	
	public static double[] oddCat={2.00,5.00,10.00,15.00};
	
	public static int AVGAmountFrames=60;
	
	public static Vector<Double> AM_t10_fav[] =new Vector[oddCat.length];
	public static Vector<Double> AM_t10[] =new Vector[oddCat.length];
	
	public static Vector<Double> AM_t2_fav[] =new Vector[oddCat.length];
	public static Vector<Double> AM_t2[] =new Vector[oddCat.length];
	
	// [odd cat] [pouco-med-muito] [inicio-fim] 
	public static double intervals_AM_t10_fav[][][] =new double[oddCat.length][3][2];
	public static double intervals_AM_t10[][][] =new double[oddCat.length][3][2];
	
	public static double intervals_AM_t2_fav[][][] =new double[oddCat.length][3][2];
	public static double intervals_AM_t2[][][] =new double[oddCat.length][3][2];
	
	// directories 
	public static String directories[]=new String[48];
	public static Category categories[]=new Category[48];
	
	// write data
	public static BufferedWriter out=null;
	
	public static void writeTrainDataIntoCSVCat(Category cat,double input[],double output)
	{
		try {
			out = new BufferedWriter(new FileWriter(CategoriesManeger.getDirectory(cat)+"/nn-raw-data.csv", true));
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open "+CategoriesManeger.getDirectory(cat)+"/nn-raw-data.csv for writing");
			}
		if(out==null)
		{
			System.err.println("could not open "+CategoriesManeger.getDirectory(cat)+"/nn-raw-data.csv" );
			return;
		}
		//System.out.println("File : "+CategoriesManeger.getDirectory(cat)+"/nn-raw-data.csv");
		String s="";
		
		//if(cat.getNumberInputValues()!=input.length)
		//	System.err.println("cat.getNumberInputValues()!=input.length");
		//else
		//	System.err.println("cat.getNumberInputValues()!=input.length --- OK");
		
		//System.out.println("Cat inpur : "+cat.getNumberInputValues()+ "  lenght : "+ input.length);
		
		for(int i=0;i<cat.getNumberInputValues();i++)
			s=s+input[i]+",";
			
		s=s+output;
		
		//System.err.println(s);
		 
		try {
			out.write(s);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			System.out.println("SaveFav:Error wrtting data to log file");
			e.printStackTrace();
		}
		
		
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void init()
	{
		
		for(int i=0;i<oddCat.length;i++)
		{
			AM_t10_fav[i] =new Vector<Double>();
			AM_t10[i] =new Vector<Double>();
			
			AM_t2_fav[i] =new Vector<Double>();
			AM_t2[i] = new Vector<Double>();
		}
		
		int x=0;
		for(int i=0;i<oddCat.length;i++)
		{
			categories[x]=new Category(true, i, true, 0);
			directories[x++]="10m/odd-"+i+"/fav-true/Am-Low";
			categories[x]=new Category(true, i, true, 1);
			directories[x++]="10m/odd-"+i+"/fav-true/Am-med";
			categories[x]=new Category(true, i, true, 2);
			directories[x++]="10m/odd-"+i+"/fav-true/Am-hi";
						
			categories[x]=new Category(true, i, false, 0);
			directories[x++]="10m/odd-"+i+"/fav-false/Am-Low";
			categories[x]=new Category(true, i, false, 1);
			directories[x++]="10m/odd-"+i+"/fav-false/Am-med";
			categories[x]=new Category(true, i, false, 2);
			directories[x++]="10m/odd-"+i+"/fav-false/Am-hi";
			
			categories[x]=new Category(false, i, true, 0);
			directories[x++]="2m/odd-"+i+"/fav-true/Am-Low";
			categories[x]=new Category(false, i, true, 1);
			directories[x++]="2m/odd-"+i+"/fav-true/Am-med";
			categories[x]=new Category(false, i, true, 2);
			directories[x++]="2m/odd-"+i+"/fav-true/Am-hi";
			
			categories[x]=new Category(false, i, false, 0);
			directories[x++]="2m/odd-"+i+"/fav-false/Am-Low";
			categories[x]=new Category(false, i, false, 1);
			directories[x++]="2m/odd-"+i+"/fav-false/Am-med";
			categories[x]=new Category(false, i, false, 2);
			directories[x++]="2m/odd-"+i+"/fav-false/Am-hi";
		}
		
		for(Category cat:categories)
		{
			if(cat.getOddCat()==0)
			{
				cat.setWindowSize(30);
				cat.setAxisSize(4);
				cat.setAhead(30);
				cat.setAheadOffset(10);
				cat.setAxisSizeAmounts(5);	
				cat.setAxisSizeVolume(5);
				cat.setAxisSizeVolumeDiff(4);
			}
			
			if(cat.getOddCat()==1)
			{
				cat.setWindowSize(30);
				cat.setAxisSize(4);
				cat.setAhead(35);
				cat.setAheadOffset(10);
				cat.setAxisSizeAmounts(5);	
				cat.setAxisSizeVolume(5);
				cat.setAxisSizeVolumeDiff(4);
			}
			
			if(cat.getOddCat()==2)
			{
				cat.setWindowSize(40);
				cat.setAxisSize(3);
				cat.setAhead(40);
				cat.setAheadOffset(15);
				cat.setAxisSizeAmounts(5);	
				cat.setAxisSizeVolume(5);
				cat.setAxisSizeVolumeDiff(3);
			}
				
			if(cat.getOddCat()==3)
			{
				cat.setWindowSize(60);
				cat.setAxisSize(2);
				cat.setAhead(50);
				cat.setAheadOffset(20);
				cat.setAxisSizeAmounts(5);	
				cat.setAxisSizeVolume(5);	
				cat.setAxisSizeVolumeDiff(2);
			}
		}
	}
	
	public static boolean isRaceFavorite(MarketData md)
	{
		RunnersData rdf = Utils.getFavorite(md);
		if(Utils.getOddBackFrame(rdf, 0)<Parameters.ODD_FAVORITE)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static Category getCategory(int x)
	{
		return categories[x];
	}
	
	public static Category getCategory(RunnersData rd)
	{
		if(rd==null)
			return null;
		int oddCat=getOddCat(rd);
		if(oddCat==-1)
			return null;
			
		
		MarketData md=rd.getMarketData();
		
		long nowMin=md.getCurrentTime().getTimeInMillis();
		long startMin=md.getStart().getTimeInMillis();
		long sub=startMin-nowMin;
	
		int minute=(int)(sub/60000);
		
		if(minute>8 && minute<=9 )
		{
			double amtAvg=Utils.getMatchedAmountAVG(rd, CategoriesManeger.AVGAmountFrames, 0);
			if(amtAvg>0)
			{	
				boolean fav=isRaceFavorite(md);
				if(fav)
				{
					// [odd cat] [pouco-med-muito] [inicio-fim] 
					if(amtAvg<intervals_AM_t10_fav[oddCat][1][0])
						return categories[getNumber(true,oddCat, fav, 0)];
					else if(amtAvg>intervals_AM_t10_fav[oddCat][1][0] && amtAvg<intervals_AM_t10_fav[oddCat][1][1])
						return categories[getNumber(true,oddCat, fav, 1)];
					else if(amtAvg>intervals_AM_t10_fav[oddCat][1][1])
						return categories[getNumber(true,oddCat, fav, 2)];
						
						
				}
				else
				{
					if(amtAvg<intervals_AM_t10[oddCat][1][0])
						return categories[getNumber(true,oddCat, fav, 0)];
					else if(amtAvg>intervals_AM_t10[oddCat][1][0] && amtAvg<intervals_AM_t10_fav[oddCat][1][1])
						return categories[getNumber(true,oddCat, fav, 1)];
					else if(amtAvg>intervals_AM_t10[oddCat][1][1])
						return categories[getNumber(true,oddCat, fav, 2)];
				}
			}
			else
				return null;
		}
		
		
		if(minute>1 && minute<=2 )
		{
			double amtAvg=Utils.getMatchedAmountAVG(rd, CategoriesManeger.AVGAmountFrames, 0);
			if(amtAvg>0)
			{	
				boolean fav=isRaceFavorite(md);
				if(fav)
				{
					// [odd cat] [pouco-med-muito] [inicio-fim] 
					if(amtAvg<intervals_AM_t2_fav[oddCat][1][0])
						return categories[getNumber(false,oddCat, fav, 0)];
					else if(amtAvg>intervals_AM_t2_fav[oddCat][1][0] && amtAvg<intervals_AM_t2_fav[getOddCat(rd)][1][1])
						return categories[getNumber(false,oddCat, fav, 1)];
					else if(amtAvg>intervals_AM_t2_fav[oddCat][1][1])
						return categories[getNumber(false,oddCat, fav, 2)];
						
						
				}
				else
				{
					if(amtAvg<intervals_AM_t2[oddCat][1][0])
						return categories[getNumber(false,oddCat, fav, 0)];
					else if(amtAvg>intervals_AM_t2[oddCat][1][0] && amtAvg<intervals_AM_t2[getOddCat(rd)][1][1])
						return categories[getNumber(false,oddCat, fav, 1)];
					else if(amtAvg>intervals_AM_t2[oddCat][1][1])
						return categories[getNumber(false,oddCat, fav, 2)];
				}
				
			}
			else
				return null;
		}
		
		return null;
	}
	
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
	
	public static void loadRawAMFromFile()
	{
		File f=new File("10m.txt");
		BufferedReader input=getBufferedReader(f);
		System.out.println("AM file ("+f.getAbsolutePath()+") reading...");
		String s;
		try {
			while ((s=input.readLine()) != null)
			{
				
				String[] split=s.split(" ");
				int oddcat=Integer.parseInt(split[1]);
				boolean fav=Boolean.parseBoolean(split[2]);
				double am=Double.parseDouble(split[3]);
				
				//System.out.println("Fav: "+fav);
				if(fav)
				{
					AM_t10_fav[oddcat].add(am);
				}
				else
				{
					AM_t10[oddcat].add(am);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			input.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		System.out.println("AM file ("+f.getAbsolutePath()+") END");
		
		f=new File("2m.txt");
		input=getBufferedReader(f);
		System.out.println("AM file ("+f.getAbsolutePath()+") reading...");
		try {
			while ((s=input.readLine()) != null)
			{
				
				String[] split=s.split(" ");
				int oddcat=Integer.parseInt(split[1]);
				boolean fav=Boolean.parseBoolean(split[2]);
				double am=Double.parseDouble(split[3]);
				
				if(fav)
				{
					AM_t2_fav[oddcat].add(am);
				}
				else
				{
					AM_t2[oddcat].add(am);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			input.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		System.out.println("AM file ("+f.getAbsolutePath()+") END");
		
	}
	
	
	public static void processAMCatIntervals()
	{
		System.out.println("Processing Amounts for categories...");
		for(int i=0;i<AM_t10_fav.length;i++)
		{
			Vector<Double> vm=AM_t10_fav[i];
			double min=vm.get(0);
			double max=vm.get(0);
			
			
			/*for(double d:vm)
			{
				if(d<min)
					min=d;
				if(d>max)
					max=d;
			}
			
			double dif=max-min;
			double step=dif/3;
			
			double start=min;
			double end=min+step;
			
			intervals_AM_t10_fav[i][0][0]=start;
			intervals_AM_t10_fav[i][0][1]=end;
			
			start=end;
			end+=step;
			
			intervals_AM_t10_fav[i][1][0]=start;
			intervals_AM_t10_fav[i][1][1]=end;
			
			start=end;
			end+=step;
			
			intervals_AM_t10_fav[i][2][0]=start;
			intervals_AM_t10_fav[i][2][1]=end;
			*/
			
			if(vm.size()>0)
			{
				Collections.sort(vm);
				int step=vm.size()/3;
				intervals_AM_t10_fav[i][0][0]=vm.get(0);
				intervals_AM_t10_fav[i][0][1]=vm.get(step);
				
				intervals_AM_t10_fav[i][1][0]=vm.get(step);
				intervals_AM_t10_fav[i][1][1]=vm.get(step*2);
				
				intervals_AM_t10_fav[i][2][0]=vm.get(step*2);
				intervals_AM_t10_fav[i][2][1]=vm.get(vm.size()-1);
			}
		}
		
		
		for(int i=0;i<AM_t2_fav.length;i++)
		{
			Vector<Double> vm=AM_t2_fav[i];
			
			/*double min=vm.get(0);
			double max=vm.get(0);
			
			for(double d:vm)
			{
				if(d<min)
					min=d;
				if(d>max)
					max=d;
			}
			
			double dif=max-min;
			double step=dif/3;
			
			double start=min;
			double end=min+step;
			
			intervals_AM_t2_fav[i][0][0]=start;
			intervals_AM_t2_fav[i][0][1]=end;
			
			start=end;
			end+=step;
			
			intervals_AM_t2_fav[i][1][0]=start;
			intervals_AM_t2_fav[i][1][1]=end;
			
			start=end;
			end+=step;
			
			intervals_AM_t2_fav[i][2][0]=start;
			intervals_AM_t2_fav[i][2][1]=end;
			*/
			
			
			if(vm.size()>0)
			{
				Collections.sort(vm);
				int step=vm.size()/3;
				intervals_AM_t2_fav[i][0][0]=vm.get(0);
				intervals_AM_t2_fav[i][0][1]=vm.get(step);
				
				intervals_AM_t2_fav[i][1][0]=vm.get(step);
				intervals_AM_t2_fav[i][1][1]=vm.get(step*2);
				
				intervals_AM_t2_fav[i][2][0]=vm.get(step*2);
				intervals_AM_t2_fav[i][2][1]=vm.get(vm.size()-1);
			}
			
		}
		
		
		for(int i=0;i<AM_t2.length;i++)
		{
			Vector<Double> vm=AM_t2[i];
			/*if(vm.size()>0)
			{
				double min=vm.get(0);
				double max=vm.get(0);
				
				for(double d:vm)
				{
					if(d<min)
						min=d;
					if(d>max)
						max=d;
				}
				
				double dif=max-min;
				double step=dif/3;
				
				double start=min;
				double end=min+step;
				
				intervals_AM_t2[i][0][0]=start;
				intervals_AM_t2[i][0][1]=end;
				
				start=end;
				end+=step;
				
				intervals_AM_t2[i][1][0]=start;
				intervals_AM_t2[i][1][1]=end;
				
				start=end;
				end+=step;
				
				intervals_AM_t2[i][2][0]=start;
				intervals_AM_t2[i][2][1]=end;
			}
			*/
			if(vm.size()>0)
			{
				Collections.sort(vm);
				int step=vm.size()/3;
				intervals_AM_t2[i][0][0]=vm.get(0);
				intervals_AM_t2[i][0][1]=vm.get(step);
				
				intervals_AM_t2[i][1][0]=vm.get(step);
				intervals_AM_t2[i][1][1]=vm.get(step*2);
				
				intervals_AM_t2[i][2][0]=vm.get(step*2);
				intervals_AM_t2[i][2][1]=vm.get(vm.size()-1);
			}
		}
		
		for(int i=0;i<AM_t10.length;i++)
		{
			Vector<Double> vm=AM_t10[i];
			/*if(vm.size()>0)
			{
				double min=vm.get(0);
				double max=vm.get(0);
			
			
				for(double d:vm)
				{
					if(d<min)
						min=d;
					if(d>max)
						max=d;
				}
				
				double dif=max-min;
				double step=dif/3;
				
				double start=min;
				double end=min+step;
				
				intervals_AM_t10[i][0][0]=start;
				intervals_AM_t10[i][0][1]=end;
				
				start=end;
				end+=step;
				
				intervals_AM_t10[i][1][0]=start;
				intervals_AM_t10[i][1][1]=end;
				
				start=end;
				end+=step;
				
				intervals_AM_t10[i][2][0]=start;
				intervals_AM_t10[i][2][1]=end;
			}
			*/
			if(vm.size()>0)
			{
				Collections.sort(vm);
				int step=vm.size()/3;
				intervals_AM_t10[i][0][0]=vm.get(0);
				intervals_AM_t10[i][0][1]=vm.get(step);
				
				intervals_AM_t10[i][1][0]=vm.get(step);
				intervals_AM_t10[i][1][1]=vm.get(step*2);
				
				intervals_AM_t10[i][2][0]=vm.get(step*2);
				intervals_AM_t10[i][2][1]=vm.get(vm.size()-1);
			}
		}
		System.out.println("Amounts for categories Processed");
		
	}
	
	
	
	public static int getOddCat(RunnersData rd)
	{
		if(Utils.getOddBackFrame(rd, 0)<oddCat[0])
			return 0;
		for(int i=1;i<oddCat.length;i++)
		{
			if(Utils.getOddBackFrame(rd, 0)>=oddCat[i-1] && Utils.getOddBackFrame(rd, 0)<oddCat[i])
				return i;
		}
		
		return -1;
	}
	
	public static String getDirectory(Category cat)
	{
		int n=getNumber(cat);
		if(n==-1)
			return null;
		else
			return directories[n];
	}
	
	
	public static int getNumber(boolean t10m,int oddCat,boolean fav,int amCat)
	{
		
		for(int i=0;i<categories.length;i++)
		{
			if(categories[i].isTime10m()==t10m &&
					categories[i].getOddCat()==oddCat &&
					categories[i].isFavorite()==fav &&
					categories[i].getAm()==amCat)
				return i;
		}
		
		return -1;
	}
	
	public static int getNumber(Category cat)
	{
		return getNumber(cat.isTime10m(),cat.getOddCat(),cat.isFavorite(),cat.getAm());
	}
	
	public static BasicNetwork getNN(Category cat)
	{
		BasicNetwork ret=null;
		File networkFile=new File(CategoriesManeger.getDirectory(cat),"nn.eg");
		if (!networkFile.exists())
			return null;
		
		ret=(BasicNetwork) EncogDirectoryPersistence.loadObject(networkFile);
		
		return ret;
	}
	
	
	public static EncogAnalyst  getStats(Category cat)
	{
		EncogAnalyst analyst=null;
		File statsFile=new File(CategoriesManeger.getDirectory(cat)+"/nn-stats.ega");
		
		
		if (!statsFile.exists())
			return null;
		
		analyst = new EncogAnalyst();
		analyst.load(statsFile);
		
		
		return analyst;
	}
	
	
	
	
	public static void printCategory(Category cat)
	{
		System.out.println("id:"+CategoriesManeger.getNumber(cat)+" Time:"+cat.isTime10m()+" Odd cat:"+cat.getOddCat()+" Fav:"+cat.isFavorite()+" Am Cat:"+cat.getAm());
	}
	
	public static int getCategoriesSize()
	{
		return directories.length;
	}
	
	public static String getDirectory(int x)
	{
		return directories[x];
	}
	
	public static void main(String[] args)
	{
		Utils.init();
		CategoriesManeger.init();
		CategoriesManeger.loadRawAMFromFile();
		CategoriesManeger.processAMCatIntervals();
		
		int x=0;
		for(int i=0;i<oddCat.length;i++)
		{
			//printCategory(categories[x]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 10m | Odd cat: "+i+" | fav: true  | Am: Low [ "+ intervals_AM_t10_fav[i][0][0]+" : "+ intervals_AM_t10_fav[i][0][1]+" ]" );
			//printCategory(categories[x]);
			//System.out.println(directories[CategoriesManeger.getNumber(categories[x])]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 10m | Odd cat: "+i+" | fav: true  | Am: Med [ "+ intervals_AM_t10_fav[i][1][0]+" : "+ intervals_AM_t10_fav[i][1][1]+" ]" );
			//printCategory(categories[x]);
			//System.out.println(directories[CategoriesManeger.getNumber(categories[x])]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 10m | Odd cat: "+i+" | fav: true  | Am: Hi  [ "+ intervals_AM_t10_fav[i][2][0]+" : "+ intervals_AM_t10_fav[i][2][1]+" ]" );
			System.out.println("");
			//printCategory(categories[x]);
			//System.out.println(directories[CategoriesManeger.getNumber(categories[x])]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 10m | Odd cat: "+i+" | fav: false | Am: Low [ "+ intervals_AM_t10[i][0][0]+" : "+ intervals_AM_t10[i][0][1]+" ]" );
			//printCategory(categories[x]);
			//System.out.println(directories[CategoriesManeger.getNumber(categories[x])]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 10m | Odd cat: "+i+" | fav: false | Am: Med [ "+ intervals_AM_t10[i][1][0]+" : "+ intervals_AM_t10[i][1][1]+" ]" );
			//printCategory(categories[x]);
			//System.out.println(directories[CategoriesManeger.getNumber(categories[x])]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 10m | Odd cat: "+i+" | fav: false | Am: Hi  [ "+ intervals_AM_t10[i][2][0]+" : "+ intervals_AM_t10[i][2][1]+" ]" );
			System.out.println("");
			//printCategory(categories[x]);
			//System.out.println(directories[CategoriesManeger.getNumber(categories[x])]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 2m | Odd cat: "+i+" | fav: true  | Am: Low [ "+ intervals_AM_t2_fav[i][0][0]+" : "+ intervals_AM_t2_fav[i][0][1]+" ]" );
			//printCategory(categories[x]);
			//System.out.println(directories[CategoriesManeger.getNumber(categories[x])]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 2m | Odd cat: "+i+" | fav: true  | Am: Med [ "+ intervals_AM_t2_fav[i][1][0]+" : "+ intervals_AM_t2_fav[i][1][1]+" ]" );
			//printCategory(categories[x]);
			//System.out.println(directories[CategoriesManeger.getNumber(categories[x])]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 2m | Odd cat: "+i+" | fav: true  | Am: Hi  [ "+ intervals_AM_t2_fav[i][2][0]+" : "+ intervals_AM_t2_fav[i][2][1]+" ]" );
			System.out.println("");
			//printCategory(categories[x]);
			//System.out.println(directories[CategoriesManeger.getNumber(categories[x])]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 2m | Odd cat: "+i+" | fav: false | Am: Low [ "+ intervals_AM_t2[i][0][0]+" : "+ intervals_AM_t2[i][0][1]+" ]" );
			//printCategory(categories[x]);
			//System.out.println(directories[CategoriesManeger.getNumber(categories[x])]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 2m | Odd cat: "+i+" | fav: false | Am: Med [ "+ intervals_AM_t2[i][1][0]+" : "+ intervals_AM_t2[i][1][1]+" ]" );
			//printCategory(categories[x]);
			//System.out.println(directories[CategoriesManeger.getNumber(categories[x])]);
			//System.out.println(getDirectory(categories[x]));
			System.out.println((x)+":"+categories[x++].getNumberInputValues()+":time: 2m | Odd cat: "+i+" | fav: false | Am: Hi  [ "+ intervals_AM_t2[i][2][0]+" : "+ intervals_AM_t2[i][2][1]+" ]" );
			System.out.println("");
		}
		
	}
}
