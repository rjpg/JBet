package bets;

import generated.exchange.BFExchangeServiceStub.BetCategoryTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetPersistenceTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.Market;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.Runner;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import DataRepository.MarketData;
import DataRepository.OddObj;
import DataRepository.RunnerObj;
import DataRepository.Utils;

public class BetPanel extends JPanel{

	public static Double[] stakes={10.00,1.00,2.00,5.00,7.00,20.00,50.00,100.00,200.00,500.00,1000.00};
	
	public static String[] backLay={"L","B"};
	
	public RunnerObj[] runners;
	
	public JComboBox<OddObj> comboOdd;
	public JComboBox<Double> comboStake;
	public JComboBox<String> comboBackLay;
	public JComboBox<RunnerObj> comboRunner;
	
	public JCheckBox checkIP;
	

	public Market market = null;
	public MarketData md =null;
	
	public BetPanel(MarketData MDa) {
		super();
		md=MDa;
		market=md.getSelectedMarket();
		initialize();
	}
	
	public void initialize()
	{
		
		
		
		comboOdd=new JComboBox<OddObj>(Utils.getLadderOddObj());
		comboOdd.setMaximumRowCount(30);
		
		comboStake=new JComboBox<Double>(stakes);
		
		comboBackLay=new JComboBox<String>(backLay);
		
		initializeRunnersArray();
		comboRunner=new JComboBox<RunnerObj>(runners);
		
		checkIP=new JCheckBox("IP",false);
		
		this.setLayout(new GridLayout(1, 4));
		
		this.add(comboOdd);
		this.add(comboStake);
		this.add(comboBackLay);
		this.add(comboRunner);
		this.add(checkIP);
		
	}
	
	public void initializeRunnersArray()
	{
		
		runners=new RunnerObj[market.getRunners().getRunner().length];
		
		for(int i=0;i<runners.length;i++)
		{
			runners[i]=new RunnerObj(market.getRunners().getRunner()[i]);
		}
			
	}
	
	public void reset(MarketData MDa) {
	
		md=MDa;
		market=md.getSelectedMarket();
		initializeRunnersArray();
		comboRunner.removeAllItems();
		
		for(int i=0;i<runners.length;i++)
		{
			comboRunner.addItem(runners[i]);
		}
	}
	
	public Runner getRunner()
	{
		return ((RunnerObj)comboRunner.getSelectedItem()).getRunner();
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
			if(id==runners[i].getRunner().getSelectionId())
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
	
	public PlaceBets createPlaceBet()
	{
		PlaceBets bet = new PlaceBets();
		bet.setMarketId(market.getMarketId());
		bet.setSelectionId(getRunner().getSelectionId());
		//bet.setSelectionId(prevSelectionId);
		
		bet.setBetCategoryType(BetCategoryTypeEnum.E);
		
		if(checkIP.isSelected())
			bet.setBetPersistenceType(BetPersistenceTypeEnum.IP);
		else
			bet.setBetPersistenceType(BetPersistenceTypeEnum.NONE);
		
		bet.setBetType(BetTypeEnum.Factory.fromValue(getBackLay()));
		//bet.setBetType(BetTypeEnum.Factory.fromValue("B"));
		
		bet.setPrice(getOdd());
		bet.setSize(getStake());
		//bet.setPrice(1000);
		//bet.setSize(2);
		
		return bet;
	}
	
	public BetData createBetData()
	{
		BetData ret=null;
		if(getBackLay().equals(backLay[0]))
			ret=new BetData(md.getRunnersById(getRunner().getSelectionId()),getStake(),getOdd(),BetData.LAY,false);
		else // Is B or L
			ret=new BetData(md.getRunnersById(getRunner().getSelectionId()),getStake(),getOdd(),BetData.BACK,false);
		
		if(checkIP.isSelected())
			ret.setKeepInPlay(true);
		
		return ret;
	}
	

}
