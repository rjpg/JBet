package TradeMechanisms.trailingStop;

import java.awt.Color;
import java.util.Vector;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.TradeMechanismUtils;
import TradeMechanisms.close.ClosePosition;
import TradeMechanisms.close.ClosePositionOptions;
import TradeMechanisms.open.OpenPosition;
import TradeMechanisms.open.OpenPositionOptions;
import bets.BetData;
import bets.BetUtils;

public class TrailingStop extends TradeMechanism implements TradeMechanismListener,MarketChangeListener{

	private static final int I_OPENING = 0;
	private static final int I_TRAILING = 1;
	private static final int I_CLOSING = 2;
	private static final int I_END = 3;
	
	
	private int I_STATE=I_OPENING;
	
	// data
	private MarketData md;
	
	private boolean ended=false;
	
	private Vector<TradeMechanismListener> listeners=new Vector<TradeMechanismListener>();
	
	// args
	private int waitFramesOpen;
	private int waitFramesNormal;
	private int waitFramesBestPrice;
	private int ticksProfit;
	private int ticksLoss;
	private BetData betOpen=null;
	private boolean forceCloseOnStopLoss=true;
	private int updateInterval=TradeMechanism.SYNC_MARKET_DATA_UPDATE;
	private boolean useStopProfifInBestPrice=false;
	
	private boolean goOnfrontInBestPrice=false;
	private int delayBetweenOpenClose=-1;
	private int delayIgnoreStopLoss=-1;
	
	private double percentageOpen=1.00;
	private boolean insistOpen=false;
	private int  waitFramesLay1000=100;
	
	private int reference=TrailingStopOptions.REF_BEST_PRICE;
	private int movingAverageSamples=1;
		// end args
	
	private double oddStopLoss=0;
	
	private double closeOdd; 
	private int ticksLossRelative;
	private double closeOddStopLoss; 
	
	private OpenPosition open;
	private ClosePositionTrailingStop close;
	
	private BetData betClose=null;
	
	private OddData openInfo=null;
	private OddData closeInfo=null;
	
	private Vector<BetData> matchedInfoOpen=null;
	private Vector<BetData> matchedInfoClose=null;
	
	private boolean pause=false;
	
	// statistics
	private String eventName="No_Name";
	private String marketName="No_Name";
	private double matchedOnRunner=0.0;
	private int secToStart=0;
	private int numberOfRunners=0;
	//---
	
	
	public TrailingStop(TrailingStopOptions tso) {
		this(tso.getDefaultListener(),
				tso.getBetOpenInfo(),
				tso.getWaitFramesOpen(),
				tso.getWaitFramesNormal(),
				tso.getWaitFramesBestPrice(),
				tso.getTicksProfit(),
				tso.getTicksLoss(),
				tso.isForceCloseOnStopLoss(),
				tso.getUpdateInterval(),
				tso.isUseStopProfifInBestPrice(),
				tso.isGoOnfrontInBestPrice(),
				tso.getDelayBetweenOpenClose(),
				tso.getDelayIgnoreStopLoss(),
				tso.getPercentageOpen(),
				tso.isInsistOpen(),
				tso.getWaitFramesLay1000(),
				tso.getReference(),
				tso.getMovingAverageSamples());
	}
	
