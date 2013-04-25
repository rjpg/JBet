package marketProviders;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import correctscore.CorrectScoreMainFrame;

import demo.handler.ExchangeAPI;
import demo.handler.GlobalAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.util.APIContext;
import demo.util.Display;

import bets.BetManager;
import bets.BetManagerReal.BetsManagerThread;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;
import DataRepository.MarketProvider;
import DataRepository.MarketProviderListerner;

public class NextPreLiveMO extends MarketProvider{

	private Vector<BFEvent> todayGames=new Vector<BFEvent>();

	private Market currentMarket=null;

	private Vector<MarketProviderListerner> listeners=new Vector<MarketProviderListerner>();

	private Vector<BFEvent> eventsInformed=new Vector<BFEvent>();

	// THREAD
	private MOMPThread as;
	private Thread t;
	protected int updateInterval = 400;
	private boolean polling = false;

	// API login
	private APIContext apiContext;
	private Exchange selectedExchange;

	public NextPreLiveMO(Exchange selectedExchangeA,APIContext apiContextA) {

		
		apiContext=apiContextA;
		selectedExchange=selectedExchangeA;
		try {
			loadEvents();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		startPolling();

	}


	public void loadEvents() throws Exception {

		// Get available event types.
		EventType[] types = GlobalAPI.getActiveEventTypes(apiContext);
		int indexFound=-1;
		for(int i=0;i<types.length;i++)
		{
			//System.out.println("\""+types[i].getName()+"\"");
			if(types[i].getName().equals("Soccer - Fixtures"))
			{
				indexFound=i;
			}
		}

		System.out.println(types[indexFound].getName()+" with "+indexFound +" entries");

		GetEventsResp resp = GlobalAPI.getEvents(apiContext, types[indexFound].getId());
		BFEvent[] events = resp.getEventItems().getBFEvent();

		String fixturesToday="Fixtures ";

		Calendar now = Calendar.getInstance();
		int day=now.get(Calendar.DAY_OF_MONTH);
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMMMM",Locale.UK);	

		if(day>9)
			fixturesToday+=day+" "+sdf.format(now.getTime());
		else
			fixturesToday+="0"+day+" "+sdf.format(now.getTime());
	
		System.out.println(fixturesToday);

		for(int i=0;i<events.length;i++)
		{
			//System.out.println("\""+events[i].getEventName()+"\"");
			if(events[i].getEventName().startsWith(fixturesToday))
			{
				indexFound=i;
			}
		}

//		if (indexFound==-1 && indexFound>=events.length)
//			return;
			System.out.println(events[indexFound].getEventName());


		resp=null;

		while (resp==null)
			resp = GlobalAPI.getEvents(apiContext, events[indexFound].getEventId());

		events = resp.getEventItems().getBFEvent();

		for(int i=0;i<events.length;i++)
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
			
			System.out.println("#############: "+events[i].getStartTime());
			
			System.out.println(dateFormat.format(new Date(events[i].getStartTime().getTimeInMillis()))+" \""+events[i].getEventName()+"\"");
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

	private Market chooseMarket(BFEvent event) throws Exception {

		Market selectedMarket=null;
		GetEventsResp resp = GlobalAPI.getEvents(apiContext, event.getEventId());

		MarketSummary[] markets = resp.getMarketItems().getMarketSummary();

		if (markets == null) {
			return null;
		}

		int indexFound=-1;

		for (int i = 0; i < markets.length; i++) {
			if (markets[i].getMarketName().contains(new String("Match Odds"))) {
				System.out.println("\"" + markets[i].getMarketName()
						+ "\" Market Found");
				indexFound = i;
			}
			// todayGames.add(events[i]);
		}

		if (indexFound == -1) 
		{
			System.out.println("Match Odds market not fount in envent"+event.getEventName());
			return null;
		}
		selectedMarket = ExchangeAPI.getMarket(selectedExchange, apiContext, markets[indexFound].getMarketId());

		return selectedMarket;
	}

	private void refresh()
	{
		Calendar now= Calendar.getInstance();
		Calendar nowPlusTenSecs = (Calendar) now.clone();
		nowPlusTenSecs.add(Calendar.SECOND, 10);
		
		System.out.println("running");
		
		for (BFEvent bfe:todayGames)
		{
			Calendar eventMinus1M= (Calendar) bfe.getStartTime().clone();
			eventMinus1M.add(Calendar.MINUTE, -1);
			if (eventMinus1M.compareTo(now)>=0 &&eventMinus1M.compareTo(nowPlusTenSecs)<0 && !eventsInformed.contains(bfe))
			{
				eventsInformed.add(bfe);
				
				GetEventsResp resp=null;
				MarketSummary[] markets = null;
				int indexFound = -1;
				try {
					resp = GlobalAPI.getEvents(CorrectScoreMainFrame.apiContext,
							bfe.getEventId());
					markets = resp.getMarketItems().getMarketSummary();

					for (int i = 0; i < markets.length; i++) {
						if (markets[i].getMarketName().contains(new String("Match Odds"))) {
							
							indexFound = i;
						}
						// todayGames.add(events[i]);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Market matchOddsMarket = null;
				
				if(indexFound!=-1)
				{
					try {
						matchOddsMarket = ExchangeAPI.getMarket(
								CorrectScoreMainFrame.selectedExchange,
								CorrectScoreMainFrame.apiContext,
								markets[indexFound].getMarketId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						
						e.printStackTrace();
						
					}
				}
				
				if(matchOddsMarket!=null)
				{
					currentMarket=matchOddsMarket;
					informListeners();
				}
			}
		}
		

	}

	private void informListeners()
	{

		for(MarketProviderListerner mpl:listeners)
			mpl.newMarketSelected(this, currentMarket);
	}

	@Override
	public void addMarketProviderListener(MarketProviderListerner mpl) {
		listeners.add(mpl);


	}

	@Override
	public void removeMarketProviderListener(MarketProviderListerner mpl) {
		listeners.remove(mpl);

	}

	@Override
	public Market getCurrentSelectedMarket() {
		return currentMarket;

	}


	//---------------------------------thread -----
	private class MOMPThread extends Object implements Runnable {
		private volatile boolean stopRequested;

		private Thread runThread;

		public void run() {
			runThread = Thread.currentThread();
			stopRequested = false;

			while (!stopRequested) {
				try {

					refresh(); /// connect and get the data
					//System.out.println("Not sync with MarketData");


					//	refreshBets();
				} catch (Exception e) {
					e.printStackTrace();
				}




				try {
					Thread.sleep(updateInterval);
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}

		public void stopRequest() {
			stopRequested = true;

			if (runThread != null) {
				runThread.interrupt();

				// suspend()stop();
			}
		}
	}
	//-----------------------------------------end thread -------------------

	public void startPolling() {
		if (polling)
			return;

		as = new MOMPThread();
		t = new Thread(as);
		t.start();


		polling = true;

	}

	public void stopPolling() {
		if (!polling)
			return;
		as.stopRequest();
		polling = false;

	}


	public boolean isPolling() {

		return polling;
	}

	public static void main(String[] args) {



	}
}
