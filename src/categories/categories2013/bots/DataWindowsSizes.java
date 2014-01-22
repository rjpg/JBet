package categories.categories2013.bots;

import java.util.Vector;

import categories.categories2013.CategoriesParameters;
import categories.categories2013.CategoryNode;
import categories.categories2013.Root;

public class DataWindowsSizes {

	public static double HIGH_ODD_MINUTES=3.0;
	public static double MIDLE_ODD_MINUTES=2.5;
	public static double LOW_ODD_MINUTES=2.0;
	
	public static double EXP_FACTOR=2.0;
	
	public static int SEGMENTS=10;
	
	public static int HIGH_ODD_WINDOWS[][]=new int[SEGMENTS][2];
	public static int MIDLE_ODD_WINDOWS[][]=new int[SEGMENTS][2];
	public static int LOW_ODD_WINDOWS[][]=new int[SEGMENTS][2];
	
	
	
	
	public static void init() {
		
		double maxNormal=Math.pow(SEGMENTS, EXP_FACTOR);
		
		int totalSizeFrames=(int) (LOW_ODD_MINUTES*CategoriesParameters.FRAMES_PER_MINUTE);
		
		for(int i=1;i<SEGMENTS+1;i++)
		{
			double indexNormalA=Math.pow(i-1, EXP_FACTOR);
			double indexNormalB=Math.pow(i, EXP_FACTOR);
			
			int indexA=(int)((indexNormalA*totalSizeFrames)/maxNormal);
			int indexB=(int)((indexNormalB*totalSizeFrames)/maxNormal);
			int size=indexB-indexA;
						
			LOW_ODD_WINDOWS[i-1][0]=indexA;
			LOW_ODD_WINDOWS[i-1][1]=size;
		}
		
		totalSizeFrames=(int) (MIDLE_ODD_MINUTES*CategoriesParameters.FRAMES_PER_MINUTE);
		
		for(int i=1;i<SEGMENTS+1;i++)
		{
			double indexNormalA=Math.pow(i-1, EXP_FACTOR);
			double indexNormalB=Math.pow(i, EXP_FACTOR);
			
			int indexA=(int)((indexNormalA*totalSizeFrames)/maxNormal);
			int indexB=(int)((indexNormalB*totalSizeFrames)/maxNormal);
			int size=indexB-indexA;
						
			MIDLE_ODD_WINDOWS[i-1][0]=indexA;
			MIDLE_ODD_WINDOWS[i-1][1]=size;
		}
		
		totalSizeFrames=(int) (HIGH_ODD_MINUTES*CategoriesParameters.FRAMES_PER_MINUTE);
		
		for(int i=1;i<SEGMENTS+1;i++)
		{
			double indexNormalA=Math.pow(i-1, EXP_FACTOR);
			double indexNormalB=Math.pow(i, EXP_FACTOR);
			
			int indexA=(int)((indexNormalA*totalSizeFrames)/maxNormal);
			int indexB=(int)((indexNormalB*totalSizeFrames)/maxNormal);
			int size=indexB-indexA;
						
			HIGH_ODD_WINDOWS[i-1][0]=indexA;
			HIGH_ODD_WINDOWS[i-1][1]=size;
		}
	
		/*
		for(int i=0;i<SEGMENTS;i++)
		{
			System.out.println("LOW window ["+i+"]=["+LOW_ODD_WINDOWS[i][0]+","+LOW_ODD_WINDOWS[i][1]+"]");
		}
		
		for(int i=0;i<SEGMENTS;i++)
		{
			System.out.println("MIDLE window ["+i+"]=["+MIDLE_ODD_WINDOWS[i][0]+","+MIDLE_ODD_WINDOWS[i][1]+"]");
		}
		
		for(int i=0;i<SEGMENTS;i++)
		{		
			System.out.println("HIGH window ["+i+"]=["+HIGH_ODD_WINDOWS[i][0]+","+HIGH_ODD_WINDOWS[i][1]+"]");
		}*/
	}
	
	public static int[][] getWindowsByCategory(Vector<CategoryNode> cat) {
		
		if(cat.size()<7) return null;
		
		if(cat.get(6).getPath().equals("lowOdd"))
		{
			return LOW_ODD_WINDOWS;
		}
		else if(cat.get(6).getPath().equals("midleOdd"))
		{
			return MIDLE_ODD_WINDOWS;
		}
		else if(cat.get(6).getPath().equals("highOdd"))
		{
			return HIGH_ODD_WINDOWS;
		}
		
		return null;
	}
	
	
	public static void main(String[] args) {
		Root root=new Root(0);
		
		DataWindowsSizes.init();
		
		Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root, 8);
		int [][] windows=DataWindowsSizes.getWindowsByCategory(cat);
		System.out.println("Category id (start):"+cat.get(cat.size()-1).getIdStart()+" : "+CategoryNode.getAncestorsStringPath(cat));
		
		for(int i=0;i<SEGMENTS;i++)
		{		
			System.out.println("window ["+i+"]=["+windows[i][0]+","+windows[i][1]+"]");
		}
	}
}
