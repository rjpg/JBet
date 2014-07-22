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
import javax.swing.border.LineBorder;

import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.script.normalize.AnalystField;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

import bets.BetData;
import statistics.Statistics;
import categories.categories2011.CategoriesManager;
import categories.categories2011.Category;
import main.Parameters;
import correctscore.MessageJFrame;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MyChart2D;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.swing.Swing;


public class NeuralBot extends Bot implements TradeMechanismListener{

	public int runnerPos=0;
	public RunnersData rd=null;
	
	public RunnersData rdNeighbour=null;
	
	public boolean processed10=false;
	public boolean processed2=false;
	public Category cat=null;
	
	//Bot Control
	public boolean activated=false;
	public MessageJFrame msgjf=null;
	
	//NN data
	public double ladderWindow[][]=null;
	public int oddWindow[]=null;
	public int oddNeighbourWindow[]=null;
	public double volumeDiff[]=null;
	
	public double volumePercent[]=null;
	public double amountPercent[]=null;
	
	public double inputValues[]=null;
	
	public double predictValue=0.00;
	
	//NN visuals
	
	public MyChart2D chart;
	public MyChart2D chartOdd;
	int time=0;
	public JPanel panel=null;
	public JPanel panel2=null;
	public JLabel labels2[][]=null;
	public JPanel panelWindow=null;
	public JLabel labels[][]=null;
	
	public JLabel predict=null;

	// visuals reds greens
	private JPanel actionsPanel=null;
	private JButton pauseButton=null;
	
	private JLabel greenLabel=null;
	private JLabel redLabel=null;
	
	private JLabel greenAmountLabel=null;
	private JLabel redAmountLabel=null;
	
	public boolean pauseFlag=false;
	
	Swing swing=null;
	
	// NN
	BasicNetwork network=null;
	EncogAnalyst analyst=null;
	
	Vector <Double> averagePred=null;
	
	
	public NeighboursCorrelationBot ncBot = null;

	public NeuralBot(MarketData md,int runnerPosA, NeighboursCorrelationBot ncBotA) {
		super(md,"Neural Data Bot - "+runnerPosA+" - ");
		runnerPos=runnerPosA;
		ncBot = ncBotA;
		initialize();
	}
	
	public void initialize()
	{
		//md.addMarketChangeListener(this);
		if(Parameters.graphicalInterfaceBots)
		{
			msgjf=new MessageJFrame("Neural Data Bot - "+runnerPos+" - ");
			getMsgFrame().setTitle("Neural Data Bot - "+runnerPos+" - ");
			msgjf.getBaseJpanel().add(getPanelWindow(),BorderLayout.EAST);
			msgjf.getBaseJpanel().add(getActionsPanel(),BorderLayout.SOUTH);
			msgjf.setSize(300,200);
			msgjf.setVisible(true);
		}
		//msgjf.setAlwaysOnTop(true);
	}
	
	public JPanel getPanelWindow()
	{
		if(panelWindow==null)
		{
			panelWindow=new JPanel();
			panelWindow.setLayout(new BorderLayout());
			
		}
		return panelWindow;
	}
	
	public MessageJFrame getMsgFrame() {
		return msgjf;
	}
	
	public void clearActivation()
	{
		activated=false;
		processed10=false;
		processed2=false;
		rd=null;
		rdNeighbour=null;
		
		cat=null;
		
		ladderWindow=null;
		oddWindow=null;
		oddNeighbourWindow=null;
		volumeDiff=null;
		
		volumePercent=null;
		amountPercent=null;
		
		inputValues=null;
		
		if(averagePred!=null)
		{
			averagePred.clear();
			averagePred=null;
		}
		// graphical
		chart=null;
		chartOdd=null;
		time=0;
		panel=null;
		panel2=null;
		labels=null;
		labels2=null;
		
		predict=null;
		
		network=null;
		
		analyst=null;
	}
	
	
	public void selectRunner()
	{
		if(getMd().getRunners().size()>runnerPos)
		{
			rd=getMd().getRunners().get(runnerPos);
		}
		else
		{
			rd=null;
		}
	}
	
