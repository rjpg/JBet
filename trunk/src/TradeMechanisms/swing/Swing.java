package TradeMechanisms.swing;

import java.awt.Color;
import java.util.Vector;

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

public class Swing extends TradeMechanism implements TradeMechanismListener{

	private static final int I_OPENING = 0;
	private static final int I_CLOSING = 1;
	private static final int I_END = 2;
	
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
		// end args
	
	private double closeOdd; 
	private int ticksLossRelative;
	private double closeOddStopLoss; 
	
	private OpenPosition open;
	private ClosePosition close;
	
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
	
	
	public Swing(SwingOptions so) {
		this(so.getDefaultListener(),
				so.getBetOpenInfo(),
				so.getWaitFramesOpen(),
				so.getWaitFramesNormal(),
				so.getWaitFramesBestPrice(),
				so.getTicksProfit(),
				so.getTicksLoss(),
				so.isForceCloseOnStopLoss(),
				so.getUpdateInterval(),
				so.isUseStopProfifInBestPrice(),
				so.isGoOnfrontInBestPrice(),
				so.getDelayBetweenOpenClose(),
				so.getDelayIgnoreStopLoss(),
				so.getPercentageOpen(),
				so.isInsistOpen(),
				so.getWaitFramesLay1000());
	}
	
	public Swing(TradeMechanismListener listenerA, BetData betOpenA, int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int ticksProfitA,int ticksLossA,boolean forceCloseOnStopLossA, int updateIntervalA,boolean useStopProfifInBestPriceA, boolean goOnfrontInBestPriceA, int delayBetweenOpenCloseA,int delayIgnoreStopLossA, double percentageOpenA, boolean insistOpenA,int  waitFramesLay1000A)
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
	
	public Swing(TradeMechanismListener listenerA, BetData betOpenA, int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int ticksProfitA,int ticksLossA,boolean forceCloseOnStopLossA, int updateIntervalA)
	{
		this(listenerA, betOpenA, waitFramesOpenA, waitFramesNormalA, waitFramesBestPriceA, ticksProfitA, ticksLossA, forceCloseOnStopLossA,updateIntervalA,false,false,-1,-1, 1.00, false,100);
	}
	
	public Swing(TradeMechanismListener listenerA, BetData betOpenA, int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int ticksProfitA,int ticksLossA,boolean forceCloseOnStopLossA)
	{
		this(listenerA, betOpenA, waitFramesOpenA, waitFramesNormalA, waitFramesBestPriceA, ticksProfitA, ticksLossA, forceCloseOnStopLossA,TradeMechanism.SYNC_MARKET_DATA_UPDATE);
	}

	
	public Swing(TradeMechanismListener listenerA, BetData betOpenA, int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int ticksProfitA,int ticksLossA) {
		this(listenerA, betOpenA, waitFramesOpenA,waitFramesNormalA,waitFramesBestPriceA,ticksProfitA,ticksLossA,true);
	}
	
