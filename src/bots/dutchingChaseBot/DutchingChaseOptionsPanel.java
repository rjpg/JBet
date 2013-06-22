package bots.dutchingChaseBot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import TradeMechanisms.TradeMechanism;
import TradeMechanisms.dutchingChase.DutchingChaseOptions;
import bets.BetData;

import DataRepository.RunnersData;
import DataRepository.Utils;

public class DutchingChaseOptionsPanel extends JPanel{
	
	//Not optional by the user
	public JLabel runnerLabel=new JLabel("No name"); 
	RunnersData rd;
	
	public JLabel bfoddDataLabel=new JLabel("BF Bet: ");
	public JLabel oddDataLabel=new JLabel("Original Bet: ");
	double odd=1000;
	double stake=2.00;
	
	public JLabel netLabel=new JLabel("Potencial Profit: ");
	double net=0;
	double netBF=0;
	//--------------------------
	
	static Integer[] ticksBestPriceOffset={0,0,1,2,3,4,5,6,7,8,9,10};
	
	public static Integer[] ticksStopLoss={2,1,2,3,4,5,6,7,8,9,10};
	
	public static Integer[] timeBestOffer={60,0,10,15,20,25,30,35,40,45,50};
	
	public static Integer[] timeForceClose={40,0,10,15,20,25,30,35,40,45,50};
	
	public static Integer[] timeDelayStart={-1,0,10,15,20,25,30,35,40,45,50};
	
	public static Integer[] timeDelayIgnoreSL={-1,0,10,15,20,25,30,35,40,45,50};

	public JPanel panelcomboBestPriceOffset=new JPanel();
	public JLabel comboBestPriceOffsetLabel=new JLabel("Entry Offset:");
	JComboBox<Integer> comboBestPriceOffset=new JComboBox<Integer>(ticksBestPriceOffset);
	
	public JPanel panelcomboStopLossTicks=new JPanel();
	public JLabel comboStopLossTicksLabel=new JLabel("Stop Loss Ticks:");
	public JComboBox<Integer> comboStopLossTicks=new JComboBox<Integer>(ticksStopLoss);
	
	public JPanel panelcomboTimeBestOffer=new JPanel();
	public JLabel comboTimeBestOfferLabel=new JLabel("Time Wait Normal:");
	public JComboBox<Integer> comboTimeBestOffer=new JComboBox<Integer>(timeBestOffer);
	
	public JPanel panelcomboTimeForceClose=new JPanel();
	public JLabel comboTimeForceCloseLabel=new JLabel("Time Wait Best Price:");
	public JComboBox<Integer> comboTimeForceClose=new JComboBox<Integer>(timeForceClose);
	
	public JPanel panelcomboTimeDelayStart=new JPanel();
	public JLabel comboTimeDelayStartLabel=new JLabel("Time Wait To Start:");
	public JComboBox<Integer> comboTimeDelayStart=new JComboBox<Integer>(timeDelayStart);
	
	public JPanel panelcomboTimeDelayIgnoreSL=new JPanel();
	public JLabel comboTimeDelayIgnoreSLLabel=new JLabel("Time Ignore Stop Loss:");
	public JComboBox<Integer> comboTimeDelayIgnoreSL=new JComboBox<Integer>(timeDelayIgnoreSL);
	
	public JCheckBox forceCloseOnStopLoss=new JCheckBox("Close on Stop Loss",false);
	
	public JCheckBox useStopProfit=new JCheckBox("Use Stop Profit",false);
	
	public JCheckBox goOnFrontInBetsPrice=new JCheckBox("Go on Front in Best Price",false);
	
	public JCheckBox checkIP=new JCheckBox("Inplay Bets",false);
	
	
	/*int stopLossTicks=1;
	int waitFramesNormal=20;
	int waitFramesBestPrice=10;
	int updateInterval=TradeMechanism.SYNC_MARKET_DATA_UPDATE;
	boolean forceCloseOnStopLoss=true;
	boolean useStopProfitInBestPrice=false;
	boolean goOnfrontInBestPrice=false;
	int startDelay=-1;
	int ignoreStopLossDelay=-1;*/
	
	
	public DutchingChaseOptionsPanel(RunnersData rdA) {
		rd=rdA;
		initialize();
	}
	
