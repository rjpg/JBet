package nextGoal;

import generated.exchange.BFExchangeServiceStub.ArrayOfPlaceBets;
import generated.exchange.BFExchangeServiceStub.BetCategoryTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetPersistenceTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.GetAccountFundsResp;
import generated.exchange.BFExchangeServiceStub.MarketStatusEnum;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.PlaceBetsE;
import generated.exchange.BFExchangeServiceStub.PlaceBetsErrorEnum;
import generated.exchange.BFExchangeServiceStub.PlaceBetsReq;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResp;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResult;
import generated.exchange.BFExchangeServiceStub.Runner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import bets.BetData;
import bfapi.handler.ExchangeAPI;
import bfapi.handler.ExchangeAPI.Exchange;

import main.Manager;
import nextGoal.InterfaceNextGoal.MarketThread;

import demo.util.APIContext;
import demo.util.Display;
import demo.util.InflatedMarketPrices;
import demo.util.InflatedMarketPrices.InflatedPrice;
import demo.util.InflatedMarketPrices.InflatedRunner;

import DataRepository.OddData;
import DataRepository.OddObj;
import DataRepository.Utils;

public class NextGoalPanel extends JPanel {

	public static Double[] stakes={2.00,5.00,7.00,10.00,20.00,30.00,40.00,50.00,100.00,200.00,500.00,1000.000};
	
	public Score prev;
	public Score actual;
	
	 
	// Interface
	public JLabel prevScoreLegendLabel = new JLabel("Prev(Lay):",JLabel.RIGHT);
	public JLabel prevScore = new JLabel("Previous Score:",JLabel.RIGHT);
	public JLabel prevScoreLabel=null;
	public JLabel prevScoreOddLabel = new JLabel("Odd:",JLabel.RIGHT);
	public JComboBox<OddObj> prevScoreComboOdd;
	public JLabel prevScoreStakeLabel = new JLabel("Stake:",JLabel.RIGHT);
	public JComboBox<Double> prevScoreComboStake;
	public JCheckBox prevCheck=new JCheckBox("Process Lay",false);
	public int prevSelectionId=0;
	
	
	public JLabel actualScoreLegendLabel = new JLabel("Actual(Lays):",JLabel.RIGHT);
	public JLabel actualScore = new JLabel("Actual Score:",JLabel.RIGHT);
	public JLabel actualScoreLabel=null;
	public JLabel actualScoreOddLabel = new JLabel("Odd:",JLabel.RIGHT);
	public JComboBox<OddObj> actualScoreComboOdd;
	public JLabel actualScoreStakeLabel = new JLabel("Stake:",JLabel.RIGHT);
	public JComboBox<Double> actualScoreComboStake;
	public JCheckBox actualCheck=new JCheckBox("Process Back",false);
	public int actualSelectionId=0;
	
	
	public JPanel centralPanel;
	public JButton process;
	
	public String title;
	
	public double balance=0.00;
	
	// THREAD
	private MarketThread as;
	private Thread t;
	private boolean polling = false;
	//------ demand freq------
	protected int updateInterval = 200;
	
	InterfaceNextGoal ing=null;
	
	public NextGoalPanel(Score prevA,Score actualA,String titleA, InterfaceNextGoal ingA)
	{
		title=titleA;
		ing=ingA;
		initialize();
		setScores(prevA, actualA);
	}
	
	public void initialize()
	{
		TitledBorder border;
		border = BorderFactory.createTitledBorder(title);
		border.setTitleJustification(TitledBorder.CENTER);
		this.setBorder(border);
		this.setLayout(new BorderLayout());
		this.add(getCentralPanel(),BorderLayout.CENTER);
		this.add(getProcess(),BorderLayout.SOUTH);
	}
	
