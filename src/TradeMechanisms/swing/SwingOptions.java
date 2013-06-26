package TradeMechanisms.swing;

import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import bets.BetData;

public class SwingOptions {
	TradeMechanismListener defaultListener=null;
	BetData betOpenInfo;
	int waitFramesOpen=20;
	int waitFramesNormal=20;
	int waitFramesBestPrice=10;
	int ticksProfit=1;
	int ticksLoss=1;
	int updateInterval=TradeMechanism.SYNC_MARKET_DATA_UPDATE;
	boolean forceCloseOnStopLoss=true;
	boolean useStopProfifInBestPrice=false;
	boolean goOnfrontInBestPrice=false;
	int delayBetweenOpenClose=-1;
	int delayIgnoreStopLoss=-1;
	double percentageOpen=1.00;
	boolean insistOpen=false;
	int waitFramesLay1000=100;
	

	public SwingOptions(BetData betOpenInfoA) {
		betOpenInfo=betOpenInfoA;
	}
	
	public SwingOptions(BetData betOpenInfoA,TradeMechanismListener defaultListenerA) {
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

	public int getWaitFramesOpen() {
		return waitFramesOpen;
	}

	public void setWaitFramesOpen(int waitFramesOpen) {
		this.waitFramesOpen = waitFramesOpen;
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

	public int getTicksProfit() {
		return ticksProfit;
	}

	public void setTicksProfit(int ticksProfit) {
		this.ticksProfit = ticksProfit;
	}

	public int getTicksLoss() {
		return ticksLoss;
	}

	public void setTicksLoss(int ticksLoss) {
		this.ticksLoss = ticksLoss;
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

	public boolean isUseStopProfifInBestPrice() {
		return useStopProfifInBestPrice;
	}

	public void setUseStopProfifInBestPrice(boolean useStopProfifInBestPrice) {
		this.useStopProfifInBestPrice = useStopProfifInBestPrice;
	}

	public boolean isGoOnfrontInBestPrice() {
		return goOnfrontInBestPrice;
	}

	public void setGoOnfrontInBestPrice(boolean goOnfrontInBestPrice) {
		this.goOnfrontInBestPrice = goOnfrontInBestPrice;
	}

	public int getDelayBetweenOpenClose() {
		return delayBetweenOpenClose;
	}

	public void setDelayBetweenOpenClose(int delayBetweenOpenClose) {
		this.delayBetweenOpenClose = delayBetweenOpenClose;
	}

	public int getDelayIgnoreStopLoss() {
		return delayIgnoreStopLoss;
	}

	public void setDelayIgnoreStopLoss(int delayIgnoreStopLossA) {
		this.delayIgnoreStopLoss = delayIgnoreStopLossA;
	}

	public double getPercentageOpen() {
		return percentageOpen;
	}

	public void setPercentageOpen(double percentageOpen) {
		this.percentageOpen = percentageOpen;
	}

	public boolean isInsistOpen() {
		return insistOpen;
	}

	public void setInsistOpen(boolean insistOpen) {
		this.insistOpen = insistOpen;
	}
	
	public int getWaitFramesLay1000() {
		return waitFramesLay1000;
	}

	public void setWaitFramesLay1000(int waitFramesLay1000) {
		this.waitFramesLay1000 = waitFramesLay1000;
	}
}
