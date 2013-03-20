package bets;

import generated.exchange.BFExchangeServiceStub.Bet;
import generated.exchange.BFExchangeServiceStub.BetCategoryTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetPersistenceTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetStatusEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.CancelBets;
import generated.exchange.BFExchangeServiceStub.CancelBetsResult;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.UpdateBets;
import generated.exchange.BFExchangeServiceStub.UpdateBetsResult;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.TradeMecanism;
import DataRepository.Utils;

import main.Manager;
import demo.handler.ExchangeAPI;

public class BetUtils {

	
	public static long cancelBetID(long betID, MarketData marketData) {
		
		CancelBets canc = new CancelBets();
		canc.setBetId(betID);
		
		// We can ignore the array here as we only sent in one bet.
		CancelBetsResult betResult=null;
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
			try {
				betResult = ExchangeAPI.cancelBets(marketData.getSelectedExchange(),marketData.getApiContext(), new CancelBets[] {canc})[0];
			} catch (Exception e) {
				//tm.writeMsgTM(e.getMessage(), Color.RED);
				if(e.getMessage().contains(new String("EVENT_SUSPENDED"))  || e.getMessage().contains(new String("BET_IN_PROGRESS")) )
				{
					//tm.writeMsgTM("ExchangeAPI.cancelBets Returned NULL: Market is supended | Bet in progress",Color.BLUE);
					attempts--;
				}				
				//tm.writeMsgTM("ExchangeAPI.cancelBets Returned NULL: bet not canceled :Attempt :"+attempts, Color.RED);
				e.printStackTrace();
			}
			attempts++;
		}
		
		if(betResult==null)
		{
			//tm.writeMsgTM("Failed to cancel bet: ExchangeAPI.cancelBets return null ",Color.RED);
			return -1;
		}
		
		if (betResult.getSuccess()) {
			//tm.writeMsgTM("Bet "+betResult.getBetId()+" cancelled.",Color.BLUE);
		} else {
			//tm.writeMsgTM("Failed to cancel bet: Problem was: "+betResult.getResultCode(),Color.RED);
			return -1;
		}
		
