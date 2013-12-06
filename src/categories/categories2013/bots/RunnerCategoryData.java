package categories.categories2013.bots;

import java.awt.Color;
import java.util.Vector;

import DataRepository.RunnersData;
import DataRepository.Utils;
import categories.categories2013.CategoriesParameters;
import categories.categories2013.CategoryNode;

public class RunnerCategoryData {

	Vector<CategoryNode> cat=null;
	
	RunnersData rd;
	
	RunnersData neighbour;
	
	public RunnerCategoryData(RunnersData rdA,Vector<CategoryNode> catA) {
		rd=rdA;
		cat=catA;
		
		if(CategoriesParameters.clollect)
		{
			System.out.println("favorite : "+cat.get(6).getPath());
			
			if(cat.get(2).getPath().equals("nofavorite"))
			{
				neighbour=Utils.getNeighbour(rd,90);
				System.out.println("no favorite : "+neighbour.getName());
				
			}
			else
			{
				if(cat.get(6).getPath().equals("lowOdd"))
					neighbour=Utils.getNeighbour(rd,90);
				else
					neighbour=Utils.getFavorite(rd.getMarketData(),90);
				
				System.out.println("has favorite : "+neighbour.getName());
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
	}
	
	
	
}