	public Swing(TradeMechanismListener listenerA, RunnersData rdA, double stakeSizeA, double entryOddA,int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int directionBLA,int ticksProfitA,int ticksLossA,boolean forceCloseOnStopLossA, boolean ipKeepA,int updateIntervalA, boolean useStopProfifInBestPriceA) {
		super();
		
		if(directionBLA==BetData.BACK)
			betOpen=new BetData(rdA,stakeSizeA,entryOddA,BetData.BACK,ipKeepA);
		else
			betOpen=new BetData(rdA,stakeSizeA,entryOddA,BetData.LAY,ipKeepA);
		
		md=betOpen.getRd().getMarketData();
		
		//this(listenerA,betOpen,waitFramesOpenA, waitFramesNormalA,waitFramesBestPriceA,ticksProfitA,ticksLossA);
		waitFramesOpen=waitFramesOpenA;
		waitFramesNormal=waitFramesNormalA;
		waitFramesBestPrice=waitFramesBestPriceA;
		ticksProfit=ticksProfitA;
		ticksLoss=ticksLossA;
		
		forceCloseOnStopLoss=forceCloseOnStopLossA;
		
		updateInterval=updateIntervalA;
		
		useStopProfifInBestPrice=useStopProfifInBestPriceA;
		
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
	
	public Swing(TradeMechanismListener listenerA, RunnersData rdA, double stakeSizeA, double entryOddA,int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int directionBLA,int ticksProfitA,int ticksLossA,boolean forceCloseOnStopLossA, boolean ipKeepA,int updateIntervalA) {
		this( listenerA, rdA, stakeSizeA, entryOddA, waitFramesOpenA, waitFramesNormalA, waitFramesBestPriceA, directionBLA, ticksProfitA, ticksLossA, forceCloseOnStopLossA,  ipKeepA,updateIntervalA,false);
	}
		
	public Swing(TradeMechanismListener listenerA, RunnersData rdA, double stakeSizeA, double entryOddA,int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int directionBLA,int ticksProfitA,int ticksLossA,boolean forceCloseOnStopLossA, boolean ipKeepA) {
		this( listenerA, rdA, stakeSizeA, entryOddA, waitFramesOpenA, waitFramesNormalA, waitFramesBestPriceA, directionBLA, ticksProfitA, ticksLossA, forceCloseOnStopLossA,  ipKeepA,TradeMechanism.SYNC_MARKET_DATA_UPDATE);
	}
	
	public Swing(TradeMechanismListener listenerA, RunnersData rdA, double stakeSizeA, double entryOddA,int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int directionBLA,int ticksProfitA,int ticksLossA,boolean forceCloseOnStopLossA, boolean ipKeepA,boolean useStopProfifInBestPriceA) {
		this( listenerA, rdA, stakeSizeA, entryOddA, waitFramesOpenA, waitFramesNormalA, waitFramesBestPriceA, directionBLA, ticksProfitA, ticksLossA, forceCloseOnStopLossA,  ipKeepA,TradeMechanism.SYNC_MARKET_DATA_UPDATE,useStopProfifInBestPriceA);
	}
	
	public Swing(TradeMechanismListener listenerA, RunnersData rdA, double stakeSizeA, double entryOddA,int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int directionBLA,int ticksProfitA,int ticksLossA,boolean forceCloseOnStopLossA) {
		this( listenerA, rdA, stakeSizeA, entryOddA,waitFramesOpenA, waitFramesNormalA,waitFramesBestPriceA,directionBLA,ticksProfitA,ticksLossA,true,false);
	}
	
	public Swing(TradeMechanismListener listenerA, RunnersData rdA, double stakeSizeA, double entryOddA,int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int directionBLA,int ticksProfitA,int ticksLossA) {
		this( listenerA, rdA, stakeSizeA, entryOddA,waitFramesOpenA, waitFramesNormalA,waitFramesBestPriceA,directionBLA,ticksProfitA,ticksLossA,true);
	}
		
	public Swing( RunnersData rdA, double stakeSizeA, double entryOddA,int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int directionBLA,int ticksProfitA,int ticksLossA) {
		this(null,rdA, stakeSizeA, entryOddA, waitFramesOpenA, waitFramesNormalA, waitFramesBestPriceA, directionBLA, ticksProfitA, ticksLossA);
	}
	
	public void initialize()
	{
		
		ticksLossRelative=ticksProfit+ticksLoss;
		
		if(betOpen.getType()==BetData.BACK)
		{
			closeOdd=Utils.indexToOdd((Utils.oddToIndex( betOpen.getOddRequested())-ticksProfit));
			if(closeOdd==-1) closeOdd=1.01;
						
			closeOddStopLoss=Utils.indexToOdd((Utils.oddToIndex(closeOdd)+ticksLossRelative));
			System.out.println("Close Odd : "+closeOdd+ "  StopLoss Odd : "+closeOddStopLoss);
		}
		else
		{
			closeOdd=Utils.indexToOdd((Utils.oddToIndex(betOpen.getOddRequested())+ticksProfit));
			if(closeOdd==-1) closeOdd=1000;
			
			closeOddStopLoss=Utils.indexToOdd((Utils.oddToIndex(closeOdd)-ticksLossRelative));
			System.out.println("Close Odd : "+closeOdd+ "  StopLoss Odd : "+closeOddStopLoss);
		}
		
		writeMsgToListeners("Swing Start in state : "+TradeMechanismUtils.getStateString(STATE), Color.BLUE);
		writeMsgToListeners("Swing Entry Odd : "+betOpen.getOddRequested(), Color.BLUE);
		writeMsgToListeners("Swing Profit Odd : "+closeOdd, Color.BLUE);
		writeMsgToListeners("Swing StopLoss Odd : "+closeOddStopLoss, Color.BLUE);
		open();
		
		
	}
	
	//--------------------------------- TM --------------------------
	
	@Override
	public void forceClose() {
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
			potentialP=closeAMP-betOpen.getAmount();
			
			double closeAML=Utils.closeAmountLay(betOpen.getOddRequested(), betOpen.getAmount(),  closeOddStopLoss);
			potentialL=closeAML-betOpen.getAmount();
			
			
		}
		else
		{
			double closeAMP=Utils.closeAmountBack(betOpen.getOddRequested(), betOpen.getAmount(),   closeOdd);
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
		System.out.println("Swind clean runned");
		
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
		
		OddData odClose=BetUtils.getEquivalent(openInfo, closeOdd);
		if(openInfo.getType()==BetData.BACK)
			odClose.setType(BetData.LAY);
		else
			odClose.setType(BetData.BACK);
		
		odClose.setRd(betOpen.getRd());
		
		System.out.println("closing on : "+odClose+" stop Loss :"+ticksLossRelative);
		
		betClose=new BetData(odClose.getRd(),odClose,betOpen.isKeepInPlay());
		/////////////debug
		if(close !=null)
			System.err.println("Running close for the second time in swing !!!!!!");
		/////////////
		
		ClosePositionOptions cpo=new ClosePositionOptions(betClose, this);
		cpo.setStopLossTicks(ticksLossRelative);
		cpo.setWaitFramesNormal(waitFramesNormal);
		cpo.setWaitFramesUntilForceClose(waitFramesBestPrice);
		cpo.setUpdateInterval(updateInterval);
		cpo.setForceCloseOnStopLoss(forceCloseOnStopLoss);
		cpo.setUseStopProfitInBestPrice(useStopProfifInBestPrice);
		cpo.setGoOnfrontInBestPrice(goOnfrontInBestPrice);
		cpo.setStartDelay(delayBetweenOpenClose);
		cpo.setIgnoreStopLossDelay(delayIgnoreStopLoss);
		cpo.setWaitFramesLay1000(waitFramesLay1000);
		
		close=new ClosePosition(cpo);
		
		setI_STATE(I_CLOSING);
		
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
				
				writeMsgToListeners("Swing Open with : "+openInfo, Color.BLUE);
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
	
	private void processClose()
	{
		System.out.println("process Close");
		if(close.isEnded())
		{
			System.out.println("process Close has end");
			
			if(close.getState()==CRITICAL_ERROR)
			{
				setState(CRITICAL_ERROR);
				System.out.println("Did not close : "+BetUtils.getOpenInfo(close.getMatchedOddDataVector()));
				setI_STATE(I_END);
				refresh();
				return;
			} else if(close.getState()==UNMONITORED)
			{
				setState(UNMONITORED);
				System.out.println("Did not close : "+BetUtils.getOpenInfo(close.getMatchedOddDataVector()));
				setI_STATE(I_END);
				refresh();
				return;
			}else if(close.getState()==CLOSED)
			{
				setState(CLOSED);
				matchedInfoClose=close.getMatchedInfo();
				closeInfo=BetUtils.getOpenInfo(close.getMatchedOddDataVector());
				System.out.println("Did close with : "+closeInfo);
				writeMsgToListeners("Swing Closed with : "+closeInfo, Color.BLUE);
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
		
		writeMsgToListeners("Swing End in State : "+TradeMechanismUtils.getStateString(STATE), Color.BLUE);
		informListenersEnd();
		
		
		System.out.println("Swing ended");
		
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
}
