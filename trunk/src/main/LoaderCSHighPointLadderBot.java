package main;

import generated.exchange.BFExchangeServiceStub.Market;

import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import logienvironment.LoginEnvironment;
import marketProviders.MarketProvider;
import marketProviders.MarketProviderListerner;
import marketProviders.nextPreLiveCS.NextPreLiveCS;
import marketProviders.nextPreLiveMo.LogMOPreLiveData;
import marketProviders.nextPreLiveMo.NextPreLiveMO;
import marketProviders.nextPreLiveMo.NextPreLiveMOTester;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import scrapers.xscores.ScraperGoals;

import bfapi.handler.GlobalAPI;
import bfapi.handler.ExchangeAPI.Exchange;
import bots.csHighPointLadder.CSHighPointLadder;


import DataRepository.MarketData;
import DataRepository.Utils;
import demo.util.APIContext;
import demo.util.Display;

public class LoaderCSHighPointLadderBot  implements MarketProviderListerner{

	// ---------------------- BETFAIR ----------------------------------
	LoginEnvironment loginEnv=null;
	
	// ---------user ------------------------
	public String username=null;
	public String password=null;
	
	//public ScraperGoals sg;
	NextPreLiveCS nplcs=null;
	
	public LoaderCSHighPointLadderBot() {
		// API login
		
		
		
		startBetFair();
		
	//	sg=new ScraperGoals(); 
	//	sg.startPolling();
	//	System.out.println("Scraper is now polling");
		
		nplcs=new NextPreLiveCS(loginEnv);
		nplcs.addMarketProviderListener(this);
		nplcs.startPolling();
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

		showLoginInterface();

		loginEnv = new LoginEnvironment();
		
		loginEnv.setUsername(username);
		loginEnv.setPassword(password);
		
		if(loginEnv.login()==-1)
		{
			System.err.println("*** Failed to log in");
			System.exit(1);
		}
		

		
	}
	
	public void showLoginInterface(){
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new GridLayout(2,2));

        //Labels for the textfield components        
        JLabel usernameLbl = new JLabel("Username:");
        JLabel passwordLbl = new JLabel("Password:");

        JTextField username = new JTextField();
        JPasswordField passwordFld = new JPasswordField();
        

        //Add the components to the JPanel        
        userPanel.add(usernameLbl);
        userPanel.add(username);
        userPanel.add(passwordLbl);
        userPanel.add(passwordFld);

        //As the JOptionPane accepts an object as the message
        //it allows us to use any component we like - in this case 
        //a JPanel containing the dialog components we want
        JOptionPane.showConfirmDialog(null, userPanel, "Enter your password:",JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE);
        this.username = username.getText();
        this.password = passwordFld.getText();
        
 
   }


	
	@Override
	public void newMarketSelected(MarketProvider mp, Market m) {
		MarketData md=new MarketData(m, loginEnv);
		md.setUpdateInterval(500);
		new CSHighPointLadder(md);
		md.startPolling();
		
	}

	@Override
	public void newMarketsSelected(MarketProvider mp, Vector<Market> mv) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		Utils.init();
		System.out.println("Utils.init() called ");
		
		Parameters.log=false;  // Log or not to Log when not in replay
		Parameters.replay=false; // replay or read from file
		Parameters.simulation=false;
		
		new LoaderCSHighPointLadderBot();
	}


}
