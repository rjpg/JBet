package bets;

import generated.exchange.BFExchangeServiceStub.BetCategoryTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetPersistenceTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.CancelBets;
import generated.exchange.BFExchangeServiceStub.CancelBetsResult;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.UpdateBets;
import generated.exchange.BFExchangeServiceStub.UpdateBetsResult;

import java.awt.Color;

import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.TradeMecanism;
import DataRepository.Utils;

import main.Manager;
import demo.handler.ExchangeAPI;

public class BetUtils {

	//static public int PLACE_BETS_RETRIES = 3;
	
	public static long changeOrigLowBetPrice(long id,double price,double size,double holdOdd,TradeMecanism tm,MarketData marketData)
	{
		long newPriceLowLayBetId=-1;
		
		tm.writeMsgTM("changeOrigLowBet id:"+id+"  Size:"+size+" hold Odd:"+holdOdd,Color.BLUE);
		UpdateBets upd = new UpdateBets(); 
		upd.setBetId(id);
		upd.setOldBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setOldPrice(holdOdd);
		upd.setOldSize(size);
		upd.setNewBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setNewPrice(price);
		upd.setNewSize(size);
		
		UpdateBetsResult betResult=null;
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
			tm.writeMsgTM("ExchangeAPI.updateBets (Low Orig Update Price) Attempt :"+attempts, Color.BLUE);
			try {
				betResult = ExchangeAPI.updateBets(marketData.getSelectedExchange(),
						 Manager.apiContext, new UpdateBets[] {upd})[0];
			} catch (Exception e) {
				tm.writeMsgTM(e.getMessage(), Color.RED);
				if( e.getMessage().contains(new String("EVENT_SUSPENDED")) || e.getMessage().contains(new String("BET_IN_PROGRESS")) )
				{
					tm.writeMsgTM("ExchangeAPI.placeBets Returned NULL: Market is supended",Color.BLUE);
					attempts--;
				}
				// TODO Auto-generated catch block
				tm.writeMsgTM("ExchangeAPI.updateBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (betResult.getSuccess()) {
			tm.writeMsgTM("Bet "+betResult.getBetId()+" New Bet ID:"+betResult.getNewBetId() +" updated. New bet is "+betResult.getNewSize() +" @ "+betResult.getNewPrice(),Color.BLUE);
			 newPriceLowLayBetId=betResult.getNewBetId();
		} else {
			tm.writeMsgTM("changeOrigLowLayBetPrice() - Failed to update bet: Problem was: "+betResult.getResultCode(),Color.RED);
			return -1;
		}
		
		return newPriceLowLayBetId;
	}
	
	
	public static long changeOrigLowBetSize(long id,double size,double oldOdd,TradeMecanism tm,MarketData marketData)
	{
		long newSizeLowLayBetId=0;
		tm.writeMsgTM("changeOrigLowBetSize() id:"+id+"  Size:"+size,Color.BLUE);
		UpdateBets upd = new UpdateBets(); 
		upd.setBetId(id);
		upd.setOldBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setOldPrice(oldOdd);
		upd.setOldSize(2.0);
		upd.setNewBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setNewPrice(oldOdd);
		upd.setNewSize(Utils.convertAmountToBF(2.0+size));
		
		UpdateBetsResult betResult=null;
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
			tm.writeMsgTM("ExchangeAPI.updateBets (Low Orig Update) Attempt :"+attempts, Color.BLUE);
			try {
				betResult = ExchangeAPI.updateBets(marketData.getSelectedExchange(),
						 Manager.apiContext, new UpdateBets[] {upd})[0];
			} catch (Exception e) {
				tm.writeMsgTM(e.getMessage(), Color.RED);
				if(e.getMessage().contains(new String("EVENT_SUSPENDED"))  || e.getMessage().contains(new String("BET_IN_PROGRESS")) )
				{
					tm.writeMsgTM("ExchangeAPI.updateBets Returned NULL: Market is supended | Bet in progress",Color.BLUE);
					attempts--;
				}				
				tm.writeMsgTM("ExchangeAPI.updateBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
				e.printStackTrace();
			}
			attempts++;
		}
		
		if(betResult==null)
		{
			tm.writeMsgTM("ExchangeAPI.updateBets Returned NULL: No bets placed", Color.RED);
			return -1;
		}
		
		if (betResult.getSuccess()) {
			tm.writeMsgTM("Bet "+betResult.getBetId()+" New Bet ID:"+betResult.getNewBetId() +" updated. New bet is "+betResult.getNewSize() +" @ "+betResult.getNewPrice(),Color.BLUE);
			newSizeLowLayBetId=betResult.getNewBetId();
		} else {
			tm.writeMsgTM("changeOrigLowBetSize() - Failed to update bet: Problem was: "+betResult.getResultCode(),Color.RED);
			return -1;
		}
		
		return newSizeLowLayBetId;
	}
	
	
	
	
	public static long cancelBetID(long betID, TradeMecanism tm,MarketData marketData) {
		
		CancelBets canc = new CancelBets();
		canc.setBetId(betID);
		
		// We can ignore the array here as we only sent in one bet.
		CancelBetsResult betResult=null;
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
			try {
				betResult = ExchangeAPI.cancelBets(marketData.getSelectedExchange(),Manager.apiContext, new CancelBets[] {canc})[0];
			} catch (Exception e) {
				tm.writeMsgTM(e.getMessage(), Color.RED);
				if(e.getMessage().contains(new String("EVENT_SUSPENDED"))  || e.getMessage().contains(new String("BET_IN_PROGRESS")) )
				{
					tm.writeMsgTM("ExchangeAPI.cancelBets Returned NULL: Market is supended | Bet in progress",Color.BLUE);
					attempts--;
				}				
				tm.writeMsgTM("ExchangeAPI.cancelBets Returned NULL: bet not canceled :Attempt :"+attempts, Color.RED);
				e.printStackTrace();
			}
			attempts++;
		}
		
		if(betResult==null)
		{
			tm.writeMsgTM("Failed to cancel bet: ExchangeAPI.cancelBets return null ",Color.RED);
			return -1;
		}
		
		if (betResult.getSuccess()) {
			tm.writeMsgTM("Bet "+betResult.getBetId()+" cancelled.",Color.BLUE);
		} else {
			tm.writeMsgTM("Failed to cancel bet: Problem was: "+betResult.getResultCode(),Color.RED);
			return -1;
		}
		
		return betResult.getBetId();
	}
	
