package TradeMechanisms.swing;

import java.awt.Color;
import java.util.Vector;

import DataRepository.RunnersData;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import bets.BetData;

public class swing extends TradeMechanism implements TradeMechanismListener{

	private Vector<TradeMechanismListener> listeners=new Vector<TradeMechanismListener>();
	
	private RunnersData rd;
	private double stakeSize;
	private double entryOdd;
	private int waitFramesOpen;
	private int waitFramesNormal;
	private int waitFramesBestPrice;
	private boolean directionBL;
	private int ticksProfit;
	private int ticksLoss;
	
	public swing(TradeMechanismListener listenerA, RunnersData rdA, double stakeSizeA, double entryOddA,int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,boolean directionBLA,int ticksProfitA,int ticksLossA) {
		super();
		
		rd=rdA;
		stakeSize=stakeSizeA;
		entryOdd=entryOddA;
		waitFramesOpen=waitFramesOpenA;
		waitFramesNormal=waitFramesNormalA;
		waitFramesBestPrice=waitFramesBestPriceA;
		directionBL=directionBLA;
		ticksProfit=ticksProfitA;
		ticksLoss=ticksLossA;
		
		if(listenerA!=null)
			addTradeMechanismListener(listenerA);
		
	}
	
	
	public swing( RunnersData rdA, double stakeSizeA, double entryOddA,int waitFramesOpenA, int waitFramesNormalA,int waitFramesBestPriceA,boolean directionBLA,int ticksProfitA,int ticksLossA) {
		this(null,rdA, stakeSizeA, entryOddA, waitFramesOpenA, waitFramesNormalA, waitFramesBestPriceA, directionBLA, ticksProfitA, ticksLossA);
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clean() {
		// TODO Auto-generated method stub
		
	}

	//--------------------- TradeMechanismListener ------------------------
	@Override
	public void tradeMechanismChangeState(TradeMechanism tm, int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tradeMechanismEnded(TradeMechanism tm, int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {
		// TODO Auto-generated method stub
		
	}
	//--------------------- End TradeMechanismListener ------------------------
	
}
