package categories.categories2013.bots;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;

import DataRepository.RunnersData;
import DataRepository.Utils;
import categories.categories2013.CategoriesParameters;
import categories.categories2013.CategoryNode;
import categories.categories2013.Liquidity;

public class RunnerCategoryData {

	Vector<CategoryNode> cat=null;
	
	RunnersData rd;
	
	RunnersData neighbour;
	
	int axisSize=3;
	
	// execute Model data
	public static int PREDICT_NO_DATA_ERROR=-1;
	public static int PREDICT_COLLECT_ERROR=-2;
	public static int PREDICT_NO_MODEL_ERROR=-3;
	
	public static int PREDICT_ZERO=0;
	public static int PREDICT_WEEAK_UP=1;
	public static int PREDICT_WEEAK_DOWN=2;
	public static int PREDICT_STRONG_UP=3;
	public static int PREDICT_STRONG_DOWN=4;
	
	public static String SUFIX_MODEL="F";
	double[][] minmax=null;
	BasicNetwork network=null;
	
	
	
	public RunnerCategoryData(RunnersData rdA,Vector<CategoryNode> catA) {
		rd=rdA;
		cat=catA;
		
		if(CategoriesParameters.COLLECT)
		{
			System.out.println("favorite : "+cat.get(6).getPath());
			if(Utils.isValidWindow(rd, CategoriesParameters.FRAMES_PREDICTION, 0))
			{
				if(cat.get(2).getPath().equals("nofavorite"))
				{
					neighbour=Utils.getNeighbour(rd,CategoriesParameters.FRAMES_PREDICTION);
					System.out.println("no favorite : "+neighbour.getName());
					
				}
				else
				{
					if(cat.get(6).getPath().equals("lowOdd"))
						neighbour=Utils.getNeighbour(rd,CategoriesParameters.FRAMES_PREDICTION);
					else
						neighbour=Utils.getFavorite(rd.getMarketData(),CategoriesParameters.FRAMES_PREDICTION);
					
					System.out.println("has favorite : "+neighbour.getName());
				}
			}
			else
			{
				System.out.println("RunnerCategoryData - No Vald window to get neighbour - "+ rd.getName() );
				
			}
		}
		else
		{
			System.out.println("favorite : "+cat.get(6).getPath());
			
			if(cat.get(2).getPath().equals("nofavorite"))
			{
				neighbour=Utils.getNeighbour(rd);
				System.out.println("no favorite : "+neighbour.getName());
				
			}
			else
			{
				if(cat.get(6).getPath().equals("lowOdd"))
					neighbour=Utils.getNeighbour(rd);
				else
					neighbour=Utils.getFavorite(rd.getMarketData());
				
				System.out.println("has favorite : "+neighbour.getName());
			}
			
		}
		
		if(cat.get(6).getPath().equals("lowOdd"))
		{
			axisSize=4;
		}
		if(cat.get(6).getPath().equals("midleOdd"))
		{
			axisSize=3;
		}
		if(cat.get(6).getPath().equals("highOdd"))
		{
			axisSize=2;
		}
		
		loadExecutionModelData();
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
	
	public int loadMinMaxValues()
	{
		
		String fileName=CategoryNode.getAncestorsStringPath(cat)+"NNMinMax.csv";
		File f=new File(fileName);
		
		if(f.exists()) { 
		
			BufferedReader input=getBufferedReader(f);
			String s=null;
			
			 minmax=new double[DataWindowsSizes.INPUT_NEURONS+2][2];
			
			
			try {
				for(int i=0;i<DataWindowsSizes.INPUT_NEURONS+2;i++)
				{
					s=input.readLine();
					String sa[]=s.split(" ");
					minmax[i][0]=Double.parseDouble(sa[0]);
					minmax[i][1]=Double.parseDouble(sa[1]);
				}
				
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
			
			
		}else
		{
			System.out.println("File does not exists : "+fileName);
			return -1;
		}
		return 0;
	}
	
	public int loadNN()
	{
		String fileName=CategoryNode.getAncestorsStringPath(cat)+"nn-"+RunnerCategoryData.SUFIX_MODEL+".eg";
		File networkFile=new File(fileName);
		
		if(networkFile.exists()) { 
			network=(BasicNetwork) EncogDirectoryPersistence.loadObject(networkFile);
		}
		else
		{
			System.out.println("File does not exists : "+fileName);
			return -1;
		}
		return 0;
	}
	
	public void loadExecutionModelData()
	{
		String catPath=CategoryNode.getAncestorsStringPath(cat);
		if(CategoriesParameters.COLLECT)
		{
			System.out.println("Loading model execution data for : "+catPath);
			System.out.println("Loading normalization data for : "+catPath);
			if(loadMinMaxValues()==-1)
			{
				System.out.println("Error Loading normalization data for : "+catPath);
				return;
			}
			System.out.println("Loading NN for : "+catPath);
			if(loadNN()==-1)
			{
				System.out.println("Error NN for : "+catPath);
				return;
			}
			System.out.println("Loading model complete for : "+catPath);
			
		}
		else
		{
			System.out.println("Not Loading model execution data : Running in Collect Data Mode");
		}
	}
	
	public double generateNNOutput()
	{
		if(CategoriesParameters.COLLECT==false)
		{
			return 0;
		}
		
		double past=Utils.getOddBackAVG(rd, 10, CategoriesParameters.FRAMES_PREDICTION-10);
		double present=Utils.getOddBackAVG(rd, 0, 10);
		
		return Utils.oddToIndex(Utils.nearValidOdd(present))-Utils.oddToIndex(Utils.nearValidOdd(past));
		
	}
	
	public Vector<Double> generateNNInputs()
	{
		Vector<Double> ret=new Vector<Double>();
		int timeFramesOffset=0;
		if(CategoriesParameters.COLLECT==true)
		{
			timeFramesOffset=CategoriesParameters.FRAMES_PREDICTION;
		}
		int timeDataWindows[][]=DataWindowsSizes.getWindowsByCategory(getCat());
		
		if(!Utils.isValidWindow(rd,  timeDataWindows[DataWindowsSizes.SEGMENTS-1][1]*2, timeDataWindows[DataWindowsSizes.SEGMENTS-1][0]+timeFramesOffset))
		{
			return null;
		}
		// 7 frames de interpolação cada imput  (está em DataWindowsSizes)
		
		// evolução da odd do proprio 
		// evolução da odd do vizinho (ou favorito)
		// evolução da oferta correspondida 
		// evolução dos backs disponiveis  
		// evolução dos lays disponiveis 
		
		// 7 x 5 = 35 inputs 
		
		
		for(int i=0;i<DataWindowsSizes.SEGMENTS;i++)
		{
			ret.add((double)UtilsCollectData.getOddLayTickVariationIntegral(rd, timeDataWindows[i][0]+timeFramesOffset, timeDataWindows[i][1]));
		}
		
		for(int i=0;i<DataWindowsSizes.SEGMENTS;i++)
		{
			ret.add((double)UtilsCollectData.getOddLayTickVariationIntegral(neighbour, timeDataWindows[i][0]+timeFramesOffset, timeDataWindows[i][1]));
		}
		
		for(int i=0;i<DataWindowsSizes.SEGMENTS;i++)
		{
			ret.add(UtilsCollectData.getAmountOfferVariationAVGBackDepthWindow(rd, timeDataWindows[i][0]+timeFramesOffset, timeDataWindows[i][1],axisSize));
		}
		
		for(int i=0;i<DataWindowsSizes.SEGMENTS;i++)
		{
			ret.add(UtilsCollectData.getAmountOfferVariationAVGLayDepthWindow(rd, timeDataWindows[i][0]+timeFramesOffset, timeDataWindows[i][1],axisSize));
		}
		
		for(int i=0;i<DataWindowsSizes.SEGMENTS;i++)
		{
			ret.add(UtilsCollectData.getAmountMatchedVariationAVGAxisWindow(rd, timeDataWindows[i][0]+timeFramesOffset, timeDataWindows[i][1],axisSize));
		}
		
		return ret;
	}
	
	public double[] generateNNTrainSample()
	{
		if(CategoriesParameters.COLLECT==false)
		{
			return null;
		}
		// generateNNInputs() + output
		
		return null;
	}
	
	public Vector<CategoryNode> getCat() {
		return cat;
	}


	public RunnersData getRd() {
		return rd;
	}

	public RunnersData getNeighbour() {
		return neighbour;
	}


	
	
	
}
