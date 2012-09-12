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

public class BotDutching extends Bot{

public int number=5;

	public static int PAST_FRAMES=240;
	
	public Color colorList[]={Color.BLUE,Color.CYAN,Color.GRAY,Color.GREEN,Color.MAGENTA,Color.ORANGE,Color.PINK,Color.DARK_GRAY,Color.RED,Color.YELLOW,Color.GRAY};
	
	public boolean activated=false;
	public MessageJFrame msgjf=null;
	
	public JPanel panelWindow=null;

	public JPanel panel=null;
	public MyChart2D chart;
	public MyChart2D chartOdd;
	int time=0;
	
	SwingFrontLine swing=null;
	
	public BotDutching(MarketData md) {
		super(md,"BotDutching - ");
		
		initialize();
	}
	
	public void initialize()
	{
		//md.addMarketChangeListener(this);
		if(Parameters.graphicalInterfaceBots)
		{
			msgjf=new MessageJFrame("BotDutching - ");
			getMsgFrame().setTitle("BotDutching - ");
						
			JPanel aux=msgjf.getBaseJpanel();
			JPanel panelaux=new JPanel();
			panelaux.setLayout(new BorderLayout());
			panelaux.add(aux,BorderLayout.EAST);
			
			panelaux.add(getPanelWindow(),BorderLayout.CENTER);
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
			chart.setMaxPoints(BotDutching.PAST_FRAMES);
			panel.add(chart);
			chartOdd=new MyChart2D();
			chartOdd.setMaximumSize(new Dimension(100, 300));
			chartOdd.setSize(200, 100);
			chartOdd.setMaxPoints(BotDutching.PAST_FRAMES);
			panel.add(chartOdd);
			panel.setMinimumSize(new Dimension(200, 500));
			panel.setSize(new Dimension(200, 500));
			
			panelWindow.add(panel,BorderLayout.CENTER);
			
			panelWindow.repaint();
		}
	}
	
	public void updateGlobalVars()
	{
	
	}
	
	public Color getColorIndex(int index)
	{
		return colorList[index%colorList.length];
	}
	
	public void updateGraphicalInterface()
	{
		if(Parameters.graphicalInterfaceBots)
		{
			//System.out.println("--------------------------");
			for(int i=0;i<getMd().getRunners().size();i++)
			{
				
				if(Utils.getOddBackFrame(getMd().getRunners().get(i), 0)<14)
				{
					chart.addValue(getMd().getRunners().get(i).getName(), time, Utils.getOddBackFrame(getMd().getRunners().get(i), 0) , getColorIndex(i));
					writeMsg(getMd().getRunners().get(i).getName()+" Odd: "+Utils.getOddBackFrame(getMd().getRunners().get(i), 0), getColorIndex(i));
				//	System.out.println( getColorIndex(i));
					
					if(Utils.isValidWindow(getMd().getRunners().get(i), BotDutching.PAST_FRAMES, 0))
					{
						int aux=Utils.getOddBackTicksVariation(getMd().getRunners().get(i), BotDutching.PAST_FRAMES, 0);
						
						chartOdd.addValue(getMd().getRunners().get(i).getName(), time, aux , getColorIndex(i));
					}
				}
			}
			
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
	
	public static void main(String[] args)  throws Exception {
		System.out.println("result:"+(11%11));
	}

}
