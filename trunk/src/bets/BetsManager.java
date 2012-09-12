package bets;

import generated.exchange.BFExchangeServiceStub.BetStatusEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.MUBet;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.PlaceBetsErrorEnum;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResult;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResultEnum;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Vector;

import main.Manager;

import demo.handler.ExchangeAPI;
import demo.util.Display;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import DataRepository.MarketData.MarketThread;


public class BetsManager  {

	public MarketData md;
	
	public Vector<BetData> bets=new Vector<BetData>();
	
	public Vector<BetListener> betListeners=new Vector<BetListener>();
	
	public Vector<BetData> copyBets=new Vector<BetData>();
	
	
	// THREAD
	private BetsManagerThread as;
	private Thread t;
	private boolean polling = false;
	//------ demand freq------
	protected int updateInterval = 400;
	
	
	public BetsManager(MarketData maA) {
		setMd(maA);
		
	}
	
	public void addBetListener(BetListener bl)
	{
		betListeners.add(bl);
	}
	
	public void removeBetListener(BetListener bl)
	{
		betListeners.remove(bl);
	}
	
	public void update()
	{
		
	}
	
	
	public BetData placeBet(BetData bd)
	{
		BetData ret=null;
		
		return ret;
	}
	
	public BetData[] placeBets(BetData[] bds)
	{
		PlaceBets[] betsAPI=new PlaceBets[bds.length];
		
		for(int i=0;i<bds.length;i++)
			betsAPI[i]=BetUtils.createPlaceBet(bds[i]);
		
		PlaceBetsResult[] betResult=null;
		
		try {
			betResult=ExchangeAPI.placeBets(getMd().getSelectedExchange(),  Manager.apiContext, betsAPI);
		} catch (Exception e) {
			e.printStackTrace();		
		}
		
		if(betResult==null)
		{
			for(int i=0;i<bds.length;i++)
			{
				bds[i].setState(BetData.PLACING_ERROR);
				bets.add(bds[i]);
			}
			return bds;
		}
		
		for(int i=0;i<bds.length;i++)
		{
			if (betResult[i].getSuccess()) {
				
				if(betResult[i].getBetId()!=0)
				{
					bds[i].setBetID(betResult[i].getBetId());
				
					if( betResult[i].getSizeMatched()>0)
					{
						bds[i].setMatchedAmount(betResult[i].getSizeMatched());
						bds[i].setOddMached(betResult[i].getAveragePriceMatched());
						if(Utils.convertAmountToBF(bds[i].getAmount())==bds[i].getMatchedAmount())
							bds[i].setState(BetData.MATHED);
						else
							bds[i].setState(BetData.PARCIAL_MACHED);
					}
					bets.add(bds[i]);
				}
				else
				{
					bds[i].setState(BetData.PLACING_ERROR);
				}	
			} 
			else
			{
				if(betResult[i].getResultCode()==PlaceBetsResultEnum.BET_IN_PROGRESS)
				{
					bds[i].setState(BetData.BET_IN_PROGRESS);
					//betsInProgress.add(bds[i]);
					bets.add(bds[i]);
				}
				else
				{
					bds[i].setState(BetData.PLACING_ERROR);
				}
			}
			
			
		}
		
		return bds;
	}
	
	public int cancelBet(BetData bd)
	{
		return 0;
	}
	
	public MarketData getMd() {
		return md;
	}

	public void setMd(MarketData md) {
		this.md = md;
	}
	

	public void updateCopyBets()
	{
		copyBets.clear();
		for(BetData bet:bets)
		{
			BetData betAux=new BetData(bet.getOwner(), bet.getRd(), bet.getAmount(), bet.getOddRequested(), bet.getType(), bet.getTimestamp());
			betAux.setMatchedAmount(bet.getMatchedAmount());
			betAux.setOddMached(bet.getOddMached());
			betAux.setState(bet.getState());
			
			copyBets.add(betAux);
		}
	}
	
	private BetData search(Vector<BetData> bdv,long ID)
	{
		for(BetData bd:bdv)
			if(bd.getBetID()==ID)
				return bd;
		return null;
	}
	
