package bets;

import generated.exchange.BFExchangeServiceStub.GetMarket;

import java.util.Calendar;

import DataRepository.OddData;
import DataRepository.RunnersData;

public class BetData {
	
	public RunnersData rd;
	
	protected double oddRequested;
	protected double amount=0;
	
	//--dynamic change variables-- 
	protected double oddMached=-1;
	protected double matchedAmount=0;
	protected int state=BetData.NOT_PLACED;
	
	protected int lastState=BetData.NOT_PLACED;
	protected int transition=BetData.SYSTEM;
	protected boolean passedOnGetMUBetsUpdate=false;
	//----------------------------
	
	// Transitions
	public static final int SYSTEM = 0;
	public static final int PLACE = 1;
	public static final int CANCEL = 2;
	
	protected Calendar timestampPlace=null;
	protected Calendar timestampCancel=null;
	protected Calendar timestampFinalState=null;
	
	public boolean keepInPlay=false;   // BetPersistenceType (IP)
	
	public int type=BetData.BACK;
	
	public static final int BACK = 1;
	public static final int LAY = 2;

	// Error processing
	protected int errorType=BetData.ERROR_NONE;
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
	
	protected int updatesBetInProgress=0;
	
	//public static final int CANCEL_ERROR = -2;	    // after cancel error  (check error)
	public static final int PLACING_ERROR = -1;     // after placed error (check error)
	public static final int NOT_PLACED = 0;         // before place is called on this bet
	public static final int PLACING = 1;            // in thread waiting for bf server count down  
	public static final int BET_IN_PROGRESS = 2;    // waiting to recover betId by BetManager 
	public static final int UNMATCHED = 3;          // Waiting to be Matched
	public static final int PARTIAL_MATCHED = 4;     // Partial Matched waiting to be Completely Matched
	public static final int MATCHED =  5;           // Completely matched 
	public static final int CANCELED = 6;           // Nothing Matched and Canceled 
	public static final int PARTIAL_CANCELED = 7;   // Partial Matched then Canceled
	public static final int CANCEL_WAIT_UPDATE = 8; // If getBet() after Cancel() does not work it will use normal update
	public static final int UNMONITORED = 9;        // BetManager stop updates on this bet
	
	
	protected Long BetID=null; // given from betfair after placed 
	
	//To use in Simulation (and in real to get estimated place on queue)
	protected double entryVolume=-1;
	protected double entryAmount=-1;
	
	protected double LastAvailableAmount=0;
	protected double LastVolumeUpdate=0;
	
	
	
	public BetData (RunnersData rdA, double  amountA,double  oddA, int typeA,boolean IPA)
	{
		this.rd=rdA;
		this.amount=amountA;
		this.oddRequested=oddA;
		this.type=typeA;
		this.keepInPlay=IPA;
	}
	
	public BetData (RunnersData rdA, OddData od,boolean keepIP)
	{
		this.rd=rdA;
		this.amount=od.getAmount();
		this.oddRequested=od.getOdd();
		this.type=od.getType();
		this.keepInPlay=keepIP;
	}
	
	public BetData (OddData od,boolean keepIP)
	{
		this.rd=od.getRd();
		this.amount=od.getAmount();
		this.oddRequested=od.getOdd();
		this.type=od.getType();
		this.keepInPlay=keepIP;
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

	protected void setMatchedAmount(double corresponded) {
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
			setTimestampFinalState(getRd().getMarketData().getCurrentTime());
		
		if(this.state == BetData.CANCELED || this.state == BetData.PARTIAL_CANCELED)
			setTimestampCancel(getRd().getMarketData().getCurrentTime());
	}

	public Long getBetID() {
		return BetID;
	}

	protected void setBetID(Long betID) {
		BetID = betID;
	}
	
	public Calendar getTimestampPlace() {
		return timestampPlace;
	}

	protected void setTimestampPlace(Calendar timestamp) {
		this.timestampPlace = timestamp;
	}

	public Calendar getTimestampCancel() {
		return timestampCancel;
	}

	protected void setTimestampCancel(Calendar timestampCancel) {
		this.timestampCancel = timestampCancel;
	}

	public Calendar getTimestampFinalState() {
		return timestampFinalState;
	}

	protected void setTimestampFinalState(Calendar timestampFinalState) {
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

	protected void setOddMached(double oddMached) {
		this.oddMached = oddMached;
	}
	
	public boolean isKeepInPlay() {
		return keepInPlay;
	}

	public void setKeepInPlay(boolean keepInPlay) {
		this.keepInPlay = keepInPlay;
	}

	protected void setEntryVolume(double entryVolume) {
		this.entryVolume = entryVolume;
	}
	
	public double getEntryVolume() {
		return entryVolume;
	}

	public double getEntryAmount() {
		return entryAmount;
	}
	
	protected void setEntryAmount(double entryAmount) {
		this.entryAmount = entryAmount;
	}
	public int getErrorType() {
		return errorType;
	}

	protected void setErrorType(int errorType) {
		this.errorType = errorType;
	}

	public int getLastState() {
		return lastState;
	}

	protected int getTransition() {
		return transition;
	}
	
	protected void setTransition(int transition) {
		this.transition = transition;
	}

	protected double getLastAvailableAmount() {
		return LastAvailableAmount;
	}

	protected void setLastAvailableAmount(double lastAvailableAmount) {
		LastAvailableAmount = lastAvailableAmount;
	}

	protected double getLastVolumeUpdate() {
		return LastVolumeUpdate;
	}

	protected void setLastVolumeUpdate(double lastVolumeUpdate) {
		LastVolumeUpdate = lastVolumeUpdate;
	}

	public OddData getOddDataMatched()
	{
		return new OddData(getOddMached(), getMatchedAmount(),getType(),getRd());
	}
	
	public OddData getOddDataOriginal()
	{
		return new OddData(getOddRequested(), getAmount(),getType(),getRd());
	}
	
	protected boolean isPassedOnGetMUBetsUpdate() {
		return passedOnGetMUBetsUpdate;
	}

	protected void setPassedOnGetMUBetsUpdate(boolean passedOnGetMUBetsUpdate) {
		this.passedOnGetMUBetsUpdate = passedOnGetMUBetsUpdate;
	}

	
}
