package TradeMechanisms.dutchingChase;

import com.sun.org.omg.CORBA.OpDescriptionSeqHelper;

import DataRepository.OddData;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import bets.BetData;

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
	
	public DutchingChaseOptions(OddData odCloseInfoA) {
		odCloseInfo=odCloseInfoA;
	}
	
	
	
}
