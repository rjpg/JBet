package bots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import statistics.Statistics;

import main.Parameters;
import correctscore.MessageJFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.SwingFrontLine;
import DataRepository.Utils;
import GUI.MyChart2D;

public class InfluenceBot extends Bot {

	public static boolean USE_ONLY_MOUST_INFLUENCE = false;

	public RunnersData rd = null;

	public boolean activated = false;
	public MessageJFrame msgjf = null;

	public NeighboursCorrelationBot ncBot = null;

	public int runnerPos = 0;

	SwingFrontLine swing = null;

	// visuals reds greens
	private JPanel actionsPanel = null;
	private JButton pauseButton = null;

	private JLabel greenLabel = null;
	private JLabel redLabel = null;

	private JLabel greenAmountLabel = null;
	private JLabel redAmountLabel = null;

	public boolean pauseFlag = false;

	public InfluenceBot(MarketData md, int runnerPosA,
			NeighboursCorrelationBot ncBotA) {
		super(md, "InfluenceBot - " + runnerPosA + " - ");
		runnerPos = runnerPosA;
		ncBot = ncBotA;
		initialize();
	}

	public void initialize() {
		// md.addMarketChangeListener(this);
		if (Parameters.graphicalInterfaceBots) {
			msgjf = new MessageJFrame(getName());
			getMsgFrame().setTitle(getName());
			msgjf.getBaseJpanel().add(getActionsPanel(), BorderLayout.SOUTH);
			msgjf.setSize(300, 200);
			msgjf.setVisible(true);
		}
		// msgjf.setAlwaysOnTop(true);
	}

