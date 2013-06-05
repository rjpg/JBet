package TradeMechanisms.open;

import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import bets.BetData;

public class OpenPositionOptions {
	
	TradeMechanismListener defaultListener=null;
	BetData betOpenInfo; 
	int waitFrames=20;
	int updateInterval=TradeMechanism.SYNC_MARKET_DATA_UPDATE;
	boolean insistOpen=false;
	double percentageOpen=1.00;
	
	
	public OpenPositionOptions(BetData betOpenInfoA) {
		betOpenInfo=betOpenInfoA;
	}
	
	public OpenPositionOptions(BetData betOpenInfoA,TradeMechanismListener defaultListenerA) {
		betOpenInfo=betOpenInfoA;
		defaultListener=defaultListenerA;
	}
	
	public TradeMechanismListener getDefaultListener() {
		return defaultListener;
	}

	public void setDefaultListener(TradeMechanismListener defaultListener) {
		this.defaultListener = defaultListener;
	}

	public BetData getBetOpenInfo() {
		return betOpenInfo;
	}

	public void setBetOpenInfo(BetData betOpenInfo) {
		this.betOpenInfo = betOpenInfo;
	}

	public int getWaitFrames() {
		return waitFrames;
	}

	public void setWaitFrames(int waitFrames) {
		this.waitFrames = waitFrames;
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public boolean isInsistOpen() {
		return insistOpen;
	}

	public void setInsistOpen(boolean insistOpen) {
		this.insistOpen = insistOpen;
	}

	public double getPercentageOpen() {
		return percentageOpen;
	}

	public void setPercentageOpen(double percentageOpen) {
		this.percentageOpen = percentageOpen;
	}

	
}
