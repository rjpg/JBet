package marketProviders.nextPreLiveMo;

import java.util.Vector;

import marketProviders.MarketProvider;
import marketProviders.MarketProviderListerner;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import demo.handler.GlobalAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.util.APIContext;
import demo.util.Display;
import generated.exchange.BFExchangeServiceStub.Market;
import DataRepository.MarketData;

public class NextPreLiveMOTester implements MarketProviderListerner{

	// ---------------------- BETFAIR ----------------------------------
	public static APIContext apiContext = new APIContext();
	public static Exchange selectedExchange;
	// ---------user ------------------------
	public String username=null;
	public String password=null;
	
	NextPreLiveMO nplmo=null;
	
	public NextPreLiveMOTester() {
		// API login
		startBetFair();
		nplmo=new NextPreLiveMO(selectedExchange, apiContext);
		nplmo.addMarketProviderListener(this);
		nplmo.startPolling();
		
	
	}
	
	private void startBetFair()
	{
		LogManager.resetConfiguration();
		Logger rootLog = LogManager.getRootLogger();
		Level lev = Level.toLevel("OFF");
		rootLog.setLevel(lev);

		Display.println("Welcome");
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

		
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	

	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public void newMarketSelected(MarketProvider mp, Market m) {
		MarketData md=new MarketData(m, selectedExchange, apiContext);
		md.setUpdateInterval(500);
		md.addMarketChangeListener(new LogMOPreLiveData());
		md.startPolling();
		
	}

	@Override
	public void newMarketsSelected(MarketProvider mp, Vector<Market> mv) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		new NextPreLiveMOTester();
	}


}
