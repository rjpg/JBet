package DataRepository;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.exchange.BFExchangeServiceStub.Runner;
import generated.exchange.BFExchangeServiceStub.VolumeInfo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import main.Parameters;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismUtils;
import bets.BetData;
import bets.BetManager;
import bets.BetManagerReal;
import bets.BetManagerSim;
import bots.Bot;
import demo.handler.ExchangeAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.util.APIContext;
import demo.util.InflatedCompleteMarketPrices;
import demo.util.InflatedMarketPrices;
import demo.util.InflatedCompleteMarketPrices.InflatedCompletePrice;
import demo.util.InflatedCompleteMarketPrices.InflatedCompleteRunner;
import demo.util.RunnerTradedVolumeCompressed;

public class MarketData {
	
	//-------- fps ----------
	public int FPS=5;
	public int getFPS() {
		return FPS;
	}

	//-----------Trade -------------
	public Vector<Bot> botsTrading=new Vector<Bot>();
	public Vector<TradeMechanism> TMTrading=new Vector<TradeMechanism>();
	
	public BetManager betManager=null;
	
	//true if it is in tradding process 
	//public boolean inTrade=false;
	
	

	// true if the market is inplay
	public boolean inPlay=false;
	
	public boolean suspended=false;
	public boolean error=false;
	public boolean closed=false;
	
	
	private int fpsAux=0;
	private Calendar calendarFpsAux; 
	//------------------------
	
	//------ demand freq------
	protected int updateInterval = 400;
	//------------------------
	
	
	//------- Market----------
	public String name=null;
	public int id;
	public Calendar start;
	public String eventName=null;
	

	public Calendar currentTime=null;
	//public String exchange;
	
	
	private APIContext apiContext;
	private Market selectedMarket; 
	private Exchange selectedExchange;
	//------------------------

	//----- Internal Data ------
	public Vector<RunnersData> runners=new Vector<RunnersData>();
	public Vector<BetData> bets=new Vector<BetData>();	

	
	public List<MarketChangeListener> listeners= Collections.synchronizedList(new Vector<MarketChangeListener>());;
	//--------------------------
	
	// THREAD
	private MarketThread as;
	private Thread t;
	private boolean polling = false;
	
	//logging
	public boolean logging=false;
	private int frame =0;
	private BufferedWriter out=null;
	private int dayLog=0;
	
	//Volume capture 
	//private int volumeCapCurrentRunner=0;
	
	
	public MarketData(Market selectedMarketA, Exchange selectedExchangeA,APIContext apiContextA)
	{
		
		selectedMarket=selectedMarketA ; 
		selectedExchange=selectedExchangeA;
		apiContext=apiContextA;
		
		calendarFpsAux=Calendar.getInstance();
		
		if(selectedMarket!=null)
		{
			name=selectedMarket.getName();
			id=selectedMarket.getMarketId();
			start=selectedMarket.getMarketTime();
			String slist[]=selectedMarket.getMenuPath().split("\\\\");
			
			eventName=selectedMarket.getMenuPath().split("\\\\")[slist.length-1];
			
			System.out.println("Track:"+selectedMarket.getMenuPath()+" eventName:"+eventName);
			//initializeData();
			
			
			for(int i=0;i<this.selectedMarket.getRunners().getRunner().length;i++)
			{
				
				RunnersData runner=new RunnersData(this.selectedMarket.getRunners().getRunner()[i].getName(), this.selectedMarket.getRunners().getRunner()[i].getSelectionId(), this);
				
				runners.add(runner);
				
			}

			
			initializeBetManager();
			
			
		}
		else
			System.out.println("Warn: selectedMarket == null - Probably reading form file");
		
		//
	}
	
	public void initializeBetManager()
	{
		if(Parameters.simulation || Parameters.replay)
			betManager=new BetManagerSim(this);
		else
			betManager=new BetManagerReal(this);
		
		if(Parameters.synchronizeBetManagerWithMarketData)
			betManager.setPollingInterval(BetManager.SYNC_MARKET_DATA_UPDATE);
		
		betManager.startPolling();
	}
	
	public void finalizeBetManager()
	{
		if(betManager!=null)
		{
			betManager.stopPolling();
			betManager.clean();
			betManager=null;
		}
	}
	
	public void addRunner(String name, int id, Calendar timestamp, double roddLay,
	double ramountLay, double roddBack, double ramountBack,/* double rweightmoneyLay, double rweightmoneyBack,*/
	double rmatchedAmount,	double rlastMatchet , Vector<OddData> rlayPrices, Vector<OddData> rbackPrices)
	{
		RunnersData runner=null;
		for (RunnersData rd:runners)
		{
			if(rd.getId()==id)
				runner=rd;
		}
		
		if(runner==null)
		{
			runner=new RunnersData(name, id, this);
			runner.addPricesData(timestamp, roddLay, ramountLay, roddBack, ramountBack,/*rweightmoneyLay, rweightmoneyBack,*/ rmatchedAmount, rlastMatchet, rlayPrices,rbackPrices);
			runners.add(runner);
		}
		else
		{
			runner.addPricesData(timestamp, roddLay, ramountLay, roddBack, ramountBack,/*rweightmoneyLay, rweightmoneyBack,*/ rmatchedAmount, rlastMatchet, rlayPrices,rbackPrices);
		}
	}
	
