package main;

import statistics.Statistics;
import DataRepository.Utils;
import categories.categories2011.CategoriesManager;

public class LoaderManualPlaceBetBot {
	public static void main(String[] args)  throws Exception {
		Utils.init();
		
		
		//Statistics.init();
		
		//CategoriesManager.init();
		//CategoriesManager.loadRawAMFromFile();
		//CategoriesManager.processAMCatIntervals();
			
		Parameters.log=false;  // Log or not to Log when not in replay
		Parameters.replay=false; 
		Parameters.replay_file_list=false; 
		Parameters.replay_file_list_test=false; 
		Parameters.REALISTIC_TIME_REPLAY=false;
		Parameters.PAUSE_BETWEEN_RACES_REPLAY=false;
		Parameters.saveFavorite=false; 
		Parameters.graphicalInterface=true; 
		Parameters.graphicalInterfaceBots=true; 
		Parameters.amountBot=false;  
		Parameters.manualBot=false; 
		Parameters.manualPlaceBetBot=false; // manual place bet bot for betManager test
		Parameters.studyBot=false; 
		Parameters.neuralBot=false;
		Parameters.neighboursCorrelationBot=false;
		Parameters.neuralDataBot=false; 
		
		Parameters.simulation=true;
		//Parameters.matchedStepsSimulation = 1; // in simulation part of matched amount in each call
		Parameters.WOM_DIST_CENTER=5;
		Parameters.CHART_FRAMES=300;
		Parameters.ODD_FAVORITE=2.00;
		int x=Integer.MAX_VALUE;
		System.out.println(x);
		new Manager();
		}
}
