package correctscore;

import generated.exchange.BFExchangeServiceStub.BetCategoryTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetPersistenceTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetStatusEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.CancelBets;
import generated.exchange.BFExchangeServiceStub.CancelBetsResult;
import generated.exchange.BFExchangeServiceStub.MUBet;
import generated.exchange.BFExchangeServiceStub.Market;
import generated.exchange.BFExchangeServiceStub.MarketStatusEnum;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResult;
import generated.exchange.BFExchangeServiceStub.Runner;
import generated.exchange.BFExchangeServiceStub.UpdateBets;
import generated.exchange.BFExchangeServiceStub.UpdateBetsResult;
import generated.exchange.BFExchangeServiceStub.VolumeInfo;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import bfapi.handler.ExchangeAPI;
import bfapi.handler.GlobalAPI;
import bfapi.handler.ExchangeAPI.Exchange;

import DataRepository.Utils;
import demo.APIDemo;
import demo.util.APIContext;
import demo.util.Display;
import demo.util.InflatedMarketPrices;
import demo.util.InflatedMarketPrices.InflatedPrice;
import demo.util.InflatedMarketPrices.InflatedRunner;

public class GameMarketProcessFrame {

	// states
	static public final int UNDEFINED = -1;
	static public final int FINISH = 0;
	static public final int WAIT_TO_SUSTEND = 1; // start state
	static public final int SUSPENDED = 2;
	static public final int PLACING_BETS = 3;
	static public final int WAIT_TO_MATCH_BETS = 4;
	static public final int WAIT_1M_FOR_CLOSING_TRADE = 5;
	static public final int WAIT_FOR_LAY_ODD__FOR_CLOSING_TRADE = 6;
	static public final int CLOSING_TRADE = 7;
	static public final int CLOSING_TRADE_WAIT_MATCH_CONFIRM = 8;
	static public final int EDGE = 9;
	static public final int WAIT_MATCH_EDGE = 10;
	

	static public final int DIMENTION = 5;
	static public final double STAKE = 2.0;
	static public final double TOTAL_STAKE_80_PART  = 5.0; //80% de total (DIMENTION * STAKE)
	static public final int  ODD_BACK_TICKS_ABOVE_PROTECTION = 20;
	
	public int state = -1;
	public boolean CANCEL_REQUESTED = false;

	public double oddBackPrevResult = 0.0;
	public double oddLayPrevResult = 0.0;
	
	public double oddBackActualResult = 0.0;
	public double oddLayActualResult = 0.0;

	private double lowerMatchedOdd = 2000.;
	
	private double lastMachedPricePrevResult=0;
	
	// info for closing trade
	private double totalrMatchedAmountBack=0.0;
	private double avarageMatchetOddBack=0.0;
	
	private double bestLayOddToClose=2000.0;
	private double amountToClose=0;
	
	private double bestLayFound1M=2000.0;
	
	// info in close
	private double totalrMatchedAmountLay=0.0;
	private double avarageMatchetOddLay=0.0;
	private MUBet unmatchedBetLay=null;
	
	private long betLayInprocessID=0;
	
	private double totalAmountMarket=0;
	

	// THREAD
	private MarketThread as;
	private Thread t;
	private boolean polling = false;
	protected int updateInterval = 500;

	// Scores
	private int prevGoalsA = 0;
	private int prevGoalsB = 0;

	private int actualGoalsA = 0;
	private int actualGoalsB = 0;
	
	private boolean teamScore = true; // true se equipa A false se Equipa B
	
	private boolean inside_initial_20m =false;
 
	
	// BF variables Game
	BFEvent gameEvent = null;
	Market correctScoreMarket = null;
	Market matchOdds =null;
	
	public Vector<MUBet> unmatchedBetsBack=new Vector<MUBet>();
	
	public Runner marketRunnerPrevResult = null;
	public InflatedRunner iflatedRunnerPrevResult = null;
	
	public Runner marketRunnerActualResult = null;
	public InflatedRunner iflatedRunnerActualResult = null;

	// Visuals
	private MessageJFrame mjf=null;
	

	// framework
	public CorrectScoreMainFrame csmf = null;

	public GameMarketProcessFrame(CorrectScoreMainFrame csmfa, BFEvent game,
			int pgA, int pgB, int agA, int agB, String scraperName) {
		
		mjf=new MessageJFrame(game.getEventName()+":"+ agA + " ("
				+ pgA + ") -" + agB + " (" + pgB + ")");
	
		
		prevGoalsA = pgA;
		prevGoalsB = pgB;

		actualGoalsA = agA;
		actualGoalsB = agB;

		gameEvent = game;

		csmf = csmfa;
		
		if(pgA<agA)
			teamScore=true;
		else
			teamScore=false;
		
		/*try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		writeMessageText(gameEvent.getEventName() + " : " + actualGoalsA + " ("
				+ prevGoalsA + ") -" + actualGoalsB + " (" + prevGoalsB + ")",
				Color.BLACK);
		writeMessageText("Scraper Teams Names:"+scraperName,Color.BLUE);
		this.state = GameMarketProcessFrame.WAIT_TO_SUSTEND;
		
		if (actualGoalsA > 3 || actualGoalsB > 3) {
			writeMessageText("More than 3 Goals:GOTO END", Color.RED);
			this.state = GameMarketProcessFrame.FINISH;
			writeMessageText("END.", Color.ORANGE);
			clean();
			
		} else {
			if (loadCorrectScoreMarket() == -1) {
				writeMessageText(
						"Error loading Correct Score Market for this event:GOTO END",
						Color.RED);
				writeMessageText("END.", Color.ORANGE);
				this.state = GameMarketProcessFrame.FINISH;
				clean();
				// csmf.removeGameInProc(this);
				// this.stopPolling();
				// writeMessageText("END.", Color.ORANGE);
				// clean();
				// return;
			} else {
				writeMessageText("Correct Score Market for this Event Loaded",
						Color.BLUE);
			}
		}
		/*
		 * int returnState=getOddPrevGoals();
		 * if(returnState==GameMarketProcessFrame.FINISH) {
		 * writeMessageText("Correct Score Market is NOT suspended",Color.RED);
		 * this.state=GameMarketProcessFrame.FINISH; clean(); return;
		 * 
		 * } else { this.state=returnState; }
		 */

		Calendar now = Calendar.getInstance();
		//long dif=now.getTimeInMillis()-correctScoreMarket.getMarketTime().getTimeInMillis();
		
		Calendar calendar = (Calendar) correctScoreMarket.getMarketTime().clone();
		
		calendar.add(Calendar.MINUTE, 95);
		
		writeMessageText("Market Start Time:"+getTimeStamp(correctScoreMarket.getMarketTime().getTimeInMillis()), Color.BLUE);
		//writeMessageText("Diference from start time:"+getTimeStamp(dif), Color.BLUE);
		//writeMessageText("End processing time:"+getTimeStamp(calendar.getTimeInMillis()), Color.BLUE);
		writeMessageText("Now time:"+getTimeStamp(now.getTimeInMillis()), Color.BLUE);
		
		if(now.compareTo(calendar)<0)
		{
			writeMessageText("Now time is less then start plus 95 min:OK", Color.GREEN);
		}
		else
		{
			writeMessageText("Now time is more then start plus 95 min: GOTO END", Color.RED);
			this.state = GameMarketProcessFrame.FINISH;
		}
		
		calendar = (Calendar) correctScoreMarket.getMarketTime().clone();
		calendar.add(Calendar.MINUTE, 20);
		
		if(now.compareTo(calendar)<0)
		{
			writeMessageText("We are inside the initial 20 minutes", Color.ORANGE);
			inside_initial_20m=true;
		}
		else
		{
			writeMessageText("We are off the initial 20 minutes", Color.GREEN);
			inside_initial_20m=false;
		}
		
		totalAmountMarket=getTotalMatchedAmount();
		
		if(totalAmountMarket>0)
		{
			writeMessageText("Total Amount was calculated : "+ totalAmountMarket , Color.GREEN);
		}
		else
		{
			writeMessageText("Total Amount was NOT calculated : "+ totalAmountMarket , Color.RED);
			writeMessageText("END.", Color.ORANGE);
			this.state = GameMarketProcessFrame.FINISH;
			//clean();
		}
		
		if(totalAmountMarket>10000.00)
		{
			writeMessageText("Total Amount is more then 10000.00 : OK ", Color.GREEN);
		}
		else
		{
			writeMessageText("Total Amount is less then 10000.00: END ", Color.RED);
			writeMessageText("END.", Color.ORANGE);
			this.state = GameMarketProcessFrame.FINISH;
			//clean();
		}
		
		writeMessageText("Start Polling", Color.BLACK);
		startPolling();
		// this.setSize(500, 400);
		// this.setVisible(true);

	}



