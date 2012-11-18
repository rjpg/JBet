package main;

import java.io.File;

import org.encog.util.arrayutil.NormalizeArray;

import statistics.Statistics;
import DataRepository.Utils;
import categories.CategoriesManager;

public class LoaderCreateDirectoryCats {
	public static void main(String[] args)  throws Exception {
		Utils.init();
		Statistics.init();
		
		CategoriesManager.init();
		CategoriesManager.loadRawAMFromFile();
		CategoriesManager.processAMCatIntervals();
		
		for (int i=0;i<CategoriesManager.getCategoriesSize();i++)
		{
			System.out.println(i+":"+CategoriesManager.getDirectory(i));
			try{ 
				boolean success = (new File(CategoriesManager.getDirectory(i))).mkdirs();
				  if (success) {
					  System.out.println("Directories: " 
							  + CategoriesManager.getDirectory(i) + " created");
				  }

			  }catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
			  }
		}
	}
}
