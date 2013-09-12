package main;

import generated.exchange.BFExchangeServiceStub.GetAccountFundsResp;
import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Calendar;
import java.util.Vector;

import javax.smartcardio.ATR;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import logienvironment.LoginEnvironment;
import marketProviders.MarketProvider;
import marketProviders.MarketProviderListerner;
import marketProviders.marketNavigator.MarketNavigator;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.IORetrievalOperator;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.generator.ExampleSetGenerator;
import com.rapidminer.operator.io.ExampleSource;
import com.rapidminer.operator.io.ModelLoader;
import com.rapidminer.operator.io.RepositorySource;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;

import bfapi.handler.ExchangeAPI;
import bfapi.handler.GlobalAPI;
import bfapi.handler.ExchangeAPI.Exchange;
import bots.BaseOfBot;
import bots.BotAmountCat;
import bots.InfluenceBot;
import bots.ManualBot;
import bots.ManualPlaceBetBot;
import bots.MecanicBot;
import bots.NeighboursCorrelationBot;
import bots.NeuralBot;
import bots.NeuralDataBot;
import bots.StudyBot;
import bots.WomNeighboursBot;
import bots.dutchinBot.ManualDutchingBot;
import bots.horseLay3Bot.HorseLay3Bot;
import bots.horseLay3Bot.HorseLay3BotAbove6;
import bots.horseLay3Bot.HorseLayEnd;
import bots.horseLay3Bot.HorseLayFavorite;
import bots.horseLay3Bot.HorseLayOptions;
import demo.util.APIContext;
import demo.util.Display;
import demo.util.InflatedMarketPrices;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.Utils;
import GUI.MarketMainFrame;

public class LoaderHorseLayBots implements MarketChangeListener,MarketProviderListerner{

	
	
	private static EventType selectedEventType;
	private static Market selectedMarket;
	
	LoginEnvironment loginEnv=null;
	
	// ---------user ------------------------
	public String username=null;
	public String password=null;
	
	public MarketData md;
	
	public HorseLay3Bot horseLay3Bot;
	public HorseLay3BotAbove6 horseLay3BotAbove6;
	public HorseLayFavorite horseLayFavorite;
	public HorseLayEnd horseLayEnd;
	
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
		
			
			showLoginInterface();

			loginEnv = new LoginEnvironment();
			
			loginEnv.setUsername(username);
			loginEnv.setPassword(password);
			
			if(loginEnv.login()==-1)
			{
				System.err.println("*** Failed to log in");
				System.exit(1);
			}

