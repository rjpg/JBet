package categories.categories2013.bots;

import java.util.Vector;

import DataRepository.RunnersData;
import DataRepository.Utils;
import categories.categories2013.CategoriesParameters;
import categories.categories2013.CategoryNode;

public class RunnerCategoryData {

	Vector<CategoryNode> cat=null;
	
	RunnersData rd;
	
	RunnersData neighbour;
	
	int axisSize=3;
	
	
	
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
	}
	
	
	public double generateNNOutput()
	{
		if(CategoriesParameters.COLLECT==false)
		{
			return 0;
		}
		
		
		int ret= Utils.getOddLayTickVariation(rd, CategoriesParameters.FRAMES_PREDICTION-10, 0);
		
		return (double)ret;
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
