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
	
	
	public static String getStateString(int state)
	{
		switch (state) {
        case  TradeMechanism.NOT_OPEN: return "NOT_OPEN"; 
        case  TradeMechanism.PARTIAL_OPEN: return "PARTIAL_OPEN"; 
        case  TradeMechanism.OPEN: return "OPEN"; 
        case  TradeMechanism.PARTIAL_CLOSED: return "PARTIAL_CLOSED"; 
        case  TradeMechanism.CLOSED: return "CLOSED"; 
        case  TradeMechanism.CANCELED: return "CANCELED"; 
        case  TradeMechanism.UNMONITORED: return "UNMONITORED";
        case  TradeMechanism.CRITICAL_ERROR: return "CRITICAL_ERROR"; 
        default: return null; 
		}
	}
	
}
