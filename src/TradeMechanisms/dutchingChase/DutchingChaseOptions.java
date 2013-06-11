package TradeMechanisms.dutchingChase;


import DataRepository.OddData;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.close.ClosePosition;



public class DutchingChaseOptions {
	
	OddData odCloseInfo;

	int stopLossTicks=1;
	int waitFramesNormal=20;
	int waitFramesBestPrice=10;
	int updateInterval=TradeMechanism.SYNC_MARKET_DATA_UPDATE;
	boolean forceCloseOnStopLoss=true;
	boolean useStopProfitInBestPrice=false;
	boolean goOnfrontInBestPrice=false;
	int startDelay=-1;
	int ignoreStopLossDelay=-1;
	boolean keepIP=false;	
	protected ClosePosition close=null;
	
	public DutchingChaseOptions(OddData odCloseInfoA) {
		odCloseInfo=odCloseInfoA;
	}

	public OddData getOdCloseInfo() {
		return odCloseInfo;
	}

	public void setOdCloseInfo(OddData odCloseInfo) {
		this.odCloseInfo = odCloseInfo;
	}

	public int getStopLossTicks() {
		return stopLossTicks;
	}

	public void setStopLossTicks(int stopLossTicks) {
		this.stopLossTicks = stopLossTicks;
	}

	public int getWaitFramesNormal() {
		return waitFramesNormal;
	}

	public void setWaitFramesNormal(int waitFramesNormal) {
		this.waitFramesNormal = waitFramesNormal;
	}

	public int getWaitFramesBestPrice() {
		return waitFramesBestPrice;
	}

	public void setWaitFramesBestPrice(int waitFramesBestPrice) {
		this.waitFramesBestPrice = waitFramesBestPrice;
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

	public void setUseStopProfitInBestPrice(boolean useStopProfitInBestPrice) {
		this.useStopProfitInBestPrice = useStopProfitInBestPrice;
	}

	public boolean isGoOnfrontInBestPrice() {
		return goOnfrontInBestPrice;
	}

	public void setGoOnfrontInBestPrice(boolean goOnfrontInBestPrice) {
		this.goOnfrontInBestPrice = goOnfrontInBestPrice;
	}

	public int getStartDelay() {
		return startDelay;
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
	
	public boolean isKeepIP() {
		return keepIP;
	}

	public void setKeepIP(boolean keepIP) {
		this.keepIP = keepIP;
	}
	
	protected ClosePosition getClose() {
		return close;
	}

	protected void setClose(ClosePosition close) {
		this.close = close;
	}
}