	public TrailingStop(TradeMechanismListener listenerA, BetData betOpenA, int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int ticksProfitA,int ticksLossA,boolean forceCloseOnStopLossA, int updateIntervalA,boolean useStopProfifInBestPriceA, boolean goOnfrontInBestPriceA, int delayBetweenOpenCloseA,int delayIgnoreStopLossA, double percentageOpenA, boolean insistOpenA,int  waitFramesLay1000A,int referenceA,int movingAverageSamplesA)
	{
		super();
		
		if(betOpenA==null) return;
		
		betOpen=betOpenA;
		
		md=betOpen.getRd().getMarketData();
		
		waitFramesOpen=waitFramesOpenA;
		waitFramesNormal=waitFramesNormalA;
		waitFramesBestPrice=waitFramesBestPriceA;
		
		ticksProfit=ticksProfitA;
		ticksLoss=ticksLossA;
		
		forceCloseOnStopLoss=forceCloseOnStopLossA;
		
		updateInterval=updateIntervalA;
		
		useStopProfifInBestPrice=useStopProfifInBestPriceA;
		
		goOnfrontInBestPrice=goOnfrontInBestPriceA;
		delayBetweenOpenClose=delayBetweenOpenCloseA;
		delayIgnoreStopLoss=delayIgnoreStopLossA;
		percentageOpen=percentageOpenA; 
		insistOpen=insistOpenA;
		waitFramesLay1000=waitFramesLay1000A;
		
		reference=referenceA;
		movingAverageSamples=movingAverageSamplesA;
		
		if(listenerA!=null)
			addTradeMechanismListener(listenerA);
		
		//statistics init data
		eventName=md.getEventName();
		marketName=md.getName();
		matchedOnRunner=betOpen.getRd().getDataFrames().get(0).getMatchedAmount();
		long nowMin=md.getCurrentTime().getTimeInMillis();
		long startMin=md.getStart().getTimeInMillis();
		long sub=startMin-nowMin;
		secToStart=(int)(sub/1000);
		numberOfRunners=md.getRunners().size();
		
		initialize();
	}
	
	public void initialize()
	{
		
		ticksLossRelative=ticksProfit+ticksLoss;
		
		if(betOpen.getType()==BetData.BACK)
		{
			if(ticksProfit<0)
			{
				closeOdd=-1;
				closeOddStopLoss=Utils.indexToOdd((Utils.oddToIndex( betOpen.getOddRequested())+ticksLoss));
			}
			else
			{
				closeOdd=Utils.indexToOdd((Utils.oddToIndex( betOpen.getOddRequested())-ticksProfit));
				if(closeOdd==-1) closeOdd=1.01;
							
				closeOddStopLoss=Utils.indexToOdd((Utils.oddToIndex(closeOdd)+ticksLossRelative));
				System.out.println("Close Odd : "+closeOdd+ "  StopLoss Odd : "+closeOddStopLoss);
			}
		}
		else
		{
			if(ticksProfit<0)
			{
				closeOdd=-1;
				closeOddStopLoss=Utils.indexToOdd((Utils.oddToIndex( betOpen.getOddRequested())-ticksLoss));
			}
			else
			{
				closeOdd=Utils.indexToOdd((Utils.oddToIndex(betOpen.getOddRequested())+ticksProfit));
				if(closeOdd==-1) closeOdd=1000;
				
				closeOddStopLoss=Utils.indexToOdd((Utils.oddToIndex(closeOdd)-ticksLossRelative));
				System.out.println("Close Odd : "+closeOdd+ "  StopLoss Odd : "+closeOddStopLoss);
			}
		}
		
		
		
		oddStopLoss=closeOddStopLoss;
		
		
		writeMsgToListeners("Swing Start in state : "+TradeMechanismUtils.getStateString(STATE), Color.BLUE);
		writeMsgToListeners("Swing Entry Odd : "+betOpen.getOddRequested(), Color.BLUE);
		writeMsgToListeners("Swing Profit Odd : "+closeOdd, Color.BLUE);
		writeMsgToListeners("Swing StopLoss Odd : "+closeOddStopLoss, Color.BLUE);
		open();
		
		
	}
	
	//--------------------------------- TM --------------------------
	
	@Override
	public void forceClose() {
		
		delayBetweenOpenClose=0;
		waitFramesNormal=0;
		waitFramesBestPrice=0;
		
		if(close!=null)
		{
			close.forceClose();
			return;
		}
		
		if(open!=null)
		{
			open.forceClose();
			return;
		}
		
		
		
		
		
	}

