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

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import GUI.MarketMainFrame;
import bots.BaseOfBot;
import bots.BotAmountCat;
import bots.BotDutching;
import bots.BotSaveFavoriteToFile;
import bots.InfluenceBot;
import bots.ManualBot;
import bots.ManualPlaceBetBot;
import bots.MecanicBot;
import bots.NeighboursCorrelationBot;
import bots.NeuralBot;
import bots.NeuralDataBot;
import bots.StudyBot;
import bots.WomNeighboursBot;
import demo.handler.ExchangeAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.handler.GlobalAPI;
import demo.util.APIContext;
import demo.util.Display;
import demo.util.InflatedMarketPrices;

public class Manager  implements MarketChangeListener,MarketProviderListerner{

	
	
	public static APIContext apiContext = new APIContext();
	
	private static Market selectedMarket;
	private static Exchange selectedExchange;
	private static EventType selectedEventType;
	
	public MarketData md;
	
	
	// close (logout Frame)
	JFrame closeFrame;
	JButton close;
	JLabel fps;
	
	//interface
	MarketMainFrame mmf;
	
	public Manager() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// Initialise logging and turn logging off. Change OFF to DEBUG for detailed output.
		Logger rootLog = LogManager.getRootLogger();
		Level lev = Level.toLevel("OFF");
		rootLog.setLevel(lev);
		
