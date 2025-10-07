package categories.categories2018;

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

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.Utils;
import GUI.MarketMainFrame;
import bfapi.handler.ExchangeAPI;
import bfapi.handler.GlobalAPI;
import categories.categories2013.bots.CollectNNRawDataBot;
import categories.categories2013.bots.CollectSamplesInfo;
import categories.categories2018.bots.CollectNNRawBot2018;
import demo.util.Display;
import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;
import logienvironment.LoginEnvironment;
import main.Parameters;
import marketProviders.MarketProvider;
import marketProviders.MarketProviderListerner;
import marketProviders.marketNavigator.MarketNavigator;

public class LoaderCollectRaw2018 implements MarketChangeListener,MarketProviderListerner{
	
	
	
	private static EventType selectedEventType;
	private static Market selectedMarket;
	

	
	// ---------user ------------------------
	public String username=null;
	public String password=null;
	
	public MarketData md;
	
	
	
	// close (logout Frame)
	JFrame closeFrame;
	JButton close;
	JLabel fps;
	
	//interface
	MarketMainFrame mmf;
	
	public LoaderCollectRaw2018() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// Initialise logging and turn logging off. Change OFF to DEBUG for detailed output.
		Logger rootLog = LogManager.getRootLogger();
		Level lev = Level.toLevel("OFF");
		rootLog.setLevel(lev);
		
	
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
		
		
		CollectNNRawBot2018 cNNRDB = new CollectNNRawBot2018(md);

		md.runFile();

		System.out.println("CollectNNRawBot2018 Runned");
		Vector<CollectSamplesInfo> csiv=cNNRDB.getCollectSamplesInfo();
		for(CollectSamplesInfo csi:csiv)
			System.out.println("Category Id "+csi.getCategoryId()+" samples collected "+csi.getSamplesCollected()+ " last Market Collected Date " + csi.getLastSampleEventDate());
		
	
			
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
							LoaderCollectRaw2018.this.md.stopPolling();
							LoaderCollectRaw2018.this.logout();
							
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
		System.out.println("Logout successful");
	}
	

	

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		// TODO Auto-generated method stub
		
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			fps.setText("FPS:"+md.getFPS());
		}
		
	}
	
	@Override
	public void newMarketSelected(MarketProvider mp, Market m) {
		

	}
	

	@Override
	public void newMarketsSelected(MarketProvider mp, Vector<Market> mv) {
		// TODO Auto-generated method stub
		
	}


	
	
	public static void main(String[] args)  throws Exception {
		Utils.init();
		
			
		Parameters.log=false;  // Log or not to Log when not in replay
		
		Parameters.replay=true; // replay or read from file
		Parameters.simulation=true; // with replay true is redundant
		
		Parameters.replay_file_list=true; // use file list instead of open
		
		//############################## Collect for Validation ->True (generate *Val.csv files -rename on Bot)
		Parameters.replay_file_list_test=true;  // false use logs/file-list.txt (true use logs/file-list-test.txt)
		
		Parameters.REALISTIC_TIME_REPLAY=false;
		Parameters.PAUSE_BETWEEN_RACES_REPLAY=false;
		Parameters.graphicalInterface=false; // replay or read from file
		
		Parameters.graphicalInterfaceBots=false; // replay or read from file
		
		
		
		Parameters.WOM_DIST_CENTER=5;
		Parameters.CHART_FRAMES=512;
		Parameters.ODD_FAVORITE=2.00;
		
		
		Parameters.simulation=true;
		new LoaderCollectRaw2018();
		}


}
