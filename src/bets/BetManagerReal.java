package bets;

import generated.exchange.BFExchangeServiceStub.Bet;
import generated.exchange.BFExchangeServiceStub.BetPersistenceTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetStatusEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.CancelBets;
import generated.exchange.BFExchangeServiceStub.CancelBetsResult;
import generated.exchange.BFExchangeServiceStub.MUBet;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResult;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResultEnum;
import generated.exchange.BFExchangeServiceStub.UpdateBets;
import generated.exchange.BFExchangeServiceStub.UpdateBetsResult;
import generated.exchange.BFExchangeServiceStub.UpdateBetsResultEnum;

import java.util.Calendar;
import java.util.Vector;

import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import demo.handler.ExchangeAPI;

public class BetManagerReal extends BetManager {
	
	// THREAD
	private BetsManagerThread as;
	private Thread t;
	protected int updateInterval = 400;
	private boolean polling = false;
	
	//Bets data
	private Vector<BetData> bets=new Vector<BetData>();
	
	//Bet in progress Max frames until error state
	protected int BIP_ERROR_UPDATES = 10;
	
	public BetManagerReal(MarketData mdA) {
		super(mdA);
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
			if(b.getState()==BetData.UNMATCHED || 
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
			if(bd.getState()==BetData.PARTIAL_MACHED || bd.getState()==BetData.UNMATCHED)
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
					bd.setState(BetData.UNMATCHED,BetData.SYSTEM);
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
						bd.setState(BetData.PARTIAL_CANCELED,BetData.SYSTEM);
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
			if(bd.getState()==BetData.BET_IN_PROGRESS)
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
	
	public BetData getBetById(long ID)
	{
		for(BetData bd:bets)
		{
			if(bd.getBetID()!=null && bd.getBetID()==ID)
				return bd;
		}
		return null;
	}
	
	private BetData getBetFromAPI(long id)
	{
		Bet gb=null;
		try {
			gb =ExchangeAPI.getBet(getMd().getSelectedExchange(), getMd().getApiContext(),id);
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
	
	private void fillBetFromAPI(BetData bd)
	{
		if(bd.getBetID()==null) return;
		
		BetData bdAux=getBetFromAPI(bd.getBetID());
		
		bd.setState(bdAux.getState(), bdAux.getTransition());
		bd.setAmount(bdAux.getAmount());
		bd.setOddMached(bdAux.getOddMached());
		bd.setOddRequested(bdAux.getOddRequested());
		bd.setMatchedAmount(bdAux.getMatchedAmount());
		
	}
	
	public int placeBet(BetData bet)
	{
		Vector<BetData> place=new Vector<BetData>();
		place.add(bet);
		return placeBets(place);
	}
	/**
	 * 
	 * @param place - vector of BetData
	 * @return 
	 * 0 All placed Ok
	 * -1 Nothing placed
	 * -2 At least some not places 
	 */
	public int placeBets(Vector<BetData> place)
	{
	
		
		int ret=0; // For bets over 2.00 All placed - 0, Only some Placed -2, all Not placed -1;
		
		Vector<BetData> bdsLow=new Vector<BetData>();
		Vector<BetData> placeAux=new Vector<BetData>();
		
		for(BetData bd:place)
		{
			if(bd.getAmount()<2.00)
				bdsLow.add(bd);
			else
				placeAux.add(bd);
		}
		
		
		if(placeAux.size()>0) // if there are bets over 2.00 (normal)
		{
			PlaceBets[] betsAPI=new PlaceBets[placeAux.size()];
			
			BetData[] bds=placeAux.toArray(new BetData[]{});
				
			
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
			
			// to test bet in progress
			//for(int i=0;i<bds.length;i++)
			//{
			//	bds[i].setState(BetData.BET_IN_PROGRESS,BetData.PLACE);
			//}
			//return 0;
			//
			
			
			PlaceBetsResult[] betResult=null;
			
			try {
				betResult=ExchangeAPI.placeBets(getMd().getSelectedExchange(),  getMd().getApiContext(), betsAPI);
			} catch (Exception e) {
				e.printStackTrace();
				
				if(e.getMessage().contains(new String("EVENT_SUSPENDED")))
				{
					for(int i=0;i<bds.length;i++)
					{
						bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
						bds[i].setErrorType(BetData.ERROR_MARKET_SUSPENDED);
					}
					return -1;
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
			
			
			boolean somePlaced=false;
			boolean someNotPlaced=false;
			
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
						bds[i].setState(BetData.UNMATCHED,BetData.PLACE);
					}
					somePlaced=true;
				
				}
				else
				{
					if(betResult[i].getResultCode()==PlaceBetsResultEnum.BET_IN_PROGRESS)
					{
						bds[i].setState(BetData.BET_IN_PROGRESS,BetData.PLACE);
						somePlaced=true;			
						
					}
					else if(betResult[i].getResultCode()==PlaceBetsResultEnum.EVENT_CLOSED)
					{
						bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
						bds[i].setErrorType(BetData.ERROR_MARKET_CLOSED);
						someNotPlaced=true;
					}
					else if(betResult[i].getResultCode()==PlaceBetsResultEnum.EXPOSURE_OR_AVAILABLE_BALANCE_EXCEEDED)
					{
						bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
						bds[i].setErrorType(BetData.ERROR_BALANCE_EXCEEDED);
						someNotPlaced=true;
					}
					else 
					{
						bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
						bds[i].setErrorType(BetData.ERROR_UNKNOWN);
						someNotPlaced=true;
					}
				}
			}
			
			
			if(someNotPlaced==true && somePlaced==true)
				ret=-2;
			else if(somePlaced==false)
				return -1; // ret= -1 // exit - dont try to place Low bets (if exist)
			else 
				ret=0;
		}
		////////////////////////// END IF
		
		
		
		
		int retLow=0;
		if(bdsLow.size()>0) //if there are bets under 2.00 
			retLow=placeLowBets(bdsLow);
		
		if(placeAux.size()==0)
			return retLow;
		
		if(ret!=0) // some placed -1
			return -2;
		else
			if(retLow!=0)   // low error or some placed 
				return -2;
			else
				return 0;   // all OK
		
		
		//*/
	}
	
	/**
	 * This method does not take into account bet_in_progress sate. 
	 * If there are Bet_in_progress response from API it will assume PLACING_ERROR
	 *
	 * @param bdsLow - vector of BetData under 2.00€
	 * @return 0 OK -1 All not placed -2 Some placed
	 */
	private int placeLowBets(Vector<BetData> bdsLow) 
	{
		
		int placeLow2euroBets=placeLow2euroBets(bdsLow);
				
		if(placeLow2euroBets==-1)
			return -1;
		
		Vector<BetData> bdsLow2euro=bdsLow;
		
		if(placeLow2euroBets==-2)
		{
			bdsLow2euro=new Vector<BetData>();
			for(BetData bd:bdsLow)
			{
				if(bd.getState()==BetData.NOT_PLACED)
					bdsLow2euro.add(bd);
			}
		}
		
		int updateAmount=updateAmount(bdsLow2euro);
		
		if(updateAmount==-1)
			return -1;
		
		Vector<BetData> bdsLowUpdateAmount=bdsLow2euro;
		
		if(updateAmount==-2)
		{
			bdsLowUpdateAmount=new Vector<BetData>();
			for(BetData bd:bdsLow2euro)
			{
				if(bd.getState()==BetData.NOT_PLACED)
					bdsLowUpdateAmount.add(bd);
			}
		}
		
		
		int updateOdd=updateOdds(bdsLowUpdateAmount);
		
		return updateOdd;
				
	}
	
	private int placeLow2euroBets(Vector<BetData> bdsLow)
	{
		PlaceBets[] betsAPI=new PlaceBets[bdsLow.size()];
		
		BetData[] bds=bdsLow.toArray(new BetData[]{});
			
		
		// initiate
		for(int i=0;i<bds.length;i++)
		{
			betsAPI[i]=BetUtils.createPlaceBet(bds[i]);
			betsAPI[i].setSize(2.00);
			
			if(bds[i].getType()==BetData.BACK)
			{
				bds[i].setEntryAmount(Utils.getAmountLayOddFrame(bds[i].getRd(), bds[i].getOddRequested(), 0));
				betsAPI[i].setPrice(1000);
			}
			else
			{
				bds[i].setEntryAmount(Utils.getAmountBackOddFrame(bds[i].getRd(), bds[i].getOddRequested(), 0));
				betsAPI[i].setPrice(1.01);
			}
			
			bds[i].setEntryVolume(Utils.getVolumeFrame(bds[i].getRd(), 0, bds[i].getOddRequested()));
			
			bets.add(bds[i]);
			bds[i].setTimestampPlace(Calendar.getInstance());
			
		}
		
		PlaceBetsResult[] betResult=null;
		
		try {
			betResult=ExchangeAPI.placeBets(getMd().getSelectedExchange(),  getMd().getApiContext(), betsAPI);
		} catch (Exception e) {
			e.printStackTrace();
			
			if(e.getMessage().contains(new String("EVENT_SUSPENDED")))
			{
				for(int i=0;i<bds.length;i++)
				{
					bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds[i].setErrorType(BetData.ERROR_MARKET_SUSPENDED);
				}
				return -1;
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
		
		boolean somePlaced=false;
		boolean someNotPlaced=false;
		
		
		
		for(int i=0;i<betResult.length;i++)
		{
			if(betResult[i].getSuccess()==true)
			{
				bds[i].setBetID(betResult[i].getBetId());
				somePlaced=true;
			}
			else
			{
				if(betResult[i].getResultCode()==PlaceBetsResultEnum.EVENT_CLOSED)
				{
					bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds[i].setErrorType(BetData.ERROR_MARKET_CLOSED);
					someNotPlaced=true;
				}
				else if(betResult[i].getResultCode()==PlaceBetsResultEnum.EXPOSURE_OR_AVAILABLE_BALANCE_EXCEEDED)
				{
					bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds[i].setErrorType(BetData.ERROR_BALANCE_EXCEEDED);
					someNotPlaced=true;
				}
				else 
				{
					bds[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds[i].setErrorType(BetData.ERROR_UNKNOWN);
					someNotPlaced=true;
				}
			}
			
		}
		
		if(someNotPlaced==true && somePlaced==true)
			return -2;
		else if(somePlaced==false)
			return -1;
		else
			return 0;
			
	}
	
	private int updateAmount(Vector<BetData> bdsLow2euro)
	{
		
		UpdateBets[] betsUpdAPI1=new UpdateBets[bdsLow2euro.size()];
		
		BetData[] bds1=bdsLow2euro.toArray(new BetData[]{});
		
		long betsIDs[]=new long[bdsLow2euro.size()];
		
		for(int i=0;i<bds1.length;i++)
		{
			betsIDs[i]=bds1[i].getBetID();
			//System.out.println("BetID (size):"+bds1[i].getBetID());
			betsUpdAPI1[i]=new UpdateBets();
			if(bds1[i].isKeepInPlay())
			{
				betsUpdAPI1[i].setOldBetPersistenceType(BetPersistenceTypeEnum.IP);
				betsUpdAPI1[i].setNewBetPersistenceType(BetPersistenceTypeEnum.IP);
			}
			else
			{
				betsUpdAPI1[i].setOldBetPersistenceType(BetPersistenceTypeEnum.NONE);
				betsUpdAPI1[i].setNewBetPersistenceType(BetPersistenceTypeEnum.NONE);
			}
			
			if(bds1[i].getType()==BetData.BACK)
			{
				
				betsUpdAPI1[i].setOldPrice(1000);
				betsUpdAPI1[i].setNewPrice(1000);
			}
			else
			{
				
				betsUpdAPI1[i].setOldPrice(1.01);
				betsUpdAPI1[i].setNewPrice(1.01);
			}
			
			//System.out.println("ID: "+ bds1[i].getBetID());
			betsUpdAPI1[i].setBetId(bds1[i].getBetID());
			
			betsUpdAPI1[i].setOldSize(2.0);
			betsUpdAPI1[i].setNewSize(Utils.convertAmountToBF(2.0+bds1[i].getAmount()));
		}
		
		UpdateBetsResult[] betUpdateResult1=null;
		
		
		try {
			betUpdateResult1 = ExchangeAPI.updateBets(getMd().getSelectedExchange(),  getMd().getApiContext(), betsUpdAPI1);
		} catch (Exception e) {
			e.printStackTrace();
			
			if(e.getMessage().contains(new String("EVENT_SUSPENDED")))
			{
				for(int i=0;i<bds1.length;i++)
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_MARKET_SUSPENDED);
				}
				
				cancelBetsID(betsIDs);
				return -1;
			}
			else if(e.getMessage().contains(new String("EVENT_CLOSED")))
			{
				for(int i=0;i<bds1.length;i++)
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_MARKET_CLOSED);
				}
				cancelBetsID(betsIDs);
				return -1;
			}
			else
			{
				for(int i=0;i<bds1.length;i++)
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_UNKNOWN);
				}
				cancelBetsID(betsIDs);
				return -1;
			}
			
		}
		
		if(betUpdateResult1==null)
		{
			
			for(int i=0;i<bds1.length;i++)
			{
				bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
				bds1[i].setErrorType(BetData.ERROR_UNKNOWN);
			}
			
			cancelBetsID(betsIDs);
			
			return -1;
		}
		
		boolean someUpdated=false;
		boolean someNotUpdated=false;
		
		for(int i=0;i<betUpdateResult1.length;i++)
		{
			if(betUpdateResult1[i].getSuccess()==true)
			{
				cancelBetsID(new long[]{bds1[i].getBetID()});
				bds1[i].setBetID(betUpdateResult1[i].getNewBetId());
				someUpdated=true;
				
			}
			else
			{
				System.out.println("error:"+betUpdateResult1[i].getResultCode());
				if(betUpdateResult1[i].getResultCode()==UpdateBetsResultEnum.EVENT_CLOSED_CANNOT_MODIFY_BET)
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_MARKET_CLOSED);
					someNotUpdated=true;
					cancelBetsID(new long[]{bds1[i].getBetID()});
				}
				else if(betUpdateResult1[i].getResultCode()==UpdateBetsResultEnum.EXCEEDED_EXPOSURE_OR_AVAILABLE_TO_BET_BALANCE)
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_BALANCE_EXCEEDED);
					someNotUpdated=true;
					
