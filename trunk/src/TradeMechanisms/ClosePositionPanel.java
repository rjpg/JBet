package TradeMechanisms;




import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import bets.BetData;

import DataRepository.MarketData;
import DataRepository.OddObj;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class ClosePositionPanel extends JPanel{

	public static Double[] stakes={10.00,1.00,2.00,5.00,7.00,20.00,50.00,100.00,200.00,500.00,1000.00};
	
	public static Integer[] ticksStopLoss={1,2,3,4,5,6,7,8,9,10};
	
	public static Integer[] timeBestOffer={0,10,15,20,25,30,35,40,45,50};
	
	public static Integer[] timeForceClose={0,10,15,20,25,30,35,40,45,50};
	
	public static String[] backLay={"L","B"};
	
	public RunnersData[] runners;
	
	public JComboBox<OddObj> comboOdd;
	public JComboBox<Double> comboStake;
	public JComboBox<String> comboBackLay;
	public JComboBox<RunnersData> comboRunner;
	
	public JComboBox<Integer> comboStopLossTicks;
	public JComboBox<Integer> comboTimeBestOffer;
	public JComboBox<Integer> comboTimeForceClose;
	
	public JCheckBox checkIP;
	

	//public Market market = null;
	public MarketData md =null;
	
	public ClosePositionPanel(MarketData MDa) {
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
		
		comboStopLossTicks=new JComboBox<Integer>(ticksStopLoss);
		comboTimeBestOffer=new JComboBox<Integer>(timeBestOffer);
		comboTimeForceClose=new JComboBox<Integer>(timeForceClose);
		
		this.setLayout(new GridLayout(1, 7));
		
		this.add(comboOdd);
		this.add(comboStake);
		this.add(comboBackLay);
		this.add(comboRunner);
		this.add(checkIP);
		this.add(comboStopLossTicks);
		this.add(comboTimeBestOffer);
		this.add(comboTimeForceClose);
		
		
		
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
	
	public int getTicksStopLoss()
	{
		return (Integer) comboStopLossTicks.getSelectedItem();
	}
	
	public int getTimeBestOffer()
	{
		return (Integer)comboTimeBestOffer.getSelectedItem();
	}
	
	public int getTimeForceClose()
	{
		return (Integer)comboTimeForceClose.getSelectedItem();
	}
	
	
	
}