	@Override
	public void forceCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getStatisticsFields() {
		return "END_STATE EVENT MARKET RUNNER RUNNER_MATCHED_AMOUNT NUMBER_OF_RUNNERS TIME_TO_START REQUEST_ODD PROFIT_ODD STOPLOSS_ODD DIRECTION TICKS_PROFIT TICKS_LOSS STAKE POTENTIAL_PROFIT POTENTIAL_LOSS PROFIT_LOSS TICKS_MOVED OPEN_ODD CLOSE_ODD OPEN_STAKE CLOSE_STAKE";
	}

	@Override
	public String getStatisticsValues() {
		
		String ret=TradeMechanismUtils.getStateString(getState());
				
		
		ret+=" \""+eventName.replace(" ","_")+"\" \""+marketName.replace(" ","_")+"\" \""+betOpen.getRd().getName().replace(" ","_")+"\" "+matchedOnRunner;
		
		ret+=" "+numberOfRunners;
		
		ret+=" "+secToStart+" "+betOpen.getOddRequested()+" "+closeOdd+" "+closeOddStopLoss;
		
		if(betOpen.getType()==BetData.BACK)	
			ret+=" BL";
		else
			ret+=" LB";
		
		ret+=" "+ticksProfit+" "+ticksLoss+" "+betOpen.getAmount();
		
		double potentialP,potentialL;
				
		if(betOpen.getType()==BetData.BACK)
		{
			double closeAMP=Utils.closeAmountLay(betOpen.getOddRequested(), betOpen.getAmount(),   closeOdd);
			if(closeOdd==-1)
				potentialP=betOpen.getAmount()*(betOpen.getOddRequested()-1);
			else
				potentialP=closeAMP-betOpen.getAmount();
			
			double closeAML=Utils.closeAmountLay(betOpen.getOddRequested(), betOpen.getAmount(),  closeOddStopLoss);
			potentialL=closeAML-betOpen.getAmount();
			
			
		}
		else
		{
			double closeAMP=Utils.closeAmountBack(betOpen.getOddRequested(), betOpen.getAmount(),   closeOdd);
			if(closeOdd==-1)
				potentialP=betOpen.getAmount();
			else
				potentialP=betOpen.getAmount()-closeAMP;
			
			double closeAML=Utils.closeAmountBack(betOpen.getOddRequested(), betOpen.getAmount(),   closeOddStopLoss);
			potentialL=betOpen.getAmount()-closeAML;
			
		
		}
		
		ret+=" "+potentialP+" "+potentialL;
		
		if(getState()!=TradeMechanism.CLOSED)
			return ret+" 0 0 0.00 0.00 0.0 0.0";
		
		int ticksMoved=0;
		
		if(betOpen.getType()==BetData.BACK)
		{
			int openIndex=Utils.oddToIndex(Utils.nearValidOdd(openInfo.getOdd()));
			int closeIndex=Utils.oddToIndex(Utils.nearValidOdd(closeInfo.getOdd()));
			
			ticksMoved=openIndex-closeIndex;
		}
		else
		{
			int openIndex=Utils.oddToIndex(Utils.nearValidOdd(openInfo.getOdd()));
			int closeIndex=Utils.oddToIndex(Utils.nearValidOdd(closeInfo.getOdd()));
			
			ticksMoved=closeIndex-openIndex;
		}
		
		ret+=" "+getEndPL()+" "+ticksMoved;
		
		if(openInfo!=null)
			ret+=" "+openInfo.getOdd();
		else
			ret+=" 0.00";
		
		if(closeInfo!=null)
			ret+=" "+closeInfo.getOdd();
		else
			ret+=" 0.00";
		
		if(openInfo!=null)
			ret+=" "+openInfo.getAmount();
		else
			ret+=" 0.0";
		
		if(closeInfo!=null)
			ret+=" "+closeInfo.getAmount();
		else
			ret+=" 0.0";
		
		return ret;
	}

