package marketProviders;

import java.util.Vector;


import generated.exchange.BFExchangeServiceStub.Market;

public interface MarketProviderListerner {

		public void newMarketSelected(MarketProvider mp,Market m);
		public void newMarketsSelected(MarketProvider mp,Vector<Market> mv);
}
