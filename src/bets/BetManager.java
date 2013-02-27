package bets;

import generated.exchange.BFExchangeServiceStub.Bet;
import generated.exchange.BFExchangeServiceStub.BetPersistenceTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetStatusEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.CancelBets;
import generated.exchange.BFExchangeServiceStub.CancelBetsResult;
import generated.exchange.BFExchangeServiceStub.CancelBetsResultEnum;
import generated.exchange.BFExchangeServiceStub.MUBet;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResult;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResultEnum;

import java.awt.Color;
import java.util.Calendar;
import java.util.Vector;

import main.Manager;

import DataRepository.MarketData;
import DataRepository.Utils;
import demo.handler.ExchangeAPI;

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
	
	public MarketData getMd() {
		return md;
	}

	
	private void refresh()
	{
		//System.out.println("processing");
		if(!isBetsToProcess())
			return;
		
		MUBet[] betsbf=null;
		try {
			betsbf = ExchangeAPI.getMUBets(getMd().getSelectedExchange(), getMd().getApiContext(), getMd().getSelectedMarket().getMarketId());
		} catch (Exception e) {
			e.printStackTrace();
			writeError(e.getMessage());
			return;
		}
		
		if(betsbf==null)
			return;
		
		// processar normal 
		processBetsNormal(betsbf);
		
		
		// processar bets in progress
		if(isBetsInProgress())
		{
			processBetsInProgress(betsbf);
		}
		
	}
		
	private boolean isBetsToProcess()
	{
		boolean ret=false;
		for(BetData b:bets)
		{
			if(b.getState()==BetData.UNMATHED || 
					b.getState()==BetData.PARTIAL_MACHED || 
					b.getState()==BetData.BET_IN_PROGRESS)
				ret=true;
		}
		return ret;
	}
	
	// --------------- bets normal --------------------
	private void processBetsNormal(MUBet[] betsbf)
	{
		for(BetData bd:bets)
		{
			if(bd.getState()==BetData.PARTIAL_MACHED || bd.getState()==BetData.UNMATHED)
			{
				Vector<MUBet> matched=new Vector<MUBet>();
				Vector<MUBet> unmatched=new Vector<MUBet>();
				
				for(MUBet mubet:betsbf)
				{
					if(mubet.getBetId()==bd.getBetID())
					{
						if(mubet.getBetStatus()==BetStatusEnum.U)
						{
							unmatched.add(mubet);
						}
						else
						{
							matched.add(mubet);
						}
					}
				}
				
				if(matched.size()==0 && unmatched.size()==0) //external cancel (without any match)
				{
					bd.setState(BetData.CANCELED,BetData.SYSTEM);
				}
				else if(matched.size()==0) // unmached
				{
					// does not do nothing 
					bd.setState(BetData.UNMATHED,BetData.SYSTEM);
				}
				else if(unmatched.size()==0) // matched or external partial canceled (parcial matched but impossible to match the rest)
				{
					double msizes[]=new double[matched.size()];
					double modds[]=new double[matched.size()];
					
					double oddAvg=0.;
					double totalSize=0.;
					for(int i=0;i<matched.size();i++)
					{
						msizes[i]=matched.get(i).getSize();
						
						totalSize+=matched.get(i).getSize();
						
						modds[i]=matched.get(i).getPrice();
						
					}
					
					oddAvg=Utils.calculateOddAverage(modds,msizes);
					
					totalSize=Utils.convertAmountToBF(totalSize);
					
					if(totalSize < bd.amount) //caneled external
					{
						bd.setMatchedAmount(totalSize);
						bd.setOddMached(oddAvg);
						bd.setState(BetData.PARCIAL_CANCELED,BetData.SYSTEM);
					}
					else //matched
					{
						bd.setMatchedAmount(totalSize);
						bd.setOddMached(oddAvg);
						bd.setState(BetData.MATCHED,BetData.SYSTEM);
					}
					
				}
				else // partial matched
				{
					double msizes[]=new double[matched.size()];
					double modds[]=new double[matched.size()];
					
					double oddAvg=0.;
					double totalSize=0.;
					for(int i=0;i<matched.size();i++)
					{
						msizes[i]=matched.get(i).getSize();
						
						totalSize+=matched.get(i).getSize();
						
						modds[i]=matched.get(i).getPrice();
						
					}
					
					oddAvg=Utils.calculateOddAverage(modds,msizes);
					
					totalSize=Utils.convertAmountToBF(totalSize);
					
					bd.setMatchedAmount(totalSize);
					bd.setOddMached(oddAvg);
					bd.setState(BetData.PARTIAL_MACHED,BetData.SYSTEM);
				}
			}
		}
	}
	
	
	// --------------- bets normal end -----------------
	
	// --------------- bets in progress --------------------
	
	private void processBetsInProgress(MUBet[] betsbf)
	{
		Vector<BetData> possibleBetsInProgress=new Vector<BetData>();
		
		for(MUBet mubet:betsbf)
		{
			BetData bdAux=getBetById(mubet.getBetId());
			if(bdAux==null)
			{
				// If this bet was not processed yet in this refresh
				if(getBetById(mubet.getBetId(), possibleBetsInProgress)==null)
				{
					if(isPossibleBetInProgress(mubet))
					{
						BetData bd=getBetFromAPI(mubet.getBetId());
						if(bd!=null)
							possibleBetsInProgress.add(bd);
					}
				}
			}
		}
		
		assignBetsInProgress(possibleBetsInProgress);
		
		updateBetsInProgress();
	}
	
	private boolean isBetsInProgress()
	{
		boolean ret=false;
		for(BetData b:bets)
		{
			if(b.getState()==BetData.BET_IN_PROGRESS)
				ret=true;
		}
		return ret;
	}
	
	private boolean isPossibleBetInProgress(MUBet mubet)
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
	
	//Change bets
	private void assignBetsInProgress(Vector<BetData> externalPossibleBets)
	{
		for(BetData bd:bets)
		{
			for(BetData bdPossible:externalPossibleBets)
			{
				if(bd.getRd().getId()==bdPossible.getRd().getId()
						&& bd.getOddRequested()==bdPossible.getOddRequested()
						&& bd.getAmount()==bdPossible.getAmount()
						&& bd.getType()==bdPossible.getType())
				{
					bd.setBetID(bdPossible.getBetID());
					
					bd.setState(bdPossible.getState(),BetData.SYSTEM);
					bd.setMatchedAmount(bdPossible.getMatchedAmount());
					bd.setOddMached(bdPossible.getOddMached());
					
				}
			}
		}
	}
	
	
	
	private void updateBetsInProgress()
	{
		for(BetData bd:bets)
		{
			if(bd.getState()==BetData.BET_IN_PROGRESS)
				bd.updatesBetInProgress++;
			
			if(bd.updatesBetInProgress>BIP_ERROR_UPDATES)
			{
				bd.setState(BetData.PLACING_ERROR,BetData.SYSTEM);
				bd.setErrorType(BetData.ERROR_BET_IN_PROGRESS);
			}
		}
	}
	
	// --------------- bets in progress end --------------------
	
	
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
		
		return BetUtils.createBetData(gb,getMd());
	
	}
	
	public int placeBet(BetData bet)
	{
		Vector<BetData> place=new Vector<BetData>();
		place.add(bet);
		return placeBets(place);
	}
	
	public int placeBets(Vector<BetData> place)
	{
		int ret=0;
		
		PlaceBets[] betsAPI=new PlaceBets[place.size()];
		
		BetData[] bds=place.toArray(new BetData[]{});
		
		// initiate
		for(int i=0;i<bds.length;i++)
		{
			betsAPI[i]=BetUtils.createPlaceBet(bds[i]);
			
			
			
			if(bds[i].getType()==BetData.BACK)
				bds[i].setEntryAmount(Utils.getAmountLayOddFrame(bds[i].getRd(), bds[i].getOddRequested(), 0));
			else
				bds[i].setEntryAmount(Utils.getAmountBackOddFrame(bds[i].getRd(), bds[i].getOddRequested(), 0));
			
			bds[i].setEntryVolume(Utils.getVolumeFrame(bds[i].getRd(), 0, bds[i].getOddRequested()));
			
			bets.add(bds[i]);
			bds[i].setTimestampPlace(Calendar.getInstance());
			
		}
		
		PlaceBetsResult[] betResult=null;
		
		try {
			betResult=ExchangeAPI.placeBets(getMd().getSelectedExchange(),  Manager.apiContext, betsAPI);
		} catch (Exception e) {
			e.printStackTrace();
			
			if(e.getMessage().contains(new String("EVENT_SUSPENDED")))
			{
				for(int i=0;i<bds.length;i++)
				{
					bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds[i].setErrorType(BetData.ERROR_MARKET_SUSPENDED);
				}
			}
			else if(e.getMessage().contains(new String("EVENT_CLOSED")))
			{
				for(int i=0;i<bds.length;i++)
				{
					bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds[i].setErrorType(BetData.ERROR_MARKET_CLOSED);
				}
				return -1;
			}
			else if(e.getMessage().contains(new String("BET_IN_PROGRESS")))
			{
				for(int i=0;i<bds.length;i++)
				{
					bds[i].setState(BetData.BET_IN_PROGRESS,BetData.PLACE);
				}
				return 0;
			}
			else
			{
				for(int i=0;i<bds.length;i++)
				{
					bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds[i].setErrorType(BetData.ERROR_UNKNOWN);
				}
				return -1;
			}
			
		}
		
		if(betResult==null)
		{
			for(int i=0;i<bds.length;i++)
			{
				bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
				bds[i].setErrorType(BetData.ERROR_UNKNOWN);
			}
			return -1;
		}
		
		for(int i=0;i<betResult.length;i++)
		{
			if(betResult[i].getSuccess()==true)
			{
				bds[i].setBetID(betResult[i].getBetId());
				
				bds[i].setMatchedAmount(betResult[i].getSizeMatched());
				bds[i].setOddMached(betResult[i].getAveragePriceMatched());
			
				if( betResult[i].getSizeMatched()>0)
				{
					
					if(Utils.convertAmountToBF(bds[i].getAmount())<=bds[i].getMatchedAmount())
						bds[i].setState(BetData.MATCHED,BetData.PLACE);
					else
						bds[i].setState(BetData.PARTIAL_MACHED,BetData.PLACE);
				}
				else
				{
					bds[i].setState(BetData.UNMATHED,BetData.PLACE);
				}
				
			
			}
			else
			{
				if(betResult[i].getResultCode()==PlaceBetsResultEnum.BET_IN_PROGRESS)
				{
					bds[i].setState(BetData.BET_IN_PROGRESS,BetData.PLACE);
					//betsInProgress.add(bds[i]);
					
				}
				else if(betResult[i].getResultCode()==PlaceBetsResultEnum.EVENT_CLOSED)
				{
					bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds[i].setErrorType(BetData.ERROR_MARKET_CLOSED);
					ret=-2;
				}
				else if(betResult[i].getResultCode()==PlaceBetsResultEnum.EXPOSURE_OR_AVAILABLE_BALANCE_EXCEEDED)
				{
					bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds[i].setErrorType(BetData.ERROR_BALANCE_EXCEEDED);
					ret=-2;
				}
				else 
				{
					bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds[i].setErrorType(BetData.ERROR_UNKNOWN);
					ret=-2;
				}
			}
		}
		
		return ret;
	}

	public int cancelBets(Vector<BetData> cancelBets)
	{
		CancelBets canc[] = new CancelBets[cancelBets.size()];
		
		BetData[] bds=cancelBets.toArray(new BetData[]{});
		
		for(int i=0;i<bds.length;i++)
		{
			canc[i]= new CancelBets();
			canc[i].setBetId(bds[i].getBetID());
			
			bds[i].setTimestampPlace(Calendar.getInstance());
		}
		
		CancelBetsResult betResult[]=null;
		
		try {
			betResult = ExchangeAPI.cancelBets(getMd().getSelectedExchange(),getMd().getApiContext(), canc);
		} catch (Exception e) {
			if(e.getMessage().contains(new String("BET_NOT_CANCELLED")))  
			{
				
			}
		}
		
		return 0;
	}
	
	

	
	private void writeError(String s)
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
					
					refresh(); /// connect and get the data 
					
				
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
		bets.clear();
		bets=null;
		md=null;
	}
	
	public static void main(String[] args)  throws Exception {
		Vector<BetData> possibleBetsInProgress=new Vector<BetData>();
		// possibleBetsInProgress.add(null);
		// possibleBetsInProgress.add(null);
		
 		System.out.println( possibleBetsInProgress.size());
 		
 		BetData bd=new BetData( null, 100, 4.5, BetData.LAY,false);
 		System.out.println(BetUtils.printBet(bd));
	}
}
