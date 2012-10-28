package nextGoal;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import generated.exchange.BFExchangeServiceStub.BetCategoryTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetPersistenceTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.Market;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.Runner;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import DataRepository.OddObj;
import DataRepository.RunnerObj;
import DataRepository.Utils;

public class BetInterface extends JPanel{
	
	public static Double[] stakes={2.00,5.00,7.00,10.00,20.00,50.00,100.00,200.00,500.00,1000.00};
	
	public static String[] backLay={"L","B"};
	
	public static RunnerObj[] runners;
	
	public JComboBox<OddObj> comboOdd;
	public JComboBox<Double> comboStake;
	public JComboBox<String> comboBackLay;
	public JComboBox<RunnerObj> comboRunner;
	
	public JCheckBox checkProcess;
	
	public JCheckBox checkAuto;

	public static Market market = null;
	
	public BetInterface(Market marketA) {
		super();
		market=marketA;
		initialize();
	}
	
	public void initialize()
	{
		
		checkProcess=new JCheckBox("Process",false);
		
		comboOdd=new JComboBox<OddObj>(Utils.getLadderOddObj());
		comboOdd.setMaximumRowCount(30);
		
		comboStake=new JComboBox<Double>(stakes);
		
		comboBackLay=new JComboBox<String>(backLay);
		
		initializeRunnersArray();
		comboRunner=new JComboBox<RunnerObj>(runners);
		
		checkAuto=new JCheckBox("Auto",true);
		
		this.setLayout(new GridLayout(1, 6));
		
		this.add(checkProcess);
		this.add(comboOdd);
		this.add(comboStake);
		this.add(comboBackLay);
		this.add(comboRunner);
		this.add(checkAuto);
	
	}
	
	public void initializeRunnersArray()
	{
		
		runners=new RunnerObj[market.getRunners().getRunner().length];
		
		for(int i=0;i<runners.length;i++)
		{
			runners[i]=new RunnerObj(market.getRunners().getRunner()[i]);
		}
			
	}
	
	public boolean isActive()
	{
		return checkProcess.isSelected();
	}
	
	public boolean isAuto()
	{
		return checkAuto.isSelected();
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
	
	public PlaceBets createBet()
	{
		PlaceBets bet = new PlaceBets();
		bet.setMarketId(market.getMarketId());
		bet.setSelectionId(getRunner().getSelectionId());
		//bet.setSelectionId(prevSelectionId);
		
		bet.setBetCategoryType(BetCategoryTypeEnum.E);
		bet.setBetPersistenceType(BetPersistenceTypeEnum.NONE);
		
		bet.setBetType(BetTypeEnum.Factory.fromValue(getBackLay()));
		//bet.setBetType(BetTypeEnum.Factory.fromValue("B"));
		
		bet.setPrice(getOdd());
		bet.setSize(getStake());
		//bet.setPrice(1000);
		//bet.setSize(2);
		
		return bet;
	}
}
