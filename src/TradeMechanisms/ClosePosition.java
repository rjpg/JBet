package TradeMechanisms;

import generated.exchange.BFExchangeServiceStub.GetMarket;

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
	private static final int I_PLACING = 0;
	private static final int I_WAIT = 1;
	private static final int I_CANCEL = 2;
	private static final int I_HEDGE = 3;
	private static final int I_END = 4;
	
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
		waitFramesNormal=waitFramesNormalA;
		waitFramesUntilForceClose=waitFramesUntilForceCloseA;
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
			
		//if(bet.)
		
		//System.out.println("Stop loss Odd:"+oddStopLoss);
		
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
		
		if(updateInterval==TradeMechanism.SYNC_MARKET_DATA_UPDATE)
		{
			md.addMarketChangeListener(this);
		}
		
		betInProcess=betCloseInfo;

		startPolling();
		
	}
	
	private void setState(int state)
	{
		
		STATE=state;
	}
	
	@Override
	public void forceClose() {
		waitFramesNormal=0;
		waitFramesUntilForceClose=0;
	}

	@Override
	public void forceCancel() {
		this.setI_STATE(I_END);
		
		end();
		
		setState(TradeMechanism.CANCELED);
		
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
	
	private double calculateAmountMissing()
	{
		Vector<Double> odds=new Vector<Double>();
		Vector<Double> ams=new Vector<Double>();
		double totalAm=0;
		
		if(historyBetsMatched.size()==0)
			return betCloseInfo.getAmount();
		
		for(BetData bd:historyBetsMatched)
		{
			odds.add(bd.getOddMached());
			ams.add(bd.getMatchedAmount());
			totalAm+=bd.getMatchedAmount();
		}
		
		double oddAvg=Utils.calculateOddAverage(odds.toArray(new Double[]{}), ams.toArray(new Double[]{}));
		
		double ret=0;
		
		if(betCloseInfo.getType()==BetData.BACK)
		{
			double amountToCloseBack=Utils.closeAmountLay(oddAvg, totalAm, betCloseInfo.getOddRequested());
			ret= betCloseInfo.getAmount()-amountToCloseBack;
		}
		else
		{
			double amountToCloseLay=Utils.closeAmountBack(oddAvg, totalAm, betCloseInfo.getOddRequested());
			ret= betCloseInfo.getAmount()-amountToCloseLay;
		}
		
		return (Utils.convertAmountToBF(ret));
	}
	
	private BetData createBetForOdd(double odd)
	{
		BetData ret=null;
		
		double miss=calculateAmountMissing();
		
		
		if(miss==0)
		{
			return null; // All closed
		} else if(miss<0)
		{
			double am=0;
			if(betCloseInfo.getType()==BetData.BACK)
			{
				am=Utils.closeAmountLay(betCloseInfo.getOddRequested(), Math.abs(miss), odd);
				am=Utils.convertAmountToBF(am);
				if(am==0)
					return null;
				
				ret=new BetData(betCloseInfo.getRd(), am, odd, BetData.LAY, betCloseInfo.isKeepInPlay());
				return ret;
				
			}
			else
			{
				am=Utils.closeAmountBack(betCloseInfo.getOddRequested(), Math.abs(miss), odd);
				am=Utils.convertAmountToBF(am);
				if(am==0)
					return null;
				
				ret=new BetData(betCloseInfo.getRd(), am, odd, BetData.BACK, betCloseInfo.isKeepInPlay());
				return ret;
			}
			
			
			
		} else 
		{
			double am=0;
			
			if(betCloseInfo.getType()==BetData.BACK)
			{
				am=Utils.closeAmountBack(betCloseInfo.getOddRequested(), miss, odd);
				am=Utils.convertAmountToBF(am);
				if(am==0)
					return null;
				
			}
			else
			{
				am=Utils.closeAmountLay(betCloseInfo.getOddRequested(), miss, odd);
				am=Utils.convertAmountToBF(am);
				if(am==0)
					return null;
			}
			
			
			ret=new BetData(betCloseInfo.getRd(), am, odd, betCloseInfo.getType(), betCloseInfo.isKeepInPlay());
			return ret;
		
		}

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
		if(waitFramesNormal<0)
		{
			if(waitFramesUntilForceClose<0)
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
	        case I_WAIT   : waitMatch(); break;
	        case I_CANCEL : cancel(); break;
	        case I_HEDGE  : hedge(); break;
	        case I_END    : end(); break;
	        default: end(); break;
		}
		
	}
	
	
	public void placing()
	{
		System.out.println("placing");
		if(betInProcess==null)
		{
			betInProcess=createBetForOdd(targetOdd);
			
			if(betInProcess==null)   // nothing to close
			{
				this.setI_STATE(I_END);
				return;
			}
			if(betInProcess.getType()!=betCloseInfo.getType())
			{
				betInProcess=null;
				this.setI_STATE(I_HEDGE);
				refresh();
				return ;
			}
		}
		
		if(betInProcess.getState()==BetData.NOT_PLACED)
		{
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
			
			betInProcess=null;
			setI_STATE(I_HEDGE);
			refresh();
		}
		else if(betInProcess.getState()==BetData.PARTIAL_MATCHED)
		{
			setState(TradeMechanism.PARTIAL_CLOSED);
			setI_STATE(ClosePosition.I_WAIT);
		}
		else if(betInProcess.getState()==BetData.UNMATCHED)
		{
			setI_STATE(ClosePosition.I_WAIT);
		}
		else if(betInProcess.getState()==BetData.PLACING_ERROR)
		{
			if(betInProcess.getErrorType()==BetData.ERROR_MARKET_CLOSED || betInProcess.getErrorType()==BetData.ERROR_BALANCE_EXCEEDED)
			{
				this.setState(TradeMechanism.CRITICAL_ERROR);
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
			this.setState(TradeMechanism.CRITICAL_ERROR);
			end();
			return;
		}
	
	}
	
	private void  waitMatch()
	{
		if(betInProcess==null)
		{
			setI_STATE(I_PLACING);
			refresh();
			return;
		}
		
		
		if(betInProcess.getState()==BetData.NOT_PLACED)
		{
			betInProcess = null;
			setI_STATE(I_PLACING);
			refresh();
			return;
		}
		else if(betInProcess.getState()==BetData.CANCELED )
		{
			betInProcess=null;
			setI_STATE(I_PLACING);
			refresh();
			return;
		}
		else if(betInProcess.getState()==BetData.PARTIAL_CANCELED)
		{
			setState(TradeMechanism.PARTIAL_CLOSED);
			historyBetsMatched.add(betInProcess);
			
			betInProcess=null;
			setI_STATE(I_PLACING);
			refresh();
			return;
		}
		else if(betInProcess.getState()==BetData.MATCHED)
		{
			historyBetsMatched.add(betInProcess);
			setState(TradeMechanism.PARTIAL_CLOSED);
			
			betInProcess=null;
			setI_STATE(I_HEDGE);
			refresh();
		}
		else if(betInProcess.getState()==BetData.PARTIAL_MATCHED)
		{
			setState(TradeMechanism.PARTIAL_CLOSED);
			
			if(targetOdd!=betInProcess.getOddRequested())
			{
				int resultCancel=md.getBetManager().cancelBet(betInProcess);
				if(resultCancel==0)
				{
					setI_STATE(I_CANCEL);
					refresh();
					return;
				}
				
			}
		}
		else if(betInProcess.getState()==BetData.UNMATCHED)
		{
			if(targetOdd!=betInProcess.getOddRequested())
			{
				int resultCancel=md.getBetManager().cancelBet(betInProcess);
				if(resultCancel==0)
				{
					setI_STATE(I_CANCEL);
					refresh();
					return;
				}
				
			}
		}
		else if(betInProcess.getState()==BetData.UNMONITORED)
		{
			this.setState(TradeMechanism.CRITICAL_ERROR);
			end();
			return;
		}
		
		
	}
	
	private void cancel()
	{
		if(betInProcess==null)
		{
			setI_STATE(I_PLACING);
			refresh();
			return;
		}
		
		
		
			
	}
	
	private void hedge()
	{
		
	}
	
	
	private void end()
	{
		md.removeTradingMechanismTrading(this);
		stopPolling();
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
		if(updateInterval==TradeMechanism.SYNC_MARKET_DATA_UPDATE)
			md.removeMarketChangeListener(this);
		md.removeTradingMechanismTrading(this);
		stopPolling();
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			if(isPolling())
			{
				refresh();
				System.out.println("Sync with MarketData");
			}
		}
		
	}
	
	

}
