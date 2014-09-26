package categories.categories2013.bots;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.encog.ml.data.MLData;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.junit.experimental.categories.Categories;

import bets.BetData;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.swing.Swing;
import TradeMechanisms.swing.SwingOptions;
import TradeMechanisms.trailingStop.TrailingStop;
import TradeMechanisms.trailingStop.TrailingStopOptions;
import DataRepository.RunnersData;
import DataRepository.Utils;
import categories.categories2011.CategoriesManager;
import categories.categories2013.CategoriesParameters;
import categories.categories2013.CategoryNode;
import categories.categories2013.Liquidity;

public class RunnerCategoryData implements TradeMechanismListener{

	Vector<CategoryNode> cat=null;
	
	RunnersData rd;
	
	RunnersData neighbour;
	
	int axisSize=3;
	
	// execute Model data
	public static int PREDICT_NO_DATA_ERROR=-1;
	public static int PREDICT_COLLECT_ERROR=-2;
	public static int PREDICT_NO_MODEL_ERROR=-3;
	
	public static int PREDICT_ZERO=0;
	public static int PREDICT_WEEAK_UP=1;
	public static int PREDICT_WEEAK_DOWN=2;
	public static int PREDICT_STRONG_UP=3;
	public static int PREDICT_STRONG_DOWN=4;
	
	public static String SUFIX_MODEL="F";
	double[][] minmax=null;
	BasicNetwork network=null;
	
	public Vector<Double> votes=new Vector<Double>();
	public static int NUMBER_OF_VOTES=30;
	
	public static boolean TRADE_AT_BEST_PRICE=true;
	
	public RunnerCategoryData(RunnersData rdA,Vector<CategoryNode> catA) {
		rd=rdA;
		cat=catA;
		
		if(CategoriesParameters.COLLECT)
		{
			System.out.println("favorite : "+cat.get(6).getPath());
			if(Utils.isValidWindow(rd, CategoriesParameters.FRAMES_PREDICTION, 0))
			{
				if(cat.get(2).getPath().equals("nofavorite"))
				{
					neighbour=Utils.getNeighbour(rd,CategoriesParameters.FRAMES_PREDICTION);
					System.out.println("no favorite : "+neighbour.getName());
					
				}
				else
				{
					if(cat.get(6).getPath().equals("lowOdd"))
						neighbour=Utils.getNeighbour(rd,CategoriesParameters.FRAMES_PREDICTION);
					else
						neighbour=Utils.getFavorite(rd.getMarketData(),CategoriesParameters.FRAMES_PREDICTION);
					
					System.out.println("has favorite : "+neighbour.getName());
				}
			}
			else
			{
				System.out.println("RunnerCategoryData - No Vald window to get neighbour - "+ rd.getName() );
				
			}
		}
		else
		{
			System.out.println("favorite : "+cat.get(6).getPath());
			
			if(cat.get(2).getPath().equals("nofavorite"))
			{
				neighbour=Utils.getNeighbour(rd);
				System.out.println("no favorite : "+neighbour.getName());
				
			}
			else
			{
				if(cat.get(6).getPath().equals("lowOdd"))
					neighbour=Utils.getNeighbour(rd);
				else
					neighbour=Utils.getFavorite(rd.getMarketData());
				
				System.out.println("has favorite : "+neighbour.getName());
			}
			
		}
		
		if(cat.get(6).getPath().equals("lowOdd"))
		{
			axisSize=4;
		}
		if(cat.get(6).getPath().equals("midleOdd"))
		{
			axisSize=3;
		}
		if(cat.get(6).getPath().equals("highOdd"))
		{
			axisSize=2;
		}
		
		loadExecutionModelData();
	
	}
	
