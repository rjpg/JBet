package bets;

import java.util.Vector;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;

public class BetManagerSim extends BetManager implements MarketChangeListener{

	public Vector<BetData> bets=new Vector<BetData>();
	
	private boolean polling = false;
	
	public BetManagerSim(MarketData mdA) {
		super(mdA);
		
		getMd().addMarketChangeListener(this);
		
	}

	@Override
	public Vector<BetData> getBets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<BetData> getBetsByRunner(RunnersData rd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BetData getBetById(long ID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int placeBet(BetData bet) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int placeBets(Vector<BetData> place) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int cancelBet(BetData bet) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int cancelBets(Vector<BetData> cancelBets) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void startPolling() {
		polling=true;
		
	}

	@Override
	public void stopPolling() {
		polling=false;
		
	}
	
	@Override
	public boolean isPolling() {

		return polling;	
	}

	
	@Override
	public void clean() {
		for(BetData bd:bets)
		{
			bd.setState(BetData.UNMONITORED, BetData.SYSTEM);
		}
		bets.clear();
		bets=null;
		md=null;
		
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		// TODO Auto-generated method stub
		
	}

	


}
