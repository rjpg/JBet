package bots.horseLay3Bot;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.JFrame;

import main.Manager;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MessagePanel;
import bets.BetData;
import bets.BetUtils;
import bots.Bot;

public class HorseLayFavorite extends Bot {

	// Visuals
	private JFrame frame;
	private MessagePanel msgPanel;

	public boolean betPlaced = false;

	public boolean win = false;

	public BetData betMatched = null;

	// parameters
	public int martingaleTries = 40;
	public double oddActuation = 3.00;
	public double initialAmount = 3.00;
	//

	public double amount = initialAmount;

	public int misses = 0;

	public HorseLayFavorite(MarketData md, double initStake) {
		super(md, "HorseLayFavorite - ");
	
		amount = initStake;
		initialize();
	}

	public void initialize() {
		
		setInTrade(true);
		
		frame = new JFrame(this.getName());
		frame.setSize(640, 480);

		msgPanel = new MessagePanel();

		frame.setContentPane(msgPanel);

		frame.setVisible(true);

	}

	public void update() {
		// writeMsg("MarketState :"+Utils.getMarketSateFrame(md,0)+" Market Live : "+Utils.isInPlayFrame(md,0)+
		// "  Minutes to start : "+getMinutesToStart(), Color.BLUE);

		if (Utils.isValidWindow(getMd().getRunners().get(0), 0, 0) && Utils.getMarketSateFrame(md, 0) == MarketData.SUSPENDED
				&& Utils.isInPlayFrame(md, 0) == true && isInTrade()) // end
		{

			RunnersData rdLow = getMd().getRunners().get(0);

			for (RunnersData rdAux : getMd().getRunners())
				if (Utils.getOddBackFrame(rdAux, 0) < Utils.getOddBackFrame(
						rdLow, 0))
					rdLow = rdAux;

			writeMsg("The lower runner (winner) found is " + rdLow.getName()
					+ " with the odd : " + Utils.getOddBackFrame(rdLow, 0),
					Color.BLUE);
			if (betMatched!=null)
			{
				if (betMatched.getRd() == rdLow) {
					writeMsg(
							"The mached Lay Bet was on the winner - Martingale for the nest race",
							Color.RED);
					misses++;
					win = false;
	
					if (misses >= martingaleTries) {
						writeMsg("Reset Martingale - More than " + martingaleTries
								+ " consecutives misses", Color.BLUE);
						misses = 0;
						amount = initialAmount;
						writeMsg("Reset amount to :" + amount, Color.BLUE);
					} else {
						writeMsg("Executing Martingale - try number " + misses,
								Color.ORANGE);
						amount = (amount * (oddActuation - 1.00)) + amount;
						writeMsg("Seting amount to :" + amount, Color.ORANGE);
					}
	
				} else {
					writeMsg("The mached Lay Bet was NOT on the winner",
							Color.GREEN);
					writeMsg("Reset Martingale ", Color.BLUE);
					misses = 0;
					amount = initialAmount;
					win = true;
				}
			}
			writeMsg("Going to the next Race (finish)", Color.RED);
			writeStatisticsToFile();
			setInTrade(false);
			// manager.MarketLiveMode(getMd());
		}

		if (!betPlaced) {
			if (getMinutesToStart() == 0) {

				RunnersData rdLow = getMd().getRunners().get(0);

				for (RunnersData rdAux : getMd().getRunners())
					if (Utils.getOddBackFrame(rdAux, 0) < Utils
							.getOddBackFrame(rdLow, 0))
						rdLow = rdAux;

				if(Utils.getOddLayFrame(rdLow, 0)>2.00)
				{
					oddActuation = Utils.getOddLayFrame(rdLow, 0);
	
					writeMsg("Favorite Runner to placeBet : LAY " + rdLow.getName()
							+ " - " + amount + " @ " + oddActuation, Color.BLUE);
	
					BetData bd = new BetData(rdLow, amount, oddActuation,
							BetData.LAY, false);
					getMd().getBetManager().placeBet(bd);
					writeMsg("Bet was Placed", Color.BLUE);
					betMatched=bd;
					writeMsg("Bet :"+BetUtils.printBet(bd), Color.BLUE);
					betPlaced = true;
				}

			}
		}

	}

	public void writeStatisticsToFile() {
		writeMsg("Writing Stat file HorseLayFavorite.txt",Color.RED);
		BufferedWriter out = null;

		try {
			out = new BufferedWriter(new FileWriter("HorseLayFavorite.txt",
					true));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open HorseLayFavorite.txt for writing");
		}
		if (out == null) {
			System.err.println("could not open HorseLayFavorite.txt");
			return;
		}

		String s = "";

		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Calendar c=getMd().getStart();
		//dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timeStart=dateFormat.format(c.getTimeInMillis());
		
		if( betMatched==null)
		{
			s+="0.00 0.00 "+getMd().getRunners().size()+" "+timeStart+" \"NO_Name\" \""+getMd().getEventName()+"\" \""+getMd().getName()+"\""; 
		}
		else
		{
			if (win)
				s += betMatched.getAmount() + " " + oddActuation +" "+getMd().getRunners().size()+" "+timeStart
						+" \""+ betMatched.getRd().getName() + "\" \""
						+ getMd().getEventName() + "\" \"" + getMd().getName()
						+ "\"";
			else
				s += ((betMatched.getAmount() * (betMatched.getOddRequested() - 1)) * -1)
						+ " "
						+ oddActuation+" "+getMd().getRunners().size()+" "+timeStart
						+ " \""
						+ betMatched.getRd().getName()
						+ "\" \""
						+ getMd().getEventName()
						+ "\" \""
						+ getMd().getName() + "\"";
		}
		
		try {
			out.write(s);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			System.out.println("Favorite:Error wrtting data to log file");
			e.printStackTrace();
		}

		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void newMarket(MarketData md) {
		writeMsg("************** NEW MARKET **************", Color.BLUE);
		setMd(md);

		setInTrade(true);
		
		win = false;

		betPlaced = false;

		betMatched = null;

	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if (marketEventType == MarketChangeListener.MarketNew)
			newMarket(md);

		if (marketEventType == MarketChangeListener.MarketUpdate)
			update();
	}

	@Override
	public void writeMsg(String s, Color c) {

		msgPanel.writeMessageText(s, c);
	}
	
	
	
	

}