	public void activate()
	{
		
		selectRunner();
		
		
		if(rd==null)
		{
			writeMsg("No Runner Selected : Race has "+getMd().getRunners().size()+" Runners", Color.RED);
			return ;
		}
		else
		{
			writeMsg("Runner Selected :"+rd.getName() +": OK", Color.GREEN);
		}
		
		activated=true;
	}
	
	public void activateGraphicalInterface()
	{
		if(Parameters.graphicalInterfaceBots)
		{
			panelWindow.removeAll();					
			panel=new JPanel();
			panel.setLayout(new GridLayout((cat.getAxisSize()*2)+2,cat.getWindowSize()));
			labels=new JLabel[(cat.getAxisSize()*2)+2][cat.getWindowSize()];
			
			for(int l=0;l<(cat.getAxisSize()*2)+2;l++)
				for(int c=0;c<cat.getWindowSize();c++)
				{
					labels[l][c]=new JLabel("0.00",JLabel.CENTER);
					labels[l][c].setBorder(new LineBorder(Color.blue, 1));
					panel.add(labels[l][c]);
				}
			panelWindow.add(panel,BorderLayout.NORTH);
			
			panel2=new JPanel();
			panel2.setLayout(new GridLayout((cat.getAxisSizeAmounts()*2),3));
			labels2=new JLabel[(cat.getAxisSizeAmounts()*2)][3];
			
			for(int l=0;l<cat.getAxisSizeAmounts()*2;l++)
				for(int c=0;c<3;c++)
				{
					labels2[l][c]=new JLabel("0.00",JLabel.CENTER);
					labels2[l][c].setBorder(new LineBorder(Color.blue, 1));
					panel2.add(labels2[l][c]);
				}
			panelWindow.add(panel2,BorderLayout.EAST);
			//panelWindow.add(panel2,BorderLayout.EAST);
			predict=new JLabel("0.00",JLabel.CENTER);
			panelWindow.add(predict,BorderLayout.SOUTH);
			
			JPanel chartsPanel=new JPanel(new GridLayout(2,1));
			
			chart=new MyChart2D();
			chart.setMaximumSize(new Dimension(100, 300));
			chart.setSize(200, 100);
			chart.setMaxPoints(cat.getWindowSize()*2);
			chartsPanel.add(chart);
			chartOdd=new MyChart2D();
			chartOdd.setMaximumSize(new Dimension(100, 300));
			chartOdd.setSize(200, 100);
			chartOdd.setMaxPoints(cat.getWindowSize()*2);
			chartsPanel.add(chartOdd);
			
			panelWindow.add(chartsPanel,BorderLayout.CENTER);
			
		}
	}
	

	
	public void activateNNData()
	{
		
		oddWindow=new int[cat.getWindowSize()];
		oddNeighbourWindow=new int[cat.getWindowSize()];
		
		ladderWindow=new double[cat.getAxisSize()*2][cat.getWindowSize()];
		
		volumeDiff=new double[cat.getAxisSizeVolumeDiff()*2];
		volumePercent=new double[cat.getAxisSizeVolume()*2];
		amountPercent=new double[cat.getAxisSizeAmounts()*2];
		
		inputValues=new double[cat.getNumberInputValues()];
		
		if(Parameters.graphicalInterfaceBots)
		{
			 activateGraphicalInterface();
		}
		
		analyst=CategoriesManager.getStats(cat);
		if(analyst==null)
		{
			writeMsg("Stats not loaded from File (category:"+CategoriesManager.getNumber(cat)+")", Color.RED);
		}
		else
		{
			writeMsg("Stats loaded from File (category:"+CategoriesManager.getNumber(cat)+")", Color.GREEN);
		}
		
		network=CategoriesManager.getNN(cat);
		if(network==null)
		{
			writeMsg("NN not loaded from File (category:"+CategoriesManager.getNumber(cat)+")", Color.RED);
		}
		else
		{
			writeMsg("NN loaded from File (category:"+CategoriesManager.getNumber(cat)+")", Color.GREEN);
			rd.getMarketData().pause=true;
			System.out.println("See bot :"+getName());
		}
		
		averagePred=new Vector<Double>();
	}
	
	double total_offer=0;
	
