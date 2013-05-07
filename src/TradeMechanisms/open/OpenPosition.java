package TradeMechanisms.open;

import java.util.Vector;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.Utils;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import bets.BetData;
import bets.BetManager;

public class OpenPosition extends TradeMechanism implements MarketChangeListener{
	// States
	private static final int I_PLACING = 0;
	private static final int I_END = 1;

	private int I_STATE=I_PLACING;

	// dynamic vars
	private int waitFramesNormal=20;
	private BetData betInProcess;
	private Vector<BetData> historyBetsMatched=new Vector<BetData>();
	private boolean ended=false;
	
	// this
	private Vector<TradeMechanismListener> listeners=new Vector<TradeMechanismListener>();
	private BetData betOpenInfo=null;
	private MarketData md;

	// THREAD
	private OpenPositionThread as;
	private Thread t;
	protected int updateInterval = 500;
	private boolean polling = false;



	public OpenPosition(TradeMechanismListener botA,BetData betOpenInfoA, int waitFramesNormalA, int updateIntervalA) {
		super();

		if(botA!=null)
			addTradeMechanismListener(botA);

		betOpenInfo=betOpenInfoA;
		waitFramesNormal=waitFramesNormalA;
		updateInterval=updateIntervalA;

		md=betOpenInfoA.getRd().getMarketData();

		md.addTradingMechanismTrading(this);

		initialize();

	}

	public OpenPosition(TradeMechanismListener botA,BetData betOpenInfoA, int waitFramesNormalA) {
		this(botA, betOpenInfoA, waitFramesNormalA, TradeMechanism.SYNC_MARKET_DATA_UPDATE);
	}

	
	private void initialize()
	{
		setState(TradeMechanism.NOT_OPEN);

		if(updateInterval==TradeMechanism.SYNC_MARKET_DATA_UPDATE)
			md.addMarketChangeListener(this);


		betInProcess=betOpenInfo;

		startPolling();

	}

	private void setState(int state)
	{
		if(STATE==state) return;

		STATE=state;

		for(TradeMechanismListener tml: listeners)
		{
			tml.tradeMechanismChangeState(this, STATE);
		}
	}

	public int getI_STATE() {
		return I_STATE;
	}

	public void setI_STATE(int i_STATE) {
		I_STATE = i_STATE;
	}


	public void addTradeMechanismListener(TradeMechanismListener listener)
	{
		listeners.add(listener);
	}

	public void removeTradeMechanismListener(TradeMechanismListener listener)
	{
		listeners.remove(listener);
	}


	private void refresh()
	{
		System.out.println("refresh.. listeners number :"+listeners.size());
		waitFramesNormal--;
		
		if(waitFramesNormal<0 && betInProcess!=null )
			md.getBetManager().cancelBet(betInProcess);
		
		switch (getI_STATE()) {
		case I_PLACING: placing(); break;
		case I_END    : end(); break;
		default: end(); break;
		}
	}

	private void placing()
	{
		//System.out.println("placing");
		if(betInProcess==null)
		{
			this.setI_STATE(I_END);
			refresh();
			return;
		}

		if(betInProcess.getState()==BetData.NOT_PLACED)
		{
			md.getBetManager().placeBet(betInProcess);
		}
		else if(betInProcess.getState()==BetData.CANCELED )
		{
			betInProcess=null;

			this.setI_STATE(I_END);
			refresh();
			return;
		}
		else if(betInProcess.getState()==BetData.PARTIAL_CANCELED)
		{
			setState(TradeMechanism.PARTIAL_OPEN);
			historyBetsMatched.add(betInProcess);

			betInProcess=null;
			this.setI_STATE(I_END);
			refresh();
			return;
		}
		else if(betInProcess.getState()==BetData.MATCHED)
		{
			//System.out.println("matched");
			historyBetsMatched.add(betInProcess);
			setState(TradeMechanism.OPEN);

			betInProcess=null;
			this.setI_STATE(I_END);
			refresh();
		}
		
		else if(betInProcess.getState()==BetData.PARTIAL_MATCHED)
		{
			setState(TradeMechanism.PARTIAL_OPEN);
			
		}
		else if(betInProcess.getState()==BetData.UNMATCHED)
		{
			// wait ... (do nothing)
		}
		else if(betInProcess.getState()==BetData.PLACING_ERROR)
		{
			if(betInProcess.getErrorType()==BetData.ERROR_MARKET_CLOSED || betInProcess.getErrorType()==BetData.ERROR_BALANCE_EXCEEDED)
			{
				this.setState(TradeMechanism.CRITICAL_ERROR);
				this.setI_STATE(I_END);
				end();
				return;
			} 
			else
			{
				betInProcess=new BetData(betOpenInfo.getRd(), betOpenInfo.getAmount(), betOpenInfo.getOddRequested(),betOpenInfo.getType(), betOpenInfo.isKeepInPlay());
				md.getBetManager().placeBet(betInProcess);
				refresh();
			}
		}
		else if(betInProcess.getState()==BetData.UNMONITORED)
		{
			this.setState(TradeMechanism.UNMONITORED);
			this.setI_STATE(I_END);
			end();
			return;
		}

	}

	private void end()
	{
		//if(!TradeMechanismUtils.isTradeMechanismFinalState(getState()))
		//	setState(TradeMechanism.CLOSED);

		md.removeTradingMechanismTrading(this);
		stopPolling();
		
		ended=true;
		informListenersEnd();
		
		clean();


	}
	
	private void informListenersEnd()
	{
		for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
		{
			tml.tradeMechanismEnded(this, STATE);
		}
	}
	
	
	public  Vector<OddData> getMatchedOddDataVector()
	{
		Vector<OddData> ret=new Vector<OddData>();
		
		for(BetData bd:historyBetsMatched)
		{
			//System.out.println("Bet List : "+bd.getOddDataMatched());
			ret.add(bd.getOddDataMatched());
		}
		
		return ret;
	}
	
	
	@Override
	public void forceClose() {
		waitFramesNormal=0;

	}
	@Override
	public void forceCancel() {
		waitFramesNormal=0;

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
	
	@Override
	public Vector<BetData> getMatchedInfo()
	{
		return historyBetsMatched; 
	}
	
	@Override
	public boolean isEnded() {
		return ended;
	}
	
	@Override
	public void clean() {
		if(updateInterval==TradeMechanism.SYNC_MARKET_DATA_UPDATE)
			md.removeMarketChangeListener(this);
		md.removeTradingMechanismTrading(this);
		stopPolling();

		listeners.clear();
		listeners=null;

		betInProcess=null;

		betOpenInfo=null;

		System.out.println("Open Position clean runned");
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			if(isPolling() && updateInterval==TradeMechanism.SYNC_MARKET_DATA_UPDATE)
			{
				refresh();
				//System.out.println("Sync with MarketData");
			}
		}

		if(marketEventType==MarketChangeListener.MarketNew)
		{
			this.forceCancel();
		}

	}

	//---------------------------------thread -----
	private class OpenPositionThread extends Object implements Runnable {
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

		if(updateInterval!=TradeMechanism.SYNC_MARKET_DATA_UPDATE)
		{

			System.out.println("using thread");

			as = new OpenPositionThread();
			t = new Thread(as);
			t.start();
		}
		polling = true;

	}

	public void stopPolling() {
		if (!polling)
			return;

		if(updateInterval!=TradeMechanism.SYNC_MARKET_DATA_UPDATE)
			as.stopRequest();

		polling = false;

	}


	public boolean isPolling() {

		return polling;
	}




}
