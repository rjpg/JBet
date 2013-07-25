package main;

import java.io.File;

import org.encog.app.analyst.AnalystFileFormat;
import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.csv.normalize.AnalystNormalizeCSV;
import org.encog.app.analyst.wizard.AnalystWizard;
import org.encog.app.analyst.wizard.NormalizeRange;
import org.encog.app.analyst.wizard.WizardMethodType;
import org.encog.ml.data.specific.CSVNeuralDataSet;
import org.encog.util.csv.CSVFormat;
import org.encog.util.simple.EncogUtility;

import statistics.Statistics;
import DataRepository.Utils;
import categories.categories2011.CategoriesManager;
import categories.categories2011.ProcessThreshold;

public class LoaderNormalizeRawData {
	public static void main(String[] args)  throws Exception {
		Utils.init();
		Statistics.init();
		
		CategoriesManager.init();
		CategoriesManager.loadRawAMFromFile();
		CategoriesManager.processAMCatIntervals();

		
		//for(int i =0 ; i<CategoriesManeger.getCategoriesSize();i++)
		int i=16; //16
		{
			ProcessThreshold pt=new ProcessThreshold(i, 98);
			
			String catDir=CategoriesManager.getDirectory(i);
			System.out.println("---------------------------------------------------");
			System.out.println("Processing : "+catDir);
			//File sourceFile=new File(catDir+"/nn-raw-data.csv");
			File sourceFile=new File(catDir+"/nn-raw-data-threshold.csv");
			File targetFile=new File(catDir+"/nn-normalized-data.csv");
			
			System.out.println("Analyzing "+catDir+"/nn-raw-data-threshold.csv ...");
			EncogAnalyst analyst = new EncogAnalyst();
			AnalystWizard wizard = new AnalystWizard(analyst);
			wizard.setTargetField("field:"+(CategoriesManager.getCategory(i).getNumberInputValues()+1));
			System.out.println("Target field: \"field:"+(CategoriesManager.getCategory(i).getNumberInputValues()+1)+"\"");
			wizard.wizard(sourceFile, false, AnalystFileFormat.DECPNT_COMMA);

//			File statsFile=new File(catDir+"/nn-stats.ega");
//			analyst.load(statsFile);

			System.out.println("Analyzing Complete.");
			
			
			
			System.out.println("Normalizing to "+catDir+"/nn-normalized-data.csv ...");
			AnalystNormalizeCSV norm=new AnalystNormalizeCSV();
			norm.analyze(sourceFile, false, CSVFormat.ENGLISH, analyst);
			
			
			norm.setProduceOutputHeaders(false);
			
			
			
			norm.normalize(targetFile);
			System.out.println("Normalization Complete.");
			
			System.out.println("Saving normalization parameters to "+catDir+"/nn-stats.ega ...");
			File statsFile=new File(catDir+"/nn-stats.ega");
			analyst.save(statsFile);
			System.out.println("Save Complete.");
			
			System.out.println("Writing binary Encog training to "+catDir+"/nn-train-data.egb");
			File normalizaedCVSFile=new File(catDir+"/nn-normalized-data.csv");
			File targetEGBFile=new File(catDir+"/nn-train-data.egb");
			CSVNeuralDataSet csvnds=new CSVNeuralDataSet(normalizaedCVSFile.getAbsolutePath(), CategoriesManager.getCategory(i).getNumberInputValues(), 1, false);
			EncogUtility.saveEGB(targetEGBFile, csvnds);
			System.out.println("Write Complete.");
		}
	}
}
