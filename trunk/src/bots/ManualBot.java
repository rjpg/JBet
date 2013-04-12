package bots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import statistics.Statistics;

import main.Manager;
import main.Parameters;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Scalping;
import DataRepository.Swing;
import DataRepository.SwingFrontLine;
import correctscore.MessageJFrame;

public class ManualBot extends Bot{

	//visuals
	private JPanel actionsPanel=null;
	private JComboBox comboRunners=null;
	private JButton upButton=null;
	private JButton downButton=null;
	private JButton forceCloseButton=null;
	
	private JButton jumpButton=null;
	
	
	private JLabel greenLabel=null;
	private JLabel redLabel=null;
	
	private JLabel greenAmountLabel=null;
	private JLabel redAmountLabel=null;
	
	public Manager manager=null;
	
	public MessageJFrame msgjf=new MessageJFrame("Manual Bot (scalping)");
	
	
	//scalping
	Scalping scalping=null;
	Swing swing=null;
	
	
	
	public ManualBot(MarketData md, Manager managerA) {
		super(md,"Manual Bot");
		manager=managerA;
		initialize();
	}
	
	public void initialize()
	{
		//md.addMarketChangeListener(this);
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
			actionsPanel.add(getComboRunners(),BorderLayout.NORTH);
			JPanel auxPanel=new JPanel();
			auxPanel.setLayout(new GridLayout(3,1));
			auxPanel.add(getUpButton());
			auxPanel.add(getDownButton());
			auxPanel.add(getForceCloseButton());
			actionsPanel.add(auxPanel,BorderLayout.CENTER);
			
			JPanel auxPanel2=new JPanel();
			auxPanel2.setLayout(new GridLayout(2,2));
			auxPanel2.add(getGreenLabel());
			auxPanel2.add(getGreenAmountLabel());
			auxPanel2.add(getRedLabel());
			auxPanel2.add(getRedAmountLabel());
			actionsPanel.add(auxPanel2,BorderLayout.SOUTH);
		}
		return actionsPanel;
	}
	
	public JComboBox getComboRunners()
	{
		if(comboRunners==null)
		{
			comboRunners=new JComboBox();
			for(RunnersData rd:md.getRunners())
			{
				comboRunners.addItem(rd);
			}
			
		}
		return comboRunners;
	}
	
	public JButton getUpButton()
	{
		if(upButton==null)
		{
			upButton=new JButton("UP");
			upButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					//System.out.println(getMinutesToStart());
					//Statistics.writeStatistics(2, 100323.00, 12, false, true, 10, 4.5, 1);
					
					msgjf.writeMessageText("Up pressed for "+((RunnersData)comboRunners.getSelectedItem()).toString(),Color.BLUE);
					if(!isInTrade())
					{
						RunnersData rd=md.getRunnersById(((RunnersData)comboRunners.getSelectedItem()).getId());
						if(rd!=null)
						{
							double oddLay=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
							msgjf.writeMessageText("Odd Lay ("+rd.getName()+"):"+oddLay, Color.BLACK);
							swing=new Swing(md,rd, 2.50, oddLay,100, 50,50, ManualBot.this,1,5,10);
							//scalping=new Scalping(md,rd, 0.20, oddLay, 50,50, ManualBot.this,1);
						}
					}
					else
						msgjf.writeMessageText("Processing Last Trade",Color.RED);
				}
			});
		}
		return upButton;
	}
	
	
	public JButton getDownButton()
	{
		if(downButton==null)
		{
			downButton=new JButton("Down");
			downButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					msgjf.writeMessageText("Down pressed for "+((RunnersData)comboRunners.getSelectedItem()).toString(),Color.BLUE);
					if(!isInTrade())
					{
						RunnersData rd=md.getRunnersById(((RunnersData)comboRunners.getSelectedItem()).getId());
					
						if(rd!=null)
						{
							double oddBak=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddLay();
							msgjf.writeMessageText("Odd back ("+rd.getName()+"):"+oddBak, Color.BLACK);
							swing=new Swing(md,rd, 2.50, oddBak,100, 50,50, ManualBot.this,-1,10,5);
							//scalping=new Scalping(md,rd, 0.20, oddBak, 50,50, ManualBot.this,-1);
						}
					}
					else
						msgjf.writeMessageText("Processing Last Trade",Color.RED);
				}
			});
		}
		return downButton;
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
	
	public JButton getForceCloseButton()
	{
		if(forceCloseButton==null)
		{
			forceCloseButton=new JButton("Force Close");
			forceCloseButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
				
						msgjf.writeMessageText("Forcing Close trade",Color.RED);
						if(swing!=null)
							swing.forceClose();
						
				}
			});
		}
		return forceCloseButton;
	}
	
	
	public JLabel getGreenLabel()
	{
		if (greenLabel==null)
		{
			greenLabel=new javax.swing.JLabel("Greens:0",JLabel.CENTER);
			greenLabel.setForeground(Color.WHITE);
			greenLabel.setBackground(Color.GREEN);
			greenLabel.setOpaque(true);
			greenLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return greenLabel;
	}
	
	public JLabel getGreenAmountLabel()
	{
		if (greenAmountLabel==null)
		{
			greenAmountLabel=new javax.swing.JLabel("(0.00)",JLabel.CENTER);
			greenAmountLabel.setForeground(Color.GREEN);
			//greenAmountLabel.setBackground(Color.GREEN);
			//greenAmountLabel.setOpaque(true);
			greenAmountLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return greenAmountLabel;
	}
	
	public JLabel getRedLabel()
	{
		if (redLabel==null)
		{
			redLabel=new javax.swing.JLabel("Reds:0",JLabel.CENTER);
			redLabel.setForeground(Color.WHITE);
			redLabel.setBackground(Color.RED);
			redLabel.setOpaque(true);
			redLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return redLabel;
	}
	
	public JLabel getRedAmountLabel()
	{
		if (redAmountLabel==null)
		{
			redAmountLabel=new javax.swing.JLabel("(0.00)",JLabel.CENTER);
			redAmountLabel.setForeground(Color.RED);
			//greenAmountLabel.setBackground(Color.GREEN);
			//greenAmountLabel.setOpaque(true);
			redAmountLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return redAmountLabel;
	}
	
	public MessageJFrame getMsgFrame() {
		return msgjf;
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		
		if(comboRunners.getItemCount()!=md.getRunners().size())
		{
			
			comboRunners.removeAllItems();
			for(RunnersData rd:md.getRunners())
			{
				comboRunners.addItem(rd);
			}
		}
		
		if( marketEventType==MarketChangeListener.MarketNew)
		{
			setMd(md);
			comboRunners.removeAllItems();
			for(RunnersData rd:md.getRunners())
			{
				comboRunners.addItem(rd);
			}
			if(Parameters.simulation)
			{
					if(scalping!=null)
					{
						scalping.clean();
					}
					
					if(swing!=null)
					{
						swing.clean();
					}
			}
		}
		
		if(Parameters.simulation)
		{
			if(marketEventType==MarketChangeListener.MarketUpdate)
			{
				if(scalping!=null)
				{
					scalping.updateState();
				}
				if(swing!=null)
				{
					swing.updateState();
				}
			}
		}
		
	}

	@Override
	public void setInTrade(boolean inTrade) {
		super.setInTrade(inTrade);
		if(inTrade==false)
		{
			//System.out.println("scalping is null now");
			swing=null;
			scalping=null;
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
	public void setGreens(int greens) {
		super.setGreens(greens);
		greenLabel.setText("Greens:"+greens);
	}

	@Override
	public void setReds(int reds) {
		super.setReds(reds);
		redLabel.setText("reds:"+reds);
	}
	
	@Override
	public void setAmountGreen(double amountGreenA) {
		super.setAmountGreen(amountGreenA);
		greenAmountLabel.setText("("+getAmountGreen()+")");
	}

	@Override
	public void  setAmountRed(double amountRedA) {
		super.setAmountRed(amountRedA);
		redAmountLabel.setText("("+getAmountRed()+")");
	}

	//@Override
	public void tradeResults(RunnersData rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {

		
		writeMsg("Name:"+rd.getName(), Color.BLACK);
		writeMsg("R/G:"+redOrGreen, Color.BLACK);
		writeMsg("U/D:"+entryUpDown, Color.BLACK);
		writeMsg("Entry Odd:"+entryOdd, Color.BLACK);
		writeMsg("Exit Odd:"+exitOdd, Color.BLACK);
		writeMsg("Entry stake:"+stake, Color.BLACK);
		writeMsg("Exit stake:"+exitStake, Color.BLACK);
		writeMsg("Amount Made:"+amountMade, Color.BLACK);
		writeMsg("Ticks Moved:"+ticksMoved, Color.BLACK);
		
		
	}

	
	
}
