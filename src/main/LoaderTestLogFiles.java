package main;

import statistics.Statistics;
import DataRepository.Utils;
import categories.CategoriesManager;

public class LoaderTestLogFiles {
	public static void main(String[] args)  throws Exception {
	Utils.init();
	Statistics.init();
	
	CategoriesManager.init();
	CategoriesManager.loadRawAMFromFile();
	CategoriesManager.processAMCatIntervals();
		
	Parameters.log=false;  // Log or not to Log when not in replay
	Parameters.replay=true; // replay or read from file
	Parameters.replay_file_list=true; // replay or read from file
	Parameters.replay_file_list_test=true; 
	Parameters.REALISTIC_TIME_REPLAY=false;
	Parameters.PAUSE_BETWEEN_RACES_REPLAY=false;
	Parameters.saveFavorite=false; // replay or read from file
	Parameters.graphicalInterface=false; // replay or read from file
	Parameters.graphicalInterfaceBots=false; // replay or read from file
	Parameters.amountBot=false;  // see the amounts
	Parameters.manualBot=false; // manual bot
	Parameters.studyBot=false; // manual bot
	Parameters.neuralBot=false; // manual bot
	Parameters.neuralDataBot=false; // manual bot
	Parameters.simulation=false;
	Parameters.matchedStepsSimulation = 1; // in simulation part of matched amount in each call
	Parameters.WOM_DIST_CENTER=5;
	Parameters.CHART_FRAMES=22;
	Parameters.ODD_FAVORITE=2.00;
	
	new Manager();
	}
}