	public void updateGraphicalInterface()
	{
		if(Parameters.graphicalInterfaceBots)
		{
			for(int i=0;i<cat.getWindowSize();i++)
			{
				labels[0][cat.getWindowSize()-i-1].setText(""+oddWindow[i]);
				labels[1][cat.getWindowSize()-i-1].setText(""+oddNeighbourWindow[i]);
			}
			
			
			double total_offer2=0;
			for(int l=0;l<cat.getAxisSize()*2;l++)
			{
				for(int c=0;c<cat.getWindowSize();c++)
				{
					labels[l+2][cat.getWindowSize()-c-1].setText(""+Utils.convertAmountToBF(ladderWindow[l][c]));
					
				}
			}
			
			for(int l=0;l<cat.getAxisSize()*2;l++)
			{
				
				total_offer+=ladderWindow[l][0];
			}
			
			
			for(int i=0;i<cat.getAxisSizeVolumeDiff()*2;i++)
			{
				labels2[i][0].setText(""+Utils.convertAmountToBF(volumeDiff[i]));
			}
			
			for(int i=0;i<cat.getAxisSizeAmounts()*2;i++)
			{
				labels2[i][1].setText(""+Utils.convertAmountToBF(amountPercent[i]));
			}
			
			for(int i=0;i<cat.getAxisSizeVolume()*2;i++)
			{
				labels2[i][2].setText(""+Utils.convertAmountToBF(volumePercent[i]));
			}
			
			predict.setText(""+predictValue);
			chart.addValue("predictValue", time, predictValue, Color.BLUE);
			chart.addValue("p", time, 0.25, Color.BLACK);
			chart.addValue("n", time, -0.25, Color.BLACK);
			int var[]=new int[1];
			Utils.getOddBackVariation(rd, 1, 0,var );
			chart.addValue("odd-var", time,((double)var[0]/4.0) , Color.RED);
			chartOdd.addValue("odd-Back",time, Utils.getOddBackFrame(rd, 0) , Color.BLUE);
			
			double avg=0;
			int avgSteps=5;
			if(averagePred.size()>avgSteps)
			{
				for(int i=0;i<avgSteps;i++)
					avg+=averagePred.get(averagePred.size()-i-1);
			}
			avg=(double)((double)avg/(double)avgSteps);
			
			chart.addValue("avg", time, total_offer , Color.GREEN);
			//chart.addValue("avg", time, (double)avg*2. , Color.GREEN);
			//System.out.println(avg);
			time++;
		}
	}

	public void updateInputValues()
	{
		
		for(int i=0;i<cat.getWindowSize();i++)
		{
			//inputValues[i]=oddWindow[i];
			//inputValues[i/*+cat.getWindowSize()*/]=oddNeighbourWindow[i];
			for(int l=0;l<cat.getAxisSize()*2;l++)
			{
				inputValues[i+((cat.getWindowSize())*(l+0))]=ladderWindow[l][i];
			}
		}
		
		
		for(int i=0;i<cat.getAxisSizeVolumeDiff()*2;i++)
		{
			inputValues[i+(cat.getWindowSize()*((cat.getAxisSize()*2)+0))]=volumeDiff[i];
		}
		
		/*for(int i=0;i<cat.getAxisSizeAmounts()*2;i++)
		{
			inputValues[i+((cat.getWindowSize()*((cat.getAxisSize()*2)+1))+(cat.getAxisSizeVolumeDiff()*2))]=amountPercent[i];
		}*/
		
		
//		for(int i=0;i<cat.getAxisSizeVolume()*2;i++)
//		{
//			inputValues[i+((cat.getWindowSize()*((cat.getAxisSize()*2)+1))+(cat.getAxisSizeVolumeDiff()*2)/*+(cat.getAxisSizeAmounts()*2)*/)]=volumePercent[i];
//			//System.err.println("i="+(i+((cat.getWindowSize()*((cat.getAxisSize()*2)+2))+(cat.getAxisSizeVolumeDiff()*2)+(cat.getAxisSizeAmounts()*2)))+" :"+volumePercent[i]);
//		}
		
	}
	
//	public void updateInputValues()
//	{
//		
//		for(int i=0;i<cat.getWindowSize();i++)
//		{
//			//inputValues[i]=oddWindow[i];
//			inputValues[i/*+cat.getWindowSize()*/]=oddNeighbourWindow[i];
//			for(int l=0;l<cat.getAxisSize()*2;l++)
//			{
//				inputValues[i+((cat.getWindowSize())*(l+1))]=ladderWindow[l][i];
//			}
//		}
//		
//		for(int i=0;i<cat.getAxisSizeVolumeDiff()*2;i++)
//		{
//			inputValues[i+(cat.getWindowSize()*((cat.getAxisSize()*1)+2))]=volumeDiff[i];
//		}
//		
//		
//		for(int i=0;i<cat.getAxisSizeAmounts()*2;i++)
//		{
//			inputValues[i+((cat.getWindowSize()*((cat.getAxisSize()*2)+1))+(cat.getAxisSizeVolumeDiff()*2))]=amountPercent[i];
//		}
//		
//		for(int i=0;i<cat.getAxisSizeVolume()*2;i++)
//		{
//			inputValues[i+((cat.getWindowSize()*((cat.getAxisSize()*2)+1))+(cat.getAxisSizeVolumeDiff()*2)/*+(cat.getAxisSizeAmounts()*2)*/)]=volumePercent[i];
//			//System.err.println("i="+(i+((cat.getWindowSize()*((cat.getAxisSize()*2)+2))+(cat.getAxisSizeVolumeDiff()*2)+(cat.getAxisSizeAmounts()*2)))+" :"+volumePercent[i]);
//		}
//	}
	
