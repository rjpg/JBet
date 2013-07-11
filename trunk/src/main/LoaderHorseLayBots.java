package main;

import generated.exchange.BFExchangeServiceStub.GetAccountFundsResp;
import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import marketProviders.MarketProvider;
import marketProviders.MarketProviderListerner;
import marketProviders.marketNavigator.MarketNavigator;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import bots.horseLay3Bot.HorseLay3Bot;
import bots.horseLay3Bot.HorseLay3BotAbove6;
import bots.horseLay3Bot.HorseLayFavorite;
import demo.handler.ExchangeAPI;
import demo.handler.GlobalAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.util.APIContext;
import demo.util.Display;
import demo.util.InflatedMarketPrices;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.Utils;
import GUI.MarketMainFrame;

public class LoaderHorseLayBots implements MarketChangeListener,MarketProviderListerner{

	
	
	public static APIContext apiContext = new APIContext();
	
	private static Market selectedMarket;
	private static Exchange selectedExchange;
	private static EventType selectedEventType;
	
	public MarketData md;
	
	public HorseLay3Bot horseLay3Bot;
	public HorseLay3BotAbove6 horseLay3BotAbove6;
	public HorseLayFavorite horseLayFavorite;
	
	// close (logout Frame)
	JFrame closeFrame;
	JButton close;
	JLabel fps;
	
	//interface
	MarketMainFrame mmf;
	
	public LoaderHorseLayBots() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// Initialise logging and turn logging off. Change OFF to DEBUG for detailed output.
		Logger rootLog = LogManager.getRootLogger();
		Level lev = Level.toLevel("OFF");
		rootLog.setLevel(lev);
		
