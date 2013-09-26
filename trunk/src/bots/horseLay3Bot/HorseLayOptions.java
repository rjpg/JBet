package bots.horseLay3Bot;

import java.io.File;
import java.util.Vector;

import com.rapidminer.Process;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.ModelApplier;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.ModelLoader;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;

public class HorseLayOptions {
	double entryOdd=4;
	double aboveOdd=6;
	
	double numberOffRunnersLow=1;
	double numberOffRunnersHigh=30;
	
	double timeHourLow=10;
	double timeHourHigh=22;
	
	double lenghtInSecondsLow=0;
	double lenghtInSecondsHigh=1000;
	
	double liquidityLow=1000;
	double liquidityHigh=10000000;

	public boolean useRapidMinerModel=true;
	public Process rapidMinerProcess= null;
	public ModelApplier applier=null;
	
	public HorseLayOptions(boolean useRapidMinerModelA,double entryOddA,double aboveOddA)
	{
		entryOdd=entryOddA;
		aboveOdd=aboveOddA;
		useRapidMinerModel=useRapidMinerModelA;
		
		if(useRapidMinerModel)
		{
			loadRapidMinerProcess();
		}
	}
	
	public void loadRapidMinerProcess()
	{
		// instanciate model 
		ModelLoader model;
		try {
			model = OperatorService.createOperator(ModelLoader.class);
		
			File modelFilePath=new File("horseLayModels/model-"+entryOdd+"-"+aboveOdd+".mod");
			model.setParameter("model_file", modelFilePath.getAbsolutePath());
			
			// instanciate apply model operator
			applier = OperatorService.createOperator(ModelApplier.class);
			// connect model to applier
	        model.getOutputPorts().getPortByIndex(0).connectTo(applier.getInputPorts().getPortByIndex(0));
	        // create process
	        rapidMinerProcess = new com.rapidminer.Process();
	        rapidMinerProcess.getRootOperator().getSubprocess(0).addOperator(model);
	        rapidMinerProcess.getRootOperator().getSubprocess(0).addOperator(applier);
			// connect process to input of apply model data set
	        rapidMinerProcess.getRootOperator().getSubprocess(0).getInnerSources().getPortByIndex(0).connectTo( 
		        		applier.getInputPorts().getPortByIndex(1));
	        
        
		} catch (OperatorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	public boolean isEntryConditions(double numberOffRunners,double timeHour,double lenghtInSeconds, double liquidity)
	{
		if(useRapidMinerModel)
		{
			
			// create attribute for every column
			Vector<Attribute> listOfAttributes= new Vector<Attribute>();
			listOfAttributes.add(AttributeFactory.createAttribute("NRunners",Ontology.INTEGER ));
			listOfAttributes.add(AttributeFactory.createAttribute("Hour",Ontology.INTEGER ));
			listOfAttributes.add(AttributeFactory.createAttribute("Lenght",Ontology.INTEGER ));
			listOfAttributes.add(AttributeFactory.createAttribute("Liquidity",Ontology.REAL ));
			
			MemoryExampleTable table = new MemoryExampleTable(listOfAttributes);
			
			// add data as datarow
			// provide example set to apply model operator
			DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
			DataRow testRow = factory.create(new Double []{numberOffRunners, timeHour, lenghtInSeconds, liquidity}, listOfAttributes.toArray(new Attribute[]{}));
			table.addDataRow(testRow);
			//testRow = factory.create(new Double []{10.,16.,248.,235980.29}, listOfAttributes.toArray(new Attribute[]{}));
			//table.addDataRow(testRow);
	        		
			ExampleSet exampleset= table.createExampleSet();
	        
			// execute process with table as input
			ExampleSet data=null;
			try {
				rapidMinerProcess.run(new IOContainer(new IOObject[] { exampleset }));
			
			
		        // check output of applier
				OutputPort portByIndex = applier.getOutputPorts().getPortByIndex(0);
				data = portByIndex.getData(ExampleSet.class);
			} catch (OperatorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double value = data.getExample(0).getValue(data.getAttributes().getPredictedLabel());
			//double confidence = data.getExample(0).getValue(data.getAttributes().getConfidence("3"));
			double retrunPredPL=Double.parseDouble(data.getAttributes().getPredictedLabel().getMapping().mapIndex((int)value));
			//System.out.println("data RETURN = " +data);
			//System.out.println("DOUBLE RETURN = " +value+" - " + confidence);
			
			System.out.println("PRED : "+numberOffRunners+" , "+ timeHour+" , "+ lenghtInSeconds+ " ,"+ liquidity+" = "+retrunPredPL);
			if(retrunPredPL>=0)
				return true;
			else
				return false;
		}
		else
		{
			if(numberOffRunners<getNumberOffRunnersLow() || numberOffRunners>getNumberOffRunnersHigh())
				return false;
			
			if(timeHour<getTimeHourLow() || timeHour>getTimeHourHigh())
				return false;
										
			if(lenghtInSeconds<getLenghtInSecondsLow() || lenghtInSeconds>getLenghtInSecondsHigh())
				return false;
			                                                //  1128191,72  
			if(liquidity<getLiquidityLow() || liquidity>getLiquidityHigh())
				return false;
			
			return true;
		}
	}
	
	public double getEntryOdd() {
		return entryOdd;
	}
	public void setEntryOdd(double entryOdd) {
		this.entryOdd = entryOdd;
	}
	public double getAboveOdd() {
		return aboveOdd;
	}
	public void setAboveOdd(double aboveOdd) {
		this.aboveOdd = aboveOdd;
	}
	public double getNumberOffRunnersLow() {
		return numberOffRunnersLow;
	}
	public void setNumberOffRunnersLow(double numberOffRunnersLow) {
		this.numberOffRunnersLow = numberOffRunnersLow;
	}
	public double getNumberOffRunnersHigh() {
		return numberOffRunnersHigh;
	}
	public void setNumberOffRunnersHigh(double numberOffRunnersHigh) {
		this.numberOffRunnersHigh = numberOffRunnersHigh;
	}
	public double getTimeHourLow() {
		return timeHourLow;
	}
	public void setTimeHourLow(double timeHourLow) {
		this.timeHourLow = timeHourLow;
	}
	public double getTimeHourHigh() {
		return timeHourHigh;
	}
	public void setTimeHourHigh(double timeHourHigh) {
		this.timeHourHigh = timeHourHigh;
	}
	public double getLenghtInSecondsLow() {
		return lenghtInSecondsLow;
	}
	public void setLenghtInSecondsLow(double lenghtInSecondsLow) {
		this.lenghtInSecondsLow = lenghtInSecondsLow;
	}
	public double getLenghtInSecondsHigh() {
		return lenghtInSecondsHigh;
	}
	public void setLenghtInSecondsHigh(double lenghtInSecondsHigh) {
		this.lenghtInSecondsHigh = lenghtInSecondsHigh;
	}
	public double getLiquidityLow() {
		return liquidityLow;
	}
	public void setLiquidityLow(double liquidityLow) {
		this.liquidityLow = liquidityLow;
	}
	public double getLiquidityHigh() {
		return liquidityHigh;
	}
	public void setLiquidityHigh(double liquidityHigh) {
		this.liquidityHigh = liquidityHigh;
	}
	
}
