package TradeMechanisms;

import java.util.Vector;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import bets.BetData;
import bets.BetManager;
import bets.BetManagerReal.BetsManagerThread;
import bots.Bot;

public class ClosePosition extends TradeMechanism implements MarketChangeListener{

	
	// States
	private static final int I_OPEN = 2;
	private static final int I_PARTIAL_CLOSED = 3;
	private static final int I_CLOSED = 4;
	private static final int I_CANCELED = 5;
	private static final int I_UNMONITORED = 6;
	
	
	// this
	private Bot bot=null;
	private BetData betCloseInfo=null;
	private int stopLossTicks=1;
	private int waitFramesNormal=20;
	private int waitFramesUntilForceClose=10;
	
	private Vector<BetData> historyBets=new Vector<BetData>();

	private MarketData md;
	
	// THREAD
	private ClosePositionThread as;
	private Thread t;
	protected int updateInterval = 500;
	private boolean polling = false;
	
	
	public ClosePosition(Bot botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA, int updateIntervalA)
	{
		super();
		bot=botA;
		betCloseInfo=betCloseInfoA;
		stopLossTicks=stopLossTicksA;
		waitFramesNormal=waitFramesNormalA;
		waitFramesUntilForceClose=waitFramesUntilForceCloseA;
		
		updateInterval=updateIntervalA;
		
		
		
		
		if(updateInterval==TradeMechanism.SYNC_MARKET_DATA_UPDATE)
		{
			
		}
	}
	
	public ClosePosition(Bot botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA)
	{
		super();
		
		
		
				
		
	}
	
	public ClosePosition(BetData betCloseA,int StopLossTicksA, int waitFramesNormal, int waitUntilForceClose)
	{
		super();
		
	}
	
	
	
	
	private void setState(int state)
	{
		
		STATE=TradeMechanism.OPEN;
	}
	
	@Override
	public void forceClose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forceCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getStatisticsFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatisticsValues() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private void refresh()
	{
		
	}

	//---------------------------------thread -----
	public class ClosePositionThread extends Object implements Runnable {
		private volatile boolean stopRequested;

		private Thread runThread;

		public void run() {
			runThread = Thread.currentThread();
			stopRequested = false;
			
			while (!stopRequested) {
				try {
					if(updateInterval!=BetManager.SYNC_MARKET_DATA_UPDATE)
					{
						refresh(); /// connect and get the data
						System.out.println("Not sync with MarketData");
					}
				
					//	refreshBets();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				
				
				try {
					Thread.sleep(updateInterval);
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}

		public void stopRequest() {
			stopRequested = true;

			if (runThread != null) {
				runThread.interrupt();

				// suspend()stop();
			}
		}
	}
	
	
	public void startPolling() {
		if (polling)
			return;
		as = new ClosePositionThread();
		t = new Thread(as);
		t.start();

		polling = true;
		
	}

	public void stopPolling() {
		if (!polling)
			return;
		as.stopRequest();
		polling = false;

	}
	

	public boolean isPolling() {
	
		return polling;
	}
		//-----------------------------------------end thread -------------------
		
	@Override
	public void clean() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		// TODO Auto-generated method stub
		
	}

}
