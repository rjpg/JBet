package TradeMechanisms;

import java.util.Vector;

import DataRepository.MarketData;
import TradeMechanisms.ClosePosition.ClosePositionThread;
import bets.BetData;

public class OpenPosition extends TradeMechanism{
	// States
		private static final int I_PLACING = 0;
		private static final int I_END = 1;
		
		private int I_STATE=I_PLACING;
		
		// dynamic vars
		private int waitFramesNormal=20;
				
		// this
		private Vector<TradeMechanismListener> listeners=new Vector<TradeMechanismListener>();
		private BetData betCloseInfo=null;
		private MarketData md;
		
		// THREAD
		private ClosePositionThread as;
		private Thread t;
		protected int updateInterval = 500;
		private boolean polling = false;
		
		
		
		public OpenPosition(TradeMechanismListener botA,BetData betCloseInfoA, int waitFramesNormalA, int updateIntervalA) {
			super();
			
			if(botA!=null)
				addTradeMechanismListener(botA);
			
			betCloseInfo=betCloseInfoA;
			waitFramesNormal=waitFramesNormalA;
			updateInterval=updateIntervalA;
			
		}

		public void addTradeMechanismListener(TradeMechanismListener listener)
		{
			listeners.add(listener);
		}
		
		public void removeTradeMechanismListener(TradeMechanismListener listener)
		{
			listeners.remove(listener);
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
			// TODO Auto-generated method stub
			
		}
		
		
}
