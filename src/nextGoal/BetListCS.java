package nextGoal;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.exchange.BFExchangeServiceStub.MarketStatusEnum;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResult;
import generated.exchange.BFExchangeServiceStub.Runner;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import nextGoal.NextGoalPanel.MarketThread;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import DataRepository.OddObj;
import DataRepository.RunnerObj;
import DataRepository.Utils;

import correctscore.MessageJFrame;
import demo.handler.ExchangeAPI;
import demo.handler.GlobalAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.util.APIContext;
import demo.util.Display;
import demo.util.InflatedMarketPrices;
import demo.util.InflatedMarketPrices.InflatedPrice;
import demo.util.InflatedMarketPrices.InflatedRunner;

public class BetListCS extends JFrame{

	public static int NUMBER_OF_BETS=10;

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
	
	
	//CS
	public JPanel panelCS=null;
	public JComboBox<Score> comboScores=null;
	public Score scores[] = new Score[16];
	public static String[] teams={"A","B"};
	public JComboBox<String> comboTeam=null;
	public JButton computeOdds=null;
	
	public JButton computeResults=null;
	
	public JCheckBox checkAutoUpdate=null;
	
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
	protected int updateInterval = 5000;
	
	public BetListCS() {
		super();
		startBetFair();
		initialize();
		startPolling();
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
					BetListCS.this.msjf.dispose();
					logout();
					stopPolling();
					System.out.println("X:"+BetListCS.this.getWidth()+"Y:"+BetListCS.this.getHeight());
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
		
		JScrollPane jsp = new JScrollPane();
		jsp.setViewportView(north);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		this.getContentPane().add(north,BorderLayout.NORTH);
		
		

	    // Add scroll pane to the content pane.
		
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
				checkAutoUpdate.setSelected(false);
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
		
		
		int i=0;
		for(int a=0;a<=3;a++)
		{
			for(int b=0;b<=3;b++)
			{
				scores[i]=new Score(a,b);
				i++;
				
			}
		}
		
		south.add(getPanelCS(),BorderLayout.CENTER);
		
		this.getContentPane().add(south,BorderLayout.SOUTH);
		
		this.repaint();
		
		//this.setSize(820,620);
		this.setSize(820,370);
		
		this.setAlwaysOnTop(true);
		
		//this.repaint();
		
		
	}
	
