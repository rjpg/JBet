package bots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

import main.Manager;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.RunnersData;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.TradeMechanismUtils;
import TradeMechanisms.close.ClosePosition;
import TradeMechanisms.close.ClosePositionPanel;
import TradeMechanisms.open.OpenPosition;
import TradeMechanisms.swing.Swing;
import TradeMechanisms.swing.SwingOptions;
import TradeMechanisms.swing.SwingPanel;
import bets.BetData;
import bets.BetPanel;
import bets.BetUtils;
import correctscore.MessageJFrame;

public class ManualPlaceBetBot extends Bot implements TradeMechanismListener{

	
	//visuals
	private JPanel actionsPanel=null;
	private JButton placeButton=null;
	private JButton cancelButton=null;
	private JButton closePositionButton=null;
	private JButton openPositionButton=null;
	private JButton swingButton=null;
	
	
	private JButton jumpButton=null;
	
	public Manager manager=null;
	
	public MessageJFrame msgjf=new MessageJFrame("Manual Place Bet Bot");
	
	public BetPanel betPanel=null;
	public BetPanel betPanel2=null;
	
	public ClosePositionPanel closePanel=null;
	public SwingPanel swingPanel=null;
	
	//---------
	
	
	public BetData bet=null;
	public BetData bet2=null;
	
