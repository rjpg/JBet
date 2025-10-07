package categories.categories2018.bots;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.encog.ml.data.MLData;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import DataRepository.RunnersData;
import DataRepository.Utils;
import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.swing.Swing;
import TradeMechanisms.swing.SwingOptions;
import TradeMechanisms.trailingStop.TrailingStop;
import TradeMechanisms.trailingStop.TrailingStopOptions;
import bets.BetData;
import bots.Bot;

import categories.categories2013.CategoryNode;
import categories.categories2013.bots.UtilsCollectData;
import categories.categories2013.scripts.ProcessNNRawData;
import categories.categories2018.DataWindowsSizes2018;
import categories.categories2018.TFManager;
import categories.categories2018.scripts.ProcessInputRawValData;
import tfserving.Classifier;
import tfserving.Prediction;

public class RunnerCatData2018 implements TradeMechanismListener{

	Vector<CategoryNode> cat=null;
	
	RunnersData rd;
	
	RunnersData neighbour;
	
	int axisSize=3;
	
	// execute Model data
	public static int PREDICT_NO_DATA_ERROR=-1;
	public static int PREDICT_COLLECT_ERROR=-2;
	public static int PREDICT_NO_MODEL_ERROR=-3;
	public static int EXECUTE_NO_PREDICTION_ERROR=-4;
	
	public static int PREDICT_NOT_MADE=-1;
	public static int PREDICT_STRONG_DOWN=0;
	public static int PREDICT_WEEAK_DOWN=1;
	public static int PREDICT_ZERO=2;
	public static int PREDICT_STRONG_UP=4;
	public static int PREDICT_WEEAK_UP=3;
	
	double[][] minmax=null; // For Normalizations and Denormalizations 
	
	double[][] tradingParams=null; // Trading parameters
	
	Vector<Double[]> holdInputValues=null;
	Vector<Double> holdOutputValues=null;
	
	// for TensorFlow Models
	Session TFsession = null; //bundle.session();
	
	int prediction = PREDICT_NOT_MADE;
	float predctionProbs[] = new  float[5];
			
	public static boolean TRADE_AT_BEST_PRICE=true;
	
	public static boolean TRADE_AT_BEST_PRICE_TS=true;
	public static boolean TRADE_AT_BEST_PRICE_SW=true;
	
	public Vector<TradeMechanism> tmUp=new Vector<TradeMechanism>();
	public Vector<TradeMechanism> tmDown=new Vector<TradeMechanism>();
	
	public double stake=3.00;
	
	public Bot botInUse=null;
	
	String logString ="";
	
	public RunnerCatData2018(RunnersData rdA,Vector<CategoryNode> catA) {
		rd=rdA;
		cat=catA;
		
		if(cat.get(1).getPath().equals("nofavorite"))
		{
			neighbour=Utils.getNeighbour(rd,0);
			System.out.println("no favorite : "+neighbour.getName());
			
		}
		else
		{
			if(cat.get(3).getPath().equals("lowOdd"))
				neighbour=Utils.getNeighbour(rd,0);
			else
				neighbour=Utils.getFavorite(rd.getMarketData(),0);
			
			System.out.println("has favorite : "+neighbour.getName());
		}
		
		
		if(cat.get(3).getPath().equals("lowOdd"))
		{
			axisSize=4;
		}
		if(cat.get(3).getPath().equals("midleOdd"))
		{
			axisSize=3;
		}
		if(cat.get(3).getPath().equals("highOdd"))
		{
			axisSize=2;
		}
		
		loadExecutionModelData();
	
	}
	