		return betResult.getBetId();
	}
	
	
	public static  BetData createBetData(Bet bet,MarketData md)
	{
		//System.out.println("entrei");
		if(bet==null)
			return null;
		
		BetData ret=null;
		if(bet.getBetType()==BetTypeEnum.B)
			ret=new BetData(md.getRunnersById(bet.getSelectionId()),bet.getRequestedSize(),bet.getPrice(),BetData.BACK,false);
		else // Is B or L
			ret=new BetData(md.getRunnersById(bet.getSelectionId()),bet.getRequestedSize(),bet.getPrice(),BetData.LAY,false);
		
		ret.setBetID(bet.getBetId());
		ret.setMatchedAmount(bet.getMatchedSize());
		//System.err.println(bet.getMatchedSize());
		ret.setOddMached(bet.getAvgPrice());
		
		//System.out.println("--------------------------");
		//System.out.println("Bet Status: "+bet.getBetStatus());
		//System.out.println("Bet Id: "+bet.getBetId());
		//System.out.println("Bet RequestedSize: "+bet.getRequestedSize());
		//System.out.println("Bet MatchedSize: "+bet.getMatchedSize());
		//System.out.println("Bet RemainingSize: "+bet.getRemainingSize());
		
		
		if(bet.getBetStatus()==BetStatusEnum.U)
			ret.setState(BetData.UNMATCHED,BetData.SYSTEM);
		
		if(bet.getBetStatus()==BetStatusEnum.M)
		{
			if(bet.getRequestedSize()>bet.getMatchedSize())
				ret.setState(BetData.PARTIAL_MACHED,BetData.SYSTEM);
			else
				ret.setState(BetData.MATCHED,BetData.SYSTEM);
		}
		
		if(bet.getBetStatus()==BetStatusEnum.MU)
			ret.setState(BetData.PARTIAL_MACHED,BetData.SYSTEM);
		
		if(bet.getBetStatus()==BetStatusEnum.C)
			if(bet.getMatchedSize()>0)  // Never happens otherwise is considered MATCHED and UNMATCHED part disappears 
				ret.setState(BetData.PARTIAL_CANCELED,BetData.SYSTEM); 
			else
				ret.setState(BetData.CANCELED,BetData.SYSTEM);
		
		
		if(bet.getBetStatus()==BetStatusEnum.V) //Voided (?)
			ret.setState(BetData.CANCELED,BetData.SYSTEM);
		//testar os voideds quando o mercado fica suspenso para ver se fica estado Cancelado "C" ou voided "V"
		
		//System.out.println("Bet State Number final: "+ret.getState());
		
		if(bet.getBetPersistenceType()==BetPersistenceTypeEnum.IP)
			ret.setKeepInPlay(true);
		
		return ret;
	}
	
	public static BetData getBetFromAPI(long id,MarketData md)
	{
		Bet gb=null;
		try {
			gb =ExchangeAPI.getBet(md.getSelectedExchange(), md.getApiContext(),id);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			return null;
		}
			
		if(gb==null)
		{
			System.err.println("Failed to get Bet: ExchangeAPI.getBet return null ");
			return null;
		}
		
		
		return BetUtils.createBetData(gb,md);
	
	}
	
	public static int fillBetFromAPI(BetData bd)
	{
		
		if(bd.getBetID()==null) return -1;
		
		if(bd.getRd()==null) return -1;
		
		if(bd.getRd().getMarketData()==null) return -1;
		
		
		BetData bdAux=BetUtils.getBetFromAPI(bd.getBetID(),bd.getRd().getMarketData());
		
		if(bdAux==null) return -1;

		
		bd.setOddMached(bdAux.getOddMached());
		
		bd.setOddRequested(bdAux.getOddRequested());
		
		bd.setMatchedAmount(bdAux.getMatchedAmount());
		bd.setRd(bdAux.getRd());
		
		//bd.setAmount(bdAux.getAmount());  //error
		// When bet is canceled with partial Matched the API getbet() gives only data about 
		// matched and consider complete matched (canceled part simply disappears)  
		if(bd.getAmount()>bdAux.getMatchedAmount() && bdAux.getState()==BetData.MATCHED)
		{
			bd.setState(BetData.PARTIAL_CANCELED, BetData.SYSTEM);	
		}
		else
			bd.setState(bdAux.getState(), BetData.SYSTEM);
		
		return 0;
	}
	
	public static PlaceBets createPlaceBet(BetData bd)
	{
		PlaceBets bet = new PlaceBets();
		bet.setMarketId(bd.getRd().getMarketData().getSelectedMarket().getMarketId());
		bet.setSelectionId(bd.getRd().getId());
		bet.setBetCategoryType(BetCategoryTypeEnum.E);
	
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
		String ret="\n";
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
		else if(bd.getState()==BetData.UNMATCHED)
			ret+="State: UNMATHED \n";
		else if(bd.getState()==BetData.PARTIAL_MACHED)
			ret+="State: PARTIAL_MACHED \n";
		else if(bd.getState()==BetData.MATCHED)
			ret+="State: MACHED \n";
		else if(bd.getState()==BetData.CANCELED)
			ret+="State: CANCELED \n";
		else if(bd.getState()==BetData.PARTIAL_CANCELED)
			ret+="State: PARTIAL_CANCELED \n";
		else if(bd.getState()==BetData.CANCEL_WAIT_UPDATE)
			ret+="State: CANCEL_WAIT_UPDATE \n";
		else if(bd.getState()==BetData.PLACING_ERROR)
			ret+="State: PLACING_ERROR \n";
		else if(bd.getState()==BetData.BET_IN_PROGRESS)
			ret+="State: BET_IN_PROGRESS \n";
		else if(bd.getState()==BetData.UNMONITORED)
			ret+="State: UNMONITORED \n";
		
		if(bd.getLastState()==BetData.NOT_PLACED)
			ret+="Last State: NOT_PLACED \n";
		else if(bd.getLastState()==BetData.UNMATCHED)
			ret+="Last State: UNMATHED \n";
		else if(bd.getLastState()==BetData.PARTIAL_MACHED)
			ret+="Last State: PARTIAL_MACHED \n";
		else if(bd.getLastState()==BetData.MATCHED)
			ret+="Last State: MACHED \n";
		else if(bd.getLastState()==BetData.CANCELED)
			ret+="Last State: CANCELED \n";
		else if(bd.getLastState()==BetData.PARTIAL_CANCELED)
			ret+="Last State: PARTIAL_CANCELED \n";
		else if(bd.getLastState()==BetData.CANCEL_WAIT_UPDATE)
			ret+="Last State: CANCEL_WAIT_UPDATE \n";
		else if(bd.getLastState()==BetData.PLACING_ERROR)
			ret+="Last State: PLACING_ERROR \n";
		else if(bd.getLastState()==BetData.BET_IN_PROGRESS)
			ret+="Last State: BET_IN_PROGRESS \n";
		else if(bd.getLastState()==BetData.UNMONITORED)
			ret+="Last State: UNMONITORED \n";

		//System.out.println("TRANSITION:"+bd.getTransition());
		if(bd.getTransition()==BetData.SYSTEM)
			ret+="Transition: SYSTEM \n";
		else if(bd.getTransition()==BetData.PLACE)
			ret+="Transition: PLACE \n";
		else if(bd.getTransition()==BetData.CANCEL)
			ret+="Transition: CANCEL \n";
		
		ret+="Entry Queue Amount: "+bd.entryAmount+"\n";
		ret+="Entry Volume: "+bd.entryVolume+"\n";
		ret+="Matched: "+bd.getMatchedAmount()+" @ "+bd.getOddMached()+"\n";
		
		ret+="Keep IP: "+bd.isKeepInPlay()+"\n";
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
		
		
		if(bd.getTimestampPlace()==null)
			ret+="Place Time : NULL\n";
		else
			ret+="Place Time : "+dateFormat.format(new Date(bd.getTimestampPlace().getTimeInMillis()))+"\n";
		
		if(bd.getTimestampFinalState()==null)
			ret+="Final State Time : NULL\n";
		else
			ret+="Final State Time : "+dateFormat.format(new Date(bd.getTimestampFinalState().getTimeInMillis()))+"\n";
		
		if(bd.getTimestampCancel()==null)
			ret+="Cancel State Time : NULL\n";
		else
			ret+="Cancel State Time : "+dateFormat.format(new Date(bd.getTimestampCancel().getTimeInMillis()))+"\n";
		
		
		ret+=" --- --- \n";
		
		return ret;
	}
	
	public static boolean isBetFinalState(int state)
	{
		if(state==BetData.MATCHED || 
				state==BetData.PARTIAL_CANCELED || 
				state==BetData.CANCELED || 
				state==BetData.PLACING_ERROR ||
				state==BetData.UNMONITORED)
			return true;
		else
			return false;
	}
}
