package TradeMechanisms.dutching;

import generated.exchange.BFExchangeServiceStub.GetMarket;

import java.awt.Color;
import java.util.Vector;

import bets.BetData;
import bets.BetUtils;

import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.Utils;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.close.ClosePosition;
import TradeMechanisms.open.OpenPosition;

public class Dutching extends TradeMechanism implements TradeMechanismListener{
	
	private static final int I_OPENING = 0;
	private static final int I_CLOSING = 1;
	private static final int I_END = 2;
	
	private int I_STATE=I_OPENING;
	
	// data
	private boolean useKeeps=false;
	private MarketData md;

	private double globalStake=0;
	private int timeWaitOpen=10;
	private Vector<TradeMechanismListener> listeners=new Vector<TradeMechanismListener>();
	
	// dynamic data
	private Vector<DutchingRunnerOptions> vdro=new Vector<DutchingRunnerOptions>();
	
	private Vector<DutchingRunnerOptions> vdroOpen=new Vector<DutchingRunnerOptions>();
	
	
	boolean ended=false;
	
	
	public Dutching(TradeMechanismListener bot, Vector<DutchingRunnerOptions> vdroA,double globalStakeA, int timeWaitOpenA , boolean useKeepsA) {
	
		super();
		
		
		vdro=vdroA;
		globalStake=globalStakeA;
		timeWaitOpen=timeWaitOpenA;
		
		useKeeps=useKeepsA;
		
		if(bot!=null)
			addTradeMechanismListener(bot);
		
		initialize();
	}
	
	public Dutching(TradeMechanismListener bot, Vector<DutchingRunnerOptions> vdroA,double globalStakeA, int timeWaitOpenA ) {
		this(null,vdroA,globalStakeA,timeWaitOpenA,false);
	}
	
	public Dutching(Vector<DutchingRunnerOptions> vdroA,double globalStakeA, int timeWaitOpenA) {
	
		this(null,vdroA,globalStakeA,timeWaitOpenA);
	}
	
	public void initialize()
	{
		
		if(vdro==null || vdro.size()==0)
		{
			setI_STATE(I_END);
			refresh();
			return;
		}
		
		
		
		md=vdro.get(0).getRd().getMarketData();
		md.addTradingMechanismTrading(this);
		
		boolean hasOpen=false;
		
		Vector<OddData> vod=new Vector<OddData>();
		for(DutchingRunnerOptions dro:vdro)
		{
			if(dro.isOpen())
			{
				hasOpen=true;
				OddData od=new OddData(dro.getOddOpenInfo(),0.01,BetData.BACK,dro.getRd());
				vod.add(od);
				dro.setOddData(od);
				vdroOpen.add(dro);
			}
			else
			{
				double oddBack=Utils.getOddBackFrame(dro.getRd(),0); // actual odd Back to close
				
				OddData od=new OddData(oddBack,0.01,BetData.BACK,dro.getRd());
				vod.add(od);
				dro.setOddData(od);
				
			}
			
			DutchingUtils.calculateAmounts(vod, globalStake);
			
		}
		if(hasOpen)
		{
			setState(TradeMechanism.NOT_OPEN);
			open();
		}
		else
		{
			setState(TradeMechanism.OPEN);
			close();
		}
		
	}
	
	
	//----------------------------------------------- TradeMechanism
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
	public Vector<BetData> getMatchedInfo()
	{
		Vector<BetData> ret=new Vector<BetData>();
		
		for (DutchingRunnerOptions dro:vdro)
		{
			for (BetData bd:dro.getMatchedInfo())
				ret.add(bd);
		}
		
		return ret; 
	}
	
	@Override
	public boolean isEnded() {
		return ended;
	}
	
