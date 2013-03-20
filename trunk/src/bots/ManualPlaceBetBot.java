package bots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

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
	public BetPanel betPanel2=null;
	//---------
	
	
	public BetData bet=null;
	public BetData bet2=null;
	
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
	
	public JPanel getBetPanel()
	{
		JPanel placeBetsPanel=new JPanel();
		
		placeBetsPanel.setLayout(new BorderLayout());
		if(betPanel==null)
		{
			betPanel= new BetPanel(getMd());
		}
		
		placeBetsPanel.add(betPanel,BorderLayout.NORTH);
		
		if(betPanel2==null)
		{
			betPanel2= new BetPanel(getMd());
		}
		
		placeBetsPanel.add(betPanel2,BorderLayout.SOUTH);
		return placeBetsPanel;
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
					bet2=betPanel2.createBetData();
					
					Vector<BetData> bds=new Vector<BetData>();
					
					bds.add(bet);
					bds.add(bet2);
					
					getMd().getBetManager().placeBets(bds);
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
					Vector<BetData> bds=new Vector<BetData>();
					if(bet!=null) 
						bds.add(bet);
					
					if(bet2!=null)
						bds.add(bet2);
						
					if(bds.size()>0)
						getMd().getBetManager().cancelBets(bds);
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
			betPanel2.reset(getMd());
		}
		
		if(bet!=null)
		{
			writeMsg(BetUtils.printBet(bet), Color.BLACK);
			if(BetUtils.isBetFinalState(bet.getState()))
				bet=null;
			if(BetUtils.isBetFinalState(bet2.getState()))
				bet2=null;
		}
		
		
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