	public Swing swing;
	
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
			auxPanel.setLayout(new GridLayout(5,1));
			auxPanel.add(getPlaceButton());
			auxPanel.add(getCancelButton());
			auxPanel.add(getClosePositionButton());
			auxPanel.add(getOpenPositionButton());
			auxPanel.add(getSwingButton());
			actionsPanel.add(auxPanel,BorderLayout.CENTER);
			
		}
		return actionsPanel;
	}
	
	public JPanel getBetPanel()
	{
		JPanel placeBetsPanel=new JPanel();
		
		placeBetsPanel.setLayout(new GridLayout(4, 1));
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
		
		if(swingPanel==null)
		{
			swingPanel=new SwingPanel(getMd());	
		}
		
		placeBetsPanel.add(swingPanel);
		
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
					//bet2=betPanel2.createBetData();
					
					Vector<BetData> bds=new Vector<BetData>();
					
					bds.add(bet);
					//bds.add(bet2);
					
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
					
					ClosePosition cp=new ClosePosition(ManualPlaceBetBot.this, bd, closePanel.getTicksStopLoss(), closePanel.getTimeBestOffer(), closePanel.getTimeForceClose(),closePanel.isforceCloseOnStopLoss());
					
					//cp.addTradeMechanismListener(ManualPlaceBetBot.this);
					
					msgjf.writeMessageText("Close Position caled",Color.BLUE);
				}
			});
			
		}
		return closePositionButton;
	}
	
	public JButton getOpenPositionButton()
	{
		if(openPositionButton==null)
		{
			openPositionButton=new JButton("Open Position");
			openPositionButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					
					BetData bd=closePanel.createBetData();
					getMd().getBetManager().placeBet(bd);
					
					OpenPosition cp=new OpenPosition(ManualPlaceBetBot.this, bd, closePanel.getTimeBestOffer());
					
					//cp.addTradeMechanismListener(ManualPlaceBetBot.this);
					
					msgjf.writeMessageText("Open Position caled",Color.BLUE);
				}
			});
			
		}
		return openPositionButton;
	}
	
	public JButton getSwingButton()
	{
		if(swingButton==null)
		{
			swingButton=new JButton("Swing");
			swingButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					msgjf.writeMessageText("Swing caled",Color.BLUE);
					
					//swing=new Swing(listenerA, rdA, stakeSizeA, entryOddA, waitFramesOpenA, waitFramesNormalA, waitFramesBestPriceA, directionBLA, ticksProfitA, ticksLossA, forceCloseOnStopLossA, ipKeepA)				
					msgjf.writeMessageText("Market : "+swingPanel.getRunner().getMarketData().getName(),Color.BLACK);
					msgjf.writeMessageText("Runner : "+swingPanel.getRunner().getName(),Color.BLACK);
					msgjf.writeMessageText("Entry Stake : "+swingPanel.getStake(),Color.BLACK);
					msgjf.writeMessageText("Entry Odd : "+swingPanel.getOdd(),Color.BLACK);
					msgjf.writeMessageText("Frames Open : "+swingPanel.getTimeOpen(),Color.BLACK);
					msgjf.writeMessageText("Frames Normal Close : "+swingPanel.getTimeClose(),Color.BLACK);
					msgjf.writeMessageText("Frames Best Price : "+swingPanel.getTimeBestPrice(),Color.BLACK);
					msgjf.writeMessageText("Direction : "+swingPanel.getBackLay(),Color.BLACK);
					msgjf.writeMessageText("Ticks Profit : "+swingPanel.getTicksProfit(),Color.BLACK);
					msgjf.writeMessageText("Ticks Stop Loss : "+swingPanel.getTicksStopLoss(),Color.BLACK);
					msgjf.writeMessageText("Force Close Stop Loss : "+swingPanel.isforceCloseOnStopLoss(),Color.BLACK);
					msgjf.writeMessageText("Use Keeps : "+swingPanel.isKeepIP(),Color.BLACK);
					
					msgjf.writeMessageText("--------- Open Bet ------------ ",Color.BLACK);
					msgjf.writeMessageText(BetUtils.printBet(swingPanel.createBetData()),Color.BLACK);
					
					BetData betOpen=new BetData(swingPanel.getRunner(),
							swingPanel.getStake(),
							swingPanel.getOdd(),
							swingPanel.getBackLayBetData(),
							swingPanel.isKeepIP());
					
					SwingOptions so=new SwingOptions(betOpen, ManualPlaceBetBot.this);
					so.setWaitFramesOpen(swingPanel.getTimeOpen());
					so.setWaitFramesNormal(swingPanel.getTimeClose());
					so.setWaitFramesBestPrice(swingPanel.getTimeBestPrice());
					so.setTicksProfit(swingPanel.getTicksProfit());
					so.setTicksLoss(swingPanel.getTicksStopLoss());
					so.setForceCloseOnStopLoss(swingPanel.isforceCloseOnStopLoss());
					so.setInsistOpen(false);
					so.setGoOnfrontInBestPrice(true);
					so.setUseStopProfifInBestPrice(true);
					so.setPercentageOpen(1.00);
					so.setDelayBetweenOpenClose(10);
					so.setDelayIgnoreStopLossA(10);
					so.setUpdateInterval(TradeMechanism.SYNC_MARKET_DATA_UPDATE);
					
					swing=new Swing(so);
					
				}
			});
			
		}
		return swingButton;
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
			//System.out.println("Market NEW");
			setMd(md);
			betPanel.reset(getMd());
			betPanel2.reset(getMd());
			closePanel.reset(getMd());
			swingPanel.reset(getMd());
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
		System.out.println("Tm state : "+tm.getState());
	}
	
	public void tradeMechanismEnded(TradeMechanism tm, int state) {
		
		System.out.println("Listenr end informed");
		
		if(TradeMechanismUtils.isTradeMechanismFinalState(state))
		{
			System.out.println("Trade mecanism ended in a final state");	
		}
		else
		{
			System.out.println("Trade mecanism did not end in a final state");
		}

		System.out.println("Tm state : "+tm.getState());
		
		if(tm instanceof OpenPosition)
		{
			for(OddData od:((OpenPosition)tm).getMatchedOddDataVector())
				System.out.println("Tm OpenPosition Matched List :"+od);
			
			System.out.println("Tm OpenPosition Matched Info : "+BetUtils.getOpenInfo(((OpenPosition)tm).getMatchedOddDataVector()));
		}
		
		if(tm instanceof ClosePosition)
		{
			
			for(OddData od:((ClosePosition)tm).getMatchedOddDataVector())
				System.out.println("Tm ClosePosition Matched List :"+od);
			
			System.out.println("Tm ClosePosition Matched Info : "+BetUtils.getOpenInfo(((ClosePosition)tm).getMatchedOddDataVector()));
		}
		
		if(tm instanceof Swing)
		{
			
			String[] fields=tm.getStatisticsFields().split(" ");
			String[] values=tm.getStatisticsValues().split(" ");
			           
			String msg="----- Swing Statistics -----\n";
			
			for(int i=0;i<fields.length;i++)
			{
				msg+="["+i+"] "+fields[i]+" : "+values[i]+"\n";
			}
			
			msg+="------------ || ------------";
			msgjf.writeMessageText(msg,Color.BLUE);
			
		}
		
		tm.removeTradeMechanismListener(this);
	}

	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {

		msgjf.writeMessageText(msg,color);
		
	}




	
		
}
