package nextGoal;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.exchange.BFExchangeServiceStub.MarketStatusEnum;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResult;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import nextGoal.NextGoalPanel.MarketThread;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import correctscore.MessageJFrame;
import demo.handler.ExchangeAPI;
import demo.handler.GlobalAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.util.APIContext;
import demo.util.Display;
import demo.util.InflatedMarketPrices;

public class InterfaceBetList extends JFrame{

	public static int NUMBER_OF_BETS=7;

	JPanel north= new JPanel();
	
	BetInterface betInterface[]=new BetInterface[NUMBER_OF_BETS];
	
	JPanel south = new JPanel();
	// Cancel
	JButton cancelButton=new JButton("CANCEL");
	boolean stopCicle=false; 
	// Place
	JButton placeButton=new JButton("PLACE");

	// messages
	public static MessageJFrame msjf=null;
	
	//Betfair
    private Vector<BFEvent> todayGames=new Vector<BFEvent>();
    public int eventSelected=-1;
  
    // ---------------------- BETFAIR ----------------------------------
    public static APIContext apiContext = new APIContext();
	public static Exchange selectedExchange;
	
	public static Market correctScoreMarket = null;
	
	// ---------user ------------------------
	public String username=null;
	public String password=null;
	//private static EventType selectedEventType;
	// -----------------------------------------------------------------
    
	protected int updateInterval = 5000;
	
	
	public InterfaceBetList() {
		super();
		startBetFair();
		initialize();
	}
	