	@Override
	public void addTradeMechanismListener(TradeMechanismListener listener) {
		listeners.add(listener);
		
	}

	@Override
	public void removeTradeMechanismListener(TradeMechanismListener listener) {
		listeners.remove(listener);
		
	}

	@Override
	public Vector<BetData> getMatchedInfo() {
		Vector<BetData> ret=new Vector<BetData>();
		if(matchedInfoOpen!=null)
			for(BetData bd:matchedInfoOpen)
				ret.add(bd);
		
		if(matchedInfoClose!=null)
			for(BetData bd:matchedInfoClose)
				ret.add(bd);
		
		return ret;
	}

	@Override
	public double getEndPL()
	{
		
		if(openInfo==null || closeInfo==null)
			return 0.00;
		else
		{
			if(openInfo.getType()==BetData.BACK)
				return closeInfo.getAmount()-openInfo.getAmount();
			else
				return openInfo.getAmount()-closeInfo.getAmount();
		}
	}
	
	@Override
	public boolean isEnded() {
		return ended;
	}

	@Override
	public void clean() {
		System.out.println("TrailingStop clean runned");
		
	}
	//--------------------------------- TM END --------------------------

	private void setState(int state)
	{
		System.out.println("seting state");
		if(STATE==state) return;
		
		STATE=state;
		
		writeMsgToListeners("Swing State : "+TradeMechanismUtils.getStateString(STATE), Color.BLUE);
		
		for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
		{
			tml.tradeMechanismChangeState(this, STATE);
		}
	}
	
	//--------------------- TradeMechanismListener ------------------------
	@Override
	public void tradeMechanismChangeState(TradeMechanism tm, int state) {
		if(tm==open)
		{
			if (state==CRITICAL_ERROR)
			{
				setState(CRITICAL_ERROR);
				setI_STATE(I_END);
				refresh();
			}
			
			if(state==PARTIAL_OPEN)
				setState(PARTIAL_OPEN);
		}
		
		if(tm==close)
		{
			if (state==TradeMechanism.CRITICAL_ERROR)
			{
				setState(CRITICAL_ERROR);
				setI_STATE(I_END);
				refresh();
			}
			
			if(state==PARTIAL_CLOSED)
				setState(PARTIAL_CLOSED);
			
		}
		
	}

	@Override
	public void tradeMechanismEnded(TradeMechanism tm, int state) {
		refresh();
		
	}

	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {
		if(tm==open)
			writeMsgToListeners("[OPEN]"+msg, color);
		else if(tm==close)
			writeMsgToListeners("[CLOSE]"+msg, color);
		else
			writeMsgToListeners(msg, color);
		
	}
	//--------------------- End TradeMechanismListener ------------------------
	private int getI_STATE() {
		return I_STATE;
	}

	private void setI_STATE(int i_STATE) {
		I_STATE = i_STATE;
	}
	
