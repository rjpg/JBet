package bets;

import generated.exchange.BFExchangeServiceStub.Bet;
import generated.exchange.BFExchangeServiceStub.BetCategoryTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetPersistenceTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetStatusEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.CancelBets;
import generated.exchange.BFExchangeServiceStub.CancelBetsResult;
import generated.exchange.BFExchangeServiceStub.PlaceBets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.encog.mathutil.Equilateral;

import bfapi.handler.ExchangeAPI;

import sun.nio.cs.ext.MacHebrew;
import sun.nio.cs.ext.MacThai;

import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.Utils;

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
				ret.setState(BetData.PARTIAL_MATCHED,BetData.SYSTEM);
			else
				ret.setState(BetData.MATCHED,BetData.SYSTEM);
		}
		
		if(bet.getBetStatus()==BetStatusEnum.MU)
			ret.setState(BetData.PARTIAL_MATCHED,BetData.SYSTEM);
		
		if(bet.getBetStatus()==BetStatusEnum.C)
		{
			if(bet.getMatchedSize()>0)  // Never happens otherwise is considered MATCHED, the UNMATCHED part disappears 
				ret.setState(BetData.PARTIAL_CANCELED,BetData.SYSTEM); 
			else
				ret.setState(BetData.CANCELED,BetData.SYSTEM);
		}
		
		if(bet.getBetStatus()==BetStatusEnum.V) //Voided (?)
			return null;
			
		//	ret.setState(BetData.CANCELED,BetData.SYSTEM);
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
		
		if(bd==null) return null;
		
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
		else if(bd.getState()==BetData.PLACING)
			ret+="State: PLACING \n";
		else if(bd.getState()==BetData.UNMATCHED)
			ret+="State: UNMATHED \n";
		else if(bd.getState()==BetData.PARTIAL_MATCHED)
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
		else if(bd.getLastState()==BetData.PLACING)
			ret+="Last State: PLACING \n";
		else if(bd.getLastState()==BetData.UNMATCHED)
			ret+="Last State: UNMATHED \n";
		else if(bd.getLastState()==BetData.PARTIAL_MATCHED)
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
		
		ret+="PassedOnGetMUBetsUpdate : "+bd.isPassedOnGetMUBetsUpdate()+"\n";
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
	
	
	public static OddData getOpenInfoBetData(Vector<BetData> vbd)
	{
		Vector <OddData> vod=new Vector<OddData>();
		for(BetData bd:vbd)
		{
			vod.add(bd.getOddDataMatched());
		}
		
		return getOpenInfo(vod);
	}
	
	public static OddData getOpenInfo(Vector<OddData> vod)
	{
		
		OddData ret=null;
		
		
		if(vod==null || vod.size()==0)
			return ret;
		
		Vector<OddData> bdB=new Vector<OddData>();
		Vector<OddData> bdL=new Vector<OddData>();
		
		for(OddData bd:vod)
		{
			if(bd.getType()==BetData.BACK)
				bdB.add(bd);
			else //Lay
				bdL.add(bd);
		}
		
		double totalAmB=0;
		double oddAvgB=0;
		
		if(bdB.size()!=0)
		{
		
			Vector<Double> oddsB=new Vector<Double>();
			Vector<Double> amsB=new Vector<Double>();
			
			for(OddData bd:bdB)
			{
				oddsB.add(bd.getOdd());
				amsB.add(bd.getAmount());
				totalAmB+=bd.getAmount();
			}
			
			oddAvgB=Utils.calculateOddAverage(oddsB.toArray(new Double[]{}), amsB.toArray(new Double[]{}));
		}
		
		double totalAmL=0;
		double oddAvgL=0;
		
		if(bdL.size()!=0)
		{
		
			Vector<Double> oddsL=new Vector<Double>();
			Vector<Double> amsL=new Vector<Double>();
			
			for(OddData bd:bdL)
			{
				oddsL.add(bd.getOdd());
				amsL.add(bd.getAmount());
				totalAmL+=bd.getAmount();
			}
			
			oddAvgL=Utils.calculateOddAverage(oddsL.toArray(new Double[]{}), amsL.toArray(new Double[]{}));
		}
		
		if(totalAmL==0)
			return new OddData(oddAvgB, totalAmB,BetData.BACK);
		
		if(totalAmB==0)
			return new OddData(oddAvgL, totalAmL,BetData.LAY);
		
		//System.out.println("Odd Avg L :"+oddAvgL+ " Total Am L : "+totalAmL); 
		//System.out.println("Odd Avg B :"+oddAvgB+ " Total Am B : "+totalAmB); 
		
		double amToReduce =Utils.closeAmountBack(oddAvgL, totalAmL, oddAvgB);
		//System.out.println("Am to reduce B :"+amToReduce); 
		OddData odB = new OddData(oddAvgB, totalAmB-amToReduce,BetData.BACK,vod.get(0).getRd());
		
		amToReduce =Utils.closeAmountLay(oddAvgB, totalAmB, oddAvgL);
		//System.out.println("Am to reduce L :"+amToReduce);
		OddData odL = new OddData(oddAvgL, totalAmL-amToReduce,BetData.LAY,vod.get(0).getRd());
		
		if(odB.getAmount()>odL.getAmount())
			return odB;
		else
			return odL;
	}
	
	public static OddData calculateMissing(OddData odPretended,OddData odHave)
	{
		
		double ret=0;
		if(odHave==null)
		{
			return odPretended;
		}
		
		if(odPretended==null)
			if(odHave.getType()==BetData.BACK)
				return new OddData(odHave.getOdd(),odHave.getAmount(),BetData.LAY);
			else
				return new OddData(odHave.getOdd(),odHave.getAmount(),BetData.BACK);
					
			
		if(odPretended.getType()==BetData.BACK && odHave.getType()==BetData.BACK)
		{
			double amountToCloseBack=Utils.closeAmountLay(odHave.getOdd(), odHave.getAmount(), odPretended.getOdd());
			
			ret= odPretended.getAmount()-amountToCloseBack;
			
			if(ret>0)
				return new OddData(odPretended.getOdd(),ret,BetData.BACK);
			else
				return new OddData(odPretended.getOdd(),Math.abs(ret),BetData.LAY);
		}
		else if(odPretended.getType()==BetData.BACK && odHave.getType()==BetData.LAY)
		{
			double amountToCloseLay=Utils.closeAmountBack(odHave.getOdd(), odHave.getAmount(), odPretended.getOdd());
			
			ret= odPretended.getAmount()+amountToCloseLay;
			
			if(ret>0)
				return new OddData(odPretended.getOdd(),ret,BetData.BACK);
			else
				return new OddData(odPretended.getOdd(),Math.abs(ret),BetData.LAY);
		}
		else if(odPretended.getType()==BetData.LAY && odHave.getType()==BetData.BACK)
		{
			double amountToCloseBack=Utils.closeAmountLay(odHave.getOdd(), odHave.getAmount(), odPretended.getOdd());
			
			ret= odPretended.getAmount()+amountToCloseBack;
			
			if(ret>0)
				return new OddData(odPretended.getOdd(),ret,BetData.LAY);
			else
				return new OddData(odPretended.getOdd(),Math.abs(ret),BetData.BACK);
		}
		else //if(odPretended.getType()==BetData.LAY && odHave.getType()==BetData.LAY)
		{
			double amountToCloseLay=Utils.closeAmountBack(odHave.getOdd(), odHave.getAmount(), odPretended.getOdd());
			
			ret= odPretended.getAmount()-amountToCloseLay;
			
			if(ret>0)
				return new OddData(odPretended.getOdd(),ret,BetData.LAY);
			else
				return new OddData(odPretended.getOdd(),Math.abs(ret),BetData.BACK);
		}
		
	}
	
	public static OddData getGreening(Vector<OddData> vod,OddData pretended, double oddGreening )
	{
			
		OddData odaux=getOpenInfo(vod);
		
		OddData odmissing=calculateMissing(odaux,pretended);
		
		return calculateMissing(new OddData(oddGreening,0),odmissing);
		
		
	}
	
	public static OddData getEquivalent(OddData od,double odd)
	{
		
		OddData aux=calculateMissing(new OddData(odd,0,od.getType()), od);
		aux.setType(od.getType());
		return aux;
		
	}
	
	public static void main(String[] args) {
		OddData od1=new OddData(6.0, 16.67, BetData.BACK);
		OddData od2=new OddData(10, 0, BetData.BACK);
		OddData od3=new OddData(100, 1, BetData.LAY);
		
		Vector<OddData> odv=new Vector<OddData>();
		
		
		//odv.add(od1);
		//odv.add(od2);
		//odv.add(od3);
		
		OddData odret=getOpenInfo(odv);
		System.out.println("Total : "+odret);
		//System.out.println("Lyability "+(odret.getAmount()*(odret.getOdd())));
		
		//OddData odret2=getGreening(odv,new OddData(40,4,BetData.BACK), 40 );
		//System.out.println("Close : "+Utils.convertAmountToBF(odret2.getAmount())+" @ "+odret2.getOdd()+ " "+odret2.getType());
		
		System.out.println(Utils.convertAmountToBF(getGreening(odv,od1,5.7999).getAmount()));
		
		//System.out.println(getEquivalent(od3, 10));
	}
	
}