	public RunnerCatData2018(RunnersData rdA,Vector<CategoryNode> catA, Bot bot) {
		this(rdA,catA);
		botInUse=bot;
		
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
			
			 minmax=new double[DataWindowsSizes2018.INPUT_NEURONS][2];
			
			
			try {
				for(int i=0;i<DataWindowsSizes2018.INPUT_NEURONS;i++)
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
	
	public int loadTradingParams()
	{
		
		String fileName=CategoryNode.getAncestorsStringPath(cat)+"TradingParams.csv";
		File f=new File(fileName);
		
		if(f.exists()) { 
		
			BufferedReader input=getBufferedReader(f);
			String s=null;
			
			 tradingParams=new double[4][2];
			
			
			try {
				for(int i=0;i<4;i++)
				{
					s=input.readLine();
					String sa[]=s.split(",");
					tradingParams[i][0]=Math.round(Math.abs(Double.parseDouble(sa[0])));
					tradingParams[i][1]=Math.round(Math.abs(Double.parseDouble(sa[1])));
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
		
		
		int id=cat.get(4).getIdStart();
		//catPath+="/saved_model.pbtxt";
		
		//if(id==41)
		//	System.out.println(" #################### MODEL LOAD ############### \n");
		
		//getRd().getMarketData().pause=true;
		System.out.println(id);
		TFsession = TFManager.getModel(id);
		
		if(TFsession==null)
		{
			System.out.println("Tensoflow model does not exists on : id-"+id+" "+CategoryNode.getAncestorsStringPath(cat));
			return -1;
		}
		//File file = new File("/path/to/directory");
	/*	String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		System.out.println(Arrays.toString(directories));*/
		return 0;
	}
	
	public void loadExecutionModelData()
	{
		String catPath=CategoryNode.getAncestorsStringPath(cat);
		
		System.out.println("Loading model execution data for : "+catPath);
		System.out.println("Loading normalization data for : "+catPath);
		if(loadMinMaxValues()==-1)
		{
			System.out.println("Error Loading normalization data for : "+catPath);
			return;
		}
		
		if(loadTradingParams()==-1)
		{
			System.out.println("Error Loading Trading Parameters data for : "+catPath);
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
	
	   public static JSONArray cleanup(String jsonData){
	        JSONObject jobj = new JSONObject(jsonData); //
	        JSONArray s = jobj.getJSONArray("predictions");  //this will have one or more predictions ex [[0.123], [1.234]]
	        

	        JSONArray pred = new JSONArray();  //this will become flatten predictions ex: [0.123, 1.234]

	        for (int a = 0; a < s.length(); a++) {
	            JSONArray inner = s.getJSONArray(a);
	            if (inner.length() > 0) {
	                pred.put(inner.get(0));
	            }
	        }
	        
	        return pred;

	    }
	
		
	public int predictTensorFlow()
	{
		if(TFsession == null)
			return PREDICT_NO_MODEL_ERROR;
		
		if(holdInputValues==null)
			return PREDICT_NO_DATA_ERROR;
		
		
		Vector<Double[]> transposeExample = new Vector<Double[]>();
		
		for(int x=0;x<DataWindowsSizes2018.SEGMENTS;x++)
		{
			Double[] aux=new Double[9];
			for(int i=0;i<holdInputValues.size();i++)
			{
				
				aux[i]=holdInputValues.get(i)[x];
			}
			transposeExample.add(aux); 
		}
		
		
		
		Vector<Double[]> normalExample=ProcessInputRawValData.normalizeDouble(transposeExample, minmax);
		
		
		
		/*Double normalExampleD[][]=normalExample.toArray(new Double[][]{});
		
		
		//convert to float and transpose for TF
		float [][] inputfloat=new float[normalExampleD[0].length][normalExampleD.length];
		for(int i=0;i<normalExampleD.length;i++)	
			for(int x=0;x<normalExampleD[0].length;x++)
					inputfloat[x][i]=(float) ((double)normalExampleD[i][x]);
		*/
		
		//convert to float and transpose for TF, like in saving in file
		float [][] inputfloat=new float[DataWindowsSizes2018.SEGMENTS][DataWindowsSizes2018.TIME_SERIES];
		for(int x=0;x<DataWindowsSizes2018.SEGMENTS;x++)
		{
			for(int i=0;i<DataWindowsSizes2018.TIME_SERIES;i++)
			{
				inputfloat[x][i]=(float) ((double)normalExample.get(x)[i]);
				System.out.print(inputfloat[x][i]+" , ");
			}
			System.out.println();
		}
		
		// build an batch data set with one example for TF : final shape [1][128][9]
		
		
		float[][][] data= new float[1][inputfloat.length][inputfloat[0].length];
		data[0]=inputfloat;
		
		String json ="{\"instances\": "+Arrays.deepToString(data)+"}";
		
		int predict=-1;
		
		HttpClient client = HttpClient.newBuilder().build();
		
		HttpRequest request = HttpRequest.newBuilder()
        		//http://localhost:8501/v1/models/my_model:predict
                .uri(URI.create("http://localhost:8501/v1/models/my_model:predict"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		

	        if (response.statusCode()== 200){
	        
	
		        JSONArray pred = cleanup(response.body());
		        List<Prediction> predictions = Classifier.processPredictions(pred);
		        
		
		        // Output the predictions
		        for (Prediction prediction : predictions) {
		            System.out.println(prediction);
		            predict=prediction.getClassified();
		        }
	        }
	        
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        System.out.println("##########################################");
        System.out.println("For "+getRd().getName()+" predict : "+predict);
        
        //if we want to execute high certainty
        //if(vector[0][predict]>0.3)
        //	return predict;
        //else
        //	return PREDICT_NO_MODEL_ERROR;
        
        return predict;

	}

	public int predictTensorFlowOld()
	{
		if(TFsession == null)
			return PREDICT_NO_MODEL_ERROR;
		
		if(holdInputValues==null)
			return PREDICT_NO_DATA_ERROR;
		
		
		Vector<Double[]> transposeExample = new Vector<Double[]>();
		
		for(int x=0;x<DataWindowsSizes2018.SEGMENTS;x++)
		{
			Double[] aux=new Double[9];
			for(int i=0;i<holdInputValues.size();i++)
			{
				
				aux[i]=holdInputValues.get(i)[x];
			}
			transposeExample.add(aux); 
		}
		
		
		
		Vector<Double[]> normalExample=ProcessInputRawValData.normalizeDouble(transposeExample, minmax);
		
		
		
		/*Double normalExampleD[][]=normalExample.toArray(new Double[][]{});
		
		
		//convert to float and transpose for TF
		float [][] inputfloat=new float[normalExampleD[0].length][normalExampleD.length];
		for(int i=0;i<normalExampleD.length;i++)	
			for(int x=0;x<normalExampleD[0].length;x++)
					inputfloat[x][i]=(float) ((double)normalExampleD[i][x]);
		*/
		
		//convert to float and transpose for TF, like in saving in file
		float [][] inputfloat=new float[DataWindowsSizes2018.SEGMENTS][DataWindowsSizes2018.TIME_SERIES];
		for(int x=0;x<DataWindowsSizes2018.SEGMENTS;x++)
		{
			for(int i=0;i<DataWindowsSizes2018.TIME_SERIES;i++)
			{
				inputfloat[x][i]=(float) ((double)normalExample.get(x)[i]);
				System.out.print(inputfloat[x][i]+" , ");
			}
			System.out.println();
		}
		
		// build an batch data set with one example for TF : final shape [1][128][9]
		
		
		float[][][] data= new float[1][inputfloat.length][inputfloat[0].length];
		data[0]=inputfloat;
		
		
		
		Tensor inputTensor=Tensor.create(data);
		
		Tensor result = TFsession.runner()
	            .feed("input_1", inputTensor)
	            //.feed("dropout_1/keras_learning_phase", no_learning)
	            .fetch("dense_1/Softmax")
	            .run().get(0);
		
		float[][] m = new float[1][5];
        float[][] vector = (float [][])result.copyTo(m);
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
        
        predctionProbs=vector[0];
        //writePredictions(vector[0]);
        
        System.out.println("##########################################");
        System.out.println("For "+getRd().getName()+" predict : "+predict);
        
        //if we want to execute high certainty
        //if(vector[0][predict]>0.3)
        //	return predict;
        //else
        //	return PREDICT_NO_MODEL_ERROR;
        
        return predict;

	}

	public int predict()
	{
		
		if(minmax == null)
			return PREDICT_NO_MODEL_ERROR;
		
		Vector<Double[]> rawInputs=getHoldInputValues();
		if(rawInputs==null)
			return PREDICT_NO_DATA_ERROR;
		
		prediction=predictTensorFlow();
		return prediction; 
			

	}
	
	public int executePrediction()
	{
		if(prediction==PREDICT_NOT_MADE)
			return EXECUTE_NO_PREDICTION_ERROR;
		
		if(prediction==PREDICT_STRONG_DOWN) trailDown();
		
		if(prediction==PREDICT_WEEAK_DOWN) swingDown(); 
		
		if(prediction==PREDICT_STRONG_UP) trailUp();
		
		if(prediction==PREDICT_WEEAK_UP) swingUp();
		
		
		return 0;
	}
	
	
	public void swingUp()
	{
		//forceCloseTMDown();
		
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE_SW)
			if (Utils.getAmountBackFrame(rd, 0)>Utils.getAmountLayFrame(rd, 0)*2)
				entryOdd=Utils.getOddLayFrame(rd, 0);
			else if(Utils.getAmountBackFrame(rd, 0) *2 < Utils.getAmountLayFrame(rd, 0))
				entryOdd=Utils.indexToOdd(Utils.oddToIndex(Utils.getOddLayFrame(rd, 0)) -1);
			else
				entryOdd=Utils.getOddBackFrame(rd, 0);
		else
			entryOdd=Utils.getOddLayFrame(rd, 0);
			
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd));
		
		BetData betOpen=new BetData(rd,
				stake,
				entryOdd,
				BetData.LAY,
				false);
		
		SwingOptions so=new SwingOptions(betOpen, this);
		so.setWaitFramesOpen(30);      // 0.75 minute 1,5
		so.setWaitFramesNormal(90);   //2.25- 3 minutes
		so.setWaitFramesBestPrice(20);  // 0.75 - 1.5 minute
		so.setTicksProfit((int)tradingParams[2][0]);
		so.setTicksLoss((int)tradingParams[2][0]);
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
		//rd.getMarketData().pause=true;
		
		System.out.println("Swing Started - going to state EXECUTING_SWING");	
	
	}
	
	public void swingDown()
	{
		
		//forceCloseTMUp();
		
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE_SW)
			if (Utils.getAmountBackFrame(rd, 0)*2<Utils.getAmountLayFrame(rd, 0))
				entryOdd=Utils.getOddBackFrame(rd, 0);
			else if(Utils.getAmountBackFrame(rd, 0)>Utils.getAmountLayFrame(rd, 0)*2)
				entryOdd=Utils.indexToOdd(Utils.oddToIndex(Utils.getOddLayFrame(rd, 0)) +1);
			else
				entryOdd=Utils.getOddLayFrame(rd, 0);
		else
			entryOdd=Utils.getOddBackFrame(rd, 0);
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd));
		
		BetData betOpen=new BetData(rd,
				stake,
				entryOdd,
				BetData.BACK,
				false);
		
		
		
		SwingOptions so=new SwingOptions(betOpen, this);
		so.setWaitFramesOpen(30);      // 0.75 minute 1,5
		so.setWaitFramesNormal(90);   //2.25- 3 minutes
		so.setWaitFramesBestPrice(20);  // 0.75 - 1.5 minute
		so.setTicksProfit((int)tradingParams[1][0]);
		so.setTicksLoss((int)tradingParams[1][0]);
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
		//rd.getMarketData().pause=true;
		
		
		System.out.println("Swing Started - going to state EXECUTING_SWING");	
	}
	
	public void trailUp()
	{
		//forceCloseTMDown();
		
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE_TS)
			if (Utils.getAmountBackFrame(rd, 0)>Utils.getAmountLayFrame(rd, 0)*2)
				entryOdd=Utils.getOddLayFrame(rd, 0);
			else if(Utils.getAmountBackFrame(rd, 0) *2 < Utils.getAmountLayFrame(rd, 0))
				entryOdd=Utils.indexToOdd(Utils.oddToIndex(Utils.getOddLayFrame(rd, 0)) -1);
			else
				entryOdd=Utils.getOddBackFrame(rd, 0);
		else
			entryOdd=Utils.getOddLayFrame(rd, 0);
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd));
		
		BetData betOpen=new BetData(rd,
				stake,
				entryOdd,
				BetData.LAY,
				false);
		
		TrailingStopOptions tso=new TrailingStopOptions(betOpen, this);
		tso.setWaitFramesOpen(30);      // 0.75 minute 1,5
		tso.setWaitFramesNormal(90);   //2.25- 3 minutes
		tso.setWaitFramesBestPrice(20);  // 0.75 - 1.5 minute
		System.err.println("Trail up Tick Profit "+(int)tradingParams[3][0]);
		tso.setTicksProfit((int)tradingParams[3][0]);
		tso.setTicksLoss((int)tradingParams[3][1]+4);
		System.err.println("Trail up Tick Loss "+(int)tradingParams[3][1]);
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
		//rd.getMarketData().pause=true;
		
		
		System.out.println("TrailingStop Started - going to state EXECUTING_TRAIL");
		
		

	}
	
