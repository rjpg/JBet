package bots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import categories.categories2011.CategoriesManager;
import categories.categories2011.Category;

import main.Parameters;
import correctscore.MessageJFrame;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;


public class NeuralDataBot extends Bot{

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
	public JPanel panel=null;
	public JPanel panel2=null;
	public JLabel labels2[][]=null;
	public JPanel panelWindow=null;
	public JLabel labels[][]=null;
	
	public JLabel predict=null;
	

	public NeuralDataBot(MarketData md,int runnerPosA) {
		super(md,"Neural Data Bot - "+runnerPosA+" - ");
		runnerPos=runnerPosA;
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
		
		// graphical
		panel=null;
		panel2=null;
		labels=null;
		labels2=null;
		
		predict=null;
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
			panelWindow.add(panel,BorderLayout.CENTER);
			
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
			predict=new JLabel("0.00",JLabel.CENTER);
			panelWindow.add(predict,BorderLayout.SOUTH);
			
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
		
	}
	
	public void updateGraphicalInterface()
	{
		if(Parameters.graphicalInterfaceBots)
		{
			for(int i=0;i<cat.getWindowSize();i++)
			{
				labels[0][cat.getWindowSize()-i-1].setText(""+oddWindow[i]);
				labels[1][cat.getWindowSize()-i-1].setText(""+oddNeighbourWindow[i]);
			}
			
			
			for(int l=0;l<cat.getAxisSize()*2;l++)
			{
				for(int c=0;c<cat.getWindowSize();c++)
				{
					labels[l+2][cat.getWindowSize()-c-1].setText(""+Utils.convertAmountToBF(ladderWindow[l][c]));
				}
			}
			
			for(int i=0;i<cat.getAxisSizeVolumeDiff()*2;i++)
			{
				labels2[i+2][0].setText(""+Utils.convertAmountToBF(volumeDiff[i]));
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
	
	public void processThresholdOddVariation()
	{
		if(cat==null)
			return;
		
		for(int i=0;i<cat.getWindowSize();i++)
		{
			if(oddWindow[i]<-cat.axisSize)
				oddWindow[i]=-cat.axisSize;
			
			if(oddNeighbourWindow[i]<-cat.axisSize)
				oddNeighbourWindow[i]=-cat.axisSize;
			
			if(oddWindow[i]>cat.axisSize)
				oddWindow[i]=cat.axisSize;
			
			if(oddNeighbourWindow[i]>cat.axisSize)
				oddNeighbourWindow[i]=cat.axisSize;
			
		}
		
		if(predictValue<-cat.axisSize)
			predictValue=-cat.axisSize;
		
		if(predictValue>cat.axisSize)
			predictValue=cat.axisSize;
	}
	
	public int updateNNData()
	{
		//System.out.println("update");
		if(!Utils.isValidWindow(rd, cat.getWindowSize(), cat.getAhead()+5))
			return -1;
		
		//oddWindow = Utils.getOddBackVariation(rd, cat.getWindowSize(), cat.getAhead()+5,oddWindow );
		
		//oddNeighbourWindow = Utils.getOddBackVariation(rdNeighbour, cat.getWindowSize(), cat.getAhead(),oddNeighbourWindow);
		
		//int oddWindowAUX[]= Utils.getOddBackVariation(rd, cat.getWindowSize(), cat.getAhead());
		//ladderWindow=Utils.getLadderAmountDiffWindow(rd,cat.getWindowSize(),cat.getAhead(), cat.getAxisSize(), ladderWindow,oddWindowAUX);
		ladderWindow=Utils.getLadderAmountOfferDiffWindow(rd, cat.getWindowSize(),cat.getAhead(), cat.getAxisSize(), ladderWindow);
		
		//ladderWindow=Utils.processThreshold(ladderWindow, -500., 500.);
		//volumePercent=Utils.getPercentage(Utils.getVolumeLadderFramePivot(rd, cat.getAhead(), cat.getAxisSizeVolume(), Utils.getOddBackFrame(rd, cat.getAhead())),volumePercent);
		
		//amountPercent=Utils.getPercentage(Utils.getAmountLadderFrameBackPivot(rd, cat.getAhead() , cat.getAxisSizeAmounts()),amountPercent);
		
		volumeDiff=Utils.getVolumeLadderFramePivotdiff(rd, cat.getWindowSize(), cat.getAhead(), cat.getAxisSizeVolumeDiff(),Utils.getOddBackFrame(rd, cat.getAhead()));
		//volumeDiff=Utils.processThreshold(volumeDiff, 0, 1000.);
		//volumeDiff=Utils.getPercentage(Utils.getVolumeLadderFramePivotdiff(rd, cat.getWindowSize(), cat.getAhead(), cat.getAxisSizeVolumeDiff(),Utils.getOddBackFrame(rd, cat.getAhead())),volumeDiff);
		
		predictValue=Utils.oddToIndex(Utils.getOddBackFrame(rd,0))-Utils.oddToIndex(Utils.getOddBackFrame(rd, cat.getAhead()-cat.getAheadOffset()));
		
		/*
		//--------------- DEBUG ---------------------
		
		//oddWindow = Utils.getOddBackVariation(rd, cat.getWindowSize(), cat.getAhead(),oddWindow );
		oddWindow = Utils.getOddBackVariation(rd, cat.getWindowSize(), 0,oddWindow );
		
		//oddNeighbourWindow = Utils.getOddBackVariation(rdNeighbour, cat.getWindowSize(), cat.getAhead(),oddNeighbourWindow);
		oddNeighbourWindow = Utils.getOddBackVariation(rdNeighbour, cat.getWindowSize(), 0,oddNeighbourWindow);
		
		//ladderWindow=Utils.getLadderAmountDiffWindow(rd,cat.getWindowSize(),cat.getAhead(), cat.getAxisSize(), ladderWindow,oddWindow);
		ladderWindow=Utils.getLadderAmountDiffWindow(rd,cat.getWindowSize(),0, cat.getAxisSize(), ladderWindow,oddWindow);
		
		//volumePercent=Utils.getPercentage(Utils.getVolumeLadderFramePivot(rd, cat.getAhead(), cat.getAxisSizeVolume(), Utils.getOddBackFrame(rd, cat.getAhead())),volumePercent);
		volumePercent=Utils.getVolumeLadderFramePivot(rd, 0, cat.getAxisSizeVolume(), Utils.getOddBackFrame(rd, 0));
		
		//amountPercent=Utils.getPercentage(Utils.getAmountLadderFrameBackPivot(rd, cat.getAhead() , cat.getAxisSizeAmounts()),amountPercent);
		amountPercent=Utils.getAmountLadderFrameBackPivot(rd, 0 , cat.getAxisSizeAmounts());
		
		//volumeDiff=Utils.getPercentage(Utils.getVolumeLadderFramePivotdiff(rd, cat.getWindowSize(), cat.getAhead(), cat.getAxisSizeVolumeDiff(),Utils.getOddBackFrame(rd, cat.getAhead())),volumeDiff);
		volumeDiff=Utils.getVolumeLadderFramePivotdiff(rd, 1, 0, cat.getAxisSizeVolumeDiff(),Utils.getOddBackFrame(rd, 0));
		
		//predictValue=Utils.oddToIndex(Utils.getOddBackFrame(rd,0))-Utils.oddToIndex(Utils.getOddBackFrame(rd, cat.getAhead()-(cat.getWindowSize()/3)));
		predictValue=Utils.oddToIndex(Utils.getOddBackFrame(rd,0))-Utils.oddToIndex(Utils.getOddBackFrame(rd, 1));
		
		
		//--------------- end DEBUF ---------------------
		*/
		processThresholdOddVariation();
		
		updateInputValues();
		/*
		String s="";
		int x=0;
		for(int i=0;i<cat.getNumberInputValues();i++)
		{
			s=s+","+inputValues[i];
			x++;
			if(x==cat.getWindowSize())
			{
				x=0;
				s=s+"\n";
			}
		}
		
		System.out.println(s);
		*/
		
		CategoriesManager.writeTrainDataIntoCSVCat(cat,inputValues,predictValue);
		
		
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
					//System.out.println("ahead:"+cat.getAhead());
					activateNNData();
					
				}
			}
			else
			{
				if(!processed2)
				{
					if(CategoriesManager.getNumber(cat)==16) //16 use if if you want to process only one cat
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

	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketNew)
		{
			clearActivation();
			setMd(md);
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

	

	
}
