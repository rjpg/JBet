package bets;

import generated.exchange.BFExchangeServiceStub.Bet;
import generated.exchange.BFExchangeServiceStub.BetStatusEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.GetMarket;
import generated.exchange.BFExchangeServiceStub.MUBet;

import java.awt.Color;
import java.util.Vector;

import main.Manager;
import demo.handler.ExchangeAPI;

import DataRepository.MarketData;
import bets.BetsManager.BetsManagerThread;

public class BetManager {
	
	// THREAD
	private BetsManagerThread as;
	private Thread t;
	private boolean polling = false;
	protected int updateInterval = 400;
	
	//Bets data
	public Vector<BetData> bets=new Vector<BetData>();
	
	//Market
	public MarketData md;
	
	//Bet in progress Max frames until error state
	protected int BIP_ERROR_UPDATES = 10;
	
	public BetManager(MarketData mdA) {
		
		md=mdA;
		
	}
	
	public void refresh()
	{
		if(!isBetsToProcess())
			return;
		
		boolean betsInProgress=isBetsInProgress();
		
		MUBet[] betsbf=null;
		try {
			betsbf = ExchangeAPI.getMUBets(md.getSelectedExchange(), md.getApiContext(), md.getSelectedMarket().getMarketId());
		} catch (Exception e) {
			e.printStackTrace();
			writeError(e.getMessage());
		}
		
		if(betsbf==null)
			return;
		
		// processar normal 
		
		
		
		// processar bets in progress
		if(isBetsInProgress())
		{
			Vector<BetData> possibleBetsInProgress=new Vector<BetData>();
			
			for(MUBet mubet:betsbf)
			{
				BetData bdAux=getBetById(mubet.getBetId());
				if(bdAux==null)
				{
					// If there are bets in progress and not pressed yet in this refresh
					if( betsInProgress && getBetById(mubet.getBetId(), possibleBetsInProgress)==null)
					{
						BetData bd=getBetFromAPI(mubet.getBetId());
						if(bd!=null)
							possibleBetsInProgress.add(getBetFromAPI(mubet.getBetId()));
					}
				}
			}
			
			assignBetsInProgress(possibleBetsInProgress);
			
			updateBetsInProgress();
		}
		
	}
	
	public boolean isBetsToProcess()
	{
		boolean ret=false;
		for(BetData b:bets)
		{
			if(b.getState()==BetData.UNMATHED || 
					b.getState()==BetData.PARCIAL_MACHED || 
					b.getState()==BetData.BET_IN_PROGRESS)
				ret=true;
		}
		return ret;
	}
	
	public boolean isBetsInProgress()
	{
		boolean ret=false;
		for(BetData b:bets)
		{
			if(b.getState()==BetData.BET_IN_PROGRESS)
				ret=true;
		}
		return ret;
	}
	
	public boolean isPossibleBetInProgress(MUBet mubet)
	{
		boolean ret=false;
		
		for(BetData b:bets)
		{
			
			if(mubet.getSelectionId()==b.getRd().getId())
			{
				if(mubet.getBetType()==BetTypeEnum.B && b.getType()==BetData.BACK  )
					ret=true;
				if(mubet.getBetType()==BetTypeEnum.L && b.getType()==BetData.LAY  )
					ret=true;
			}
		}
		
		return ret;
	}
	
	private void assignBetsInProgress(Vector<BetData> externalPossibleBets)
	{
		for(BetData bd:bets)
		{
			
		}
	}
	
	private void updateBetsInProgress()
	{
		for(BetData bd:bets)
		{
			if(bd.getState()==BetData.BET_IN_PROGRESS)
				bd.updatesBetInProgress++;
			
			if(bd.updatesBetInProgress>BIP_ERROR_UPDATES)
				bd.setState(BetData.PLACING_ERROR);
		}
	}
	
	private BetData getBetById(long ID,Vector<BetData> betsVector)
	{
		for(BetData bd:betsVector)
			if(bd.getBetID()==ID)
				return bd;
		return null;
	}
	
	private BetData getBetById(long ID)
	{
		for(BetData bd:bets)
			if(bd.getBetID()==ID)
				return bd;
		return null;
	}
	
	private BetData getBetFromAPI(long id)
	{
		Bet gb=null;
		try {
			gb =ExchangeAPI.getBet(md.getSelectedExchange(), md.getApiContext(),id);
		} catch (Exception e) {
			e.printStackTrace();
			writeError(e.getMessage());
		}
			
		if(gb==null)
		{
			writeError("Failed to get Bet: ExchangeAPI.getBet return null ");
			return null;
		}
		
		return createBetData(gb);
	}
	
	public BetData createBetData(Bet bet)
	{
		
		if(bet==null)
			return null;
		
		BetData ret=null;
		if(bet.getBetType()==BetTypeEnum.B)
			ret=new BetData(null,md.getRunnersById(bet.getSelectionId()),bet.getRequestedSize(),bet.getPrice(),BetData.BACK,null);
		else // Is B or L
			ret=new BetData(null,md.getRunnersById(bet.getSelectionId()),bet.getRequestedSize(),bet.getPrice(),BetData.LAY,null);
		
		ret.setBetID(bet.getBetId());
		ret.setMatchedAmount(bet.getMatchedSize());
		ret.setOddMached(bet.getAvgPrice());
		
		if(bet.getBetStatus()==BetStatusEnum.U)
			ret.setState(BetData.UNMATHED);
		
		if(bet.getBetStatus()==BetStatusEnum.M)
			ret.setState(BetData.MATHED);
		
		if(bet.getBetStatus()==BetStatusEnum.MU)
			ret.setState(BetData.PARCIAL_MACHED);
		
		if(bet.getBetStatus()==BetStatusEnum.C)
			ret.setState(BetData.CANCELED);
		
		if(bet.getBetStatus()==BetStatusEnum.V) //Voided (?)
			ret.setState(BetData.CANCELED);
		//testar os voideds quando o mercado fica suspenso para ver se fica estado Cancelado "C" ou voided "V"
		
		return ret;
	}
	
	public void writeError(String s)
	{
		System.out.println("Error : "+s);
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
					
					refresh(); /// connect and get the data prices
					
				
					//	refreshBets();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				
				
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
	
	public static void main(String[] args)  throws Exception {
		Vector<BetData> possibleBetsInProgress=new Vector<BetData>();
		 possibleBetsInProgress.add(null);
		 possibleBetsInProgress.add(null);
		 System.out.println( possibleBetsInProgress.size());
	}
}
