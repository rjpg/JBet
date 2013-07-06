package bots.dutchingChaseBot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MessagePanel;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.dutching.Dutching;
import TradeMechanisms.dutching.DutchingRunnerOptions;
import TradeMechanisms.dutching.DutchingUtils;
import TradeMechanisms.dutchingChase.DutchingChase;
import TradeMechanisms.dutchingChase.DutchingChaseOptions;
import bets.BetData;
import bots.Bot;
import bots.dutchinBot.DutchingRunnerOptionsPanel;

public class ManualDutchingChaseBot extends Bot implements TradeMechanismListener{

	private DutchingChase duchingChase=null;
	
	// graphical interface 
	private JFrame frame;
	
	private Vector<DutchingChaseOptionsPanel> vdcop;
	private JPanel runnersOptionsPanel;
	
	private MessagePanel msgPanel;
	
	private JPanel actionsPanel;
	
	private JButton startButton;
	private JButton cancelButton;
	private JButton forceClose;
	private JLabel margin;
	private JLabel netPL;
	public static Double[] stakes={1.00,10.00,20.00,30.00,40.00,50.00,70.00,80.00,100.00,200.00,300.00,400.00,500.00,1000.00};
	public JComboBox<Double> comboStake;
		
	
	public ManualDutchingChaseBot(MarketData md) {
		super(md,"Bot Dutching Chase - ");
		
		initialize();
	}
	
	public void initialize()
	{
		frame=new JFrame("Dutching Bot");
		frame.getContentPane().setLayout(new BorderLayout());
		
		vdcop=new Vector<DutchingChaseOptionsPanel>();
		runnersOptionsPanel=new JPanel();
		runnersOptionsPanel.setLayout(new GridLayout(1,2));
		
		msgPanel=new MessagePanel();
		
		actionsPanel=new JPanel();
		actionsPanel.setLayout(new GridLayout(1,2));
		
		
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
				Vector<DutchingChaseOptions> vdro=new Vector<DutchingChaseOptions>();
				for(DutchingChaseOptionsPanel drop:vdcop)
					vdro.add(drop.getDutchingChaseOptions());
				duchingChase=new DutchingChase(ManualDutchingChaseBot.this,vdro,(Double)(comboStake.getSelectedItem()),0.001);
			}
		});
		
		forceClose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				writeMsg("Calling DuchingChase.forceClose() ", Color.GREEN);
				duchingChase.forceClose();
			}
		});
		
		actionsPanel.add(comboStake);
		actionsPanel.add(startButton);
		actionsPanel.add(cancelButton);
		actionsPanel.add(forceClose);
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
		//writeMsg("Minutes to start :"+getMinutesToStart(), Color.BLUE);
		
		writeMsg("MarketState :"+Utils.getMarketSateFrame(md,0)+" Market Live :"+Utils.isInPlayFrame(md,0), Color.BLUE);
		
		Vector<OddData> vod=new Vector<OddData>();
		//if(vod.size()==0) return;
		for(DutchingChaseOptionsPanel drop:vdcop)
		{
			drop.updateOdd();
			vod.add(new OddData(drop.getOdd(),0,BetData.BACK,drop.getRd()));
		}
		
		margin.setText(DutchingUtils.calculateMargin(vod)+"");
		DutchingUtils.calculateAmounts(vod, (Double)(comboStake.getSelectedItem()));
		
		double[] pl=DutchingUtils.calculateNetProfitLoss(vod);
		
		
		
		Vector<OddData> vodbf=new Vector<OddData>();
		for(OddData od:vod)
		{
			vodbf.add(new OddData(od.getOdd(),Utils.convertAmountToBF(od.getAmount())));
		}
		
		double[] plbf=DutchingUtils.calculateNetProfitLoss(vodbf);
		
		for(int i=0;i<vdcop.size();i++)
		{
			vdcop.get(i).setStake(vod.get(i).getAmount());
			vdcop.get(i).setNet(pl[i]);
			vdcop.get(i).setNetBf(plbf[i]);
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
		
		vdcop.removeAllElements();
		
		for(RunnersData rd:getMd().getRunners())
		{
			DutchingChaseOptionsPanel drop=new DutchingChaseOptionsPanel(rd);
			vdcop.add(drop);
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
		
		msgPanel.writeMessageText(s, color);
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
		msgPanel.writeMessageText(msg, color);
		
	}
}
