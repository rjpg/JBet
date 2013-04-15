package TradeMechanisms;

import java.util.Vector;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.Swing;
import DataRepository.Utils;
import bets.BetData;
import bets.BetManager;
import bets.BetUtils;
import bets.BetManagerReal.BetsManagerThread;
import bots.Bot;

public class ClosePosition extends TradeMechanism implements MarketChangeListener{

	
	// States
	private static final int I_PLACING = 2;
	private static final int I_PLACED = 2;
	private static final int I_PARTIAL_CLOSED = 3;
	private static final int I_CLOSED = 4;
	private static final int I_CANCELED = 5;
	private static final int I_UNMONITORED = 6;
	
	private int I_STATE=ClosePosition.I_PLACING;
	
	
	

	// this
	private Vector<TradeMechanismListener> listeners=new Vector<TradeMechanismListener>();
	private BetData betCloseInfo=null;
	private int stopLossTicks=1;
	private double oddStopLoss=0;
	private int waitFramesNormal=20;
	private int waitFramesUntilForceClose=10;
	
	private int placingErrors=10;
	
	private boolean useIPKeep=false;
	
	private Vector<BetData> historyBetsMatched=new Vector<BetData>();
	
	private BetData betInProcess=null;

	private MarketData md;
	
	// THREAD
	private ClosePositionThread as;
	private Thread t;
	protected int updateInterval = 500;
	private boolean polling = false;
	
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA, int updateIntervalA, boolean useIPKeepA)
	{
		super();
		
		if(botA!=null)
			addTradeMechanismListener(botA);
		
		betCloseInfo=betCloseInfoA;
		stopLossTicks=stopLossTicksA;
		waitFramesNormal=waitFramesNormalA;
		waitFramesUntilForceClose=waitFramesUntilForceCloseA;
		updateInterval=updateIntervalA;
		useIPKeep=useIPKeepA;
		
		
		betInProcess=betCloseInfo;
		
		if(betCloseInfoA.getType()==BetData.BACK)
			oddStopLoss=Utils.indexToOdd(Utils.oddToIndex(betCloseInfoA.getOddRequested())-stopLossTicks);
		else
			oddStopLoss=Utils.indexToOdd(Utils.oddToIndex(betCloseInfoA.getOddRequested())+stopLossTicks);
			
		//if(bet.)
		
		//System.out.println("Stop loss Odd:"+oddStopLoss);
		
		md=betCloseInfoA.getRd().getMarketData();
		
		md.addTradingMechanismTrading(this);
		initialize();
		
	}
	
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA, int updateIntervalA)
	{
		
		this(botA,betCloseInfoA,stopLossTicksA,waitFramesNormalA,waitFramesUntilForceCloseA,updateIntervalA,betCloseInfoA.isKeepInPlay());
	}
	
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA)
	{
		this(botA,betCloseInfoA,stopLossTicksA,waitFramesNormalA,waitFramesUntilForceCloseA,TradeMechanism.SYNC_MARKET_DATA_UPDATE);
	}

	public ClosePosition(BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA)
	{
		this(null,betCloseInfoA,stopLossTicksA,waitFramesNormalA,waitFramesUntilForceCloseA);
	}
	
	public void addTradeMechanismListener(TradeMechanismListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeTradeMechanismListener(TradeMechanismListener listener)
	{
		listeners.remove(listener);
	}
	
	private void initialize()
	{
		
		
	/*	if(betCloseInfo.getState()==BetData.NOT_PLACED)
			place
		*/
		if(updateInterval==TradeMechanism.SYNC_MARKET_DATA_UPDATE)
		{
			md.addMarketChangeListener(this);
		}
		else
		{
			startPolling();
		}
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
	
	
	public int getI_STATE() {
		return I_STATE;
	}

	public void setI_STATE(int i_STATE) {
		I_STATE = i_STATE;
	}
	
	private void refresh()
	{
		
		switch (getI_STATE()) {
	        case ClosePosition.I_PLACING:  placing();       break;
	        
	        default: clean(); break;
		}
		
	}
	
	public void placing()
	{
		System.out.println("Bet.State : "+BetUtils.printBet(betInProcess));
		
		if(betInProcess.getState()==BetData.BET_IN_PROGRESS)
		{
			waitFramesNormal--;
		}
		else
		if(betInProcess.getState()==BetData.NOT_PLACED)
		{
			
		}
		else
		if(betInProcess.getState()==BetData.CANCELED)
		{
			
		}
		else
		if(betInProcess.getState()==BetData.PARTIAL_CANCELED)
		{
			
		}
		else
		if(betInProcess.getState()==BetData.MATCHED)
		{
			
		}
		else
		if(betInProcess.getState()==BetData.PARTIAL_MATCHED)
		{
			
		}
		else
		if(betInProcess.getState()==BetData.PLACING_ERROR)
		{
				
		}	
		
		
		
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
		System.out.println("*********************************************");
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
		if(updateInterval==TradeMechanism.SYNC_MARKET_DATA_UPDATE)
			md.removeMarketChangeListener(this);
		
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			refresh();
			System.out.println("Sync with MarketData");
		}
		
	}

}