			try {
				EventType[] types = GlobalAPI.getActiveEventTypes(loginEnv.getApiContext());
				
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
				selectedEventType = GlobalAPI.getActiveEventTypes(loginEnv.getApiContext())[indexFound];
				System.out.println(selectedEventType.getName());

				selectedMarket = selectMarketNextEvent(selectedEventType);

			} catch (Exception e) {
				// If we can't log in for any reason, just exit.
				Display.showException("*** Failed to getActiveEventTypes", e);
				System.exit(1);
			}
			
			
			md = new MarketData(selectedMarket,loginEnv);
			
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
				MarketNavigator mp=new MarketNavigator(loginEnv);
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
				//horseLay3BotAbove6=new HorseLay3BotAbove6(md,4,6);
				horseLayFavorite=new HorseLayFavorite(md, 3);
			}
		
			// /////////////////////after////////////////////////////////////////////
			md.startPolling();

			
			
		} 
		 else {
				MarketData md = new MarketData(null, null);
				
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
				
				if(Parameters.horselayBots)
				{
//					//horseLay3Bot=new HorseLay3Bot(md,3);
//					
//					HorseLayOptions olo=null;
//					for(int i = 2;i<7;i++)
//					{
//						for(int x= i+1;x<8;x++)
//						{
//							olo=new HorseLayOptions(false, i, x);
//							horseLay3BotAbove6=new HorseLay3BotAbove6(md,olo);
//						}
//					}
					
					Vector<HorseLayOptions> olov=new Vector<HorseLayOptions>();
					
					HorseLayOptions olo0=new HorseLayOptions(false,2,3);
					olo0.setEntryOdd(2);
					olo0.setAboveOdd(3);
//					olo0.setNumberOffRunnersLow(8);
//					olo0.setNumberOffRunnersHigh(14);
//					olo0.setTimeHourLow(17.444444444444446);
//					olo0.setTimeHourHigh(20.111111111111114);
//					olo0.setLenghtInSecondsLow(60.0);
//					olo0.setLenghtInSecondsHigh(392.0);
					olo0.setLenghtInSecondsHigh(212.0);
					
//					olo0.setLiquidityLow(46100.2);
//					olo0.setLiquidityHigh(695574.7422222223);
					
					olov.add(olo0);
					
					HorseLayOptions olo1=new HorseLayOptions(false,2,5);
					olo1.setEntryOdd(2);
					olo1.setAboveOdd(5);
//					olo1.setNumberOffRunnersLow(2);
//					olo1.setNumberOffRunnersHigh(8);
//					olo1.setTimeHourLow(15.666666666666668);
//					olo1.setTimeHourHigh(20.111111111111114);
//					olo1.setLenghtInSecondsLow(60.0);
//					olo1.setLenghtInSecondsHigh(133.77777777777777);
					olo1.setLenghtInSecondsHigh(212.0);
//					olo1.setLiquidityLow(46100.2);
//					olo1.setLiquidityHigh(695574.7422222223);
					
					olov.add(olo1);
					
					HorseLayOptions olo2=new HorseLayOptions(false,3,6);
					olo2.setEntryOdd(3);
					olo2.setAboveOdd(6);
//					olo2.setNumberOffRunnersLow(2);
//					olo2.setNumberOffRunnersHigh(26.0);
//					olo2.setTimeHourLow(13.0);
//					olo2.setTimeHourHigh(20.111111111111114);
//					olo2.setLenghtInSecondsLow(60.0);
//					olo2.setLenghtInSecondsHigh(133.77777777777777);
					olo2.setLenghtInSecondsHigh(212.0);
//					olo2.setLiquidityLow(370837.4711111111);
//					olo2.setLiquidityHigh(1020312.0133333334);
					
					olov.add(olo2);
					
					HorseLayOptions olo3=new HorseLayOptions(false,4,6);
					olo3.setEntryOdd(4);
					olo3.setAboveOdd(6);
//					olo3.setNumberOffRunnersLow(2);
//					olo3.setNumberOffRunnersHigh(11.0);
//					olo3.setTimeHourLow(14.777777777777779);
//					olo3.setTimeHourHigh(20.111111111111114);
//					olo3.setLenghtInSecondsLow(60.0);
//					olo3.setLenghtInSecondsHigh(133.77777777777777);
					olo3.setLenghtInSecondsHigh(212.0);
//					olo3.setLiquidityLow(46100.2);
//					olo3.setLiquidityHigh(695574.7422222223);
					
					olov.add(olo3);
					
//					HorseLayOptions olo4=new HorseLayOptions(false,6,7);
//					olo4.setEntryOdd(6);
//					olo4.setAboveOdd(7);
//					olo4.setNumberOffRunnersLow(2);
//					olo4.setNumberOffRunnersHigh(23);
//					olo4.setTimeHourLow(14.777777777777779);
//					olo4.setTimeHourHigh(20.111111111111114);
//					olo4.setLenghtInSecondsLow(60.0);
//					olo4.setLenghtInSecondsHigh(133.77777777777777);
//					olo4.setLiquidityLow(370837.4711111111);
//					olo4.setLiquidityHigh(2319261.097777778);
//					
//					olov.add(olo4);
					
					for(HorseLayOptions olo:olov)
						new HorseLay3BotAbove6(md,olo);
						
					
					//horseLayFavorite=new HorseLayFavorite(md, 3);
					//horseLayEnd=new HorseLayEnd(md, 3);
				}
	
				// /////////////////////after////////////////////////////////////////////
				md.runFile();
			
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
							LoaderHorseLayBots.this.logout();
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
	
	public void logout() {

		// Logout before shutting down.
		if(loginEnv.logout()!=0)
			System.out.println("Failed to Logout");
		else
			System.out.println("Logout successful");
	}
	

	
	public Market selectMarketNextEvent(EventType  selectedEventTypeA)
	{
		Market ret=null;
		
		GetEventsResp resp=null;
		try {
			resp = GlobalAPI.getEvents(loginEnv.getApiContext(), selectedEventTypeA.getId());
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
			ret=ExchangeAPI.getMarket(loginEnv.getSelectedExchange(), loginEnv.getApiContext(), next.getMarketId());
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
		//System.out.println("MarketState :"+Utils.getMarketSateFrame(md,0)+" Market Live : "+Utils.isInPlayFrame(md,0));
		if(marketEventType==MarketChangeListener.MarketLive)
			//if(!md.isInTrade()) // espera para fechar a operaÁ„o de trading mesmo inPlay ...
			if(Parameters.jump_to_the_next_race)
				MarketLiveMode(md);
		
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			fps.setText("FPS:"+md.getFPS());
			
//			if(Parameters.horselayBots 
//					&& md.getRunners()!=null
//					&& md.getRunners().size()>0
//					&& md.getRunners().get(0).getDataFrames().size()>0
//					&& Utils.getMarketSateFrame(md,0)==MarketData.SUSPENDED && Utils.isInPlayFrame(md,0)==true
//					&& horseLay3Bot!=null 
//					&& horseLay3BotAbove6!=null
//					&& horseLayFavorite!=null
//					&& !horseLay3Bot.isInTrade()
//					&& !horseLay3BotAbove6.isInTrade()
//					&& !horseLayFavorite.isInTrade())
//			{
//				//System
//				MarketLiveMode(md);
//			}
			
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
		RapidMiner.init();
		
		// instanciate model 
		ModelLoader model = OperatorService.createOperator(ModelLoader.class);
		File modelFilePath=new File("horseLayModels/model-3.0-6.0.mod");
		model.setParameter("model_file", modelFilePath.getAbsolutePath());
		
		// instanciate apply model operator
		ModelApplier applier = OperatorService.createOperator(ModelApplier.class);
		// connect model to applier
        model.getOutputPorts().getPortByIndex(0).connectTo(applier.getInputPorts().getPortByIndex(0));
        // create process
        com.rapidminer.Process process = new com.rapidminer.Process();
        process.getRootOperator().getSubprocess(0).addOperator(model);
        process.getRootOperator().getSubprocess(0).addOperator(applier);
		// connect process to input of apply model data set
        process.getRootOperator().getSubprocess(0).getInnerSources().getPortByIndex(0).connectTo( 
	        		applier.getInputPorts().getPortByIndex(1));
        
    	// create attribute for every column
		Vector<Attribute> listOfAttributes= new Vector<Attribute>();
		listOfAttributes.add(AttributeFactory.createAttribute("NRunners",Ontology.INTEGER ));
		listOfAttributes.add(AttributeFactory.createAttribute("Hour",Ontology.INTEGER ));
		listOfAttributes.add(AttributeFactory.createAttribute("Lenght",Ontology.INTEGER ));
		listOfAttributes.add(AttributeFactory.createAttribute("Liquidity",Ontology.REAL ));
		
		MemoryExampleTable table = new MemoryExampleTable(listOfAttributes);
		
		// add data as datarow
		// provide example set to apply model operator
		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
		DataRow testRow = factory.create(new Double []{15.,16.,88.,811962.52}, listOfAttributes.toArray(new Attribute[]{}));
		table.addDataRow(testRow);
		testRow = factory.create(new Double []{10.,16.,248.,235980.29}, listOfAttributes.toArray(new Attribute[]{}));
		table.addDataRow(testRow);
        		
		ExampleSet exampleset= table.createExampleSet();
        
		// execute process with table as input
		process.run(new IOContainer(new IOObject[] { exampleset }));
		
        // check output of applier
		OutputPort portByIndex = applier.getOutputPorts().getPortByIndex(0);
		ExampleSet data = portByIndex.getData(ExampleSet.class);
		
		double value = data.getExample(0).getValue(data.getAttributes().getPredictedLabel());
		double confidence = data.getExample(0).getValue(data.getAttributes().getConfidence("3"));
		
		System.out.println("data RETURN = " +data);
		System.out.println("DOUBLE RETURN = " +value+" - " + confidence);
		
		
		// -------------------- JBet -----------------------
		Parameters.log=false;  // Log or not to Log when not in replay
		Parameters.replay=true; 
		Parameters.replay_file_list=true; 
		Parameters.replay_file_list_test=false; 
		Parameters.jump_to_the_next_race=false; //not go inplay ? 
		Parameters.REALISTIC_TIME_REPLAY=false;
		Parameters.PAUSE_BETWEEN_RACES_REPLAY=false;
		Parameters.saveFavorite=false; 
		Parameters.graphicalInterface=false; 
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