		if (!Parameters.replay) {
			Display.println("Starting...");
		
			
			String username = "birinhos";
			String password = "birinhos777";
			
			
			selectedExchange = Exchange.UK;

			try {
				GlobalAPI.login(apiContext, username, password);
				Display.println("We are in");
				showAccountFunds(Exchange.UK);

				EventType[] types = GlobalAPI.getActiveEventTypes(apiContext);
				
				int indexFound=0;
				for(int i=0;i<types.length;i++)
				{
					//System.out.println("\""+types[i].getName()+"\"");
					if(types[i].getName().equals("Horse Racing - Todays Card"))
					{
						indexFound=i;
					}
				}
				
				System.out.println(types[indexFound].getName()+"-"+indexFound);
				//GetEventsResp resp = GlobalAPI.getEvents(apiContext, types[indexFound].getId());
				//BFEvent[] events = resp.getEventItems().getBFEvent();
				//indexFound=18;
				//System.out.println(events[indexFound].getEventName());
				// 17 "Market Racing todays"
				selectedEventType = GlobalAPI.getActiveEventTypes(apiContext)[indexFound];
				System.out.println(selectedEventType.getName());

				selectedMarket = selectMarketNextEvent(selectedEventType);

				InflatedMarketPrices prices = ExchangeAPI.getMarketPrices(
						selectedExchange, apiContext,
						selectedMarket.getMarketId());

				// Now show the inflated compressed market prices.
				Display.showMarket(selectedExchange, selectedMarket, prices);

			} catch (Exception e) {
				// If we can't log in for any reason, just exit.
				Display.showException("*** Failed to log in", e);
				System.exit(1);
			}
			
			
			
			
			md = new MarketData(selectedMarket, selectedExchange,apiContext);
			
			JFrame close = getCloseFrame();
			close.setVisible(true);
			close.setAlwaysOnTop(true);
			
			md.setLogging(Parameters.log);

			md.addMarketChangeListener(this);
			// //////////////////init Bots and displays
			// //////////////////////////////
			if(Parameters.graphicalInterface)
			{
				JFrame jf=new JFrame();
				MarketNavigator mp=new MarketNavigator(apiContext,selectedExchange);
				mp.addMarketProviderListener(this);
				
				JScrollPane jsp=new JScrollPane(mp.getPanel());
				jf.add(jsp);
				jf.setSize(400,400);
				jf.setLocation(0,100);
				jf.setVisible(true);
				
				//System.out.println("passei aqui");
				mmf = new MarketMainFrame(md);
				mmf.setSize(400, 400);
				mmf.setLocation(0,500);
				mmf.setVisible(true);
			}
			
		
			
			if(Parameters.horselayBots)
			{
				horseLay3Bot=new HorseLay3Bot(md,3);
				horseLay3BotAbove6=new HorseLay3BotAbove6(md,3);
				horseLayFavorite=new HorseLayFavorite(md, 3);
			}
		
			// /////////////////////after////////////////////////////////////////////
			md.startPolling();

			
			
		} 
		
		
			
	}
	
	public JFrame getCloseFrame()
	{
		if(closeFrame==null)
		{
			String sim="Real";
			String con="BF Server";
			
			if(Parameters.simulation)
				sim="Sim";
			
			if(Parameters.replay)
				con="File";
			
			 closeFrame=new JFrame(sim+" - "+con);
			 if(close==null)
			 {
				 close=new JButton("Logout");
				 close.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						if(!Parameters.replay)
						{
							LoaderHorseLayBots.this.md.stopPolling();
							LoaderHorseLayBots.logout();
							System.out.println("bye bye");
							System.exit(0);
						}
						else
						{
							System.out.println("bye bye");
							System.exit(0);
						}
						
					}
				});
			 }
			 
			 fps=new JLabel("FPS:");
			 
			 JPanel panel=new JPanel();
			 panel.setLayout(new BorderLayout());
			 panel.add(close,BorderLayout.CENTER);
			 panel.add(fps,BorderLayout.SOUTH);
			 
			 closeFrame.setContentPane(panel);
			 closeFrame.setSize(400, 100);
			 
		}
		return closeFrame;
	}
	
	public static void logout() {

		// Logout before shutting down.
		try {
			GlobalAPI.logout(apiContext);
		} catch (Exception e) {
			// If we can't log out for any reason, there's not a lot to do.
			Display.showException("Failed to log out", e);
		}
		Display.println("Logout successful");
	}
	
	// Retrieve and display the account funds for the specified exchange
	private static void showAccountFunds(Exchange exch) throws Exception {
		GetAccountFundsResp funds = ExchangeAPI.getAccountFunds(exch, apiContext);
		Display.showFunds(exch, funds);
	}

	
	public static Market selectMarketNextEvent(EventType  selectedEventTypeA)
	{
		Market ret=null;
		
		GetEventsResp resp=null;
		try {
			resp = GlobalAPI.getEvents(apiContext, selectedEventTypeA.getId());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BFEvent[] events = resp.getEventItems().getBFEvent() == null 
		? new BFEvent[0] : resp.getEventItems().getBFEvent();
MarketSummary[] markets = resp.getMarketItems().getMarketSummary() == null 
		? new MarketSummary[0] : resp.getMarketItems().getMarketSummary();

		Calendar now = Calendar.getInstance();
		
		System.out.println("NOW:"+now.get(Calendar.HOUR_OF_DAY)+":"+ 
				now.get(Calendar.MINUTE));
		
		MarketSummary next=null;
		
		for (MarketSummary m:markets)
		{
			
			String s=m.getMarketName();
			if(!s.contains("("))
			{
				long diffaux =m.getStartTime().getTime().getTime()-now.getTime().getTime() ;
				if(diffaux>0)
				{
					next=m;
					break;
				}
			}
		}
		
		if(next==null)
		{
			System.out.println("n√£o h√° eventos");
			logout();
			System.exit(1);
		}
		
		long diff =next.getStartTime().getTime().getTime()-now.getTime().getTime() ;
		
		for (MarketSummary m:markets)
		{
			
			String s=m.getMarketName();
			if(!s.contains("("))
			{
				long diffaux =  m.getStartTime().getTime().getTime() -now.getTime().getTime();
				
				if(diffaux<diff && diffaux>0)
				{
					next=m;
					System.out.println("trocou:"+  diffaux+" <"+diff);
					diff=diffaux;
				}
				
				
				//Calendar c=m.getStartTime();
				//System.out.println("Market:"+
				//m.getStartTime().get(Calendar.HOUR_OF_DAY)+":"+ 
				//m.getStartTime().get(Calendar.MINUTE)+"-"+
				//m.getMarketName());
			}
			
		}
		
		System.out.println("---------------------------------");
		System.out.println("Market:"+
				next.getStartTime().get(Calendar.HOUR_OF_DAY)+":"+ 
				next.getStartTime().get(Calendar.MINUTE)+"-"+
				next.getMarketName());
		
		int tries=3;
		while (tries>0)
		{
		try {
			ret=ExchangeAPI.getMarket(selectedExchange, apiContext, next.getMarketId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			tries--;
		}
		
		if(ret!=null)
			tries=0;
		
		}
		return ret;
	}
	
	

	

	public void MarketLiveMode(MarketData md) {
	//	System.out.println("XXXXXXXXXXX");
		System.err.println("Manager knows the market is Live");
		
		if(md.isInTrade())
		{
			System.err.println("Cant change the market because Market is in trade operation");
			return;
		}
		
		
		md.stopPolling();
		System.err.println("Stop Polling");
		try {
			Thread.sleep(md.getUpdateInterval()+100);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		md.clean();
		System.err.println("Clean");
		/*try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// e.printStackTrace();
		}*/
		
		selectedMarket=selectMarketNextEvent(selectedEventType);
		System.err.println("Select new Market");
		/*try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// e.printStackTrace();
		}*/
		System.err.println("Instantating new Market");
		md.setSelectedMarket(selectedMarket);
		/*try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// e.printStackTrace();
		}*/
		
		
		/// delay ?? for graphical restart...
		System.err.println("Start Pooling");
		md.startPolling();
	
	
	}


	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if(marketEventType==MarketChangeListener.MarketLive)
			//if(!md.isInTrade()) // espera para fechar a operaÁ„o de trading mesmo inPlay ...
			if(Parameters.jump_to_the_next_race)
				MarketLiveMode(md);
		
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			fps.setText("FPS:"+md.getFPS());
			
			if(Parameters.horselayBots 
					&& Utils.getMarketSateFrame(md,0)==MarketData.SUSPENDED && Utils.isInPlayFrame(md,0)==true
					&& horseLay3Bot!=null 
					&& horseLay3BotAbove6!=null
					&& horseLayFavorite!=null
					&& !horseLay3Bot.isInTrade()
					&& !horseLay3BotAbove6.isInTrade()
					&& !horseLayFavorite.isInTrade())
			{
				//System
				MarketLiveMode(md);
			}
			
		}
		//System.out.println("Chamou");
	}

	@Override
	public void newMarketSelected(MarketProvider mp, Market m) {
		
		if(md.isInTrade())
		{
			System.err.println("Cant change the market because Market is in trade operation");
			return;
		}
		
		md.stopPolling();
		System.err.println("Stop Polling");
		/*try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// e.printStackTrace();
		}*/
		md.clean();
		System.err.println("Clean");
	/*	try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// e.printStackTrace();
		}*/
		
		selectedMarket=m;
		System.err.println("Select new Market");
		/*try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// e.printStackTrace();
		}*/
		System.err.println("Instantating new Market");
		md.setSelectedMarket(selectedMarket);
		/*try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// e.printStackTrace();
		}*/
		
		
		/// delay ?? for graphical restart...
		System.err.println("Start Pooling");
		md.startPolling();
	
		
	}
	

	@Override
	public void newMarketsSelected(MarketProvider mp, Vector<Market> mv) {
		// TODO Auto-generated method stub
		
	}


	public static void main(String[] args)  throws Exception {
		Utils.init();
		
		
		//Statistics.init();
		
		//CategoriesManager.init();
		//CategoriesManager.loadRawAMFromFile();
		//CategoriesManager.processAMCatIntervals();
			
		Parameters.log=true;  // Log or not to Log when not in replay
		Parameters.replay=false; 
		Parameters.replay_file_list=false; 
		Parameters.replay_file_list_test=false; 
		Parameters.jump_to_the_next_race=false; //not go inplay ? 
		Parameters.REALISTIC_TIME_REPLAY=false;
		Parameters.PAUSE_BETWEEN_RACES_REPLAY=false;
		Parameters.saveFavorite=false; 
		Parameters.graphicalInterface=true; 
		Parameters.graphicalInterfaceBots=true; 
		Parameters.amountBot=false;  
		Parameters.manualBot=false; 
		Parameters.manualPlaceBetBot=false; // manual place bet bot for betManager test
		Parameters.studyBot=false; 
		Parameters.neuralBot=false;
		Parameters.neighboursCorrelationBot=false;
		Parameters.neuralDataBot=false; 
		Parameters.horselayBots=true; 
		
		Parameters.simulation=true;
		//Parameters.matchedStepsSimulation = 1; // in simulation part of matched amount in each call
		Parameters.WOM_DIST_CENTER=5;
		Parameters.CHART_FRAMES=300;
		Parameters.ODD_FAVORITE=2.00;
		int x=Integer.MAX_VALUE;
		System.out.println(x);
		new LoaderHorseLayBots();
		}
}
