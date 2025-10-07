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
import categories.categories2018.cattree.Root2018;
import main.Parameters;

public class CollectNNRawBot2018 extends Bot {

	// y m d - m=0 January
	public static Calendar UNTIL_DATE= new GregorianCalendar(2011,11,7);
	
	Root2018 root=null;
	
	boolean nearActive=false;

	int framesInNear=0;
	Vector<RunnerCatData2018> rcdv=null;
	
	String fileNameIn="NNRawDataIn2018.csv";
	String fileNameOut="NNRawDataOut2018.csv";
	/**********************************************
	 * for every 128 lines in IN(inputs) file there is one output on output file
	 * 512 frames interpolated in constant 4 frames = 128 time steps  
	 */
	
	Vector<CollectSamplesInfo> csiv=null;
	
	
		
	public CollectNNRawBot2018(MarketData md) {
		super(md, "CollectNNRawData2018");
		initialize();
	}

	public void initialize()
	{
		CategoriesParameters.COLLECT=false; // dont want predict time off-set for this type of collect
		
		if(Parameters.replay_file_list_test)
		{
			fileNameIn="NNRawDataIn2018Val.csv";
			fileNameOut="NNRawDataOut2018Val.csv";
		}
		
		DataWindowsSizes2018.init();
		 
		root=new Root2018(0);
		CategoryNode.printIDs(root);
		
		csiv=new Vector<CollectSamplesInfo>();
		for(int i=0;i<root.getIdEnd();i++)
		{
			CollectSamplesInfo csi=new CollectSamplesInfo();
			csi.setCategoryId(i);
			csiv.add(csi);
		}
		
		
			
		//CategoryNode.buildDirectories(root);
		
		//Vector<CategoryNode> cnv=CategoryNode.getAncestorsById(root,500);
		
		//for(CategoryNode cn:cnv)
		//	System.out.print(cn.getPath()+"\\");
		
	}

	public void newMarket(MarketData md)
	{
	
		setMd(md);
		
	}
	
