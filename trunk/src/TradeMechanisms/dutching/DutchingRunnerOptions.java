package TradeMechanisms.dutching;

import DataRepository.RunnersData;
import bets.BetData;

public class DutchingRunnerOptions {

	private boolean open=false;
	private RunnersData rd=null;

	// open type
	private BetData bet=null;
	private int timeWaitOpen=10;
			
	// close type and also open type (in case of not completed open)
	private int timeHoldForceClose=10;
	private boolean forceClose=false;
		
	public DutchingRunnerOptions() {
			
	}
	
	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public int getTimeWaitOpen() {
		return timeWaitOpen;
	}

	public void setTimeWaitOpen(int timeWaitOpen) {
		this.timeWaitOpen = timeWaitOpen;
	}

	public int getTimeHoldForceClose() {
		return timeHoldForceClose;
	}

	public void setTimeHoldForceClose(int timeHoldForceClose) {
		this.timeHoldForceClose = timeHoldForceClose;
	}

	public boolean isForceClose() {
		return forceClose;
	}

	public void setForceClose(boolean forceClose) {
		this.forceClose = forceClose;
	}

	public BetData getBet() {
		return bet;
	}

	
}
