package dl4j;

import java.io.File;
import java.util.Map;


import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.AutoEncoder;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;


public class dl4jTest {

	public static void main(String[] args) 	throws Exception {

		//Utils.init();
		//new OddConverter(3);
		

			int numLinesToSkip = 0;
			String delimiter = ",";
			RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
			// recordReader.initialize(new FileSplit(new
			// ClassPathResource("iris.txt").getFile()));
			recordReader.initialize(new FileSplit(new File("NNNormalizeData-out.csv")));

			
			
			int labelIndex = 35;// 4; //5 values in each row of the iris.txt CSV: 4
								// input features followed by an integer label
								// (class) index. Labels are the 5th value (index 4)
								// in each row
			int numClasses = 5;// 3; //3 classes (types of iris flowers) in the iris
								// data set. Classes have integer values 0, 1 or 2
			int batchSize = 24300; // Iris data set: 150 examples total. We are
									// loading all of them into one DataSet (not
									// recommended for large data sets)

			DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, numClasses);
			DataSet allData = iterator.next();
			allData.shuffle();
			SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65); // Use
																				// 65%
																				// of
																				// data
																				// for
																				// training

			DataSet trainingData = testAndTrain.getTrain();
			DataSet testData = testAndTrain.getTest();

			NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(-1.,1.);
	        
			
			normalizer.fit(trainingData);           //Collect the statistics (mean/stdev) from the training data. This does not modify the input data
	        normalizer.transform(trainingData);     //Apply normalization to the training data
	        normalizer.transform(testData);         //Apply normalization to the test data. This is using statistics calculated from the *training* set

	        //...normalizer.save()
	        System.out.println("Norm : "+normalizer.getTargetMin());
			
			int seed = 123;
			int iterations = 1;
			int listenerFreq = iterations / 5;

			System.out.println("Build model....");
			MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed).iterations(iterations)
					.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).list()
					.layer(0,
							new AutoEncoder.Builder().nIn(35).nOut(100)
									.lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
					.layer(1,
							new AutoEncoder.Builder().nIn(100).nOut(50)
									.lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
					.layer(2,
							new AutoEncoder.Builder().nIn(50).nOut(25)
									.lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
					.layer(3,
							new AutoEncoder.Builder().nIn(25).nOut(10)
									.lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
					.layer(4,
							new AutoEncoder.Builder().nIn(10).nOut(5).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
									.build()) // encoding stops
					.layer(5,
							new AutoEncoder.Builder().nIn(5).nOut(10).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE)
									.build()) // decoding starts
					.layer(6,
							new AutoEncoder.Builder().nIn(10).nOut(25)
									.lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
					.layer(7,
							new AutoEncoder.Builder().nIn(25).nOut(50)
									.lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
					.layer(8,
							new AutoEncoder.Builder().nIn(50).nOut(100)
									.lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
					.layer(9, new AutoEncoder.Builder().lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).nIn(100)
							.nOut(35).build())
					.pretrain(true).backprop(false).build();

			MultiLayerNetwork model = new MultiLayerNetwork(conf);
			model.init();

			Map<java.lang.String, INDArray> param = model.paramTable();
			for (Map.Entry<String, INDArray> entry : param.entrySet()) {
				String key = entry.getKey().toString();
				INDArray value = entry.getValue();
				// if(x++==3)
				int size = value.toString().length();
				if (size > 100)
					size = 100;
				System.out.println("key, " + key + " value " + value.toString().substring(0, size));

			}
			// System.out.println("---------------------------\n"+conf.toJson()+"\n-------------------");
			model.setListeners(new ScoreIterationListener(listenerFreq));

			DataSetIterator dsi = trainingData.iterateWithMiniBatches();
			System.out.println("Train model....");
			for (int i = 0; i < 10; i++) {// while(iter.hasNext()) {
				// DataSet next = dsi.next();
				System.out.println("###########Fit calls " + i);
				// model.fit(new
				// DataSet(next.getFeatureMatrix(),next.getFeatureMatrix()));
				model.fit(new DataSet(trainingData.getFeatureMatrix(), trainingData.getFeatureMatrix()));
			}

			Map<java.lang.String, INDArray> param1 = model.paramTable();
			for (Map.Entry<String, INDArray> entry : param1.entrySet()) {
				String key = entry.getKey().toString();
				INDArray value = entry.getValue();
				// if(x++==3)
				int size = value.toString().length();
				if (size > 100)
					size = 100;
				System.out.println("key, " + key + " value " + value.toString().substring(0, size));

			}

			System.out.println("TransferLearning.Builder(model);");
			TransferLearning.Builder c = new TransferLearning.Builder(model);
			// c.removeOutputLayer();
			System.out.println("c.removeLayersFromOutput(5);");
			c.removeLayersFromOutput(5);

			// c.removeOutputLayer();
			System.out.println("c.setFeatureExtractor(4);");
			c.setFeatureExtractor(4);

			System.out.println("FineTuneConfiguration.Builder()");
			int iterations2 = 500;
			FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder().iterations(iterations2)
					.activation(Activation.TANH).weightInit(WeightInit.XAVIER).learningRate(5e-5)
					.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).updater(Updater.NESTEROVS)
					.dropOut(0.5).seed(seed).backprop(true).pretrain(false).build();

			System.out.println("c.fineTuneConfiguration(fineTuneConf);");
			c.fineTuneConfiguration(fineTuneConf);

			System.out.println("new OutputLayer.Builder(LossFunctions.LossFunction.MSE)");
			// OutputLayer out = new
			// OutputLayer.Builder(LossFunctions.LossFunction.MSE)
			// .activation(Activation.TANH).
			// nIn(30).
			// nOut(1).
			// build();

			
			System.out.println("c.addLayer(out);");
			c.addLayer(new DenseLayer.Builder().nIn(5).nOut(5).build());
			OutputLayer out = new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
					.activation(Activation.SOFTMAX).nIn(5).nOut(numClasses).build();
			c.addLayer(out);

			System.out.println("c.build()");
			MultiLayerNetwork model2 = c.build();

			int x = 3;
			Map<java.lang.String, INDArray> param2 = model2.paramTable();
			for (Map.Entry<String, INDArray> entry : param2.entrySet()) {
				String key = entry.getKey().toString();
				INDArray value = entry.getValue();
				// if(x++==3)
				int size = value.toString().length();
				if (size > 100)
					size = 100;
				System.out.println("key, " + key + " value " + value.toString().substring(0, size));

			}
			// System.out.println("Param "+model2.paramTable());
			// System.out.println("---------------------------\n"+model2.getLayerWiseConfigurations().toJson()+"\n-------------------");

			// c.nOutReplace(5, 1, new NormalDistribution() );
			// addLayer(out);

			model2.setListeners(new ScoreIterationListener(10));
			// for (int i=0;i<1;i++){//while(iter.hasNext()) {
			model2.fit(trainingData);
			// }

			Map<java.lang.String, INDArray> param3 = model2.paramTable();
			for (Map.Entry<String, INDArray> entry : param3.entrySet()) {
				String key = entry.getKey().toString();
				INDArray value = entry.getValue();
				// if(x++==3)
				int size = value.toString().length();
				if (size > 100)
					size = 100;
				System.out.println("key, " + key + " value " + value.toString().substring(0, size));

			}

			
			// evaluate the model on the test set
			Evaluation eval = new Evaluation(5);
			
			//INDArray indarray=new 
			//Iterate testData.iterator();
			
			INDArray output = model2.output(testData.getFeatureMatrix());
			eval.eval(testData.getLabels(), output);
			System.out.println(eval.stats());

			ModelSerializer.writeModel(model2, "model.m", true);
			
			double inputExample[]={0.44551458447992087,0.16966683810503302,-0.3321230557872511,-1.0,0.14040606233915343,-0.09716678086712205,0.8984152783421453,-0.35555080823186214,-0.5062735986058726,-0.02601739670704506,0.04820256230479658,0.595503362441028,0.5269983894210484,-0.5306309251389426,-0.5392767630768707,-0.7029360103295457,-0.7978867710305673,-1.0,-0.7193740216752254,-0.5379036603480698,-0.790387402551268,0.5453044078417111,0.5046815846633801,0.9047170697885281,0.16529680939935454,0.8386165482995556,0.6980657833588042,0.6753647661048352,0.10687891602557675,0.17392244946372948,0.5221110675943752,-0.46978828320941457,0.5084842229056377,0.3706826246717114,0.7055395209357465};
			System.out.println(Nd4j.create(inputExample));
			INDArray oututTestOne=model2.output(Nd4j.create(inputExample));
			System.out.println(oututTestOne);
			for(int i=0;i<oututTestOne.columns();i++)
			{
				System.out.print(oututTestOne.getDouble(i)+",");
			}
			System.out.println();
			
			MultiLayerNetwork model3 = ModelSerializer.restoreMultiLayerNetwork("model.m");
			
			
			System.out.println(Nd4j.create(inputExample));
			oututTestOne=model3.output(Nd4j.create(inputExample));
			System.out.println(oututTestOne);
			for(int i=0;i<oututTestOne.columns();i++)
			{
				System.out.print(oututTestOne.getDouble(i)+",");
			}
			System.out.println();
		}

}
