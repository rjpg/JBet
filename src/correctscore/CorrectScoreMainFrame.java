package correctscore;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.exchange.BFExchangeServiceStub.Runner;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleConstants.ColorConstants;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import scrapers.GameScoreData;
import scrapers.UpdateScoresListener;

import demo.handler.ExchangeAPI.Exchange;
import demo.handler.ExchangeAPI;
import demo.handler.GlobalAPI;
import demo.util.APIContext;
import demo.util.Display;
import demo.util.InflatedMarketPrices;
import demo.util.InflatedMarketPrices.InflatedPrice;
import demo.util.InflatedMarketPrices.InflatedRunner;

public class CorrectScoreMainFrame  implements UpdateScoresListener{

	public final static int MAX_GOALS_TO_PROCESS=1000;
	public int goalsProcessed=0;
	
	private MessageJFrame msgMF=null; 
	private JButton logoutButton=null;
	private JButton noMoreGoalsButton=null;
    
    private Vector<BFEvent> todayGames=new Vector<BFEvent>();
    
    private Vector<GameMarketProcessFrame> gamesInProc= new  Vector<GameMarketProcessFrame>();
    
    private Vector<MatchOddPreLive> matchOddPreLive=new Vector<MatchOddPreLive>();
    
    
    private boolean ignoreEvents=true;
    
    // ---------------------- BETFAIR ----------------------------------
    public static APIContext apiContext = new APIContext();
	public static Exchange selectedExchange;
	// ---------user ------------------------
	public String username=null;
	public String password=null;
	//private static EventType selectedEventType;
	// -----------------------------------------------------------------
    
	

	public CorrectScoreMainFrame ()
	{
		msgMF=new MessageJFrame("MAIN FRAME");
		
		msgMF.getBaseJpanel().add(getNoMoreGoalsButton(),BorderLayout.NORTH);
	    msgMF.getBaseJpanel().add(getLogoutButton(),BorderLayout.SOUTH);
	    
	    msgMF.setSize(600, 500);
		//csmf.setVisible(true);
	    
	    startBetFair();
	    
	    loadMatchOddGameFormFile();
	   
	}
	
	public void refreshMatchOddFile()
	{
		  for(BFEvent game:todayGames)
		    {
		    	try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	if(loadMatchOddGame(game)!=0)
		    		writeMessageText("Some problem Loading Match Odds in "+game.getEventName(), Color.RED);
		    }
		    
		    saveMatchOddGameFormFile();
	}
	
	private void saveMatchOddGameFormFile()
	{
		Calendar now = Calendar.getInstance();
		int day=now.get(Calendar.DAY_OF_MONTH);
		int month=now.get(Calendar.MONTH);
		month++;
		
		String fileName=day+"-"+month+"-favorites.txt";
		
		BufferedWriter out=null;
		
		try 
		{
			out = new BufferedWriter(new FileWriter(fileName, false));

			//out.write("aString");
			//out.newLine();
			//out.close();
		} 
		catch (IOException e) 
		{
			writeMessageText("Unable to open File:"+fileName+" to write", Color.ORANGE);
			e.printStackTrace();
			return;
		} 
		
		if(out==null)
		{
			writeMessageText("File:"+fileName+" Not Processed", Color.ORANGE);
			return;
		}
		
		for (MatchOddPreLive mopl:matchOddPreLive)
		{
			try 
			{
				out.write(mopl.makeString());
				out.newLine();
				out.flush();
			} catch (IOException e) {
				writeMessageText("Error wrtting data to log file:"+fileName,Color.RED);
				writeMessageText(e.getMessage(), Color.RED);
				e.printStackTrace();
			}
		}
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		writeMessageText("File:"+fileName+" Saved", Color.GREEN);
	}
	
	public MatchOddPreLive getMatchOddPreLiveById(long id)
	{
		for (MatchOddPreLive mopl:matchOddPreLive)
		if(mopl.getIdBF()==id)
			return mopl;
		return null;
	}
	