	public void initialize()
	{
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		setRd(rd);
		
		setStake(stake);
		
		this.setLayout(new GridLayout(14,1));
		this.add(runnerLabel);
		this.add(bfoddDataLabel);
		this.add(oddDataLabel);
		this.add(netLabel);
		
		panelcomboBestPriceOffset.setLayout(new BorderLayout());
		panelcomboBestPriceOffset.add(comboBestPriceOffsetLabel,BorderLayout.WEST);
		panelcomboBestPriceOffset.add(comboBestPriceOffset,BorderLayout.EAST);
		
		this.add(panelcomboBestPriceOffset);
		
		panelcomboStopLossTicks.setLayout(new BorderLayout());
		panelcomboStopLossTicks.add(comboStopLossTicksLabel,BorderLayout.WEST);
		panelcomboStopLossTicks.add(comboStopLossTicks,BorderLayout.EAST);
		
		this.add(panelcomboStopLossTicks);
		
		panelcomboTimeBestOffer.setLayout(new BorderLayout());
		panelcomboTimeBestOffer.add(comboTimeBestOfferLabel,BorderLayout.WEST);
		panelcomboTimeBestOffer.add(comboTimeBestOffer,BorderLayout.EAST);
		
		this.add(panelcomboTimeBestOffer);
		
		
		panelcomboTimeForceClose.setLayout(new BorderLayout());
		panelcomboTimeForceClose.add(comboTimeForceCloseLabel,BorderLayout.WEST);
		panelcomboTimeForceClose.add(comboTimeForceClose,BorderLayout.EAST);
		
		this.add(panelcomboTimeForceClose);
		
		
		panelcomboTimeDelayStart.setLayout(new BorderLayout());
		panelcomboTimeDelayStart.add(comboTimeDelayStartLabel,BorderLayout.WEST);
		panelcomboTimeDelayStart.add(comboTimeDelayStart,BorderLayout.EAST);
		
		this.add(panelcomboTimeDelayStart);
		
		
		panelcomboTimeDelayIgnoreSL.setLayout(new BorderLayout());
		panelcomboTimeDelayIgnoreSL.add(comboTimeDelayIgnoreSLLabel,BorderLayout.WEST);
		panelcomboTimeDelayIgnoreSL.add(comboTimeDelayIgnoreSL,BorderLayout.EAST);
		
		this.add(panelcomboTimeDelayIgnoreSL);
		
		
		
		this.add(forceCloseOnStopLoss);
		this.add(useStopProfit);
		this.add(goOnFrontInBetsPrice);
		this.add(checkIP);
				
		
	}

	public RunnersData getRd() {
		return rd;
	}

	public void setRd(RunnersData rd) {
		this.rd = rd;
		runnerLabel.setText(rd.getName());
	}
	
	public double getOdd() {
		
		return odd;
	}

	public void updateOdd() {
		double ret=0;
		
		ret=Utils.getOddLayFrame(rd, 0);
		
		ret=Utils.indexToOdd(Utils.oddToIndex(ret)+(Integer)(comboBestPriceOffset.getSelectedItem()));
		
		odd = ret;
		bfoddDataLabel.setText("BF Bet: "+Utils.convertAmountToBF(this.stake)+" @ "+this.odd);
		oddDataLabel.setText("Original Bet: "+this.stake+" @ "+this.odd);
	}

	public double getStake() {
		return stake;
	}

	public void setStake(double stake) {
		this.stake = stake;
		bfoddDataLabel.setText("BF Bet: "+Utils.convertAmountToBF(this.stake)+" @ "+this.odd);
		oddDataLabel.setText("Original Bet: "+this.stake+" @ "+this.odd);
	}
	
	public void setNet(double am)
	{
		net=am;
		netLabel.setText("Potencial Profit: "+netBF+" ("+net+")");
		
	}
	
	public void setNetBf(double am)
	{
		netBF=Utils.convertAmountToBF(am);
		netLabel.setText("Potencial Profit: "+netBF+" ("+net+")");
	}
	
	public DutchingChaseOptions getDutchingChaseOptions() {
		
		BetData bd=new BetData(rd, stake, odd, BetData.BACK,checkIP.isSelected());
		DutchingChaseOptions dco=new DutchingChaseOptions(bd);
		
		dco.setStopLossTicks((Integer) comboStopLossTicks.getSelectedItem());
		
		dco.setWaitFramesNormal((Integer)comboTimeBestOffer.getSelectedItem());
		
		dco.setWaitFramesBestPrice((Integer)comboTimeForceClose.getSelectedItem());
		
		dco.setStartDelay((Integer)comboTimeDelayStart.getSelectedItem());
		
		dco.setIgnoreStopLossDelay((Integer)comboTimeDelayIgnoreSL.getSelectedItem());
		
		dco.setForceCloseOnStopLoss(forceCloseOnStopLoss.isSelected());
		
		dco.setUseStopProfitInBestPrice(useStopProfit.isSelected());
		
		dco.setGoOnfrontInBestPrice(goOnFrontInBetsPrice.isSelected());
		
		
		
		return dco;
	}
	

}
