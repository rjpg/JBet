package marketProviders.nextPreLiveMo;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class LogMOPreLiveData implements MarketChangeListener{

	int x=1;
	
	public LogMOPreLiveData() {
	
	}
	
	
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			for(RunnersData rd:md.getRunners())
			{
				System.out.println(rd.getName()+" Odd Back:"+Utils.getAmountBackFrame(rd, 0)+" @ "+Utils.getOddBackFrame(rd, 0));
			}
			
			x--;
			
			if(x<=0)
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
