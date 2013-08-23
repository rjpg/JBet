package categories.categories2013;

import java.awt.Color;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import bots.Bot;

public class FillLiquidityFileBot extends Bot{

	boolean farWritten=false;
	boolean mediumWritten=false;
	boolean nearWritten=false;
	
	public FillLiquidityFileBot(MarketData md) {
		super(md, "FillLiquidityFileBot");
		
	}
	
	public void newMarket(MarketData md)
	{
	
		setMd(md);
		
	}

	public void update()
	{
		if(!farWritten)
		{
			farWritten=true;
		}
		else if (!mediumWritten)
		{
			
		}else if(!nearWritten)
		{
			
		}
	}
	
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketNew)
			newMarket(md);
			
		if(marketEventType==MarketChangeListener.MarketUpdate)
			update();			
		
	}

	@Override
	public void writeMsg(String s, Color c) {
		System.out.println("Bot "+getName()+" Msg :"+s);
		
	}

}
