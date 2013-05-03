package marketProviders;

import java.util.Vector;


import generated.exchange.BFExchangeServiceStub.Market;

public abstract class MarketProvider {
	
	public abstract void addMarketProviderListener(MarketProviderListerner mpl);
	
	public abstract void removeMarketProviderListener(MarketProviderListerner mpl);
	
	public abstract Market getCurrentSelectedMarket();
	
	public abstract Vector<Market> getCurrentSelectedMarkets();
	
	
	
}