	public void writeNNRawDataIntoFiles(RunnerCatData2018 rcd)
	{
		Vector<Double[]> inputs=rcd.getHoldInputValues();
		Vector<Double> outputs=rcd.getHoldOuputValues();
		
		if(inputs==null || outputs==null)
		{
			System.out.println("Error getting outputs or inputs in writeNNRawDataIntoFiles()");
			return;
		}
		int id=rcd.getCat().get(rcd.getCat().size()-1).getIdStart();
		CollectSamplesInfo csi=csiv.get(id);
		
		if(getMd().getStart().before(UNTIL_DATE))
		{
			//System.out.println("testing");
			if(csi.getSamplesCollected()>DataWindowsSizes2018.COLLECT_EXAMPLES)
			{
				System.out.println("Reach until Data to collect data ");
				return ;
			}
				
		}
		
		System.out.println("#####################################################");
		System.out.println("About to write on "+CategoryNode.getAncestorsStringPath(rcd.getCat())+fileNameIn+" ("+rcd.getRd().getName()+")");
		System.out.println("About to write on "+CategoryNode.getAncestorsStringPath(rcd.getCat())+fileNameOut+" ("+rcd.getRd().getName()+")");
		System.out.println("#####################################################");
		
		//System.out.println("Inputs data Size : "+inputs.size() );
//		else
//			System.out.println("Less Date wrtite");
//		
		
//		else
//			System.out.println("ok collect "+ csi.getSamplesCollected()+" < "+DataWindowsSizes.COLLECT_EXAMPLES);
		
//		if(csi.getCategoryId()==id)
//			System.out.println("csi OK");
//		else
//			System.out.println("csi NOT OK");
//		
//		System.out.println("Category Id "+csi.getCategoryId()+" samples collected "+csi.getSamplesCollected()+ " last Market Collected Date " + csi.getLastSampleEventDate());
		
/*		double output=rcd.generateNNOutput();
		inputs.add(output);
	*/	
		String filePathInputs=CategoryNode.getAncestorsStringPath(rcd.getCat())+fileNameIn;
		String filePathOutputs=CategoryNode.getAncestorsStringPath(rcd.getCat())+fileNameOut;
		
		BufferedWriter inputsBuffer=null;
		BufferedWriter outputsBuffer=null;
		
		// -------------------------- inputs
		
		try {
			inputsBuffer = new BufferedWriter(new FileWriter(filePathInputs, true));
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open "+filePathInputs+" for writing");
			}
		
		if(inputsBuffer==null)
		{
			System.err.println("could not open "+filePathInputs);
			return;
		}
		
		String lines="";
		for(int x=0;x<DataWindowsSizes2018.SEGMENTS;x++)
		{
			for(int i=0;i<inputs.size();i++)
			{
				lines+= inputs.get(i)[x]+" ";
			}
			lines+="\n";
		}
		
		try {
			inputsBuffer.write(lines);
			//System.out.println("writeen "+ filePath);
			//inputsBuffer.newLine();
			inputsBuffer.flush();
		} catch (IOException e) {
			System.out.println(filePathInputs+":Error writting data to file "+filePathInputs);
			e.printStackTrace();
		}
		try {
			inputsBuffer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// ----------------------------- outputs 
		try {
			outputsBuffer = new BufferedWriter(new FileWriter(filePathOutputs, true));
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open "+filePathOutputs+" for writing");
			}
		
		if(outputsBuffer==null)
		{
			System.err.println("could not open "+filePathOutputs);
			return;
		}

		lines="";
		for(int i=0;i<outputs.size();i++)
			{
				lines+= outputs.get(i)+" ";
			}
		lines+="\n";
		
		
		try {
			outputsBuffer.write(lines);
			//System.out.println("writeen "+ filePath);
			//inputsBuffer.newLine();
			outputsBuffer.flush();
		} catch (IOException e) {
			System.out.println(filePathInputs+":Error writting data to file "+filePathOutputs);
			e.printStackTrace();
		}
		try {
			outputsBuffer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
/*		//System.out.println("Runner "+rcd.getRd().getName()+" line to file:" +filePath);
		
		String line="";
		for(Double v:inputs)
		{
			line+=(v+" ");
		}
		//System.out.println(line);
		
		BufferedWriter out=null;
		
		try {
			out = new BufferedWriter(new FileWriter(filePath, true));
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open "+filePath+" for writing");
			}
		
		if(out==null)
		{
			System.err.println("could not open "+filePath);
			return;
		}
		
		try {
			out.write(line);
			//System.out.println("writeen "+ filePath);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			System.out.println(filePath+":Error wrtting data to log file");
			e.printStackTrace();
		}
		
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		*/
		
		csi.setAnotherSample( getMd().getStart().get(Calendar.DAY_OF_MONTH)+"-"+(getMd().getStart().get(Calendar.MONTH)+1)+"-"+getMd().getStart().get(Calendar.YEAR));
		
		
	}

	//int framesMinute=0;
	//int minute=0;
	public void update()
	{
		/*int minuteAux=getMinutesToStart();
		
		if(minuteAux!=minute)
		{
			System.out.println("Frames minute : "+framesMinute);
			minute=minuteAux;
			framesMinute=0;
		}
		else
			framesMinute++;
		*/
		
		/*
		System.out.println("seconds to start : "+getSecondsToStart());
		System.out.println("frames Collected : "+getMd().getRunners().get(0).getDataFrames().size());
		
		double globalVolume=0;
		double amountBack=0;
		double amountLay=0;
		for(RunnersData rdc:getMd().getRunners())
		{
			globalVolume+=rdc.getDataFrames().get(rdc.getDataFrames().size()-1).getMatchedAmount();
			amountBack=rdc.getDataFrames().get(rdc.getDataFrames().size()-1).getAmountBack();
			amountLay=rdc.getDataFrames().get(rdc.getDataFrames().size()-1).getAmountLay();
			
		}
		System.out.println(globalVolume+","+(amountBack+amountLay)/1000);
		*/
		
		updateRunnerCategoryData();
		
		if(nearActive)
		{
			framesInNear++;
			if(framesInNear==DataWindowsSizes2018.FRAMES_TO_PREDICT)
			if(rcdv!=null && rcdv.size()>0)
			{
				//RunnerCategoryData rcd=rcdv.get(0);
				for(RunnerCatData2018 rcd:rcdv)
				{	
					if(rcd.getHoldInputValues()!=null)
					{
						System.out.println("Generatin output values for - "+rcd.getRd().getName()+" - in market : "+getMd().getName());
						rcd.holdOutputValuesNow();
						System.out.println("Vatiation ticks integral : "+rcd.getHoldOuputValues().get(0));
						System.out.println("Vatiation ticks : "+rcd.getHoldOuputValues().get(1));
						System.out.println("Vatiation ticks Max Up: "+rcd.getHoldOuputValues().get(2));
						System.out.println("Vatiation ticks Max Down: "+rcd.getHoldOuputValues().get(3));
						
						writeNNRawDataIntoFiles(rcd);
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
	
	public Vector<CollectSamplesInfo> getCollectSamplesInfo()
	{
		return csiv;
	}

}
