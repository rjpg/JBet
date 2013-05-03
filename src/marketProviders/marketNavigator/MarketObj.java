package marketProviders.marketNavigator;

import java.text.SimpleDateFormat;
import java.util.Date;

import generated.global.BFGlobalServiceStub.MarketSummary;

public class MarketObj {
	
	public MarketSummary market;
	
	public MarketObj(MarketSummary m) {
		market=m;
	}
	
	@Override
	public String toString() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		
		
		//dateFormat.format(new Date(bd.getTimestampPlace().getTimeInMillis()))+"\n";
		
		return dateFormat.format(new Date(market.getStartTime().getTimeInMillis()))+" - "+market.getMarketName();
	}
}
