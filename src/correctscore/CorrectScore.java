package correctscore;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import bfapi.handler.GlobalAPI;
import bfapi.handler.ExchangeAPI.Exchange;

import DataRepository.Utils;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.EventType;
import demo.util.APIContext;
import demo.util.Display;

public class CorrectScore {

	public static APIContext apiContext = new APIContext();
	
	private static Market selectedMarket;
	private static Exchange selectedExchange;
	private static EventType selectedEventType;
	
	public CorrectScore()
	{
		Logger rootLog = LogManager.getRootLogger();
		Level lev = Level.toLevel("OFF");
		rootLog.setLevel(lev);
		
		Display.println("Welcome to the Betfair API Demo");
		String username=null;
		String password=null;
		try {
			username = Display.getStringAnswer("Betfair username:");
			password = Display.getStringAnswer("Betfair password:");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
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
		selectedMarket=null;
		
		
		try {
			selectedMarket=Utils.chooseMarket(apiContext,selectedExchange );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(selectedMarket==null)
		{
			System.out.println("No market selected");
			System.exit(-1);
		}
		
		
	}

	public static void main(String[] args) throws Exception {
		new  CorrectScore();
	}
	
}
