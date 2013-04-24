package marketNavigator;

import generated.global.BFGlobalServiceStub.EventType;

public class EventTypeObj {
	
		public EventType eventType;
	
		public EventTypeObj(EventType et) {
			eventType=et;
		}
		
		@Override
		public String toString() {
			return eventType.getName();
		}
}