	private void initializeData()
	{
		System.out.println("Initializing");
		
		InflatedCompleteMarketPrices prices=null;
		while (prices==null)
		{
			try {
				prices = ExchangeAPI.getCompleteMarketPrices(selectedExchange, apiContext, selectedMarket.getMarketId());
			} catch (Exception e) {
				System.out.println("Error Reading InflatedCompleteMarketPrices in initializing");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		name=selectedMarket.getName();
		id=selectedMarket.getMarketId();
		start=selectedMarket.getMarketTime();
		Calendar timestamp=Calendar.getInstance();
		
		for (InflatedCompleteRunner r : prices.getRunners()) {

			String rname = null;
			int rid = 0;

			double roddLay = 0;
			double ramountLay = 0;

			double roddBack = 0;
			double ramountBack = 0;

			double rmatchedAmount = 0;
			double rlastMatchet = 0;

			Vector<OddData> oddsLay = new Vector<OddData>();
			Vector<OddData> oddsBack = new Vector<OddData>();

			Runner marketRunner = null;

			for (Runner mr : selectedMarket.getRunners().getRunner()) {
				if (mr.getSelectionId() == r.getSelectionId()) {
					marketRunner = mr;
					break;
				}
			}

			for (InflatedCompletePrice p : r.getPrices()) {
				if (p.getBackAmountAvailable() != 0.) {
					oddsBack.add(new OddData(p.getPrice(), p.getBackAmountAvailable()));
				} else if (p.getLayAmountAvailable() != 0) {
					oddsLay.add(new OddData(p.getPrice(), p.getLayAmountAvailable()));
				}
			}

			if (oddsLay.size() > 0) {
				ramountLay = oddsLay.get(0).getAmount();
				roddLay = oddsLay.get(0).getOdd();
			}

			if (oddsBack.size() > 0) {
				roddBack = oddsBack.get(oddsBack.size() - 1).getOdd();
				ramountBack = oddsBack.get(oddsBack.size() - 1).getAmount();
			}

			if (marketRunner != null) {
				rname = marketRunner.getName();

				rid = r.getSelectionId();

				rmatchedAmount = r.getTotalAmountMatched();
				rlastMatchet = r.getLastPriceMatched();

				addRunner(rname, rid, timestamp, roddLay, ramountLay,
						roddBack, ramountBack, rmatchedAmount,
						rlastMatchet, oddsLay, oddsBack);
			}
		}
		
		for(RunnersData rd:getRunners())
		{
			VolumeInfo[] volInfo = null;
			
			while ( volInfo == null) {
				try {
					Thread.sleep(200);
					
					volInfo = ExchangeAPI.getMarketTradedVolume(	
							selectedExchange,
							apiContext,
							selectedMarket.getMarketId(),
							rd.getId());
				} catch (Exception e) {
					System.out.println("Error Reading getMarketTradedVolume in initializing - for runner :"+rd.getName());
					e.printStackTrace();
				}	
			}
			HistoryData hd=rd.getDataFrames().get(rd.getDataFrames().size()-1);
			Hashtable<Double, Double> volume=new Hashtable<Double, Double>();
			for (int i = 0; i < volInfo.length; i++) 
			{
				volume.put(volInfo[i].getOdds(),  volInfo[i].getTotalMatchedAmount());	
			}
			System.out.println("Volume initialized for runner :"+rd.getName());
			hd.setVolume(volume);
		}
		
	}
	
	
	public Calendar getCurrentTime() {
		
		if (selectedMarket==null) // reading from file 
			return currentTime;
		else
			return Calendar.getInstance();
		
	}

	private void refresh()
	{
		if(!polling) return;
		/*GetBet a=new GetBet();
		GetBetReq br=new GetBetReq();
		br.setBetId(10);
		a.setRequest(new GetBetReq());
		br=a.getRequest();
		*/
		
		currentTime = Calendar.getInstance();
		long diffaux =currentTime.getTime().getTime()-calendarFpsAux.getTime().getTime();
		if(diffaux>1000)
		{
			//System.out.println(fpsAux);
			FPS=fpsAux;
			
			fpsAux=0;
			calendarFpsAux = currentTime;
		}
		
		InflatedCompleteMarketPrices prices=null;
		try {
			prices = ExchangeAPI.getCompleteMarketPrices(selectedExchange,apiContext, selectedMarket.getMarketId());
		//	prices = ExchangeAPI.getCompleteMarketPrices(selectedExchange, Manager.apiContext, selectedMarket.getMarketId());
		//	MUBet[] bets=ExchangeAPI.getMUBets(selectedExchange, Manager.apiContext, selectedMarket.getMarketId());
		//	bets=ExchangeAPI.getMUBets(selectedExchange, Manager.apiContext, selectedMarket.getMarketId());
		//	System.out.println(prices.getInPlayDelay());
			fpsAux++;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (prices == null ) 
		{
			System.err.println("No frame captured in :getCompleteMarketPrices : Making copy of data from last frame");
			//return;
		}
		
		// Now show the inflated compressed market prices.
		//Display.showMarket(selectedExchange, selectedMarket, prices);
		//name=selectedMarket.getName();
		//id=selectedMarket.getMarketId();
		//start=selectedMarket.getMarketTime();
		//if(selectedMarket.getMarketStatus()==MarketStatusEnum.ACTIVE)
		//	System.err.println("Active");
			
	
		

		
		
		diffaux =start.getTime().getTime()-currentTime.getTime().getTime() ;
		if(diffaux<=0 /*|| prices.getInPlayDelay()>0*/)
		{
			setInPlay(true);
			warnListenersLive();
			//return;
		}
		
		if(prices!=null && prices.getInPlayDelay()>0)
		{
			setInPlay(true);
			warnListenersLive();
		}
		//System.out.println("---------------------------------------------------");
		
		Calendar timestamp=currentTime;
		
		
	
		//prices.
		if (prices != null) {
			for (InflatedCompleteRunner r : prices.getRunners()) {

				String rname = null;
				int rid = 0;

				double roddLay = 0;
				double ramountLay = 0;

				double roddBack = 0;
				double ramountBack = 0;

				double rmatchedAmount = 0;
				double rlastMatchet = 0;

				// double rweightmoneyBack=0;
				// double rweightmoneyLay=0;

				Vector<OddData> oddsLay = new Vector<OddData>();
				Vector<OddData> oddsBack = new Vector<OddData>();

				// ............
				Runner marketRunner = null;

				for (Runner mr : selectedMarket.getRunners().getRunner()) {
					if (mr.getSelectionId() == r.getSelectionId()) {
						marketRunner = mr;
						break;
					}
				}

				// System.out.println("--------------");

				for (InflatedCompletePrice p : r.getPrices()) {
					if (p.getBackAmountAvailable() != 0.) {

						// System.out.println("p.getPrice():"+p.getPrice());
						// System.out.println("p.getBackAmountAvailable():"+p.getBackAmountAvailable());
						oddsBack.add(new OddData(p.getPrice(), p
								.getBackAmountAvailable()));

						/*
						 * p.getBackAmountAvailable();
						 * p.getLayAmountAvailable(); p.getPrice();
						 * p.getTotalBSPBackersStake();
						 * p.getTotalBSPLayLiability();
						 */
						// rweightmoneyBack+= p.getBackAmountAvailable();
					} else if (p.getLayAmountAvailable() != 0) {

						// System.out.println("p.getPrice():"+p.getPrice());
						// System.out.println("p.getLayAmountAvailable():"+p.getLayAmountAvailable());
						oddsLay.add(new OddData(p.getPrice(), p
								.getLayAmountAvailable()));
						// rweightmoneyLay+=p.getLayAmountAvailable();
					}

					// System.out.println("p.getTotalBSPBackersStake():"+p.getTotalBSPBackersStake());
					// System.out.println("p.getTotalBSPLayLiability():"+p.getTotalBSPLayLiability());

				}

				/*
				 * rweightmoneyBack=0; rweightmoneyLay=0;
				 * 
				 * for (int x=0;x<Parameters.WOM_DIST_CENTER;x++) {
				 * if(x<oddsBack.size())
				 * rweightmoneyBack+=oddsBack.get(oddsBack.
				 * size()-1-x).getAmount();;
				 * 
				 * if(x<oddsLay.size())
				 * rweightmoneyLay+=oddsLay.get(x).getAmount(); }
				 */

				if (oddsLay.size() > 0) {
					ramountLay = oddsLay.get(0).getAmount();
					roddLay = oddsLay.get(0).getOdd();
				}

				if (oddsBack.size() > 0) {
					roddBack = oddsBack.get(oddsBack.size() - 1).getOdd();
					ramountBack = oddsBack.get(oddsBack.size() - 1).getAmount();
				}

				// System.out.println("name:"+marketRunner.getName());
				// String bestLay = "";
				/*
				 * if (r.getPrices().size() > 0) {
				 * 
				 * InflatedCompleteMarketPrices p = r.getPrices().get(0);
				 * 
				 * //bestLay = String.format("%,10.2f %s @ %,6.2f",
				 * p.getAmountAvailable(), prices.getCurrency(), p.getPrice());
				 * 
				 * 
				 * //System.out.println("Lay"); for(InflatedPrice
				 * ip:r.getLayPrices()) {
				 * rweightmoneyLay+=ip.getAmountAvailable(); oddsLay.add(new
				 * OddData(ip.getPrice(), ip.getAmountAvailable(),
				 * ip.getDepth()));
				 * //System.out.println("odd"+ip.getPrice()+":"+
				 * ip.getAmountAvailable()); }
				 * 
				 * ramountLay=p.getAmountAvailable(); roddLay=
				 * r.getActualSPPrice();
				 */
				/* } */

				// String bestBack = "";
				/*
				 * if (r.getBackPrices().size() > 0) { InflatedPrice p =
				 * r.getBackPrices().get(0); //r.getBackPrices().size();
				 * 
				 * //System.out.println("Back"); for(InflatedPrice
				 * ip:r.getBackPrices()) {
				 * rweightmoneyBack+=ip.getAmountAvailable(); oddsBack.add(new
				 * OddData(ip.getPrice(), ip.getAmountAvailable(),
				 * ip.getDepth()));
				 * //System.out.println("odd"+ip.getPrice()+":"+
				 * ip.getAmountAvailable()); } //bestBack =
				 * String.format("%,10.2f %s @ %,6.2f", p.getAmountAvailable(),
				 * prices.getCurrency(), p.getPrice());
				 * 
				 * roddBack=p.getPrice();
				 */
				// ramountBack=r.getActualSPPrice();;

				/* } */

				// println(String.format("%20s (%7d): Matched Amount: %,10.2f, Last Matched: %,6.2f, Best Back %s, Best Lay:%s"
				// , marketRunner.getName(), r.getSelectionId(),
				// r.getTotalAmountMatched(), r.getLastPriceMatched(), bestBack,
				// bestLay));

				if (marketRunner != null) {
					rname = marketRunner.getName();

					rid = r.getSelectionId();

					rmatchedAmount = r.getTotalAmountMatched();
					rlastMatchet = r.getLastPriceMatched();
					
					addRunner(rname, rid, timestamp, roddLay, ramountLay,
							roddBack, ramountBack, /*
													 * rweightmoneyLay,
													 * rweightmoneyBack ,
													 */rmatchedAmount,
							rlastMatchet, oddsLay, oddsBack);
				}
			}
		} else {
			System.out.println("prices are Null clone prices info from last frame");
			logWritteErrorFrame();
			for(RunnersData rd:getRunners())
			{
				String rname = rd.getName();
				int rid = rd.getId();

				double roddLay = rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddLay();
				double ramountLay = rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountLay();

				double roddBack = rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
				double ramountBack = rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountBack();

				double rmatchedAmount = rd.getDataFrames().get(rd.getDataFrames().size()-1).getMatchedAmount();
				double rlastMatchet = rd.getDataFrames().get(rd.getDataFrames().size()-1).getLastMatchet();

				// double rweightmoneyBack=0;
				// double rweightmoneyLay=0;

				Vector<OddData> oddsLay = rd.getDataFrames().get(rd.getDataFrames().size()-1).getLayPrices();
				Vector<OddData> oddsBack = rd.getDataFrames().get(rd.getDataFrames().size()-1).getBackPrices();
				
				addRunner(rname, rid, timestamp, roddLay, ramountLay,
						roddBack, ramountBack, /*
												 * rweightmoneyLay,
												 * rweightmoneyBack ,
												 */rmatchedAmount,
						rlastMatchet, oddsLay, oddsBack);
				
				
			
				
			}
			
			
		}
		
		/*System.out.print(getRunners().size());
		if(isInTrade())
		for(Bot b:botsTrading)
		{
			if(b.isInTrade())
			System.out.println("bot in trade:"+b.getName());
		}
			*/
		
		//frame++; //passou para cima sÃ³ conta se hÃ¡ dados
		//System.out.println(frame);

	}
	
	private void cloneVolumeFromPreviousFrame()
	{
		for (RunnersData rd:getRunners())
		{
			if(rd.getDataFrames().size()>2 && rd.getDataFrames().get(rd.getDataFrames().size()-2).getVolume()!=null)
			{
				Hashtable<Double, Double>volume_prev=(Hashtable<Double, Double>) rd.getDataFrames().get(rd.getDataFrames().size()-2).getVolume().clone();
				rd.getDataFrames().get(rd.getDataFrames().size()-1).setVolume(volume_prev);
			}
			else if(rd.getDataFrames().size()>0)
			{
				Hashtable<Double, Double>volume_prev=new Hashtable<Double, Double>();
				rd.getDataFrames().get(rd.getDataFrames().size()-1).setVolume(volume_prev);
			}
				
		}
	}
	/*
	private void refreshCompleteVolume()
	{
		//clone for the last frame
	
	
		if(volumeCapCurrentRunner>=getRunners().size())
			volumeCapCurrentRunner=0;
		RunnersData rd=getRunners().get(volumeCapCurrentRunner);
		
		VolumeInfo[] volInfo = null;
		try {
				volInfo = ExchangeAPI.getMarketTradedVolume(	
					selectedExchange,
					apiContext,
					selectedMarket.getMarketId(),
					rd.getId());
		} catch (Exception e) {
				System.out.println("Error Reading getMarketTradedVolume in refreshCompleteVolume");
				e.printStackTrace();
		}	
		
		if( volInfo != null)
		{
			
			Hashtable<Double, Double> volume=rd.getDataFrames().get(rd.getDataFrames().size()-1).getVolume();
			for (int i = 0; i < volInfo.length; i++) 
			{
				volume.put(volInfo[i].getOdds(),  volInfo[i].getTotalMatchedAmount());	
			}
			//hd.setVolume(volume);
		}
		//System.out.println(rd.getName()+" volume NOW is:"+rd.getDataFrames().get(rd.getDataFrames().size()-1).getVolume());
		volumeCapCurrentRunner++;
	}
	*/
	private void refreshCompressedVolume()
	{
		if(!polling) return;
		Vector<RunnerTradedVolumeCompressed> ret=null;
		try {
			ret=ExchangeAPI.getMarketTradedVolumeCompressed(selectedExchange,
					apiContext, selectedMarket.getMarketId());
		} catch (Exception e) {
			System.out
					.println("Error Reading getMarketTradedVolume in refreshCompleteVolume");
			e.printStackTrace();
		}
		//getRunners()
		if(ret!=null)
		{
			for(RunnerTradedVolumeCompressed rtvc:ret)
			{
				RunnersData rd=getRunnersById(rtvc.getId());
				if(rd!=null)
				{
					Hashtable<Double, Double> volume=rd.getDataFrames().get(rd.getDataFrames().size()-1).getVolume();
					Hashtable<Double, Double> newVolume=rtvc.getVolumeLadder();
					
					Enumeration<Double> e = newVolume.keys();
					while(e.hasMoreElements())
					{
						Double odd=e.nextElement();
						Double vol=newVolume.get(odd);
						volume.put(odd, vol);
					
					}
				}
			}
			
		}
	}
	
	private void logLastFrame()
	{
		if(getRunners().size()<1) return;
		boolean inside_log_time=false;
		Calendar timestamp=Calendar.getInstance();
		long diffaux =start.getTime().getTime()-timestamp.getTime().getTime() ;
		// if logging print header frame
		if (logging)
		{
			
			
			///////////////////////////////////////////////
			Calendar calendar = Calendar.getInstance();
			int min=calendar.get(Calendar.MINUTE);
			calendar.set(Calendar.MINUTE,min+10); //now+10 min.
			long diffaux2 =start.getTime().getTime()-calendar.getTime().getTime() ;
			// see if we are inside the 10min before  race start
			if(diffaux2<0)
			{
				inside_log_time=true;
				
			}
			//System.out.println("depois dos 10 min."+inside_log_time);
			
			if (inside_log_time)
			{
				
				if(diffaux<=0) // do not log live markets
				{
					
					inside_log_time=false;
					System.out.println("já passou a live: "+inside_log_time);
				}
			}
			//System.out.println("antes dos 10 min: "+inside_log_time);
			
			if (inside_log_time)
			{
			
				if(dayLog!=timestamp.get(Calendar.DAY_OF_MONTH) || out==null) // if we are in the next day
				{
					setLogging(false); 
					setLogging(true);
					logWritteHeaderMarket(); // writte market header in new file
				}
				
				try {
					//System.out.println("#"+frame+" "+timestamp.getTimeInMillis()+" "+id);
					out.write("#"+frame+" "+timestamp.getTimeInMillis()+" "+id);
					out.newLine();
					out.flush();
					frame++;
				} catch (IOException e) {
					System.out.println("Error writting to Log File");
					e.printStackTrace();
				}
			}
		}
		
		if (logging) {
			if (inside_log_time) {
				for(RunnersData rd:getRunners())
				{
					String rname = rd.getName();
					int rid = rd.getId();

					double roddLay = rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddLay();
					double ramountLay = rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountLay();

					double roddBack = rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
					double ramountBack = rd.getDataFrames().get(rd.getDataFrames().size()-1).getAmountBack();

					double rmatchedAmount = rd.getDataFrames().get(rd.getDataFrames().size()-1).getMatchedAmount();
					double rlastMatchet = rd.getDataFrames().get(rd.getDataFrames().size()-1).getLastMatchet();

					// double rweightmoneyBack=0;
					// double rweightmoneyLay=0;

					Vector<OddData> oddsLay = rd.getDataFrames().get(rd.getDataFrames().size()-1).getLayPrices();
					Vector<OddData> oddsBack = rd.getDataFrames().get(rd.getDataFrames().size()-1).getBackPrices();
					Hashtable<Double, Double> volume=rd.getDataFrames().get(rd.getDataFrames().size()-1).getVolume();
					
					
					
				try {
					// System.out.println(rid+" "+roddBack+" "+ramountBack+" "+roddLay+" "+ramountLay+" "+rweightmoneyBack+" "+rweightmoneyLay+" "+rmatchedAmount+" "+rlastMatchet+" "+rname);

					String s = rid + " " + roddBack + " "
							+ ramountBack + " " + roddLay + " "
							+ ramountLay + " " + /*
												 * rweightmoneyBack+" "
												 * +
												 * rweightmoneyLay+
												 * " "
												 */+rmatchedAmount
							+ " " + rlastMatchet + " \"" + rname
							+ "\" " + "\\";

					for (OddData od : oddsBack)
					// for(int i=oddsBack.size()-1;i>=0;i--)
					{
						s += od.getOdd() + " ";
						s += od.getAmount() + " ";
						// s+=od.getDepth()+" ";
					}

					s += "/";

					for (OddData od : oddsLay)
					// for(int i=oddsLay.size()-1;i>=0;i--)
					{
						s += od.getOdd() + " ";
						s += od.getAmount() + " ";
						// s+=od.getDepth()+" ";
					}
					
					s+="|";
					
					if(volume!=null)
					{
						Enumeration<Double> e = volume.keys();
						while(e.hasMoreElements())
						{
							Double odd=e.nextElement();
							s+= odd + " ";
							s+= volume.get(odd)+" ";
						} 
					}
					
					out.write(s);

					out.newLine();
					out.flush();
				} catch (IOException e) {
					System.out
							.println("Error wrtting data to log file");
					e.printStackTrace();
				}
			}
			}

		}
	}
	
	private void refreshBets()
	{
		if(Parameters.replay)
		{
			for(BetData bd:bets)
			{
				
			}
			
	//		for (MarketChangeListener mcl:listeners)
	//			mcl.MarketBetUpdated(bd);
		}
		else // connection
		{
			
		}
		
	}
	
	
	
	public Vector<RunnersData> getRunners() {
		return runners;
	}

	public RunnersData getRunnersById(int id) {
		RunnersData ret=null;
		for(RunnersData rd:getRunners())
			if(rd.getId()==id)
				return rd;
		return ret;
	}
	
	public Calendar getStart() {
		return start;
	}

	
	public void addMarketChangeListener(MarketChangeListener mcl)
	{
		listeners.add(mcl);
	}
	
	public void removeMarketChangeListener(MarketChangeListener mcl)
	{
		listeners.remove(mcl);
	}
	
	
	public void warnListenersLive()
	{
		for (MarketChangeListener mcl:listeners.toArray(new MarketChangeListener[0]))
			try {
				mcl.MarketChange(this, MarketChangeListener.MarketLive);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public void warnListenersUpdate()
	{
		
			try {
				for (MarketChangeListener mcl:listeners.toArray(new MarketChangeListener[0]))
					mcl.MarketChange(this, MarketChangeListener.MarketUpdate);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	/*public Market getSelectedMarket() {
		return selectedMarket;
	}*/
	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getEventName() {
		return eventName;
	}
	
	public Market getSelectedMarket() {
		return selectedMarket;
	}

	public Exchange getSelectedExchange() {
		return selectedExchange;
	}
	
	public APIContext getApiContext() {
		return apiContext;
	}


	public void setSelectedMarket(Market selectedMarketA) {
		
		finalizeBetManager();
		clean();
		
		setInPlay(false);
		if(this.selectedMarket==selectedMarketA)
			return;
		
				
		this.selectedMarket = selectedMarketA;
		
		System.out.println("Track:"+selectedMarket.getMenuPath());
		name=selectedMarket.getName();
		id=selectedMarket.getMarketId();
		start=selectedMarket.getMarketTime();
		String[] slist=selectedMarket.getMenuPath().split("\\\\");
		eventName=selectedMarket.getMenuPath().split("\\\\")[slist.length-1];
		System.out.println("Event"+eventName);

		for(int i=0;i<this.selectedMarket.getRunners().getRunner().length;i++)
		{
			
			RunnersData runner=new RunnersData(this.selectedMarket.getRunners().getRunner()[i].getName(), this.selectedMarket.getRunners().getRunner()[i].getSelectionId(), this);
			
			runners.add(runner);
			
		}
		
		frame=0;
		
		for (MarketChangeListener mcl:listeners.toArray(new MarketChangeListener[0]))
		{
			boolean warned=false;
			while (!warned)
			{
				try {
					mcl.MarketChange(this, MarketChangeListener.MarketNew);
					warned=true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// if logging print header market
		//initializeData();
		logWritteHeaderMarket();
		
		initializeBetManager();
		
	}

	public void logWritteHeaderMarket()
	{
		if(logging)
		{
			try 
			{
				//System.out.println("writing to file");
				out.write("*"+id+" "+start.getTimeInMillis()+" \""+name+"\""+" \""+eventName+"\"");
				out.newLine();
				out.flush();
			} catch (IOException e) {
				System.out.println("Error wrtting data to log file");
				e.printStackTrace();
			}
		}
	}
	
	public void logWritteErrorFrame()
	{
		if(getRunners().size()<1) return;
		boolean inside_log_time=false;
		Calendar timestamp=Calendar.getInstance();
		long diffaux =start.getTime().getTime()-timestamp.getTime().getTime() ;
		// if logging print header frame
		if (logging)
		{
			
			
			///////////////////////////////////////////////
			Calendar calendar = Calendar.getInstance();
			int min=calendar.get(Calendar.MINUTE);
			calendar.set(Calendar.MINUTE,min+10); //now+10 min.
			long diffaux2 =start.getTime().getTime()-calendar.getTime().getTime() ;
			// see if we are inside the 10min before  race start
			if(diffaux2<0)
			{
				inside_log_time=true;
				
			}
			//System.out.println("depois dos 10 min."+inside_log_time);
			
			if (inside_log_time)
			{
				
				if(diffaux<=0) // do not log live markets
				{
					
					inside_log_time=false;
					System.out.println("já passou a live: "+inside_log_time);
				}
			}
			//System.out.println("antes dos 10 min: "+inside_log_time);
			
			if (inside_log_time)
			{
				try 
				{
					//System.out.println("writing to file");
					out.write("%"+frame);
					out.newLine();
					out.flush();
				} catch (IOException e) {
					System.out.println("Error wrtting data to log file");
					e.printStackTrace();
				}
			}
		
		}
		
	}
	
	
	public void clean()
	{
		
		stopPolling();
		for(RunnersData r:runners)
		{
			r.clean();
		}
		runners.clear();
		//runners=null;
		
		Runtime r = Runtime.getRuntime();
		r.gc();
	}
	
	
	
	//---------------------------------thread -----
	public class MarketThread extends Object implements Runnable {
		private volatile boolean stopRequested;

		private Thread runThread;

		public void run() {
			runThread = Thread.currentThread();
			stopRequested = false;
			
			while (!stopRequested) {
				try {
					refresh(); /// connect and get the data prices
					
					//refreshCompleteVolume(); // get volumes 
					
					cloneVolumeFromPreviousFrame();
					refreshCompressedVolume();       
					
					logLastFrame(); // log if the flag is on 
					//	refreshBets();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				warnListenersUpdate(); // warn all listeners
				
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
		as = new MarketThread();
		t = new Thread(as);
		t.start();

		polling = true;
		
	}

	public void stopPolling() {
		if (!polling)
			return;
		if(as!=null)
			as.stopRequest();
		polling = false;

	}
	
	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	
	public boolean isLogging() {
		return logging;
	}

	public void setLogging(boolean logging) {
		
		if(this.logging == logging)
			return;
		this.logging = logging;
		if(logging==true)
		{
			if (out!=null) // close last
				try {
					out.close();
				} catch (IOException e1) {
					System.out.println("Closing File Error");
					e1.printStackTrace();
				}
				
			///////////////// new Log File with "[day of monyh].log" name	
			Calendar calendar = Calendar.getInstance();
			dayLog=calendar.get(Calendar.DAY_OF_MONTH);
			String filename=dayLog+".log";
			try 
			{
				out = new BufferedWriter(new FileWriter(filename, true));
				logWritteHeaderMarket();
				//out.write("aString");
				//out.newLine();
				//out.close();
			} 
			catch (IOException e) 
			{
				System.out.println("Error open Log File");
				e.printStackTrace();
			} 
			
		}
		else // false close file
		{
			if (out!=null)
				try {
					out.close();
					out=null;
				} catch (IOException e1) {
					System.out.println("Closing File Error");
					e1.printStackTrace();
				}
		}
		System.out.println("Logging "+logging);
	}
	

	
	//----------------------Market Live  -------------------------------
	public boolean isInPlay() {
		return inPlay;
	}

	public void setInPlay(boolean inPlay) {
		this.inPlay = inPlay;
	}
	
	//------------------------from File --------------------------------
	
	public static File showOpenImageDialog() {

		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File("."));

		JFrame jf=new JFrame();
		int result = jfc.showDialog(jf, "Open File");
		if(result == JFileChooser.CANCEL_OPTION)
			return null;
		
		File ret=jfc.getSelectedFile();
		jf.dispose();
		
		return ret;
	}
	
	
	public boolean pause=true;
	public boolean fw=false;
	
	public boolean advanceOneFrame=false;
	
	public BufferedReader getBufferedReader(File f)
	{
		BufferedReader input=null;
		try {
				input= new BufferedReader(new FileReader(f));
		} catch (Exception e) {
				e.printStackTrace();
		} 
		return input;
	}
	
	public void runFile()
	{
		if(!Parameters.replay_file_list)
		{	
			File f=showOpenImageDialog();
			
			BufferedReader input=null;
			try {
					input= new BufferedReader(new FileReader(f));
			} catch (Exception e) {
					System.err.println("No file selected ");
					System.out.println("Bye Bye");
					System.exit(-1);
			} 
			
			if(input==null)
			{
				System.err.println("Log file ("+f.getAbsolutePath()+") not processed");
				return;
			}
			
			System.out.println("Log file ("+f.getAbsolutePath()+") in processing...");
			runFile(input);
		}
		else
		{
			if(Parameters.replay_file_list_test)
			{
				File ff=new File("logs/file-list-test.txt");
				BufferedReader inputList=getBufferedReader(ff);
				String s;
				try {
					while ((s=inputList.readLine()) != null)
					{
						File f=new File(s);
						BufferedReader input=getBufferedReader(f);
						System.out.println("Log file ("+f.getAbsolutePath()+") in processing...");
						runFile(input);
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("END OF FILE LIST");
				try {
					inputList.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
			else
			{
				File ff=new File("logs/file-list.txt");
				BufferedReader inputList=getBufferedReader(ff);
				String s;
				try {
					while ((s=inputList.readLine()) != null)
					{
						File f=new File(s);
						BufferedReader input=getBufferedReader(f);
						System.out.println("Log file ("+f.getAbsolutePath()+") in processing...");
						runFile(input);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("END OF FILE LIST");
				try {
					inputList.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
				
			}
			
			/*File f=new File("logs/11-2011/1.log");
			BufferedReader input=getBufferedReader(f);
			System.out.println("Log file ("+f.getAbsolutePath()+") in processing...");
			runFile(input);
			
		
			f=new File("logs/12-2011/16.log");
			input=getBufferedReader(f);
			System.out.println("Log file ("+f.getAbsolutePath()+") in processing...");
			runFile(input);
			*/
			
			
		}
	}
	
	public void runFile(BufferedReader input)
	{
		
	
		JFrame control=new JFrame("Play/Pause");
		control.setSize(200, 100);
		JButton play=new JButton("Play/Pause");
		
		play.addActionListener(new ActionListener() {
			
			
			public void actionPerformed(ActionEvent e) {
				pause=!pause;
				System.out.println("pause :"+pause);
			}
		});
		
		JButton forw=new JButton(">>");
		forw.addActionListener(new ActionListener() {
			
			
			public void actionPerformed(ActionEvent e) {
				fw=true;
				
			}
		});
		JButton oneFrame=new JButton(">");
		oneFrame.addActionListener(new ActionListener() {
			
			
			public void actionPerformed(ActionEvent e) {
				
				pause=false;
				advanceOneFrame=true;
				
			}
		});
		
		JButton realTime=new JButton("Real Time");
		realTime.addActionListener(new ActionListener() {
			
			
			public void actionPerformed(ActionEvent e) {
				
				Parameters.REALISTIC_TIME_REPLAY=!Parameters.REALISTIC_TIME_REPLAY;
				System.out.println("Real Time :"+Parameters.REALISTIC_TIME_REPLAY);
				
			}
		});
		control.getContentPane().setLayout(new BorderLayout());
		control.getContentPane().add(oneFrame,BorderLayout.WEST);
		control.getContentPane().add(play,BorderLayout.CENTER);
		control.getContentPane().add(forw,BorderLayout.EAST);
		control.getContentPane().add(realTime,BorderLayout.SOUTH);
		
		control.setVisible(true);
		
		boolean marketNew=false;
		boolean readOneCompleteFrame=false;
		
		String s;
		currentTime = null;
		try 
		{
			//System.out.println("Antes do ciclo ");
			while ((s=input.readLine()) != null)
			{
				while (pause) 
				{
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				if(s.startsWith("*"))
				{
					System.out.println("Race :"+s);
					fw=false;
					pause=Parameters.PAUSE_BETWEEN_RACES_REPLAY; //False sempre a abrir 
					while (pause)
					{
						try {
							Thread.sleep(200);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					
					if(Parameters.PAUSE_BETWEEN_RACES_REPLAY)
					{
						JFrame jf=new JFrame();
					
						JOptionPane.showMessageDialog(jf,
							"Click 'ok' to proceed ",
							"Pause - Before reading next market",
							JOptionPane.INFORMATION_MESSAGE,
							null);
					
						jf.dispose();
					}
					finalizeBetManager();
					this.clean();
					
					
					
					//System.out.println("Playing Market");
					s=s.substring(1);
					
					String[] sarray=s.split(" ");
					
					
					name=sarray[2];//.replace(" ", "");
					id=Integer.parseInt(sarray[0]);
					
					start = Calendar.getInstance();
					start.setTimeInMillis(Long.parseLong(sarray[1]));
					
					//Calendar calendar =Calendar.getInstance();  // star = now +10min.
					//int min=calendar.get(Calendar.MINUTE);
					//calendar.set(Calendar.MINUTE,min+10);
					//start=calendar;
					
					marketNew=true;
					 readOneCompleteFrame=false;
					
					//System.out.println("today day "+start.get(Calendar.DAY_OF_MONTH));
					
					
				}
				else if(s.startsWith("#"))   // update time
				{
					if(!fw)
					{
					if(marketNew && readOneCompleteFrame)
					{
						initializeBetManager();
						
						for (MarketChangeListener mcl:listeners.toArray(new MarketChangeListener[0]))
							mcl.MarketChange(this,MarketChangeListener.MarketNew);
						
						marketNew=false;
					}
					
					if(readOneCompleteFrame)
						warnListenersUpdate();
					
					
					if(Parameters.REALISTIC_TIME_REPLAY)
					{
						try {
							Thread.sleep(300);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					//System.out.println("reading Frame");
					s=s.substring(1);
					
					String[] sarray=s.split(" ");
					currentTime=Calendar.getInstance();
					currentTime.setTimeInMillis(Long.parseLong(sarray[1]));
					
					if(advanceOneFrame)
					{
						pause=true;
						advanceOneFrame=false;
					}
					
					//System.out.println(currentTime.getTime());
					Calendar now = Calendar.getInstance();
					

					long diffaux =now.getTime().getTime()-calendarFpsAux.getTime().getTime();
					if(diffaux>1000)
					{
						//System.out.println(fpsAux);
						FPS=fpsAux;
						
						fpsAux=0;
						calendarFpsAux = now;
					}
					fpsAux++;
					
					readOneCompleteFrame=true;
					}
				}
				else // runners
				{
					if(!fw)
					{
					String rname=null;
					int rid=0;
					
					
					
					double roddLay=0;
					double ramountLay=0;
					
					double roddBack=0;
					double ramountBack=0;
					
					double rmatchedAmount=0;
					double rlastMatchet=0;
					
					//double rweightmoneyBack=0;
					//double rweightmoneyLay=0;
					
					String saux="\\\\";
					//System.out.println(s);
					String[] sarray2=s.split(saux);
					String[] sarray1=sarray2[0].split("\"");
					//System.out.println("array 0:"+sarray1[0]);
					if(sarray1.length<2)
						System.out.println("sarray2[0]:"+sarray2[0]);
					rname=sarray1[1];
					String[] sarray=sarray1[0].split(" ");
					
					rid=Integer.parseInt(sarray[0]);
					roddBack=Double.parseDouble(sarray[1]);
					ramountBack=Double.parseDouble(sarray[2]);
					roddLay=Double.parseDouble(sarray[3]);
					ramountLay=Double.parseDouble(sarray[4]);
					//rweightmoneyBack=Double.parseDouble(sarray[5]);
					//rweightmoneyLay=Double.parseDouble(sarray[6]);
					rmatchedAmount=Double.parseDouble(sarray[5]);
					rlastMatchet=Double.parseDouble(sarray[6]);
					
					String[] odds=sarray2[1].split("/");
					
					String sarray3[]= odds[1].split("\\|");
					
					String volume="";
					if(sarray3.length>1)
						volume=sarray3[1];
					
						
					odds[1]=sarray3[0];
									
					
					Vector<OddData> back=new Vector<OddData>();
					Vector<OddData> lay=new Vector<OddData>();
					
					if(!odds[0].startsWith(" /"))
					{
						String[] sback=odds[0].split(" ");
						for(int i=0;i<sback.length;i+=2)
						{
							
							OddData od=new OddData(Double.parseDouble(sback[i]), Double.parseDouble(sback[i+1])); // corrigir isto com ficheiro
							back.add(od);
						}
					}
					
					if(odds.length>1)
					{
						//System.out.print("slay:"+odds[1]);
						String[] slay=odds[1].split(" ");
						if(slay.length>1)
						{
						for(int i=0;i<slay.length;i+=2)
						{
							//System.out.println("array"+i+":"+slay[i]+" -- "+slay[i+1]+" -- "+slay[i+2]);
							OddData od=new OddData(Double.parseDouble(slay[i]), Double.parseDouble(slay[i+1])); // corrigir isto com ficheiro
							lay.add(od);
						}
						}
					}
					addRunner(rname, rid, currentTime, roddLay,ramountLay, roddBack, ramountBack, /* rweightmoneyLay,rweightmoneyBack ,*/rmatchedAmount, 
							rlastMatchet,lay,back); 
					
					//System.out.println("volume:"+volume);
					String volumeArray[]=volume.split(" ");
					if(volumeArray.length>1)
					{
						RunnersData rd=getRunnersById(rid);
					
						Hashtable<Double, Double> newVolume=new Hashtable<Double, Double>();
					
						for(int i=0;i<volumeArray.length;i+=2)
						{
							double odd = Double.parseDouble(volumeArray[i]);
							double vol = Double.parseDouble(volumeArray[i+1]);
							
							newVolume.put(odd, vol);
						}
						rd.getDataFrames().get(rd.getDataFrames().size()-1).setVolume(newVolume);
					}
					else
					{
						RunnersData rd=getRunnersById(rid);
						Hashtable<Double, Double> newVolume=new Hashtable<Double, Double>();	
						rd.getDataFrames().get(rd.getDataFrames().size()-1).setVolume(newVolume);
					}
					
				
					
				//	System.out.println("added"+ rname);
					
				
					
				}
				}
				
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("END OF FILE");
		try {
			input.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		control.dispose();
		
	}
	
	
	//----------------------Trading ------------------------
	public void addBotTrading(Bot b)
	{
		botsTrading.add(b);
	}
	
	public void removeBotTrading(Bot b)
	{
		botsTrading.remove(b);
	}
	
	public void addTradingMechanismTrading(TradeMechanism tm)
	{
		TMTrading.add(tm);
	}
	
	public void removeTradingMechanismTrading(TradeMechanism tm)
	{
		TMTrading.remove(tm);
	}
	
	public boolean isInTrade() {
		//System.out.println("Called : is in inTrade");
		for(Bot b:botsTrading)
		{
			if(b.isInTrade())
				return true;
		}
		
		for(TradeMechanism tm:TMTrading)
			if(TradeMechanismUtils.isTradeMechanismFinalState(tm.getState()))
				return true;
		
		return false;
		
		
	}
	
	public BetManager getBetManager() {
		return betManager;
	}

	/*public void setInTrade(boolean inTrade) {
		this.inTrade = inTrade;
	}*/
}
	
	