	public JPanel getCentralPanel()
	{
		if(centralPanel==null)
		{
			centralPanel=new JPanel();
			centralPanel.setLayout(new GridLayout(2,7));
			
			prevScoreLabel=new JLabel("",JLabel.CENTER);
			
			actualScoreLabel=new JLabel("",JLabel.CENTER);
			actualScoreLabel.setBackground(Color.GREEN);
			actualScoreLabel.setOpaque(true);
			
			prevScoreComboOdd=new JComboBox<OddObj>(Utils.getLadderOddObj());
			prevScoreComboOdd.setMaximumRowCount(30);
			actualScoreComboOdd=new JComboBox<OddObj>(Utils.getLadderOddObj());
			actualScoreComboOdd.setMaximumRowCount(30);
			
			prevScoreComboStake=new JComboBox<Double>(stakes);
			actualScoreComboStake=new JComboBox<Double>(stakes);

			centralPanel.add(actualScoreLegendLabel);
			centralPanel.add(actualScoreLabel);
			centralPanel.add(actualScoreOddLabel);
			centralPanel.add(actualScoreComboOdd);
			centralPanel.add(actualScoreStakeLabel);
			centralPanel.add(actualScoreComboStake);
			centralPanel.add(actualCheck);
			
			centralPanel.add(prevScoreLegendLabel);
			centralPanel.add(prevScoreLabel);
			centralPanel.add(prevScoreOddLabel);
			centralPanel.add(prevScoreComboOdd);
			centralPanel.add(prevScoreStakeLabel);
			centralPanel.add(prevScoreComboStake);
			centralPanel.add(prevCheck);
			
	
		}
		return centralPanel;
	}
	
