package DataRepository;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import main.Parameters;

import com.sun.xml.bind.v2.runtime.RuntimeUtil.ToStringAdapter;

import GUI.MyChart2D;

public class RunnersData {
	public String name;
	
	public int id;
	
	public Vector<HistoryData> dataFrames=new Vector<HistoryData>();
	
	
	MarketData md=null;
	
	public RunnersData(String nameA,int idA, MarketData mdA)
	{
		md=mdA;
		name=nameA;
		id=idA;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Vector<HistoryData> getDataFrames() {
		return dataFrames;
	}
	
	public MarketData getMarketData() {
		return md;
	
	}
	
	public void addPricesData (Calendar timeA, double oddLayA,double amountLayA, double oddBackA,double amountBackA,/* double weightmoneyLayA, double weightmoneyBackA ,*/ double matchedAmountA, double lastMatchetA,  Vector<OddData> layPricesA, Vector<OddData> backPricesA,int state)
	{
		HistoryData d=new HistoryData(timeA, oddLayA, amountLayA, oddBackA, amountBackA,/* weightmoneyLayA, weightmoneyBackA,*/ matchedAmountA, lastMatchetA,layPricesA, backPricesA,state);
		dataFrames.add(d);
		
		if(Parameters.HISTORICAL_DATA_FRAMES_MEMORY<1)
			return;
		if(dataFrames.size()>Parameters.HISTORICAL_DATA_FRAMES_MEMORY)
		{
			dataFrames.get(0).clean();
			dataFrames.removeElementAt(0);
		}
		
		//System.out.println("size : "+dataFrames.size());
	}

	@Override
	public String toString()
	{
		return name+"("+id+")";
	}

	public void clean()
	{
		for(HistoryData d:dataFrames)
		{
			d.clean();
			
		}
		
		
		
		dataFrames.clear();
		dataFrames=null;
		md=null;
	}
}
