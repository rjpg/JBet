package TradeMechanisms;

import java.awt.Color;

public abstract class TradeMechanism {
	
	public static int SYNC_MARKET_DATA_UPDATE = 0;
	
	// States
	public static final int NOT_OPEN = 0;
	public static final int PARTIAL_OPEN = 1;
	public static final int OPEN = 2;
	public static final int PARTIAL_CLOSED = 3;
	public static final int CLOSED = 4;
	public static final int CANCELED = 5;
	public static final int UNMONITORED = 6;
	public static final int CRITICAL_ERROR = 7;
	
	protected int STATE=NOT_OPEN;
	
	public int getState()
	{
		return STATE;
	}
	
	public abstract void forceClose();
	
	public abstract void forceCancel(); //STATE=UNMONITORED;
	
	public abstract String getStatisticsFields();
	
	public abstract String getStatisticsValues();
	
	public abstract void clean();
	
	public abstract void addTradeMechanismListener(TradeMechanismListener listener);
	
	public abstract void removeTradeMechanismListener(TradeMechanismListener listener);
	
}
