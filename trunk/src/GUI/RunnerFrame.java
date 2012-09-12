package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLDocument.RunElement;

import main.Parameters;

import aw.gui.chart.ITrace2D;

import DataRepository.MarketData;
import DataRepository.OddData;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class RunnerFrame extends JFrame{
	
	public RunnersData runnerData;
	
	private JPanel contentPane;
	
	MyChart2D coddLay;
	MyChart2D camountLay;
	
	MyChart2D coddBack;
	MyChart2D camountBack;
	
	MyChart2D cmatchedAmount;
	MyChart2D clastMatchet;
	
	
	MyChart2D cweightmoney;
	MyChart2D cweightmoneydiff;
	
	
	
	private JPanel chartsPanel;
	
	JLabel loddLay;
	JLabel lamountLay;
	
	JLabel loddBack;
	JLabel lamountBack;
	
	JLabel llmatchedAmount;
	JLabel llastMatchet;
	JLabel lweightmoneyLay; 
	JLabel lweightmoneyBack;
	
	public static final int ODDSDEPTHS=Utils.ladderSize();
	//JLabel[] backOdd=new JLabel[ODDSDEPTHS];
	JLabel[] ladderBackAmount=new JLabel[ODDSDEPTHS];
	JLabel[] ladderOdd=new JLabel[ODDSDEPTHS];
	JLabel[] ladderLayAmount=new JLabel[ODDSDEPTHS];
	JLabel[] ladderVolume=new JLabel[ODDSDEPTHS];
	
	JPanel back;
	JPanel odd;
	JPanel lay;
	JPanel volume;
	
	private JPanel labelsPanel;
	
	public RunnerFrame(RunnersData rd) {
		super(rd.getName());
		
		runnerData=rd;
		
		coddLay=new MyChart2D(runnerData.getMarketData().getStart());
		camountLay=new MyChart2D(runnerData.getMarketData().getStart());
		
		coddBack=new MyChart2D(runnerData.getMarketData().getStart());
		camountBack=new MyChart2D(runnerData.getMarketData().getStart());
		
		cmatchedAmount=new MyChart2D(runnerData.getMarketData().getStart());
		clastMatchet=new MyChart2D(runnerData.getMarketData().getStart());
		
		cweightmoney=new MyChart2D(runnerData.getMarketData().getStart());
		cweightmoneydiff=new MyChart2D(runnerData.getMarketData().getStart());

		chartsPanel=new JPanel();
		chartsPanel.setLayout(new GridLayout(8,1));
		chartsPanel.add(coddBack);chartsPanel.add(coddLay);
		
		chartsPanel.add(camountBack);chartsPanel.add(camountLay);
		
		chartsPanel.add(cmatchedAmount);chartsPanel.add(clastMatchet);
		
		chartsPanel.add(cweightmoney);chartsPanel.add(cweightmoneydiff);
		
		loddLay=new JLabel("Odd Lay: ");
		lamountLay=new JLabel("Amount Lay: ");
		
		loddBack=new JLabel("Odd Back: ");
		lamountBack=new JLabel("Amount Back: ");
		
		llmatchedAmount=new JLabel("Last Matched Am.: ");
		llastMatchet=new JLabel("Last Matched Odd: ");
		
		lweightmoneyLay= new JLabel("Weight Lay"); 
		lweightmoneyBack = new JLabel("Weight Back");
		
		labelsPanel=new JPanel();
		labelsPanel.add(loddBack);
		labelsPanel.add(lamountBack);
		
		labelsPanel.add(loddLay);
		labelsPanel.add(lamountLay);
		
		labelsPanel.add(llmatchedAmount);
		labelsPanel.add(llastMatchet);
		
		labelsPanel.add(lweightmoneyBack);
		labelsPanel.add(lweightmoneyLay);
		
		
				
		contentPane=new JPanel();
		contentPane.setLayout(new BorderLayout());
		
		contentPane.add(labelsPanel,BorderLayout.NORTH);
		contentPane.add(chartsPanel,BorderLayout.CENTER);
		
		JPanel aux2=new JPanel();
		aux2.setLayout(new BorderLayout());
	    aux2.add(contentPane,BorderLayout.CENTER);
		
	    JScrollPane jScrollPane = new JScrollPane();
	    jScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    
		JPanel aux=new JPanel();
		aux.setLayout(new GridLayout(0,4));
		aux.add(getBack());
		aux.add(getOdds());
		aux.add(getLay());
		aux.add(getVolume());
		jScrollPane.setViewportView(aux);
		
		//aux.setSize(300, 500);
		aux2.add(jScrollPane,BorderLayout.EAST);
		
		this.setContentPane(aux2);
		this.setSize(400, 800);
	}
	
	public JPanel getBack() {
		if(back==null)
		{
			back=new JPanel();
			back.setLayout(new GridLayout(ODDSDEPTHS,1));
			back.setMinimumSize(new Dimension(150, 300));
			back.setBorder(BorderFactory.createLineBorder(Color.GREEN.darker()));
			for(int i=ODDSDEPTHS-1;i>=0;i--)
			{
				ladderBackAmount[i]=new JLabel();
				ladderBackAmount[i].setBorder(BorderFactory.createLoweredBevelBorder());
				back.add(ladderBackAmount[i]);
				
			}
		}
		return back;
	}
	
	public JPanel getOdds() {
		if(odd==null)
		{
			odd=new JPanel();
			odd.setLayout(new GridLayout(ODDSDEPTHS,1));
			odd.setMinimumSize(new Dimension(150, 300));
			//odd.setBorder(BorderFactory.createLineBorder(Color.GREEN.darker()));
			for(int i=ODDSDEPTHS-1;i>=0;i--)
			{
				ladderOdd[i]=new JLabel();
				//ladderOdd[i].setBorder(BorderFactory.createLoweredBevelBorder());
				ladderOdd[i].setText(Utils.indexToOdd(i)+"");
				odd.add(ladderOdd[i]);
				
			}
		}
		return odd;
	}
	
	public JPanel getLay() {
		if(lay==null)
		{
			lay=new JPanel();
			lay.setLayout(new GridLayout(ODDSDEPTHS,1));
			lay.setMinimumSize(new Dimension(150, 300));
			lay.setBorder(BorderFactory.createLineBorder(Color.RED.darker()));
			for(int i=ODDSDEPTHS-1;i>=0;i--)
			{
				ladderLayAmount[i]=new JLabel();
				ladderLayAmount[i].setBorder(BorderFactory.createLoweredBevelBorder());
				lay.add(ladderLayAmount[i]);
				
			}
		}
		return lay;
	}
	
	public JPanel getVolume() {
		if(volume==null)
		{
			volume=new JPanel();
			volume.setLayout(new GridLayout(ODDSDEPTHS,1));
			volume.setMinimumSize(new Dimension(150, 300));
			volume.setBorder(BorderFactory.createLineBorder(Color.BLUE.darker()));
			for(int i=ODDSDEPTHS-1;i>=0;i--)
			{
				ladderVolume[i]=new JLabel();
				ladderVolume[i].setBorder(BorderFactory.createLoweredBevelBorder());
				volume.add(ladderVolume[i]);
				
			}
		}
		return volume;
	}

	
	long time=0;
	public void update()
	{
		time++;
		double value=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getOddBack();
		//long time=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getTimestamp().getTimeInMillis();
		coddBack.addValue("Odd Back", time, value, Color.BLUE);
		loddBack.setText("Odd Back: "+Utils.getFormatedDouble(value));
		
		value=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getAmountBack();
		camountBack.addValue("Amount Back", time, value, Color.BLUE);
		lamountBack.setText("Amount Back: "+Utils.getFormatedDouble(value));
		
		value=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getOddLay();
		coddLay.addValue("Odd Lay", time, value, Color.BLUE);
		loddLay.setText("Odd Lay: "+ Utils.getFormatedDouble(value));
		
		value=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getAmountLay();
		camountLay.addValue("Amount Lay", time, value, Color.BLUE);
		lamountLay.setText("Amount Lay: "+ Utils.getFormatedDouble(value));
		
		value=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getMatchedAmount();
		cmatchedAmount.addValue("Matched Amount", time, value, Color.BLUE);
		llmatchedAmount.setText("Matched Amount: "+ Utils.getFormatedDouble(value));
		
		value=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getLastMatchet();
		clastMatchet.addValue("Last Matched Amount", time, value, Color.BLUE);
		llastMatchet.setText("Last Matched Amount: "+ Utils.getFormatedDouble(value));
		
		
		
		
		Vector<OddData> odds=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getLayPrices();
		//int size=odds.size();
		
		//if(size > ODDSDEPTHS)
		//	size=ODDSDEPTHS;
		
		for(int i=0;i<ODDSDEPTHS;i++)
		{
			double processingOdd=Utils.indexToOdd(i);
			boolean zero=true;
			double amountValue=0;
			for(int x=0;x<odds.size();x++)
			{
				if(odds.get(x).getOdd()==processingOdd)
				{
					zero=false;
					amountValue=odds.get(x).getAmount();
				}
			}
			if(zero)
				ladderLayAmount[i].setText("");
			else
				ladderLayAmount[i].setText(amountValue+"");
		}
		
		//odds.clear();
		
		odds=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getBackPrices();
		
		
		//if(size > ODDSDEPTHS)
		//	size=ODDSDEPTHS;
		
		for(int i=0;i<ODDSDEPTHS;i++)
		{
			double processingOdd=Utils.indexToOdd(i);
			boolean zero=true;
			double amountValue=0;
			for(int x=0;x<odds.size();x++)
			{
				if(odds.get(x).getOdd()==processingOdd)
				{
					zero=false;
					amountValue=odds.get(x).getAmount();
				}
			}
			if(zero)
				ladderBackAmount[i].setText("");
			else
				ladderBackAmount[i].setText(amountValue+"");
		}
		
		Vector<OddData> oddsB=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getBackPrices();						 
		Vector<OddData> oddsL=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getLayPrices();
		value=0;
		double value2=0;
		double oddBack=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getOddBack();
		double oddLay=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getOddLay();
		for(int i=0;i<=Parameters.WOM_DIST_CENTER;i++)
		{
			double amountValueB=0;
			//System.out.println("B size:"+oddsB.size());
			for(int x=0;x<oddsB.size();x++)
			{
				if(oddsB.get(x).getOdd()==Utils.indexToOdd(Utils.oddToIndex(oddBack)-i))
				{
					amountValueB=oddsB.get(x).getAmount();
				}
			}
			value+=amountValueB;
			
			double amountValueL=0;
			//System.out.println("l size:"+oddsL.size());
			for(int x=0;x<oddsL.size();x++)
			{
				if(oddsL.get(x).getOdd()==Utils.indexToOdd(Utils.oddToIndex(oddLay)+i))
				{
					amountValueL=oddsL.get(x).getAmount();
				}
			}
			value2+=amountValueL;
			
		}
		
		//value=runnerData.getData().get(runnerData.getData().size()-1).getWeightmoneyBack();
		//value2=runnerData.getData().get(runnerData.getData().size()-1).getWeightmoneyLay();
		cweightmoney.addValue("Weight Back", time, value, Color.BLUE);
		cweightmoney.addValue("Weight Lay", time, value2, Color.RED);
		cweightmoneydiff.addValue("Weight Back-Lay", time, value-value2, Color.BLUE);
		cweightmoneydiff.addValue("zero", time, 0, Color.MAGENTA);
		lweightmoneyBack.setText("Weight Back: "+ Utils.getFormatedDouble(value));
		lweightmoneyLay.setText("Weight Lay: "+ Utils.getFormatedDouble(value2));
		
		//System.out.println("Size:"+runnerData.getDataFrames().size()+" # getting volume from "+runnerData.getName()+" is "+runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getVolume());
		Hashtable<Double, Double> volume=runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getVolume();
		
		if(volume!=null)
		{
			Enumeration<Double> e = volume.keys();
			while(e.hasMoreElements())
			{
				Double odd=e.nextElement();
				//System.out.println("Volume ODD:"+odd);
				if(Utils.validOdd(odd))
					ladderVolume[Utils.oddToIndex(/*Utils.nearValidOdd(*/odd/*)*/)].setText(""+volume.get(odd));
				//else
				//	System.out.println("odd não valida :"+odd);
			} 
		}
	
	}
	
	public void clean()
	{
		runnerData=null;
		
		contentPane.removeAll();
		//coddLay.removetr
		
		/*List tl = coddLay.getTraces();
		for (Object ob:tl)
		{
			ITrace2D it=(ITrace2D)ob;
			coddLay.removeTrace(it);
		}*/
		coddLay.removeAll();
		coddLay=null;
		
		/*tl=camountLay.getTraces();
		for (Object ob:tl)
		{
			ITrace2D it=(ITrace2D)ob;
			camountLay.removeTrace(it);
		}*/
		camountLay.removeAll();
		camountLay=null;
		
		/*tl=coddBack.getTraces();
		for (Object ob:tl)
		{
			ITrace2D it=(ITrace2D)ob;
			coddBack.removeTrace(it);
		}*/
		coddBack.removeAll();
		coddBack=null;
		
		/*tl=camountBack.getTraces();
		for (Object ob:tl)
		{
			ITrace2D it=(ITrace2D)ob;
			camountBack.removeTrace(it);
		}*/
		camountBack.removeAll();
		camountBack=null;
		
		/*tl=cmatchedAmount.getTraces();
		for (Object ob:tl)
		{
			ITrace2D it=(ITrace2D)ob;
			cmatchedAmount.removeTrace(it);
		}*/
		cmatchedAmount.removeAll();
		cmatchedAmount=null;
		
		/*tl=clastMatchet.getTraces();
		for (Object ob:tl)
		{
			ITrace2D it=(ITrace2D)ob;
			clastMatchet.removeTrace(it);
		}*/
		clastMatchet.removeAll();
		clastMatchet=null;
		
		/*tl=cweightmoney.getTraces();
		for (Object ob:tl)
		{
			ITrace2D it=(ITrace2D)ob;
			cweightmoney.removeTrace(it);
		}*/
		cweightmoney.removeAll();
		cweightmoney=null;
		
		/*tl=cweightmoneydiff.getTraces();
		for (Object ob:tl)
		{
			ITrace2D it=(ITrace2D)ob;
			cweightmoneydiff.removeTrace(it);
		}
		tl=null;*/
		
		cweightmoneydiff.removeAll();
		cweightmoneydiff=null;
		
		chartsPanel.removeAll();
		chartsPanel=null;
		
		loddLay.removeAll();
		loddLay=null; 
		
		lamountLay.removeAll();
		lamountLay=null;
		
		loddBack.removeAll();
		loddBack=null;
		
		lamountBack.removeAll();
		lamountBack=null;
		
		llmatchedAmount.removeAll();
		llmatchedAmount=null;
		
		llastMatchet.removeAll();
		llastMatchet=null;
		
		lweightmoneyLay.removeAll();
		lweightmoneyLay=null;
		
		lweightmoneyBack.removeAll();
		lweightmoneyBack=null;
		
		labelsPanel.removeAll();
		labelsPanel=null;
	}
	
	
	
	public MyChart2D getCoddLay() {
		return coddLay;
	}

	public MyChart2D getCamountLay() {
		return camountLay;
	}

	public MyChart2D getCoddBack() {
		return coddBack;
	}

	public MyChart2D getCamountBack() {
		return camountBack;
	}

	public MyChart2D getCmatchedAmount() {
		return cmatchedAmount;
	}

	public MyChart2D getClastMatchet() {
		return clastMatchet;
	}

	public MyChart2D getCweightmoney() {
		return cweightmoney;
	}

	public MyChart2D getCweightmoneydiff() {
		return cweightmoneydiff;
	}

}
