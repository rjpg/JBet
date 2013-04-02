package bets;

import java.util.Vector;

import DataRepository.MarketData;
import DataRepository.RunnersData;

public abstract class  BetManager {
	
	// THREAD
	public static int SYNC_MARKET_DATA_UPDATE = 0;
	
	
	
	//Market
	public MarketData md;
	
	public BetManager(MarketData mdA) {
		md=mdA;
	}
	
	/**
	 * 
	 * @return the monitored Market  
	 */
	public MarketData getMd() {
		return md;
	}
	
	/**
	 * Make polling = true; 
	 */
	public abstract void startPolling();
	
	/**
	 * Make polling = false;
	 */
	public abstract void stopPolling();
	
	public abstract boolean  isPolling();
	
	public abstract void setPollingInterval (int milis);
	
	/**
	 * 
	 * @return All bets that entered the manager by placeBet()
	 */
	public abstract Vector<BetData> getBets();
	
	
	/**
	 * 
	 * @param rd - Runner 
	 * @return All bets placed on rd
	 */
	public abstract Vector<BetData> getBetsByRunner(RunnersData rd);
	
	/**
	 * 
	 * @param ID - Id of bet
	 * @return return the bet with ID
	 */
	public abstract BetData getBetById(long ID);
	
	/**
	 * After a bet is placed the BetData fields will be altered by the manager. After placed the BetData should be read Only
	 * 
	 * @param bet - BetData
	 * @return 
	 * 0 All placed Ok
	 * -1 Nothing placed
	 * -2 At least some not places 
	 */
	public abstract void placeBet(BetData bet);
	
	/**
	 * After a bet is placed the BetData fields will be altered by the manager. After placed the BetData should be read Only 
	 * 
	 * @param place - vector of BetData
	 * After processing the BetData.PLACING state if there is an error the state of the bet will be BetData.PLACING_ERROR 
	 * and the cause of the error can be debug using BetData.getErrorType()  
	 * 
	 */
	public abstract void placeBets(Vector<BetData> place);
	
	/**
	 * 
	 * @param bet - BetData
	 * @return 
	 * 0 canceled Ok
	 * -1 Nothing Canceled
	 * -2 At least some not Canceled 
	 */
	public abstract int cancelBet(BetData bet);
	
	
	/**
	 * 
	 * @param place - vector of BetData
	 * @return 
	 * 0 All canceled Ok
	 * -1 Nothing Canceled
	 * -2 At least some not Canceled 
	 */
	public abstract int cancelBets(Vector<BetData> cancelBets);
	
	/**
	 * Clean memory and make all bets became UNMONITORED
	 */
	public abstract void clean();
	
}