	public static PlaceBets createPlaceBet(double odd,double size, int type,  TradeMecanism tm ,MarketData marketData, RunnersData rd)
	{
		tm.writeMsgTM("Create Back PLACE BETS:"+size+"@"+odd, Color.BLUE);
		PlaceBets bet = new PlaceBets();
		bet.setMarketId(marketData.getSelectedMarket().getMarketId());
		bet.setSelectionId(rd.getId());
		bet.setBetCategoryType(BetCategoryTypeEnum.E);
		bet.setBetPersistenceType(BetPersistenceTypeEnum.NONE);
		if(type==BetData.BACK)
			bet.setBetType(BetTypeEnum.Factory.fromValue("B"));
		else
			bet.setBetType(BetTypeEnum.Factory.fromValue("L"));
		bet.setPrice(odd);
		bet.setSize(size);
		return bet;
	}
	
	public static PlaceBets createPlaceBet(BetData bd)
	{
		PlaceBets bet = new PlaceBets();
		bet.setMarketId(bd.getRd().getMarketData().getSelectedMarket().getMarketId());
		bet.setSelectionId(bd.getRd().getId());
		bet.setBetCategoryType(BetCategoryTypeEnum.E);
		bet.setBetPersistenceType(bd.getPersistenceType());
		if(bd.getType()==BetData.BACK)
			bet.setBetType(BetTypeEnum.Factory.fromValue("B"));
		else
			bet.setBetType(BetTypeEnum.Factory.fromValue("L"));
		bet.setPrice(bd.getOddRequested());
		bet.setSize(Utils.convertAmountToBF(bd.getAmount()));
		if(bd.isKeepInPlay())
			bet.setBetPersistenceType(BetPersistenceTypeEnum.IP);
		else
			bet.setBetPersistenceType(BetPersistenceTypeEnum.NONE);
		
		return bet;
	}
	
	public static String printBet(BetData bd)
	{
		String ret="";
		ret+="--- Bet Id: "+bd.getBetID()+" ---\n";
		
		if(bd.getRd()==null)
			ret+="Runner: null\n";
		else
			ret+="Runner: "+bd.getRd().getName()+"\n";
		
		if(bd.getType()==BetData.LAY)
			ret+="Type : LAY\n";
		else
			ret+="Type : BACK\n";
		
		ret+="Request: "+ bd.getAmount()+" @ "+bd.getOddRequested()+"\n";
		
		if(bd.getState()==BetData.NOT_PLACED)
			ret+="State: NOT_PLACED \n";
		else if(bd.getState()==BetData.UNMATHED)
			ret+="State: UNMATHED \n";
		else if(bd.getState()==BetData.PARTIAL_MACHED)
			ret+="State: PARTIAL_MACHED \n";
		else if(bd.getState()==BetData.MATHED)
			ret+="State: MACHED \n";
		else if(bd.getState()==BetData.CANCELED)
			ret+="State: CANCELED \n";
		else if(bd.getState()==BetData.PLACING_ERROR)
			ret+="State: PLACING_ERROR \n";

		ret+="Entry Queue Amount: "+bd.entryAmount+"\n";
		ret+="Entry Volume: "+bd.entryVolume+"\n";
		ret+="Matched: "+bd.getMatchedAmount()+" @ "+bd.getOddMached()+"\n";
		ret+=" --- --- \n";
		
		return ret;
	}
	
}
