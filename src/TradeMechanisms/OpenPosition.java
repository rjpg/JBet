package TradeMechanisms;

import java.util.Vector;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import TradeMechanisms.ClosePosition.ClosePositionThread;
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
			
		}

		
		private void initialize()
		{
			setState(TradeMechanism.NOT_OPEN);
			
			if(updateInterval!=TradeMechanism.SYNC_MARKET_DATA_UPDATE)
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
			System.out.println("refresh");
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
		public class OpenPositionThread extends Object implements Runnable {
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
