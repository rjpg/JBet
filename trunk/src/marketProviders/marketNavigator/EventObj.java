package marketProviders.marketNavigator;

import generated.global.BFGlobalServiceStub.BFEvent;

public class EventObj {

	public BFEvent event;
	
	public EventObj(BFEvent e) {
		event=e;
	}
	
	
	@Override
	public String toString() {
		return event.getEventName();
	}
}
