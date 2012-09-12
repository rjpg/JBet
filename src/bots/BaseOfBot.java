package bots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;

import main.Parameters;
import correctscore.MessageJFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.SwingFrontLine;
import DataRepository.Utils;
import GUI.MyChart2D;

public class BaseOfBot extends Bot{

public int number=5;

	
	public boolean activated=false;
	public MessageJFrame msgjf=null;
	
	public JPanel panelWindow=null;

	public JPanel panel=null;
	public MyChart2D chart;
	public MyChart2D chartOdd;
	int time=0;
	
	SwingFrontLine swing=null;
	
	public BaseOfBot(MarketData md) {
		super(md,"NeighboursCorrelationBot - ");
		
		initialize();
	}
	
	public void initialize()
	{
		//md.addMarketChangeListener(this);
		if(Parameters.graphicalInterfaceBots)
		{
			msgjf=new MessageJFrame("NeighboursCorrelationBot - ");
			getMsgFrame().setTitle("NeighboursCorrelationBot - ");
						
			JPanel aux=msgjf.getBaseJpanel();
			JPanel panelaux=new JPanel();
			panelaux.setLayout(new GridLayout(2,1));
			panelaux.add(aux);
			
			panelaux.add(getPanelWindow());
			msgjf.setContentPane(panelaux);
			
			//msgjf.getBaseJpanel().add(getActionsPanel(),BorderLayout.SOUTH);
			msgjf.setSize(300,200);
			msgjf.setVisible(true);
		}
		//msgjf.setAlwaysOnTop(true);
	}
	
	
	
	public void clearActivation()
	{
		activated=false;
	
		
		// graphical
		chart=null;
		chartOdd=null;
		time=0;
	}
	
	public void activate()
	{
		if(Parameters.graphicalInterfaceBots)
			activateGraphicalInterface();
		
		activated=true;
	}
	
	public void activateGraphicalInterface()
	{
		if(Parameters.graphicalInterfaceBots)
		{
			panelWindow.removeAll();					
			panel=new JPanel();
			panel.setLayout(new BorderLayout());
			panel.setLayout(new GridLayout(2,1));
			chart=new MyChart2D();
			chart.setMaximumSize(new Dimension(100, 300));
			chart.setSize(200, 100);
			chart.setMaxPoints(Parameters.CHART_FRAMES);
			panel.add(chart);
			chartOdd=new MyChart2D();
			chartOdd.setMaximumSize(new Dimension(100, 300));
			chartOdd.setSize(200, 100);
			chartOdd.setMaxPoints(Parameters.CHART_FRAMES);
			panel.add(chartOdd);
			panel.setMinimumSize(new Dimension(200, 500));
			panel.setSize(new Dimension(200, 500));
			
			panelWindow.add(panel,BorderLayout.CENTER);
		}
	}
	
	public void updateGlobalVars()
	{
	
	}
	
	public void updateGraphicalInterface()
	{
		if(Parameters.graphicalInterfaceBots)
		{
			chart.addValue("predictValue", time, 0, Color.BLUE);
			//chart.addValue("5", time, 5.0, Color.BLACK);
			//chart.addValue("20", time, 20.0, Color.BLACK);
			//chartOdd.addValue("odd-Back",time, Utils.getOddBackFrame(rd, 0) , Color.BLUE);
			time++;
		}
	}
	

	public MessageJFrame getMsgFrame() {
		return msgjf;
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
	

	
	public void update()
	{
		writeMsg("Minutes to start :"+getMinutesToStart(), Color.BLUE);
		updateGlobalVars();
		updateGraphicalInterface();
		
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
				
				if(Parameters.simulation)
				{
					if(swing!=null)
					{
						swing.updateState();
					}
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
	public void tradeResults(RunnersData rd, int redOrGreen, int entryUpDown,
			double entryOdd, double exitOdd, double stake, double exitStake,
			double amountMade, int ticksMoved) {
		// TODO Auto-generated method stub
		
	}
	


}
