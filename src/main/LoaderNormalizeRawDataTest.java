package main;

import java.io.File;

import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.csv.normalize.AnalystNormalizeCSV;
import org.encog.ml.data.specific.CSVNeuralDataSet;
import org.encog.util.csv.CSVFormat;
import org.encog.util.simple.EncogUtility;

import statistics.Statistics;
import DataRepository.Utils;
import categories.categories2011.CategoriesManager;

public class LoaderNormalizeRawDataTest {
	public static void main(String[] args)  throws Exception {
		Utils.init();
		Statistics.init();
		
		CategoriesManager.init();
		CategoriesManager.loadRawAMFromFile();
		CategoriesManager.processAMCatIntervals();

		
		//for(int i =0 ; i<CategoriesManeger.getCategoriesSize();i++)
		int i=29;
		{
			String catDir=CategoriesManager.getDirectory(i);
			System.out.println("---------------------------------------------------");
			System.out.println("Processing : "+catDir);
			File sourceFile=new File(catDir+"/nn-raw-data-test.csv");
			File targetFile=new File(catDir+"/nn-normalized-data-test.csv");
			
			System.out.println("Analyzing "+catDir+"/nn-raw-data.csv ...");
			EncogAnalyst analyst = new EncogAnalyst();
		/*	AnalystWizard wizard = new AnalystWizard(analyst);
			wizard.setTargetField("field:227");
			wizard.wizard(sourceFile, false, AnalystFileFormat.DECPNT_COMMA);
*/
			File statsFile=new File(catDir+"/nn-stats.ega");
			analyst.load(statsFile);
			System.out.println("Analyzing Complete.");
			
			System.out.println("Normalizing to "+catDir+"/nn-normalized-data.csv ...");
			AnalystNormalizeCSV norm=new AnalystNormalizeCSV();
			norm.analyze(sourceFile, false, CSVFormat.ENGLISH, analyst);
			
			
			norm.setProduceOutputHeaders(false);
			
			norm.normalize(targetFile);
			System.out.println("Normalization Complete.");
			
			/*System.out.println("Saving normalization parameters to "+catDir+"/nn-stats.ega ...");
			File statsFile=new File(catDir+"/nn-stats.ega");
			analyst.save(statsFile);
			System.out.println("Save Complete.");
			*/
			System.out.println("Writing binary Encog training to "+catDir+"/nn-train-data.egb");
			File normalizaedCVSFile=new File(catDir+"/nn-normalized-data-test.csv");
			File targetEGBFile=new File(catDir+"/nn-train-data-test.egb");
			CSVNeuralDataSet csvnds=new CSVNeuralDataSet(normalizaedCVSFile.getAbsolutePath(), CategoriesManager.getCategory(i).getNumberInputValues(), 1, false);
			EncogUtility.saveEGB(targetEGBFile, csvnds);
			System.out.println("Write Complete.");
		}
	}
}
