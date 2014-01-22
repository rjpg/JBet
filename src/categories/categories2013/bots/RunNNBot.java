package categories.categories2013.bots;

import java.awt.Color;
import java.util.Vector;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import bots.Bot;
import categories.categories2013.CategoriesParameters;
import categories.categories2013.CategoryNode;
import categories.categories2013.Root;

public class RunNNBot extends Bot {

	
	Root root=null;
	
	boolean farActive=false;
	boolean mediumActive=false;
	boolean nearActive=false;
	
	Vector<RunnerCategoryData> rcdv=null;

	
	public RunNNBot(MarketData md) {
		super(md, "RunNNBot");
		initialize();
	}

	public void initialize()
	{
	
		CategoriesParameters.COLLECT=false;
		root=new Root(0);
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
		updateRunnerCategoryData();
	}
	
	//pathroot\shortLenhgt\favorite\beginingDay\mediumRunners\farFromBegining\lowOdd\highLiquidity\
	
	public void reset()
	{
		if(rcdv!=null)
		{
			rcdv.removeAllElements();
			rcdv.clear();
			rcdv=null;
			rcdv=new Vector<RunnerCategoryData>();
		}	
		
		farActive=false;
		mediumActive=false;
		nearActive=false;
		
	}
	
	public void updateRunnerCategoryData()
	{
		
		
		
		int timeOffSet=0;
		if(CategoriesParameters.COLLECT)
			timeOffSet=1;
		
		int minuteToStart =getMinutesToStart();
		if(!farActive && (minuteToStart<=(10 - timeOffSet) && minuteToStart>=(5 - timeOffSet)))
		{
			System.out.println("activating far");
			
			farActive=true;
			if(rcdv!=null)
			{
				rcdv.removeAllElements();
				rcdv.clear();
				rcdv=null;
				
			}	
			
			rcdv=new Vector<RunnerCategoryData>();
			
			for(RunnersData rd:getMd().getRunners())
			{
				Vector<CategoryNode> cat=CategoryNode.getAncestorsByRunner(root, rd);
				if(cat==null)
					System.out.println(rd.getName()+" has no category");
				else
				{
					System.out.println(rd.getName()+" category id (start):"+cat.get(cat.size()-1).getIdStart()+" (end):"+cat.get(cat.size()-1).getIdEnd()+" path : "+CategoryNode.getAncestorsStringPath(cat));
					rcdv.add(new RunnerCategoryData(rd, cat));
				}
				
			}
			
		
			
		}
		
		if(!mediumActive && (minuteToStart<=(4 - timeOffSet) && minuteToStart>=(2 - timeOffSet)))
		{
			mediumActive=true;
			
			System.out.println("activating medium");
			
			farActive=true;
			if(rcdv!=null)
			{
				rcdv.removeAllElements();
				rcdv.clear();
				rcdv=null;
				
			}	
			
			rcdv=new Vector<RunnerCategoryData>();
			
			for(RunnersData rd:getMd().getRunners())
			{
				Vector<CategoryNode> cat=CategoryNode.getAncestorsByRunner(root, rd);
				if(cat==null)
					System.out.println(rd.getName()+" has no category");
				else
				{
					System.out.println(rd.getName()+" category id (start):"+cat.get(cat.size()-1).getIdStart()+" (end):"+cat.get(cat.size()-1).getIdEnd()+" path : "+CategoryNode.getAncestorsStringPath(cat));
					rcdv.add(new RunnerCategoryData(rd, cat));
				}
				
			}
		}
		
		if(!nearActive && (minuteToStart<=(1 - timeOffSet) && minuteToStart>=(0 - timeOffSet)))
		{
			nearActive=true;
			
			System.out.println("activating near");
			
			farActive=true;
			if(rcdv!=null)
			{
				rcdv.removeAllElements();
				rcdv.clear();
				rcdv=null;
				
			}	
			
			rcdv=new Vector<RunnerCategoryData>();
			
			for(RunnersData rd:getMd().getRunners())
			{
				Vector<CategoryNode> cat=CategoryNode.getAncestorsByRunner(root, rd);
				if(cat==null)
					System.out.println(rd.getName()+" has no category");
				else
				{
					System.out.println(rd.getName()+" category id (start):"+cat.get(cat.size()-1).getIdStart()+" (end):"+cat.get(cat.size()-1).getIdEnd()+" path : "+CategoryNode.getAncestorsStringPath(cat));
					rcdv.add(new RunnerCategoryData(rd, cat));
				}
				
			}
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
