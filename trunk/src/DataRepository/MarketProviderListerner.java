package DataRepository;

import generated.exchange.BFExchangeServiceStub.Market;

public interface MarketProviderListerner {

		public void newMarketSelected(MarketProvider mp,Market m);
}