	public void trailDown()
	{
		
		//forceCloseTMUp();
		
		double entryOdd=0;
		if(TRADE_AT_BEST_PRICE_TS)
		{
			if (Utils.getAmountBackFrame(rd, 0)*2<Utils.getAmountLayFrame(rd, 0))
				entryOdd=Utils.getOddBackFrame(rd, 0);
			else if(Utils.getAmountBackFrame(rd, 0)>Utils.getAmountLayFrame(rd, 0)*2)
				entryOdd=Utils.indexToOdd(Utils.oddToIndex(Utils.getOddLayFrame(rd, 0)) +1);
			else
				entryOdd=Utils.getOddLayFrame(rd, 0);
		}
		else
			entryOdd=Utils.getOddBackFrame(rd, 0);
		
		entryOdd=Utils.indexToOdd(Utils.oddToIndex(entryOdd));
		
		BetData betOpen=new BetData(rd,
				stake,
				entryOdd,
				BetData.BACK,
				false);
		
		System.out.println("Ammount Back "+Utils.getAmountBackFrame(rd, 0));
		TrailingStopOptions tso=new TrailingStopOptions(betOpen, this);
		tso.setWaitFramesOpen(30);      // 0.75 minute 1,5
		tso.setWaitFramesNormal(90);   //2.25- 3 minutes
		tso.setWaitFramesBestPrice(20);  // 0.75 - 1.5 minute
		tso.setTicksProfit((int)tradingParams[0][0]);
		tso.setTicksLoss((int)tradingParams[0][1]+4);
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
		//rd.getMarketData().pause=true;
		
		
		System.out.println("TrailingStop Started - going to state EXECUTING_TRAIL");	
	}
	
