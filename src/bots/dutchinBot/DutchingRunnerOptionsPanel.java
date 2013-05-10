package bots.dutchinBot;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MessagePanel;

public class DutchingRunnerOptionsPanel extends JPanel {
	
	RunnersData rd=null;
	JLabel runnerName=new JLabel();
	
	JCheckBox open=new JCheckBox("Open :",false);
	static Integer[] ticksStopLoss={1,2,3,4,5,6,7,8,9,10};
	JComboBox<Integer> comboStopLossTicks=new JComboBox<Integer>(ticksStopLoss);
	JPanel openPanel=new JPanel();
	
	public static Integer[] timeBestOffer={0,10,15,20,25,30,35,40,45,50};
	JComboBox<Integer> comboTimeBestPrice=new JComboBox<Integer>(timeBestOffer);
	
	MessagePanel msgPanel=new MessagePanel();
	
	JLabel backOdd=new JLabel();
	JLabel layOdd=new JLabel();
	JPanel backLayPanel=new JPanel(); 
	
	JLabel stake=new JLabel("359.00 €");
	
	public DutchingRunnerOptionsPanel(RunnersData rdA) {
		rd=rdA;
		
		initialize();
	}
	
	public void initialize()
	{
		
		this.setLayout(new BorderLayout());
		
		
		
		JPanel aux=new JPanel();
		aux.setLayout(new GridLayout(4,1));
		
		runnerName.setText(getRd().getName());
		
		backLayPanel.setLayout(new BorderLayout());
		backLayPanel.add(layOdd,BorderLayout.WEST);
		backLayPanel.add(backOdd,BorderLayout.EAST);
		
		openPanel.setLayout(new BorderLayout());
		openPanel.add(open,BorderLayout.WEST);
		openPanel.add(comboStopLossTicks,BorderLayout.EAST);
		
		
		aux.add(runnerName);
		aux.add(backLayPanel);
		aux.add(openPanel);
		aux.add(comboTimeBestPrice);
		aux.add(stake);
		
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
		stake.setText(""+Utils.convertAmountToBF(am));
	}

}
