package bots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bets.BetData;
import bets.BetPanel;
import bets.BetUtils;

import nextGoal.BetInterface;

import main.Manager;
import main.Parameters;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Scalping;
import DataRepository.Swing;
import correctscore.MessageJFrame;

public class ManualPlaceBetBot extends Bot{

	
	//visuals
	private JPanel actionsPanel=null;
	private JButton placeButton=null;
	private JButton cancelButton=null;
	
	private JButton jumpButton=null;
	
	public Manager manager=null;
	
	public MessageJFrame msgjf=new MessageJFrame("Manual Place Bet Bot");
	
	public BetPanel betPanel=null;
	//---------
	
	
	public BetData bet=null;
	
	public ManualPlaceBetBot(MarketData md, Manager managerA) {
		super(md,"Manual Place Bet Bot");
		manager=managerA;
		initialize();
	}
	
	public void initialize()
	{
		//md.addMarketChangeListener(this);
		
		msgjf.getBaseJpanel().add(getBetPanel(),BorderLayout.NORTH);
		msgjf.getBaseJpanel().add(getActionsPanel(),BorderLayout.EAST);
		msgjf.getBaseJpanel().add(getJumpButton(),BorderLayout.SOUTH);
		msgjf.setVisible(true);
		msgjf.setAlwaysOnTop(true);
	}
	
	public JPanel getActionsPanel()
	{
		if(actionsPanel==null)
		{
			actionsPanel=new JPanel();
			actionsPanel.setLayout(new BorderLayout());
			
			JPanel auxPanel=new JPanel();
			auxPanel.setLayout(new GridLayout(2,1));
			auxPanel.add(getPlaceButton());
			auxPanel.add(getCancelButton());
			actionsPanel.add(auxPanel,BorderLayout.CENTER);
			
		}
		return actionsPanel;
	}
	
	public BetPanel getBetPanel()
	{
		if(betPanel==null)
		{
			betPanel= new BetPanel(getMd());
		}
		
		return betPanel;
	}
	
	
	
	public JButton getPlaceButton()
	{
		if(placeButton==null)
		{
			placeButton=new JButton("Place");
			placeButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					//System.out.println(getMinutesToStart());
					//Statistics.writeStatistics(2, 100323.00, 12, false, true, 10, 4.5, 1);
					
					msgjf.writeMessageText("Place pressed",Color.BLUE);
					bet=betPanel.createBetData();
				}
			});
		}
		return placeButton;
	}
	
	
	public JButton getCancelButton()
	{
		if(cancelButton==null)
		{
			cancelButton=new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					msgjf.writeMessageText("Cancel pressed",Color.BLUE);
				}
			});
		}
		return cancelButton;
	}
	
	public JButton getJumpButton()
	{
		if(jumpButton==null)
		{
			jumpButton=new JButton("Jump to the Next Race");
			jumpButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
				
						msgjf.writeMessageText("Going to the next Race",Color.RED);
						manager.MarketLiveMode(getMd());
				}
			});
		}
		return jumpButton;
	}
	
	
	
	
		
	public MessageJFrame getMsgFrame() {
		return msgjf;
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		
//		
		if( marketEventType==MarketChangeListener.MarketNew)
		{
			setMd(md);
			betPanel.reset(getMd());
		}
		
		if(bet!=null)
			writeMsg(BetUtils.printBet(bet), Color.BLACK);
		
	}

	
	@Override
	public void writeMsg(String s, Color c) {
		if(getMsgFrame()!=null)
			getMsgFrame().writeMessageText(s, c);
		else
			System.out.println(s);
	}

	@Override
	public void tradeResults(RunnersData rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {
		// TODO Auto-generated method stub
		
	}
	
		
}
