package correctscore;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import demo.handler.ExchangeAPI;
import demo.handler.GlobalAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.util.APIContext;
import demo.util.Display;

public class LogCorrectScoreSoccerData{

	// ---------------------- BETFAIR ----------------------------------
	public static APIContext apiContext = new APIContext();
	public static Exchange selectedExchange;
	// ---------user ------------------------
	public String username=null;
	public String password=null;
	//private static EventType selectedEventType;
	// -----------------------------------------------------------------

    private Vector<BFEvent> todayGames=new Vector<BFEvent>();

	private static Market selectedMarket;


	public LogCorrectScoreSoccerData() {
		super();


	}

	private void startBetFair()
	{
		LogManager.resetConfiguration();
		Logger rootLog = LogManager.getRootLogger();
		Level lev = Level.toLevel("OFF");
		rootLog.setLevel(lev);

		Display.println("Welcome to After Goal Correct Score Bot");
		/*
			try {
				this.setUsername(Display.getStringAnswer("Betfair username:"));
				this.setPassword(Display.getStringAnswer("Betfair password:"));
			} catch (IOException e1) {
				System.out.println("Error reading Username and/or Password");
				e1.printStackTrace();
			}
		 */

		this.setUsername("cocotaxi");
		this.setPassword("merdamerda0");


		// Perform the login before anything else.
		try
		{
			GlobalAPI.login(apiContext, username, password);
		}
		catch (Exception e)
		{
			// If we can't log in for any reason, just exit.
			Display.showException("*** Failed to log in", e);
			System.exit(1);
		}

		selectedExchange = Exchange.UK;

		try {
			this.loadEvents(apiContext,selectedExchange );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
    public void loadEvents(APIContext apiContext,Exchange selectedExchange ) throws Exception {
		// Get available event types.
		EventType[] types = GlobalAPI.getActiveEventTypes(apiContext);
		int indexFound=0;
		for(int i=0;i<types.length;i++)
		{
			//System.out.println("\""+types[i].getName()+"\"");
			if(types[i].getName().equals("Soccer - Fixtures"))
			{
				indexFound=i;
			}
		}
		
		System.out.println(types[indexFound].getName()+"-"+indexFound);
		
		GetEventsResp resp = GlobalAPI.getEvents(apiContext, types[indexFound].getId());
		BFEvent[] events = resp.getEventItems().getBFEvent();
		
		String fixturesToday="Fixtures ";
		
		Calendar now = Calendar.getInstance();
		int day=now.get(Calendar.DAY_OF_MONTH);
		SimpleDateFormat sdf;
		
		 
		sdf = new SimpleDateFormat("MMMMM",Locale.UK);
		sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		int hour=now.get(Calendar.HOUR_OF_DAY);
		System.out.println("HOUR:"+hour);
		if(hour<=3)
		{
			day--;//########################### se se correr depois da meia noite 
		}
		
		if(day>9)
			fixturesToday+=day;
		else
			fixturesToday+="0"+day+" "+sdf.format(now.getTime());
		//Calendar.AUGUST
		
		
		
		System.out.println(fixturesToday);
		
		for(int i=0;i<events.length;i++)
		{
			//System.out.println("\""+events[i].getEventName()+"\"");
			if(events[i].getEventName().startsWith(fixturesToday))
			{
				indexFound=i;
			}
		}
		
		System.out.println(events[indexFound].getEventName());
		
		
		resp=null;
		
		while (resp==null)
			resp = GlobalAPI.getEvents(apiContext, events[indexFound].getEventId());
		
		events = resp.getEventItems().getBFEvent();
		
		for(int i=0;i<events.length;i++)
		{
			System.out.println("\""+events[i].getEventName()+"\"");
			todayGames.add(events[i]);
			//System.out.println(events[i].getStartTime().toString()); //dá 0 em tempo
		}                                                            
		
/*
		// Get available events of this type
		Market selectedMarket = null;
		int eventId = types[typeChoice].getId();
		while (selectedMarket == null) {
			GetEventsResp resp = GlobalAPI.getEvents(apiContext, eventId);
			BFEvent[] events = resp.getEventItems().getBFEvent();
			if (events == null) {
				events = new BFEvent[] {};
			} else {
				// The API returns Coupons as event names, but Coupons don't contain markets so we remove any
				// events that are Coupons.
				ArrayList<BFEvent> nonCouponEvents = new ArrayList<BFEvent>(events.length);
				for(BFEvent e: events) {
					if(!e.getEventName().equals("Coupons")) {
						nonCouponEvents.add(e);
					}
				}
				events = (BFEvent[]) nonCouponEvents.toArray(new BFEvent[]{});
			}
			MarketSummary[] markets = resp.getMarketItems().getMarketSummary();
			if (markets == null) {
				markets = new MarketSummary[] {};
			}
			int choice = Display.getChoiceAnswer("Choose a Market or Event:", events, markets);

			// Exchange ID of 1 is the UK, 2 is AUS
			if (choice < events.length) {
				eventId = events[choice].getEventId(); 
			} else {
				choice -= events.length;
				selectedExchange = markets[choice].getExchangeId() == 1 ? Exchange.UK : Exchange.AUS;
				selectedMarket = ExchangeAPI.getMarket(selectedExchange, apiContext, markets[choice].getMarketId());
			}				
		}*/
		
	}

    public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}

	private static void chooseMarket() throws Exception {
		// Get available event types.
		EventType[] types = GlobalAPI.getActiveEventTypes(apiContext);
		
		int typeChoice = Display.getChoiceAnswer("Choose an event type:", types);
		
		System.err.println("Choosing Markets");
		// Get available events of this type
		selectedMarket = null;
		int eventId = types[typeChoice].getId();
		while (selectedMarket == null) {
			GetEventsResp resp = GlobalAPI.getEvents(apiContext, eventId);
			BFEvent[] events = resp.getEventItems().getBFEvent();
			if (events == null) {
				events = new BFEvent[] {};
			} else {
				// The API returns Coupons as event names, but Coupons don't contain markets so we remove any
				// events that are Coupons.
				ArrayList<BFEvent> nonCouponEvents = new ArrayList<BFEvent>(events.length);
				for(BFEvent e: events) {
					if(!e.getEventName().equals("Coupons")) {
						nonCouponEvents.add(e);
					}
				}
				events = (BFEvent[]) nonCouponEvents.toArray(new BFEvent[]{});
			}
			MarketSummary[] markets = resp.getMarketItems().getMarketSummary();
			if (markets == null) {
				markets = new MarketSummary[] {};
			}
			int choice = Display.getChoiceAnswer("Choose a Market or Event:", events, markets);

			// Exchange ID of 1 is the UK, 2 is AUS
			if (choice < events.length) {
				eventId = events[choice].getEventId(); 
			} else {
				choice -= events.length;
				selectedExchange = markets[choice].getExchangeId() == 1 ? Exchange.UK : Exchange.AUS;
				selectedMarket = ExchangeAPI.getMarket(selectedExchange, apiContext, markets[choice].getMarketId());
			}				
		}
	}


	public static void main(String [] args)
	{

		LogCorrectScoreSoccerData logger= new LogCorrectScoreSoccerData();

		logger.startBetFair();
	
		System.out.println("----------------------------------");

		String teamA, teamB, startTime, currentTime;

		for (BFEvent e:logger.todayGames)
		{
			
			String [] temp=e.getEventName().split(" v ");
			teamA=temp[0];
			teamB=temp[1];
			
			System.out.println(e.getEventId());
			
			System.out.println(e.getStartTime().getTimeInMillis()+" "+teamA+" "+teamB);
		}

	}





}