	private double getOddReference()
	{
		double ref;
		
		double aux=0;
		if(movingAverageSamples>1 && Utils.isValidWindow(betOpen.getRd(), movingAverageSamples, 0))
		{
			if(reference==TrailingStopOptions.REF_BEST_PRICE)
			{
				
				for(int i=0;i<movingAverageSamples;i++)
				{
					if(betOpen.getType()==BetData.LAY)
						aux+=Utils.getOddBackFrame(betOpen.getRd(), i);
					else
						aux+=Utils.getOddLayFrame(betOpen.getRd(), i);
				}
				aux/=movingAverageSamples;
			} else if(reference==TrailingStopOptions.REF_BEST_OFFER)
			{
				
				for(int i=0;i<movingAverageSamples;i++)
				{
					if(betOpen.getType()==BetData.LAY)
						aux+=Utils.getOddLayFrame(betOpen.getRd(), i);
					else
						aux+=Utils.getOddBackFrame(betOpen.getRd(), i);
				}
				aux/=movingAverageSamples;
			} else // REF_MIDLE
			{
				
				for(int i=0;i<movingAverageSamples;i++)
				{
					aux+=((Utils.getOddLayFrame(betOpen.getRd(), i)+Utils.getOddBackFrame(betOpen.getRd(), i))/2.);
				}
				aux/=movingAverageSamples;
			}
		}
		else
		{
			if(reference==TrailingStopOptions.REF_BEST_PRICE)
			{
				if(betOpen.getType()==BetData.LAY)
					aux=Utils.getOddBackFrame(betOpen.getRd(), 0);
				else
					aux=Utils.getOddLayFrame(betOpen.getRd(), 0);
				
			} else if(reference==TrailingStopOptions.REF_BEST_OFFER)
			{
				if(betOpen.getType()==BetData.LAY)
					aux=Utils.getOddLayFrame(betOpen.getRd(), 0);
				else
					aux=Utils.getOddBackFrame(betOpen.getRd(), 0);
				
			} else // REF_MIDLE
			{
				aux=((Utils.getOddLayFrame(betOpen.getRd(), 0)+Utils.getOddBackFrame(betOpen.getRd(), 0))/2.);
			}
		}
		
		ref=Utils.nearValidOdd(aux);
		
		return ref;
	}
	
	private void updateOddStopLoss()
	{
		if(betOpen.getType()==BetData.LAY)
		{
			double ref=getOddReference();
			ref=Utils.indexToOdd(Utils.oddToIndex(ref)-ticksLoss);
			
			if(ref>oddStopLoss)
				oddStopLoss=ref;
		}
		else
		{
			double ref=getOddReference();
			ref=Utils.indexToOdd(Utils.oddToIndex(ref)+ticksLoss);
			
			if(ref<oddStopLoss)
				oddStopLoss=ref;
		}
	}
	
	
	private void open()
	{
		OpenPositionOptions opo=new OpenPositionOptions(betOpen,this);
		opo.setWaitFrames(waitFramesOpen);
		opo.setUpdateInterval(updateInterval);
		opo.setPercentageOpen(percentageOpen);
		opo.setInsistOpen(insistOpen);
		
		open=new OpenPosition(opo);
	}
	
	private void close()
	{
		if(ticksProfit<1)
		{
			setState(I_TRAILING);
			md.addMarketChangeListener(this);
			
			
		}
		else
		{
			OddData odClose=BetUtils.getEquivalent(openInfo, closeOdd);
			if(openInfo.getType()==BetData.BACK)
				odClose.setType(BetData.LAY);
			else
				odClose.setType(BetData.BACK);
			
			odClose.setRd(betOpen.getRd());
			
			System.out.println("closing on : "+odClose+" stop Loss :"+ticksLoss);
			
			betClose=new BetData(odClose.getRd(),odClose,betOpen.isKeepInPlay());
			/////////////debug
			if(close !=null)
				System.err.println("Running close for the second time in swing !!!!!!");
			/////////////
			
			ClosePositionTrailingStopOptions cpo=new ClosePositionTrailingStopOptions(betClose, this);
			cpo.setMovingStopLossTicks(ticksLoss);
			cpo.setWaitFramesNormal(waitFramesNormal);
			cpo.setWaitFramesUntilForceClose(waitFramesBestPrice);
			cpo.setUpdateInterval(updateInterval);
			cpo.setForceCloseOnStopLoss(forceCloseOnStopLoss);
			cpo.setUseStopProfitInBestPrice(useStopProfifInBestPrice);
			cpo.setGoOnfrontInBestPrice(goOnfrontInBestPrice);
			cpo.setStartDelay(delayBetweenOpenClose);
			cpo.setIgnoreStopLossDelay(delayIgnoreStopLoss);
			cpo.setWaitFramesLay1000(waitFramesLay1000);
			cpo.setReference(reference);
			cpo.setMovingAverageSamples(movingAverageSamples);
			cpo.setInitialRelativeStopLossTicks(ticksLossRelative);
			
			close=new ClosePositionTrailingStop(cpo);
			
			setI_STATE(I_CLOSING);
		}
		
	}
	
