package TradeMechanisms.swing;

import java.awt.Color;
import java.util.Vector;

import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.dutching.DutchingRunnerOptions;
import TradeMechanisms.dutching.DutchingUtils;
import TradeMechanisms.open.OpenPosition;
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
	// end args
	
	private double closeOdd; 
	private int ticksLossRelative;
	
	private OpenPosition open;
	private OpenPosition close;

	
	public Swing(TradeMechanismListener listenerA, BetData betOpenA, int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int ticksProfitA,int ticksLossA,boolean forceCloseOnStopLossA)
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
		
		if(listenerA!=null)
			addTradeMechanismListener(listenerA);
		
		initialize();
	}

	
	public Swing(TradeMechanismListener listenerA, BetData betOpenA, int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int ticksProfitA,int ticksLossA) {
		this(listenerA, betOpenA, waitFramesOpenA,waitFramesNormalA,waitFramesBestPriceA,ticksProfitA,ticksLossA,true);
	}
	
	public Swing(TradeMechanismListener listenerA, RunnersData rdA, double stakeSizeA, double entryOddA,int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,int directionBLA,int ticksProfitA,int ticksLossA,boolean forceCloseOnStopLossA, boolean ipKeepA) {
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
		
		if(listenerA!=null)
			addTradeMechanismListener(listenerA);
		
		initialize();
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
			
			System.out.println("Close Odd : "+closeOdd+ "  StopLoss Odd : "+Utils.indexToOdd((Utils.oddToIndex(closeOdd)+ticksLossRelative)));
		}
		else
		{
			closeOdd=Utils.indexToOdd((Utils.oddToIndex(betOpen.getOddRequested())+ticksProfit));
			if(closeOdd==-1) closeOdd=1000;
			
			System.out.println("Close Odd : "+closeOdd+ "  StopLoss Odd : "+Utils.indexToOdd((Utils.oddToIndex(closeOdd)-ticksLossRelative)));
		}
		
		
		
		open();
		
		
	}
	
	//--------------------------------- TM --------------------------
	
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnded() {
		return ended;
	}

	@Override
	public void clean() {
		// TODO Auto-generated method stub
		
	}
	//--------------------------------- TM END --------------------------

	private void setState(int state)
	{
		System.out.println("seting state");
		if(STATE==state) return;
		
		STATE=state;
		
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
		// TODO Auto-generated method stub
		
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
		open=new OpenPosition(this, betOpen, waitFramesOpen);
	}
	
	private void close(OddData od)
	{
		
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
				OddData od=BetUtils.getOpenInfo(open.getMatchedOddDataVector());
				System.out.println("Open : "+od);
				if(od!=null)
					close(BetUtils.getOpenInfo(open.getMatchedOddDataVector()));
				else
				{
					setState(NOT_OPEN);
					System.out.println("Did not Open : "+od);
					setI_STATE(I_END);
					refresh();
					return;
				}
					
			}
			
		}
		else
			System.out.println("process Open did not end");
	
		
	}
	
	private void processClose()
	{
		
	}
	
	private void end()
	{
		md.removeTradingMechanismTrading(this);
		
		ended=true;
		
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
}
