package categories.categories2013.bots;

import java.io.File;
import java.util.Vector;

import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.simple.EncogUtility;

import categories.categories2013.CategoryNode;
import categories.categories2013.Root;

public class TrainSaveNN {

	
	public static void main(String[] args) {
		
		Root root=new Root(0);
		
		int i=203;
		int usedCores=2;
		
		Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
		String fileName=CategoryNode.getAncestorsStringPath(cat)+"nn-train-data.egb";
		System.out.println("-----------------------------------------------------");
		System.out.println("--- Processing Category "+i+" : "+CategoryNode.getAncestorsStringPath(cat)+" ---");
		
		File file = new File(fileName);
		if(file.exists()) { 
			System.out.println("Train File found in "+fileName);
			
			System.out.println("Loading "+fileName);
			
			MLDataSet trainingSet=EncogUtility.loadEGB2Memory(new File(fileName));
			
			System.out.println("Preparing NN");
			
			BasicNetwork network = EncogUtility.simpleFeedForward(DataWindowsSizes.INPUT_NEURONS, DataWindowsSizes.MIDLE_LAYER_NEURONS, 0, 1, true);
//			network.addLayer(new BasicLayer(DataWindowsSizes.INPUT_NEURONS));
//			network.addLayer(new BasicLayer(DataWindowsSizes.MIDLE_LAYER_NEURONS));
//			network.addLayer(new BasicLayer(1));
//			network.getStructure().finalizeStructure();
//			network.reset();
			
			final ResilientPropagation train = new ResilientPropagation(network, trainingSet);
            train.setThreadCount(2);
            
			int epoch = 1;

			do {
				train.iteration();
				System.out.println("Epoch #" + epoch + " Error:" + train.getError());
				epoch++;
			} while(train.getError() > 0.01);
			train.finishTraining();

			
		}
		else
		{
			System.out.println("Train File Not found in "+fileName);
		}
		
	

		
	}
}
