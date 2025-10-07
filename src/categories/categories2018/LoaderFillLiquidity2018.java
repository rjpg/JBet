package categories.categories2018;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
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
import categories.categories2013.bots.CollectSamplesInfo;
import categories.categories2018.bots.CollectNNRawBot2018;
import categories.categories2018.bots.FillLiquidityBot2018;
import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.EventType;
import main.Parameters;
import marketProviders.MarketProvider;
import marketProviders.MarketProviderListerner;

public class LoaderFillLiquidity2018 implements MarketChangeListener,MarketProviderListerner{
	
	
	
		

	
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
	
	public LoaderFillLiquidity2018() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

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
		
		
		FillLiquidityBot2018 flb2018 = new FillLiquidityBot2018(md);

		md.runFile();

		System.out.println("FillLiquidityBot2018 Runned");
		
		
			
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
							LoaderFillLiquidity2018.this.md.stopPolling();
							LoaderFillLiquidity2018.this.logout();
							
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
		Parameters.replay_file_list_test=false;  // false use logs/file-list.txt (true use logs/file-list-test.txt)
		
		Parameters.REALISTIC_TIME_REPLAY=false;
		Parameters.PAUSE_BETWEEN_RACES_REPLAY=false;
		Parameters.graphicalInterface=false; // replay or read from file
		
		Parameters.graphicalInterfaceBots=false; // replay or read from file
		
		
		
		Parameters.WOM_DIST_CENTER=5;
		Parameters.CHART_FRAMES=512;
		Parameters.ODD_FAVORITE=2.00;
		
		
		Parameters.simulation=true;
		new LoaderFillLiquidity2018();
		}


}

