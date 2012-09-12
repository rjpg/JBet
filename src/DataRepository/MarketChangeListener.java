package DataRepository;

public interface MarketChangeListener {

	
	public static final int MarketUpdate=0;
	public static final int MarketLive=1;
	public static final int MarketNew=2;
	
	public void MarketChange(MarketData md, int marketEventType);
	
	
/*	public static final int BetPlaced=0;
	public static final int BetMatched=1;
	public static final int BetPartialMatched=2;
	public static final int BetCancelled=2;

	public void MarketBetChange(MarketData md, BetData bd, int marketBetEventType);*/
}
