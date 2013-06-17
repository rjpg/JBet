package bets;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import DataRepository.MarketData;
import DataRepository.OddObj;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class BetPanel extends JPanel{

	public static Double[] stakes={0.01,10.00,1.00,2.00,5.00,7.00,20.00,50.00,100.00,200.00,500.00,1000.00};
	
	public static String[] backLay={"L","B"};
	
	public RunnersData[] runners;
	
	public JComboBox<OddObj> comboOdd;
	public JComboBox<Double> comboStake;
	public JComboBox<String> comboBackLay;
	public JComboBox<RunnersData> comboRunner;
	
	public JCheckBox checkIP;
	

	//public Market market = null;
	public MarketData md =null;
	
	public BetPanel(MarketData MDa) {
		super();
		md=MDa;
		//market=md.getSelectedMarket();
		initialize();
	}
	
	public void initialize()
	{
		
		
		
		comboOdd=new JComboBox<OddObj>(Utils.getLadderOddObj());
		comboOdd.setMaximumRowCount(30);
		
		comboStake=new JComboBox<Double>(stakes);
		
		comboBackLay=new JComboBox<String>(backLay);
		
		initializeRunnersArray();
		comboRunner=new JComboBox<RunnersData>(runners);
		
		checkIP=new JCheckBox("IP",false);
		
		JPanel aux=new JPanel();
		FlowLayout fl=new FlowLayout();
		fl.setHgap(0);
		fl.setVgap(0);
		aux.setLayout(fl);
		
//		fl.setAlignment(FlowLayout.TRAILING);
		this.setLayout(new BorderLayout());
		
		aux.add(checkIP);
		aux.add(comboOdd);
		aux.add(comboStake);
		aux.add(comboBackLay);
		
		this.add(comboRunner,BorderLayout.NORTH);
		this.add(aux,BorderLayout.SOUTH);
		
	}
	
	public void initializeRunnersArray()
	{
		runners=md.getRunners().toArray(new RunnersData[]{});
		//System.out.println("Number of runners="+md.getRunners().size());	
	}
	
	public void reset(MarketData MDa) {
	
		md=MDa;
		//market=md.getSelectedMarket();
		initializeRunnersArray();
		comboRunner.removeAllItems();
		
		for(int i=0;i<runners.length;i++)
		{
			comboRunner.addItem(runners[i]);
		}
	}
	
	public RunnersData getRunner()
	{
		return ((RunnersData)comboRunner.getSelectedItem());
	}
	
	public double getStake()
	{
		return (Double)comboStake.getSelectedItem();
	}

	public double getOdd()
	{
		return ((OddObj)comboOdd.getSelectedItem()).getOdd();
	}
	
	public void setOdd(double odd)
	{
		comboOdd.setSelectedItem(Utils.getOddObjByOdd(odd));
		this.repaint();
	}
	
	public void setRunner(int id)
	{
		for(int i =0;i<runners.length;i++)
			if(id==runners[i].getId())
			{
			//	System.out.println("detectou :" + runners[i].getRunner().getName());
				comboRunner.setSelectedIndex(i);
				this.repaint();
				return;
			}
		//System.out.println("chamou :"+runners.length);
		comboRunner.setSelectedIndex(16);
		this.repaint();
	}
	
	public void setBackLay(String bl)
	{
		if(bl.equals(backLay[0]))
			comboBackLay.setSelectedItem(backLay[0]);
		if(bl.equals(backLay[1]))
			comboBackLay.setSelectedItem(backLay[1]);
		
		this.repaint();
	}
	
	public String getBackLay()
	{
		return (String)comboBackLay.getSelectedItem();
	}
	
	
	public BetData createBetData()
	{
		BetData ret=null;
		if(getBackLay().equals(backLay[0]))
			ret=new BetData(md.getRunnersById(getRunner().getId()),getStake(),getOdd(),BetData.LAY,false);
		else // Is B or L
			ret=new BetData(md.getRunnersById(getRunner().getId()),getStake(),getOdd(),BetData.BACK,false);
		
		if(checkIP.isSelected())
			ret.setKeepInPlay(true);
		
		return ret;
	}
	

}