	private void loadMatchOddGameFormFile()
	{
		File f;
		
		Calendar now = Calendar.getInstance();
		int day=now.get(Calendar.DAY_OF_MONTH);
		int month=now.get(Calendar.MONTH);
		month++;
		
		String fileName=day+"-"+month+"-favorites.txt";
		try {
			if ((new File(fileName)).exists()) {
				 writeMessageText("File:"+fileName+" Found", Color.GREEN);
			} else {
				writeMessageText("File:"+fileName+" Not Found", Color.ORANGE);
				return;
			}

		} catch (Exception e) {
			writeMessageText(e.getMessage(), Color.RED);
			e.printStackTrace();
		}
		f=new File(fileName);
		
		BufferedReader input=null;
		
		try {
			input= new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			writeMessageText(e.getMessage(), Color.RED);
			System.err.println("Unable to open file ("+f.getAbsolutePath()+")"+e);
			e.printStackTrace();
		}
		
		if(input==null)
		{
			writeMessageText("File:"+fileName+" Not Processed", Color.ORANGE);
			return;
		}
		
		String s;
		try {
			while ((s=input.readLine()) != null)
			{
				MatchOddPreLive mopl=new MatchOddPreLive(s);
				matchOddPreLive.add(mopl);
				writeMessageText("Read Data from File:"+mopl.getTeams(), Color.BLUE);
				
			}
		} catch (Exception e) {
			writeMessageText(e.getMessage(), Color.RED);
			e.printStackTrace();
		}
		
		writeMessageText("File:"+fileName+" Processed", Color.GREEN);
				
		
	}
	