	private void refresh()
	{
		switch (getI_STATE()) {
	        case I_OPENING: processOpen(); break;
	        case I_CLOSING: processClose(); break;
	        case I_END    : end(); break;
	        default: end(); break;
		}
		
	}
	
	private void processOpen()
	{
		System.out.println("process Open");
		if(open.isEnded())
		{
			System.out.println("process Open has end");
		
			if(open.getState()==NOT_OPEN )
			{
				setState(NOT_OPEN);
				System.out.println("Did not Open : "+BetUtils.getOpenInfo(open.getMatchedOddDataVector()));
				setI_STATE(I_END);
				refresh();
				return;
			} else if(open.getState()==CRITICAL_ERROR)
			{
				setState(CRITICAL_ERROR);
				System.out.println("Did not Open : "+BetUtils.getOpenInfo(open.getMatchedOddDataVector()));
				setI_STATE(I_END);
				refresh();
				return;
			} else if(open.getState()==UNMONITORED)
			{
				setState(UNMONITORED);
				System.out.println("Did not Open : "+BetUtils.getOpenInfo(open.getMatchedOddDataVector()));
				setI_STATE(I_END);
				refresh();
				return;
			}else if(open.getState()==PARTIAL_OPEN || open.getState()==OPEN)
			{
				setState(OPEN);
				matchedInfoOpen=open.getMatchedInfo();
				openInfo=BetUtils.getOpenInfo(open.getMatchedOddDataVector());
				System.out.println("Open : "+openInfo);
				
				writeMsgToListeners("TralingStop Open with : "+openInfo, Color.BLUE);
				if(openInfo!=null)
					close();
				else
				{
					setState(NOT_OPEN);
					System.out.println("Did not Close : "+openInfo);
					setI_STATE(I_END);
					refresh();
					return;
				}
				return;
			}
			
		}
		else
			System.out.println("process Open did not end");
	
		
	}
	
	void processTrailing()
	{
		if(delayBetweenOpenClose >0)
		{
			delayBetweenOpenClose--;
			return;
		}
		
		updateOddStopLoss();
		
		System.out.println("ODD STOPLOSS : "+oddStopLoss);
		
		boolean closeFlag=false;
		
		if(betOpen.getType()==BetData.LAY)
		{
			if(getOddReference()<oddStopLoss)
				closeFlag=true;
		}
		else
		{
			if(getOddReference()>oddStopLoss)
				closeFlag=true;
		}
		
		if(waitFramesNormal--<0)
		{
			closeFlag=true;
		}
		
		if(closeFlag)
		{
			md.removeMarketChangeListener(this);
			
			OddData odClose;
			if(openInfo.getType()==BetData.BACK)
			{
				odClose=BetUtils.getEquivalent(openInfo, Utils.getOddBackFrame(betOpen.getRd(), 0));
				odClose.setType(BetData.LAY);
			}
			else
			{
				odClose=BetUtils.getEquivalent(openInfo, Utils.getOddLayFrame(betOpen.getRd(), 0));
				odClose.setType(BetData.BACK);
			}
				
			
			odClose.setRd(betOpen.getRd());
			
			System.out.println("closing on : "+odClose+" stop Loss :"+ticksLoss);
			
			betClose=new BetData(odClose.getRd(),odClose,betOpen.isKeepInPlay());
			/////////////debug
			if(close !=null)
				System.err.println("Running close for the second time in swing !!!!!!");
			/////////////
			
			ClosePositionTrailingStopOptions cpo=new ClosePositionTrailingStopOptions(betClose, this);
			cpo.setMovingStopLossTicks(1);
			cpo.setWaitFramesNormal(0);
			cpo.setWaitFramesUntilForceClose(waitFramesBestPrice);
			cpo.setUpdateInterval(updateInterval);
			cpo.setForceCloseOnStopLoss(forceCloseOnStopLoss);
			cpo.setUseStopProfitInBestPrice(useStopProfifInBestPrice);
			cpo.setGoOnfrontInBestPrice(goOnfrontInBestPrice);
			cpo.setStartDelay(-1);
			cpo.setIgnoreStopLossDelay(-1);
			cpo.setWaitFramesLay1000(waitFramesLay1000);
			cpo.setReference(reference);
			cpo.setMovingAverageSamples(0);
			cpo.setInitialRelativeStopLossTicks(1);
			
			close=new ClosePositionTrailingStop(cpo);
			
			setI_STATE(I_CLOSING);

			
		}
		
	}
	
