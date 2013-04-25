package TradeMechanisms;

import java.util.Vector;

import DataRepository.MarketData;
import TradeMechanisms.ClosePosition.ClosePositionThread;
import bets.BetData;

public class OpenPosition {
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
}