	public void normalizeInputValues()
	{
		if(analyst==null)
			return;
		
		//String s="";
		int i=0;
		for(AnalystField field: analyst.getScript().getNormalize().getNormalizedFields())
		{
			//s+=field.getName()+" "+field.getActualLow()+" "+field.getActualHigh()+"\n";
			
			if(i<cat.getNumberInputValues())
			{
				inputValues[i]=field.normalize(inputValues[i]);
				//System.out.println(field.getName()+" : "+i);
				//s+=inputValues[i]+" ";
			}
			i++;
		}
		
		//System.out.println(cat.getNumberInputValues());
		//System.out.println(s);
		
		
		//TODO use stats 
	}
	
	public void processThresholdOddVariation()
	{
		if(cat==null)
			return;
		
		for(int i=0;i<cat.getWindowSize();i++)
		{
			if(oddWindow[i]<-cat.axisSize-2)
				oddWindow[i]=-cat.axisSize-2;
			
			if(oddNeighbourWindow[i]<-cat.axisSize-2)
				oddNeighbourWindow[i]=-cat.axisSize-2;
			
			if(oddWindow[i]>cat.axisSize+2)
				oddWindow[i]=cat.axisSize+2;
			
			if(oddNeighbourWindow[i]>cat.axisSize+2)
				oddNeighbourWindow[i]=cat.axisSize+2;
			
		}
		
		if(predictValue<-cat.axisSize-2)
			predictValue=-cat.axisSize-2;
		
		if(predictValue>cat.axisSize+2)
			predictValue=cat.axisSize+2;
	}
	
	public double predict()
	{
		if(network==null)
			return 0.0;
		
		
		double[] out =new double[1];
		//System.out.println("Number of values to compute:"+inputValues.length+"  input count:"+network.getInputCount());
		
		
		NeuralData in=new BasicNeuralData(inputValues);
		NeuralData output = network.compute(in);
		out=output.getData();
		
		
		//System.out.println(out[0]);
		//System.out.println(analyst.getScript().getNormalize().getNormalizedFields().get(cat.getNumberInputValues()).getName());
		return analyst.getScript().getNormalize().getNormalizedFields().get(cat.getNumberInputValues()).deNormalize(out[0]);
		//return out[0]*4;
		
	}
	
