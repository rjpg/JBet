package categories.categories2018.bots;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.JFrame;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import GUI.MyChart2D;
import bots.Bot;
import categories.categories2013.CategoriesParameters;
import categories.categories2013.CategoryNode;
import categories.categories2013.bots.CollectSamplesInfo;
import categories.categories2018.DataWindowsSizes2018;
import categories.categories2018.TFManager;
import categories.categories2018.cattree.Root2018;


public class ExecuteNNBot2018 extends Bot {

	
	Root2018 root=null;
	
	boolean nearActive=false;

	int framesInNear=0;
	
	boolean outputProcessed=false;
	boolean inputProcessed=false;
	
	Vector<RunnerCatData2018> rcdv=null;
		
	public ExecuteNNBot2018(MarketData md) {
		super(md, "ExecuteNNData2018");
		initialize();
	}

	public void initialize()
	{
		CategoriesParameters.COLLECT=false; // dont want predict time off-set for this type of collect
	
		DataWindowsSizes2018.init();
		 
		root=new Root2018(0);
		CategoryNode.printIDs(root);	
		
		TFManager.initialize();
	}

	public void newMarket(MarketData md)
	{
	
		if(inputProcessed==true && outputProcessed==false)
		{
			System.out.println("###########################################");
			System.out.println("###########################################");
			System.out.println("###########################################");
			System.out.println("###### Race data ende before expected #####");
			//getMd().pause=true;

		}
		setMd(md);
		
	}
	
	public void update()
	{

		updateRunnerCategoryData();
		
		if(nearActive)
		{
			framesInNear++;
			if(framesInNear==DataWindowsSizes2018.FRAMES_TO_PREDICT)
			if(rcdv!=null && rcdv.size()>0)
			{
				System.out.println("##### Pre-live is ending closing all positions  ######");
				//RunnerCategoryData rcd=rcdv.get(0);
				for(RunnerCatData2018 rcd:rcdv)
				{	
					
					
					if(rcd.getHoldInputValues()!=null)
					{
						/*System.out.println("Generatin output values for - "+rcd.getRd().getName()+" - in market : "+getMd().getName());
						rcd.holdOutputValuesNow();
						System.out.println("Vatiation ticks integral : "+rcd.getHoldOuputValues().get(0));
						System.out.println("Vatiation ticks : "+rcd.getHoldOuputValues().get(1));
						System.out.println("Vatiation ticks Max Up: "+rcd.getHoldOuputValues().get(2));
						System.out.println("Vatiation ticks Max Down: "+rcd.getHoldOuputValues().get(3));
						Vector<Double> outputs = rcd.getHoldOuputValues();
						
						String line="";
						for(int i=0;i<outputs.size();i++)
							{
								line+= outputs.get(i)+" ";
							}
						
						if(rcd.TFsession!=null)
							rcd.writeLogStringToFile(line);*/
						//outputProcessed=true;
						//getMd().pause=true;
					}
					
				}
			}
			
		}
		
		

	}
	
	//pathroot\shortLenhgt\favorite\beginingDay\mediumRunners\farFromBegining\lowOdd\highLiquidity\
	
	public void reset()
	{
		if(rcdv!=null)
		{
			rcdv.removeAllElements();
			rcdv.clear();
			rcdv=null;
			rcdv=new Vector<RunnerCatData2018>();
		}	
		
		outputProcessed=false;
		inputProcessed=false;
		
		framesInNear=0;
		nearActive=false;
		
	}
	
