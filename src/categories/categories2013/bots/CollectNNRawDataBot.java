package categories.categories2013.bots;

import java.awt.Color;
import java.util.Vector;

import categories.categories2013.CategoryNode;
import categories.categories2013.Root;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import bots.Bot;

public class CollectNNRawDataBot extends Bot {

	
	Root root=new Root(0);
	
	boolean farActive=false;
	boolean mediumActive=false;
	boolean nearActive=false;
	
	Vector<RunnerCategoryData> rcdv=null;

	
	public CollectNNRawDataBot(MarketData md) {
		super(md, "CollectNNRawData");
		initialize();
	}

	public void initialize()
	{
	
		
		CategoryNode.printIDs(root);
		//CategoryNode.buildDirectories(root);
		
		//Vector<CategoryNode> cnv=CategoryNode.getAncestorsById(root,500);
		
		//for(CategoryNode cn:cnv)
		//	System.out.print(cn.getPath()+"\\");
		
	}

	public void newMarket(MarketData md)
	{
	
		setMd(md);
		
	}

	public void update()
	{
		
	}
	
	//pathroot\shortLenhgt\favorite\beginingDay\mediumRunners\farFromBegining\lowOdd\highLiquidity\
	
	public void reset()
	{
		
	}
	
	public void updateRunnerCategoryData()
	{
		int minuteToStart =getMinutesToStart();
		if(!farActive && (minuteToStart<10 || minuteToStart>5))
		{
			
		}
	}
	
	
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketNew)
		{
			newMarket(md);
			reset();
		}
		
		if(marketEventType==MarketChangeListener.MarketUpdate)
			update();			
		
	}

	@Override
	public void writeMsg(String s, Color c) {
		System.out.println("Bot "+getName()+" Msg :"+s);
		
	}

}
