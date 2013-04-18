package TradeMechanisms;

import bets.BetData;

public class TradeMechanismUtils {
	
	
	public static boolean isTradeMechanismFinalState(int state)
	{
		if(state==TradeMechanism.CLOSED || 
				state==TradeMechanism.CANCELED || 
				state==TradeMechanism.UNMONITORED ||
				state==TradeMechanism.CRITICAL_ERROR)
			return true;
		else
			return false;
	}
	
}
