package categories.categories2013;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import DataRepository.RunnersData;
import DataRepository.Utils;

public class OddInterval extends CategoryNode{

	
	double oddStart;
	double oddEnd;
	
	public OddInterval(Vector<CategoryNode> ancestorsA,double oddStartA,double oddEndA,String path) {
		super(ancestorsA);
		oddStart=oddStartA;
		oddEnd=oddEndA;
		setPath(path);
		initialize();
	}

	public void initialize()
	{
		boolean flagActivate=false;
		
		Vector<CategoryNode> cat=getAncestors();
		
		String fileName=cat.get(0).getPath();;
		for(int x=1;x<cat.size();x++)
			fileName+="/"+cat.get(x).getPath();
		
		fileName+="/liquitidyIntervalsFile.txt";
		File f=new File(fileName);
		
		if(f.exists()) { 
		
			BufferedReader input=getBufferedReader(f);
			String s=null;
			
			int recordsNumber=0;
			double firstInterval=2000.;
			double secondInterval=10000.;
			
			try {
				s=input.readLine();
				recordsNumber=Integer.parseInt(s);
				s=input.readLine();
				firstInterval=Double.parseDouble(s);
				s=input.readLine();
				secondInterval=Double.parseDouble(s);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				input.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(recordsNumber>=500)
				flagActivate=true;
			
			System.out.println("On "+fileName+" \nNumber of records = "+recordsNumber+"\nFirst Interval = "+firstInterval+"\nSecond Interval = "+secondInterval);
			if(flagActivate)
				System.out.println("1");
			else
				System.out.println("0");
			
			addChild(new Liquidity(getAncestors(),0, firstInterval, "lowLiquidity",flagActivate));
			addChild(new Liquidity(getAncestors(),firstInterval+0.01, secondInterval, "mediumLiquidity",flagActivate));
			addChild(new Liquidity(getAncestors(),secondInterval+0.01, Double.MAX_VALUE, "highLiquidity",flagActivate));
			
			
		}else
		{
			System.out.println("File does not exists : "+fileName);
			flagActivate=true;
		
			addChild(new Liquidity(getAncestors(),0, 2000, "lowLiquidity",flagActivate));
			addChild(new Liquidity(getAncestors(),2000.01, 10000, "mediumLiquidity",flagActivate));
			addChild(new Liquidity(getAncestors(),10000.01, Double.MAX_VALUE, "highLiquidity",flagActivate));
		
		}
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

	@Override
	public boolean isRunnerOnThisCategory(RunnersData rd) 
	{
		//System.out.println("testing odd - "+ rd.getName()+" - "+ Utils.getOddBackFrame(rd, 0)); 
		
		if(CategoriesParameters.COLLECT  )
		{
			if(Utils.isValidWindow(rd, CategoriesParameters.FRAMES_PREDICTION, 0))
			{
				if(Utils.getOddBackFrame(rd, CategoriesParameters.FRAMES_PREDICTION)>=oddStart && Utils.getOddBackFrame(rd, CategoriesParameters.FRAMES_PREDICTION)<=oddEnd)
					return true;
				else
					return false;
			}
			else
			{
				System.out.println("No Vald window to get Favorite - "+ rd.getName() );
				return false;
			}
		}
		else
		{
		
			if(Utils.getOddBackFrame(rd, 0)>=oddStart && Utils.getOddBackFrame(rd, 0)<=oddEnd)
				return true;
			else
				return false;
		}
	}


	

}
