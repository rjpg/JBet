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

import TradeMechanisms.ClosePosition;
import TradeMechanisms.ClosePositionPanel;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
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

public class ManualPlaceBetBot extends Bot implements TradeMechanismListener{

	
	//visuals
	private JPanel actionsPanel=null;
	private JButton placeButton=null;
	private JButton cancelButton=null;
	private JButton closePositionButton=null;
	
	private JButton jumpButton=null;
	
	public Manager manager=null;
	
	public MessageJFrame msgjf=new MessageJFrame("Manual Place Bet Bot");
	
	public BetPanel betPanel=null;
	public BetPanel betPanel2=null;
	
	public ClosePositionPanel closePanel=null;
	
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
			auxPanel.setLayout(new GridLayout(3,1));
			auxPanel.add(getPlaceButton());
			auxPanel.add(getCancelButton());
			auxPanel.add(getClosePositionButton());
			actionsPanel.add(auxPanel,BorderLayout.CENTER);
			
		}
		return actionsPanel;
	}
	
	public JPanel getBetPanel()
	{
		JPanel placeBetsPanel=new JPanel();
		
		placeBetsPanel.setLayout(new GridLayout(3, 1));
		if(betPanel==null)
		{
			betPanel= new BetPanel(getMd());
		}
		
		placeBetsPanel.add(betPanel);
		
		if(betPanel2==null)
		{
			betPanel2= new BetPanel(getMd());
		}
		
		placeBetsPanel.add(betPanel2);
		
		if(closePanel==null)
		{
			closePanel=new ClosePositionPanel(getMd());	
		}
		
		placeBetsPanel.add(closePanel);
		
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
					msgjf.writeMessageText("Place End",Color.BLUE);
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
					{
						Vector<BetData> bets=getMd().getBetManager().getBets();
						Vector<BetData> unbets=new Vector<BetData>();
						for(BetData b:bets)
						{
							if(b.getState()== BetData.UNMATCHED || b.getState()== BetData.PARTIAL_MATCHED)
								unbets.add(b);
						}
						int retCancel=getMd().getBetManager().cancelBets(unbets);
						msgjf.writeMessageText("Return From Cancel : "+retCancel,Color.BLUE);
						
					}
					
					msgjf.writeMessageText("Cancel End",Color.BLUE);
				}
			});
		}
		return cancelButton;
	}
	
	public JButton getClosePositionButton()
	{
		if(closePositionButton==null)
		{
			closePositionButton=new JButton("Close Position");
			closePositionButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					
					BetData bd=closePanel.createBetData();
					getMd().getBetManager().placeBet(bd);
					
					ClosePosition cp=new ClosePosition(ManualPlaceBetBot.this, bd, closePanel.getTicksStopLoss(), closePanel.getTimeBestOffer(), closePanel.getTimeForceClose());
					
					msgjf.writeMessageText("Close Position caled",Color.BLUE);
				}
			});
			
		}
		return closePositionButton;
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
			System.out.println("Market NEW");
			setMd(md);
			betPanel.reset(getMd());
			betPanel2.reset(getMd());
			closePanel.reset(getMd());
		}
		else if( marketEventType==MarketChangeListener.MarketUpdate)
		{
		//System.out.println("Market :"+md.getName()+" "+md.getEventName());
		if(bet!=null)
		{
			writeMsg(BetUtils.printBet(bet), Color.BLACK);
			if(BetUtils.isBetFinalState(bet.getState()))
				bet=null;
			
		}
		
		if(bet2!=null)
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
	public void tradeMechanismChangeState(TradeMechanism tm, int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tradeResults(RunnersData rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {
		// TODO Auto-generated method stub
		
	}


	
		
}
