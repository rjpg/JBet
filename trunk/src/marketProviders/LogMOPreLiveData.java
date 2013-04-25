package marketProviders;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class LogMOPreLiveData implements MarketChangeListener{

	int x=3;
	
	public LogMOPreLiveData() {
	
	}
	
	
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			for(RunnersData rd:md.getRunners())
			{
				System.out.println(rd.getName()+" Odd Back:"+Utils.getOddBackLastFrameMove(rd));
			}
			
			x--;
			
			if(x<0)
			{
				System.out.println("Closing Market : "+md.getName()+" from :"+md.getEventName());
				md.removeMarketChangeListener(this);
				md.stopPolling();
				md.clean();
				md=null;
			}
		}
		
	}

}