	// min max are simetric so whe only need max
	public double deNormalizeOutput(double value,double max)
	{
		return value*max;    
	
	}
	
	public double getNNOutputVariationNow()
	{
		if(!Utils.isValidWindow(rd, DataWindowsSizes2018.FRAMES_TO_PREDICT, 0 ))
		{
			System.out.println("Not Valid window at getNNOutputVariationNow()");
			return 0;
		}
		
		double past=Utils.getOddBackAVG(rd, 10, DataWindowsSizes2018.FRAMES_TO_PREDICT-10);
		double present=Utils.getOddBackAVG(rd, 10, 0);
		
		return Utils.oddToIndex(Utils.nearValidOdd(present))-Utils.oddToIndex(Utils.nearValidOdd(past));
		
	}
	
	public double getNNOutputIntegralNow()
	{
		if(!Utils.isValidWindow(rd, DataWindowsSizes2018.FRAMES_TO_PREDICT, 0 ))
		{
			System.out.println("Not Valid window at getNNOutputIntegralNow()");
			return 0;
		}
		
		return (double)UtilsCollectData.getOddLayTickVariationIntegral(rd, 0, DataWindowsSizes2018.FRAMES_TO_PREDICT );		
	}

	
	public Vector<Double[]> generateNNInputs()
	{
		Vector<Double[]> ret=new Vector<Double[]>();
		
		int timeDataWindows[][]=DataWindowsSizes2018.getWindows(getCat());
		
		//System.out.println("Window size test - "
		//		+ "size :"+ timeDataWindows[DataWindowsSizes2018.SEGMENTS-1][1]*2+
		//		" past frame :"+timeDataWindows[DataWindowsSizes2018.SEGMENTS-1][0]
		//				+" total size :"+rd.getDataFrames().size());
		
		if(!Utils.isValidWindow(rd,  (timeDataWindows[DataWindowsSizes2018.SEGMENTS-1][1]+1)*2, timeDataWindows[DataWindowsSizes2018.SEGMENTS-1][0]))
		{
			System.out.println("Not Valid window");
			return null;
		}
		// 7 frames de interpola��o cada imput  (est� em DataWindowsSizes)
		
		// evolu��o da odd do proprio 
		// evolu��o da odd do vizinho (ou favorito)
		// evolu��o da oferta correspondida 
		// evolu��o dos backs disponiveis  
		// evolu��o dos lays disponiveis 
		
		// 7 x 5 = 35 inputs 
		
		int nIndicators=9;
		
		Double[][] indicators=new Double[nIndicators][DataWindowsSizes2018.SEGMENTS];
		
		//System.out.println("Iam :"+rd.getName()+" neighbour:"+neighbour.getName());
		//System.out.println("My cat :"+CategoryNode.getAncestorsStringPath(cat));
		
		int rdRef=Utils.oddToIndex(Utils.getOddLayFrame(rd, DataWindowsSizes2018.TOTAL_SIZE_FRAMES-1));
		int neighbourRef=Utils.oddToIndex(Utils.getOddLayFrame(neighbour, DataWindowsSizes2018.TOTAL_SIZE_FRAMES-1));
		for(int i=0;i<DataWindowsSizes2018.SEGMENTS;i++)
		{
			
			indicators[0][i]=(double)UtilsCollectData.getOddLayTickVariationIntegral(rd, timeDataWindows[i][0], timeDataWindows[i][1]+1);
			indicators[1][i]=(double)UtilsCollectData.getOddLayTickVariationIntegral(neighbour, timeDataWindows[i][0], timeDataWindows[i][1]+1);
			
			indicators[2][i]=UtilsCollectData.getAmountOfferVariationAVGBackDepthWindow(rd, timeDataWindows[i][0], timeDataWindows[i][1]+1,axisSize);
			indicators[3][i]=UtilsCollectData.getAmountOfferVariationAVGLayDepthWindow(rd, timeDataWindows[i][0], timeDataWindows[i][1]+1,axisSize);
			
			indicators[4][i]=UtilsCollectData.getAmountMatchedVariationAVGAxisWindow(rd, timeDataWindows[i][0], timeDataWindows[i][1],axisSize);
			
			indicators[5][i]=(double)Utils.oddToIndex(Utils.nearValidOdd(Utils.getOddLayAVG(rd, timeDataWindows[i][1],timeDataWindows[i][0])))-rdRef;
			indicators[6][i]=(double)Utils.oddToIndex(Utils.nearValidOdd(Utils.getOddLayAVG(neighbour, timeDataWindows[i][1],timeDataWindows[i][0])))-neighbourRef;
			
			indicators[7][i]=UtilsCollectData.getWomAVGWindow(rd,timeDataWindows[i][0], timeDataWindows[i][1],axisSize,true);
			
			indicators[8][i]=UtilsCollectData.getWomOthersAVGWindow(rd,timeDataWindows[i][0], timeDataWindows[i][1],3,true,20);
			
		
		}
		
		for(int i=0;i<nIndicators;i++)
			ret.add(indicators[i]);

		/*
		for(int i=0;i<DataWindowsSizes2018.SEGMENTS;i++)
		{
			ret.add((double)UtilsCollectData.getOddLayTickVariationIntegral(neighbour, timeDataWindows[i][0], timeDataWindows[i][1]));
		}
		
		for(int i=0;i<DataWindowsSizes2018.SEGMENTS;i++)
		{
			ret.add(UtilsCollectData.getAmountOfferVariationAVGBackDepthWindow(rd, timeDataWindows[i][0], timeDataWindows[i][1],axisSize));
		}
		
		for(int i=0;i<DataWindowsSizes2018.SEGMENTS;i++)
		{
			ret.add(UtilsCollectData.getAmountOfferVariationAVGLayDepthWindow(rd, timeDataWindows[i][0], timeDataWindows[i][1],axisSize));
		}
		
		for(int i=0;i<DataWindowsSizes2018.SEGMENTS;i++)
		{
			ret.add(UtilsCollectData.getAmountMatchedVariationAVGAxisWindow(rd, timeDataWindows[i][0], timeDataWindows[i][1],axisSize));
		}*/
		
		return ret;
	}
	
	
	public Vector<Double> generateNNOutputs()
	{
		Vector<Double> ret=new Vector<Double>();
		
		int rdRef=Utils.oddToIndex(Utils.getOddLayFrame(rd, DataWindowsSizes2018.FRAMES_TO_PREDICT));
		
		double variationTicksIntegral=(double)UtilsCollectData.getOddLayTickVariationIntegral(rd, 0, DataWindowsSizes2018.FRAMES_TO_PREDICT,10);
		double variationTicks=(double)Utils.oddToIndex(Utils.nearValidOdd(Utils.getOddLayAVG(rd, 0,10)))-rdRef;
		
		double[] extremesTicksVariation=UtilsCollectData.getMaxTickVariation(rd, 0, DataWindowsSizes2018.FRAMES_TO_PREDICT, 10);
		
		ret.add(variationTicksIntegral);
		ret.add(variationTicks);
		ret.add(extremesTicksVariation[0]);
		ret.add(extremesTicksVariation[1]);
		//System.out.println("Window size test - "
		//		+ "size :"+ timeDataWindows[DataWindowsSizes2018.SEGMENTS-1][1]*2+
		//		" past frame :"+timeDataWindows[DataWindowsSizes2018.SEGMENTS-1][0]
		//				+" total size :"+rd.getDataFrames().size());
		
		
		return ret;
	}
	
	
	public Vector<Double[]>  holdInputValuesNow()
	{
		System.out.println("Creating inputs");
		holdInputValues=generateNNInputs();
		return holdInputValues; 
	}

	
	public Vector<Double>  holdOutputValuesNow()
	{
		System.out.println("Creating Outputs");
		holdOutputValues=generateNNOutputs();
		return holdOutputValues; 
	}

	
	public Vector<Double[]> getHoldInputValues()
	{
		return holdInputValues;
	}
	
