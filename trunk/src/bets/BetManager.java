package bets;

import generated.exchange.BFExchangeServiceStub.Bet;
import generated.exchange.BFExchangeServiceStub.BetStatusEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.MUBet;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResult;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResultEnum;

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
	
	private void refresh()
	{
		if(!isBetsToProcess())
			return;
		
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
				
				if(matched.size()==0 && unmatched.size()==0) //external cancel
				{
					bd.setState(BetData.CANCELED);
				}
				else if(matched.size()==0) // unmached
				{
					// does not do nothing 
					bd.setState(BetData.UNMATHED);
				}
				else if(unmatched.size()==0) // matched or external partial canceled 
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
					
					if(totalSize < bd.amount) //caneled 
					{
						bd.setMatchedAmount(totalSize);
						bd.setOddMached(oddAvg);
						bd.setState(BetData.CANCELED);
					}
					else //matched
					{
						bd.setMatchedAmount(totalSize);
						bd.setOddMached(oddAvg);
						bd.setState(BetData.MATHED);
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
					bd.setState(BetData.PARTIAL_MACHED);
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
					
					bd.setState(bdPossible.getState());
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
				bd.setState(BetData.PLACING_ERROR);
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
		
		return createBetData(gb);
	
	}
	
	public int placeBets(Vector<BetData> place)
	{
		PlaceBets[] betsAPI=new PlaceBets[place.size()];
		
		BetData[] bds=place.toArray(new BetData[]{});
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
			return -1;
		}
		
		
			
		return 0;
	}

	public MarketData getMd() {
		return md;
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
				
				if(betResult[i].getSuccess()==true)
				{
					bds[i].setBetID(betResult[i].getBetId());
				
					bds[i].setMatchedAmount(betResult[i].getSizeMatched());
					bds[i].setOddMached(betResult[i].getAveragePriceMatched());
				
					if( betResult[i].getSizeMatched()>0)
					{
						
						if(Utils.convertAmountToBF(bds[i].getAmount())==bds[i].getMatchedAmount())
							bds[i].setState(BetData.MATHED);
						else
							bds[i].setState(BetData.PARTIAL_MACHED);
					}
					else
					{
						bds[i].setState(BetData.UNMATHED);
					}
					bets.add(bds[i]);
				}
				else
				{
					bds[i].setState(BetData.PLACING_ERROR);
					bets.add(bds[i]);
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

	
	/// passar esta função para utils 
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
			ret.setState(BetData.PARTIAL_MACHED);
		
		if(bet.getBetStatus()==BetStatusEnum.C)
			ret.setState(BetData.CANCELED);
		
		if(bet.getBetStatus()==BetStatusEnum.V) //Voided (?)
			ret.setState(BetData.CANCELED);
		//testar os voideds quando o mercado fica suspenso para ver se fica estado Cancelado "C" ou voided "V"
		
		return ret;
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
		
	}
	
	public static void main(String[] args)  throws Exception {
		Vector<BetData> possibleBetsInProgress=new Vector<BetData>();
		// possibleBetsInProgress.add(null);
		// possibleBetsInProgress.add(null);
		 System.out.println( possibleBetsInProgress.size());
	}
}
