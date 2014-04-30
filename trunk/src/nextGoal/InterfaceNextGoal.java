package nextGoal;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.exchange.BFExchangeServiceStub.MarketStatusEnum;
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
import javax.swing.JPanel;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import bfapi.handler.ExchangeAPI;
import bfapi.handler.GlobalAPI;
import bfapi.handler.ExchangeAPI.Exchange;

import DataRepository.MarketData.MarketThread;

import correctscore.CorrectScoreMainFrame;
import correctscore.GameMarketProcessFrame;
import correctscore.MatchOddPreLive;
import correctscore.MessageJFrame;
import demo.util.APIContext;
import demo.util.Display;
import demo.util.InflatedMarketPrices;

public class InterfaceNextGoal extends JFrame{

	JPanel north= new JPanel();
	JPanel center= null;
	JPanel south = new JPanel();
	
	Score scores[] = new Score[16];
	
	
	
	// north
	JLabel arLabel=new JLabel("Now Result :",JLabel.RIGHT);
	JComboBox<Score> arDrop=null;
	public JCheckBox autoOddsSet=new JCheckBox("Periodic Odds set",false);
	//JLabel arDataLabel=new JLabel("Odd : ",JLabel.RIGHT);
	//JCheckBox arNext=new JCheckBox("Process Next Goal");
	
	JPanel nextGoalsPanel=new JPanel();
	// Next Goal A
	NextGoalPanel nextGoalA=null;
	
	// Next Goal B
	NextGoalPanel nextGoalB=null;
	
	// messages
	public static MessageJFrame msjf=null;

	// Cancel
	JButton cancelButton=new JButton("CANCEL");
	
	
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
    
	// THREAD
	private MarketThread as;
	private Thread t;
	private boolean polling = false;
	//------ demand freq------
	protected int updateInterval = 5000;
	
	
	public InterfaceNextGoal() {
		super();
		startBetFair();
		initialize();
		startPolling();
	}
	
	public void initialize()
	{
		int i=0;
		for(int a=0;a<=3;a++)
		{
			for(int b=0;b<=3;b++)
			{
				scores[i]=new Score(a,b);
				i++;
				
			}
		}
		
		//scores[i]=new Score(-1, -1);
		
		
		
		setTitle("Next Goal - "+todayGames.get(eventSelected).getEventName());
		setSize(800,600);
		setVisible(true);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter() {
			 public void windowClosing(WindowEvent e) {
					e.getWindow().dispose();
					InterfaceNextGoal.this.msjf.dispose();
					stopPolling();
					logout();
			 }
		}
		);
		
		this.setLayout(new BorderLayout());
		
		//North
		north.setLayout(new GridLayout(1, 3));
		north.add(arLabel);
		arLabel.setHorizontalAlignment(JLabel.CENTER);
		north.add(getArDrop());
		
		autoOddsSet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(autoOddsSet.isSelected())
				{
					InterfaceNextGoal.this.startPolling();
				}
				else
				{
					InterfaceNextGoal.this.stopPolling();
				}
			}
		});
		
		
		
		north.add(autoOddsSet);
		//north.add(arDataLabel);
		//arDataLabel.setHorizontalAlignment(JLabel.CENTER);
	//	north.add(arNext);
		
		this.add(north,BorderLayout.NORTH);
		
		this.add(getCenterPanel(),BorderLayout.CENTER);
		
		
		
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextGoalA.stopPolling();
				nextGoalB.stopPolling();
			}
		});
		this.add(cancelButton,BorderLayout.SOUTH);
		
	}
	
	public JPanel getCenterPanel()
	{
		if(center==null)
		{
			center=new JPanel();
			center.setLayout(new BorderLayout());
			nextGoalsPanel.setLayout(new GridLayout(1,2));
			
			String teamA =todayGames.get(eventSelected).getEventName().split(" v ")[0];
					
			String teamB = todayGames.get(eventSelected).getEventName().split(" v ")[1];
			// Next Goal A
			nextGoalA=new NextGoalPanel(scores[0],scores[4],teamA,this);
			
			// Next Goal B
			nextGoalB=new NextGoalPanel(scores[0],scores[1],teamB,this);
			
			nextGoalsPanel.add( nextGoalA);
			nextGoalsPanel.add( nextGoalB);
			
			center.add(nextGoalsPanel,BorderLayout.NORTH);
			msjf=new MessageJFrame("x");
			center.add(msjf.getContentPane(),BorderLayout.CENTER);
			msjf.setVisible(false);
			msjf.writeMessageText("hello", Color.BLUE);
		}
		return center;
	}
	
	public JComboBox<Score> getArDrop()
	{
		if(arDrop==null)
		{
			
			arDrop=new JComboBox<Score>(scores);
			
			 
			arDrop.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println(arDrop.getSelectedItem());
						InterfaceNextGoal.this.nextGoalA.setScores((Score)arDrop.getSelectedItem(), ((Score)arDrop.getSelectedItem()).getNextScoreA());
						InterfaceNextGoal.this.nextGoalB.setScores((Score)arDrop.getSelectedItem(), ((Score)arDrop.getSelectedItem()).getNextScoreB());
						
					}
				}
					    
			);
			
		}
		return arDrop;
	}
	
	  private void startBetFair()
	    {
	    	LogManager.resetConfiguration();
	    	Logger rootLog = LogManager.getRootLogger();
			Level lev = Level.toLevel("OFF");
			rootLog.setLevel(lev);
			
			Display.println("Welcome to After Goal Correct Score Bot");
			
			try {
				//this.setUsername(Display.getStringAnswer("Betfair username:"));
				//username="Kariff";
				username="birinhos";
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
					fixturesToday+="0"+day;//+" ";//+sdf.format(now.getTime());
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
							InterfaceNextGoal.selectedExchange,
							InterfaceNextGoal.apiContext,
							markets[indexFound].getMarketId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(-1);
					return -1;
				}
				

				return 0;
			}
			
			
			//---------------------------------thread -----
			public class MarketThread extends Object implements Runnable {
				private volatile boolean stopRequested;

				private Thread runThread;

				public void run() {
					runThread = Thread.currentThread();
					stopRequested = false;
					
					while (!stopRequested) {
						System.out.println("pooling");
						InflatedMarketPrices prices = null;

						try {
								prices = ExchangeAPI.getMarketPrices(
										selectedExchange,
										apiContext,
										correctScoreMarket.getMarketId());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (prices.getMarketStatus()
									.equals(MarketStatusEnum._ACTIVE)) {
								nextGoalA.refreshOdds(prices);
								nextGoalB.refreshOdds(prices);
								
							}
							else
							{
								if (prices.getMarketStatus()
										.equals(MarketStatusEnum._CLOSED)) {
									
									System.out.println("Marke is closed - logout and exit");
									logout();
									System.exit(0);
								}
								else
									System.out.println("Market is not active");
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
				msjf.writeMessageText("Auto Odds Update Started", Color.ORANGE);
				autoOddsSet.setSelected(true);
				if (polling)
					return;
				as = new MarketThread();
				t = new Thread(as);
				t.start();

				polling = true;
				
			}

			public void stopPolling() {
				msjf.writeMessageText("Auto Odds Update Stoped", Color.ORANGE);
				autoOddsSet.setSelected(false);
				if (!polling)
					return;
				as.stopRequest();
				polling = false;

			}
			
		

}