	public void initialize()
	{
		setTitle("Bet Placer - "+todayGames.get(eventSelected).getEventName());
		//setSize(800,600);
		setVisible(true);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter() {
			 public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
					InterfaceBetList.this.msjf.dispose();
					logout();
			 }
		}
		);
		
		this.getContentPane().setLayout(new BorderLayout());
		
		north.setLayout(new GridLayout(NUMBER_OF_BETS, 1));
		
		for (int i=0;i<NUMBER_OF_BETS;i++)
		{
			betInterface[i]=new BetInterface(correctScoreMarket);
			north.add(betInterface[i]);
		}
		
		
		this.getContentPane().add(north,BorderLayout.NORTH);
		
		msjf=new MessageJFrame("x");
		msjf.setVisible(false);
		this.getContentPane().add(msjf.getContentPane(),BorderLayout.CENTER);
		msjf.writeMessageText("hello", Color.BLUE);
		
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopCicle=true;
				msjf.writeMessageText("Stop Trying", Color.RED);
			}
		});
		
		placeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopCicle=false;
				msjf.writeMessageText(" ----------------------------------------------------" , Color.BLACK);
				msjf.writeMessageText("if you press yes, when the market becames active, the following will be placed:", Color.RED);
				
				Vector<PlaceBets> bets=new Vector<PlaceBets>();
				
				for (int i=0;i<NUMBER_OF_BETS;i++)
				{
					if(betInterface[i].isActive())
					{
						if(betInterface[i].getBackLay().equals("L"))
							msjf.writeMessageText(betInterface[i].getStake()+"@"+betInterface[i].getOdd()+"  "+betInterface[i].getRunner().getName()+"    Lay" , Color.PINK);
						else
							msjf.writeMessageText(betInterface[i].getStake()+"@"+betInterface[i].getOdd()+"  "+betInterface[i].getRunner().getName()+"    Back", Color.BLUE);
						bets.add(betInterface[i].createBet());
					}
				}
				
				if(bets.size()==0)
				{
					msjf.writeMessageText(" No bets Selected to be Placed",Color.ORANGE);
					return ;
				}
				msjf.writeMessageText(" ----------------------------------------------------" , Color.BLACK);
				
				if(JOptionPane.showConfirmDialog(null, "Process Selected Bets ?")==0)
				{
					msjf.writeMessageText("Starting the process", Color.BLUE);
					
					
						
					startProcess(bets);	
						
					
					/*while ( stopCicle==false)
					 //placeBets(bets)==-1 &&
					{
						//try {
						//	Thread.sleep(100);
						//} catch (InterruptedException e1) {
						//	// TODO Auto-generated catch block
						//	e1.printStackTrace();
						//}
						msjf.writeMessageText("Trying ...", Color.BLUE);
					}
					*/
					
				}
				else
				{
					msjf.writeMessageText("Cancelling the process", Color.GREEN);
				}
	
			}
		});
		
		south.setLayout(new BorderLayout());
		south.add(placeButton,BorderLayout.WEST);
		south.add(cancelButton,BorderLayout.EAST);
		this.getContentPane().add(south,BorderLayout.SOUTH);
		
		this.repaint();
		
		this.setSize(640,400);
		
		this.setAlwaysOnTop(true);
		
		//this.repaint();
	}
	
	public void startProcess(Vector<PlaceBets> bets)
	{
		ProcessThread as;
		Thread t;
		as = new ProcessThread(bets);
		t = new Thread(as);
		t.start();
			
	}
	  private void startBetFair()
	    {
	    	LogManager.resetConfiguration();
	    	Logger rootLog = LogManager.getRootLogger();
			Level lev = Level.toLevel("OFF");
			rootLog.setLevel(lev);
			
			Display.println("Welcome to After Goal Correct Score Bot");
			
			try {
				this.setUsername(Display.getStringAnswer("Betfair username:"));
				//username="Kariff";
				//username="376235";
				//username="vedatrade1978";
				this.setPassword(Display.getStringAnswer("Betfair password ("+username+"):"));
			} catch (IOException e1) {
				System.out.println("Error reading Username and/or Password");
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
			
			try {
				this.loadEvents(apiContext,selectedExchange );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				eventSelected=Display.getIntAnswer("Game : ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Invalid Game");
				System.exit(-1);
			}
			
			System.out.println("Selected event : "+todayGames.get(eventSelected).getEventName());
			
			if (loadCorrectScoreMarket() == -1) {
				System.out.println(
						"Error loading Correct Score Market for this event !!");
				logout();
				System.exit(-1);
				// csmf.removeGameInProc(this);
				// this.stopPolling();
				// writeMessageText("END.", Color.ORANGE);
				// clean();
				// return;
			} else {
				System.out.println("Correct Score Market for this Event Loaded - Starting interface");
			}
			
			
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
	    
		
		  public void loadEvents(APIContext apiContext,Exchange selectedExchange ) throws Exception {
				// Get available event types.
				EventType[] types = GlobalAPI.getActiveEventTypes(apiContext);
				int indexFound=0;
				for(int i=0;i<types.length;i++)
				{
					//System.out.println("\""+types[i].getName()+"\"");
					if(types[i].getName().equals("Soccer - Fixtures") || types[i].getName().equals("Futebol - Fixtures"))
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
				
				int month=now.get(Calendar.MONTH);
				 
				String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
				
				sdf = new SimpleDateFormat("MMMMM",Locale.UK);
				sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
				int hour=now.get(Calendar.HOUR_OF_DAY);
				System.out.println("HOUR:"+hour);
				if(hour<3)
				{
					day--;//########################### se se correr depois da meia noite 
				}
				
				if(day>9)
					fixturesToday+=day;
				else
					fixturesToday+="0"+day+" "+sdf.format(now.getTime());
				//Calendar.AUGUST
				
				fixturesToday+=" "+months[month];
				
				
				System.out.println(fixturesToday);
				
				for(int i=0;i<events.length;i++)
				{
					//System.out.println("\""+events[i].getEventName()+"\"");
					if(events[i].getEventName().startsWith(fixturesToday))
					{
						indexFound=i;
						//break;
						
					}
				}
				
				System.out.println("selected "+events[indexFound].getEventName());
				
				
				resp=null;
				
				while (resp==null)
					resp = GlobalAPI.getEvents(apiContext, events[indexFound].getEventId());
				
				events = resp.getEventItems().getBFEvent();
				
				for(int i=0;i<events.length;i++)
				{
					System.out.println(i+" - \""+events[i].getEventName()+"\"");
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
		    
			public int loadCorrectScoreMarket() {

				GetEventsResp resp=null;
				MarketSummary[] markets = null;
				int indexFound = -1;

				int attempts = 0;
				while (attempts < 3 && markets == null) {
					attempts++;
					try {
						resp = GlobalAPI.getEvents(apiContext,
								todayGames.get(eventSelected).getEventId());
						markets = resp.getMarketItems().getMarketSummary();

						for (int i = 0; i < markets.length; i++) {
							if (markets[i].getMarketName().equals("Correct Score") || markets[i].getMarketName().equals("Correct Score")) {
								System.out.println("\"" + markets[i].getMarketName()
										+ "\" Market Found");
								indexFound = i;
							}
							// todayGames.add(events[i]);
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						
									
						
						//return -1;
					}
					if(markets == null)
					{
						System.out.println("Error Reading Markets from BETFAIR API: Try Login Again");

						try
						{
							
							LogManager.resetConfiguration();
					    	Logger rootLog = LogManager.getRootLogger();
							Level lev = Level.toLevel("OFF");
							rootLog.setLevel(lev);
							//CorrectScoreMainFrame.logout();
							selectedExchange = Exchange.UK;
							apiContext=new  APIContext();
							GlobalAPI.login(apiContext, getUsername(), getPassword());
							System.out.println("Re Login Again OK");
							
						}
						catch (Exception e)
						{
							// If we can't log in for any reason, just exit.
							
							System.out.println("Re Login Again NOT OK");
							
						}
					}
					//#######################
					// LOGIN  ???? if null  (3x)
					//#######################
				}

				if (indexFound == -1) {
					System.out.println("Correct Score Not Found: NOT OK");
					
					return -1;
				}

				try {
					correctScoreMarket = ExchangeAPI.getMarket(
							InterfaceBetList.selectedExchange,
							InterfaceBetList.apiContext,
							markets[indexFound].getMarketId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(-1);
					return -1;
				}
				

				return 0;
			}
			
		
			public int placeBets(Vector<PlaceBets> betsA) {
				msjf.writeMessageText("placing Bets",Color.BLACK);
				long id = 0;

				PlaceBets[] bets = betsA.toArray(new PlaceBets[] {});
				PlaceBetsResult[] betResult = null;

				
				try {
						betResult = ExchangeAPI.placeBets(
								InterfaceBetList.selectedExchange,
								InterfaceBetList.apiContext, bets);
					} catch (Exception e) {
						msjf.writeMessageText(e.getMessage(),
								Color.RED);
						
						return -1;
					}


				if (betResult == null) {
					msjf.writeMessageText(
							"ExchangeAPI.placeBets Returned NULL: No bets placed",
							Color.RED);
					return -1;
				} else {
					for (int x = 0; x < betResult.length; x++) {
						if (betResult[0].getSuccess()) {
							msjf.writeMessageText(
									"Bet Id:" + betResult[x].getBetId() + " placed("
											+ bets[x].getSize() + "@"
											+ bets[x].getPrice() + ") : Matched "
											+ betResult[x].getSizeMatched() + "@"
											+ betResult[x].getAveragePriceMatched(),
									Color.GREEN);

							id = betResult[0].getBetId();
							
						} else {

							msjf.writeMessageText(
									"Failed to place bet(" + bets[x].getSize() + "@"
											+ bets[x].getPrice() + "): Problem was: "
											+ betResult[x].getResultCode(), Color.RED);
							

						}
					}
					return 0;

				}
			}
	

			public class ProcessThread extends Object implements Runnable {
			
				Vector<PlaceBets> bets;
				
				public ProcessThread(Vector<PlaceBets> betsA)
				{
					bets=betsA;
				}
				
				public void run() {
					while ( stopCicle==false && placeBets(bets)==-1)
						{
							try {
								Thread.sleep(20);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							msjf.writeMessageText("Trying ...", Color.BLACK);
						}
					
					msjf.writeMessageText("Ending the process.", Color.BLUE);
				}
			}
}
