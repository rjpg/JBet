package bets;

import java.util.Calendar;

import DataRepository.RunnersData;

public class BetData {
	
	public RunnersData rd;
	
	public double oddRequested;
	public double amount=0;
	
	//--dynamic change variables-- 
	public double oddMached=-1;
	public double matchedAmount=0;
	public int state=BetData.NOT_PLACED;
	
	public int lastState=BetData.NOT_PLACED;
	public int transition=BetData.SYSTEM;
	//----------------------------

	
	// Transitions
	public static final int SYSTEM = 0;
	public static final int PLACE = 1;
	public static final int CANCEL = 2;
	
	public Calendar timestampPlace=null;
	public Calendar timestampCancel=null;
	public Calendar timestampFinalState=null;
	
	public boolean keepInPlay=false;   // BetPersistenceType (IP)
	
	public int type=BetData.BACK;
	
	public static final int BACK = 1;
	public static final int LAY = 2;

	// Error processing
	public int errorType=BetData.ERROR_NONE;
	// place erros 
	public static final int ERROR_NONE = 0;
	public static final int ERROR_MARKET_SUSPENDED = 1;
	public static final int ERROR_MARKET_CLOSED = 2;
	public static final int ERROR_BALANCE_EXCEEDED=3;
	public static final int ERROR_BET_IN_PROGRESS=4;
	// cancel errors
	//public static final int ERROR_TAKEN_OR_LAPSED = 5;
	//public static final int ERROR_NOT_CANCELED= 5;
	// generic error
	public static final int ERROR_UNKNOWN = 6;
	
	public int updatesBetInProgress=0;
	
	//public static final int CANCEL_ERROR = -2;	 	 // after cancel error  (check error)
	public static final int PLACING_ERROR = -1;      // after placed error (check error)
	public static final int NOT_PLACED = 0;          // before placed 
	public static final int BET_IN_PROGRESS = 1;     // waiting 
	public static final int UNMATCHED = 2;            //
	public static final int PARTIAL_MACHED = 3;
	public static final int MATCHED =  4;
	public static final int CANCELED = 5;
	public static final int PARTIAL_CANCELED = 6;
	
	
	public Long BetID=null; // given from betfair after placed 
	
	//Simulation
	public double entryVolume=-1;
	public double entryAmount=-1;
	
	
	
	public BetData (RunnersData rdA, double  amountA,double  oddA, int typeA,boolean IPA)
	{
		this.rd=rdA;
		this.amount=amountA;
		this.oddRequested=oddA;
		this.type=typeA;
		this.keepInPlay=IPA;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getMatchedAmount() {
		return matchedAmount;
	}

	public void setMatchedAmount(double corresponded) {
		this.matchedAmount = corresponded;
	}

	public int getState() {
		return state;
	}

	public void setState(int stateA,int transitionA) {
		
		if(stateA==this.state) return;
		this.lastState=this.state;
		this.state = stateA;
		this.transition=transitionA;
		
		if(BetUtils.isBetFinalState(this.state))
			setTimestampFinalState(Calendar.getInstance());
	}

	public Long getBetID() {
		return BetID;
	}

	public void setBetID(Long betID) {
		BetID = betID;
	}
	
	public Calendar getTimestampPlace() {
		return timestampPlace;
	}

	public void setTimestampPlace(Calendar timestamp) {
		this.timestampPlace = timestamp;
	}

	public Calendar getTimestampCancel() {
		return timestampCancel;
	}

	public void setTimestampCancel(Calendar timestampCancel) {
		this.timestampCancel = timestampCancel;
	}

	public Calendar getTimestampFinalState() {
		return timestampFinalState;
	}

	public void setTimestampFinalState(Calendar timestampFinalState) {
		this.timestampFinalState = timestampFinalState;
	}

	
	public RunnersData getRd() {
		return rd;
	}

	public void setRd(RunnersData rd) {
		this.rd = rd;
	}

	public double getOddRequested() {
		return oddRequested;
	}

	public void setOddRequested(double oddRequested) {
		this.oddRequested = oddRequested;
	}

	public double getOddMached() {
		return oddMached;
	}

	public void setOddMached(double oddMached) {
		this.oddMached = oddMached;
	}
	
	public boolean isKeepInPlay() {
		return keepInPlay;
	}

	public void setKeepInPlay(boolean keepInPlay) {
		this.keepInPlay = keepInPlay;
	}

	public void setEntryVolume(double entryVolume) {
		this.entryVolume = entryVolume;
	}
	
	public double getEntryVolume() {
		return entryVolume;
	}

	public double getEntryAmount() {
		return entryAmount;
	}
	
	public void setEntryAmount(double entryAmount) {
		this.entryAmount = entryAmount;
	}
	public int getErrorType() {
		return errorType;
	}

	public void setErrorType(int errorType) {
		this.errorType = errorType;
	}

	public int getLastState() {
		return lastState;
	}

	public int getTransition() {
		return transition;
	}
	
	public void setTransition(int transition) {
		this.transition = transition;
	}

}
