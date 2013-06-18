package TradeMechanisms.dutchingChase;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import TradeMechanisms.TradeMechanism;
import bets.BetData;

import DataRepository.RunnersData;

public class DutchingChaseOptionsPanel extends JPanel{
	
	//Not optional by the user
	RunnersData rd;
	double odd=1000;
	double stake=2.00;
	//--------------------------
	
	public JCheckBox checkIP;
	

	/*int stopLossTicks=1;
	int waitFramesNormal=20;
	int waitFramesBestPrice=10;
	int updateInterval=TradeMechanism.SYNC_MARKET_DATA_UPDATE;
	boolean forceCloseOnStopLoss=true;
	boolean useStopProfitInBestPrice=false;
	boolean goOnfrontInBestPrice=false;
	int startDelay=-1;
	int ignoreStopLossDelay=-1;*/
	
	
	public DutchingChaseOptionsPanel(RunnersData rdA) {
		rd=rdA;
		
	}
	
	

}
