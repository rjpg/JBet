package DataRepository;

import generated.exchange.BFExchangeServiceStub.Market;

public abstract class MarketProvider {
	
	public abstract void addMarketProviderListener(MarketProviderListerner mpl);
	
	public abstract void removeMarketProviderListener(MarketProviderListerner mpl);
	
	public abstract Market getCurrentSelectedMarket();
	
	
	
}