	public static BufferedReader getBufferedReader(File f)
	{
		BufferedReader input=null;
		try {
			input= new BufferedReader(new FileReader(f));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return input;
	}
	
	public int loadMinMaxValues()
	{
		
		String fileName=CategoryNode.getAncestorsStringPath(cat)+"NNMinMax.csv";
		File f=new File(fileName);
		
		if(f.exists()) { 
		
			BufferedReader input=getBufferedReader(f);
			String s=null;
			
			 minmax=new double[DataWindowsSizes.INPUT_NEURONS+2][2];
			
			
			try {
				for(int i=0;i<DataWindowsSizes.INPUT_NEURONS+2;i++)
				{
					s=input.readLine();
					String sa[]=s.split(" ");
					minmax[i][0]=Double.parseDouble(sa[0]);
					minmax[i][1]=Double.parseDouble(sa[1]);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				input.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}else
		{
			System.out.println("File does not exists : "+fileName);
			return -1;
		}
		return 0;
	}
	
	public int loadNN()
	{
		String fileName=CategoryNode.getAncestorsStringPath(cat)+"nn-"+RunnerCategoryData.SUFIX_MODEL+".eg";
		File networkFile=new File(fileName);
		
		if(networkFile.exists()) { 
			network=(BasicNetwork) EncogDirectoryPersistence.loadObject(networkFile);
		}
		else
		{
			System.out.println("File does not exists : "+fileName);
			return -1;
		}
		return 0;
	}
	
	public void loadExecutionModelData()
	{
		String catPath=CategoryNode.getAncestorsStringPath(cat);
		if(!CategoriesParameters.COLLECT)
		{
			System.out.println("Loading model execution data for : "+catPath);
			System.out.println("Loading normalization data for : "+catPath);
			if(loadMinMaxValues()==-1)
			{
				System.out.println("Error Loading normalization data for : "+catPath);
				return;
			}
			System.out.println("Loading NN for : "+catPath);
			if(loadNN()==-1)
			{
				System.out.println("Error NN for : "+catPath);
				return;
			}
			System.out.println("Loading model complete for : "+catPath);
//			for(int i=0;i<minmax.length;i++)
//			{
//				System.out.println("minmax["+i+"][0]="+minmax[i][0]+"     minmax["+i+"][1]="+minmax[i][1]    );
//			}
			
		}
		else
		{
			System.out.println("Not Loading model execution data : Running in Collect Data Mode");
		}
	}
	
	public int predict()
	{
		if(CategoriesParameters.COLLECT)
			return PREDICT_COLLECT_ERROR;
		if(network== null || minmax == null)
			return PREDICT_NO_MODEL_ERROR;
		Vector<Double> rawInputs=generateNNInputs();
		if(rawInputs==null)
			return PREDICT_NO_DATA_ERROR;
		
		
		
		Double rawExample[]=rawInputs.toArray(new Double[]{});
		double normalExample[]=new double[DataWindowsSizes.INPUT_NEURONS];
		for(int i=0;i<DataWindowsSizes.INPUT_NEURONS;i++)
		{		
			normalExample[i]=ProcessNNRawData.normalizeValue(rawExample[i], minmax[i][0], minmax[i][1]);
			//System.out.println("Normalize["+i+"]="+rawInputs.get(i)+" with ("+minmax[i][0]+","+minmax[i][1]+") = "+normalExample[i]);
		}
		
		double[] out =new double[1];
		//System.out.println("Number of values to compute:"+inputValues.length+"  input count:"+network.getInputCount());
			
		MLData pair=new BasicNeuralData(normalExample);
		
		MLData output = network.compute(pair);
		out=output.getData();
				
		System.out.println("predict Value : "+out[0]+" in Ticks : "+deNormalizeOutput(out[0],minmax[DataWindowsSizes.INPUT_NEURONS][1]));
		double value = deNormalizeOutput(out[0],minmax[DataWindowsSizes.INPUT_NEURONS][1]);
		
		double min=minmax[DataWindowsSizes.INPUT_NEURONS+1][0];
		double max=minmax[DataWindowsSizes.INPUT_NEURONS+1][1];
		
		int ret=0;
		if(value>-min && value<min)
			ret=PREDICT_ZERO;
		else if(value>=min && value<=max)
			ret=PREDICT_WEEAK_UP;
		else if(value>max)
			ret=PREDICT_STRONG_UP;
		else if(value>=-max && value<=-min)
			ret=PREDICT_WEEAK_DOWN;
		else if(value<-max)
			ret=PREDICT_STRONG_DOWN;
	
		votes.add(value);
		
		if(votes.size()>NUMBER_OF_VOTES)
		{
			votes.remove(0);
		}
		
		System.out.println("min : "+min+" max : "+max+"  predict : "+ret+" vector votes :"+votes);
		
		return ret;
	}
	
	public double [] getOuputIntervals()
	{
		return minmax[DataWindowsSizes.INPUT_NEURONS+1];
	}
	
	public void executePredictions()
	{
		if(votes.size()<NUMBER_OF_VOTES)
			return;
		
		double avgVotes=0;
		for(double x:votes)
		{
			avgVotes+=x;
		}
		
		avgVotes=avgVotes/(double)votes.size();
		
		int value=(int)(avgVotes+0.5);
		
		double min=minmax[DataWindowsSizes.INPUT_NEURONS+1][0];
		double max=minmax[DataWindowsSizes.INPUT_NEURONS+1][1];
		
		System.out.println("runner : "+rd.getName()+"min : "+min+" max : "+max+"  predict : "+value);
		if(value>=min && value<=max)
			swingUp();
		else if(value>max)
			trailUp();
		else if(value>=-max && value<=-min)
			swingDown();
		else if(value<-max)
			trailDown();
		
	}
	
	public void swingUp()
	{
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE)
			entryOdd=Utils.getOddBackFrame(rd, 0);
		else
			entryOdd=Utils.getOddLayFrame(rd, 0);
			
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd)-1);
		
		BetData betOpen=new BetData(rd,
				3.00,
				entryOdd,
				BetData.LAY,
				false);
		
		SwingOptions so=new SwingOptions(betOpen, this);
		so.setWaitFramesOpen(40);      // 0.75 minute 1,5
		so.setWaitFramesNormal(80);   //2.25- 3 minutes
		so.setWaitFramesBestPrice(30);  // 0.75 - 1.5 minute
		so.setTicksProfit((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][1]);
		so.setTicksLoss((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][0]+2);
		so.setForceCloseOnStopLoss(false);
		so.setInsistOpen(false);
		so.setGoOnfrontInBestPrice(false);
		so.setUseStopProfifInBestPrice(true);
		so.setPercentageOpen(0.80);   // if 80% is open go to close  
		so.setDelayBetweenOpenClose(-1);
		so.setDelayIgnoreStopLoss(-1);
		so.setUpdateInterval(TradeMechanism.SYNC_MARKET_DATA_UPDATE);
		
			
		Swing swing=new Swing(so);
		System.out.println("Swing Started - going to state EXECUTING_SWING");	
	
	}
	
