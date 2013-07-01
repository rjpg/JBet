package TradeMechanisms.dutchingChase;

import java.awt.Color;
import java.util.Vector;

import bets.BetData;
import bets.BetUtils;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.Utils;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.close.ClosePosition;
import TradeMechanisms.close.ClosePositionOptions;
import TradeMechanisms.dutching.DutchingRunnerOptions;
import TradeMechanisms.dutching.DutchingUtils;

public class DutchingChase extends TradeMechanism implements TradeMechanismListener, MarketChangeListener{

	private Vector<TradeMechanismListener> listeners=new Vector<TradeMechanismListener>();
	
	//args
	private Vector<DutchingChaseOptions> dcov;
	private double globalStake;
	private double percentProfitForceCLose=-1;
	
	//dynamic vars
	private boolean ended=false;
	private boolean pause=false;
	
	private MarketData md;
	
	
	public DutchingChase(TradeMechanismListener bot,Vector<DutchingChaseOptions> dcovA, double globalStakeA ,double percentProfitForceCLoseA) {
		if(bot!=null)
			addTradeMechanismListener(bot);
		
		dcov=dcovA;
		
		globalStake=globalStakeA;
		
		percentProfitForceCLose=percentProfitForceCLoseA;
		
		initialize();
	}
	
	
	public DutchingChase(TradeMechanismListener bot,Vector<DutchingChaseOptions> dcovA, double globalStakeA ) {
		this(bot,dcovA,globalStakeA,-1);
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
		
		md.addMarketChangeListener(this);
		
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
		md.removeMarketChangeListener(this);
		md.removeTradingMechanismTrading(this);
				
		listeners.clear();
		listeners=null;
		
		System.out.println("Dutching Chase clean runned");
		
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

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		
		Vector<OddData> vod=new Vector<OddData>();
	
		boolean someCloseIsNull=false;
		boolean someCloseIsEnded=false; 
		
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			for(DutchingChaseOptions dro:dcov)
			{
				if(dro.getClose()!=null)
				{
					BetData betCloseInfo=dro.getClose().getBetCloseInfo();
					Vector<OddData> vodAux=new Vector<OddData>();
							
					for(OddData od:dro.getClose().getMatchedOddDataVector())
							vodAux.add(od);
					
					if(dro.getClose().isEnded())
						someCloseIsEnded=true;
					else
					{
						OddData odMissing=BetUtils.getGreening(vodAux,betCloseInfo.getOddDataOriginal(),Utils.getOddBackFrame(betCloseInfo.getRd(),0));
						vodAux.add(odMissing);
					}
					
					vod.add(BetUtils.getOpenInfo(vodAux));
					
				}
				else
				{
					someCloseIsNull=true;
				}
				
				
			}
			
			
			if(someCloseIsNull)
			{
				writeMsgToListeners("Some close is null", Color.BLUE);
			}
			else
			{
				 if(!someCloseIsEnded)
					 writeMsgToListeners("No close has ended yet", Color.BLUE);
				 
				 
				 double netNow[]=DutchingUtils.calculateNetProfitLoss(vod);
				 
				 if(percentProfitForceCLose>0)
				 {
					 writeMsgToListeners("Net Profit/Loss if force close now :"+netNow[0] + " (to execute force close = "+(globalStake*percentProfitForceCLose)+")", Color.BLUE);
					 if(netNow[0]>(globalStake*percentProfitForceCLose))
					 {
						 writeMsgToListeners("Net Profit/Loss is gt Stop Profit - Calling forceClose() ", Color.RED);
						 forceClose();
						 
					 }
				 }
				 else
				 {
					 writeMsgToListeners("Net Profit/Loss if force close now :"+netNow[0], Color.BLUE);
				 }
			}
		}
	}
}
