package marketProviders;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.EventType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import com.sun.xml.internal.bind.v2.TODO;

import DataRepository.MarketProvider;
import DataRepository.MarketProviderListerner;
import correctscore.CorrectScoreMainFrame;
import demo.handler.ExchangeAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.handler.GlobalAPI;
import demo.util.APIContext;

public class NextPreLiveMO extends MarketProvider{

	private Vector<EventData> todayGames=new Vector<EventData>();

	private Market currentMarket=null;
	
	private Vector<Market> currentMarkets=new Vector<Market>();

	private Vector<MarketProviderListerner> listeners=new Vector<MarketProviderListerner>();

	private Vector<EventData> eventsInformed=new Vector<EventData>();

	// THREAD
	private MOMPThread as;
	private Thread t;
	protected int updateInterval = 3000;
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
			
			System.out.println("\""+types[i].getName()+"\"");
			if(types[i].getName().equals("Soccer"))
			{
				indexFound=i;
			}
		}
		
		Calendar from=Calendar.getInstance();
		
		Calendar to=(Calendar)from.clone();
		
		to.add(Calendar.HOUR, 24);
		
		String sret=ExchangeAPI.getAllMarkets(selectedExchange, apiContext, new int[]{types[indexFound].getId()},from,to);	
		
		String[] markets=sret.split(":");
		
		String name="";
		
		EventData edInprocess=null;
		
		for(int i=1;i<markets.length;i++)
		{
			String fields[]=markets[i].split("~");
			String path[]=fields[5].split("\\\\");
			String name2=path[path.length-1];
			if(!name2.equals(name))
			{
				name=name2;
				System.out.println(name);
				edInprocess=findEventDataTodayGames(name);
				if(edInprocess==null)
				{
				edInprocess=new EventData(name);
				Calendar starttime=Calendar.getInstance();
				
				starttime.setTimeInMillis(Long.parseLong(fields[4]));
				//System.out.println("Time Milis "+fields[4] );
				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
				System.out.println("Time : "+dateFormat.format(starttime.getTimeInMillis()));
				edInprocess.setStartTime(starttime);
				todayGames.add(edInprocess);
				}
				
			}
			
			if(fields[1].equals("Match Odds"))
			{
				edInprocess.setMatchOddsId(Integer.parseInt(fields[0]));
				//System.out.println("id MO: " + edInprocess.getMatchOddsId());
			}
			
			if(fields[1].equals("Correct Score"))
			{
				edInprocess.setCorrectScoreId(Integer.parseInt(fields[0]));
				//System.out.println("id MO: " + edInprocess.getCorrectScoreId());
			}
			
			
			if(fields[1].equals("Over/Under 0.5 Goals"))
			{
				edInprocess.setOverUnderId(0,Integer.parseInt(fields[0]));
				//System.out.println("id MO: " + edInprocess.getCorrectScoreId());
			}
			
			if(fields[1].equals("Over/Under 1.5 Goals"))
			{
				edInprocess.setOverUnderId(1,Integer.parseInt(fields[0]));
				//System.out.println("id MO: " + edInprocess.getCorrectScoreId());
			}
			
			if(fields[1].equals("Over/Under 2.5 Goals"))
			{
				edInprocess.setOverUnderId(2,Integer.parseInt(fields[0]));
				//System.out.println("id MO: " + edInprocess.getCorrectScoreId());
			}
			
			if(fields[1].equals("Over/Under 3.5 Goals"))
			{
				edInprocess.setOverUnderId(3,Integer.parseInt(fields[0]));
				//System.out.println("id MO: " + edInprocess.getCorrectScoreId());
			}
			
			if(fields[1].equals("Over/Under 4.5 Goals"))
			{
				edInprocess.setOverUnderId(4,Integer.parseInt(fields[0]));
				//System.out.println("id MO: " + edInprocess.getCorrectScoreId());
			}
			
			if(fields[1].equals("Over/Under 5.5 Goals"))
			{
				edInprocess.setOverUnderId(5,Integer.parseInt(fields[0]));
				//System.out.println("id MO: " + edInprocess.getCorrectScoreId());
			}
			
			if(fields[1].equals("Over/Under 6.5 Goals"))
			{
				edInprocess.setOverUnderId(6,Integer.parseInt(fields[0]));
				//System.out.println("id MO: " + edInprocess.getCorrectScoreId());
			}
			
			if(fields[1].equals("Over/Under 7.5 Goals"))
			{
				edInprocess.setOverUnderId(7,Integer.parseInt(fields[0]));
				//System.out.println("id MO: " + edInprocess.getCorrectScoreId());
			}
			
			if(fields[1].equals("Over/Under 8.5 Goals"))
			{
				edInprocess.setOverUnderId(8,Integer.parseInt(fields[0]));
				//System.out.println("id MO: " + edInprocess.getCorrectScoreId());
			}
			
		}
		
		int i=0;
		for (EventData ed:todayGames)
		{
			System.out.println(i+" "+ed.getEventName()+" "+ed.getMatchOddsId()+" "+ed.getCorrectScoreId());
			i++;
		}
	}
	
	private EventData findEventDataTodayGames(String name) {
		for(EventData ed: todayGames)
		{
			if(ed.getEventName().equals(name))
				return ed;
		}
		
		return null;
	}


	private void refresh()
	{
		Calendar now= Calendar.getInstance();
		Calendar nowPlusTenSecs = (Calendar) now.clone();
		nowPlusTenSecs.add(Calendar.SECOND, 20);
		
		for(EventData ed:todayGames)
		{
			Calendar eventMinus1M= (Calendar) ed.getStartTime().clone();
			eventMinus1M.add(Calendar.MINUTE, -2);
			if (eventMinus1M.compareTo(now)>=0 &&eventMinus1M.compareTo(nowPlusTenSecs)<0 && !eventsInformed.contains(ed))
			{
				
				System.out.println(" Game processing :"+ed.getEventName()+ " MO Id : "+ed.getMatchOddsId());
				if(ed.getMatchOddsId()!=0)
				{
					Market matchOddsMarket = null;
					try {
						matchOddsMarket = ExchangeAPI.getMarket(
								selectedExchange,
								apiContext,
								ed.getMatchOddsId());
					} catch (Exception e) {
						e.printStackTrace();	
					}
					
					try {
						Thread.sleep(500);
						System.out.println("Slepping ");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(matchOddsMarket!=null)
					{
						System.out.println("Informing listeners : "+ed.getEventName()+ " MO Id : "+ed.getMatchOddsId());
						eventsInformed.add(ed);
						currentMarket=matchOddsMarket;
						informListeners();
					}
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

	@Override
	public Vector<Market> getCurrentSelectedMarkets() {
		// TODO Auto-generated method stub
		return currentMarkets;
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
					refresh(); 
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



}