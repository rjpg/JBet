package bots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import main.Parameters;
import correctscore.MessageJFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.SwingFrontLine;
import DataRepository.Utils;
import GUI.MyChart2D;

public class NeighboursCorrelationBot extends Bot{

	//public int number=5;

	public static int CORRELATION_WINDOW=5;

	public static int NEIGHBOUR_LIMIT_TICKS=100;
	
	public static int MINUTES_TO_MAKE_CONCLUSIONS=1;
	
	public static boolean INCONCLUSIVE=true;
	
	public static boolean SYMMETRY=false;
	
	public static boolean USE_GLOBAL_WINDOW=true;
	
	public static int GLOBAL_WINDOW_SIZE=120;
	
	
	public boolean timePassed=false;
	
	public boolean activated=false;
	public MessageJFrame msgjf=null;
	
	public JPanel panelWindow=null;

	public JPanel panel=null;
	public MyChart2D chart;
	public MyChart2D chartOdd;
	int time=0;
	
	public int nRunners=0;
	
	public int neighboursMatrix[][];
	public JLabel neighboursMatrixLabels[][];
	public JPanel labelsPanel=new JPanel();
	
	SwingFrontLine swing=null;
	
	private Calendar calendarStart=null; 
	
	public NeighboursCorrelationBot(MarketData md) {
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
			panelaux.setLayout(new GridLayout(1,1));
			//panelaux.add(aux);
			
			getPanelWindow();
			//panelaux.add(getPanelWindow());
			panelaux.add(getLabelsPanel());
		
			msgjf.setContentPane(panelaux);
			
			//msgjf.getBaseJpanel().add(getActionsPanel(),BorderLayout.SOUTH);
			msgjf.setSize(300,200);
			msgjf.setVisible(true);
		}
		//msgjf.setAlwaysOnTop(true);
	}
	
	
	
	public void clearActivation()
	{
		if(Parameters.graphicalInterfaceBots)
		{
		for(int i=0;i<nRunners;i++)
			for(int x=0;x<nRunners;x++)
			{
				neighboursMatrixLabels[i][x]=null;
			}
		}
		calendarStart=null;
		
		neighboursMatrixLabels=null;
		activated=false;
		nRunners=0;
		 neighboursMatrix=null;
		
		// graphical
		chart=null;
		chartOdd=null;
		time=0;
		timePassed=false;
		
	}
	
	
	
	
	public void activate()
	{
		int number=getMd().getRunners().size();
		if(number>0)
		{
			nRunners=number;
			neighboursMatrix=new int[nRunners][nRunners];
			
		}
		else
		{
			return;
		}
		
		calendarStart=(Calendar) getMd().getCurrentTime().clone();
		activateGraphicalInterface();
		activated=true;
	}
	
	public void activateGraphicalInterface()
	{
		if(Parameters.graphicalInterfaceBots)
		{
			panelWindow.removeAll();
			labelsPanel.removeAll();
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
			
			
			labelsPanel.setLayout(new GridLayout(nRunners+1,nRunners+1));
			neighboursMatrixLabels=new JLabel[nRunners+1][nRunners+1];
			
			
			
			for(int i=0;i<nRunners+1;i++)
				for(int x=0;x<nRunners+1;x++)
				{
					neighboursMatrixLabels[i][x]=new JLabel();
					neighboursMatrixLabels[i][x].setBorder(new LineBorder(Color.blue, 1));
					neighboursMatrixLabels[i][x].setHorizontalAlignment(JLabel.CENTER);
					neighboursMatrixLabels[i][x].setOpaque(true);
					labelsPanel.add(neighboursMatrixLabels[i][x]);
				}
			
			
			getMsgFrame().setTitle("NeighboursCorrelationBot - "+getMd().getEventName()+" - "+getMd().getName());
			
		}
	}
	
