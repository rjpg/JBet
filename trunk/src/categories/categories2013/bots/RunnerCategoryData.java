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
	
	
	
}
