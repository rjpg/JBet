package bots.dutchinBot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MessagePanel;
import TradeMechanisms.dutching.DutchingRunnerOptions;

public class DutchingRunnerOptionsPanel extends JPanel {
	
	RunnersData rd=null;
	JLabel runnerName=new JLabel();
	
	// open
	JCheckBox open=new JCheckBox("Open :",false);
	static Integer[] ticksBestPriceOffset={0,1,2,3,4,5,6,7,8,9,10};
	JComboBox<Integer> comboBestPriceOffset=new JComboBox<Integer>(ticksBestPriceOffset);
	JPanel openPanel=new JPanel();
	
	//close
	public static Integer[] timeHoldForceClose={0,10,15,20,25,30,35,40,45,50};
	JComboBox<Integer> comboTimeHoldForceClose=new JComboBox<Integer>(timeHoldForceClose);
	
	//msg
	MessagePanel msgPanel=new MessagePanel();
	
	JLabel backOdd=new JLabel();
	JLabel layOdd=new JLabel();
	JPanel backLayPanel=new JPanel(); 
	
	JLabel net=new JLabel();
	JLabel netbf=new JLabel();
	
	JLabel stake=new JLabel("359.00 €");
	
	public DutchingRunnerOptionsPanel(RunnersData rdA) {
		rd=rdA;
		
		initialize();
	}
	
	public void initialize()
	{
		
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		
		JPanel aux=new JPanel();
		aux.setLayout(new GridLayout(7,1));
		
		runnerName.setText(getRd().getName());
		
		backLayPanel.setLayout(new BorderLayout());
		backLayPanel.add(layOdd,BorderLayout.WEST);
		backLayPanel.add(backOdd,BorderLayout.EAST);
		
		openPanel.setLayout(new BorderLayout());
		openPanel.add(open,BorderLayout.WEST);
		openPanel.add(comboBestPriceOffset,BorderLayout.EAST);
		
		aux.add(runnerName);
		aux.add(backLayPanel);
		aux.add(openPanel);
		aux.add(comboTimeHoldForceClose);
		aux.add(stake);
		aux.add(net);
		aux.add(netbf);
		
		this.add(aux,BorderLayout.NORTH);
		this.add(msgPanel,BorderLayout.CENTER);
		
		
	}
	
	public RunnersData getRd() {
		return rd;
	}
	
	public boolean isOpen()
	{
		return open.isSelected();
	}
	
	public void setStake(double am)
	{
		stake.setText(""+Utils.convertAmountToBF(am)+" @ "+getWorkingOdd()+" ("+am+")");
	}
	
	public void setNet(double am)
	{
		net.setText("net:"+am);
	}
	
	public void setNetBf(double am)
	{
		netbf.setText("net BF:"+Utils.convertAmountToBF(am));
	}
	
	public void updateOdds()
	{
		layOdd.setText(""+Utils.getOddLayFrame(rd, 0));
		backOdd.setText(""+Utils.getOddBackFrame(rd, 0));
	}

	public double getOpenOdd()
	{
		double ret=0;
		
		ret=Utils.getOddLayFrame(rd, 0);
		
		ret=Utils.indexToOdd(Utils.oddToIndex(ret)+(Integer)(comboBestPriceOffset.getSelectedItem()));
		
		return ret;
	}
	
	public double getWorkingOdd()
	{
		if(isOpen())
			return getOpenOdd();
		else
			return Utils.getOddBackFrame(rd, 0);
	}
	
	
	public DutchingRunnerOptions getDutchingRunnerOptions()
	{
		DutchingRunnerOptions ret=null;
		if(isOpen())
			ret=new DutchingRunnerOptions(rd, getOpenOdd());
		else
			ret=new DutchingRunnerOptions(rd, (Integer)comboTimeHoldForceClose.getSelectedItem());
		
		return ret;
	}
}