	public JButton getNoMoreGoalsButton()
	{
		  if (noMoreGoalsButton == null)
          {
			  noMoreGoalsButton = new JButton();
              //resetMsgsPanelButton.setBounds(new java.awt.Rectangle(8,232,196,16));
			  if(ignoreEvents)
				  noMoreGoalsButton.setText("Start Capturing Goals Events");
			  else
				noMoreGoalsButton.setText("Ignore Goals Events");
			  noMoreGoalsButton.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent e)
                  {
                	  if(ignoreEvents)
                	  {
                		  writeMessageText("Restart Capturing Events",Color.GREEN);
  						noMoreGoalsButton.setText("Ignore Goals Events");
  						ignoreEvents=false;
                	  }
                	  else
                	  {
						writeMessageText("Ignoring Events",Color.RED);
						noMoreGoalsButton.setText("Restart Capturing Goals Events");
						ignoreEvents=true;
                	  }
						//System.exit(0);
                     //logout
                  }
              });
          }
          return noMoreGoalsButton;
	}
 
    public JButton getLogoutButton()
    {
    	  if (logoutButton == null)
          {
    		  logoutButton = new JButton();
              //resetMsgsPanelButton.setBounds(new java.awt.Rectangle(8,232,196,16));
    		  logoutButton.setText("Logout");
    		  logoutButton.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent e)
                  {
                	 
						logout();
						System.out.println("bye bye");
						ignoreEvents=true;
						//System.exit(0);
                     //logout
                  }
              });
          }
          return logoutButton;
    }
    
    public void writeMessageText(String message, Color type)
    {
    	if(msgMF!=null)
			msgMF.writeMessageText(message, type);
     
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
	
		this.setUsername("birinhos");
		this.setPassword("6mgprldi777");
		
		
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
    
   
	public int loadMatchOddGame(BFEvent gameEvent)
	{
		double matchOddRunnerABack=0;
		double matchOddRunnerBBack=0;
		double matchOddRunnerALay=0;
		double matchOddRunnerBLay=0;
		
		String teamA = gameEvent.getEventName().split(" v ")[0];
		String teamB = gameEvent.getEventName().split(" v ")[1];
		
		long id = gameEvent.getEventId();
		
		Market matchOddsMarket = null;
		
		GetEventsResp resp=null;
		MarketSummary[] markets = null;
		int indexFound = -1;

		int attempts = 0;
		while (attempts < 3 && markets == null) {
			attempts++;
			try {
				resp = GlobalAPI.getEvents(CorrectScoreMainFrame.apiContext,
						gameEvent.getEventId());
				markets = resp.getMarketItems().getMarketSummary();

				for (int i = 0; i < markets.length; i++) {
					if (markets[i].getMarketName().contains(new String("Match Odds"))) {
						writeMessageText("\"" + markets[i].getMarketName()
								+ "\" Market Found", Color.BLUE);
						indexFound = i;
					}
					// todayGames.add(events[i]);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		if (indexFound == -1) {
			writeMessageText("Match Odds market not fount in envent"+gameEvent.getEventName(), Color.RED);
			return -1;
		}
		
		try {
			matchOddsMarket = ExchangeAPI.getMarket(
					CorrectScoreMainFrame.selectedExchange,
					CorrectScoreMainFrame.apiContext,
					markets[indexFound].getMarketId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			writeMessageText(e.getMessage(), Color.RED);
			e.printStackTrace();
			return -1;
		}
		
		InflatedMarketPrices prices = null;
		try {
			prices = ExchangeAPI.getMarketPrices(
					CorrectScoreMainFrame.selectedExchange,
					CorrectScoreMainFrame.apiContext,
					matchOddsMarket.getMarketId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(prices!=null)
		{
			if( prices.getInPlayDelay()>0)
			{
				writeMessageText("Loading Match Odd: Game event:"+gameEvent.getEventName()+": Event in play",Color.ORANGE);
				return -1;
				
			}
			writeMessageText("############## MATCH ODDS ############### ",Color.BLACK);
		
			writeMessageText("Market: " + matchOddsMarket.getName() + "("
					+ matchOddsMarket.getMarketId() + ")", Color.BLACK);
			writeMessageText("   Start time     : "
					+ matchOddsMarket.getMarketTime().getTime(), Color.BLACK);
			writeMessageText(
					"   Status         : " + matchOddsMarket.getMarketStatus(),
					Color.BLACK);
			writeMessageText(
					"   Location       : " + matchOddsMarket.getCountryISO3(),
					Color.BLACK);

			writeMessageText("Runners:", Color.BLUE);

			for (InflatedRunner r : prices.getRunners()) {
				Runner marketRunner = null;

				for (Runner mr : matchOddsMarket.getRunners().getRunner()) {
					if (mr.getSelectionId() == r.getSelectionId()) {
						marketRunner = mr;
						break;
					}
				}
				String bestLay = "";
				if (r.getLayPrices().size() > 0) {
					InflatedPrice p = r.getLayPrices().get(0);
					bestLay = String.format("%,10.2f %s @ %,6.2f",
							p.getAmountAvailable(), prices.getCurrency(),
							p.getPrice());
					
					if (marketRunner.getName().contains(teamA)) {
						matchOddRunnerALay=p.getPrice();
					}
					
					if (marketRunner.getName().contains(teamB)) {
						matchOddRunnerBLay=p.getPrice();
					}
				}

				String bestBack = "";
				if (r.getBackPrices().size() > 0) {
					InflatedPrice p = r.getBackPrices().get(0);
					bestBack = String.format("%,10.2f %s @ %,6.2f",
							p.getAmountAvailable(), prices.getCurrency(),
							p.getPrice());
					
					if (marketRunner.getName().contains(teamA)) {
						matchOddRunnerABack=p.getPrice();
					}
					
					if (marketRunner.getName().contains(teamB)) {
						matchOddRunnerBBack=p.getPrice();
					}
				}

				writeMessageText(
						String.format(
								"\"%20s\" (%6d): Matched Amount: %,10.2f, Last Matched: %,6.2f, Best Back %s, Best Lay:%s",
								marketRunner.getName(), r.getSelectionId(),
								r.getTotalAmountMatched(),
								r.getLastPriceMatched(), bestBack, bestLay),
						Color.BLACK);
			}
			writeMessageText("############## MATCH ODDS ############### ",
					Color.BLACK);

		}
		else
		{
			writeMessageText("Prices of Match Odss Market return null ",Color.RED);
			return -1;
		}
		
		MatchOddPreLive mopl=new MatchOddPreLive(gameEvent.getEventId(),gameEvent.getEventName(),
				 matchOddRunnerABack,matchOddRunnerBBack,matchOddRunnerALay,matchOddRunnerBLay);
		
		writeMessageText("mopl:"+mopl.makeString(), Color.BLUE);
		
		MatchOddPreLive existmopl=getMatchOddPreLiveById(mopl.getIdBF());
		
		if(existmopl==null)
		{
			matchOddPreLive.add(mopl);
		}
		else
		{
			matchOddPreLive.remove(existmopl);
			matchOddPreLive.add(mopl);
		}
		
		return 0;
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
    
    
    /* ------------------------ String manipulation ... Start ---------------------*/
    public int contain(char c,String ss)
    {
    	int x=0;
    	char[] s=ss.toCharArray();
    	
    	
    	for(int i=0;i<s.length;i++)
    	{
    		if(s[i]==c)
    			x++;
    	}
    	return x;
    }
    
  
    /**
     * The Strings must not have repeted chars (use removeRepitedChar(String ss))
     * @param a
     * @param b
     * @return
     */
    public int numberMatchedChars(String a,String b)
    {
    	int ret=0;
    	char[] s=a.toCharArray();
    	
    	for(int i=0;i<s.length;i++)
    	{
    		if(contain(s[i],b)!=0)
    		{
    			ret++;
    		}
    	}
    	
    	return ret;
    }
    
    /**
     * The Strings must not have repeted chars (use removeRepitedChar(String ss))
     * @param a
     * @param b
     * @return
     */
    public double matchedChars(String a,String b)
    {
    	double ret=0.0;
    	String sa=null;
    	String sb=null;
    	if(a.length()<b.length())
    	{
    		sa=a;
    		sb=b;
    	}
    	else
    	{
    		sa=b;
    		sb=a;
    	}
    	
    	int smallsize=sa.length();
    	
    	int matched=numberMatchedChars(sa,sb);
    	
    	ret=((double)matched*100.)/(double)smallsize;
    	
    	
    	return ret;
    	
    }
    
    public String removeRepitedChar(String ss)
    {
    	String ret="";
    	
    	char[] s=ss.toCharArray();
    	
    	for(int i=0;i<s.length;i++)
    	{
    		if(contain(s[i],ret)==0)
    		{
    			//System.out.println(contain(s[i],ret)+"ret:"+ret+":char:"+s[i]+":");
    			ret+=s[i]+"";
    		}
    	}
    	
    	
    	return ret;
    }
    
    public String removeChar(String ss,char c)
    {
    	String ret="";
    	
    	char[] s=ss.toLowerCase().toCharArray();
    	
    	for(int i=0;i<s.length;i++)
    	{
    		if(s[i]!=c)
    		{
    			//System.out.println(contain(s[i],ret)+"ret:"+ret+":char:"+s[i]+":");
    			ret+=s[i]+"";
    		}
    	}
    	
    	return ret;
    }
    
    /* ------------------------ String manipulation ... END ---------------------*/
    
    public BFEvent macthGame(GameScoreData gd, int scurePercentage)
    {
    	BFEvent ret=null;
    	
    	return ret;
    }
    
	@Override
	public void scoreUpdated(GameScoreData gd) {
		goalsProcessed++;
		if(goalsProcessed>CorrectScoreMainFrame.MAX_GOALS_TO_PROCESS)
		{
			writeMessageText("REACH MAX GOLAS TO PROCESS",Color.ORANGE);
			return;
		}
		if(ignoreEvents)
		{
			writeMessageText("Ignoring events",Color.ORANGE);
			writeMessageText(gd.getTeamA()+":"+gd.getActualGoalsA()+"("+gd.getPrevGoalsA()+")  VS "+
					gd.getTeamB()+":"+gd.getActualGoalsB()+"("+gd.getPrevGoalsB()+"): NOT PROCESSED", Color.BLUE);
			return;
		}
		
		writeMessageText(gd.getTeamA()+":"+gd.getActualGoalsA()+"("+gd.getPrevGoalsA()+")  VS "+
				gd.getTeamB()+":"+gd.getActualGoalsB()+"("+gd.getPrevGoalsB()+")", Color.BLUE);
		
		BFEvent found=null;
		for(BFEvent tg:todayGames)
		{
			//Calendar start = tg.getStartTime();
			//Calendar now = Calendar.getInstance();
			//long diffaux = start.getTime().getTime() - now.getTime().getTime();
			
			//System.out.println("Start-Time:"+getTimeStamp(start.getTimeInMillis())+" Now:"+getTimeStamp(now.getTimeInMillis())+" Diff:"+getTimeStamp(diffaux)+"  -  "+diffaux);

			//if (diffaux <= 0) // already started
			//{
				String[] testearr = tg.getEventName().split(" v ");
				// System.out.println("size:"+testearr.length);
				// System.out.println("A:"+testearr[0]+":");
				// System.out.println("B:"+testearr[1]+":");

				double ma = matchedChars(testearr[0].toLowerCase(), gd
						.getTeamA().toLowerCase());
				double mb = matchedChars(testearr[1].toLowerCase(), gd
						.getTeamB().toLowerCase());
				if (ma > 90. && mb > 90.) {
					writeMessageText(testearr[0] + ":" + ma + " VS "
							+ testearr[1] + ":" + mb, Color.ORANGE);
					if (found != null) {

						String[] testearraux = found.getEventName()
								.split(" v ");
						double maaux = matchedChars(
								testearraux[0].toLowerCase(), gd.getTeamA()
										.toLowerCase());
						double mbaux = matchedChars(
								testearraux[1].toLowerCase(), gd.getTeamB()
										.toLowerCase());
						if ((maaux + mbaux) == (ma + mb))
						{
							int nmcAAux=numberMatchedChars(testearraux[0].toLowerCase(), gd.getTeamA()
									.toLowerCase());
							int nmcBAux=numberMatchedChars(testearraux[1].toLowerCase(), gd.getTeamB()
									.toLowerCase());
							
							int nmcA = numberMatchedChars(testearr[0].toLowerCase(), gd
									.getTeamA().toLowerCase());
							int nmcB = numberMatchedChars(testearr[1].toLowerCase(), gd
									.getTeamB().toLowerCase());
							if((nmcAAux+nmcBAux)<(nmcA+nmcB))
							{
								found = tg;
								writeMessageText("new Found (longer names  matched): " + testearr[0] + ":"
										+ ma + " VS " + testearr[1] + ":" + mb,
										Color.GREEN);
							}
						}
						else if ((maaux + mbaux) < (ma + mb)) {
							found = tg;
							writeMessageText("new Found: " + testearr[0] + ":"
									+ ma + " VS " + testearr[1] + ":" + mb,
									Color.GREEN);

						}
					} else {
						found = tg;
						writeMessageText("Found: " + testearr[0] + ":" + ma
								+ " VS " + testearr[1] + ":" + mb, Color.GREEN);
					}
				}
			//}

		}
		if(found!=null) // Encontrado um jogo 
		{
			GameMarketProcessFrame foundgmpf=null;
			for (GameMarketProcessFrame gmpf:gamesInProc)
			{
				if(gmpf.getGameEvent()==found)
					foundgmpf=gmpf;
			}
			
			if(foundgmpf==null) // Não encontrado em processamento 
			{
				if(!gd.isMistakeState())
				{
					writeMessageText("New Processing Frame:"+found.getEventName(), Color.BLACK);
					GameMarketProcessFrame newgmpf=new GameMarketProcessFrame(this,found, gd.getPrevGoalsA(), gd.getPrevGoalsB(), gd.getActualGoalsA(), gd.getActualGoalsB(),gd.getTeamA()+" v "+gd.getTeamB());
					gamesInProc.add(newgmpf);
				}
				else
				{
					writeMessageText("Some Mistake in the resulst NOT OPEN Processing Frame:"+found.getEventName(), Color.RED);
					gd.setMistakeState(false);
				}
			}
			else  // se já está uma janela em processsamento 
			{
				writeMessageText(found.getEventName()+" Already in process", Color.RED);
				if(gd.isMistakeState())
				{
					foundgmpf.cancelProcessing();  // se foi engano do scraper tentar anular
					writeMessageText("Mistake in the results CANCEL CALL to "+found.getEventName(), Color.RED);
					gd.setMistakeState(false);
				}
			}
		}
		else
			writeMessageText("No Game Found", Color.RED);
		
		Toolkit.getDefaultToolkit().beep();
		
	}
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
	public String getTimeStamp(long time) {
		return dateFormat.format(new Date(time));
	}
	
	// chamado pelo clean() de janelas de processamento 
	public void removeGameInProc(GameMarketProcessFrame gameMarketProcessFrame) {
		writeMessageText("Removing processing window: "+gameMarketProcessFrame.getGameEvent().getEventName(), Color.BLACK);
		gamesInProc.remove(gameMarketProcessFrame);
		
	}
	
	public static void main(String[] args) throws Exception {
		CorrectScoreMainFrame csmf=new CorrectScoreMainFrame();
		
		String rui="rui Jorge pereira Gonçalves";
		char c='r';
		
		System.out.println(csmf.contain(c,rui));
		System.out.println(csmf.removeRepitedChar(csmf.removeChar(rui,' ')));
		System.out.println(csmf.numberMatchedChars("rui","rui"));
		System.out.println(csmf.matchedChars("rug","rui"));
		String test="Ventspils v Gulbene";
		
		String[] testearr=test.split(" v ");
		System.out.println("size:"+testearr.length);
		System.out.println("A:"+testearr[0]+":");
		System.out.println("B:"+testearr[1]+":");
		
	}
}