	public void swingDown()
	{
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE)
			entryOdd=Utils.getOddLayFrame(rd, 0);
		else
			entryOdd=Utils.getOddBackFrame(rd, 0);
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd)+1);
		
		BetData betOpen=new BetData(rd,
				3.00,
				entryOdd,
				BetData.BACK,
				false);
		
		
		
		SwingOptions so=new SwingOptions(betOpen, this);
		so.setWaitFramesOpen(40);      // 0.75 minute 1,5
		so.setWaitFramesNormal(80);   //2.25- 3 minutes
		so.setWaitFramesBestPrice(30);  // 0.75 - 1.5 minute
		so.setTicksProfit((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][1]);
		so.setTicksLoss((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][0]+2);
		so.setForceCloseOnStopLoss(false);
		so.setInsistOpen(false);
		so.setGoOnfrontInBestPrice(false);
		so.setUseStopProfifInBestPrice(true);
		so.setPercentageOpen(0.80);   // if 80% is open go to close  
		so.setDelayBetweenOpenClose(-1);
		so.setDelayIgnoreStopLoss(-1);
		so.setUpdateInterval(TradeMechanism.SYNC_MARKET_DATA_UPDATE);
		
			
		Swing swing=new Swing(so);
		System.out.println("Swing Started - going to state EXECUTING_SWING");	
	}
	
	public void trailUp()
	{
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE)
			entryOdd=Utils.getOddBackFrame(rd, 0);
		else
			entryOdd=Utils.getOddLayFrame(rd, 0);
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd)-1);
		
		BetData betOpen=new BetData(rd,
				3.00,
				entryOdd,
				BetData.LAY,
				false);
		
		TrailingStopOptions tso=new TrailingStopOptions(betOpen, this);
		tso.setWaitFramesOpen(40);      // 0.75 minute 1,5
		tso.setWaitFramesNormal(90);   //2.25- 3 minutes
		tso.setWaitFramesBestPrice(30);  // 0.75 - 1.5 minute
		tso.setTicksProfit((int)minmax[DataWindowsSizes.INPUT_NEURONS][1]);
		tso.setTicksLoss((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][1]+2);
		tso.setForceCloseOnStopLoss(false);
		tso.setInsistOpen(false);
		tso.setGoOnfrontInBestPrice(false);
		tso.setUseStopProfifInBestPrice(true);
		tso.setPercentageOpen(0.80);   // if 80% is open go to close  
		tso.setDelayBetweenOpenClose(-1);
		tso.setDelayIgnoreStopLoss(-1);
		tso.setUpdateInterval(TradeMechanism.SYNC_MARKET_DATA_UPDATE);
		
		tso.setMovingAverageSamples(0);
		tso.setReference(TrailingStopOptions.REF_BEST_PRICE);
			
		TrailingStop trailingStop=new TrailingStop(tso);
		System.out.println("TrailingStop Started - going to state EXECUTING_SWING");	

	}
	
	public void trailDown()
	{
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE)
			entryOdd=Utils.getOddLayFrame(rd, 0);
		else
			entryOdd=Utils.getOddBackFrame(rd, 0);
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd)+1);
		
		BetData betOpen=new BetData(rd,
				3.00,
				entryOdd,
				BetData.BACK,
				false);
		
		TrailingStopOptions tso=new TrailingStopOptions(betOpen, this);
		tso.setWaitFramesOpen(40);      // 0.75 minute 1,5
		tso.setWaitFramesNormal(90);   //2.25- 3 minutes
		tso.setWaitFramesBestPrice(30);  // 0.75 - 1.5 minute
		tso.setTicksProfit((int)minmax[DataWindowsSizes.INPUT_NEURONS][1]);
		tso.setTicksLoss((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][1]+2);
		tso.setForceCloseOnStopLoss(false);
		tso.setInsistOpen(false);
		tso.setGoOnfrontInBestPrice(false);
		tso.setUseStopProfifInBestPrice(true);
		tso.setPercentageOpen(0.80);   // if 80% is open go to close  
		tso.setDelayBetweenOpenClose(-1);
		tso.setDelayIgnoreStopLoss(-1);
		tso.setUpdateInterval(TradeMechanism.SYNC_MARKET_DATA_UPDATE);
		
		tso.setMovingAverageSamples(0);
		tso.setReference(TrailingStopOptions.REF_BEST_PRICE);
			
		TrailingStop trailingStop=new TrailingStop(tso);
		System.out.println("TrailingStop Started - going to state EXECUTING_SWING");	
	}
	
	// min max are simetric so whe only need max
	public double deNormalizeOutput(double value,double max)
	{
		return value*max;    
	
	}
	
	public double generateNNOutput()
	{
		if(CategoriesParameters.COLLECT==false)
		{
			return 0;
		}
		
		double past=Utils.getOddBackAVG(rd, 10, CategoriesParameters.FRAMES_PREDICTION-10);
		double present=Utils.getOddBackAVG(rd, 10, 0);
		
		return Utils.oddToIndex(Utils.nearValidOdd(present))-Utils.oddToIndex(Utils.nearValidOdd(past));
		
	}
	
	public Vector<Double> generateNNInputs()
	{
		Vector<Double> ret=new Vector<Double>();
		int timeFramesOffset=0;
		if(CategoriesParameters.COLLECT==true)
		{
			timeFramesOffset=CategoriesParameters.FRAMES_PREDICTION;
		}
		int timeDataWindows[][]=DataWindowsSizes.getWindowsByCategory(getCat());
		
		if(!Utils.isValidWindow(rd,  timeDataWindows[DataWindowsSizes.SEGMENTS-1][1]*2, timeDataWindows[DataWindowsSizes.SEGMENTS-1][0]+timeFramesOffset))
		{
			//System.out.println("Not Valid window");
			return null;
		}
		// 7 frames de interpolação cada imput  (está em DataWindowsSizes)
		
		// evolução da odd do proprio 
		// evolução da odd do vizinho (ou favorito)
		// evolução da oferta correspondida 
		// evolução dos backs disponiveis  
		// evolução dos lays disponiveis 
		
		// 7 x 5 = 35 inputs 
		
		
		for(int i=0;i<DataWindowsSizes.SEGMENTS;i++)
		{
			ret.add((double)UtilsCollectData.getOddLayTickVariationIntegral(rd, timeDataWindows[i][0]+timeFramesOffset, timeDataWindows[i][1]));
		}
		
		for(int i=0;i<DataWindowsSizes.SEGMENTS;i++)
		{
			ret.add((double)UtilsCollectData.getOddLayTickVariationIntegral(neighbour, timeDataWindows[i][0]+timeFramesOffset, timeDataWindows[i][1]));
		}
		
		for(int i=0;i<DataWindowsSizes.SEGMENTS;i++)
		{
			ret.add(UtilsCollectData.getAmountOfferVariationAVGBackDepthWindow(rd, timeDataWindows[i][0]+timeFramesOffset, timeDataWindows[i][1],axisSize));
		}
		
		for(int i=0;i<DataWindowsSizes.SEGMENTS;i++)
		{
			ret.add(UtilsCollectData.getAmountOfferVariationAVGLayDepthWindow(rd, timeDataWindows[i][0]+timeFramesOffset, timeDataWindows[i][1],axisSize));
		}
		
		for(int i=0;i<DataWindowsSizes.SEGMENTS;i++)
		{
			ret.add(UtilsCollectData.getAmountMatchedVariationAVGAxisWindow(rd, timeDataWindows[i][0]+timeFramesOffset, timeDataWindows[i][1],axisSize));
		}
		
		return ret;
	}
	
	public double[] generateNNTrainSample()
	{
		if(CategoriesParameters.COLLECT==false)
		{
			return null;
		}
		// generateNNInputs() + output
		
		return null;
	}
	
	public Vector<CategoryNode> getCat() {
		return cat;
	}


	public RunnersData getRd() {
		return rd;
	}

	public RunnersData getNeighbour() {
		return neighbour;
	}

	@Override
	public void tradeMechanismChangeState(TradeMechanism tm, int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tradeMechanismEnded(TradeMechanism tm, int state) {
		writeResultsToFile(tm);
		
	}

	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {
		// TODO Auto-generated method stub
		
	}

	private void writeResultsToFile(TradeMechanism tm)
	{
		String line=cat.get(6).getIdStart()+" "+tm.getEndPL()+" ";
		if(tm instanceof Swing)
			line+="1 ";
		else
			line+="2 ";
		
		
		String fileName="stats.txt";
		
		System.out.println("writing to file : "+fileName);
		
		BufferedWriter out=null;
			
		try {
			out = new BufferedWriter(new FileWriter(fileName, true));
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open "+fileName+" for writing");
			}
		if(out==null)
		{
			System.err.println("could not open "+fileName);
			return;
		}
		
		try {
			out.write(line);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			System.out.println(fileName+":Error wrtting data to log file");
			e.printStackTrace();
		}
		
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
}