		if (!Parameters.replay) {
			Display.println("Starting...");
		
			
			String username = "birinhos";
			String password = "6mgprldi777";
			
			
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
			
			if(Parameters.manualBot)
			{
				new ManualBot(md,this);
				new ManualBot(md,this);
			
			}
			
			if(Parameters.manualPlaceBetBot)
			{
				new ManualPlaceBetBot(md,this);
			}
			
			if(Parameters.mecanicBot)
				for(int i=0;i<20;i++)
					new MecanicBot(md, i);
			
			if(Parameters.saveFavorite)
			{
				BotSaveFavoriteToFile bsf=new BotSaveFavoriteToFile(md);
				
			}
			
			if(Parameters.neighboursCorrelationBot)
			{
					new NeighboursCorrelationBot(md);
			}
			
			if(Parameters.baseOfBot)
			{
				new BaseOfBot(md);
			}
			
			if (Parameters.dutchingBot)
			{
				new BotDutching(md);
			}

			
			// /////////////////////after////////////////////////////////////////////
			md.startPolling();

			
			
		} else {
			MarketData md = new MarketData(null, Exchange.UK,null);
			
			md.addMarketChangeListener(this);
			JFrame close = getCloseFrame();
			close.setVisible(true);
			close.setAlwaysOnTop(true);
			
			// //////////////////init Bots and displays
			// //////////////////////////////
			if(Parameters.graphicalInterface)
			{
				//System.out.println("passei aqui");
				mmf = new MarketMainFrame(md);
				//Bot123 a=new Bot123(md);
				mmf.setSize(400, 600);
				mmf.setVisible(true);
			}
			if(Parameters.manualBot)
			{
				
				new ManualBot(md,this);
			}
			
			if(Parameters.mecanicBot)
				for(int i=0;i<20;i++)
					new MecanicBot(md, i);
			
			if(Parameters.amountBot)
			{
				for(int i=0;i<20;i++)
					new BotAmountCat(md, i);

			}
			
			NeighboursCorrelationBot ncBot=null;
			
			if(Parameters.neighboursCorrelationBot)
			{
				ncBot=new NeighboursCorrelationBot(md);
			}
			
			if(Parameters.neuralBot)
			{
				for(int i=0;i<20;i++)
					new NeuralBot(md, i,ncBot);

			}
			
			if(Parameters.neuralDataBot)
			{
				for(int i=0;i<20;i++)
					new NeuralDataBot(md, i);

			}
			
			if(Parameters.womNeighboursBot)
			{
				for(int i=0;i<20;i++)
					new WomNeighboursBot(md, i);
			}
			
			
			
			if(Parameters.influenceBot)
			{
				for(int i=0;i<20;i++)
					new InfluenceBot(md,i,ncBot);
			}
			
			
			if(Parameters.studyBot)
			{
				StudyBot sb=new StudyBot(md);
			}
			
			
			if(Parameters.baseOfBot)
			{
				new BaseOfBot(md);
			}
			
			if (Parameters.dutchingBot)
			{
				new BotDutching(md);
			}
			
			if(Parameters.manualPlaceBetBot)
			{
				new ManualPlaceBetBot(md,this);
			}
			
			// /////////////////////after////////////////////////////////////////////
			md.runFile();
		
		}
		
		
	
		
		//logout();
//-------------------------------------------------------------------------------//
	/*		
			Calendar now = Calendar.getInstance();
			JFrame frame=new JFrame(selectedMarket.getName());
			MyChart2D chart=new MyChart2D();
			chart.setXRange(now.getTimeInMillis(), md.getStart().getTimeInMillis());
			//chart.setYRange(1, 5);
			
			MyChart2D chart2=new MyChart2D();
			//chart2.setYRange(0, 1000);
			chart2.setXRange(now.getTimeInMillis(), md.getStart().getTimeInMillis());
			
			JPanel panel=new JPanel();
			frame.setSize(500,500);
			panel.setLayout(new GridLayout(2,1));
			panel.add(chart);
			panel.add(chart2);
			frame.setContentPane(panel);
			frame.setVisible(true);
			
			double odd=0;
			double odd1=0;
			double odd2=0;
			double money1=0;
			double money2=0;
			RunnersData rd1=null;
			RunnersData rd2;
			long time1;
			long time2;
			while (true) {
				
				if(marketLive)
				{
					selectedMarket=selectMarketNextEvent(selectedEventType);
					md.clean();
					marketLive=false;
					chart.removeTrace(rd1.getName());
					chart2.removeTrace("money-1");
					md.refresh();
					chart.setXRange(now.getTimeInMillis(), md.getStart().getTimeInMillis());
					System.err.println("end");
				}
				else
				md.refresh();
				
				Vector<RunnersData> rdvec=md.getRunners();
				
				if(rdvec.size()>2)
				{
					//System.out.println("entrei");
					rd1=rdvec.get(0);
					odd1=rd1.getData().get(rd1.getData().size()-1).getOddLay();
					time1=rd1.getData().get(rd1.getData().size()-1).getTimestamp().getTimeInMillis();
					chart.addValue(rd1.getName(), time1, odd1, Color.RED);
				
					//rd2=rdvec.get(1);
					//odd2=rd2.getData().get(rd2.getData().size()-1).getOddLay();
					//time2=rd2.getData().get(rd2.getData().size()-1).getTimestamp().getTimeInMillis();
					//chart.addValue(rd2.getName(), time2, odd2, Color.BLUE);
				
					money1=rd1.getData().get(rd1.getData().size()-1).getAmountLay();
					chart2.addValue("money-1", System.currentTimeMillis(), money1, Color.RED);
					System.out.println(money1);
					//money2=rd2.getData().get(rd2.getData().size()-1).getAmountLay();
					//chart2.addValue("money-2", System.currentTimeMillis(), money2, Color.BLUE);
				
				
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//sleep for 1000 ms
				}	
			}
			
			
	*/
		
			
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
							Manager.this.md.stopPolling();
							Manager.logout();
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
		
		try {
			ret=ExchangeAPI.getMarket(selectedExchange, apiContext, next.getMarketId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		/*try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// e.printStackTrace();
		}*/
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
			fps.setText("FPS:"+md.getFPS());
		
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


/*	@Override
	public void MarketBetChange(MarketData md, BetData bd, int marketBetEventType) {
		// TODO Auto-generated method stub
		
	}
*/


	
}
