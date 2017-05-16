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
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

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
	
	public static int PREDICT_STRONG_DOWN=0;
	public static int PREDICT_WEEAK_DOWN=1;
	public static int PREDICT_ZERO=2;
	public static int PREDICT_STRONG_UP=4;
	public static int PREDICT_WEEAK_UP=3;
	
	double[][] minmax=null; // For Normalizations and Denormalizations 
	
	// use TF or ENCOG ?
	public static boolean useTensorFlow=true;  
		
	// for Encog Models 
	public static String SUFIX_MODEL="C";
	BasicNetwork network=null;
	
	// for TensorFlow Models
	SavedModelBundle bundle=null; //SavedModelBundle.load("tfModels/dnn/ModelSave","serve");
	Session TFsession = null; //bundle.session();
	
	
	public Vector<Double> votesEncog=new Vector<Double>();
	public Vector<Integer> votesTF=new Vector<Integer>();
	public static int NUMBER_OF_VOTES=60;
	
	public static boolean TRADE_AT_BEST_PRICE=true;
	
	public Vector<TradeMechanism> tmUp=new Vector<TradeMechanism>();
	public Vector<TradeMechanism> tmDown=new Vector<TradeMechanism>();
	
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
		
		if(useTensorFlow)
		{
			String catPath= CategoryNode.getAncestorsStringPath(cat)+"ModelSaveFinal/";
			File[] directories = new File(catPath).listFiles(File::isDirectory);
			for (int i=0;i<directories.length;i++)
			{
				System.out.println(" #################### MODEL DIRS ############### \n"+directories[i].getPath());
				catPath=directories[i].getPath();
			}
			
			//catPath+="/saved_model.pbtxt";
			bundle= SavedModelBundle.load(catPath,"serve");
			TFsession = bundle.session();
			//File file = new File("/path/to/directory");
		/*	String[] directories = file.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
			System.out.println(Arrays.toString(directories));*/
		}
		else
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
	
	
	public int predictEncog(Vector<Double> rawInputs)
	{
		
		if(network== null)
			return PREDICT_NO_MODEL_ERROR;
		
		int ret=RunnerCategoryData.PREDICT_ZERO;

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
		
	
		if(value>-min && value<min)
			ret=PREDICT_ZERO;
		else if(value>=min && value<=max)
			ret=PREDICT_WEEAK_UP;
		else if(value>max)
		{
			ret=PREDICT_STRONG_UP;
			//forceCloseTMDown();
		}
		else if(value>=-max && value<=-min)
			ret=PREDICT_WEEAK_DOWN;
		else if(value<-max)
		{
			//forceCloseTMUp();
			ret=PREDICT_STRONG_DOWN;
		}
	
		votesEncog.add(value);
		
		if(votesEncog.size()>NUMBER_OF_VOTES)
		{
			votesEncog.remove(0);
		}
		
		double avgVotes=0;
		for(double x:votesEncog)
		{
			avgVotes+=x;
		}
		
		avgVotes=avgVotes/(double)votesEncog.size();
		
		if(avgVotes>=max)
			forceCloseTMDown();
		if(avgVotes<=-max)
			forceCloseTMUp();
	
		
		System.out.println("min : "+min+" max : "+max+"  predict : "+ret+" vector votes :"+votesEncog);
	
		return ret;
	}
	
	public int predictTensorFlow(Vector<Double> rawInputs)
	{
		if(TFsession == null)
			return PREDICT_NO_MODEL_ERROR;
		
					
		Double rawExample[]=rawInputs.toArray(new Double[]{});
		double normalExample[]=new double[DataWindowsSizes.INPUT_NEURONS];
		for(int i=0;i<DataWindowsSizes.INPUT_NEURONS;i++)
		{		
			normalExample[i]=ProcessNNRawData.normalizeValue(rawExample[i], minmax[i][0], minmax[i][1]);
			//System.out.println("Normalize["+i+"]="+rawInputs.get(i)+" with ("+minmax[i][0]+","+minmax[i][1]+") = "+normalExample[i]);
		}
		
		float [] inputfloat=new float[normalExample.length];
		for(int i=0;i<inputfloat.length;i++)
		{		
			inputfloat[i]=(float) normalExample[i];
		}
		
		float[][] data= new float[1][35];
		data[0]=inputfloat;
		Tensor inputTensor=Tensor.create(data);
		
		Tensor result = TFsession.runner()
				.feed("dnn/input_from_feature_columns/input_from_feature_columns/concat", inputTensor)
				//.feed("input_example_tensor", inputTensor)
	            //.fetch("tensorflow/serving/classify")
	            .fetch("dnn/multi_class_head/predictions/probabilities")
				//.fetch("dnn/zero_fraction_3/Cast")
	            .run().get(0);
		
		 float[][] m = new float[1][5];
         float[][] vector = result.copyTo(m);
         float maxVal = 0;
         int inc = 0;
         int predict = -1;
         for(float val : vector[0]) 
         {
        	// System.out.println(val+"  ");
        	 if(val > maxVal) {
        		 predict = inc;
        		 maxVal = val;
        	 }
        	 inc++;
         }
         
         votesTF.add(predict);
 		
		 if(votesTF.size()>NUMBER_OF_VOTES)
		 {
			votesTF.remove(0);
		 }
         
		
		return predict;
	}
	
	public int predict()
	{
		if(CategoriesParameters.COLLECT)
			return PREDICT_COLLECT_ERROR;
		if(minmax == null)
			return PREDICT_NO_MODEL_ERROR;
		Vector<Double> rawInputs=generateNNInputs();
		if(rawInputs==null)
			return PREDICT_NO_DATA_ERROR;
		
		
		// force not use far (hack) - use only last 5 min.
		//if(cat.get(5).getPath().equals("farFromBegining"))
		//{
		//	return PREDICT_NO_DATA_ERROR;
		//}
		//System.out.println("cheguri aqui ");
		if(useTensorFlow)
			return predictTensorFlow(rawInputs);
		else
			return predictEncog(rawInputs);
			

	}
	
	public double [] getOuputIntervals()
	{
		return minmax[DataWindowsSizes.INPUT_NEURONS+1];
	}
	
	public void executePredictionsEncog()
	{
		if(votesEncog.size()<NUMBER_OF_VOTES)
			return;
		
		double avgVotes=0;
		for(double x:votesEncog)
		{
			avgVotes+=x;
		}
		
		avgVotes=avgVotes/(double)votesEncog.size();
		
		int value=(int)(avgVotes+0.5);
		
		double min=minmax[DataWindowsSizes.INPUT_NEURONS+1][0];
		double max=minmax[DataWindowsSizes.INPUT_NEURONS+1][1];
		
		System.out.println("runner : "+rd.getName()+" min : "+min+" max : "+max+"  predict : "+value);
		
		if(value>=min && value<=max)
			//trailUp();//
			swingUp();
			
		else if(value>max)
			trailUp();
			//swingUp();
		else if(value>=-max && value<=-min)
			//trailDown();//
			swingDown();
			
		else if(value<-max)
			trailDown();
			//swingDown();
			 
	}
	
	public void executePredictionsTensorFlow()
	{
		if(votesTF.size()<NUMBER_OF_VOTES)
			return;
		
		//System.out.println("Passei aqui");
		
		int votesClasses[]=new int[5];
		for(int x:votesTF)
		{
			votesClasses[x]++;
		}
		
		int maxClass=-1;
		int maxValue=-1;
		for(int x=0;x<votesClasses.length;x++)
		{
			if(votesClasses[x]>maxValue)
			{
				maxValue=votesClasses[x];
				maxClass=x;
			}
			
			
		}
		
		int totalForce=(votesClasses[0]+votesClasses[1])-(votesClasses[3]+votesClasses[4]);

		if(votesClasses[maxClass]>20 )
		{
			
			if(totalForce>0)
			{
				if(maxClass==PREDICT_STRONG_DOWN) {trailDown();forceCloseTMUp();}
				if(maxClass==PREDICT_WEEAK_DOWN) {swingDown();forceCloseTMUp();}
			}
			if(totalForce<0)
			{
				if(maxClass==PREDICT_WEEAK_UP) {swingUp();forceCloseTMDown();}
				if(maxClass==PREDICT_STRONG_UP) {trailUp();forceCloseTMDown();}
			}
		}

	}
	
	public void executePredictions()
	{
		
		if(useTensorFlow)
			executePredictionsTensorFlow();
		else
			executePredictionsEncog();
			 
		
	}
	
	public void swingUp()
	{
		//forceCloseTMDown();
		
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE)
			entryOdd=Utils.getOddBackFrame(rd, 0);
		else
			entryOdd=Utils.getOddLayFrame(rd, 0);
			
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd));
		
		BetData betOpen=new BetData(rd,
				3.00,
				entryOdd,
				BetData.LAY,
				false);
		
		SwingOptions so=new SwingOptions(betOpen, this);
		so.setWaitFramesOpen(30);      // 0.75 minute 1,5
		so.setWaitFramesNormal(40);   //2.25- 3 minutes
		so.setWaitFramesBestPrice(30);  // 0.75 - 1.5 minute
		so.setTicksProfit((int)minmax[DataWindowsSizes.INPUT_NEURONS][1]);
		so.setTicksLoss((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][0]);
		so.setForceCloseOnStopLoss(false);
		so.setInsistOpen(false);
		so.setGoOnfrontInBestPrice(false);
		so.setUseStopProfifInBestPrice(true);
		so.setPercentageOpen(0.80);   // if 80% is open go to close  
		so.setDelayBetweenOpenClose(-1);
		so.setDelayIgnoreStopLoss(-1);
		so.setUpdateInterval(TradeMechanism.SYNC_MARKET_DATA_UPDATE);
		
			
		Swing swing=new Swing(so);
		tmUp.add(swing);
		
		System.err.println("Executing Prediction Swing UP "+rd.getName() );
		rd.getMarketData().pause=true;
		
		System.out.println("Swing Started - going to state EXECUTING_SWING");	
	
	}
	
	public void swingDown()
	{
		
		//forceCloseTMUp();
		
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE)
			entryOdd=Utils.getOddLayFrame(rd, 0);
		else
			entryOdd=Utils.getOddBackFrame(rd, 0);
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd));
		
		BetData betOpen=new BetData(rd,
				3.00,
				entryOdd,
				BetData.BACK,
				false);
		
		
		
		SwingOptions so=new SwingOptions(betOpen, this);
		so.setWaitFramesOpen(30);      // 0.75 minute 1,5
		so.setWaitFramesNormal(40);   //2.25- 3 minutes
		so.setWaitFramesBestPrice(30);  // 0.75 - 1.5 minute
		so.setTicksProfit((int)minmax[DataWindowsSizes.INPUT_NEURONS][1]);
		so.setTicksLoss((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][0]);
		so.setForceCloseOnStopLoss(false);
		so.setInsistOpen(false);
		so.setGoOnfrontInBestPrice(false);
		so.setUseStopProfifInBestPrice(true);
		so.setPercentageOpen(0.80);   // if 80% is open go to close  
		so.setDelayBetweenOpenClose(-1);
		so.setDelayIgnoreStopLoss(-1);
		so.setUpdateInterval(TradeMechanism.SYNC_MARKET_DATA_UPDATE);
		
			
		Swing swing=new Swing(so);
		tmDown.add(swing);
		
		System.err.println("Executing Prediction Swing DOWN "+rd.getName() );
		rd.getMarketData().pause=true;
		
		
		System.out.println("Swing Started - going to state EXECUTING_SWING");	
	}
	
	public void trailUp()
	{
		//forceCloseTMDown();
		
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE)
			entryOdd=Utils.getOddBackFrame(rd, 0);
		else
			entryOdd=Utils.getOddLayFrame(rd, 0);
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd));
		
		BetData betOpen=new BetData(rd,
				3.00,
				entryOdd,
				BetData.LAY,
				false);
		
		TrailingStopOptions tso=new TrailingStopOptions(betOpen, this);
		tso.setWaitFramesOpen(30);      // 0.75 minute 1,5
		tso.setWaitFramesNormal(40);   //2.25- 3 minutes
		tso.setWaitFramesBestPrice(30);  // 0.75 - 1.5 minute
		tso.setTicksProfit((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][1]);
		tso.setTicksLoss((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][0]);
		tso.setForceCloseOnStopLoss(false);
		tso.setInsistOpen(false);
		tso.setGoOnfrontInBestPrice(false);
		tso.setUseStopProfifInBestPrice(true);
		tso.setPercentageOpen(0.80);   // if 80% is open go to close  
		tso.setDelayBetweenOpenClose(-1);
		tso.setDelayIgnoreStopLoss(-1);
		tso.setUpdateInterval(TradeMechanism.SYNC_MARKET_DATA_UPDATE);
		
		tso.setMovingAverageSamples(2);
		tso.setReference(TrailingStopOptions.REF_BEST_PRICE);
			
		TrailingStop trailingStop=new TrailingStop(tso);
		tmUp.add(trailingStop);
		
		System.err.println("Executing Prediction Trail UP "+rd.getName() );
		rd.getMarketData().pause=true;
		
		
		System.out.println("TrailingStop Started - going to state EXECUTING_TRAIL");
		
		

	}
	
	public void trailDown()
	{
		
		//forceCloseTMUp();
		
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE)
			entryOdd=Utils.getOddLayFrame(rd, 0);
		else
			entryOdd=Utils.getOddBackFrame(rd, 0);
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd));
		
		BetData betOpen=new BetData(rd,
				3.00,
				entryOdd,
				BetData.BACK,
				false);
		
		TrailingStopOptions tso=new TrailingStopOptions(betOpen, this);
		tso.setWaitFramesOpen(30);      // 0.75 minute 1,5
		tso.setWaitFramesNormal(40);   //2.25- 3 minutes
		tso.setWaitFramesBestPrice(30);  // 0.75 - 1.5 minute
		tso.setTicksProfit((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][1]);
		tso.setTicksLoss((int)minmax[DataWindowsSizes.INPUT_NEURONS+1][0]);
		tso.setForceCloseOnStopLoss(false);
		tso.setInsistOpen(false);
		tso.setGoOnfrontInBestPrice(false);
		tso.setUseStopProfifInBestPrice(true);
		tso.setPercentageOpen(0.80);   // if 80% is open go to close  
		tso.setDelayBetweenOpenClose(-1);
		tso.setDelayIgnoreStopLoss(-1);
		tso.setUpdateInterval(TradeMechanism.SYNC_MARKET_DATA_UPDATE);
		
		tso.setMovingAverageSamples(2);
		tso.setReference(TrailingStopOptions.REF_BEST_PRICE);
			
		TrailingStop trailingStop=new TrailingStop(tso);
		tmDown.add(trailingStop);
		
		System.err.println("Executing Prediction trail DOWN "+rd.getName() );
		rd.getMarketData().pause=true;
		
		
		System.out.println("TrailingStop Started - going to state EXECUTING_TRAIL");	
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
		// 7 frames de interpola��o cada imput  (est� em DataWindowsSizes)
		
		// evolu��o da odd do proprio 
		// evolu��o da odd do vizinho (ou favorito)
		// evolu��o da oferta correspondida 
		// evolu��o dos backs disponiveis  
		// evolu��o dos lays disponiveis 
		
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
		{
			line+="1 ";
			
		}
		else
		{
			line+="2 ";
		}
		
		line+=tm.getStatisticsValues().split(" ")[10]+" ";
		//System.out.println(tm.getStatisticsFields());
	
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
	
	public void forceCloseTMUp()
	{
		System.err.println("########################## UP #################");
		for(TradeMechanism tm:tmUp)
		{
			if(!tm.isEnded())
				tm.forceClose();
		}
	}
	
	public void forceCloseTMDown()
	{
		
		System.err.println("########################## DOWN #################");
		for(TradeMechanism tm:tmDown)
		{
			if(!tm.isEnded())
				tm.forceClose();
		}
	}
	
	
	
}