	public void refresh()
	{
		MUBet[] betsbf=null;
		try {
			betsbf = ExchangeAPI.getMUBets(md.getSelectedExchange(), Manager.apiContext, md.getSelectedMarket().getMarketId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(betsbf==null)
			return;
		
		//Vector<BetData> betsInProgress=new Vector<BetData>();
		boolean BetInProgressExist=false;
		
		Vector<BetData> betDataVecAux =new Vector<BetData>();
		
		for(MUBet mubet:betsbf)
		{
			BetData bdAux=search(betDataVecAux,mubet.getBetId());
			
			if(bdAux==null)
			{
				
				if( mubet.getBetType()==BetTypeEnum.Factory.fromValue("B"))
				{
					bdAux=new BetData(null, getMd().getRunnersById(mubet.getSelectionId()), 0.0,0.0, BetData.BACK, null);
				}
				else
				{
					bdAux=new BetData(null, getMd().getRunnersById(mubet.getSelectionId()), 0.0,0.0, BetData.LAY, null);
				}
				
				bdAux.setBetID(mubet.getBetId());
				
				betDataVecAux.add(bdAux);
			}
			
			if(mubet.getBetStatus()==BetStatusEnum.M)
			{
				if(bdAux.getOddMached()==-1)
				{
					bdAux.setMatchedAmount(mubet.getSize());
					bdAux.setOddMached(mubet.getPrice());
				}
				else
				{
					double oddAvg=Utils.calculateOddAverage(new double[]{bdAux.getOddMached(),mubet.getPrice()}, new double[]{bdAux.getMatchedAmount(),mubet.getSize()});
					bdAux.setOddMached(oddAvg);
					bdAux.setMatchedAmount(mubet.getSize()+bdAux.getMatchedAmount());
				}
			}
			
			if(mubet.getBetStatus()==BetStatusEnum.U)
			{
				bdAux.setAmount(mubet.getSize());
				bdAux.setOddRequested(mubet.getPrice());
			}
		}
		
		for(BetData bdAux:bets)
		{
			
			if(bdAux.getState()!=BetData.BET_IN_PROGRESS )
			{
				BetData bdAux2=search(betDataVecAux, bdAux.getBetID());
				
				if(bdAux2!=null)
				{
					
					bdAux.setMatchedAmount(bdAux2.getMatchedAmount());
					bdAux.setOddMached(bdAux2.getOddMached());
				
					//bdAux2.updatesBetInProgress=-1;
					betDataVecAux.remove(bdAux2);
					
					if(bdAux.getState()!=BetData.CANCELED)
					{
						if(bdAux.getMatchedAmount()==0)
							bdAux.setState(BetData.UNMATHED);
						else if(bdAux2.getAmount()==0)//if(Utils.convertAmountToBF(bdAux.getAmount())==bdAux.getMatchedAmount())
							bdAux.setState(BetData.MATHED);
						else
							bdAux.setState(BetData.PARCIAL_MACHED);
					}
				}
			}
			else
			{
				BetInProgressExist=true;
			}	
		}
		
		if(BetInProgressExist)
		{
			assignBetInProgress(betDataVecAux);
		}
		
		Display.showBets(md.getSelectedMarket(), betsbf);
	}
	
	void assignBetInProgress(Vector<BetData> externalBets)
	{
		for(BetData bd:bets)
		{
			
			
			if(bd.getState()==BetData.BET_IN_PROGRESS)
			{
				boolean recovered = false;

				for(BetData bdaux:externalBets)
				{
					if(bdaux.updatesBetInProgress>=0)
					{
						if(bdaux.getRd().getId()==bd.getRd().getId() && bdaux.getType()==bd.getType())
						{
							if(bdaux.getAmount()!=0 && bdaux.getOddRequested()==bd.getOddRequested())
							{
								bd.setMatchedAmount(bdaux.getMatchedAmount());
								bd.setOddMached(bdaux.getOddMached());
								
								if(bd.getMatchedAmount()==0)
									bd.setState(BetData.UNMATHED);
								else
									bd.setState(BetData.PARCIAL_MACHED);
								
								bdaux.updatesBetInProgress=-1;  //assigned recovered 
								
								recovered=true;
								
							}
							
							if(bdaux.getAmount()==0)
							{
								bd.setMatchedAmount(bdaux.getMatchedAmount());
								bd.setOddMached(bdaux.getOddMached());
								bd.setState(BetData.MATHED);
								
								bdaux.updatesBetInProgress=-1;  //assigned recovered 
								
								recovered=true;
							}
						}
					}
				}
			
				if(!recovered)
				{
					bd.updatesBetInProgress++;
					if(bd.updatesBetInProgress>3)
					{
						bd.setState(BetData.PLACING_ERROR);
					}
				}
			}
			
			
		}
	}
	
	public void warnListenersUpdate()
	{
		
		Hashtable<BetListener, Vector<BetData>> hashListeners=new Hashtable<BetListener, Vector<BetData>>();
				
		//betAux.setMatchedAmount(bet.getMatchedAmount());
		//betAux.setOddMached(bet.getOddMached());
		//betAux.setState(bet.getState());
		
		for(BetData bet:bets)
		{
			BetData bdAux=search(copyBets,bet.getBetID());
			
			if(bdAux==null)
			{
				addBetToHash(bet, hashListeners);
			}
			else if(bet.getMatchedAmount()!=bdAux.getMatchedAmount() ||
					bet.getOddMached()!=bdAux.getOddMached() ||
					bet.getState()!=bdAux.getState())
			{
				addBetToHash(bet, hashListeners);
			}
		}
		
		for(BetListener bl:betListeners)
		{
			bl.betChange(hashListeners.get(bl));
		}
	}
	
	public void addBetToHash(BetData bd,Hashtable<BetListener, Vector<BetData>> hashListeners)
	{
		Vector<BetData> vec=hashListeners.get(bd.getOwner());
		if(vec==null)
		{
			vec=new Vector<BetData>();
			vec.add(bd);
			hashListeners.put(bd.getOwner(), vec);

		}
		else
		{
			vec.add(bd);
		}
	}
	
	//---------------------------------thread -----
	public class BetsManagerThread extends Object implements Runnable {
		private volatile boolean stopRequested;

		private Thread runThread;

		public void run() {
			runThread = Thread.currentThread();
			stopRequested = false;
			
			while (!stopRequested) {
				try {
					updateCopyBets();
					refresh(); /// connect and get the data prices
					warnListenersUpdate();
				
					//	refreshBets();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				warnListenersUpdate(); // warn all listeners
				
				try {
					Thread.sleep(updateInterval);
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}

		public void stopRequest() {
			stopRequested = true;

			if (runThread != null) {
				runThread.interrupt();

				// suspend()stop();
			}
		}
	}
	//-----------------------------------------end thread -------------------
	
	public void startPolling() {
		if (polling)
			return;
		as = new BetsManagerThread();
		t = new Thread(as);
		t.start();

		polling = true;
		
	}

	public void stopPolling() {
		if (!polling)
			return;
		as.stopRequest();
		polling = false;

	}
	public void clean()
	{
		
	}
	
	
}
