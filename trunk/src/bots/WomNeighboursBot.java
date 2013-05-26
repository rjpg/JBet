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
import DataRepository.Utils;
import GUI.MyChart2D;

public class WomNeighboursBot extends Bot {

	public int number=5;
	
	public RunnersData[] neighbours=new RunnersData[number];
	
	public int runnerPos=0;
	public RunnersData rd=null;
	public boolean activated=false;
	public boolean activatedNeighbours=false;
	public MessageJFrame msgjf=null;
	
	public JPanel panel=null;
	public JPanel panelWindow=null;
	public MyChart2D chart;
	public MyChart2D chartOdd;
	int time=0;
	
	public double globalWom=0;
	
	
	public WomNeighboursBot(MarketData md, int runnerPosA) {
		super(md,"WomNeighboursBot - "+runnerPosA+" - ");
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
		activatedNeighbours=false;
		
		rd=null;
		
		for(int i=0;i<number;i++)
			neighbours[i]=null;
		
		// graphical
		chart=null;
		chartOdd=null;
		time=0;
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
	
	public void updateGlobalWom()
	{
		globalWom=0;
		for(int i=0;i<number;i++)
		{
			globalWom+=Utils.getOddBackFrame(neighbours[i], 0);
		}
		globalWom/=number;
	}
	
	public void updateGraphicalInterface()
	{
		if(Parameters.graphicalInterfaceBots)
		{
			chart.addValue("predictValue", time, globalWom, Color.BLUE);
			//chart.addValue("5", time, 5.0, Color.BLACK);
			//chart.addValue("20", time, 20.0, Color.BLACK);
			chartOdd.addValue("odd-Back",time, Utils.getOddBackFrame(rd, 0) , Color.BLUE);
			time++;
		}
	}
	
	public void selectRunner()
	{
		if(getMd().getRunners().size()>runnerPos)
		{
			rd=getMd().getRunners().get(runnerPos);
			writeMsg("Runner selected: "+rd.getName(), Color.BLUE);
		}
		else
		{
			rd=null;
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
	
	public void activateNeighbours()
	{
		for(int i =0 ;i<number;i++)
		{
			 neighbours[i]=Utils.getNeighbour(md, rd,i);
			// writeMsg("activating neighbours "+neighbours[i], Color.blue);
			 if(neighbours[i]==null)
				 return;
		}
		
		activatedNeighbours=true;
	}
	
	public void update()
	{
		//if(getMinutesToStart()<=0)
		//	return;
		
		if(rd!=null)
		{
			if(!activatedNeighbours)
			{
				
				activateNeighbours();
				
				if(activatedNeighbours)
				{
					for(int i = 0 ;i<number;i++)
					{
						writeMsg("Neighbour "+i+" : "+neighbours[i].getName(), Color.BLUE);
					}
					activateGraphicalInterface();
					updateGlobalWom();
					if(Utils.getOddBackFrame(rd, 0)>4 && Utils.getOddBackFrame(rd, 0)<8 && globalWom<30)
						System.out.println("see bot :"+getName());
				}
				else
					return;
			}
			
			// process ...
			updateGlobalWom();
			updateGraphicalInterface();
			
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
