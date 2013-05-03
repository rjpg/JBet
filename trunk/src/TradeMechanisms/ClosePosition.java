package TradeMechanisms;

import generated.exchange.BFExchangeServiceStub.GetMarket;

import java.util.Vector;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.Swing;
import DataRepository.Utils;
import bets.BetData;
import bets.BetManager;
import bets.BetUtils;
import bets.BetManagerReal.BetsManagerThread;
import bots.Bot;

public class ClosePosition extends TradeMechanism implements MarketChangeListener{

	
	// States
	private static final int I_PLACING = 0;
	private static final int I_END = 1;
	
	private int I_STATE=ClosePosition.I_PLACING;
	
	// dynamic vars
	private int waitFramesNormal=20;
	private int waitFramesUntilForceClose=10;
	private Vector<BetData> historyBetsMatched=new Vector<BetData>();
	private BetData betInProcess=null;
	private double targetOdd=1.01;
	
	// this
	private Vector<TradeMechanismListener> listeners=new Vector<TradeMechanismListener>();
	private BetData betCloseInfo=null;
	private int stopLossTicks=1;
	private double oddStopLoss=0;
	private MarketData md;
	
	// THREAD
	private ClosePositionThread as;
	private Thread t;
	protected int updateInterval = 500;
	private boolean polling = false;
	
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA, int updateIntervalA)
	{
		super();
		
		if(botA!=null)
			addTradeMechanismListener(botA);
		
		betCloseInfo=betCloseInfoA;
		stopLossTicks=stopLossTicksA;
		waitFramesNormal=waitFramesNormalA-1;
		waitFramesUntilForceClose=waitFramesUntilForceCloseA+2;
		updateInterval=updateIntervalA;
		
		if(betCloseInfoA.getType()==BetData.BACK)
		{
			oddStopLoss=Utils.indexToOdd(Utils.oddToIndex(betCloseInfoA.getOddRequested())-stopLossTicks);
			if(oddStopLoss==-1)
				oddStopLoss=1.01;
		}
		else
		{
			oddStopLoss=Utils.indexToOdd(Utils.oddToIndex(betCloseInfoA.getOddRequested())+stopLossTicks);
			if(oddStopLoss==-1)
				oddStopLoss=1000;
		}
			
		System.out.println("Stop loss Odd:"+oddStopLoss);
		
		md=betCloseInfoA.getRd().getMarketData();
		
		md.addTradingMechanismTrading(this);
		
		initialize();	
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
		setState(TradeMechanism.OPEN);
		
		//if(updateInterval==TradeMechanism.SYNC_MARKET_DATA_UPDATE)
		md.addMarketChangeListener(this);
		
		betInProcess=betCloseInfo;

		startPolling();
		
	}
	
	private void setState(int state)
	{
		if(STATE==state) return;
		
		STATE=state;
		
		for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
		{
			tml.tradeMechanismChangeState(this, STATE);
		}
	}
	
	@Override
	public void forceClose() {
		waitFramesNormal=0;
		waitFramesUntilForceClose=0;
	}

	@Override
	public void forceCancel() {
		if(!TradeMechanismUtils.isTradeMechanismFinalState(this.getState()))
		{
		this.setI_STATE(I_END);
		
		setState(TradeMechanism.CANCELED);
		
		end();
		
		}
		
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
	
	
	private double getActualOdd()
	{
		double actualOdd=0;
		if(betCloseInfo.getType()==BetData.BACK)
		{
			actualOdd=Utils.getOddBackFrame(betCloseInfo.getRd(), 0);
			if(actualOdd==0)
				actualOdd=1.01;
		}
		else
		{
			actualOdd=Utils.getOddLayFrame(betCloseInfo.getRd(), 0);
			if(actualOdd==0)
				actualOdd=1000;
		}
		
		return actualOdd;
	}
	
	private double getBestPrice()
	{
		double actualOdd=0;
		if(betCloseInfo.getType()==BetData.LAY)
		{
			actualOdd=Utils.getOddBackFrame(betCloseInfo.getRd(), 0);
			if(actualOdd==0)
				actualOdd=1.01;
		}
		else
		{
			actualOdd=Utils.getOddLayFrame(betCloseInfo.getRd(), 0);
			if(actualOdd==0)
				actualOdd=1000;
		}
		
		return actualOdd;
	} 
	

	private BetData createBetForOdd(double odd)
	{
	
		Vector<OddData> odv=getMatchedOddDataVector();
				
		OddData odMissing=BetUtils.getGreening(odv,betCloseInfo.getOddDataOriginal(),odd);
		
		double am=Utils.convertAmountToBF(odMissing.getAmount());
		if(am==0)
			return null;
		else		
			return new BetData(betCloseInfo.getRd(), odMissing, betCloseInfo.isKeepInPlay());
		
	}
	
	/**
	 * 
	 * @return
	 * 0 inside normal
	 * 1 inside close best price
	 * -1 emergency close
	 */
	private int processFrames()
	{
		if(waitFramesNormal<=0)
		{
			if(waitFramesUntilForceClose<=0)
				return -1;
			else
			{
				waitFramesUntilForceClose--;
				return 1;
			}
		}
		else
		{
			waitFramesNormal--;
			return 0;
		}
	}
	
	private void updateTargetOdd()
	{
		if(betCloseInfo.getType()==BetData.BACK)
		{
			if(getActualOdd()<=oddStopLoss)
				forceClose();
		}
		else
		{
			if(getActualOdd()>=oddStopLoss)
				forceClose();
		}
			
		int state = processFrames();
		
		switch (state) {
	        case  0: targetOdd=betCloseInfo.getOddRequested(); break;
	        case  1: targetOdd=getBestPrice(); break;
	        case -1: targetOdd=getActualOdd(); break;
	        default: targetOdd=getActualOdd(); break;
		}

	}
	
	private void refresh()
	{
		
		updateTargetOdd();
		
		switch (getI_STATE()) {
	        case I_PLACING: placing(); break;
	        case I_END    : end(); break;
	        default: end(); break;
		}
		
	}
	
	
	private void placing()
	{
		
		if(betInProcess==null || betInProcess.getState()==BetData.NOT_PLACED)
		{
			betInProcess=createBetForOdd(targetOdd);
			
			if(betInProcess==null)   // nothing to close
			{
				this.setI_STATE(I_END);
				refresh();
				return;
			}
			
			md.getBetManager().placeBet(betInProcess);
		}
		else if(betInProcess.getState()==BetData.CANCELED )
		{
			betInProcess=null;
			refresh();
			return;
		}
		else if(betInProcess.getState()==BetData.PARTIAL_CANCELED)
		{
			setState(TradeMechanism.PARTIAL_CLOSED);
			historyBetsMatched.add(betInProcess);
			
			betInProcess=null;
			refresh();
			return;
		}
		else if(betInProcess.getState()==BetData.MATCHED)
		{
			historyBetsMatched.add(betInProcess);
			setState(TradeMechanism.PARTIAL_CLOSED);
		
			this.forceClose(); // hedge at best offer
			
			betInProcess=null;
			refresh();
		}
		else if(betInProcess.getState()==BetData.PARTIAL_MATCHED )
		{
			
			setState(TradeMechanism.PARTIAL_CLOSED); // difference from UNMATCHED
				
			if(targetOdd!=betInProcess.getOddRequested())
			{
				md.getBetManager().cancelBet(betInProcess);
				refresh(); 
				return;
			}
		}
		else if(betInProcess.getState()==BetData.UNMATCHED)
		{
	
			if(targetOdd!=betInProcess.getOddRequested())
			{
				md.getBetManager().cancelBet(betInProcess);
				refresh();
				return;
			}
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
				betInProcess=null;
				refresh();
			}
		}
		else if(betInProcess.getState()==BetData.UNMONITORED)
		{
			System.out.println("unmonitored");
			this.setState(TradeMechanism.CRITICAL_ERROR);
			this.setI_STATE(I_END);
			end();
			return;
		}
	
		System.out.println(BetUtils.printBet(betInProcess));
	}
	
	private void end()
	{
		if(!TradeMechanismUtils.isTradeMechanismFinalState(getState()))
			setState(TradeMechanism.CLOSED);
		
		md.removeTradingMechanismTrading(this);
		stopPolling();
		
		informListenersEnd();
		
		clean();
			
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
	
	private void informListenersEnd()
	{
		for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
		{
			tml.tradeMechanismEnded(this, STATE);
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
		//System.out.println("*********************************************");
		
		if (polling)
			return;
		
		if(updateInterval!=TradeMechanism.SYNC_MARKET_DATA_UPDATE)
		{
			as = new ClosePositionThread();
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
		//-----------------------------------------end thread -------------------
		

	@Override
	public void clean() {
		//if(updateInterval==TradeMechanism.SYNC_MARKET_DATA_UPDATE)
		md.removeMarketChangeListener(this);
		md.removeTradingMechanismTrading(this);
		stopPolling();
		
		listeners.clear();
		listeners=null;
		
		historyBetsMatched.clear();
		historyBetsMatched=null;
		
		betInProcess=null;
		
		betCloseInfo=null;
		
		System.out.println("Close Position clean runned");
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

}