	public JLabel getGreenLabel() {
		if (greenLabel == null) {
			greenLabel = new javax.swing.JLabel("Greens:0", JLabel.CENTER);
			greenLabel.setForeground(Color.WHITE);
			greenLabel.setBackground(Color.GREEN);
			greenLabel.setOpaque(true);
			greenLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return greenLabel;
	}

	public JLabel getGreenAmountLabel() {
		if (greenAmountLabel == null) {
			greenAmountLabel = new javax.swing.JLabel("(0.00)", JLabel.CENTER);
			greenAmountLabel.setForeground(Color.GREEN);
			// greenAmountLabel.setBackground(Color.GREEN);
			// greenAmountLabel.setOpaque(true);
			greenAmountLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return greenAmountLabel;
	}

	public JLabel getRedLabel() {
		if (redLabel == null) {
			redLabel = new javax.swing.JLabel("Reds:0", JLabel.CENTER);
			redLabel.setForeground(Color.WHITE);
			redLabel.setBackground(Color.RED);
			redLabel.setOpaque(true);
			redLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return redLabel;
	}

	public JLabel getRedAmountLabel() {
		if (redAmountLabel == null) {
			redAmountLabel = new javax.swing.JLabel("(0.00)", JLabel.CENTER);
			redAmountLabel.setForeground(Color.RED);
			// greenAmountLabel.setBackground(Color.GREEN);
			// greenAmountLabel.setOpaque(true);
			redAmountLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return redAmountLabel;
	}

	public JPanel getActionsPanel() {
		if (actionsPanel == null) {
			actionsPanel = new JPanel();
			actionsPanel.setLayout(new BorderLayout());
			JPanel auxPanel = new JPanel();
			auxPanel.setLayout(new BorderLayout());
			auxPanel.add(getPauseButton(), BorderLayout.CENTER);
			actionsPanel.add(auxPanel, BorderLayout.SOUTH);

			JPanel auxPanel2 = new JPanel();
			auxPanel2.setLayout(new GridLayout(2, 2));
			auxPanel2.add(getGreenLabel());
			auxPanel2.add(getGreenAmountLabel());
			auxPanel2.add(getRedLabel());
			auxPanel2.add(getRedAmountLabel());
			actionsPanel.add(auxPanel2, BorderLayout.CENTER);
		}
		return actionsPanel;
	}

	public JButton getPauseButton() {
		if (pauseButton == null) {
			if (pauseFlag == true)
				pauseButton = new JButton("Start");
			else
				pauseButton = new JButton("Pause");
			pauseButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (!pauseFlag) {
						pauseFlag = true;
						pauseButton.setText("Continue");
					} else {
						pauseFlag = false;
						pauseButton.setText("Pause");
					}

				}
			});
		}
		return pauseButton;
	}

	public void clearActivation() {
		activated = false;
		rd = null;
	}

	public void selectRunner() {
		if (getMd().getRunners().size() > runnerPos) {
			rd = getMd().getRunners().get(runnerPos);
		} else {
			rd = null;
		}
	}

	public void activate() {
		if (getMd().getCurrentTime() == null) {
			return;
		}
		Calendar currentTime = (Calendar) getMd().getCurrentTime().clone();
		Calendar startTime = getMd().getStart();

		currentTime.add(Calendar.MINUTE, 10);

		if (startTime.compareTo(currentTime) < 0) {
			writeMsg("We are inside the 10m : OK", Color.GREEN);

		} else {
			writeMsg("We are not inside the 10m : Not initialized", Color.RED);
			return;
		}

		selectRunner();
		if (rd == null) {
			writeMsg("No Runner Selected : Race has "
					+ getMd().getRunners().size() + " Runners", Color.RED);
			return;
		} else {
			writeMsg("Runner Selected :" + rd.getName() + ": OK", Color.GREEN);
		}

		if (rd.getDataFrames().get(rd.getDataFrames().size() - 1).getOddBack() < MecanicBot.LOWER_BOUND_ODD
				|| rd.getDataFrames().get(rd.getDataFrames().size() - 1)
						.getOddBack() > MecanicBot.UPPER_BOUND_ODD) {
			writeMsg("Selected Runner(" + rd.getName()
					+ ") is outside ODD bounds ", Color.RED);
			return;
		} else {
			writeMsg("Selected Runner(" + rd.getName()
					+ ") is inside ODD bounds : OK", Color.GREEN);
		}

		if (getMsgFrame() != null) {
			getMsgFrame().setTitle(this.getName() + " " + rd.getName());
		}

		activated = true;
	}

	public void updateGraphicalInterface() {
		if (Parameters.graphicalInterfaceBots) {

		}
	}

	public MessageJFrame getMsgFrame() {
		return msgjf;
	}

	public void update() {
		
		int directionNeighbours = 0;
		int thresholdUp = 2;
		int thresholdDown = -2;

		if (USE_ONLY_MOUST_INFLUENCE) {
			int factor = 0;
			RunnersData mainInfluence = null;
			
			for (RunnersData rdInf : getMd().getRunners()) {
			
				RunnersData rdAux = ncBot.getInfluenciedRunner(rdInf);
				
				if (rdAux == rd  && Utils.getOddBackFrame(rdAux, 0)<20 ) {
					if (ncBot.getInfluenceFactor(rdInf, rdAux) > factor) {
						factor = ncBot.getInfluenceFactor(rdInf, rdAux);
						mainInfluence = rdInf;
					}
				}

			}
			if (mainInfluence == null)
				return;

			directionNeighbours = Utils.getOddBackDirection(mainInfluence, 15,0);
		} else {
			Vector<RunnersData> influenceRunners = new Vector<RunnersData>();

			
			for (RunnersData rdInf : getMd().getRunners()) {
				RunnersData rdAux = ncBot.getInfluenciedRunner(rdInf);
				// if(rdAux==rd && Utils.getOddBackFrame(rdAux, 0)<20 )
				// {
				// if(ncBot.getInfluenceFactor(rdInf, rdAux)>factor)
				// {
				// factor=ncBot.getInfluenceFactor(rdInf, rdAux);
				// mainInfluence=rdInf;
				// }
				// }
				if (rdAux == rd && Utils.getOddBackFrame(rdInf, 0)<20 /*&&
								 * ncBot.getInfluenceFactor(rdInf, rdAux)>30
								 */) {
					influenceRunners.add(rdInf);
					writeMsg("this is influenciaed by : " + rdInf.getName(),
							Color.BLUE);
				}
			}

			if (influenceRunners.size() == 0)
				return;

			for (RunnersData rdaux : influenceRunners) {
				directionNeighbours += Utils.getOddBackDirection(rdaux, 15, 0);
				// writeMsg(rdaux.getName()+" : "+Utils.getOddBackDirection(rdaux,
				// 15,0), Color.RED);
				// writeMsg(Utils.getOddBackFrame(rdaux,
				// 0)+" : "+Utils.getOddBackFrame(rdaux, 15), Color.RED);
				// writeMsg(Utils.oddToIndex(Utils.getOddBackFrame(rdaux,
				// 0))+" : "+Utils.oddToIndex(Utils.getOddBackFrame(rdaux, 15)),
				// Color.RED);
			}

		}

		if (directionNeighbours >= thresholdUp 
				&& Utils.isAmountLayBiggerThanBack(rd,0)
				//&& Utils.getWomAVGWindow(rd,MecanicBot.WINDOW_SIZE+1,MecanicBot.WINDOW_SIZE+1) > Utils.getWomAVGWindow(rd,MecanicBot.WINDOW_SIZE+1,0)
				) {
		
			double odd=Utils.getOddBackFrame(rd, 0);
			if(odd>4)
				swingDown(2,2,100,80);
			else if(odd>3)
				swingDown(3,2,75,60);
			else if(odd>2)
				swingDown(3,3,75,60);
			else 
				swingDown(4,3,50,40);
		}

		if (directionNeighbours <= thresholdDown 
				&& Utils.isAmountBackBiggerThanLay(rd,0)
				//&& Utils.getWomAVGWindow(rd,MecanicBot.WINDOW_SIZE+1,MecanicBot.WINDOW_SIZE+1) < Utils.getWomAVGWindow(rd,MecanicBot.WINDOW_SIZE+1,0)
				) {
			
			
			double odd=Utils.getOddBackFrame(rd, 0);
			if(odd>4)
				swingUP(2,2,100,80);
			else if(odd>3)
				swingUP(3,2,75,60);
			else if(odd>2)
				swingUP(3,3,75,60);
			else 
				swingUP(4,3,50,40);
		}

		updateGraphicalInterface();

	}

	public void swingUP(int ticksUpA, int ticksDownA, int closeTime,
			int emergencyTime) {
		if (!isInTrade()) {
			writeMsg("Start Processing Scalping UP", Color.BLUE);
			double oddLay = rd.getDataFrames()
					.get(rd.getDataFrames().size() - 1).getOddLay();
			writeMsg("Odd Lay (" + rd.getName() + "):" + oddLay, Color.BLACK);
			swing = new SwingFrontLine(md, rd, 2.0, oddLay, closeTime, emergencyTime,
					this, 1, ticksUpA, ticksDownA);
			// swing=new Swing(md,rd, 0.20, oddLay, 100,80, MecanicBot.this,1,2,
			// 1); 60,30
			// scalping=new Scalping(md,rd, 0.20, oddLay, 100,80,
			// MecanicBot.this,1);
		} else
			writeMsg("Processing Last Trade", Color.RED);
	}

	public void swingDown(int ticksUpA, int ticksDownA, int closeTime,
			int emergencyTime) {
		if (!isInTrade()) {
			writeMsg("Start Processing Scalping DOWN", Color.BLUE);
			double oddBak = rd.getDataFrames()
					.get(rd.getDataFrames().size() - 1).getOddBack();
			writeMsg("Odd back (" + rd.getName() + "):" + oddBak, Color.BLACK);

			swing = new SwingFrontLine(md, rd, 2.0, oddBak, closeTime, emergencyTime,
					this, -1, ticksUpA, ticksDownA);

			// swing=new Swing(md,rd, 0.20, oddBak, 100,80,
			// MecanicBot.this,-1,1,2);

			// scalping=new Scalping(md,rd, 0.20, oddBak, 100,80,
			// MecanicBot.this,-1);
		} else
			writeMsg("Processing Last Trade", Color.RED);
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if (marketEventType == MarketChangeListener.MarketNew) {
			clearActivation();
			setMd(md);

			if (Parameters.simulation) {
				if (swing != null) {
					swing.clean();
				}
			}
		}

		if (marketEventType == MarketChangeListener.MarketUpdate) {
			if (!activated) {
				activate();
			} else {
				update();
			}

			if (Parameters.simulation) {
				if (swing != null) {
					swing.updateState();
				}
			}
		}

	}

	@Override
	public void writeMsg(String s, Color c) {
		if (msgjf == null) {
			// System.out.println(getName()+": "+s);
		} else {
			msgjf.writeMessageText(s, c);
		}

	}

	@Override
	public void setInTrade(boolean inTrade) {
		super.setInTrade(inTrade);
		if (inTrade == false) {
			// System.out.println("scalping is null now");
			swing = null;
		}
	}

	@Override
	public void setGreens(int greens) {
		super.setGreens(greens);
		if (getMsgFrame() != null)
			greenLabel.setText("Greens:" + greens);

	}

	@Override
	public void setReds(int reds) {
		super.setReds(reds);
		if (getMsgFrame() != null)
			redLabel.setText("reds:" + reds);
	}

	@Override
	public void setAmountGreen(double amountGreenA) {
		super.setAmountGreen(amountGreenA);
		if (getMsgFrame() != null)
			greenAmountLabel.setText("(" + getAmountGreen() + ")");
	}

	@Override
	public void setAmountRed(double amountRedA) {
		super.setAmountRed(amountRedA);
		if (getMsgFrame() != null)
			redAmountLabel.setText("(" + getAmountRed() + ")");
	}

	@Override
	public void tradeResults(RunnersData rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {
		// TODO Auto-generated method stub
		
	}

	

}