	public int loadCorrectScoreMarket() {

		GetEventsResp resp=null;
		MarketSummary[] markets = null;
		int indexFound = -1;

		int attempts = 0;
		while (attempts < 3 && markets == null) {
			attempts++;
			try {
				resp = GlobalAPI.getEvents(CorrectScoreMainFrame.apiContext,
						gameEvent.getEventId());
				markets = resp.getMarketItems().getMarketSummary();

				for (int i = 0; i < markets.length; i++) {
					if (markets[i].getMarketName().equals("Correct Score")) {
						writeMessageText("\"" + markets[i].getMarketName()
								+ "\" Market Found", Color.BLUE);
						indexFound = i;
					}
					// todayGames.add(events[i]);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
							
				
				//return -1;
			}
			if(markets == null)
			{
				writeMessageText("Error Reading Markets from BETFAIR API: Try Login Again", Color.RED);
				csmf.writeMessageText("Error Reading Markets from BETFAIR API: Try Login Again", Color.RED);
				try
				{
					
					LogManager.resetConfiguration();
			    	Logger rootLog = LogManager.getRootLogger();
					Level lev = Level.toLevel("OFF");
					rootLog.setLevel(lev);
					//CorrectScoreMainFrame.logout();
					CorrectScoreMainFrame.selectedExchange = Exchange.UK;
					CorrectScoreMainFrame.apiContext=new  APIContext();
					GlobalAPI.login(CorrectScoreMainFrame.apiContext, csmf.getUsername(), csmf.getPassword());
					writeMessageText("Re Login Again OK", Color.GREEN);
					csmf.writeMessageText("Re Login Again OK", Color.GREEN);
				}
				catch (Exception e)
				{
					// If we can't log in for any reason, just exit.
					Display.showException("*** Failed to log in", e);
					writeMessageText("Re Login Again NOT OK", Color.RED);
					csmf.writeMessageText("Re Login Again NOT OK", Color.RED);
				}
			}
			//#######################
			// LOGIN  ???? if null  (3x)
			//#######################
		}

		if (indexFound == -1) {
			writeMessageText("Correct Score Not Found", Color.RED);
			
			return -1;
		}

		try {
			correctScoreMarket = ExchangeAPI.getMarket(
					CorrectScoreMainFrame.selectedExchange,
					CorrectScoreMainFrame.apiContext,
					markets[indexFound].getMarketId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		

		return 0;
	}
	
	private double getTotalMatchedAmount()
	{
		double ret=0;
		
		InflatedMarketPrices prices = null;
		try {
			prices = ExchangeAPI.getMarketPrices(
					CorrectScoreMainFrame.selectedExchange,
					CorrectScoreMainFrame.apiContext,
					correctScoreMarket.getMarketId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(prices!=null)
		{
			writeMessageText("############## Amount calculation ############### ",Color.BLACK);
		
			writeMessageText("Market: " + correctScoreMarket.getName() + "("
					+ correctScoreMarket.getMarketId() + ")", Color.BLACK);
			writeMessageText("   Start time     : "
					+ correctScoreMarket.getMarketTime().getTime(), Color.BLACK);
			writeMessageText(
					"   Status         : " + correctScoreMarket.getMarketStatus(),
					Color.BLACK);
			writeMessageText(
					"   Location       : " + correctScoreMarket.getCountryISO3(),
					Color.BLACK);

			writeMessageText("Runners:", Color.BLUE);

			for (InflatedRunner r : prices.getRunners()) {
				Runner marketRunner = null;

				for (Runner mr : correctScoreMarket.getRunners().getRunner()) {
					if (mr.getSelectionId() == r.getSelectionId()) {
						marketRunner = mr;
						break;
					}
				}
				String bestLay = "";
				if (r.getLayPrices().size() > 0) {
					InflatedPrice p = r.getLayPrices().get(0);
					bestLay = String.format("%,10.2f %s @ %,6.2f",
							p.getAmountAvailable(), prices.getCurrency(),
							p.getPrice());
				}

				String bestBack = "";
				if (r.getBackPrices().size() > 0) {
					InflatedPrice p = r.getBackPrices().get(0);
					bestBack = String.format("%,10.2f %s @ %,6.2f",
							p.getAmountAvailable(), prices.getCurrency(),
							p.getPrice());
				}

				writeMessageText(
						String.format(
								"\"%20s\" (%6d): Matched Amount: %,10.2f, Last Matched: %,6.2f, Best Back %s, Best Lay:%s",
								marketRunner.getName(), r.getSelectionId(),
								r.getTotalAmountMatched(),
								r.getLastPriceMatched(), bestBack, bestLay),
						Color.BLACK);
				ret+=r.getTotalAmountMatched();
				
			}
			writeMessageText("############## Amount calculation ############### ",
					Color.BLACK);

		}
		else
		{
			writeMessageText("Prices of Match Odss Market return null ",Color.RED);
		}
		
		writeMessageText("Total Amount: "+ret,Color.GREEN);
		
		return ret;
		
	}
	
	double matchOddRunnerA=0;
	double matchOddRunnerB=0;
	public double matchOddBackTeamATeamB()
	{
		
		
		String teamA = gameEvent.getEventName().split(" v ")[0];
		String teamB = gameEvent.getEventName().split(" v ")[1];
		
		Market matchOddsMarket = null;
		
		GetEventsResp resp=null;
		MarketSummary[] markets = null;
		int indexFound = -1;

		int attempts = 0;
		while (attempts < 3 && markets == null) {
			attempts++;
			try {
				resp = GlobalAPI.getEvents(CorrectScoreMainFrame.apiContext,
						gameEvent.getEventId());
				markets = resp.getMarketItems().getMarketSummary();

				for (int i = 0; i < markets.length; i++) {
					if (markets[i].getMarketName().contains(new String("Match Odds"))) {
						writeMessageText("\"" + markets[i].getMarketName()
								+ "\" Market Found", Color.BLUE);
						indexFound = i;
					}
					// todayGames.add(events[i]);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		if (indexFound == -1) {
			writeMessageText("Match Odds market not found", Color.RED);
			
			return -1;
		}
		
		try {
			matchOddsMarket = ExchangeAPI.getMarket(
					CorrectScoreMainFrame.selectedExchange,
					CorrectScoreMainFrame.apiContext,
					markets[indexFound].getMarketId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
		InflatedMarketPrices prices = null;
		try {
			prices = ExchangeAPI.getMarketPrices(
					CorrectScoreMainFrame.selectedExchange,
					CorrectScoreMainFrame.apiContext,
					matchOddsMarket.getMarketId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(prices!=null)
		{
			writeMessageText("############## MATCH ODDS ############### ",Color.BLACK);
		
			writeMessageText("Market: " + matchOddsMarket.getName() + "("
					+ matchOddsMarket.getMarketId() + ")", Color.BLACK);
			writeMessageText("   Start time     : "
					+ matchOddsMarket.getMarketTime().getTime(), Color.BLACK);
			writeMessageText(
					"   Status         : " + matchOddsMarket.getMarketStatus(),
					Color.BLACK);
			writeMessageText(
					"   Location       : " + matchOddsMarket.getCountryISO3(),
					Color.BLACK);

			writeMessageText("Runners:", Color.BLUE);

			for (InflatedRunner r : prices.getRunners()) {
				Runner marketRunner = null;

				for (Runner mr : matchOddsMarket.getRunners().getRunner()) {
					if (mr.getSelectionId() == r.getSelectionId()) {
						marketRunner = mr;
						break;
					}
				}
				String bestLay = "";
				if (r.getLayPrices().size() > 0) {
					InflatedPrice p = r.getLayPrices().get(0);
					bestLay = String.format("%,10.2f %s @ %,6.2f",
							p.getAmountAvailable(), prices.getCurrency(),
							p.getPrice());
				}

				String bestBack = "";
				if (r.getBackPrices().size() > 0) {
					InflatedPrice p = r.getBackPrices().get(0);
					bestBack = String.format("%,10.2f %s @ %,6.2f",
							p.getAmountAvailable(), prices.getCurrency(),
							p.getPrice());
					
					if (marketRunner.getName().contains(teamA)) {
						matchOddRunnerA=p.getPrice();
					}
					
					if (marketRunner.getName().contains(teamB)) {
						matchOddRunnerB=p.getPrice();
					}
				}

				writeMessageText(
						String.format(
								"\"%20s\" (%6d): Matched Amount: %,10.2f, Last Matched: %,6.2f, Best Back %s, Best Lay:%s",
								marketRunner.getName(), r.getSelectionId(),
								r.getTotalAmountMatched(),
								r.getLastPriceMatched(), bestBack, bestLay),
						Color.BLACK);
			}
			writeMessageText("############## MATCH ODDS ############### ",
					Color.BLACK);

		}
		else
		{
			writeMessageText("Prices of Match Odss Market return null ",Color.RED);
		}
		
		
		
		return 0.0;
	}
	
	public void writeMessageText(String message, Color type) {
		if(mjf!=null)
			mjf.writeMessageText(message, type);
	}

	public BFEvent getGameEvent() {
		return gameEvent;
	}

	public int getState() {
		return state;
	}

	public void cancelProcessing() {
		writeMessageText("CANCEL CALL - proceeding with caution ", Color.RED);
		this.CANCEL_REQUESTED = true;
		
		//if(this.state==GameMarketProcessFrame.)
	}
	
	public void clean() {
		
		this.stopPolling();
		//t.interrupt();
		//t.destroy();
		csmf.removeGameInProc(this);
		this.csmf = null;
	}

	int wait_suspended = 0;
	int wait_active = 0;

	int wait_in_placeBets = 0;
	int wait_1M_for_close_trade = 0;
	int wait_for_lay_odd=0;
	int close_trade_match_confirm=0;
	
	double prevTotalrMatchedAmountLay=0.0;
	
	int wait_to_correspond_4m = 0;
	
	int wait_to_correspond_80_1m = 0;
	
	int wait_to_match_edge=0;

	boolean cancelDetected=false;
	
	boolean jackpotCalled=false;
	
	public void process() {
		if (CANCEL_REQUESTED && !cancelDetected) {
			
			
			
			cancelDetected=true;
			
			writeMessageText("CANCEL REQUEST!!!!!: Inform Jackpot Process to Close", Color.RED);
			
			// ###########################################
			// Informar o processo de Jackpot para fechar onde estiver
			// ##############################################
			
			if(this.state == GameMarketProcessFrame.WAIT_TO_SUSTEND) // só neste estado faz sentido o cancel 
			{
				writeMessageText(
						"Correct Score Market is waiting to suspend: Cancel was called: ENDING",
						Color.RED);
				this.state = GameMarketProcessFrame.FINISH;
			}
			
			if(this.state == GameMarketProcessFrame.SUSPENDED) // só neste estado faz sentido o cancel 
			{
				writeMessageText(
						"Correct Score Market is suspend: Cancel was called: ENDING",
						Color.RED);
				this.state = GameMarketProcessFrame.FINISH;
			}
			
			return;
		}

		if (this.state == GameMarketProcessFrame.SUSPENDED) {
			updateInterval = 500;
			wait_active++;
			// writeMessageText("Frame:"+n, Color.BLUE);
			if (wait_active > 100) {
				writeMessageText(
						"Correct Score Market did not went to ACTIVE : ENDING",
						Color.RED);
				this.state = GameMarketProcessFrame.FINISH;
				return;
			}
			writeMessageText(
					"Correct Score Market is suspended::Waiting to go Active",
					Color.BLUE);
			InflatedMarketPrices prices = null;
			try {
				prices = ExchangeAPI.getMarketPrices(
						CorrectScoreMainFrame.selectedExchange,
						CorrectScoreMainFrame.apiContext,
						correctScoreMarket.getMarketId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (prices != null) {
				if (prices.getMarketStatus().equals(MarketStatusEnum._ACTIVE)) {
					this.state = GameMarketProcessFrame.PLACING_BETS;
					writeMessageText(
							"Correct Score Market whent ACTIVE : GOTO PLACING BETS STATE",
							Color.GREEN);
					//################### TIRAR ISTO QUANDO MUDAR DE SCRAPER !!!! ############################
					// esperar 10s para os bots porem dinheiro no back muito acima para o caso de engano no scraper
					writeMessageText("Waiting 10 for the bots to put money in high back in case of scrapper mistake...", Color.MAGENTA);
					writeMessageText("Remove this waiting time when scrapaer change...", Color.MAGENTA);	
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (prices.getMarketStatus().equals(MarketStatusEnum._CLOSED)) {
					this.state = GameMarketProcessFrame.FINISH;
					writeMessageText(
							"Correct Score Market is CLOSED : GOTO END",
							Color.RED);
				}
			}
			return; // Faz esperar mais 500 ms quando fica activo ...(?) 
					// em vez de ir logo para baixo (sem return) para colocar as apostas 
					// podia ser retirado.. mas tem dado erro na colocação das apostas 
					// diz que o mercado ainda está suspenso 
			// se for null (time out) ignora e anda para a frente ... 
		}

		if (this.state == GameMarketProcessFrame.WAIT_TO_SUSTEND) {
			updateInterval=1000;
			wait_suspended++;
			// writeMessageText("Frame:"+n, Color.BLUE);
			if (wait_suspended > 20) {
				writeMessageText(
						"Correct Score Market did not went to SUSPEND : ENDING",
						Color.RED);
				this.state = GameMarketProcessFrame.FINISH;
				return;
			}

			writeMessageText("Correct Score Market is NOT suspended:Waiting",
					Color.ORANGE);
			InflatedMarketPrices prices = null;

			try {
					prices = ExchangeAPI.getMarketPrices(
							CorrectScoreMainFrame.selectedExchange,
							CorrectScoreMainFrame.apiContext,
							correctScoreMarket.getMarketId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			if (prices != null) {
				if (prices.getMarketStatus().equals(MarketStatusEnum._CLOSED)) {
					writeMessageText("Correct Score Market is CLOSED",
							Color.RED);
					this.state = GameMarketProcessFrame.FINISH;
					return;

				}
				
				
				if (prices.getMarketStatus().equals(MarketStatusEnum._INACTIVE)) {
					writeMessageText("Correct Score Market is INACTIVE",
							Color.RED);
					this.state = GameMarketProcessFrame.FINISH;
					return;

				}

				if (prices.getMarketStatus()
						.equals(MarketStatusEnum._SUSPENDED)) {
					updateInterval = 3000; // testar de 3 em 3 segundos
					this.state = GameMarketProcessFrame.SUSPENDED;
					writeMessageText("Correct Score Market is suspended",
							Color.GREEN);
					writeMessageText("Market: " + correctScoreMarket.getName()
							+ "(" + correctScoreMarket.getMarketId() + ")",
							Color.BLACK);
					writeMessageText("   Start time     : "
							+ correctScoreMarket.getMarketTime().getTime(),
							Color.BLACK);
					writeMessageText("   Status         : "
							+ correctScoreMarket.getMarketStatus(), Color.BLACK);
					writeMessageText("   Location       : "
							+ correctScoreMarket.getCountryISO3(), Color.BLACK);

					writeMessageText("Runners:", Color.BLUE);
					

					for (InflatedRunner r : prices.getRunners()) {
						Runner marketRunner = null;

						for (Runner mr : correctScoreMarket.getRunners()
								.getRunner()) {
							if (mr.getSelectionId() == r.getSelectionId()) {
								marketRunner = mr;
								break;
							}
						}
						String bestLay = "";
						if (r.getLayPrices().size() > 0) {
							InflatedPrice p = r.getLayPrices().get(0);
							bestLay = String.format("%,10.2f %s @ %,6.2f",
									p.getAmountAvailable(),
									prices.getCurrency(), p.getPrice());
						}

						String bestBack = "";
						if (r.getBackPrices().size() > 0) {
							InflatedPrice p = r.getBackPrices().get(0);
							bestBack = String.format("%,10.2f %s @ %,6.2f",
									p.getAmountAvailable(),
									prices.getCurrency(), p.getPrice());
						}

						writeMessageText(
								String.format(
										"\"%20s\" (%6d): Matched Amount: %,10.2f, Last Matched: %,6.2f, Best Back %s, Best Lay:%s",
										marketRunner.getName(),
										r.getSelectionId(),
										r.getTotalAmountMatched(),
										r.getLastPriceMatched(), bestBack,
										bestLay), Color.BLACK);

						if (marketRunner.getName().endsWith(
								prevGoalsA + " - " + prevGoalsB)) {
							marketRunnerPrevResult = marketRunner;
							iflatedRunnerPrevResult = r;
						}

						if (marketRunner.getName().endsWith(
								actualGoalsA + " - " + actualGoalsB)) {
							marketRunnerActualResult = marketRunner;
							iflatedRunnerActualResult = r;
						}
					}
					if (marketRunnerPrevResult == null) {
						writeMessageText(
								"Did not match previous result Runner",
								Color.RED);
						this.state = GameMarketProcessFrame.FINISH;
						return;
					} else {
						// iflatedRunner.get
						
						if (iflatedRunnerPrevResult.getBackPrices().size() > 0) {
							InflatedPrice p = iflatedRunnerPrevResult
									.getBackPrices().get(0);
							oddBackPrevResult=p.getPrice();
						}
						
						if (iflatedRunnerPrevResult.getLayPrices().size() > 0) {
							InflatedPrice p = iflatedRunnerPrevResult
									.getLayPrices().get(0);
							oddLayPrevResult=p.getPrice();
						}

						writeMessageText(
								"ODDs previous result at Suspended:" + oddBackPrevResult + "/"
										+ oddLayPrevResult + " -Market:"
										+ marketRunnerPrevResult.getName(),
								Color.BLUE);
						
						if(oddBackPrevResult==0.0)
						{
							writeMessageText(
									"Previous result ODD Back not found",
									Color.RED);
							this.state = GameMarketProcessFrame.FINISH;
							return;
						}
					}

					if (marketRunnerActualResult == null) {
						writeMessageText("Did not match ACTUAL result Runner",
								Color.RED);
						this.state = GameMarketProcessFrame.FINISH;
						return;
					} else {
						String bestBack = "";
						if (iflatedRunnerActualResult.getBackPrices().size() > 0) {
							InflatedPrice p = iflatedRunnerActualResult
									.getBackPrices().get(0);
							bestBack = String.format("%,10.2f %s @ %,6.2f",
									p.getAmountAvailable(),
									prices.getCurrency(), p.getPrice());
									oddBackActualResult=p.getPrice();
						}
						writeMessageText("Actual result Best Back »" + bestBack
								+ "«", Color.BLACK);
					}
				}
				
				//#########################################################################
				//#########################################################################
				
				if (prices.getMarketStatus()
						.equals(MarketStatusEnum._ACTIVE) && !jackpotCalled) {
					updateInterval = 500;
					
					
					writeMessageText("Correct Score Market is ACTIVE:Verify Lay money in previous Result",
							Color.ORANGE);
					
					
					writeMessageText("Market: " + correctScoreMarket.getName()
							+ "(" + correctScoreMarket.getMarketId() + ")",
							Color.BLACK);
					writeMessageText("   Start time     : "
							+ correctScoreMarket.getMarketTime().getTime(),
							Color.BLACK);
					writeMessageText("   Status         : "
							+ correctScoreMarket.getMarketStatus(), Color.BLACK);
					writeMessageText("   Location       : "
							+ correctScoreMarket.getCountryISO3(), Color.BLACK);

					writeMessageText("Runners:", Color.BLUE);
					

					for (InflatedRunner r : prices.getRunners()) {
						Runner marketRunner = null;

						for (Runner mr : correctScoreMarket.getRunners()
								.getRunner()) {
							if (mr.getSelectionId() == r.getSelectionId()) {
								marketRunner = mr;
								break;
							}
						}
						String bestLay = "";
						if (r.getLayPrices().size() > 0) {
							InflatedPrice p = r.getLayPrices().get(0);
							bestLay = String.format("%,10.2f %s @ %,6.2f",
									p.getAmountAvailable(),
									prices.getCurrency(), p.getPrice());
						}

						String bestBack = "";
						if (r.getBackPrices().size() > 0) {
							InflatedPrice p = r.getBackPrices().get(0);
							bestBack = String.format("%,10.2f %s @ %,6.2f",
									p.getAmountAvailable(),
									prices.getCurrency(), p.getPrice());
						}

						writeMessageText(
								String.format(
										"\"%20s\" (%6d): Matched Amount: %,10.2f, Last Matched: %,6.2f, Best Back %s, Best Lay:%s",
										marketRunner.getName(),
										r.getSelectionId(),
										r.getTotalAmountMatched(),
										r.getLastPriceMatched(), bestBack,
										bestLay), Color.BLACK);

						if (marketRunner.getName().endsWith(
								prevGoalsA + " - " + prevGoalsB)) {
							marketRunnerPrevResult = marketRunner;
							iflatedRunnerPrevResult = r;
						}

						if (marketRunner.getName().endsWith(
								actualGoalsA + " - " + actualGoalsB)) {
							marketRunnerActualResult = marketRunner;
							iflatedRunnerActualResult = r;
						}
					}
					
					if (marketRunnerPrevResult == null) {
						writeMessageText(
								"Did not match previous result Runner",
								Color.RED);
						this.state = GameMarketProcessFrame.FINISH;
						return;
					} else {
						// iflatedRunner.get
						
	
						
						if (iflatedRunnerPrevResult.getLayPrices().size() == 0) {
							// estava à espera de ficar suspenso mas houve golo 
							writeMessageText(
									"There is NO money in lay-Probably it went to active already :GOTO PLACE BETS",
									Color.BLUE);
							lastMachedPricePrevResult=iflatedRunnerPrevResult.getLastPriceMatched(); //for to be lower as possible to be ignored in Place bet
							writeMessageText(
									"!! Last  Matched Price Previous Result:"+lastMachedPricePrevResult,Color.BLUE);
							oddBackPrevResult=0.0;
							writeMessageText(
									"!! ODD Back previous resul is setted to 0 to be the lowest"+oddBackPrevResult,Color.BLUE);
							
							this.state=GameMarketProcessFrame.PLACING_BETS;
							// no "return" here to go directly to place bets (next "if")
							
						}
						else
						{
							InflatedPrice p = iflatedRunnerPrevResult
							.getLayPrices().get(0);
							oddLayPrevResult=p.getPrice();
							
							writeMessageText(
									"There is money in lay Previous Result : Probably No goal Detected in Bet Fair YET!!",
									Color.GREEN);
							writeMessageText("Start JACKPOT AND continue in Wait to Suspend",Color.GREEN);
							//####################################
							// START JACKPOT FRAME
							//####################################
							jackpotCalled=true;
						
								
							marketRunnerPrevResult = null;
							iflatedRunnerPrevResult = null;
					
							marketRunnerActualResult =null;
							iflatedRunnerActualResult = null;	
							
							
							
							
							this.state=GameMarketProcessFrame.WAIT_TO_SUSTEND;
							return;
						}
						
						
						
						
						
					}

					if (marketRunnerActualResult == null) {
						writeMessageText("Did not match ACTUAL result Runner",
								Color.RED);
						this.state = GameMarketProcessFrame.FINISH;
						return;
					} else {
						String bestBack = "";
						if (iflatedRunnerActualResult.getBackPrices().size() > 0) {
							InflatedPrice p = iflatedRunnerActualResult
									.getBackPrices().get(0);
							bestBack = String.format("%,10.2f %s @ %,6.2f",
									p.getAmountAvailable(),
									prices.getCurrency(), p.getPrice());
						
						}
						writeMessageText("Actual result Best Back »" + bestBack
								+ "«  " , Color.BLACK);
						
						
					}
				}
			}
		}

		if (this.state == GameMarketProcessFrame.PLACING_BETS) {

			// writeMessageText("Frame:"+n, Color.BLUE);
			writeMessageText("WATING IN PLACE BETS STATE", Color.BLUE);

			VolumeInfo[] volInfo = null;
			int attempts = 0;
			while (attempts < 3 && volInfo == null) {
				try {
					volInfo = ExchangeAPI.getMarketTradedVolume(
							CorrectScoreMainFrame.selectedExchange,
							CorrectScoreMainFrame.apiContext,
							correctScoreMarket.getMarketId(),
							marketRunnerPrevResult.getSelectionId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				attempts++;
			}
			if (volInfo == null) {
				writeMessageText("MarketTradedVolume array is null",
						Color.RED);
				this.state = GameMarketProcessFrame.FINISH;
				return;
			} else {
				for (int i = 0; i < volInfo.length; i++) {
					writeMessageText(
							"volInfo["
									+ i
									+ "].getOdds()="
									+ volInfo[i].getOdds()
									+ " , volInfo["
									+ i
									+ "].getTotalMatchedAmount()="
									+ volInfo[i]
											.getTotalMatchedAmount(),
							Color.BLACK);
					if (lowerMatchedOdd > volInfo[i].getOdds()) {
						lowerMatchedOdd = volInfo[i].getOdds();
					}

				}
				writeMessageText("Lower Matched Odd: "
						+ lowerMatchedOdd, Color.BLACK);
			}
			
			// filtrar lowerMatchedOdd
			if(volInfo.length>=2)
			{
				if((Utils.oddToIndex(volInfo[1].getOdds()) - Utils.oddToIndex(volInfo[0].getOdds()))>3)
				{
					writeMessageText("Lower Matched Odd Chosen 2nd lower:"+volInfo[1].getOdds() ,Color.BLUE);
					lowerMatchedOdd =volInfo[1].getOdds();
				}
				else
				{
					writeMessageText("Lower Matched Odd Chosen 1st lower:"+volInfo[0].getOdds() ,Color.BLUE);
					lowerMatchedOdd =volInfo[0].getOdds();
				}
			}
			else
			{
				if(volInfo.length==1)
				{
					writeMessageText("Volume Info has only ONE VALUE : Lower Matched Odd Chosen 1st lower:"+volInfo[0].getOdds() ,Color.ORANGE);
					lowerMatchedOdd =volInfo[0].getOdds();
				}
			}
			
			if (marketRunnerActualResult != null) {
				// Escolher a ODD de REFERENCIA
				double oddRef;
				
				
				if(lowerMatchedOdd > oddBackPrevResult)
					oddRef=lowerMatchedOdd;
				else
					oddRef = oddBackPrevResult;
				
				if(lastMachedPricePrevResult!=0 && oddRef==lowerMatchedOdd)
				{
					int diff=Utils.oddToIndex(lastMachedPricePrevResult)-Utils.oddToIndex(lowerMatchedOdd);
					if(diff>5)
					{
						oddRef=lastMachedPricePrevResult;
						writeMessageText("Reference ODD if now last mached price in previous result:"+lastMachedPricePrevResult ,Color.BLUE);
					}
				}
				writeMessageText("Odd of Reference:" + oddRef, Color.BLUE);
				if(!Utils.validOdd(oddRef) || oddRef==1000.0)
				{
					writeMessageText("Invalid Reference ODD : "+oddRef,Color.RED);
					this.state = GameMarketProcessFrame.FINISH;
					return;
				}
				
				double oddARB=getOddActualResultBack();
				writeMessageText("Odd Actual BACK Actual Runner:" + oddARB, Color.BLUE);
				
				int dif=Utils.oddToIndex(oddARB)-Utils.oddToIndex(oddRef);
					
				if(dif>10)
				{
					writeMessageText("Odd Actual BEST BACK is more than 10 ticks above, ticks:"+dif +" :NOT PLACING BETS: GOTO END", Color.RED);
					this.state = GameMarketProcessFrame.FINISH;
					return;
				}
				else
				{
					writeMessageText("Odd Actual BEST BACK is NOT more than 10 ticks above, ticks:"+dif, Color.GREEN);
				}
				
				//################# Processar Favorito ####################
				//################# Processar Favorito ####################
				//################# Processar Favorito ####################
				if(inside_initial_20m)
				{
					writeMessageText("we are inside the fisrt 20 minutes : Not Processing Favorite",Color.RED);
				}
				else
				{
					int favOffset=0;
					MatchOddPreLive mopl=csmf.getMatchOddPreLiveById(gameEvent.getEventId());
					if(mopl==null)
					{
						writeMessageText("Odd of Match Odd For this game not loaded", Color.PINK);
					}
					else
					{
						if (teamScore)
						{
							writeMessageText("Odd of Match Odd score team A:"+mopl.getOddBackA(), Color.PINK);
							if(mopl.getOddBackA()<2.5)
							{
								writeMessageText("Goal was made by Favorite A", Color.PINK);
								if(oddRef>10)
								{
									favOffset=-3;
								}
								else if(oddRef>3)
								{
									favOffset=-4;
								}
								else if(oddRef>2)
								{
									favOffset=-6;
								}
								else 
								{
									favOffset=-8;
								}
							}
							else
							{
								writeMessageText("Goal was made NOT by Favorite A", Color.PINK);
								if(oddRef>3)
								{
									favOffset=2;
								}
								else if(oddRef>2)
								{
									favOffset=2;
								}
								else 
								{
									favOffset=3;
								}
							}
							
						}
						else
						{
							writeMessageText("Odd of Match Odd score team B:"+mopl.getOddBackB(), Color.PINK);
							if(mopl.getOddBackB()<2.5)
							{
								writeMessageText("Goal was made by Favorite B", Color.PINK);
								
							}
							else
							{
								writeMessageText("Goal was made by NOT Favorite B", Color.PINK);
								if(oddRef>3)
								{
									favOffset=3;
								}
								else if(oddRef>2)
								{
									favOffset=4;
								}
								else 
								{
									favOffset=5;
								}
							}
						}
					}
					
					writeMessageText("The offset to oddRef("+oddRef+") is:"+favOffset, Color.PINK);				
					oddRef=Utils.indexToOdd(Utils.oddToIndex(oddRef)+favOffset);
					writeMessageText("The new oddRef is:"+oddRef, Color.PINK);	
				}
				//####################################################################
				//####################################################################
				
				if(placeBetsRef(oddRef)!=0)
				{
					writeMessageText(
							"PLACING_BETS ERROR",
							Color.RED);
					this.state = GameMarketProcessFrame.FINISH;
					return;
				}
				else
				{
					writeMessageText(
							"PLACE BETS STATE COMPLETE : GOTO WAIT_TO_CORRESPOND_4M",
							Color.GREEN);
					this.state = GameMarketProcessFrame.WAIT_TO_MATCH_BETS;
					return;
				}
			
			} else {
				writeMessageText(
						"PLACING_BETS marketRunnerActualResult is NULL",
						Color.RED);
				this.state = GameMarketProcessFrame.FINISH;
				return;
			}
		}
		
		if (this.state == GameMarketProcessFrame.WAIT_TO_MATCH_BETS) {
			updateInterval = 10000; // testar de 10 em 10 segundos
			wait_to_correspond_4m++;
			writeMessageText(
					"Wait to be MATCHED (WAIT_TO_CORRESPOND_4M) :Iteration"+wait_to_correspond_4m,
					Color.BLUE);
			
			// ################################################
			// update MATCHED
			if(updateBetsInfoBack()!=0)
			{
				writeMessageText(
						"Update Bets Info did not went OK",
						Color.RED);
			}
			else
			{
				writeMessageText(
						"Update Bets Info OK",
						Color.BLUE);
			}
			//#################################################
			
			writeMessageText(
					"MATCHED amount:"+totalrMatchedAmountBack + " @ "+ avarageMatchetOddBack,
					Color.ORANGE);
			
			if(totalrMatchedAmountBack>TOTAL_STAKE_80_PART)
			{
				writeMessageText(
						"MATCHED above 80%",
						Color.GREEN);
				
				//############################################
				writeMessageText("CANCELING ALL BETS AND UPDATE GLOBAL BETS INFO", Color.ORANGE);
				//############################################
				//CANCEL ALL BETS AND UPDATE GLOBAL BETS INFO
				int retUpdateBets=-1; // assume error 
				while (unmatchedBetsBack.size()!=0 || retUpdateBets!=0)
				{
					cancelAllBetsInMarket();
					retUpdateBets=updateBetsInfoBack();
				}
				
				writeMessageText(
						"MATCHED  :"+totalrMatchedAmountBack+" @ "+ avarageMatchetOddBack +" :GOTO WAIT 1 MINUTE",
						Color.BLUE);
				
				this.state= GameMarketProcessFrame.WAIT_1M_FOR_CLOSING_TRADE;
				updateInterval = 500;
				return;
			}
			
			if(totalrMatchedAmountBack>0.0 && wait_to_correspond_4m<23 )  // some mached
			{
				writeMessageText(
						"MATCHED some:"+totalrMatchedAmountBack+" : Wait just more 1 minute",
						Color.BLUE);
				wait_to_correspond_4m=23;
			}
			
			
			// writeMessageText("Frame:"+n, Color.BLUE);
			if (wait_to_correspond_4m > 24) {
				writeMessageText("Wait time for match complete",Color.BLUE);
				writeMessageText("CANCELING ALL BETS AND UPDATE GLOBAL BETS INFO", Color.ORANGE);
				//############################################
				//CANCEL ALL BETS AND UPDATE GLOBAL BETS INFO
				int retUpdateBets=-1; // assume error 
				while (unmatchedBetsBack.size()!=0 || retUpdateBets!=0)
				{
					cancelAllBetsInMarket();
					retUpdateBets=updateBetsInfoBack();
				}
					
				//############################################
				
				//writeMessageText("Confirm no MATCH and update total Matched Amount", Color.ORANGE);
				// ################################################
				// update MATCHED
				//retUpdateBets=updateBetsInfo();
				//#################################################
				
				
				if(totalrMatchedAmountBack==0.0)
				{
				writeMessageText(
						"NO amount was MATCHED : ENDING",
						Color.RED);	
				this.state = GameMarketProcessFrame.FINISH;
				return;
				}
			
				if(totalrMatchedAmountBack>0.0)
				{
					writeMessageText(
							"MATCHED some :"+totalrMatchedAmountBack+" @ "+ avarageMatchetOddBack +" :GOTO WAIT 1 MINUTE",
							Color.BLUE);
					this.state= GameMarketProcessFrame.WAIT_1M_FOR_CLOSING_TRADE;
					updateInterval = 500;
					return;
				}
			}
		}

		if (this.state == GameMarketProcessFrame.WAIT_1M_FOR_CLOSING_TRADE) {
			wait_1M_for_close_trade++;
			updateInterval=2000;
			
			InflatedMarketPrices prices = null;
			try {
				prices = ExchangeAPI.getMarketPrices(
						CorrectScoreMainFrame.selectedExchange,
						CorrectScoreMainFrame.apiContext,
						correctScoreMarket.getMarketId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (prices != null) {
				
				for (InflatedRunner r : prices.getRunners()) {
					Runner marketRunner = null;

					for (Runner mr : correctScoreMarket.getRunners()
							.getRunner()) {
						if (mr.getSelectionId() == r.getSelectionId()) {
							marketRunner = mr;
							break;
						}
					}
					
					if (marketRunner.getSelectionId()==marketRunnerActualResult.getSelectionId()) {
						marketRunnerActualResult = marketRunner;
						iflatedRunnerActualResult = r;
					}
					
				}
				if (marketRunnerActualResult == null) {
					writeMessageText("Did not match ACTUAL result Runner",
							Color.RED);
					this.state = GameMarketProcessFrame.FINISH;
					return;
				} else {
					String bestLayStr = "";
					if (iflatedRunnerActualResult.getLayPrices().size() > 0) {
						InflatedPrice p = iflatedRunnerActualResult
								.getLayPrices().get(0);
						bestLayStr = String.format("%,10.2f %s @ %,6.2f",
								p.getAmountAvailable(),
								prices.getCurrency(), p.getPrice());
						if(bestLayFound1M>p.getPrice())
							bestLayFound1M= p.getPrice();
					}
					else
					{
						writeMessageText("NO AMOUNT ON LAY FOUND", Color.RED);
					}
					writeMessageText("Actual result Best LAY »" + bestLayStr
							+ "«", Color.BLACK);
				}
			}
			// senão ignora e anda para a frente 
			writeMessageText("VALUE FOR ODD LAY IS NOW:" + bestLayFound1M, Color.BLUE);
			
			
			writeMessageText("WATING 20s TO GO IN CLOSE TRADE STATE : "+wait_1M_for_close_trade*2+"s", Color.BLUE);
			
			if (wait_1M_for_close_trade > 1) {
				writeMessageText("WATING COMPLETE: CLOSING TRADE COMPLETE:GOTO FIND(WAIT) BEST LAY", Color.GREEN);
				this.state = GameMarketProcessFrame.WAIT_FOR_LAY_ODD__FOR_CLOSING_TRADE;
				updateInterval = 500;
				return;
			}
		}
		
		if (this.state == GameMarketProcessFrame.WAIT_FOR_LAY_ODD__FOR_CLOSING_TRADE) {
			wait_for_lay_odd++;
			updateInterval=1000;
			
			
			InflatedMarketPrices prices = null;
			try {
				prices = ExchangeAPI.getMarketPrices(
						CorrectScoreMainFrame.selectedExchange,
						CorrectScoreMainFrame.apiContext,
						correctScoreMarket.getMarketId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (prices != null) {
				
				for (InflatedRunner r : prices.getRunners()) {
					Runner marketRunner = null;

					for (Runner mr : correctScoreMarket.getRunners()
							.getRunner()) {
						if (mr.getSelectionId() == r.getSelectionId()) {
							marketRunner = mr;
							break;
						}
					}
					
					if (marketRunner.getSelectionId()==marketRunnerActualResult.getSelectionId()) {
						marketRunnerActualResult = marketRunner;
						iflatedRunnerActualResult = r;
					}
					
				}
				if (marketRunnerActualResult == null) {
					writeMessageText("Did not match ACTUAL result Runner",
							Color.RED);
					this.state = GameMarketProcessFrame.FINISH;
					return;
				} else {
					String bestLayStr = "";
					if (iflatedRunnerActualResult.getLayPrices().size() > 0) {
						InflatedPrice p = iflatedRunnerActualResult
								.getLayPrices().get(0);
						bestLayStr = String.format("%,10.2f %s @ %,6.2f",
								p.getAmountAvailable(),
								prices.getCurrency(), p.getPrice());
						bestLayOddToClose= p.getPrice();
					}
					else
					{
						writeMessageText("NO AMOUNT ON LAY FOUND", Color.RED);
					}
					writeMessageText("Actual result Best LAY »" + bestLayStr
							+ "«", Color.BLACK);
				}
			}
			// senão ignora e anda para a frente 
			writeMessageText("VALUE FOR ODD LAY IS NOW:" + bestLayOddToClose, Color.BLUE);
			
			if( bestLayOddToClose<=avarageMatchetOddBack)
			{
				if (bestLayFound1M<bestLayOddToClose)
				{
					writeMessageText("ODD LAY FOUND IN 1M WAIT IS LESS THEN VALUE FOR ODD ACTUAL LAY:" + bestLayFound1M +" < "+bestLayOddToClose +" : GOTO CLOSE TRADE", Color.ORANGE);
					
					if( bestLayOddToClose==bestLayFound1M)  // falta testar isto 
						bestLayOddToClose=Utils.indexToOdd((Utils.oddToIndex(bestLayFound1M)));
					else
						bestLayOddToClose=bestLayFound1M;
					
					this.state = GameMarketProcessFrame.CLOSING_TRADE;
					return;
				}
				else
				{
					
					writeMessageText("VALUE FOR ODD LAY IS LESS THEN AVG MATCHED BACK ODD:" + bestLayOddToClose +" < "+avarageMatchetOddBack +" : GOTO CLOSE TRADE", Color.GREEN);
					
					if( bestLayOddToClose==avarageMatchetOddBack)
						bestLayOddToClose=Utils.indexToOdd((Utils.oddToIndex(avarageMatchetOddBack)-3));
					
					this.state = GameMarketProcessFrame.CLOSING_TRADE;
					return;
				}
			}
			else
			{
				if (bestLayFound1M<avarageMatchetOddBack)
				{
					writeMessageText("ODD LAY FOUND IN 1M WAIT IS LESS THEN AVG MATCHED BACK ODD:" + bestLayFound1M +" < "+avarageMatchetOddBack +" : GOTO CLOSE TRADE", Color.ORANGE);
					bestLayOddToClose=bestLayFound1M;
					this.state = GameMarketProcessFrame.CLOSING_TRADE;
					return;
				}
				else
				{
					writeMessageText("AVG MATCHED BACK ODD IS LESS THEN VALUE FOR ODD LAY:" + bestLayOddToClose +" < "+avarageMatchetOddBack +" : Continue Waiting", Color.RED);
				}
			}
			
			// writeMessageText("Frame:"+n, Color.BLUE);
			writeMessageText("WATING 1 MINUTE FOR LAY ODD TO GO DOWN", Color.BLUE);
			
			if (wait_for_lay_odd > 2) {
				writeMessageText("WATING FOR LAY_ODD_ COMPLETE: SET best Lay Odd To Close at minimun profit", Color.ORANGE);
				
				writeMessageText("Near Valid Odd for avarageMatchetOddBack:" +avarageMatchetOddBack+ " is:"+Utils.nearValidOdd(avarageMatchetOddBack),Color.BLUE);
				
				if(Utils.oddToIndex(Utils.nearValidOdd(avarageMatchetOddBack))==-3)
				{
					writeMessageText("Invalid avarageMatchetOddBack:"+avarageMatchetOddBack +" :Probably Bets canceled By Betfair : GOTO END",Color.ORANGE);
					this.state = GameMarketProcessFrame.FINISH;
					return;
				}
				bestLayOddToClose=Utils.indexToOdd((Utils.oddToIndex(Utils.nearValidOdd(avarageMatchetOddBack))-3));
			
				writeMessageText("best Lay Odd To Close at minimun profit:"+bestLayOddToClose,Color.ORANGE);
				this.state = GameMarketProcessFrame.CLOSING_TRADE;
				return;
			}
		}
		
		if (this.state == GameMarketProcessFrame.CLOSING_TRADE) {
			
			updateInterval=500;
			
			//#################################
			// cancel all bets 
			// update MATCHED
			//#################################
			
			// writeMessageText("Frame:"+n, Color.BLUE);
			writeMessageText("IN CLOSE TRADE STATE", Color.BLUE);
			
			amountToClose=Utils.closeAmountLay(avarageMatchetOddBack, totalrMatchedAmountBack, bestLayOddToClose);
			
			writeMessageText("Trying to Close with Lay bet :"+amountToClose+" @ "+ bestLayOddToClose,Color.GREEN);
			closeTrade();
			
			/*if (close_trade > 7) {
				writeMessageText("CLOSING TRADE COMPLETE:EXIT", Color.GREEN);
				this.state = GameMarketProcessFrame.FINISH;
				return;
			}*/
			this.state = GameMarketProcessFrame.CLOSING_TRADE_WAIT_MATCH_CONFIRM;
			return;
		}

		
		if (this.state == GameMarketProcessFrame.CLOSING_TRADE_WAIT_MATCH_CONFIRM)
		{
			updateInterval=1000;
			close_trade_match_confirm++;
			writeMessageText("State WAIT_MATCH_CONFIRM iteration:"+close_trade_match_confirm,Color.BLUE);
			int retUpdateBets=-1;
			while (retUpdateBets!=0)
			{
				writeMessageText("Updating Info about Bets on Lay",Color.BLUE);
				retUpdateBets=updateBetInfoLay();	
			}
			
			if( (Utils.convertAmountToBF(amountToClose)) <=  Utils.convertAmountToBF(totalrMatchedAmountLay))
			{
				
				writeMessageText("Amount matched :" + totalrMatchedAmountLay +" @ "+ avarageMatchetOddLay+ " : bestLayOddToClose :" + bestLayOddToClose  , Color.BLUE);
				
				if(bestLayOddToClose>avarageMatchetOddLay)
				{
					writeMessageText("MATCH FOR CLOSING NOT COMPLETE: GOTO EDGE:" ,Color.ORANGE);
					this.state = GameMarketProcessFrame.EDGE;
					return;
				}
				else
				{
					writeMessageText("MATCH FOR CLOSING COMPLETE (amountToClose <= totalrMatchedAmountLay):"+ totalrMatchedAmountLay +" @ "+avarageMatchetOddLay +" GOTO END",Color.GREEN);
					this.state = GameMarketProcessFrame.FINISH;
					return;
				}
			}
			
			if(unmatchedBetLay==null )
			{
				writeMessageText("Amount matched :" + totalrMatchedAmountLay +" @ "+ avarageMatchetOddLay+ " : bestLayOddToClose :" + bestLayOddToClose  , Color.BLUE);
				
				if(bestLayOddToClose>avarageMatchetOddLay)
				{
					writeMessageText("MATCH FOR CLOSING NOT COMPLETE: GOTO EDGE:" ,Color.ORANGE);
					this.state = GameMarketProcessFrame.EDGE;
					return;
				}
				else
				{
					writeMessageText("MATCH FOR CLOSING COMPLETE (unmatchedBetLay==null):"+ totalrMatchedAmountLay +" @ "+avarageMatchetOddLay +" GOTO END",Color.GREEN);
					this.state = GameMarketProcessFrame.FINISH;
					return;	
				}
			
			}

			writeMessageText("MATCH FOR CLOSING NOT COMPLETE:"+ totalrMatchedAmountLay +" @ "+avarageMatchetOddLay + " Cmount To Close:" + amountToClose ,Color.RED);
			
			if(unmatchedBetLay.getBetStatus()==BetStatusEnum.C)
			{
				writeMessageText("BET WAS CANCELED by Betfair !! BIG RED !! : GOTO END",Color.RED);
				this.state = GameMarketProcessFrame.FINISH;
				writeMessageText("END.", Color.ORANGE);
				clean();
				return;
				// SAI A PERDER (VERMELHO ou GOLO ou ENGANO) 
			}
			
			if(totalrMatchedAmountLay<prevTotalrMatchedAmountLay)
			{
				close_trade_match_confirm-=30;
				prevTotalrMatchedAmountLay=0;
				
			}
			prevTotalrMatchedAmountLay=totalrMatchedAmountLay;
			
			if (close_trade_match_confirm > 10) {
				close_trade_match_confirm=0;
				writeMessageText("REFORMULATING ODD FOR CLOSE", Color.ORANGE);
				
				writeMessageText("Canceling Lay bet", Color.BLUE);
				try {
					cancelBet(unmatchedBetLay);
				} catch (Exception e) {
					writeMessageText("Some Problem trying to Cancel Bet",Color.RED);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				writeMessageText("CANCEL LAY BET CALLED", Color.BLUE);
				
				retUpdateBets=-1;
				while (retUpdateBets!=0)
				{
					writeMessageText("Updating Info about Bets on Lay",Color.BLUE);
					retUpdateBets=updateBetInfoLay();	
				}
				
				writeMessageText("amountToClose :"+amountToClose +" ("+Utils.convertAmountToBF(amountToClose), Color.BLUE);
				writeMessageText("totalrMatchedAmountLay:"+totalrMatchedAmountLay +" ("+Utils.convertAmountToBF(totalrMatchedAmountLay), Color.BLUE);
				//writeMessageText("unmatchedBetLay:"+unmatchedBetLay, Color.BLUE);
			
				if( (Utils.convertAmountToBF(amountToClose)) == Utils.convertAmountToBF(totalrMatchedAmountLay) )
				{
					writeMessageText("bestLayOddToClose:"+bestLayOddToClose, Color.BLUE);
					writeMessageText("avarageMatchetOddLay:"+avarageMatchetOddLay, Color.BLUE);
					
					// PROCESSAR O EDGE
					if(bestLayOddToClose>avarageMatchetOddLay)
					{
						writeMessageText("MATCH FOR CLOSING COMPLETE: GOTO EDGE:" ,Color.BLUE);
						this.state = GameMarketProcessFrame.EDGE;
						return;
						
					}
					else
					{
						writeMessageText("MATCH FOR CLOSING COMPLETE after Cancel and Update (amountToClose <= totalrMatchedAmountLay):"+ totalrMatchedAmountLay +" @ "+avarageMatchetOddLay +" GOTO END",Color.GREEN);
						this.state = GameMarketProcessFrame.FINISH;
						return;
					}
					
				}
				else 
				{
					writeMessageText("MATCH FOR CLOSING NOT COMPLETE after Cancel and Update ... : Continue reformulation",Color.RED);
					if(calculateAmountToCloseAndBestLayOddToClose()!=0)
					{
						this.state = GameMarketProcessFrame.FINISH;
						return;
					}
				}
				
				//if(unmatchedBetLay!=null)
				//{
				
				
				writeMessageText("Closing Trade CALL (in REFORMULATING)",Color.BLUE);
				
				closeTrade();
				
				//writeMessageText("Bet LAY PROCESS ID:"+ betLayInprocessID, Color.BLUE);
					
				//}
				prevTotalrMatchedAmountLay=0;

			}
						
			
		}
		
		if (this.state == GameMarketProcessFrame.EDGE) {
			writeMessageText(" Entering the EDGE state", Color.GREEN);
			
			/*int retUpdateBets=-1;
			while (retUpdateBets!=0)
			{
				writeMessageText("Updating Info about Bets on Lay",Color.BLUE);
				retUpdateBets=updateBetInfoLay();	
			}
			
			if(unmatchedBetLay!=null)
			{
				writeMessageText("unmatchedBetLay should be null - Canceling Lay bet ", Color.RED);	
				try {
					cancelBet(unmatchedBetLay);
				} catch (Exception e) {
					writeMessageText("Some Problem trying to Cancel Bet",Color.RED);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			*/
			
			double actual_odd_Back=getOddActualResultBack();
			
			writeMessageText("#################################", Color.BLUE);
			writeMessageText("unmatchedBetLay : "+unmatchedBetLay,Color.BLACK);
			writeMessageText("bestLayOddToClose : "+bestLayOddToClose,Color.BLACK);
			writeMessageText("avarageMatchetOddLay : "+avarageMatchetOddLay,Color.BLACK);
			writeMessageText("avarageMatchetOddBack : "+avarageMatchetOddBack,Color.BLACK);
			writeMessageText("amountToClose : "+ amountToClose,Color.BLACK);
			writeMessageText("totalrMatchedAmountLay : "+totalrMatchedAmountLay,Color.BLACK);
			writeMessageText("totalrMatchedAmountBack : "+totalrMatchedAmountBack,Color.BLACK);
			writeMessageText("getOddActualResultBack() -> actual_odd_Back : "+actual_odd_Back,Color.BLACK);
			writeMessageText("#################################", Color.BLUE);
			
			double recoverLastAmountBack=Utils.closeAmountBack(bestLayOddToClose,totalrMatchedAmountLay,avarageMatchetOddBack);
			writeMessageText("recoverLastAmountBack : "+recoverLastAmountBack,Color.BLACK);
			
			double desgaste=Utils.closeAmountBack(avarageMatchetOddLay, amountToClose, avarageMatchetOddBack);
			double inFault=recoverLastAmountBack-desgaste;
			// porque agora o updtate do amount correspondido no LAY é só em relação à ultima aposta do LAy e não de todas
			totalrMatchedAmountBack=inFault; 
			//bestLayOddToClose=actual_odd_Back;
			
			bestLayOddToClose=Utils.nearValidOdd(avarageMatchetOddLay);
			
			amountToClose=Utils.closeAmountLay(avarageMatchetOddBack, inFault, bestLayOddToClose);
			
			writeMessageText("desgaste : "+desgaste,Color.BLACK);
			writeMessageText("new totalrMatchedAmountBack : "+totalrMatchedAmountBack,Color.BLACK);
			writeMessageText("amountToClose : "+amountToClose,Color.BLACK);
			writeMessageText("bestLayOddToClose : "+bestLayOddToClose,Color.BLACK);
			writeMessageText("#################################", Color.BLUE);
			
			
			
			
			closeTrade();
			
			writeMessageText("CLOSE EDGE COMPLETE : GOTO WAIT_MATCH_EDGE",Color.GREEN);
			this.state = GameMarketProcessFrame.WAIT_MATCH_EDGE;
			return;
			/*
			
			writeMessageText("bestLayOddToClose > avarageMatchetOddLay : Processing Edge",Color.BLUE);
			if(calculateAmountToCloseAndBestLayOddToCloseEdge()!=0)
			{
				this.state = GameMarketProcessFrame.FINISH;
				return;
			}
			
			
			writeMessageText("Closing Trade Call (edge)",Color.BLUE);
			closeTrade();
			
			close_trade_match_confirm=0; //Recomeçar a contagem 
			
			retUpdateBets=-1;
			while (retUpdateBets!=0)
			{
				writeMessageText("Updating Info about Bets on Lay",Color.BLUE);
				retUpdateBets=updateBetInfoLay();	
			}
			
			//##########################################################################################
			
			writeMessageText("bestLayOddToClose:" +bestLayOddToClose+" avarageMatchetOddLay:"+ avarageMatchetOddLay , Color.BLUE);
			if(unmatchedBetLay!=null) // em principio desnecessario porque se foi tudo match não há unmatchedBetLay
			{
				writeMessageText("Canceling Lay bet", Color.BLUE);
				try {
					cancelBet(unmatchedBetLay);
				} catch (Exception e) {
					writeMessageText("Some Problem trying to Cancel Bet",Color.RED);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				writeMessageText("CANCEL LAY BET CALLED", Color.BLUE);
				unmatchedBetLay=null;
			}
			
			writeMessageText("bestLayOddToClose > avarageMatchetOddLay : Processing Edge (in REFORMULATING)",Color.BLUE);
			if(calculateAmountToCloseAndBestLayOddToCloseEdge()!=0)
			{
				this.state = GameMarketProcessFrame.FINISH;
				return;
			}

			retUpdateBets=-1;
			while (retUpdateBets!=0)
			{
				writeMessageText("Updating Info about Bets on Lay",Color.BLUE);
				retUpdateBets=updateBetInfoLay();	
			}*/
		}
		
		
		if (this.state == GameMarketProcessFrame.WAIT_MATCH_EDGE) {
			wait_to_match_edge++;
			writeMessageText("WAIT_MATCH_EDGE iteration:"+wait_to_match_edge, Color.BLUE);
			
			int retUpdateBets=-1;
			while (retUpdateBets!=0)
			{
				writeMessageText("Updating Info about Bets on Lay",Color.BLUE);
				retUpdateBets=updateBetInfoLay();	
			}
			
			double actual_odd_Back=getOddActualResultBack();
			
			writeMessageText("#################################", Color.BLUE);
			writeMessageText("unmatchedBetLay : "+unmatchedBetLay,Color.BLACK);
			writeMessageText("bestLayOddToClose : "+bestLayOddToClose,Color.BLACK);
			writeMessageText("avarageMatchetOddLay : "+avarageMatchetOddLay,Color.BLACK);
			writeMessageText("avarageMatchetOddBack : "+avarageMatchetOddBack,Color.BLACK);
			writeMessageText("totalrMatchedAmountLay : "+totalrMatchedAmountLay,Color.BLACK);
			writeMessageText("totalrMatchedAmountBack : "+totalrMatchedAmountBack,Color.BLACK);
			writeMessageText("amountToClose : "+amountToClose,Color.BLACK);
			writeMessageText("getOddActualResultBack() -> actual_odd_Back : "+actual_odd_Back,Color.BLACK);
			writeMessageText("#################################", Color.BLUE);
			
			if (wait_to_match_edge > 3) {
				this.state = GameMarketProcessFrame.FINISH;
				return;
			}
			
		}
		
		if (this.state == GameMarketProcessFrame.FINISH) {
			writeMessageText("END.", Color.ORANGE);
			this.state = GameMarketProcessFrame.FINISH;
			clean();
		}

	}
	
	private int calculateAmountToCloseAndBestLayOddToClose()
	{
		double desgaste=Utils.closeAmountBack(avarageMatchetOddLay, totalrMatchedAmountLay, avarageMatchetOddBack);
		double inFault=totalrMatchedAmountBack-desgaste;
		// porque agora o updtae do amount correspondido no LAY é só em relação à ultima aposta do LAy e não de todas
		totalrMatchedAmountBack=inFault;  
		if(inFault<0)
		{
			writeMessageText("Value in Fault is negative something went wrong",Color.BLUE);
			return -1;
		}
		double actual_odd_Back=getOddActualResultBack();
		
		writeMessageText("Desgaste:"+desgaste+" totalrMatchedAmountBack:"+totalrMatchedAmountBack+" inFault:"+inFault, Color.BLUE);
		
		if(actual_odd_Back<=bestLayOddToClose)
		{
			bestLayOddToClose=Utils.indexToOdd((Utils.oddToIndex(bestLayOddToClose)+1));
		}
		else
		{
			if(actual_odd_Back!=1000.)
			{
				bestLayOddToClose=Utils.indexToOdd((Utils.oddToIndex(actual_odd_Back)+1));
		
			}
			else
			{
				writeMessageText("Odd Back is at 1000. in this runner !! BIG RED !! : GOTO END",Color.RED);
				this.state = GameMarketProcessFrame.FINISH;
				writeMessageText("END.", Color.ORANGE);
				clean();
				return -1;
			}
			
		}
		amountToClose=Utils.closeAmountLay(avarageMatchetOddBack, inFault, bestLayOddToClose);
				
		return 0;
		//sdas
				
				
		///############### DEBUG #############################
		//double am=Utils.convertAmountToBF(amountToClose);
		//writeMessageText("Creating (and PLACE!!) lay bet:"+ am + " @ "+ bestLayOddToClose , Color.RED);
		///###################### END DEBUG ##################
	}
	
	private int calculateAmountToCloseAndBestLayOddToCloseEdge()
	{
		double desgaste=Utils.closeAmountBack(avarageMatchetOddLay, totalrMatchedAmountLay, avarageMatchetOddBack);
		double inFault=totalrMatchedAmountBack-desgaste;
		// porque agora o updtate do amount correspondido no LAY é só em relação à ultima aposta do LAy e não de todas
		totalrMatchedAmountBack=inFault; 
		if(inFault<0)
		{
			writeMessageText("Value in Fault is negative something went wrong",Color.BLUE);
			return -1;
		}
		writeMessageText("Desgaste:"+desgaste+" totalrMatchedAmountBack:"+totalrMatchedAmountBack+" inFault:"+inFault, Color.BLUE);
		amountToClose=Utils.closeAmountLay(avarageMatchetOddBack, inFault, bestLayOddToClose);
		
		double actual_odd_Back=getOddActualResultBack();
		if(actual_odd_Back==1000.)
		{
			writeMessageText("Odd Back is at 1000. in this runner !! BIG RED !! : GOTO END",Color.RED);
			this.state = GameMarketProcessFrame.FINISH;
			writeMessageText("END.", Color.ORANGE);
			clean();
			return -1;
		}
		return 0;
	}
	
	private void cancelBet(MUBet bet) throws Exception {
		
			//if (Display.confirm("This action will actually cancel a bet on the Betfair exchange")) {
				CancelBets canc = new CancelBets();
				canc.setBetId(bet.getBetId());
				
				// We can ignore the array here as we only sent in one bet.
				CancelBetsResult betResult = ExchangeAPI.cancelBets(csmf.selectedExchange, csmf.apiContext, new CancelBets[] {canc})[0];
				
				if (betResult.getSuccess()) {
					Display.println("Bet "+betResult.getBetId()+" cancelled.");
				} else {
					Display.println("Failed to cancel bet: Problem was: "+betResult.getResultCode());
				}
			//}
		
	}
	
	private void cancelBetID(long betID) throws Exception {
		
		//if (Display.confirm("This action will actually cancel a bet on the Betfair exchange")) {
			CancelBets canc = new CancelBets();
			canc.setBetId(betID);
			
			// We can ignore the array here as we only sent in one bet.
			CancelBetsResult betResult = ExchangeAPI.cancelBets(csmf.selectedExchange, csmf.apiContext, new CancelBets[] {canc})[0];
			
			if (betResult.getSuccess()) {
				Display.println("Bet "+betResult.getBetId()+" cancelled.");
			} else {
				Display.println("Failed to cancel bet: Problem was: "+betResult.getResultCode());
			}
		//}
	
}

	public double getOddActualResultBack()
	{
		
		if(marketRunnerActualResult==null)
		{
			writeMessageText("getOddActualResultBack() ERROR : marketRunnerActualResult==null", Color.RED);
			return -1;
		}
		
		
		int attempts = 0;
		InflatedMarketPrices prices = null;
		while (attempts < 3 && prices == null) {
			try {
				prices = ExchangeAPI.getMarketPrices(
						CorrectScoreMainFrame.selectedExchange,
						CorrectScoreMainFrame.apiContext,
						correctScoreMarket.getMarketId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			attempts++;
		}
		if (prices != null) {
			
			Runner marketRunnerFound = null;
			InflatedRunner inflatedRunnerfound =null;
			for (InflatedRunner r : prices.getRunners()) {
				Runner marketRunner = null;

				for (Runner mr : correctScoreMarket.getRunners()
						.getRunner()) {
					if (mr.getSelectionId() == r.getSelectionId()) {
						marketRunner = mr;
						break;
					}
				}
				
				if (marketRunner.getSelectionId()==marketRunnerActualResult.getSelectionId()) {
					marketRunnerFound=marketRunner;
					inflatedRunnerfound=r;
				}
				
			}
			if (marketRunnerFound == null) {
				writeMessageText("getOddActualResultBack() ERROR :Did not match ACTUAL result Runner",
						Color.RED);
				return -2;
			} else {
				
				if (inflatedRunnerfound.getBackPrices().size() > 0) {
					InflatedPrice p = inflatedRunnerfound.getBackPrices().get(0);
					writeMessageText("getOddActualResultBack():AMOUNT ON BEST BACK:"+p.getPrice(), Color.BLUE);
					return p.getPrice();
				}
				else
				{
					writeMessageText("getOddActualResultBack() ERROR:NO AMOUNT ON BACK FOUND", Color.RED);
					return 0;
				}
			}
		}
		else
		{
			writeMessageText("getOddActualResultBack() ERROR : returned prices ==null", Color.RED);
			return -3;
		}
	}
	
	public int updateBetInfoLay()
	{
		MUBet[] bets =null;
		try {
			bets=ExchangeAPI.getMUBets(CorrectScoreMainFrame.selectedExchange, CorrectScoreMainFrame.apiContext, correctScoreMarket.getMarketId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			writeMessageText("Error retieaving bets (updateBetInfoLay())", Color.RED);
			e.printStackTrace();
			return -1;
		}
		
		if(bets!=null)
		{
			updateGlobalInfoBetLay(bets);
		}
		else
		{
			writeMessageText("Warning: getMUBets has FAIL (leaving previous results)",Color.RED);
			return -1;
		}
		
		return 0;
		
	}
	
	public void updateGlobalInfoBetLay(MUBet[] bets) {
		Vector<Double> matchedSizes = new Vector<Double>();
		Vector<Double> matchedPrices = new Vector<Double>();
		int UBNumber=0;
		MUBet unmatchedBetLayAux = null;
		
		for (int i = 0; i < bets.length; i++) {
			MUBet b = bets[i];

			Runner marketRunner = null;
			for (Runner mr: correctScoreMarket.getRunners().getRunner()) {
				if (mr.getSelectionId() == b.getSelectionId()) {
					marketRunner = mr;
					break;
				}
			}
			
			if(marketRunner.getSelectionId()==marketRunnerActualResult.getSelectionId())
			{
				writeMessageText(String.format("   %2d: %9s bet on \"%s\" for %,6.2f @ %s (id = %d) : %s",
							i+1,
							((b.getBetStatus()==BetStatusEnum.M) ? "matched" : "unmatched"),
							marketRunner.getName(),
							b.getSize(),
							b.getPrice(),
							b.getBetId(),
							((b.getBetType()==BetTypeEnum.B) ? "Back" : "Lay"),
							b.getBetPersistenceType()),Color.BLACK);
				if(b.getBetType()==BetTypeEnum.L)
				{
					
					if(b.getBetStatus()==BetStatusEnum.M &&  betLayInprocessID==b.getBetId())
					{
						//Só contabilizar dados de Matched da aposta em processmento Lay actual
						matchedSizes.add(b.getSize());
						matchedPrices.add(b.getPrice());
					}
					else
					{
						if(b.getBetStatus()==BetStatusEnum.U)
						{
						//Actualizar Aposta Lay actual - só há uma de cada vez!! - (parte unmatched)
						unmatchedBetLayAux=b;
						UBNumber++;
						}
					}
				}
			}
		}
		
		unmatchedBetLay=unmatchedBetLayAux;
		
		if(UBNumber>1)
		{
			writeMessageText("!!! Unmatched BETs > 1 in updateGlobalInfoBetsLay() !!",Color.RED);
		}
		
		if(matchedPrices.size()>0)
		{
			totalrMatchedAmountLay=0.0;
			double[] prices=new double[matchedPrices.size()];
			double[] sizes=new double[matchedPrices.size()];
			for(int i = 0 ;i<matchedSizes.size();i++)
			{
				totalrMatchedAmountLay+=matchedSizes.get(i);
				sizes[i]=matchedSizes.get(i);
				prices[i]=matchedPrices.get(i);
			}
			avarageMatchetOddLay=Utils.calculateOddAverage(prices, sizes);
		}
		else
		{
			totalrMatchedAmountLay=0.0; avarageMatchetOddLay=0.0;
			writeMessageText("No Matched bets Lay Found : no match on LAY (assuming went ok) ",Color.ORANGE);
		}
		
	}
	
	public int updateBetsInfoBack()
	{
		
		// TER CUIDADOS COM AS APOSTAS DE UM MERCADO DIFERENCIAR POR RUNNER
		
		
		MUBet[] bets =null;
		try {
			bets=ExchangeAPI.getMUBets(CorrectScoreMainFrame.selectedExchange, CorrectScoreMainFrame.apiContext, correctScoreMarket.getMarketId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
		if(bets!=null)
		{
			updateGlobalInfoBetsBack(bets);
		}
		else
		{
			writeMessageText("Warning: getMUBets has FAIL",Color.RED);
			return -1;
		}
		//private double totalrMatchedAmount=0.0;
		//private double avarageMatchetOdd=0.0;
		
		return 0;
	}
	
	public void updateGlobalInfoBetsBack(MUBet[] bets) {
		Vector<Double> matchedSizes = new Vector<Double>();
		Vector<Double> matchedPrices = new Vector<Double>();
		
		unmatchedBetsBack.clear();
		
		writeMessageText(String.format("Current bets on %s market:", correctScoreMarket.getName()),Color.BLACK);
		
		for (int i = 0; i < bets.length; i++) {
			MUBet b = bets[i];

			Runner marketRunner = null;
			for (Runner mr: correctScoreMarket.getRunners().getRunner()) {
				if (mr.getSelectionId() == b.getSelectionId()) {
					marketRunner = mr;
					break;
				}
			}
			
			if(marketRunner.getSelectionId()==marketRunnerActualResult.getSelectionId())
			{
				writeMessageText(String.format("   %2d: %9s bet on \"%s\" for %,6.2f @ %s (id = %d) : %s",
							i+1,
							((b.getBetStatus()==BetStatusEnum.M) ? "matched" : "unmatched"),
							marketRunner.getName(),
							b.getSize(),
							b.getPrice(),
							b.getBetId(),
							((b.getBetType()==BetTypeEnum.B) ? "Back" : "Lay")),Color.BLACK);
				if(b.getBetType()==BetTypeEnum.B)
				{
					if(b.getBetStatus()==BetStatusEnum.M)
					{
						matchedSizes.add(b.getSize());
						matchedPrices.add(b.getPrice());
					}
					else
					{
						unmatchedBetsBack.add(b);
					
					}
				}
			}
		}
		
		if(matchedPrices.size()>0)
		{
			totalrMatchedAmountBack=0.0;
			double[] prices=new double[matchedPrices.size()];
			double[] sizes=new double[matchedPrices.size()];
			for(int i = 0 ;i<matchedSizes.size();i++)
			{
				totalrMatchedAmountBack+=matchedSizes.get(i);
				sizes[i]=matchedSizes.get(i);
				prices[i]=matchedPrices.get(i);
			}
			avarageMatchetOddBack=Utils.calculateOddAverage(prices, sizes);
		}
		else
		{
			writeMessageText("No Matched bets Back Found : LEAVE PREVIOUS VALUES for next iteration",Color.ORANGE);
		}
	}
	
	
	public int closeTrade()
	{
		// odd currente 
		if(Utils.convertAmountToBF(amountToClose)<2.0)
		{
			writeMessageText("New bet size to close request is lower then 2.0€", Color.BLUE);
			if(placeLowSizeLayBet(bestLayOddToClose,amountToClose)==-1)
			{
				writeMessageText("Some problem placing bet lower then 2€", Color.BLUE);
				return -1;
			}
		
			betLayInprocessID=newPriceLowLayBetId;
		}
		else
		{
			placeCloseBetLay();
		}
		
		return 0; 
	}
	
	public int placeCloseBetLay()
	{
		PlaceBets[] bets=new PlaceBets[1];
		
		double am=Utils.convertAmountToBF(amountToClose);
		writeMessageText("Creating (and PLACE!!) lay bet:"+ am + " @ "+ bestLayOddToClose , Color.RED);
		bets[0]=createPlaceLayBet(bestLayOddToClose, am);
		PlaceBetsResult[] betResult=null;
		
		///*
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
			writeMessageText("ExchangeAPI.placeBets (LAY) Attempt :"+attempts, Color.BLUE);
			try {
				betResult=ExchangeAPI.placeBets(CorrectScoreMainFrame.selectedExchange, CorrectScoreMainFrame.apiContext, bets);
			} catch (Exception e) {
				
				if(e.getMessage().contains(new String("EVENT_SUSPENDED")))
				{
					writeMessageText("ExchangeAPI.placeBets Returned NULL: Market is supended",Color.BLUE);
					attempts--;
				}
				// TODO Auto-generated catch block
				e.printStackTrace();
				writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
				writeMessageText("Wait 1s..."+attempts, Color.BLUE);
				try {
					
					Thread.sleep(1000);
				} catch (InterruptedException w) {
					// TODO Auto-generated catch block
					w.printStackTrace();
				}
			}
			
			attempts++;
		}
		
		if(betResult==null)
		{
			writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed", Color.RED);
			writeMessageText("Place Bet Lay (placeCloseBetLay() function) end: return 3;",Color.BLUE);
			return 3;
		}
		else
		{
			for(int i=0;i<betResult.length;i++)
			{
				if (betResult[i].getSuccess()) {
					writeMessageText("Bet Id:" + betResult[i].getBetId()
							+ " placed : " + betResult[i].getSizeMatched()
							+ " matched @ "
							+ betResult[i].getAveragePriceMatched(),Color.GREEN);
					 betLayInprocessID=betResult[i].getBetId();
				} else {
				//TODO TEST DESTE ERRO
			
					writeMessageText("Failed to place bet: Problem was: "
							+ betResult[i].getResultCode(),Color.RED);
							return 4;
				}
			}
		}
		
		writeMessageText("Bets place called. Wait 3 seconds", Color.BLUE);
		for(int i=0;i<8;i++)
		{
			writeMessageText("Waited "+3+" seconds", Color.BLUE);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		writeMessageText("Wait Complete", Color.GREEN);
		
		writeMessageText("Place Bet Lay (placeCloseBetLay() function) end: return 0;",Color.BLUE);
		
		//*/
		//@TODO Variaveis para update...
		 
		return 0;
	}
	
	
	
	public int placeBetsRef(double oddRef)
	{
		
		int offset=0;
		if(oddRef>3)
		{
			offset=1;
		}
		else if(oddRef>2)
		{
			offset=2;
		}
		else 
		{
			offset=2;
		}
		
		Vector<PlaceBets> pbv=new Vector<PlaceBets>();
		double odd;
		for(int i=0;i<GameMarketProcessFrame.DIMENTION;i++)
		{
			int oddIndex=Utils.oddToIndex(oddRef)+i+offset;
			if(Utils.validOddIndex(oddIndex))
			{
				odd=Utils.indexToOdd(oddIndex);
				if(Utils.validOdd(odd))
				{	
					PlaceBets bet=createPlaceBackBet(odd,GameMarketProcessFrame.STAKE);
					pbv.add(bet);
				}
				else
				{
					writeMessageText("Invalid odd for PlaceBet:"+odd, Color.RED);
				}
			}
			else
			{
				writeMessageText("Invalid oddIndex:"+oddRef+"+ "+i+" steps", Color.RED);
			}
		}
		
		if(pbv.size()<0)
		{
			writeMessageText("No Valid Bets to place", Color.RED);
			return 1;
		}
		///*
		else
		{
			writeMessageText("ExchangeAPI.placeBets CALL", Color.BLUE);
			PlaceBets[] bets=(PlaceBets[]) pbv.toArray(new PlaceBets[0]);
			PlaceBetsResult[] betResult=null;
			int attempts = 0;
			while (attempts < 3 && betResult == null) {
				try {
					betResult=ExchangeAPI.placeBets(CorrectScoreMainFrame.selectedExchange, CorrectScoreMainFrame.apiContext, bets);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					
					writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
					writeMessageText("Wait 1s..."+attempts, Color.BLUE);
					try {
						
						Thread.sleep(1000);
					} catch (InterruptedException w) {
						// TODO Auto-generated catch block
						w.printStackTrace();
					}
					e.printStackTrace();
				}
				
				attempts++;
			}
			
			// esperar 8 segundos e fazer update
			
			writeMessageText("Bets place called. Wait 3 seconds", Color.BLUE);
			for(int i=0;i<8;i++)
			{
				writeMessageText("Waited "+3+" seconds", Color.BLUE);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			writeMessageText("Wait Complete", Color.GREEN);
			
			if(betResult==null)
			{
				writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed", Color.RED);
				return 3;
			}
			else
			{
				for(int i=0;i<betResult.length;i++)
				{
					if (betResult[i].getSuccess()) {
						writeMessageText("Bet Id:" + betResult[i].getBetId()
								+ " placed : " + betResult[i].getSizeMatched()
								+ " matched @ "
								+ betResult[i].getAveragePriceMatched(),Color.GREEN);
					} else {
					//TODO TEST DESTE ERRO
				
						writeMessageText("Failed to place bet: Problem was: "
								+ betResult[i].getResultCode(),Color.RED);
								return 4;
					}
				}
			}
		}//*/
		
		return 0;
	}
	
	public PlaceBets createPlaceBackBet(double odd,double size)
	{
		writeMessageText("Create PLACE BETS:"+size+"@"+odd, Color.BLUE);
		PlaceBets bet = new PlaceBets();
		bet.setMarketId(correctScoreMarket.getMarketId());
		bet.setSelectionId(marketRunnerActualResult.getSelectionId());
		bet.setBetCategoryType(BetCategoryTypeEnum.E);
		bet.setBetPersistenceType(BetPersistenceTypeEnum.NONE);
		bet.setBetType(BetTypeEnum.Factory.fromValue("B"));
		bet.setPrice(odd);
		bet.setSize(size);
		return bet;
	}
	
	public PlaceBets createPlaceLayBet(double odd,double size)
	{
		writeMessageText("Create PLACE BETS:"+size+"@"+odd, Color.BLUE);
		PlaceBets bet = new PlaceBets();
		bet.setMarketId(correctScoreMarket.getMarketId());
		bet.setSelectionId(marketRunnerActualResult.getSelectionId());
		bet.setBetCategoryType(BetCategoryTypeEnum.E);
		bet.setBetPersistenceType(BetPersistenceTypeEnum.NONE);
		bet.setBetType(BetTypeEnum.Factory.fromValue("L"));
		bet.setPrice(odd);
		bet.setSize(size);
		return bet;
	}
	
			
	
	public int cancelAllBetsInMarket()
	{
		
		
		int ret =0;
		
		if(unmatchedBetsBack.size()==0)
		{
			writeMessageText("NO Bets to Cancel (all matched or no bets were placed)", Color.ORANGE);
			return ret;
		}
		
		
		CancelBets canc[] = new CancelBets[unmatchedBetsBack.size()];
		//bets.get(0).getBetStatus()
		//BetStatusEnum.
		
		for(int i=0;i<canc.length;i++)
		{
			canc[i]=new CancelBets();
			canc[i].setBetId(unmatchedBetsBack.get(i).getBetId());
			
		}
		
		// We can ignore the array here as we only sent in one bet.
		CancelBetsResult betResult[] = null;
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
			attempts++;
			try {
				betResult = ExchangeAPI.cancelBets(CorrectScoreMainFrame.selectedExchange,
						CorrectScoreMainFrame.apiContext, canc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				writeMessageText(e.getMessage(), Color.RED);
				e.printStackTrace();
				ret=-1;
			}
			if(betResult==null)
			{
				writeMessageText("CRITICAL ERROR IN CACEL BETS: ATEMPT "+attempts, Color.RED);
				
			}
			
		}
		
		if(betResult==null)
		{
			writeMessageText("!!!CRITICAL ERROR!!! IN CACEL BETS", Color.RED);
			csmf.writeMessageText("!!!CRITICAL ERROR!!! IN CACEL BETS", Color.RED);
			System.err.println("!!!CRITICAL ERROR!!! IN CACEL BETS");
			System.out.println("!!!CRITICAL ERROR!!! IN CACEL BETS");
			return -1;
			//System.exit(-1);
		}
		
		
		for(int i =0; i<betResult.length;i++)
		{
			if (betResult[i].getSuccess()) {
				writeMessageText("Bet "+betResult[i].getBetId()+" cancelled.",Color.BLUE);
			} else {
				writeMessageText("Failed to cancel bet: Problem was: "+betResult[i].getResultCode()+" For bet:"+betResult[i].getBetId(),Color.RED);
				ret=-2;
			}
		}
	
		return ret;
	}
	
	public int placeLowSizeLayBet(double price,double size)
	{
		writeMessageText("## Initiating process for making bet :"+ size +" @ "+price,Color.ORANGE);
		
		writeMessageText("Placing Originl BET", Color.BLUE);
		if(size<0)
		{
			writeMessageText("placeLowSizeLayBet() size <0  :"+size,Color.BLUE);
			return -1;
		}
		
		int retPlaceOrig= placeOrigLowLayBet();
		if(retPlaceOrig!=0)
		{
			writeMessageText("ERROR placing Orig Low Lay Bet", Color.RED);
			return retPlaceOrig;
		}
		
		writeMessageText("Placing Originl BET", Color.BLUE);
		double sizeBF=Utils.convertAmountToBF(size);
		int ret=changeOrigLowLayBetSize(origLowLayBetId,sizeBF);
		
		if(ret!=0)
		{
			writeMessageText("ERROR changeOrigLowLayBetSize()", Color.RED);
			return ret;
		}
		
		try {
			cancelBetID(origLowLayBetId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ret = changeOrigLowLayBetPrice(newSizeLowLayBetId,price,sizeBF);
		if(ret!=0)
		{
			writeMessageText("ERROR changeOrigLowLayBetPrice()", Color.RED);
			return ret;
		}
		writeMessageText("Bets place called. Wait 3 seconds", Color.BLUE);
		for(int i=0;i<8;i++)
		{
			writeMessageText("Waited "+3+" seconds", Color.BLUE);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		writeMessageText("Wait Complete", Color.GREEN);
		
		writeMessageText("## Ended the process for making bet :"+ size +" @ "+price,Color.ORANGE);
		return 0;
		
	}
	
	long newPriceLowLayBetId=0;
	public int changeOrigLowLayBetPrice(long id,double price,double size)
	{
		newPriceLowLayBetId=0;
		
		writeMessageText("changeOrigLowLayBet id:"+id+"  Size:"+size,Color.BLUE);
		UpdateBets upd = new UpdateBets(); 
		upd.setBetId(id);
		upd.setOldBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setOldPrice(1.01);
		upd.setOldSize(size);
		upd.setNewBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setNewPrice(price);
		upd.setNewSize(size);
		
		UpdateBetsResult betResult=null;
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
				writeMessageText("ExchangeAPI.placeBets (Low Orig LAY Update) Attempt :"+attempts, Color.BLUE);
			try {
				betResult = ExchangeAPI.updateBets(CorrectScoreMainFrame.selectedExchange,
						CorrectScoreMainFrame.apiContext, new UpdateBets[] {upd})[0];
			} catch (Exception e) {
				writeMessageText(e.getMessage(), Color.RED);
				if(e.getMessage().contains(new String("EVENT_SUSPENDED")) || e.getMessage().contains(new String("BET_IN_PROGRESS")) )
				{
					writeMessageText("ExchangeAPI.placeBets Returned NULL: Market is supended",Color.BLUE);
					attempts--;
				}
				// TODO Auto-generated catch block
				writeMessageText("ExchangeAPI.updateBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
				writeMessageText("Wait 1s..."+attempts, Color.BLUE);
				try {
					
					Thread.sleep(1000);
				} catch (InterruptedException w) {
					// TODO Auto-generated catch block
					w.printStackTrace();
				}
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (betResult.getSuccess()) {
			Display.println("Bet "+betResult.getBetId()+" New Bet ID:"+betResult.getNewBetId() +" updated. New bet is "+betResult.getNewSize() +" @ "+betResult.getNewPrice());
			 newPriceLowLayBetId=betResult.getNewBetId();
		} else {
			Display.println("changeOrigLowLayBetPrice() - Failed to update bet: Problem was: "+betResult.getResultCode());
			return -1;
		}
		
		return 0;
	}
	
	long newSizeLowLayBetId=0;
	public int changeOrigLowLayBetSize(long id,double size)
	{
		newSizeLowLayBetId=0;
		writeMessageText("changeOrigLowLayBet() id:"+id+"  Size:"+size,Color.BLUE);
		UpdateBets upd = new UpdateBets(); 
		upd.setBetId(id);
		upd.setOldBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setOldPrice(1.01);
		upd.setOldSize(2.0);
		upd.setNewBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setNewPrice(1.01);
		upd.setNewSize(Utils.convertAmountToBF(2.0+size));
		
		UpdateBetsResult betResult=null;
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
				writeMessageText("ExchangeAPI.placeBets (Low Orig LAY Update) Attempt :"+attempts, Color.BLUE);
			try {
				betResult = ExchangeAPI.updateBets(CorrectScoreMainFrame.selectedExchange,
						CorrectScoreMainFrame.apiContext, new UpdateBets[] {upd})[0];
			} catch (Exception e) {
				writeMessageText(e.getMessage(), Color.RED);
				if(e.getMessage().contains(new String("EVENT_SUSPENDED"))  || e.getMessage().contains(new String("BET_IN_PROGRESS")) )
				{
					writeMessageText("ExchangeAPI.placeBets Returned NULL: Market is supended | Bet in progress",Color.BLUE);
					attempts--;
				}
				// TODO Auto-generated catch block
				writeMessageText("ExchangeAPI.updateBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
				writeMessageText("Wait 1s..."+attempts, Color.BLUE);
				try {
					
					Thread.sleep(1000);
				} catch (InterruptedException w) {
					// TODO Auto-generated catch block
					w.printStackTrace();
				}
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			attempts++;
		}
		
		if (betResult.getSuccess()) {
			Display.println("Bet "+betResult.getBetId()+" New Bet ID:"+betResult.getNewBetId() +" updated. New bet is "+betResult.getNewSize() +" @ "+betResult.getNewPrice());
			newSizeLowLayBetId=betResult.getNewBetId();
		} else {
			Display.println("changeOrigLowLayBetSize() - Failed to update bet: Problem was: "+betResult.getResultCode());
			return -1;
		}
		
		return 0;
	}
	
	long origLowLayBetId=0;
	public int placeOrigLowLayBet()
	{
		origLowLayBetId=0;
		writeMessageText("Creating (and PLACE!!)Low  lay bet:"+ 2.0 + " @ "+ 1.01 , Color.RED);
		PlaceBets bet=createPlaceLayBet(1.01,2.0);
		PlaceBets[] bets=new PlaceBets[1];
		bets[0]=bet;
		PlaceBetsResult[] betResult=null;
		
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
			writeMessageText("ExchangeAPI.placeBets (LAY) Attempt :"+attempts, Color.BLUE);
			try {
				betResult=ExchangeAPI.placeBets(CorrectScoreMainFrame.selectedExchange, CorrectScoreMainFrame.apiContext, bets);
			} catch (Exception e) {
				writeMessageText(e.getMessage(), Color.RED);
				if(e.getMessage().contains(new String("EVENT_SUSPENDED")) || e.getMessage().contains(new String("BET_IN_PROGRESS")))
				{
					writeMessageText("ExchangeAPI.placeBets Returned NULL: Market is supended | Bet in progress",Color.BLUE);
					attempts--;
				}
				// TODO Auto-generated catch block
				e.printStackTrace();
				writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
				writeMessageText("Wait 1s..."+attempts, Color.BLUE);
				try {
					
					Thread.sleep(1000);
				} catch (InterruptedException w) {
					// TODO Auto-generated catch block
					w.printStackTrace();
				}
			}
			
			attempts++;
		}
		
		if(betResult==null)
		{
			writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed", Color.RED);
			writeMessageText("Place Bet Lay (placeCloseBetLay() function) end: return 3;",Color.BLUE);
			return 3;
		}
		else
		{
			for(int i=0;i<betResult.length;i++)
			{
				if (betResult[i].getSuccess()) {
					writeMessageText("Bet Id:" + betResult[i].getBetId()
							+ " placed : " + betResult[i].getSizeMatched()
							+ " matched @ "
							+ betResult[i].getAveragePriceMatched(),Color.GREEN);
					origLowLayBetId=betResult[i].getBetId();
				} else {
				//TODO TEST DESTE ERRO
			
					writeMessageText("Failed to place bet: Problem was: "
							+ betResult[i].getResultCode(),Color.RED);
							return 4;
				}
			}
		}
		
		writeMessageText("Place Low Bet Lay (placeOrigLowLayBet function) end: return 0;",Color.BLUE);
		
		//*/
		//@TODO Variaveis para update...
		 
		return 0;
	}
		
		
	
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
	public String getTimeStamp(long time) {
		return dateFormat.format(new Date(time));
	}
	// -----------------------thread -------------------------------------

	public class MarketThread extends Object implements Runnable {
		private volatile boolean stopRequested;

		private Thread runThread;

		public void run() {
			runThread = Thread.currentThread();
			stopRequested = false;

			while (!stopRequested) {
				try {
					// refresh(); /// connect and get the data
					process();

				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(updateInterval);
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}

		public void stopRequest() {
			stopRequested = true;

			if (runThread != null) {
				runThread.interrupt();

				// suspend()stop();
			}
		}
	}

	public void startPolling() {
		if (polling)
			return;
		as = new MarketThread();
		t = new Thread(as);
		t.start();

		polling = true;
	}

	public void stopPolling() {
		if (!polling)
			return;
		as.stopRequest();
		polling = false;

	}

	// ---------------------------------------------------

}