	public void updateRunnerCategoryData()
	{
		
		if(! Utils.isValidWindow(getMd().getRunners().get(0), CategoriesParameters.FRAMES_PREDICTION+2, 0))
			return;
		
		

		int minuteToStart =getMinutesToStart();
	
		
		if(!nearActive && (minuteToStart<=(1 ) && minuteToStart>=(0)))
		{
			nearActive=true;
			
			framesInNear=0;
			System.out.println("activating near");
			
		
			if(rcdv!=null)
			{
				rcdv.removeAllElements();
				rcdv.clear();
				rcdv=null;
				
			}	
			
			rcdv=new Vector<RunnerCatData2018>();
			
			for(RunnersData rd:getMd().getRunners())
			{
				Vector<CategoryNode> cat=CategoryNode.getAncestorsByRunner(root, rd);
				if(cat==null)
					System.out.println(rd.getName()+" has no category");
				else
				{
					System.out.println(rd.getName()+" category id (start):"+cat.get(cat.size()-1).getIdStart()+" (end):"+cat.get(cat.size()-1).getIdEnd()+" path : "+CategoryNode.getAncestorsStringPath(cat));
					rcdv.add(new RunnerCatData2018(rd, cat));
				}
				
			}
			
			if(rcdv!=null && rcdv.size()>0)
			{
				//RunnerCategoryData rcd=rcdv.get(0);
				for(RunnerCatData2018 rcd:rcdv)
				{	
					System.out.println("holding values for - "+rcd.getRd().getName()+" - in Marker : "+rcd.getRd().getMarketData().getEventName());
					System.out.println("Category : "+CategoryNode.getAncestorsStringPath(rcd.getCat()));
					
					
					if(rcd.holdInputValuesNow()!=null)
					{
						inputProcessed=true;
						if(rcd.predict()>=0)
						{
							System.out.println("################## PREDICTION MADE##########");
							rcd.executePrediction();
							//getMd().pause=true;
						}
						else
							System.out.println("################## PREDICTION NOT MADE##########");
						
						//plotNNinputs(rcd);
						//getMd().pause=true;
					}
					
				}
			}
		}
		
	}
	
	void plotNNinputs(RunnerCatData2018 rcd)
	{
		JFrame frame=new JFrame(rcd.getRd().getName()+" - indicator 1");
		MyChart2D chart=new MyChart2D();
		//chart.setForceXRange(new Range(50.1, 50));
		chart.setDecimalsX(0);
		frame.add(chart);
		
		Vector<Double[]> indicators = rcd.getHoldInputValues();
		
		int lenght=512;
		
		int ref=Utils.oddToIndex(Utils.getOddLayFrame(rcd.getRd(), lenght));
		
		for(int i=0;i<lenght;i++)
		{
			
			chart.addValue("zero", i,0, Color.YELLOW);
			chart.addValue("-1", i,-1, Color.BLACK);
			chart.addValue("1", i,1, Color.BLACK);
			//chart.addValue("odd", i,Utils.getOddLayFrame(rcd.getRd(), i), Color.CYAN);
			//chart.addValue("odd-index", i,Utils.oddToIndex(Utils.getOddLayFrame(rcd.getRd(), i))-ref, Color.RED);
		}
		
		
		for(int i=0;i<lenght/4 /*indicators.get(0).length/2*/;i++)
		{
			chart.addValue("indicator 5", i*4,indicators.get(5)[i], Color.GREEN);
			
			chart.addValue("indicator 8", i*4,indicators.get(8)[i], Color.BLUE);
			
			//chart.addValue("indicator 0", i*4,indicators.get(0)[i], Color.BLUE);
			//chart.addValue("indicator 6", i*4,indicators.get(6)[i], Color.ORANGE);
			//chart.addValue("indicator 2", i*4,indicators.get(2)[i], Color.PINK);
			//chart.addValue("indicator 3", i*4,indicators.get(3)[i], Color.DARK_GRAY);
			
			//chart.addValue("indicator 7", i*4,indicators.get(7)[i], Color.BLUE);
			//chart.addValue("indicator 4", i*4,indicators.get(4)[i], Color.BLACK);
			
			//System.out.println("wom others :"+indicators.get(8)[i]);
		}
		
		
		
		frame.setSize(1000, 500);
		frame.setVisible(true);
		
		
	}
	
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketNew)
		{
			newMarket(md);
			reset();
		}
		
		if(marketEventType==MarketChangeListener.MarketUpdate)
			update();			
		
	}

	@Override
	public void writeMsg(String s, Color c) {
		System.out.println("Bot "+getName()+" Msg :"+s);
		
	}
	

}