	@Override
	public void clean() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTradeMechanismListener(TradeMechanismListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeTradeMechanismListener(TradeMechanismListener listener) {
		listeners.remove(listener);
	}

	//----------------------------------------- listener
	@Override
	public void tradeMechanismChangeState(TradeMechanism tm, int state) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public synchronized void  tradeMechanismEnded(TradeMechanism tm, int state) {
		refresh();
		
	}


	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {
		// TODO Auto-generated method stub
		
	}

	//---------------------------------------- implementation
	
	public int getI_STATE() {
		return I_STATE;
	}

	public void setI_STATE(int i_STATE) {
		I_STATE = i_STATE;
	}
	
	private void setState(int state)
	{
		if(STATE==state) return;
		
		STATE=state;
		
		for(TradeMechanismListener tml: listeners.toArray(new TradeMechanismListener[]{}))
		{
			tml.tradeMechanismChangeState(this, STATE);
		}
	}
	
	private void open()
	{
		Vector<BetData> betsOpen=new Vector<BetData>();
		for(DutchingRunnerOptions dro:vdroOpen )
		{
			BetData bd=new BetData(dro.getRd(), dro.getOddData(),useKeeps);
			betsOpen.add(bd);
		}
		
		md.getBetManager().placeBets(betsOpen);
		
		for(DutchingRunnerOptions dro:vdroOpen )
		{
			for(BetData bd:betsOpen)
			{
				if(dro.getRd()==bd.getRd())
				{
					OpenPosition open=new OpenPosition(this, bd, timeWaitOpen);
					dro.setOpen(open);
				}
			}
		}
		
		setI_STATE(I_OPENING);
		refresh();
	}
	
	private void close()
	{
		Vector<BetData> betsClose=new Vector<BetData>();
		
		for(DutchingRunnerOptions dro:vdro)
		{
			OddData greening = BetUtils.getGreening(dro.getMatchedInfoOddData(), dro.getOddData(), dro.getActualOdd());
			if(Utils.convertAmountToBF(greening.getAmount())>0)
			{
				BetData bd=new BetData(dro.getRd(), dro.getOddData(),useKeeps); // (1) use initial odddata to close 
				betsClose.add(bd);
			}
		}
		
		
		md.getBetManager().placeBets(betsClose);
		
		for(DutchingRunnerOptions dro:vdro )
		{
			for(BetData bd:betsClose)
			{
				if(dro.getRd()==bd.getRd())
				{
					ClosePosition close=new ClosePosition(this, bd,1,0,dro.getTimeHoldForceClose(),false); // using (1) we can parametrize the close
					dro.setClose(close);
				}
			}
		}

		
		setI_STATE(I_CLOSING);
		refresh();
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
	
	private boolean allOpenEndedProcessed=false;
	private void processOpen()
	{
		
		// 0 - none matched ( end - do not close nothing)
		// 1 - some matched but not completed ( recalculate globalstake and do close)
		// 2 - at least one completed matched ( do close)
		int state=0; 
		
		boolean allEnded=true;

		DutchingRunnerOptions droBigestMatch=vdroOpen.get(0);
		for(DutchingRunnerOptions dro:vdroOpen )
		{
			if(!dro.getOpen().isEnded())
			{
				allEnded=false;
			}
			
			if(dro.getOpen().getState()==TradeMechanism.OPEN)
			{
				setState(TradeMechanism.PARTIAL_OPEN);
				state=2;
			}
			
			if(dro.getOpen().getState()==TradeMechanism.PARTIAL_OPEN && state<1)
			{
				setState(TradeMechanism.PARTIAL_OPEN);
				state=1;
			} 
			
			OddData odNow=BetUtils.getOpenInfoBetData(dro.getOpen().getMatchedInfo());
			OddData odBigest=BetUtils.getOpenInfoBetData(droBigestMatch.getOpen().getMatchedInfo());
			if(odBigest==null)
			{
				droBigestMatch=dro;
			}
			else if (odNow!=null)
			{
				if(odNow.getAmount()*odNow.getOdd()>odBigest.getAmount()*odBigest.getOdd())
					droBigestMatch=dro;
					
			}
			
		}
		
		
		if(allEnded && !allOpenEndedProcessed)
		{
			
			System.out.println("All OPEN ENDED");
			
			allOpenEndedProcessed=true;
			
			if(state==0)
			{
				setI_STATE(I_END);
				refresh();
				return;
			}
			
			if(state==1) // recalculate global stake
			{
				OddData odBigest=BetUtils.getOpenInfoBetData(droBigestMatch.getOpen().getMatchedInfo());
				
				Vector<OddData> vod=new Vector<OddData>();
				for(DutchingRunnerOptions dro:vdro)
					vod.add(dro.getOddData());
				
				OddData equivalent= BetUtils.getEquivalent(odBigest, droBigestMatch.getOddOpenInfo());
				globalStake=DutchingUtils.calculateGlobalStake(equivalent, DutchingUtils.calculateMargin(vod));
				
				DutchingUtils.calculateAmounts(vod, globalStake);
			}
			
			//if(state==2) //do nothing just close
			
			setState(TradeMechanism.OPEN);
			
			close();
			
		}
			
		
	}
	
	private void processClose()
	{
		boolean allEnded=true;
		
		for(DutchingRunnerOptions dro:vdro)
			if(dro.getClose()!=null)
			{
				if(!dro.getClose().isEnded())
					allEnded=false;
			
			
				if(dro.getClose().getState()==TradeMechanism.PARTIAL_CLOSED || dro.getClose().getState()==TradeMechanism.CLOSED)
					setState(TradeMechanism.PARTIAL_CLOSED);
			}
		
		if(allEnded)
		{
			System.out.println("All CLOSED ENDED");
			setState(TradeMechanism.CLOSED);
			setI_STATE(I_END);
			refresh();
		}
	}
	
	private void end()
	{
		md.removeTradingMechanismTrading(this);
		
		ended=true;
		
		informListenersEnd();
		
		System.out.println("Dutching All ended");
		
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
