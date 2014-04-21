package bots.machineGun;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Vector;

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
import main.Parameters;
import marketProviders.MarketProvider;
import marketProviders.MarketProviderListerner;
import marketProviders.marketNavigator.MarketNavigator;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import DataRepository.MarketData;
import DataRepository.Utils;
import GUI.MarketMainFrame;
import bfapi.handler.ExchangeAPI;
import bfapi.handler.GlobalAPI;
import demo.util.Display;

public class LoaderManager implements MarketProviderListerner{


	LoginEnvironment loginEnv=null;
	
	// ---------user ------------------------
	public String username=null;
	public String password=null;
	
	public MarketData md=null;
	
	// close (logout Frame)
	JFrame closeFrame;
	JButton close;
	JLabel fps;
	
	BotMouse bm;	
	
	
	public LoaderManager() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

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

			
			
			
			
			
			JFrame close = getCloseFrame();
			close.setVisible(true);
			close.setAlwaysOnTop(true);
			
			
			
			JFrame jf=new JFrame();
			MarketNavigator mp=new MarketNavigator(loginEnv);
			mp.addMarketProviderListener(this);
				
			JScrollPane jsp=new JScrollPane(mp.getPanel());
			jf.add(jsp);
			jf.setSize(400,400);
			jf.setLocation(0,100);
			jf.setVisible(true);
			
			
			bm= new BotMouse();
			// /////////////////////after////////////////////////////////////////////
			

			
			
		} 
		 else {
			 System.out.println("Does not work in replay");
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
						//	LoaderHorseLayBots.this.md.stopPolling();
						//	LoaderHorseLayBots.this.logout();
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
	

	
	

	@Override
	public void newMarketSelected(MarketProvider mp, Market m) {
		
		if(md!=null)
		{
		
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
			md.removeMarketChangeListener(bm);
			
			md.clean();
			System.err.println("Clean");
		/*	try {
				Thread.sleep(2000);
			} catch (Exception e) {
				// e.printStackTrace();
			}*/
			
			md=null;
		}
		
		
		
		System.err.println("Select new Market");
		/*try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// e.printStackTrace();
		}*/
		System.err.println("Instantating new Market");
		md=new MarketData(m, loginEnv);
		
		bm.setMarket(md);
		/*try {
			Thread.sleep(2000);
		} catch (Exception e) {
			// e.printStackTrace();
		}*/
		
		
		/// delay ?? for graphical restart...
		//System.err.println("Start Pooling");
		//md.startPolling();
	
		
	}
	

	@Override
	public void newMarketsSelected(MarketProvider mp, Vector<Market> mv) {
		// TODO Auto-generated method stub
		
	}


	public static void main(String[] args)  throws Exception {
		Utils.init();
		
		
		
		// -------------------- JBet -----------------------
		Parameters.log=false;  // Log or not to Log when not in replay
		Parameters.replay=false; 
		Parameters.replay_file_list=true; 
		Parameters.replay_file_list_test=true; 
		Parameters.jump_to_the_next_race=false; //not go inplay ? 
		Parameters.REALISTIC_TIME_REPLAY=false;
		Parameters.PAUSE_BETWEEN_RACES_REPLAY=false;
		Parameters.saveFavorite=false; 
		Parameters.graphicalInterface=false; 
		Parameters.graphicalInterfaceBots=false; 
		Parameters.amountBot=false;  
		Parameters.manualBot=false; 
		Parameters.manualPlaceBetBot=false; // manual place bet bot for betManager test
		Parameters.studyBot=false; 
		Parameters.neuralBot=false;
		Parameters.neighboursCorrelationBot=false;
		Parameters.neuralDataBot=false; 
		Parameters.horselayBots=false; 
		Parameters.collectHorseLiquidityBot=false;
		
		Parameters.simulation=true;
		//Parameters.matchedStepsSimulation = 1; // in simulation part of matched amount in each call
		Parameters.WOM_DIST_CENTER=5;
		Parameters.CHART_FRAMES=90;
		Parameters.ODD_FAVORITE=2.00;
		int x=Integer.MAX_VALUE;
		System.out.println(x);
		new LoaderManager();
		
		}
}
