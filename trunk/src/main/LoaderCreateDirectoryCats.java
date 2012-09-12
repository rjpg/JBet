package main;

import java.io.File;

import org.encog.util.arrayutil.NormalizeArray;

import statistics.Statistics;
import DataRepository.Utils;
import categories.CategoriesManeger;

public class LoaderCreateDirectoryCats {
	public static void main(String[] args)  throws Exception {
		Utils.init();
		Statistics.init();
		
		CategoriesManeger.init();
		CategoriesManeger.loadRawAMFromFile();
		CategoriesManeger.processAMCatIntervals();
		
		for (int i=0;i<CategoriesManeger.getCategoriesSize();i++)
		{
			System.out.println(i+":"+CategoriesManeger.getDirectory(i));
			try{ 
				boolean success = (new File(CategoriesManeger.getDirectory(i))).mkdirs();
				  if (success) {
					  System.out.println("Directories: " 
							  + CategoriesManeger.getDirectory(i) + " created");
				  }

			  }catch (Exception e){//Catch exception if any
				  System.err.println("Error: " + e.getMessage());
			  }
		}
	}
}