	public int updateNNData()
	{
		if(!Utils.isValidWindow(rd, cat.getWindowSize(), cat.getAhead()+5))
			return -1;
		//System.out.println("update");
		//oddWindow = Utils.getOddBackVariation(rd, cat.getWindowSize(), 0+5,oddWindow );
		
		//oddNeighbourWindow = Utils.getOddBackVariation(rdNeighbour, cat.getWindowSize(), 0,oddNeighbourWindow);
		
		//int oddWindowAUX[]= Utils.getOddBackVariation(rd, cat.getWindowSize(), 0);
		//ladderWindow=Utils.getLadderAmountDiffWindow(rd,cat.getWindowSize(),0, cat.getAxisSize(), ladderWindow,oddWindowAUX);
		
		ladderWindow=Utils.getLadderAmountOfferDiffWindow(rd, cat.getWindowSize(),0, cat.getAxisSize(), ladderWindow);
		//ladderWindow=Utils.processThreshold(ladderWindow, -500., 500.);
		//volumePercent=Utils.getPercentage(Utils.getVolumeLadderFramePivot(rd, 0, cat.getAxisSizeVolume(), Utils.getOddBackFrame(rd, 0)),volumePercent);
		
		//amountPercent=Utils.getPercentage(Utils.getAmountLadderFrameBackPivot(rd, 0, cat.getAxisSizeAmounts()),amountPercent);
		
		volumeDiff=Utils.getVolumeLadderFramePivotdiff(rd, cat.getWindowSize(), 0, cat.getAxisSizeVolumeDiff(),Utils.getOddBackFrame(rd, 0));
		//volumeDiff=Utils.processThreshold(volumeDiff, 0, 1000.);
		//volumeDiff=Utils.getPercentage(Utils.getVolumeLadderFramePivotdiff(rd, cat.getWindowSize(), 0, cat.getAxisSizeVolumeDiff(),Utils.getOddBackFrame(rd, 0)),volumeDiff);
		
		//processThresholdOddVariation();
		
		updateInputValues();
		
		normalizeInputValues();
		
		predictValue=predict();
		
		int directionNeighbours = 0;
		int thresholdUp = 2;
		int thresholdDown = -2;

		
		/*	int factor = 0;
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
			if (mainInfluence != null)
				directionNeighbours = Utils.getOddBackDirection(mainInfluence, cat.getWindowSize(),0);
		
		mainInfluence = null;
		*/
		if(cat.getOddCat()==2)
		{
			if(oddWindow[0]!=0&& oddWindow[1]!=0 && oddWindow[2]!=0)
				averagePred.add(0.);
			else
				averagePred.add(predictValue);
			
			double avg=0;
			
			if(averagePred.size()>10)
			{
				for(int i=0;i<10;i++)
					avg+=averagePred.get(averagePred.size()-i-1);
			}
			avg=(double)((double)avg/(double)10.0);
			
			boolean constant=true;
			for(int i=0;i<5;i++)
			{
				if(oddWindow[i]!=0)
					constant=false;
			}
			
			//if(constant)
			{
				if(avg>0.25)
				{
				//	if(mainInfluence!=null && directionNeighbours <= thresholdDown )
					{
					writeMsg("UPPPPPPPPPPPP", Color.GREEN);
					swingUP(2,2,cat.getWindowSize(),cat.getWindowSize()/2);
					}
//					if(mainInfluence==null )
//					{
//					writeMsg("UPPPPPPPPPPPP", Color.GREEN);
//					swingUP(2,2,cat.getWindowSize(),cat.getWindowSize()/2);
//					}
				}
				else if(avg<-0.25)
				{
//					if(mainInfluence!=null && directionNeighbours >= thresholdUp )
//					{
					writeMsg("DOWNNNNNNNNNNN", Color.RED);
					swingDown(2,2,cat.getWindowSize(),cat.getWindowSize()/2);
//					}
//					if(mainInfluence==null )
//					{
//						writeMsg("DOWNNNNNNNNNNN", Color.RED);
//						swingDown(2,2,cat.getWindowSize(),cat.getWindowSize()/2);
//					}
				}
				else
					writeMsg("ZEROOOOOOOOOO", Color.BLUE);
			}
		}
		
		if(cat.getOddCat()==1)
		{
			
			//if(oddWindow[0]!=0&& oddWindow[1]!=0 && oddWindow[2]!=0)
			//	averagePred.add(0.);
			//else
				averagePred.add(predictValue);
			
			double avg=0;
			int avgSteps=cat.getWindowSize();
			if(averagePred.size()>avgSteps)
			{
				for(int i=0;i<avgSteps;i++)
					avg+=averagePred.get(averagePred.size()-i-1);
			}
			avg=(double)((double)avg/(double)avgSteps);
			
			
			boolean constant=true;
			for(int i=0;i<5;i++)
			{
				if(oddWindow[i]!=0)
					constant=false;
			}
			
			//if(constant)
			{
				if(avg*2>0.25
						&& Utils.isAmountBackBiggerThanLay(rd,0)		
				)
				{
//					if(mainInfluence!=null && directionNeighbours <= thresholdDown )
//					{
						writeMsg("UPPPPPPPPPPPP", Color.GREEN);
						swingUP(2,2,cat.getWindowSize(),cat.getWindowSize()/2);
//					}
//					if(mainInfluence==null )
//					{
//						writeMsg("UPPPPPPPPPPPP", Color.GREEN);
//						swingUP(2,2,cat.getWindowSize(),50);
//					}
				}
				else if(avg*2<-0.25
						&& Utils.isAmountLayBiggerThanBack(rd,0)		
				)
				{
//					if(mainInfluence!=null && directionNeighbours >= thresholdUp )
//					{
						writeMsg("DOWNNNNNNNNNNN", Color.RED);
						swingDown(2,2,cat.getWindowSize(),cat.getWindowSize()/2);
//					}
//					if(mainInfluence==null )
//					{
//						writeMsg("DOWNNNNNNNNNNN", Color.RED);
//						swingDown(2,2,cat.getWindowSize(),50);
//					}
				}
				else
					writeMsg("ZERO", Color.BLUE);
			}
		}
		
		
		updateGraphicalInterface();
		
		return 0;
	}
	
