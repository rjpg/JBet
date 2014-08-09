package main;

public class Parameters {
	
	static public boolean log=false;  // Log or not to Log when not in replay
	static public boolean replay=false; // replay or read from file
	static public boolean replay_file_list=false; // replay or read from file
	static public boolean replay_file_list_test=false; // replay or read from file
	
	static public boolean jump_to_the_next_race=false; //not go inplay ? 
	
	static public boolean REALISTIC_TIME_REPLAY=false;
	static public boolean PAUSE_BETWEEN_RACES_REPLAY=false;
	
	static public boolean saveFavorite=false; // replay or read from file
	
	static public boolean graphicalInterface=true; // replay or read from file
	
	static public boolean graphicalInterfaceBots=false; // replay or read from file
	
	static public boolean synchronizeBetManagerWithMarketData=true; // replay or read from file
		
	
	static public boolean amountBot=false;  // see the amounts
	static public boolean manualBot=false; // manual bot
	static public boolean mecanicBot=false; // manual bot
	static public boolean studyBot=false; // manual bot
	static public boolean neuralBot=false; // manual bot
	static public boolean neuralDataBot=false; // manual bot
	static public boolean womNeighboursBot=false;
	static public boolean neighboursCorrelationBot=false;
	static public boolean influenceBot=false;
	static public boolean baseOfBot=false; // dummy bot for test
	static public boolean manualPlaceBetBot=false; // for testing BetManager
	static public boolean horselayBots=false; // for testing BetManager
	static public boolean collectHorseLiquidityBot=true; // for testing BetManager
	static public boolean runNN2013Bot=false; // for testing BetManager
	
	static public boolean dutchingBot=false;  // dutching bookmaking
	
	static public boolean simulation=false;
	
	static public int matchedStepsSimulation = 1; // in simulation part of matched amount in each call
	//1 - all at first 
	//5 - 1/5 of global amount is matched each time is call for update 
	
	
	//wheight of money calculation depth
	static public int WOM_DIST_CENTER=5;
	
	static public int CHART_FRAMES=1000;
	
	static public int HISTORICAL_DATA_FRAMES_MEMORY=1000;
	
	static public double ODD_FAVORITE=2.00;
	
	
}

//40
//7

//40
//7
