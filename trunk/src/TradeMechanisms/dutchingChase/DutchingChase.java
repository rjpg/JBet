package TradeMechanisms.dutchingChase;

import java.awt.Color;
import java.util.Vector;

import bets.BetData;
import DataRepository.MarketData;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.dutching.DutchingRunnerOptions;

public class DutchingChase extends TradeMechanism implements TradeMechanismListener{

	private Vector<TradeMechanismListener> listeners=new Vector<TradeMechanismListener>();
	
	//args
	private Vector<DutchingChaseOptions> dcov;
	private double globalStake;
	
	//dynamic vars
	private boolean ended=false;
	private boolean pause=false;
	
	private MarketData md;
	
	public DutchingChase(TradeMechanismListener bot,Vector<DutchingChaseOptions> dcovA, double globalStakeA ) {
		if(bot!=null)
			addTradeMechanismListener(bot);
		
		dcov=dcovA;
		
		globalStake=globalStakeA;
		
		initialize();
	}
	
	public DutchingChase(Vector<DutchingChaseOptions> dcoA) {
		this(null,dcoA,0.00);
	}
	
	public DutchingChase(Vector<DutchingChaseOptions> dcoA, double globalStakeA ) {
		this(null,dcoA,globalStakeA);
	}
	
	private void initialize()
	{
		md=dcov.get(0).getBetCloseInfo().getRd().getMarketData();
		md.addTradingMechanismTrading(this);
		
	}
	
	
	//----------------start tradeMechanism ----------------
	@Override
	public void forceClose() {
		for(DutchingChaseOptions dco:dcov)
		{
			dco.getClose().forceClose();
		}
		
	}

	@Override
	public void forceCancel() {
		for(DutchingChaseOptions dco:dcov)
		{
			dco.getClose().forceCancel();
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
		Vector<BetData> ret=new Vector<BetData>();
		
		for(DutchingChaseOptions dco:dcov)
		{
			for (BetData bd:dco.getClose().getMatchedInfo())
				ret.add(bd);
		}
		
		return ret; 
	}

	@Override
	public boolean isEnded() {
		return ended;
	}

	@Override
	public void setPause(boolean pauseA) {
		if(pauseA==pause) return;
		
		pause=pauseA;
		
		for(DutchingChaseOptions dco:dcov)
		{
			dco.getClose().setPause(pause);
		}
		
		
	}

	@Override
	public boolean isPause() {
		return pause;
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

	//----------------end tradeMechanism ----------------
	
	
	private void setState(int state)
	{
		if(STATE==CRITICAL_ERROR) return;
		
		if(STATE==state) return;
		
		STATE=state;
		
		for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
		{
			tml.tradeMechanismChangeState(this, STATE);
		}
	}
	private void writeMsgToListeners(String msg, Color color)
	{
		if(listeners!=null)
		{
			String msgaux="[DutchingChase]"+msg;
			for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
			{
				tml.tradeMechanismMsg(this, msgaux, color);
			}
		}
	}

	
	// ------------- start tradeMechanims listener --------------------- 
	@Override
	public void tradeMechanismChangeState(TradeMechanism tm, int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tradeMechanismEnded(TradeMechanism tm, int state) {
		
		boolean allEnded=true;
		boolean allClosed=true;
		
		for(DutchingChaseOptions dro:dcov)
			if(dro.getClose()!=null)
			{
				if(!dro.getClose().isEnded())
					allEnded=false;
				
				if(dro.getClose().getState()!=TradeMechanism.CLOSED)
					allClosed=false;
				
			}
		
		if(allEnded)
		{
			System.out.println("Dutching All Close Position ended");
			if(allClosed)
				setState(TradeMechanism.CLOSED);
			
			end();
		}
		
	}

	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {
		writeMsgToListeners(msg, color);
		
	}
	
	// ------------- end tradeMechanims listener ---------------------
	
	private void end()
	{
		md.removeTradingMechanismTrading(this);
		
		ended=true;
		
		informListenersEnd();
		
		clean();
	}
	
	private void informListenersEnd()
	{
		for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
		{
			tml.tradeMechanismEnded(this, STATE);
		}
	}

}