	public JPanel getPanelCS()
	{
		if(panelCS==null)
		{
			panelCS=new JPanel();
			
			comboScores=new JComboBox<Score>(scores);
			
			comboTeam=new JComboBox<String>(teams);
			
			computeOdds=new JButton("Compute");
			
			computeOdds.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					computeOdds();
				}
			});
			
			computeResults=new	 JButton("Results");
			
			computeResults.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					computeResults();
				}
			});
			
			checkAutoUpdate=new JCheckBox("Auto",false);
			
			
			panelCS.setLayout(new GridLayout(1, 5));
			
			panelCS.add(computeOdds);
			panelCS.add(checkAutoUpdate);
			panelCS.add(comboScores);
			panelCS.add(comboTeam);
			panelCS.add(computeResults);
			
			
		}
		
		return panelCS;
		
	}
	
	public void computeResults()
	{
		Score actualScore=(Score) comboScores.getSelectedItem();
		
		// 0-0 
		if(betInterface[0].isAuto())
			betInterface[0].setRunner(getRunnerByScore(actualScore).getSelectionId());
		if(((String)comboTeam.getSelectedItem()).equals("A"))
		{
			//1-0
			if(NUMBER_OF_BETS<2) return;
			Runner rn=getRunnerByScore(actualScore.getNextScoreA());
			if(betInterface[1].isAuto())
				if(rn==null)
					betInterface[1].setRunner(-1);
				else
					betInterface[1].setRunner(rn.getSelectionId());
			//2-0
			if(NUMBER_OF_BETS<3) return;
			if(betInterface[2].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreA());
				if(rn==null)
					betInterface[2].setRunner(-1);
				else
					betInterface[2].setRunner(rn.getSelectionId());
			}
			//3-0
			if(NUMBER_OF_BETS<4) return;
			if(betInterface[3].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreA().getNextScoreA());
				if(rn==null)
					betInterface[3].setRunner(-1);
				else
					betInterface[3].setRunner(rn.getSelectionId());
			}
			
			//1-1
			if(NUMBER_OF_BETS<5) return;
			if(betInterface[4].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreB());
				if(rn==null)
					betInterface[4].setRunner(-1);
				else
					betInterface[4].setRunner(rn.getSelectionId());
			}
			
			//2-1
			if(NUMBER_OF_BETS<6) return;
			if(betInterface[5].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreA().getNextScoreB());
				if(rn==null)
					betInterface[5].setRunner(-1);
				else
					betInterface[5].setRunner(rn.getSelectionId());
			}
			
			//1-2
			if(NUMBER_OF_BETS<7) return;
			if(betInterface[6].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreB().getNextScoreB());
				if(rn==null)
					betInterface[6].setRunner(-1);
				else
					betInterface[6].setRunner(rn.getSelectionId());
			}
			
			
			//0-1
			if(NUMBER_OF_BETS<8) return;
			rn=getRunnerByScore(actualScore.getNextScoreB());
			if(betInterface[7].isAuto())
				if(rn==null)
					betInterface[7].setRunner(-1);
				else
					betInterface[7].setRunner(rn.getSelectionId());
			//0-2
			if(NUMBER_OF_BETS<9) return;
			if(betInterface[8].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreB().getNextScoreB());
				if(rn==null)
					betInterface[8].setRunner(-1);
				else
					betInterface[8].setRunner(rn.getSelectionId());
			}
			//0-3
			if(NUMBER_OF_BETS<10) return;
			if(betInterface[9].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreB().getNextScoreB().getNextScoreB());
				if(rn==null)
					betInterface[9].setRunner(-1);
				else
					betInterface[9].setRunner(rn.getSelectionId());
			}
			
			//1-0
			if(NUMBER_OF_BETS<11) return;
			if(NUMBER_OF_BETS<10) return;
			rn=getRunnerByScore(actualScore.getNextScoreA());
			if(betInterface[10].isAuto())
				if(rn==null)
					betInterface[10].setRunner(-1);
				else
					betInterface[10].setRunner(rn.getSelectionId());
			//2-0
			if(NUMBER_OF_BETS<12) return;
			if(betInterface[11].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreA());
				if(rn==null)
					betInterface[11].setRunner(-1);
				else
					betInterface[11].setRunner(rn.getSelectionId());
			}
			
			//3-0
			if(NUMBER_OF_BETS<13) return;
			if(betInterface[12].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreA().getNextScoreA());
				if(rn==null)
					betInterface[12].setRunner(-1);
				else
					betInterface[12].setRunner(rn.getSelectionId());
			}
			
			//1-1
			if(NUMBER_OF_BETS<14) return;
			if(betInterface[13].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreB());
				if(rn==null)
					betInterface[13].setRunner(-1);
				else
					betInterface[13].setRunner(rn.getSelectionId());
			}
			
			//2-1
			if(NUMBER_OF_BETS<15) return;
			if(betInterface[14].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreA().getNextScoreB());
				if(rn==null)
					betInterface[14].setRunner(-1);
				else
					betInterface[14].setRunner(rn.getSelectionId());
			}
			
			//1-2
			if(NUMBER_OF_BETS<16) return;
			if(betInterface[15].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreB().getNextScoreB());
				if(rn==null)
					betInterface[15].setRunner(-1);
				else
					betInterface[15].setRunner(rn.getSelectionId());
			}
			
		}
		else
		{
			//0-1
			Runner rn=getRunnerByScore(actualScore.getNextScoreB());
			if(NUMBER_OF_BETS<2) return;
			if(betInterface[1].isAuto())
				if(rn==null)
					betInterface[1].setRunner(-1);
				else
					betInterface[1].setRunner(rn.getSelectionId());
			//0-2
			if(NUMBER_OF_BETS<3) return;
			if(betInterface[2].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreB().getNextScoreB());
				if(rn==null)
					betInterface[2].setRunner(-1);
				else
					betInterface[2].setRunner(rn.getSelectionId());
			}
			//0-3
			if(NUMBER_OF_BETS<4) return;
			if(betInterface[3].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreB().getNextScoreB().getNextScoreB());
				if(rn==null)
					betInterface[3].setRunner(-1);
				else
					betInterface[3].setRunner(rn.getSelectionId());
			}
			
			//1-1
			if(NUMBER_OF_BETS<5) return;
			if(betInterface[4].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreB());
				if(rn==null)
					betInterface[4].setRunner(-1);
				else
					betInterface[4].setRunner(rn.getSelectionId());
			}
			
			//1-2
			if(NUMBER_OF_BETS<6) return;
			if(betInterface[5].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreB().getNextScoreB());
				if(rn==null)
					betInterface[5].setRunner(-1);
				else
					betInterface[5].setRunner(rn.getSelectionId());
			}
			
			//2-1
			if(NUMBER_OF_BETS<7) return;
			if(betInterface[6].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreA().getNextScoreB());
				if(rn==null)
					betInterface[6].setRunner(-1);
				else
					betInterface[6].setRunner(rn.getSelectionId());
			}
			
			
			//1-0
			if(NUMBER_OF_BETS<8) return;
			rn=getRunnerByScore(actualScore.getNextScoreA());
			if(betInterface[7].isAuto())
				if(rn==null)
					betInterface[7].setRunner(-1);
				else
					betInterface[7].setRunner(rn.getSelectionId());
			//2-0
			if(NUMBER_OF_BETS<9) return;
			if(betInterface[8].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreA());
				if(rn==null)
					betInterface[8].setRunner(-1);
				else
					betInterface[8].setRunner(rn.getSelectionId());
			}
			//3-0
			if(NUMBER_OF_BETS<10) return;
			if(betInterface[9].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreA().getNextScoreA());
				if(rn==null)
					betInterface[9].setRunner(-1);
				else
					betInterface[9].setRunner(rn.getSelectionId());
			}
			
			//0-1
			if(NUMBER_OF_BETS<11) return;
			rn=getRunnerByScore(actualScore.getNextScoreB());
			if(betInterface[10].isAuto())
				if(rn==null)
					betInterface[10].setRunner(-1);
				else
					betInterface[10].setRunner(rn.getSelectionId());
			//0-2
			if(NUMBER_OF_BETS<12) return;
			if(betInterface[11].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreB().getNextScoreB());
				if(rn==null)
					betInterface[11].setRunner(-1);
				else
					betInterface[11].setRunner(rn.getSelectionId());
			}

			//0-3
			if(NUMBER_OF_BETS<13) return;
			if(betInterface[12].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreB().getNextScoreB().getNextScoreB());
				if(rn==null)
					betInterface[12].setRunner(-1);
				else
					betInterface[12].setRunner(rn.getSelectionId());
			}
			
			//1-1
			if(NUMBER_OF_BETS<14) return;
			if(betInterface[13].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreB());
				if(rn==null)
					betInterface[13].setRunner(-1);
				else
					betInterface[13].setRunner(rn.getSelectionId());
			}
			
			//1-2
			if(NUMBER_OF_BETS<15) return;
			if(betInterface[14].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreB().getNextScoreB());
				if(rn==null)
					betInterface[14].setRunner(-1);
				else
					betInterface[14].setRunner(rn.getSelectionId());
			}
			
			//2-1
			if(NUMBER_OF_BETS<16) return;
			if(betInterface[15].isAuto())
			{
				rn=getRunnerByScore(actualScore.getNextScoreA().getNextScoreA().getNextScoreB());
				if(rn==null)
					betInterface[15].setRunner(-1);
				else
					betInterface[15].setRunner(rn.getSelectionId());
			}

			
		}
	}
	
	public void computeOdds()
	{
		Score actualScore=(Score) comboScores.getSelectedItem();
		Runner actualRunner=getRunnerByScore(actualScore);
		
		msjf.writeMessageText("Requesting Prices...",Color.BLACK);
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
			
			if(prices==null)
			{
				msjf.writeMessageText("Prices Request Return Null.",Color.RED);
				return;
			}
			
			if (prices.getMarketStatus()
					.equals(MarketStatusEnum._ACTIVE)) {
				msjf.writeMessageText("Prices Request Successful.",Color.BLACK);
				
				
			}
			else
			{
				if (prices.getMarketStatus()
						.equals(MarketStatusEnum._CLOSED)) {
					
					msjf.writeMessageText("Marke is closed",Color.RED);
					return ;
					
				}
				else{
					msjf.writeMessageText("Market is not active - Auto compute odds OFF",Color.RED);
					checkAutoUpdate.setSelected(false);
					return;
				}				
			}
		
			
			for(BetInterface bi:betInterface)
			{
				if(!bi.isAuto())
					continue;
				Score selectedScore=null;
				Score refScore=null;
				Score nextScore=null;
				
				selectedScore=getScoreByRunner(bi.getRunner());
				if(selectedScore==null)
				{
					msjf.writeMessageText(bi.getRunner().getName()+" Not computed",Color.RED);
					continue;
				}
				
				if(((String)comboTeam.getSelectedItem()).equals("A"))
				{	
					nextScore=actualScore.getNextScoreA();
					refScore=selectedScore.getPreviousScoreA();
				}
				else
				{	
					nextScore=actualScore.getNextScoreB();
					refScore=selectedScore.getPreviousScoreB();
				}
				 
				if(nextScore.goalA>selectedScore.goalA || nextScore.goalB>selectedScore.goalB)
				{
					msjf.writeMessageText(bi.getRunner().getName()+" Becames impossible",Color.ORANGE);
					bi.setOdd(30);
					bi.setBackLay("L");
					continue;
				}
				
				
				
				
				double refOdd=getOddBack(prices, getRunnerByScore(refScore).getSelectionId());
				double selectedOdd=getOddBack(prices, getRunnerByScore(selectedScore).getSelectionId());
				
				msjf.writeMessageText(bi.getRunner().getName()+"("+selectedOdd+") -> Odd Reference "+refScore+"("+getOddBack(prices, getRunnerByScore(refScore).getSelectionId())+")",Color.BLACK);
				
				if(bi.isAutoBackLay())
				{
					if(refOdd<=selectedOdd) //back
					{
						bi.setOdd(Utils.indexToOdd(Utils.oddToIndex(refOdd)+bi.getBackOffset()));
						bi.setBackLay("B");
					}
					else
					{
						bi.setOdd(Utils.indexToOdd(Utils.oddToIndex(refOdd)-bi.getLayOffset()));
						bi.setBackLay("L");
					}
				}
				else
				{
					if(bi.getBackLay().equals("B"))
					{
						bi.setOdd(Utils.indexToOdd(Utils.oddToIndex(refOdd)+bi.getBackOffset()));
					}
					else
					{
						bi.setOdd(Utils.indexToOdd(Utils.oddToIndex(refOdd)-bi.getLayOffset()));
					}
				}
					
				
			}
			
		
		
	}
	
	public double getOddBack(InflatedMarketPrices prices,int selectionID)
	{
		for (InflatedRunner r : prices.getRunners()) {
			if (r.getSelectionId()==selectionID) 
			{
				if(r.getBackPrices().size()==0)
				{
					msjf.writeMessageText("No Back found for("+getRunnerById(selectionID).getName()+")" , Color.ORANGE);
				}
				else
				{
					return r.getBackPrices().get(0).getPrice();
				}
			}
		}
		return 0;
	}
	
	public double getOddLay(InflatedMarketPrices prices,int selectionID)
	{
		for (InflatedRunner r : prices.getRunners()) {
			if (r.getSelectionId()==selectionID) 
			{
				if(r.getLayPrices().size()==0)
				{
					msjf.writeMessageText("No Lay found for("+getRunnerById(selectionID).getName()+")" , Color.ORANGE);
				}
				else
				{
					return r.getLayPrices().get(0).getPrice();
				}
			}
		}
		return 0;
	}

	
	public Runner getRunnerByScore(Score s)
	{
		for (Runner mr : correctScoreMarket.getRunners().getRunner()) {
			if (mr.getName().endsWith(s.toString())) 
			{
				//msjf.writeMessageText("found:"+mr.getName()+" ID:"+mr.getSelectionId(), Color.BLACK);
				return mr;
			}
		}
		
		return null;
	}
	
	public Score getScoreByRunner(Runner r)
	{
		for(Score s:scores)
		{
			if (r.getName().endsWith(s.toString()))
				return s;
		}
		
		return null;
	}
	
	public Runner getRunnerById(int id)
	{
		for (Runner mr : correctScoreMarket.getRunners().getRunner()) {
			if (mr.getSelectionId()==id) 
			{
				//msjf.writeMessageText("found:"+mr.getName()+" ID:"+mr.getSelectionId(), Color.BLACK);
				return mr;
			}
		}
		
		return null;
	}

	public Score getScoreByRunnerId(int id)
	{
		return getScoreByRunner(getRunnerById(id));
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
							BetListCS.selectedExchange,
							BetListCS.apiContext,
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
								BetListCS.selectedExchange,
								BetListCS.apiContext, bets);
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
			
			
			//---------------------------------------------------------------
			//---------------------------------thread -----
			public class MarketThread extends Object implements Runnable {
				private volatile boolean stopRequested;

				private Thread runThread;

				public void run() {
					runThread = Thread.currentThread();
					stopRequested = false;
					
					while (!stopRequested) {
						if(checkAutoUpdate.isSelected())
						{
							msjf.writeMessageText("Auto Update",Color.GREEN);
							computeOdds();
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
				msjf.writeMessageText("Ready to Auto Update", Color.GREEN);
				as = new MarketThread();
				t = new Thread(as);
				t.start();

				polling = true;
				
			}

			public void stopPolling() {
				if (!polling)
					return;
				
				msjf.writeMessageText("Stop pooling", Color.GREEN);
				as.stopRequest();
				polling = false;

			}
			
}

