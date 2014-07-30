package categories.categories2013.bots;

import java.io.File;
import java.util.Calendar;
import java.util.Vector;

import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.TrainingContinuation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.simple.EncogUtility;

import categories.categories2013.CategoryNode;
import categories.categories2013.Root;

public class TrainSaveNN {

	public static boolean trainToA=false;  //15%
	public static boolean trainToB=false;  //12%
	public static boolean trainToC=false;  //10%
	public static boolean trainToD=false;  //8%
	public static boolean trainToE=false;  //5%
	public static boolean trainToF=false;  //8h
	
	public static void main(String[] args) {
		
		if(args.length<1)
			System.out.println("Missing argument (category ID)");
		for(int i = 1; i < args.length; i++) {
            System.out.println(args[i]);
		}
		
		int i=-1;
		
		i=Integer.parseInt(args[0]);
		
		if(i<0 || i>647)
			System.out.println("invalid category ID : "+i);
		
		
		Root root=new Root(0);
		
		//i=203;
		int usedCores=4;
		
		Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
		String fileName=CategoryNode.getAncestorsStringPath(cat)+"nn-train-data.egb";
		
		String fileSaveA=CategoryNode.getAncestorsStringPath(cat)+"nn-A.eg";
		String fileSaveB=CategoryNode.getAncestorsStringPath(cat)+"nn-B.eg";
		String fileSaveC=CategoryNode.getAncestorsStringPath(cat)+"nn-C.eg";
		String fileSaveD=CategoryNode.getAncestorsStringPath(cat)+"nn-D.eg";
		String fileSaveE=CategoryNode.getAncestorsStringPath(cat)+"nn-E.eg";
		String fileSaveF=CategoryNode.getAncestorsStringPath(cat)+"nn-F.eg";
		
		
		
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
            train.setThreadCount(usedCores);
            
            

    //        Calendar now=Calendar.getInstance();
            
    //        Calendar untilTime=Calendar.getInstance();
            
    //        untilTime.add(Calendar.HOUR, 6);

            double error=2;
			int epoch = 1;

			do {
				train.iteration();
				
				error=train.getError();
				
				if(epoch>50000 && !trainToA)
				{
					TrainingContinuation tc=train.pause();
					train.finishTraining();
					System.out.println("saving NN A ERROR : "+error);
					
					EncogDirectoryPersistence.saveObject(new File(fileSaveA), network);
					trainToA=true;
					train.resume(tc);
				}
				
				if(epoch>100000 && !trainToB)
				{
					TrainingContinuation tc=train.pause();
					train.finishTraining();
					System.out.println("saving NN B ERROR : "+error);
					EncogDirectoryPersistence.saveObject(new File(fileSaveB), network);
					trainToB=true;
					train.resume(tc);
				}
				
				if(epoch>150000 && !trainToC)
				{
					TrainingContinuation tc=train.pause();
					train.finishTraining();
					System.out.println("saving NN C ERROR : "+error);
					EncogDirectoryPersistence.saveObject(new File(fileSaveC), network);
					trainToC=true;
					train.resume(tc);
				}
				
//				if(error<0.08 && !trainToD)
//				{
//					TrainingContinuation tc=train.pause();
//					train.finishTraining();
//					System.out.println("saving NN D");
//					EncogDirectoryPersistence.saveObject(new File(fileSaveD), network);
//					trainToD=true;
//					train.resume(tc);
//				}
//				
//				if(error<0.05 && !trainToE)
//				{
//					TrainingContinuation tc=train.pause();
//					train.finishTraining();
//					System.out.println("saving NN E");
//					EncogDirectoryPersistence.saveObject(new File(fileSaveE), network);
//					trainToE=true;
//					train.resume(tc);
//				}
//							
		//		if(epoch % 100 == 0)
		//		{
		//			System.out.println("Epoch #" + epoch + " Error:" + train.getError());
		//		    now=Calendar.getInstance();
		//		}
				epoch++;
				          
			} while(epoch<200000 );
			train.finishTraining();

//	A		50 000
//	B		100 000
//	C		150 000
//	D		200 000
			
			System.out.println("saving NN F ERROR : "+error);
			EncogDirectoryPersistence.saveObject(new File(fileSaveF), network);
			trainToF=true;
			
			Vector<double[]> lastErrorTable=new Vector<double[]>();
			lastErrorTable.add(new double[]{error,(double)epoch});
			//ProcessNNRawData.writeTalbleFile(lastErrorTable, cat, "last-error.txt");
			
		}
		else
		{
			System.out.println("Train File Not found in "+fileName);
		}
		
	
		System.out.println("END TRAN for category : "+i);
		
		System.exit(0);
		
	}
}
