package correctscore;

import generated.exchange.BFExchangeServiceStub.Market;

public class MarketCorrectScore {
	public String name;
	public Market market;
	public boolean selected=false;
	
	public MarketCorrectScore() {
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Market getMarket() {
		return market;
	}
	public void setMarket(Market market) {
		this.market = market;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
