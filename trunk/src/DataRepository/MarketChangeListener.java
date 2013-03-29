package DataRepository;

public interface MarketChangeListener {

	public static final int MarketUpdate=0;
	public static final int MarketLive=1;
	public static final int MarketNew=2;
	
	public void MarketChange(MarketData md, int marketEventType);
	
}