	public Vector<Double> getHoldOuputValues()
	{
		return holdOutputValues;
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
		
		//logString=createLogString(tm);
		
	}

	@Override
	public void tradeMechanismMsg(TradeMechanism tm, String msg, Color color) {
		// TODO Auto-generated method stub
		
	}

	private String createLogString(TradeMechanism tm)
	{
		String line=cat.get(4).getIdStart()+" "+tm.getEndPL()+" ";
		
		if(tm instanceof Swing)
		{
			line+="1 ";
			
		}
		else
		{
			line+="2 ";
		}
		
		line+=tm.getStatisticsValues()+" ";
		
		
		for(float val : predctionProbs) 
		{
			line+=val+"  ";
			
        }
		line+=prediction+" ";
		
		return line;
		
	}
	
	private void writeResultsToFile(TradeMechanism tm)
	{
		String line=cat.get(4).getIdStart()+" "+tm.getEndPL()+" ";
		
		if(tm instanceof Swing)
		{
			line+="1 ";
			
		}
		else
		{
			line+="2 ";
		}
		
		line+=tm.getStatisticsValues();
		
		
		if(botInUse!=null)
		{
			botInUse.setAmountGreen(botInUse.getAmountGreen()+tm.getEndPL());
			botInUse.setGreens(botInUse.getGreens()+1);
		}
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
	
	public void writeLogStringToFile(String plus)
	{
		String line=logString+" "+plus;
		
		
	
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
	
	public static void writePredictions(float pred[])
	{
		String fileName="predictions.txt";
		
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
		
		String line="";
		for(float val : pred) 
		{
			line+=val+"  ";
			
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
	
	
	public static void main(String[] args) {
		Vector<Double[]> holdInputValuesTest=new Vector<Double[]>();
		
		Double[] ina={1.,2.,3.};
		Double[] inb={4.,5.,6.};
		
		holdInputValuesTest.add(ina);
		holdInputValuesTest.add(inb);
		
		Double rawExample[][]=holdInputValuesTest.toArray(new Double[][]{});
		
		System.out.println(rawExample[1][2]);
	
		float [][] inputfloat=new float[rawExample.length][rawExample[0].length];
		for(int i=0;i<rawExample.length;i++)	
			for(int x=0;x<rawExample[0].length;x++)
					inputfloat[i][x]=(float) ((double)rawExample[i][x]);
		
		System.out.println(inputfloat[1][3]);
	}
	
}