					cancelBetsID(new long[]{bds1[i].getBetID()});
				}
				else 
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_UNKNOWN);
					someNotUpdated=true;
					
					cancelBetsID(new long[]{bds1[i].getBetID()});
				}
				
				
			}
		}
		
		if(someNotUpdated==true && someUpdated==true)
			return -2;
		else if(someUpdated==false)
			return -1;
		else
			return 0;
		
	}
	
	
	
	private int updateOdds(Vector<BetData> bdsLow2euroA)
	{
		
		Vector<BetData> bdsLow2euro=new Vector<BetData>();
		
		for(BetData bd:bdsLow2euroA)
		{
			if(bd.getType()==BetData.BACK)
			{
				
				
				if(bd.getOddRequested()!=1000)
				{
					bdsLow2euro.add(bd);
				}
				else
				{
					fillBetFromAPI(bd);
					bd.setTransition(BetData.PLACE);
					
				}
			}
			else
			{
				if(bd.getOddRequested()!=1.01)
				{
					bdsLow2euro.add(bd);
				}
				else
				{
					fillBetFromAPI(bd);
					bd.setTransition(BetData.PLACE);
				}
			}
		}
		
		if(bdsLow2euro.size()==0) return 0;
		
		
		UpdateBets[] betsUpdAPI1=new UpdateBets[bdsLow2euro.size()];
		
		BetData[] bds1=bdsLow2euro.toArray(new BetData[]{});
		
		long betsIDs[]=new long[bdsLow2euro.size()];
		
		for(int i=0;i<bds1.length;i++)
		{
			betsIDs[i]=bds1[i].getBetID();
			//System.out.println("BetID (odd):"+bds1[i].getBetID());
			
			betsUpdAPI1[i]=new UpdateBets();
			
			if(bds1[i].isKeepInPlay())
			{
				betsUpdAPI1[i].setOldBetPersistenceType(BetPersistenceTypeEnum.IP);
				betsUpdAPI1[i].setNewBetPersistenceType(BetPersistenceTypeEnum.IP);
			}
			else
			{
				betsUpdAPI1[i].setOldBetPersistenceType(BetPersistenceTypeEnum.NONE);
				betsUpdAPI1[i].setNewBetPersistenceType(BetPersistenceTypeEnum.NONE);
			}
			
			if(bds1[i].getType()==BetData.BACK)
			{
				
				betsUpdAPI1[i].setOldPrice(1000);
				betsUpdAPI1[i].setNewPrice(bds1[i].getOddRequested());
			}
			else
			{
				
				betsUpdAPI1[i].setOldPrice(1.01);
				betsUpdAPI1[i].setNewPrice(bds1[i].getOddRequested());
				
				
			}
			
			//System.out.println("New price :"+bds1[i].getOddRequested());
			//System.out.println("ID: "+ bds1[i].getBetID());
			betsUpdAPI1[i].setBetId(bds1[i].getBetID());
			
			betsUpdAPI1[i].setOldSize(bds1[i].getAmount());
			betsUpdAPI1[i].setNewSize(bds1[i].getAmount());
			
			//System.out.println("bet:"+BetUtils.printBet(bds1[i]));
		}
		
		UpdateBetsResult[] betUpdateResult1=null;
		
		
		try {
			betUpdateResult1 = ExchangeAPI.updateBets(getMd().getSelectedExchange(),  getMd().getApiContext(), betsUpdAPI1);
		} catch (Exception e) {
			e.printStackTrace();
			
			if(e.getMessage().contains(new String("EVENT_SUSPENDED")))
			{
				for(int i=0;i<bds1.length;i++)
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_MARKET_SUSPENDED);
				}
				
				cancelBetsID(betsIDs);
				return -1;
			}
			else if(e.getMessage().contains(new String("EVENT_CLOSED")))
			{
				for(int i=0;i<bds1.length;i++)
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_MARKET_CLOSED);
				}
				cancelBetsID(betsIDs);
				return -1;
			}
			else
			{
				for(int i=0;i<bds1.length;i++)
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_UNKNOWN);
				}
				cancelBetsID(betsIDs);
				return -1;
			}
			
		}
		
		if(betUpdateResult1==null)
		{
			
			for(int i=0;i<bds1.length;i++)
			{
				bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
				bds1[i].setErrorType(BetData.ERROR_UNKNOWN);
			}
			
			cancelBetsID(betsIDs);
			
			return -1;
		}
		
		boolean someUpdated=false;
		boolean someNotUpdated=false;
		
		for(int i=0;i<betUpdateResult1.length;i++)
		{
			if(betUpdateResult1[i].getSuccess()==true)
			{
				//cancelBetsID(new long[]{bds1[i].getBetID()});
				bds1[i].setBetID(betUpdateResult1[i].getNewBetId());
				fillBetFromAPI(bds1[i]);
				bds1[i].setTransition(BetData.PLACE);
				someUpdated=true;
				
			}
			else
			{
				System.out.println("error:"+betUpdateResult1[i].getResultCode());
				if(betUpdateResult1[i].getResultCode()==UpdateBetsResultEnum.EVENT_CLOSED_CANNOT_MODIFY_BET)
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_MARKET_CLOSED);
					someNotUpdated=true;
					cancelBetsID(new long[]{bds1[i].getBetID()});
					// cancel this
				}
				else if(betUpdateResult1[i].getResultCode()==UpdateBetsResultEnum.EXCEEDED_EXPOSURE_OR_AVAILABLE_TO_BET_BALANCE)
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_BALANCE_EXCEEDED);
					someNotUpdated=true;
					cancelBetsID(new long[]{bds1[i].getBetID()});
					//cancel this
				}
				else 
				{
					bds1[i].setState(BetData.PLACING_ERROR,BetData.PLACE);
					bds1[i].setErrorType(BetData.ERROR_UNKNOWN);
					someNotUpdated=true;
					//cancelBetsID(new long[]{bds1[i].getBetID()});

				}
				
				
			}
		}
		
		if(someNotUpdated==true && someUpdated==true)
			return -2;
		else if(someUpdated==false)
			return -1;
		else
			return 0;
		
	}
	
	private int cancelBetsID(long betID[]) {
		
		CancelBets canc[] = new CancelBets[betID.length];
		
		for(int i=0;i<betID.length;i++)
		{
			canc[i]=new CancelBets();
			canc[i].setBetId(betID[i]);
		}
		
		
		CancelBetsResult betResult[]=null;
		
		try {
			betResult = ExchangeAPI.cancelBets(getMd().getSelectedExchange(),getMd().getApiContext(), canc);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return -1;
		}
			
		
		if(betResult==null)
		{
			System.err.println("Failed to cancel bet: ExchangeAPI.cancelBets return null");
			return -1;
		}
		
		boolean failCancel=false;
		for(int i=0;i<betResult.length;i++)
		{
			if (!betResult[i].getSuccess()) {
				System.err.println("Failed to cancel bet("+betResult[i].getBetId()+ "): Problem was: "+betResult[i].getResultCode());
				failCancel=true;
			}
		}
		
		if(failCancel)
			return -1;
		else
			return 0;
		
	}
	
	
	
	public int cancelBet(BetData bet)
	{
		Vector<BetData> place=new Vector<BetData>();
		place.add(bet);
		return cancelBets(place);
	}

	public int cancelBets(Vector<BetData> cancelBets)
	{
		CancelBets canc[] = new CancelBets[cancelBets.size()];
		
		BetData[] bds=cancelBets.toArray(new BetData[]{});
		
		for(int i=0;i<bds.length;i++)
		{
			canc[i]= new CancelBets();
			canc[i].setBetId(bds[i].getBetID());
			
			//bds[i].setTimestampPlace(Calendar.getInstance());
		}
		
		CancelBetsResult betResult[]=null;
		
		try {
			betResult = ExchangeAPI.cancelBets(getMd().getSelectedExchange(),getMd().getApiContext(), canc);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return -1;
		}
		
		if(betResult==null)
		{
			System.err.println("Failed to cancel bet: ExchangeAPI.cancelBets return null");
			return -1;
		}
		
		boolean someCancel=false;
		boolean someNotCancel=false;
		for(int i=0;i<betResult.length;i++)
		{
			if (betResult[i].getSuccess())
			{
				fillBetFromAPI(bds[i]);
				bds[i].setTransition(BetData.CANCEL);
				someCancel=true;
			}
			else
			{
				System.err.println("Failed to cancel bet("+betResult[i].getBetId()+ "): Problem was: "+betResult[i].getResultCode());
				someNotCancel=true;
			}
		}
		
		if(someNotCancel==true && someCancel==true)
			return -2;
		else if(someCancel==false)
			return -1;
		else
			return 0;
	}
	
		
	public Vector<BetData> getBetsByRunner(RunnersData rd)
	{
		Vector<BetData> ret=new Vector<BetData>();
		boolean found=false;
		for(BetData bd:bets)
		{
			if(bd.getRd()==rd)
			{
				found=true;
				ret.add(bd);
			}
		}
		if(found) return ret; else return null;
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
	
	@Override
	public boolean isPolling() {
	
		return polling;
	}
	
	@Override
	public Vector<BetData> getBets() {
		
		return bets;
	}
	
	
	public void clean()
	{
		for(BetData bd:bets)
		{
			bd.setState(BetData.UNMONITORED, BetData.SYSTEM);
		}
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
