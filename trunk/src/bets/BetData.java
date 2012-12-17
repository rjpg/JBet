package bets;

import generated.exchange.BFExchangeServiceStub.BetPersistenceTypeEnum;

import java.util.Calendar;

import DataRepository.RunnersData;
import DataRepository.TradeMecanism;
import bots.Bot;

public class BetData {
	
	public BetListener owner=null;
	
	public RunnersData rd;
	
	public BetPersistenceTypeEnum persistenceType = BetPersistenceTypeEnum.NONE;
	
	public double oddRequested;
	public double amount=0;
	
	//dynamic change variables 
	public double oddMached=-1;
	public double matchedAmount=0;
	public int state=BetData.NOT_PLACED;
	
	
	public Calendar timestamp=null;
	
	public boolean keepInPlay=false;
	
	public int type=1;
	
	public static final int BACK = 1;
	public static final int LAY = 2;

	
	
	//public boolean PLACED = false; // true if the bet was placed on betfair
	public int updatesBetInProgress=0;
	
	public static final int PLACING_ERROR = -1;      // after placed error
	public static final int NOT_PLACED = 0;          // before placed 
	public static final int BET_IN_PROGRESS = 1;     // waiting 
	public static final int UNMATHED = 2;            //
	public static final int PARTIAL_MACHED = 3;
	public static final int MATHED =  4;
	public static final int CANCELED = 5;
	
	
	public Long BetID=null; // given from betfair after placed 
	
	//Simulation
	public double entryVolume=-1;
	public double entryAmount=-1;
	
	public double getEntryVolume() {
		return entryVolume;
	}

	
	public BetData (BetListener tmA, RunnersData rdA, double  amountA,double  oddA, int typeA, Calendar time)
	{
		this.owner=tmA;
		this.rd=rdA;
		this.amount=amountA;
		this.oddRequested=oddA;
		this.type=typeA;
		this.timestamp=time;
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

	public void setState(int state) {
		this.state = state;
	}

	public Long getBetID() {
		return BetID;
	}

	public void setBetID(Long betID) {
		BetID = betID;
	}
	
	public Calendar getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}
	
	public RunnersData getRd() {
		return rd;
	}

	public void setRd(RunnersData rd) {
		this.rd = rd;
	}

	public BetPersistenceTypeEnum getPersistenceType() {
		return persistenceType;
	}

	public void setPersistenceType(BetPersistenceTypeEnum betPersistenceType) {
		this.persistenceType = betPersistenceType;
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
	
	public BetListener getOwner() {
		return owner;
	}

	public void setOwner(BetListener owner) {
		this.owner = owner;
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
	
	public double getEntryAmount() {
		return entryAmount;
	}

	public void setEntryAmount(double entryAmount) {
		this.entryAmount = entryAmount;
	}

}
