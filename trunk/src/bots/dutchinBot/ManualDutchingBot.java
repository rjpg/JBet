package bots.dutchinBot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bets.BetData;
import bots.Bot;

import main.Parameters;
import correctscore.MessageJFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.RunnerObj;
import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MessagePanel;
import GUI.MyChart2D;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.dutching.Dutching;
import TradeMechanisms.dutching.DutchingRunnerOptions;
import TradeMechanisms.dutching.DutchingUtils;

public class ManualDutchingBot extends Bot implements TradeMechanismListener{

	private Dutching duching=null;
	
	// graphical interface 
	private JFrame frame;
	
	private Vector<DutchingRunnerOptionsPanel> vdrop;
	private JPanel runnersOptionsPanel;
	
	private MessagePanel msgPanel;
	
	private JPanel actionsPanel;
	private static Integer[] timeWaitOpen={0,10,15,20,25,30,35,40,45,50};
	private JComboBox<Integer> comboTimeWaitOpen;
	private JButton startButton;
	private JButton cancelButton;
	private JButton forceClose;
	private JLabel margin;
	private JLabel netPL;
	public static Double[] stakes={1.00,10.00,20.00,30.00,40.00,50.00,70.00,80.00,100.00,200.00,300.00,400.00,500.00,1000.00};
	public JComboBox<Double> comboStake;
		
	
	public ManualDutchingBot(MarketData md) {
		super(md,"BotDutching - ");
		
		initialize();
	}
	
	public void initialize()
	{
		frame=new JFrame("Dutching Bot");
		frame.getContentPane().setLayout(new BorderLayout());
		
		vdrop=new Vector<DutchingRunnerOptionsPanel>();
		runnersOptionsPanel=new JPanel();
		runnersOptionsPanel.setLayout(new GridLayout(1,2));
		
		msgPanel=new MessagePanel();
		
		actionsPanel=new JPanel();
		actionsPanel.setLayout(new GridLayout(1,2));
		
		comboTimeWaitOpen=new JComboBox<Integer>(timeWaitOpen);		
		comboStake=new JComboBox<Double>(stakes);
		startButton=new JButton("Start");
		cancelButton=new JButton("Cancel");
		forceClose=new JButton("FC");
		margin=new JLabel("100");
		netPL=new JLabel("0.00");
		
		
		startButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				msgPanel.writeMessageText("Starting process", Color.BLUE);
				Vector<DutchingRunnerOptions> vdro=new Vector<DutchingRunnerOptions>();
				for(DutchingRunnerOptionsPanel drop:vdrop)
					vdro.add(drop.getDutchingRunnerOptions());
				duching=new Dutching(vdro, (Double)(comboStake.getSelectedItem()), (Integer)(comboTimeWaitOpen.getSelectedItem()));
			}
		});
		
		actionsPanel.add(comboTimeWaitOpen);
		actionsPanel.add(comboStake);
		actionsPanel.add(startButton);
		actionsPanel.add(cancelButton);
		actionsPanel.add(margin);
		actionsPanel.add(netPL);
		
		JPanel auxPanel=new JPanel();
		
		auxPanel.setLayout(new GridLayout(2,1));
		
		auxPanel.add(runnersOptionsPanel);
		auxPanel.add(msgPanel);
		
		frame.getContentPane().add(auxPanel,BorderLayout.CENTER);
		frame.getContentPane().add(actionsPanel,BorderLayout.SOUTH);
		
		frame.setSize(800,400);
		frame.setVisible(true);
		newMarket(getMd());
	}
	
	public void update()
	{
		writeMsg("Minutes to start :"+getMinutesToStart(), Color.BLUE);
		
		Vector<OddData> vod=new Vector<OddData>();
		//if(vod.size()==0) return;
		for(DutchingRunnerOptionsPanel drop:vdrop)
		{
			drop.updateOdds();
			vod.add(new OddData(drop.getWorkingOdd(),0,BetData.BACK,drop.getRd()));
		}
		
		margin.setText(Utils.convertAmountToBF(DutchingUtils.calculateMargin(vod))+"");
		DutchingUtils.calculateAmounts(vod, (Double)(comboStake.getSelectedItem()));
		
		double[] pl=DutchingUtils.calculateNetProfitLoss(vod);
		
		
		
		Vector<OddData> vodbf=new Vector<OddData>();
		for(OddData od:vod)
		{
			vodbf.add(new OddData(od.getOdd(),Utils.convertAmountToBF(od.getAmount())));
		}
		
		double[] plbf=DutchingUtils.calculateNetProfitLoss(vodbf);
		
		for(int i=0;i<vdrop.size();i++)
		{
			vdrop.get(i).setStake(vod.get(i).getAmount());
			vdrop.get(i).setNet(pl[i]);
			vdrop.get(i).setNetBf(plbf[i]);
		}
		
		/*for(DutchingRunnerOptionsPanel drop:vdrop)
		{
			for(OddData od:vod)
			{
				if(od.getRd()==drop.getRd())
					drop.setStake(od.getAmount());
			}
		}
		*/
		netPL.setText(Utils.convertAmountToBF(DutchingUtils.calculateNetProfitLoss(vod)[0])+"");
		
		
	}
		
	public void newMarket(MarketData md)
	{
		System.out.println("*********************************");
		setMd(md);
		runnersOptionsPanel.removeAll();
		runnersOptionsPanel.setLayout(new GridLayout(1,2));
		
		vdrop.removeAllElements();
		
		for(RunnersData rd:getMd().getRunners())
		{
			DutchingRunnerOptionsPanel drop=new DutchingRunnerOptionsPanel(rd);
			vdrop.add(drop);
			runnersOptionsPanel.add(drop);
		}
		
		runnersOptionsPanel.doLayout();
		
		
	}
	
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketNew)
			newMarket(md);
			
		if(marketEventType==MarketChangeListener.MarketUpdate)
			update();			
	}

	@Override
	public void writeMsg(String s, Color c) {
		
		
	}

	@Override
	public void tradeMechanismChangeState(TradeMechanism tm, int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tradeMechanismEnded(TradeMechanism tm, int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {
		// TODO Auto-generated method stub
		
	}

}