	public void update()
	{
		if(getMinutesToStart()<=0)
			return;
		
		if(rd!=null)
		{
			if(!processed10)
			{
				Category catAux=CategoriesManager.getCategory(rd);
				if(catAux==null)
					return;
				
				if(catAux.isTime10m())
				{
					processed10=true;
					cat=catAux;
					
					if(cat.isFavorite() && rd!=Utils.getFavorite(getMd()))
						rdNeighbour=Utils.getFavorite(getMd());
					else
						rdNeighbour=Utils.getNeighbour(getMd(), rd);
					
					writeMsg(CategoriesManager.getNumber(cat)+":"+cat.getNumberInputValues()+":"+CategoriesManager.getDirectory(catAux), Color.BLUE);
					writeMsg("Neighbour:"+rdNeighbour.getName(), Color.BLUE);
					CategoriesManager.printCategory(catAux);
					activateNNData();
				}
			}
			else
			{
				if(!processed2)
				{
					if(CategoriesManager.getNumber(cat)==16) // filter cat
						updateNNData();
				}
			}
			
			if (!processed2)
			{
				// para não estar sempre a calcular ver o tempo 
				
				Category catAux=CategoriesManager.getCategory(rd);
				if(catAux==null)
					return;
				
				
				if(!catAux.isTime10m())
				{
					processed2=true;
					cat=catAux;
					if(cat.isFavorite() && rd!=Utils.getFavorite(getMd()))
						rdNeighbour=Utils.getFavorite(getMd());
					else
						rdNeighbour=Utils.getNeighbour(getMd(), rd);
					writeMsg(CategoriesManager.getNumber(cat)+":"+CategoriesManager.getDirectory(catAux), Color.BLUE);
					writeMsg("Neighbour:"+rdNeighbour.getName(), Color.BLUE);
					CategoriesManager.printCategory(catAux);
					activateNNData();
				}
			}
			else
			{
				//call 2m NN
				//updateNNData();
			}
		}
	}
	
