package TradeMechanisms.trailingStop;

import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import bets.BetData;

public class ClosePositionTrailingStopOptions {
	
	TradeMechanismListener defaultListener=null;
	BetData betCloseInfo;
	int stopLossTicks=1;
	int waitFramesNormal=20;
	int waitFramesUntilForceClose=10;
	int updateInterval=TradeMechanism.SYNC_MARKET_DATA_UPDATE;
	boolean forceCloseOnStopLoss=true;
	boolean useStopProfitInBestPrice=false;
	boolean goOnfrontInBestPrice=false;
	int startDelay=-1;
	int ignoreStopLossDelay=-1;
	int waitFramesLay1000=100;
	
	int reference=TrailingStopOptions.REF_BEST_PRICE;
	int movingAverageSamples=1;
	
	int initialRelativeStopLossTicks=1;
	
	public ClosePositionTrailingStopOptions(BetData betCloseInfoA) {
		betCloseInfo=betCloseInfoA;
	}
	
	public ClosePositionTrailingStopOptions(BetData betCloseInfoA,TradeMechanismListener defaultListenerA) {
		betCloseInfo=betCloseInfoA;
		defaultListener=defaultListenerA;
	}
	
	public TradeMechanismListener getDefaultListener() {
		return defaultListener;
	}

	public void setDefaultListener(TradeMechanismListener defaultListener) {
		this.defaultListener = defaultListener;
	}

	public BetData getBetCloseInfo() {
		return betCloseInfo;
	}

	public void setBetCloseInfo(BetData betCloseInfo) {
		this.betCloseInfo = betCloseInfo;
	}

	public int getStartDelay() {
		return startDelay;
	}

	public int getMovingStopLossTicks() {
		return stopLossTicks;
	}

	public void setMovingStopLossTicks(int stopLossTicks) {
		this.stopLossTicks = stopLossTicks;
	}

	public int getWaitFramesNormal() {
		return waitFramesNormal;
	}

	public void setWaitFramesNormal(int waitFramesNormal) {
		this.waitFramesNormal = waitFramesNormal;
	}

	public int getWaitFramesUntilForceClose() {
		return waitFramesUntilForceClose;
	}

	public void setWaitFramesUntilForceClose(int waitFramesUntilForceClose) {
		this.waitFramesUntilForceClose = waitFramesUntilForceClose;
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public boolean isForceCloseOnStopLoss() {
		return forceCloseOnStopLoss;
	}

	public void setForceCloseOnStopLoss(boolean forceCloseOnStopLoss) {
		this.forceCloseOnStopLoss = forceCloseOnStopLoss;
	}

	public boolean isUseStopProfitInBestPrice() {
		return useStopProfitInBestPrice;
	}

	public void setUseStopProfitInBestPrice(boolean useStopProfifInBestPrice) {
		this.useStopProfitInBestPrice = useStopProfifInBestPrice;
	}

	public boolean isGoOnfrontInBestPrice() {
		return goOnfrontInBestPrice;
	}

	public void setGoOnfrontInBestPrice(boolean goOnfrontInBestPrice) {
		this.goOnfrontInBestPrice = goOnfrontInBestPrice;
	}

	public void setStartDelay(int startDelay) {
		this.startDelay = startDelay;
	}
	
	public int getIgnoreStopLossDelay() {
		return ignoreStopLossDelay;
	}

	public void setIgnoreStopLossDelay(int ignoreStopLossDelay) {
		this.ignoreStopLossDelay = ignoreStopLossDelay;
	}
	
	public int getWaitFramesLay1000() {
		return waitFramesLay1000;
	}

	public void setWaitFramesLay1000(int waitFramesLay1000) {
		this.waitFramesLay1000 = waitFramesLay1000;
	}

	public int getReference() {
		return reference;
	}

	public void setReference(int reference) {
		this.reference = reference;
	}

	public int getMovingAverageSamples() {
		return movingAverageSamples;
	}

	public void setMovingAverageSamples(int movingAverageSamples) {
		this.movingAverageSamples = movingAverageSamples;
	}
	
	public int getInitialRelativeStopLossTicks() {
		return initialRelativeStopLossTicks;
	}

	public void setInitialRelativeStopLossTicks(int initialRelativeStopLossTicks) {
		this.initialRelativeStopLossTicks = initialRelativeStopLossTicks;
	}

}
