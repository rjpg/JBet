package TradeMechanisms.close;

import java.awt.Color;
import java.util.Vector;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.TradeMechanismUtils;
import bets.BetData;
import bets.BetManager;
import bets.BetUtils;

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
	private boolean ended=false;
	private boolean isInHedge=false;
	private boolean processing=false;
	private boolean forceCloseCalledStopLoss=false;
	private boolean pause=false; 
	
	// this
	private Vector<TradeMechanismListener> listeners=new Vector<TradeMechanismListener>();
	private BetData betCloseInfo=null;
	private int stopLossTicks=1;
	private double oddStopLoss=0;
	private MarketData md;
	private boolean forceCloseOnStopLoss=true;
	private boolean useStopProfitInBestPrice=false;
	private int waitFramesLay1000=100;
	
	// False is not used - to think about it 
	private boolean goOnfrontInBestPrice=true;
	
	private int startDelay=-1;
	private int ignoreStopLossDelay=-1;
	
	// THREAD
	private ClosePositionThread as;
	private Thread t;
	protected int updateInterval = 500;
	private boolean polling = false;
	
	public ClosePosition(ClosePositionOptions cpo)
	{
		this(cpo.getDefaultListener(),
				cpo.getBetCloseInfo(),
				cpo.getStopLossTicks(),
				cpo.getWaitFramesNormal(),
				cpo.getWaitFramesUntilForceClose(),
				cpo.getUpdateInterval(),
				cpo.isForceCloseOnStopLoss(),
				cpo.isUseStopProfitInBestPrice(),
				cpo.isGoOnfrontInBestPrice(),
				cpo.getStartDelay(),
				cpo.getIgnoreStopLossDelay(),
				cpo.getWaitFramesLay1000());
		
	}
	
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA, int updateIntervalA, boolean forceCloseOnStopLossA,boolean useStopProfifInBestPriceA,boolean goOnfrontInBestPriceA, int startDelayA,int ignoreStopLossDelayA,int waitFramesLay1000A)
	{
		super();

		betCloseInfo=betCloseInfoA;
		stopLossTicks=stopLossTicksA;
		waitFramesNormal=waitFramesNormalA-1;
		waitFramesUntilForceClose=waitFramesUntilForceCloseA+2;
		updateInterval=updateIntervalA;
		forceCloseOnStopLoss=forceCloseOnStopLossA;
		useStopProfitInBestPrice=useStopProfifInBestPriceA;
		goOnfrontInBestPrice=goOnfrontInBestPriceA;
		startDelay=startDelayA;
		ignoreStopLossDelay=ignoreStopLossDelayA;
		waitFramesLay1000=waitFramesLay1000A;
		
		//System.out.println("Force : "+forceCloseOnStopLoss);
		
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
		
		if(botA!=null)
			addTradeMechanismListener(botA);
		
		md=betCloseInfoA.getRd().getMarketData();
		
		if(md==null)
		{
			setState(TradeMechanism.OPEN);
			this.setI_STATE(I_END);
			ended=true;
			informListenersEnd();
			return;		
		}
		else
			md.addTradingMechanismTrading(this);
		
		
		md.addTradingMechanismTrading(this);
		
		initialize();

	}
	
	
	
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA, int updateIntervalA, boolean forceCloseOnStopLossA,boolean useStopProfifInBestPriceA,boolean goOnfrontInBestPriceA, int startDelayA)
	{
		this(botA,betCloseInfoA,stopLossTicksA,waitFramesNormalA,waitFramesUntilForceCloseA,updateIntervalA,forceCloseOnStopLossA,useStopProfifInBestPriceA,goOnfrontInBestPriceA,-1,-1,100);
	}
	
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA, int updateIntervalA, boolean forceCloseOnStopLossA,boolean useStopProfifInBestPriceA,boolean goOnfrontInBestPriceA)
	{
		this(botA,betCloseInfoA,stopLossTicksA,waitFramesNormalA,waitFramesUntilForceCloseA,updateIntervalA,forceCloseOnStopLossA,useStopProfifInBestPriceA,goOnfrontInBestPriceA,-1);
	}
	
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA, int updateIntervalA, boolean forceCloseOnStopLossA,boolean useStopProfifInBestPriceA)
	{
		this(botA,betCloseInfoA,stopLossTicksA,waitFramesNormalA,waitFramesUntilForceCloseA,updateIntervalA,forceCloseOnStopLossA,useStopProfifInBestPriceA,false);
	}
	                   
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA, int updateIntervalA, boolean forceCloseOnStopLossA)
	{
		this(botA,betCloseInfoA,stopLossTicksA,waitFramesNormalA,waitFramesUntilForceCloseA,updateIntervalA,forceCloseOnStopLossA,false);	
	}
	
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA, int updateIntervalA)
	{
		this(botA,betCloseInfoA,stopLossTicksA,waitFramesNormalA,waitFramesUntilForceCloseA,updateIntervalA,false);	
	}
	
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA)
	{
		this(botA,betCloseInfoA,stopLossTicksA,waitFramesNormalA,waitFramesUntilForceCloseA,TradeMechanism.SYNC_MARKET_DATA_UPDATE);
	}
	
	public ClosePosition(TradeMechanismListener botA,BetData betCloseInfoA,int stopLossTicksA, int waitFramesNormalA, int waitFramesUntilForceCloseA, boolean forceCloseOnStopLossA)
	{
		this(botA,betCloseInfoA,stopLossTicksA,waitFramesNormalA,waitFramesUntilForceCloseA,TradeMechanism.SYNC_MARKET_DATA_UPDATE,forceCloseOnStopLossA);
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
	

	public void forceCloseStopLoss() {
		
		if(ignoreStopLossDelay>0)
		{
			ignoreStopLossDelay--;
			return;
		}
		
		forceCloseCalledStopLoss=true;
		if(forceCloseOnStopLoss)
			forceClose();
		else
			waitFramesNormal=0;
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
		
		if(betInProcess!=null)
		{
			if(betInProcess.getState()==BetData.UNMATCHED || betInProcess.getState()==BetData.PARTIAL_MATCHED)
			{
				md.getBetManager().cancelBet(betInProcess);
			}
		}
		setState(TradeMechanism.CANCELED);
		
		end();
		
		}
		
	}
	
	public void unmonitored() {
		if(!TradeMechanismUtils.isTradeMechanismFinalState(this.getState()))
		{
		this.setI_STATE(I_END);
		
		setState(TradeMechanism.UNMONITORED);
		
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
	
	@Override
	public Vector<BetData> getMatchedInfo()
	{
		return historyBetsMatched; 
	}
	
	@Override
	public double getEndPL()
	{
		return 0.00;
	}
	
	@Override
	public boolean isEnded() {
		return ended;
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
	

	private BetData createBetForOdd()    //arg double odd replaced by targetOdd
	{
	
		Vector<OddData> odv=getMatchedOddDataVector();
				
		OddData odMissing=BetUtils.getGreening(odv,betCloseInfo.getOddDataOriginal(),targetOdd);
		
		if(odMissing.getType()!=betCloseInfo.getType())
		{
			if(isInHedge==false)
			{
				isInHedge=true;
				
				updateTargetOdd();
			
				odMissing=BetUtils.getGreening(odv,betCloseInfo.getOddDataOriginal(),targetOdd);
			}
		}
		else
		{
			isInHedge=false;
		}
		
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
		
		if(isInHedge)
		{
			targetOdd=getBestPrice();
		}
		else
		{
			if(betCloseInfo.getType()==BetData.BACK)
			{
				if(getActualOdd()<oddStopLoss)
					forceCloseStopLoss();
			}
			else
			{
				if(getActualOdd()>oddStopLoss)
					forceCloseStopLoss();
			}
				
			int state = processFrames();
			
			
			
			switch (state) {
		        case  0: targetOdd=betCloseInfo.getOddRequested(); break;
		        case  1: {
		        		targetOdd=getBestPrice();
		        		filterTargetOddUsingStopProfifInBestPrice();
		        		filterTargetOddUsingGoOnfrontInBestPrice();
		        		}
		        		break;
		        case -1: targetOdd=getActualOdd(); break;
		        default: targetOdd=getActualOdd(); break;
			}
			
		}

	}
	
	void filterTargetOddUsingStopProfifInBestPrice()
	{
		if(useStopProfitInBestPrice)
		{
			if(betCloseInfo.getType()==BetData.BACK)
			{
				if(targetOdd > betCloseInfo.getOddRequested())
					targetOdd=betCloseInfo.getOddRequested();
					
			}
			else
			{
				if(targetOdd < betCloseInfo.getOddRequested())
					targetOdd=betCloseInfo.getOddRequested();
			}
		}
	}
	
	void filterTargetOddUsingGoOnfrontInBestPrice()
	{
		if(goOnfrontInBestPrice && forceCloseCalledStopLoss)
		{
			if(betCloseInfo.getType()==BetData.BACK)
			{
				double targetOddAux=Utils.indexToOdd((Utils.oddToIndex(getActualOdd())+1));
				if(targetOddAux!=-1)
					targetOdd=targetOddAux;
			}
			else
			{
				double targetOddAux=Utils.indexToOdd((Utils.oddToIndex(getActualOdd())-1));
				if(targetOddAux!=-1)
					targetOdd=targetOddAux;
			}
		}
	}
	
	private void refresh()
	{
		if (pause) return;
		
		if(startDelay>0)
		{
			startDelay--;
			return;
		}
		
		updateTargetOdd();
		
		processWaitFramesLay1000();
		
		switch (getI_STATE()) {
	        case I_PLACING: placing(); break;
	        case I_END    : end(); break;
	        default: end(); break;
		}
		
	}
	
	private void processWaitFramesLay1000()
	{
		if(waitFramesLay1000==-1) return;
		
		if(betInProcess!=null && betInProcess.getOddRequested()==1000 && betInProcess.getType()==BetData.LAY)
		{
			waitFramesLay1000--;
			if(waitFramesLay1000>0) 
				return;
			else
				setI_STATE(I_END);
			
		}
		
			
	}
	
	private void placing()
	{
		
		if(betInProcess==null || betInProcess.getState()==BetData.NOT_PLACED)
		{
			System.out.println("Entrei 1");
			writeMsgToListeners("Entrei 1", Color.BLUE);
			betInProcess=createBetForOdd();
			
			if(betInProcess==null)   // nothing to close
			{
				System.out.println("Entrei 1111");
				setState(TradeMechanism.CLOSED);
				this.setI_STATE(I_END);
				refresh();
				return;
			}
			
			md.getBetManager().placeBet(betInProcess);
			return;
		}
		else if(betInProcess.getState()==BetData.CANCELED )
		{
			System.out.println("Entrei 2");
			betInProcess=null;
			refresh();
			return;
		}
		else if(betInProcess.getState()==BetData.PARTIAL_CANCELED)
		{
			System.out.println("Entrei 3");
			setState(TradeMechanism.PARTIAL_CLOSED);
			historyBetsMatched.add(betInProcess);
			
			betInProcess=null;
			refresh();
			return;
		}
		else if(betInProcess.getState()==BetData.MATCHED)
		{
			System.out.println("Entrei 4");
			historyBetsMatched.add(betInProcess);
			setState(TradeMechanism.PARTIAL_CLOSED);
		
			this.forceClose(); // hedge at best offer
			
			betInProcess=null;
			refresh();
			return;
		}
		else if(betInProcess.getState()==BetData.PARTIAL_MATCHED )
		{
			System.out.println("Entrei 5");
			setState(TradeMechanism.PARTIAL_CLOSED); // difference from UNMATCHED
				
			if(targetOdd!=betInProcess.getOddRequested())
			{
				if(md.getBetManager().cancelBet(betInProcess)==0);
					refresh(); 
				// else 
				//	let it call update (when sync with marketData) when cancel return error
				//  to update bet state using betManegar Update() 
				return;
			}
			return;
		}
		else if(betInProcess.getState()==BetData.UNMATCHED)
		{
			System.out.println("Entrei 6");
	
			if(targetOdd!=betInProcess.getOddRequested())
			{
				
				if(md.getBetManager().cancelBet(betInProcess)==0);
				{
					System.out.println("Cancel return 0");
					refresh();
				}
				return;
			}
			return;
		}
		else if(betInProcess.getState()==BetData.PLACING_ERROR)
		{
			System.out.println("Entrei 7");
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
			return;
		}
		else if(betInProcess.getState()==BetData.UNMONITORED)
		{
			System.out.println("Entrei 8");
			//System.out.println("unmonitored");
			this.setState(TradeMechanism.UNMONITORED);
			this.setI_STATE(I_END);
			end();
			return;
		}
	
		//System.out.println("Bet in processs (close position)"+BetUtils.printBet(betInProcess));
	}
	
	private void end()
	{
				
		md.removeTradingMechanismTrading(this);
		stopPolling();
		
		ended=true;
		
		//System.out.println("informing listeners of end");
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
	
	
	
	public RunnersData getRunner()
	{
		return betCloseInfo.getRd();
	}
	
	public BetData getBetCloseInfo()
	{
		return betCloseInfo;
	}
	
	private void informListenersEnd()
	{
		//System.out.println("size : "+listeners.size());
		for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
		{
			//System.out.println("informing"+tml );
			tml.tradeMechanismEnded(this, STATE);
		}
	}
	
	

	//---------------------------------thread -----
	private class ClosePositionThread extends Object implements Runnable {
		private volatile boolean stopRequested;

		private Thread runThread;

		public void run() {
			runThread = Thread.currentThread();
			stopRequested = false;
			
			while (!stopRequested) {
				try {
					if(updateInterval!=BetManager.SYNC_MARKET_DATA_UPDATE)
					{
						if(!processing)
						{
							processing=true;
							try {
								refresh(); /// connect and get the data
							} catch (Exception e) {
								System.err.println("some error in close mecanism refresh() [ClosePositionThread]");
								e.printStackTrace();
							}
							
							System.out.println("Not sync with MarketData");
							processing=false;
						}

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
		
		betInProcess=null;
		
		//betCloseInfo=null;
		
		System.out.println("Close Position clean runned");
	}

	
	@Override
	public void MarketChange(MarketData mdA, int marketEventType) {
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			
			if(isPolling() && updateInterval==TradeMechanism.SYNC_MARKET_DATA_UPDATE)
			{
				if(!processing)
				{
					processing=true;
					try {
						refresh();
					} catch (Exception e) {
						System.err.println("some error in close mecanism refresh() [SYNC_MARKET_DATA_UPDATE]");
						e.printStackTrace();

					}
					
					processing=false;
				}
				
				//System.out.println("Sync with MarketData");
			}
		}
		
		if(marketEventType==MarketChangeListener.MarketNew)
		{
			this.unmonitored();
		}
	}

	@Override
	public void setPause(boolean pauseA) {
		pause=pauseA;
		writeMsgToListeners("ClosePosition setPause() was called with :"+pause, Color.BLUE);
	}

	@Override
	public boolean isPause() {
		return pause;
	}


	private void writeMsgToListeners(String msg, Color color)
	{
		if(listeners!=null)
		{
			String msgaux="[ClosePosition]";
			
			if(getRunner()!=null)
				msgaux+="["+getRunner().getName()+"]";
			
			msgaux+=msg;
			for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
			{
				tml.tradeMechanismMsg(this, msgaux, color);
			}
		}
	}
	

}