	public JButton getProcess()
	{
		if(process==null)
		{
			process=new JButton("process: ");
			process.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("Processing :"+actual);
					ing.stopPolling();
					InterfaceNextGoal.msjf.writeMessageText(" ----------------------------------------------------" , Color.BLUE);
					InterfaceNextGoal.msjf.writeMessageText("if you press yes, when the market becames active, the following will be placed:", Color.BLUE);
					if(actualCheck.isSelected())
					{
						InterfaceNextGoal.msjf.writeMessageText("Bet: "+NextGoalPanel.this.getRunnerNameById(actualSelectionId)+" L "+(Double)actualScoreComboStake.getSelectedItem()+" @ "+((OddObj)actualScoreComboOdd.getSelectedItem()).getOdd() , Color.BLUE);
						
					}
					if(prevCheck.isSelected())
					{
						InterfaceNextGoal.msjf.writeMessageText("Bet: "+NextGoalPanel.this.getRunnerNameById(prevSelectionId)+" L "+(Double)prevScoreComboStake.getSelectedItem()+" @ "+((OddObj)prevScoreComboOdd.getSelectedItem()).getOdd() , Color.BLUE);
					}
					
					
					InterfaceNextGoal.msjf.writeMessageText(" ----------------------------------------------------" , Color.BLUE);
					
					if(JOptionPane.showConfirmDialog(null, "Process Bets for "+actual+" result ?")==0)
					{
						InterfaceNextGoal.msjf.writeMessageText("Starting the Goal process", Color.BLUE);
						refreshAccountFunds();
						
						System.out.println("balance = "+balance);
						
						NextGoalPanel.this.startPolling();
						
					}
					else
					{
						InterfaceNextGoal.msjf.writeMessageText("Cancelling the Goal process", Color.GREEN);
					}
				}
			});
		}
		
		return process;
	}
	
	public void setScores(Score prevA,Score actualA)
	{
		

		actualScoreLegendLabel.setEnabled(true);
		actualScoreLabel.setEnabled(true);
		actualScoreOddLabel.setEnabled(true); 
		actualScoreComboOdd.setEnabled(true);
		actualScoreStakeLabel.setEnabled(true);
		actualScoreComboStake.setEnabled(true);
		actualCheck.setSelected(false);
		actualCheck.setEnabled(true);

	
		prevScoreLegendLabel.setEnabled(true);
		prevScoreLabel.setEnabled(true);
	    prevScoreOddLabel.setEnabled(true); 
		prevScoreComboOdd.setEnabled(true);
		prevScoreStakeLabel.setEnabled(true);
		prevScoreComboStake.setEnabled(true);
		prevCheck.setSelected(false);
		prevCheck.setEnabled(true);
		
		if(prevA.goalA==-1 || prevA.goalB==-1 || actualA.goalA==-1 || actualA.goalB==-1)
		{
			process.setEnabled(false);
			return;
		}
		
		process.setEnabled(true);
		
		int totalPrev=prevA.goalA+prevA.goalB;
		int totalActual=actualA.goalA+actualA.goalB;
		if(totalPrev+1!=totalActual)
		{
			System.out.println("Some error with the results");
		}
		
		prev=prevA;
		actual=actualA;
		
		
		
		
		prevScoreLabel.setText(""+prev);
		actualScoreLabel.setText(""+actual);
		
		
		prevSelectionId=0;
		actualSelectionId=0;
		
		for (Runner mr : InterfaceNextGoal.correctScoreMarket.getRunners().getRunner()) {
			if (mr.getName().endsWith(prevScoreLabel.getText())) 
			{
				prevSelectionId=mr.getSelectionId();
			}
			
			if (mr.getName().endsWith(actualScoreLabel.getText())) 
			{
				actualSelectionId=mr.getSelectionId();
			}
		}
		
		
		if(actualScoreLabel.getText().equals("Any Uncoted"))
		{
			actualScoreLegendLabel.setEnabled(false);
			actualScoreLabel.setEnabled(false);
			actualScoreOddLabel.setEnabled(false); 
			actualScoreComboOdd.setEnabled(false);
			actualScoreStakeLabel.setEnabled(false);
			actualScoreComboStake.setEnabled(false);
			actualCheck.setSelected(false);
			actualCheck.setEnabled(false);
		}
		
		if(prevScoreLabel.getText().equals("Any Uncoted"))
		{
			prevScoreLegendLabel.setEnabled(false);
			prevScoreLabel.setEnabled(false);
		    prevScoreOddLabel.setEnabled(false); 
			prevScoreComboOdd.setEnabled(false);
			prevScoreStakeLabel.setEnabled(false);
			prevScoreComboStake.setEnabled(false);
			prevCheck.setSelected(false);
			prevCheck.setEnabled(false);
		}
		
	}
	

	public void refreshOdds(InflatedMarketPrices prices) 
	{
		
		if(prices==null)
			return;
		
		for (InflatedRunner r : prices.getRunners()) {
			

			
			
			if (r.getSelectionId()==prevSelectionId) 
			{
				
				if(r.getLayPrices().size()==0)
				{
					InterfaceNextGoal.msjf.writeMessageText("No lay found for previous score("+prevScoreLabel.getText()+")" , Color.ORANGE);
				}
				else
				{
					InflatedPrice p = r.getLayPrices().get(0);
					
					/*
					double prevOddShift = Utils.indexToOdd(Utils.oddToIndex(p.getPrice())+10);
					if(prevOddShift<10)
						prevOddShift=10.0;
					prevScoreComboOdd.setSelectedItem(Utils.getOddObjByOdd(prevOddShift));
					prevScoreComboOdd.repaint();
					
					double otherOddShift = Utils.indexToOdd(Utils.oddToIndex(p.getPrice())+20);
					if(otherOddShift<10)
						otherOddShift=10;
					otherScoreComboOdd.setSelectedItem(Utils.getOddObjByOdd(otherOddShift));
					otherScoreComboOdd.repaint();
					*/
					
					double actualOddShift = Utils.indexToOdd(Utils.oddToIndex(p.getPrice())-4);
					actualScoreComboOdd.setSelectedItem(Utils.getOddObjByOdd(actualOddShift));
					actualScoreComboOdd.repaint();
					
					//simetricScoreComboOdd.setSelectedItem(Utils.getOddObjByOdd(p.getPrice()));
					//simetricScoreComboOdd.repaint();
					
					
					
					
					
					InterfaceNextGoal.msjf.writeMessageText("Lay found for previous score("+prevScoreLabel.getText()+") = "+p.getPrice() , Color.BLUE);
				}
			}
			/*
			if (r.getSelectionId()==simetricSelectionId) 
			{
				
				if(r.getLayPrices().size()==0)
				{
					InterfaceNextGoal.msjf.writeMessageText("No lay found for next score("+actualScoreLabel.getText()+")" , Color.ORANGE);
				}
				else
				{
					InflatedPrice p = r.getLayPrices().get(0);
					nextScoreComboOdd.setSelectedItem(Utils.getOddObjByOdd(p.getPrice()));
					nextScoreComboOdd.repaint();
					InterfaceNextGoal.msjf.writeMessageText("Lay found for next score("+actualScoreLabel.getText()+") = "+p.getPrice() , Color.BLUE);
				}
			}
			*/
		}
	}
	
	public String getRunnerNameById(int id)
	{
		String ret=null;
		
		for(Runner r:InterfaceNextGoal.correctScoreMarket.getRunners().getRunner())
		{
			if(r.getSelectionId()==id)
				ret=r.getName();
		}
		
		return ret;
	}
	
	//---------------------------------thread -----
	public class MarketThread extends Object implements Runnable {
		private volatile boolean stopRequested;

		private Thread runThread;

		public void run() {
			runThread = Thread.currentThread();
			stopRequested = false;
			
			while (!stopRequested) {
				System.out.println("pooling");
				InflatedMarketPrices prices = null;

				try {
						prices = ExchangeAPI.getMarketPrices(
								InterfaceNextGoal.selectedExchange,
								InterfaceNextGoal.apiContext,
								InterfaceNextGoal.correctScoreMarket.getMarketId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (prices.getMarketStatus()
							.equals(MarketStatusEnum._ACTIVE)) {
						InterfaceNextGoal.msjf.writeMessageText("Market Active  - Placing Bets", Color.GREEN);
						
						//Place Bets
						Vector<PlaceBets> bets=new Vector<PlaceBets>();
						if(actualCheck.isSelected())
						{
							double amountSpend=0;
							
							double odd=((OddObj)actualScoreComboOdd.getSelectedItem()).getOdd();
							double stake=(Double)actualScoreComboStake.getSelectedItem();
							
							amountSpend+=stake*(odd-1);
							
							double max=8000.00;
							
							if(max>balance)
								max=balance;
							
							while (odd>=4.0 && amountSpend<max )
							{
								bets.add(createBet(actualSelectionId, odd,stake, "L"));
								amountSpend+=stake*(odd-1);
								odd=Utils.indexToOdd(Utils.oddToIndex(odd)-2);
							}
							
							
							
							
							//(new HelloRunnable())).start()
							//(new Thread(new betPlacer(actualSelectionId,((OddObj)actualScoreComboOdd.getSelectedItem()).getOdd(),(Double)actualScoreComboStake.getSelectedItem(),"B"))).start();
						}
						
						if(prevCheck.isSelected())
						{
							bets.add(createBet(prevSelectionId,((OddObj)prevScoreComboOdd.getSelectedItem()).getOdd(),(Double)prevScoreComboStake.getSelectedItem(),"L"));
							//(new Thread(new betPlacer(prevSelectionId,((OddObj)prevScoreComboOdd.getSelectedItem()).getOdd(),(Double)prevScoreComboStake.getSelectedItem(),"L"))).start();
						}

						
						if(bets.size()>0)
						{
							InterfaceNextGoal.msjf.writeMessageText("Placing Bets", Color.BLUE);
							
							for(PlaceBets pb:bets)
							{
								InterfaceNextGoal.msjf.writeMessageText("Placing bet :"+pb.getSelectionId()+"  "+pb.getSize()+" @ "+pb.getPrice()+"  "+pb.getBetType(), Color.ORANGE);
							}
							
							placeBets(bets);
						}
						else
						{
							InterfaceNextGoal.msjf.writeMessageText("No bets placed - no options seleted", Color.ORANGE);
						}
						stopPolling();	
					}
					else
					{
						InterfaceNextGoal.msjf.writeMessageText("Suspended...", Color.ORANGE);
						
					}
			
				
				try {
					Thread.sleep(updateInterval);
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}

		public void stopRequest() {
			stopRequested = true;

			if (runThread != null) {
				runThread.interrupt();

				// suspend()stop();
			}
		}
	}
	//-----------------------------------------end thread -------------------
	
	public void startPolling() {
		
		
		if (polling)
			return;
		InterfaceNextGoal.msjf.writeMessageText("Starting Market scan...", Color.GREEN);
		as = new MarketThread();
		t = new Thread(as);
		t.start();

		polling = true;
		
	}

	public void stopPolling() {
		if (!polling)
			return;
		
		InterfaceNextGoal.msjf.writeMessageText("Stop Market scan...", Color.GREEN);
		as.stopRequest();
		polling = false;

	}
	
	public PlaceBets createBet(int runnerIdA, double oddA, double stakeA,String LBA)
	{
		PlaceBets bet = new PlaceBets();
		bet.setMarketId(InterfaceNextGoal.correctScoreMarket.getMarketId());
		bet.setSelectionId(runnerIdA);
		//bet.setSelectionId(prevSelectionId);
		
		bet.setBetCategoryType(BetCategoryTypeEnum.E);
		bet.setBetPersistenceType(BetPersistenceTypeEnum.NONE);
		
		bet.setBetType(BetTypeEnum.Factory.fromValue(LBA));
		//bet.setBetType(BetTypeEnum.Factory.fromValue("B"));
		
		bet.setPrice(oddA);
		bet.setSize(stakeA);
		//bet.setPrice(1000);
		//bet.setSize(2);
		
		return bet;
	}
	
	public int refreshAccountFunds() {
		GetAccountFundsResp funds=null;
		try {
			funds = ExchangeAPI.getAccountFunds(InterfaceNextGoal.selectedExchange,
					InterfaceNextGoal.apiContext);
		} catch (Exception e) {
			Display.showException("*** Failed to Refresh Account Funds", e);
			return -1;
		}
		
		balance=funds.getAvailBalance();
		return 0;
		
	}
	
	public void placeBets(Vector<PlaceBets> betsA) {
		System.out.println("placing Bets");
		long id = 0;

		PlaceBets[] bets = betsA.toArray(new PlaceBets[] {});
		PlaceBetsResult[] betResult = null;

		int attempts = 0;
		while (attempts < 3 && betResult == null) {
			InterfaceNextGoal.msjf.writeMessageText(
					"ExchangeAPI.placeBets Attempt :" + attempts,
					Color.BLUE);
			try {

				betResult = ExchangeAPI.placeBets(
						InterfaceNextGoal.selectedExchange,
						InterfaceNextGoal.apiContext, bets);
			} catch (Exception e) {
				InterfaceNextGoal.msjf.writeMessageText(e.getMessage(),
						Color.RED);
				if (e.getMessage().contains(new String("EVENT_SUSPENDED"))) {
					InterfaceNextGoal.msjf
							.writeMessageText(
									"ExchangeAPI.placeBets Returned NULL: Market is supended | Bet in progress",
									Color.BLUE);
					attempts--;
				}
				e.printStackTrace();
				InterfaceNextGoal.msjf.writeMessageText(
						"ExchangeAPI.placeBets Returned NULL: No bets placed:Attempt :"
								+ attempts, Color.RED);
			}
			attempts++;
		}

		if (betResult == null) {
			InterfaceNextGoal.msjf.writeMessageText(
					"ExchangeAPI.placeBets Returned NULL: No bets placed",
					Color.RED);
			return;
		} else {
			for (int x = 0; x < betResult.length; x++) {
				if (betResult[0].getSuccess()) {
					InterfaceNextGoal.msjf.writeMessageText(
							"Bet Id:" + betResult[x].getBetId() + " placed("
									+ bets[x].getSize() + "@"
									+ bets[x].getPrice() + ") : Matched "
									+ betResult[x].getSizeMatched() + "@"
									+ betResult[x].getAveragePriceMatched(),
							Color.GREEN);

					id = betResult[0].getBetId();
				} else {

					InterfaceNextGoal.msjf.writeMessageText(
							"Failed to place bet(" + bets[x].getSize() + "@"
									+ bets[x].getPrice() + "): Problem was: "
									+ betResult[x].getResultCode(), Color.RED);

				}
			}

		}
	}
	
	
	//---------------- Bet placer thread -------
	public class betPlacer extends Object implements Runnable {

		int id;
		double odd;
		double stake;
		String LB=null;
		
		private Thread runThread;
	
		public betPlacer(int idA, double oddA, double stakeA,String LBA)
		{
			id=idA;
			odd=oddA;
			stake=stakeA;
			LB=LBA;
		}
		
	    public void run() {
	    	runThread = Thread.currentThread();
	    		
	    	InterfaceNextGoal.msjf.writeMessageText("Placing Bet: "+NextGoalPanel.this.getRunnerNameById(actualSelectionId)+" "+LB+" "+stake+" @ "+odd , Color.ORANGE);
	    	
	    	PlaceBets bet = new PlaceBets();
			bet.setMarketId(InterfaceNextGoal.correctScoreMarket.getMarketId());
			bet.setSelectionId(actualSelectionId);
			bet.setBetCategoryType(BetCategoryTypeEnum.E);
			bet.setBetPersistenceType(BetPersistenceTypeEnum.NONE);
			bet.setBetType(BetTypeEnum.Factory.fromValue("B"));
			bet.setPrice(1000);
			bet.setSize(2);
			/*try {
				Thread.sleep(8000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			long idbet=0;
			//long idbet=placeBetAux(bet);
			
			InterfaceNextGoal.msjf.writeMessageText("Placed Bet - ID: "+idbet+"   "+NextGoalPanel.this.getRunnerNameById(id)+" "+LB+" "+stake+" @ "+odd , Color.ORANGE);
	  
	    	// System.out.println("Hello from a thread!");
	    }
	    
	    private long placeBetAux(PlaceBets bet)
		{
			long id=0;
			
			PlaceBets[] bets=new PlaceBets[1];
			bets[0]=bet;
			PlaceBetsResult[] betResult=null;
			
			int attempts = 0;
			while (attempts < 3 && betResult == null) {
				InterfaceNextGoal.msjf.writeMessageText("ExchangeAPI.placeBets(Back) Attempt :"+attempts, Color.BLUE);
				try {
					
					betResult=ExchangeAPI.placeBets(InterfaceNextGoal.selectedExchange,  InterfaceNextGoal.apiContext, bets);
				} catch (Exception e) {
					InterfaceNextGoal.msjf.writeMessageText(e.getMessage(), Color.RED);
					if(e.getMessage().contains(new String("EVENT_SUSPENDED")))
					{
						InterfaceNextGoal.msjf.writeMessageText("ExchangeAPI.placeBets Returned NULL: Market is supended | Bet in progress",Color.BLUE);
						attempts--;
					}
					e.printStackTrace();
					InterfaceNextGoal.msjf.writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
				}
				attempts++;
			}
			
			if(betResult==null)
			{
				InterfaceNextGoal.msjf.writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed", Color.RED);
				return -1;
			}
			else
			{
				if(betResult.length!=1)
				{
					InterfaceNextGoal.msjf.writeMessageText("ExchangeAPI.placeBets Returned !=1 lenght: Debug Bet!!", Color.RED);
					return -1;
				}
				
				if (betResult[0].getSuccess()) {
					InterfaceNextGoal.msjf.writeMessageText("Bet Id:" + betResult[0].getBetId()
								+ " placed("+stake+"@"+odd+") : Matched " + betResult[0].getSizeMatched()
								+ "@"
								+ betResult[0].getAveragePriceMatched(),Color.GREEN);
					
					id=betResult[0].getBetId();
				} else{
				
					InterfaceNextGoal.msjf.writeMessageText("Failed to place bet: Problem was: "
								+ betResult[0].getResultCode(),Color.RED);
					return -1;
				}
			}
			return id;
		}
	   /* 
	    public  PlaceBetsResult[] placeBets(Exchange exch, APIContext context, PlaceBets[] bets) throws Exception {
			
			// Create a request object
			PlaceBetsReq request = new PlaceBetsReq();
			request.setHeader(ExchangeAPI.getHeader(context.getToken()));
			
	        // Set the parameters
	        ArrayOfPlaceBets betsArray = new ArrayOfPlaceBets();
	        betsArray.setPlaceBets(bets);
	        request.setBets(betsArray);
	        // Create the message and attach the request to it.
	        PlaceBetsE msg = new PlaceBetsE();
	        msg.setRequest(request);

	        // Send the request to the Betfair Exchange Service.
	        PlaceBetsResp resp =null;
	        //sem.acquire();
	        try {
	        	resp = ExchangeAPI.getStub(exch).placeBets(msg).getResult();
			} catch (Exception e) {
				e.printStackTrace();
			}
	        //sem.release();
	        context.getUsage().addCall("placeBets");
	        
	        if(resp==null)
			{
				throw new IllegalArgumentException("Failed to retrieve data: getStub(exch).placeBets(msg).getResult() return null");
			}
	        
	        // Check the response code, and throw and exception if call failed
	        if (resp.getErrorCode() != PlaceBetsErrorEnum.OK)
	        {
	        	throw new IllegalArgumentException("Failed to retrieve data: "+resp.getErrorCode() + " Minor Error:"+resp.getMinorErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
	        	
	        }

	        // Transfer the response data back to the API context
	        ExchangeAPI.setHeaderDataToContext(context, resp.getHeader());

	        return resp.getBetResults().getPlaceBetsResult();
		}
	    */
	 

	}
}
