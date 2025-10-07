package categories.categories2018;

import java.util.Vector;

import categories.categories2013.CategoryNode;
import categories.categories2013.Root;

public class DataWindowsSizes2018 {


	public static double EXP_FACTOR=1;
	
	public static int TOTAL_SIZE_FRAMES=512;
	public static int SEGMENTS=128;
	
	public static int TIME_SERIES=9;
	
	public static int FRAMES_TO_PREDICT=150;
	
	// NN Constants
	public static int MIDLE_LAYER_NEURONS=45;
	
	public static int INPUT_NEURONS=TIME_SERIES; //*SEGMANTS;  // in 2018 is all in columns.. 
	
	public static int TOTAL_NEURONS=INPUT_NEURONS+MIDLE_LAYER_NEURONS+1;
	
	public static int TOTAL_CONECTIONS=(INPUT_NEURONS*MIDLE_LAYER_NEURONS)+MIDLE_LAYER_NEURONS;
	
	public static int REQUIRED_EXAMPLES=10*TOTAL_CONECTIONS;
	
	public static int COLLECT_EXAMPLES=(int)(1.5*REQUIRED_EXAMPLES);
	
	
	// Time Windows 
	public static int WINDOWS[][]=new int[SEGMENTS][2];

	
	public static void init() {
		
		double maxNormal=Math.pow(SEGMENTS, EXP_FACTOR);
		
		int totalSizeFrames=(int) (TOTAL_SIZE_FRAMES);
		
		for(int i=1;i<SEGMENTS+1;i++)
		{
			double indexNormalA=Math.pow(i-1, EXP_FACTOR);
			double indexNormalB=Math.pow(i, EXP_FACTOR);
			
			int indexA=(int)((indexNormalA*totalSizeFrames)/maxNormal);
			int indexB=(int)((indexNormalB*totalSizeFrames)/maxNormal);
			int size=indexB-indexA;
						
			WINDOWS[i-1][0]=indexA;
			WINDOWS[i-1][1]=size;
		}
	}
	
	public static int[][] getWindows(Vector<CategoryNode> cat) {
		

		return WINDOWS;
	}
	
	
	public static void main(String[] args) {
		Root root=new Root(0);
		
		DataWindowsSizes2018.init();
		
		Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root, 18);
		int [][] windows=DataWindowsSizes2018.getWindows(cat);
		System.out.println("Category id (start):"+cat.get(cat.size()-1).getIdStart()+" : "+CategoryNode.getAncestorsStringPath(cat));
		
		for(int i=0;i<SEGMENTS;i++)
		{		
			System.out.println("window ["+i+"]=["+windows[i][0]+","+windows[i][1]+"]");
		}
		
		System.out.println("MIDLE_LAYER_NEURONS : "+MIDLE_LAYER_NEURONS);;
		
		System.out.println("INPUT_NEURONS : "+INPUT_NEURONS);
		
		System.out.println("TOTAL_NEURONS : "+ TOTAL_NEURONS);
		
		System.out.println("TOTAL_CONECTIONS : "+TOTAL_CONECTIONS);
		
		System.out.println("REQUIRED_EXAMPLES : "+REQUIRED_EXAMPLES);
		
		System.out.println("COLLECT_EXAMPLES : "+COLLECT_EXAMPLES);
		
		
	}
}