	private void processClose()
	{
		System.out.println("process Close");
		if(close.isEnded())
		{
			System.out.println("process Close has end");
			
			if(close.getState()==CRITICAL_ERROR)
			{
				setState(CRITICAL_ERROR);
				System.out.println("TralingStop Did not close : "+BetUtils.getOpenInfo(close.getMatchedOddDataVector()));
				setI_STATE(I_END);
				refresh();
				return;
			} else if(close.getState()==UNMONITORED)
			{
				setState(UNMONITORED);
				System.out.println("TralingStop Did not close : "+BetUtils.getOpenInfo(close.getMatchedOddDataVector()));
				setI_STATE(I_END);
				refresh();
				return;
			}else if(close.getState()==CLOSED)
			{
				setState(CLOSED);
				matchedInfoClose=close.getMatchedInfo();
				closeInfo=BetUtils.getOpenInfo(close.getMatchedOddDataVector());
				System.out.println("Did close with : "+closeInfo);
				writeMsgToListeners("TralingStop Closed with : "+closeInfo, Color.BLUE);
				setI_STATE(I_END);
				refresh();
				return;
			}else if(close.getState()==PARTIAL_CLOSED )
			{
				setState(PARTIAL_CLOSED);
				matchedInfoClose=close.getMatchedInfo();
				closeInfo=BetUtils.getOpenInfo(close.getMatchedOddDataVector());
				System.out.println("Did not close with : "+closeInfo);
				writeMsgToListeners("TralingStop not Closed with : "+closeInfo, Color.BLUE);
				setI_STATE(I_END);
				refresh();
				return;
			}else if(open.getState()==OPEN)
			{
				setState(OPEN);
				matchedInfoClose=close.getMatchedInfo();
				closeInfo=BetUtils.getOpenInfo(close.getMatchedOddDataVector());
				System.out.println("Did not close with : "+closeInfo);
				writeMsgToListeners("TralingStop not Closed with : "+closeInfo, Color.BLUE);
				setI_STATE(I_END);
				refresh();
				return;
			}
			
		}
		else
			System.out.println("process Close did not end");
	
	}
	
	private void end()
	{
		md.removeTradingMechanismTrading(this);
		
		ended=true;
		
		writeMsgToListeners("TralingStop End in State : "+TradeMechanismUtils.getStateString(STATE), Color.BLUE);
		informListenersEnd();
		
		
		System.out.println("TralingStop ended");
		
		clean();
	}
	
	private void informListenersEnd()
	{
		for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
		{
			tml.tradeMechanismEnded(this, STATE);
		}
	}
	
	private void writeMsgToListeners(String msg, Color color)
	{
		for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
		{
			tml.tradeMechanismMsg(this, msg, color);
		}
	}

	@Override
	public void setPause(boolean pauseA) {
		
		pause=pauseA;
		
		if(open!=null) open.setPause(pauseA);
		if(close!=null) close.setPause(pauseA);
		
		
	}

	@Override
	public boolean isPause() {
		return pause;
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			processTrailing();
		}
		
	}
}