	public void swingUP(int ticksUpA,int ticksDownA, int closeTime, int emergencyTime)
	{
		if(!isInTrade())
		{
			writeMsg("Start Processing Scalping UP",Color.BLUE);
			double oddLay=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddLay();
			writeMsg("Odd Lay ("+rd.getName()+"):"+oddLay, Color.BLACK);
			
			swing=new Swing(NeuralBot.this,rd, 2.0, oddLay, 1,closeTime, emergencyTime ,BetData.LAY,ticksUpA,ticksDownA);
			
			//swing=new SwingFrontLine(md,rd, 2.0, oddLay,  closeTime,emergencyTime, NeuralBot.this,1,ticksUpA, ticksDownA);
			
			//swing=new Swing(md,rd, 0.20, oddLay,  100,80, MecanicBot.this,1,2, 1);  60,30
			//scalping=new Scalping(md,rd, 0.20, oddLay, 100,80, MecanicBot.this,1);
		}
		else
			writeMsg("Processing Last Trade",Color.RED);
	}
	

	
	public void swingDown(int ticksUpA,int ticksDownA, int closeTime, int emergencyTime)
	{
		if(!isInTrade())
		{
			writeMsg("Start Processing Scalping DOWN",Color.BLUE);
			double oddBak=rd.getDataFrames().get(rd.getDataFrames().size()-1).getOddBack();
			writeMsg("Odd back ("+rd.getName()+"):"+oddBak, Color.BLACK);
			
			swing=new Swing(NeuralBot.this,rd, 2.0, oddBak, 1,closeTime, emergencyTime ,BetData.BACK,ticksDownA,ticksUpA);
			
			//swing=new SwingFrontLine(md,rd, 2.0, oddBak, closeTime,emergencyTime,  NeuralBot.this,-1,ticksUpA,ticksDownA);
			
			//swing=new Swing(md,rd, 0.20, oddBak, 100,80, MecanicBot.this,-1,1,2);
			
			//scalping=new Scalping(md,rd, 0.20, oddBak, 100,80, MecanicBot.this,-1);
		}
		else
			writeMsg("Processing Last Trade",Color.RED);
	}
	
	public JPanel getActionsPanel()
	{
		if(actionsPanel==null)
		{
			actionsPanel=new JPanel();
			actionsPanel.setLayout(new BorderLayout());
			JPanel auxPanel=new JPanel();
			auxPanel.setLayout(new BorderLayout());
			auxPanel.add(getPauseButton(),BorderLayout.CENTER);
			actionsPanel.add(auxPanel,BorderLayout.SOUTH);
			
			JPanel auxPanel2=new JPanel();
			auxPanel2.setLayout(new GridLayout(2,2));
			auxPanel2.add(getGreenLabel());
			auxPanel2.add(getGreenAmountLabel());
			auxPanel2.add(getRedLabel());
			auxPanel2.add(getRedAmountLabel());
			actionsPanel.add(auxPanel2,BorderLayout.CENTER);
		}
		return actionsPanel;
	}
	
