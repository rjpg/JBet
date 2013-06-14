package TradeMechanisms.dutchingChase;

import java.awt.Color;
import java.util.Vector;

import bets.BetData;
import DataRepository.MarketData;
import DataRepository.OddData;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.close.ClosePosition;
import TradeMechanisms.close.ClosePositionOptions;
import TradeMechanisms.dutching.DutchingRunnerOptions;
import TradeMechanisms.dutching.DutchingUtils;

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
		
		setState(TradeMechanism.OPEN);
		
		Vector<OddData> vod=new Vector<OddData>();
		
		// group all the OddData from open bets 
		for(DutchingChaseOptions dco:dcov)
		{
			vod.add(dco.getBetCloseInfo().getOddDataOriginal());
		}
		
		// recalculate all the amounts based on the global stake 
		DutchingUtils.calculateAmounts(vod, globalStake);
		
		// reset all the amounts for NOT_PLACED bets 
		// WARNING: if some bets are already placed the amount for that runner
		//  will not be recalculated. 
		for(DutchingChaseOptions dco:dcov)
		{
			for(OddData od:vod)
			{
				if(od.getRd()==dco.getBetCloseInfo().getRd())
				{
					if(dco.getBetCloseInfo().getState()==BetData.NOT_PLACED)
						dco.getBetCloseInfo().setAmount(od.getAmount());
				}
			}
		}
		
		// start the closing processes 
		for(DutchingChaseOptions dco:dcov)
		{
			ClosePositionOptions cpo=new ClosePositionOptions(dco.getBetCloseInfo(), this);
			
			cpo.setStopLossTicks(dco.getStopLossTicks());
			cpo.setWaitFramesNormal(dco.getWaitFramesNormal());
			cpo.setWaitFramesUntilForceClose(dco.getWaitFramesBestPrice());
			cpo.setUpdateInterval(dco.getUpdateInterval());
			cpo.setForceCloseOnStopLoss(dco.isForceCloseOnStopLoss());
			cpo.setUseStopProfitInBestPrice(dco.isUseStopProfitInBestPrice());
			cpo.setGoOnfrontInBestPrice(dco.isGoOnfrontInBestPrice());
			cpo.setStartDelay(dco.getStartDelay());
			cpo.setIgnoreStopLossDelay(dco.getIgnoreStopLossDelay());
			
			dco.setClose(new ClosePosition(cpo));
			
		}
		
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
		
		for(DutchingChaseOptions dro:dcov)
		{
			if(dro.getClose()!=null)
			{
				if(dro.getClose().getState()==TradeMechanism.PARTIAL_CLOSED)
					setState(TradeMechanism.PARTIAL_CLOSED);
				
				if(dro.getClose().getState()==TradeMechanism.CRITICAL_ERROR)
					setState(TradeMechanism.CRITICAL_ERROR);
				
				if(dro.getClose().getState()==TradeMechanism.UNMONITORED)
					setState(TradeMechanism.UNMONITORED);
			}
		}
		
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
