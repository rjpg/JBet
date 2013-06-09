package TradeMechanisms.dutchingChase;

import java.awt.Color;
import java.util.Vector;

import bets.BetData;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;

public class DutchingChase extends TradeMechanism{

	private Vector<TradeMechanismListener> listeners=new Vector<TradeMechanismListener>();
	
	private Vector<DutchingChaseOptions> dco;
	
	public DutchingChase(TradeMechanismListener bot,Vector<DutchingChaseOptions> dcoA ) {
		if(bot!=null)
			addTradeMechanismListener(bot);
		
		dco=dcoA;
		
		initialize();
	}
	
	public DutchingChase(Vector<DutchingChaseOptions> dcoA ) {
		this(null,dcoA);
	}
	
	private void initialize()
	{
		
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

	public void addTradeMechanismListener(TradeMechanismListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeTradeMechanismListener(TradeMechanismListener listener)
	{
		listeners.remove(listener);
	}
	

	@Override
	public Vector<BetData> getMatchedInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPause(boolean pauseA) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPause() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getEndPL() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void clean() {
		// TODO Auto-generated method stub
		
	}

	private void writeMsgToListeners(String msg, Color color)
	{
		if(listeners!=null)
		for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
		{
			tml.tradeMechanismMsg(this, msg, color);
		}
	}
}