	public JLabel getGreenLabel()
	{
		if (greenLabel==null)
		{
			greenLabel=new javax.swing.JLabel("Greens:0",JLabel.CENTER);
			greenLabel.setForeground(Color.WHITE);
			greenLabel.setBackground(Color.GREEN);
			greenLabel.setOpaque(true);
			greenLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return greenLabel;
	}
	
	public JLabel getGreenAmountLabel()
	{
		if (greenAmountLabel==null)
		{
			greenAmountLabel=new javax.swing.JLabel("(0.00)",JLabel.CENTER);
			greenAmountLabel.setForeground(Color.GREEN);
			//greenAmountLabel.setBackground(Color.GREEN);
			//greenAmountLabel.setOpaque(true);
			greenAmountLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return greenAmountLabel;
	}
	
	public JLabel getRedLabel()
	{
		if (redLabel==null)
		{
			redLabel=new javax.swing.JLabel("Reds:0",JLabel.CENTER);
			redLabel.setForeground(Color.WHITE);
			redLabel.setBackground(Color.RED);
			redLabel.setOpaque(true);
			redLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return redLabel;
	}
	
	public JLabel getRedAmountLabel()
	{
		if (redAmountLabel==null)
		{
			redAmountLabel=new javax.swing.JLabel("(0.00)",JLabel.CENTER);
			redAmountLabel.setForeground(Color.RED);
			//greenAmountLabel.setBackground(Color.GREEN);
			//greenAmountLabel.setOpaque(true);
			redAmountLabel.setHorizontalTextPosition(JLabel.CENTER);
		}
		return redAmountLabel;
	}
	
	public JButton getPauseButton()
	{
		if(pauseButton==null)
		{
			if(pauseFlag==true)
				pauseButton=new JButton("Start");
			else
				pauseButton=new JButton("Pause");
			pauseButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(!pauseFlag)
					{
						pauseFlag=true;
						pauseButton.setText("Continue");
					}
					else
					{
						pauseFlag=false;
						pauseButton.setText("Pause");
					}	
					
				}
			});
		}
		return pauseButton;
	}

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketNew)
		{
			clearActivation();
			setMd(md);
			
			if(Parameters.simulation)
			{
					if(swing!=null)
					{
						swing.clean();
					}
			}
		}
		
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
				if(!activated)
				{
					activate();
				}
				else
				{
					update();
				}
				
				
		}
		
	}

	@Override
	public void writeMsg(String s, Color c) {
		if(msgjf==null)
		{
			//	System.out.println(getName()+": "+s);
		}
		else
		{
			msgjf.writeMessageText(s, c);
		}
	}

	
	@Override
	public void setInTrade(boolean inTrade) {
		super.setInTrade(inTrade);
		if(inTrade==false)
		{
			//System.out.println("scalping is null now");
			swing=null;
		}
	}

	
	@Override
	public void setGreens(int greens) {
		super.setGreens(greens);
		if(getMsgFrame()!=null)
			greenLabel.setText("Greens:"+greens);
		
	}

	@Override
	public void setReds(int reds) {
		super.setReds(reds);
		if(getMsgFrame()!=null)
			redLabel.setText("reds:"+reds);
	}
	
	@Override
	public void setAmountGreen(double amountGreenA) {
		super.setAmountGreen(amountGreenA);
		if(getMsgFrame()!=null)
			greenAmountLabel.setText("("+getAmountGreen()+")");
	}

	@Override
	public void  setAmountRed(double amountRedA) {
		super.setAmountRed(amountRedA);
		if(getMsgFrame()!=null)
			redAmountLabel.setText("("+getAmountRed()+")");
	}

	//@Override
	public void tradeResults(String rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {
		
		Statistics.writeStatistics(getMinutesToStart(),cat.getAm(), cat.getAm(), md.getRunners().size(), cat.isFavorite(), rdNeighbour.getName(), Calendar.getInstance().getTimeInMillis(), redOrGreen, entryUpDown, entryOdd, exitOdd, ticksMoved, stake, exitStake , amountMade, getMinutesToStart(), rd, 0, rdNeighbour.getName(),0,0,getMd().getCurrentTime().get(Calendar.DAY_OF_WEEK));
		
	}
	
	@Override
	public void tradeMechanismChangeState(TradeMechanism tm, int state) {
		System.out.println("Tm state : "+tm.getState());
		
	}

	@Override
	public void tradeMechanismEnded(TradeMechanism tm, int state) {
		if(tm instanceof Swing)
		{
			
			String[] fields=tm.getStatisticsFields().split(" ");
			String[] values=tm.getStatisticsValues().split(" ");
			           
			String msg="----- Swing Statistics -----\n";
			
			for(int i=0;i<fields.length;i++)
			{
				msg+="["+i+"] "+fields[i]+" : "+values[i]+"\n";
			}
			
			msg+="------------ || ------------";
			msgjf.writeMessageText(msg,Color.BLUE);
			
			if(values[0].equals("CLOSED"))
			{
				
				int ticksMoved=Integer.parseInt(values[17]);
				double pl=Double.parseDouble(values[16]);
				int direction=1;
				if(values[10].equals("BL"))
					direction=-1;
				int redOrGreen=1;
				if(pl<0)
				{
					redOrGreen=-1;
					
					setReds(getReds()+1);
					setAmountRed(getAmountRed()+pl);
				}
				else
				{
					setGreens(getGreens()+1);
					setAmountGreen(getAmountGreen()+pl);
				}
					
				
				double entryOdd=Double.parseDouble(values[18]);
				double exitOdd=Double.parseDouble(values[19]);
				
				double entryStake=Double.parseDouble(values[20]);
				double exitStake=Double.parseDouble(values[21]);
				
				tradeResults(values[3], redOrGreen, direction,entryOdd, exitOdd, entryStake, exitStake, pl, ticksMoved);
				
			}
				
			
		}
		
		tm.removeTradeMechanismListener(this);
		
	}

	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {
		msgjf.writeMessageText(msg,color);
		
	}

}