//	public void updateGlobalVars()
//	{
//		for(int i=0;i<nRunners;i++)
//			for(int x=0;x<nRunners;x++)
//			{
//				RunnersData rdi=getMd().getRunners().get(i);
//				RunnersData rdx=getMd().getRunners().get(x);
//			
//				int lastFrameMoved= Utils.getOddBackLastFrameMove(rdi);
//				if(lastFrameMoved!=-1)
//				{
//					//writeMsg("Runner last move:"+tick, Color.BLUE);
//					//writeMsg("Runner last direction:"+Utils.getOddBackDirection(rdi, window,tick), Color.BLUE);
//					if(lastFrameMoved<(window*2))
//					{
//						if(i!=x)
//						{
//						int di=Utils.getOddBackDirection(rdi, window,lastFrameMoved);
//						int dx=Utils.getOddBackDirection(rdx, lastFrameMoved,0);
//						if(di>0 && dx>0)
//							neighboursMatrix[i][x]--;
//						
//						if(di>0 && dx<0)
//							neighboursMatrix[i][x]++;
//						
//						if(di<0 && dx>0)
//							neighboursMatrix[i][x]++;
//						
//						if(di<0 && dx<0)
//							neighboursMatrix[i][x]--;
//						
//						
//						//if(dx!=0 && di!=0)
//						//	neighboursMatrix[i][x]+=di-dx;
//						}
//					}
//				}
//
//				
//			}
//		
//		
//	}
	
	public void updateGlobalVars()
	{
		for(int i=0;i<nRunners;i++)
			for(int x=0;x<nRunners;x++)
			{
				RunnersData rdi=getMd().getRunners().get(i);
				RunnersData rdx=getMd().getRunners().get(x);
			
				//int lastFrameMoved= Utils.getOddBackLastFrameMove(rdi);
				//if(lastFrameMoved!=-1)
				//{
					//writeMsg("Runner last move:"+tick, Color.BLUE);
					//writeMsg("Runner last direction:"+Utils.getOddBackDirection(rdi, window,tick), Color.BLUE);
				//	if(lastFrameMoved<(CORRELATION_WINDOW*2))
				//	{
						if(i!=x)
						{
							
							int di=0;
							int dx=0;
							if(SYMMETRY)
							{
								di=Utils.getOddBackDirection(rdi, CORRELATION_WINDOW,0);
								dx=Utils.getOddBackDirection(rdx, CORRELATION_WINDOW,0);
							}
							else
							{
								di=Utils.getOddBackDirection(rdi, CORRELATION_WINDOW,CORRELATION_WINDOW);
								dx=Utils.getOddBackDirection(rdx, CORRELATION_WINDOW,0);
							}
						
						if(di!=0 && dx!=0 && Utils.getTicksDiff(rdi, rdx)< NEIGHBOUR_LIMIT_TICKS)
						{
						
							if(di>0 && dx>0)
								neighboursMatrix[i][x]+=-di-dx;
							
							if(di>0 && dx<0)
								neighboursMatrix[i][x]+=di-dx;
							
							if(di<0 && dx>0)
								neighboursMatrix[i][x]+=-di+dx;
							
							if(di<0 && dx<0)
								neighboursMatrix[i][x]+=di+dx;
						}
						
						if(USE_GLOBAL_WINDOW && Utils.isValidWindow(rdi, CORRELATION_WINDOW*2, GLOBAL_WINDOW_SIZE))
						{
							
							if(SYMMETRY)
							{
								di=Utils.getOddBackDirection(rdi, CORRELATION_WINDOW,GLOBAL_WINDOW_SIZE);
								dx=Utils.getOddBackDirection(rdx, CORRELATION_WINDOW,GLOBAL_WINDOW_SIZE);
							}
							else
							{
								di=Utils.getOddBackDirection(rdi, CORRELATION_WINDOW,CORRELATION_WINDOW+GLOBAL_WINDOW_SIZE);
								dx=Utils.getOddBackDirection(rdx, CORRELATION_WINDOW,GLOBAL_WINDOW_SIZE);
							}
						
						if(di!=0 && dx!=0 && Utils.getTicksDiff(rdi, rdx,GLOBAL_WINDOW_SIZE)< NEIGHBOUR_LIMIT_TICKS)
						{
						
							if(di>0 && dx>0)
								neighboursMatrix[i][x]-=-di-dx;
							
							if(di>0 && dx<0)
								neighboursMatrix[i][x]-=di-dx;
							
							if(di<0 && dx>0)
								neighboursMatrix[i][x]-=-di+dx;
							
							if(di<0 && dx<0)
								neighboursMatrix[i][x]-=di+dx;
						}
					}
				}
							
			}
		
		
	}
	public void updateGraphicalInterface()
	{
		if(Parameters.graphicalInterfaceBots)
		{
			chart.addValue("predictValue", time, 0, Color.BLUE);
			//chart.addValue("5", time, 5.0, Color.BLACK);
			//chart.addValue("20", time, 20.0, Color.BLACK);
			chartOdd.addValue("odd-Back",time,0 , Color.BLUE);
			
			time++;
			
			int min=10000;
			int max=-10010;
			
			for(int i=0;i<nRunners;i++)
				for(int x=0;x<nRunners;x++)
				{
					neighboursMatrixLabels[i][x].setText("i="+i+";x="+x+";"+neighboursMatrix[i][x]);
					if(min>neighboursMatrix[i][x])
						min=neighboursMatrix[i][x];
					
					if(max<neighboursMatrix[i][x])
						max=neighboursMatrix[i][x];
					
				}
			
		
			int dist=max-min;
			
			for(int i=0;i<nRunners;i++)
			{
				//int minlinemin=neighboursMatrix[i][0];
				//int indexmin=0;
				
				int minlinemax=neighboursMatrix[i][0];
				int indexmax=0;
				
				int minlinemaxgreen=neighboursMatrix[i][0];
				int indexmaxgreen=0;
				                                
				for(int x=0;x<nRunners;x++)
				{
					if(x!=i)
					{
						int aux=neighboursMatrix[i][x]-min;
						Color color=new Color(0, 0, 0);
						if(dist!=0)
						{
							int c=(aux*255)/dist;
							color=new Color(0, 0, c);
						}
						neighboursMatrixLabels[i][x].setBackground(color);
						neighboursMatrixLabels[i][x].setBorder(new LineBorder(Color.blue, 1));
						
						//if(neighboursMatrix[i][x]<minlinemin)
						//{
						//	indexmin=x;
						//	minlinemin=neighboursMatrix[i][x];
						//}
						
						if(neighboursMatrix[i][x]>minlinemax)
						{
							indexmax=x;
							minlinemax=neighboursMatrix[i][x];
						}
						
						if(neighboursMatrix[i][x]>minlinemaxgreen)
							if(Utils.getOddBackFrame(getMd().getRunners().get(x), 0)<20)
							{
								indexmaxgreen=x;
								minlinemaxgreen=neighboursMatrix[i][x];
							} 
					}
					else
					{
						neighboursMatrixLabels[i][x].setBackground(Color.darkGray);
						neighboursMatrixLabels[i][x].setBorder(new LineBorder(Color.darkGray, 1));
						neighboursMatrixLabels[i][x].setText("");
					}
					
				}
				//neighboursMatrixLabels[i][indexmin].setBorder(new LineBorder(Color.RED, 2));
				neighboursMatrixLabels[i][indexmax].setBorder(new LineBorder(Color.ORANGE, 2));
				if(Utils.getOddBackFrame(getMd().getRunners().get(i), 0)<20 && Utils.getOddBackFrame(getMd().getRunners().get(indexmaxgreen), 0)<20)
					neighboursMatrixLabels[i][indexmaxgreen].setBorder(new LineBorder(Color.GREEN, 2));
			}
			
			for(int i=0;i<nRunners;i++)
			{
				
				neighboursMatrixLabels[nRunners][i].setText(getMd().getRunners().get(i).getName());
				
				neighboursMatrixLabels[i][nRunners].setText(getMd().getRunners().get(i).getName());
				
				if(Utils.getOddBackFrame(getMd().getRunners().get(i), 0)<20)
				{
					neighboursMatrixLabels[i][nRunners].setBackground(Color.GREEN);
					neighboursMatrixLabels[nRunners][i].setBackground(Color.GREEN);
				}
				else
				{
					neighboursMatrixLabels[i][nRunners].setBackground(Color.RED);
					neighboursMatrixLabels[nRunners][i].setBackground(Color.RED);
				}
			}
			
		}
	}
	
	public void updateTime()
	{
		Calendar now =(Calendar) getMd().getCurrentTime().clone();
		Calendar startCopy=(Calendar) calendarStart.clone();
		startCopy.add(Calendar.MINUTE,this.MINUTES_TO_MAKE_CONCLUSIONS);
		if(startCopy.compareTo(now)<0)
		{
			timePassed=true;
		}
	}
	
	public RunnersData getInfluenciedRunner(RunnersData rd)
	{
		if(calendarStart==null)
			return null;
		
		
		if(!timePassed)
		{
			//System.out.println("Not enoughf time to make conclusions");
			return null;
		}
			
		int i=0;
		for(RunnersData rdaux:getMd().getRunners())
		{	
			if(rdaux==rd)
			{
				break;
			}
			i++;
		}
		
		
		int minlinemax=neighboursMatrix[i][0];
		int indexmax=0;
	
		for(int x=0;x<nRunners;x++)
		{
			if(x!=i)
			{
				if(neighboursMatrix[i][x]>minlinemax)
				{
					indexmax=x;
					minlinemax=neighboursMatrix[i][x];
				}
			}
		}
		return getMd().getRunners().get(indexmax);
	}
	
	public RunnersData getInfluenceRunner(RunnersData rd)
	{
		if(calendarStart==null)
			return null;
		
		if(!timePassed)
		{
			//System.out.println("Not enoughf time to make conclusions");
			return null;
		}
			
		int i=0;
		for(RunnersData rdaux:getMd().getRunners())
		{	
			if(rdaux==rd)
			{
				break;
			}
			i++;
		}
		
		
		int minlinemax=neighboursMatrix[i][0];
		int indexmax=0;
	
		for(int x=0;x<nRunners;x++)
		{
			if(x!=i)
			{
				if(neighboursMatrix[x][i]>minlinemax)
				{
					indexmax=x;
					minlinemax=neighboursMatrix[x][i];
				}
			}
		}
		return getMd().getRunners().get(indexmax);
	}
	
	public RunnersData getInfluenciedRunnerOddUnder(RunnersData rd,double odd)
	{
		if(calendarStart==null)
			return null;
		
		if(!timePassed)
		{
			//System.out.println("Not enoughf time to make conclusions");
			return null;
		}
			
		int i=0;
		for(RunnersData rdaux:getMd().getRunners())
		{	
			if(rdaux==rd)
			{
				break;
			}
			i++;
		}
		
		
		int minlinemax=neighboursMatrix[i][0];
		int indexmax=0;
	
		for(int x=0;x<nRunners;x++)
		{
			if(x!=i)
			{
				if(Utils.getOddLayFrame(getMd().getRunners().get(x), 0)<odd && neighboursMatrix[i][x]>minlinemax )
				{
					indexmax=x;
					minlinemax=neighboursMatrix[i][x];
				}
			}
		}
		if(Utils.getOddBackFrame(getMd().getRunners().get(indexmax), 0)<20)
			return getMd().getRunners().get(indexmax);
		else
			return null;
	}
	
	
	public RunnersData getInfluenceRunnerOddUnder(RunnersData rd,double odd)
	{
		if(calendarStart==null)
			return null;
		
		if(!timePassed)
		{
			//System.out.println("Not enoughf time to make conclusions");
			return null;
		}
			
		int i=0;
		for(RunnersData rdaux:getMd().getRunners())
		{	
			if(rdaux==rd)
			{
				break;
			}
			i++;
		}
		
		
		int minlinemax=neighboursMatrix[i][0];
		int indexmax=0;
	
		for(int x=0;x<nRunners;x++)
		{
			if(x!=i)
			{
				if(Utils.getOddLayFrame(getMd().getRunners().get(x), 0)<odd && neighboursMatrix[x][i]>minlinemax )
				{
					indexmax=x;
					minlinemax=neighboursMatrix[x][i];
				}
			}
		}
		if(Utils.getOddBackFrame(getMd().getRunners().get(indexmax), 0)<20)
			return getMd().getRunners().get(indexmax);
		else
			return null;
	}
	
	public RunnersData getInfluenciedRunnerTickNear(RunnersData rd,int tickNear)
	{
		if(calendarStart==null)
			return null;
		
		if(!timePassed)
		{
			//System.out.println("Not enoughf time to make conclusions");
			return null;
		}
			
		int i=0;
		for(RunnersData rdaux:getMd().getRunners())
		{	
			if(rdaux==rd)
			{
				break;
			}
			i++;
		}
		
		
		
		int minlinemax=neighboursMatrix[i][0];
		int indexmax=0;
	
		for(int x=0;x<nRunners;x++)
		{
			if(x!=i)
			{
				if(Utils.getTicksDiff(rd,getMd().getRunners().get(x))<tickNear && neighboursMatrix[i][x]>minlinemax )
				{
					indexmax=x;
					minlinemax=neighboursMatrix[i][x];
				}
			}
		}
		if(getMd().getRunners().get(indexmax)!= rd)
			return getMd().getRunners().get(indexmax);
		else
			return null;
	}
	
	public int getInfluenceFactor(RunnersData rdA,RunnersData rdB)
	{
		int i=0;
		
		if(rdA==null||rdB==null)
			return 0;
		
		for(RunnersData rdaux:getMd().getRunners())
		{	
			if(rdaux==rdA)
			{
				break;
			}
			i++;
		}
		
		int x=0;
		for(RunnersData rdaux:getMd().getRunners())
		{	
			if(rdaux==rdB)
			{
				break;
			}
			x++;
		}
		
		return neighboursMatrix[i][x];
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
	
	public JPanel getLabelsPanel()
	{
		if(labelsPanel==null)
		{
			labelsPanel=new JPanel();
			labelsPanel.setLayout(new BorderLayout());
			
		}
		return labelsPanel;
	}
	

	
	public void update()
	{
		updateTime();
		if(!timePassed)
			updateGlobalVars();
		else if(INCONCLUSIVE)
			updateGlobalVars();
		
		
		
		//System.out.println(getMd().getRunners().get(0)+" influenciate "+getInfluenceRunnerOddUnder(getMd().getRunners().get(0),20.)+
		//	"----- influence Factor:"+ getInfluenceFactor(getMd().getRunners().get(0),getInfluenceRunnerOddUnder(getMd().getRunners().get(0),20.)));
		
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
