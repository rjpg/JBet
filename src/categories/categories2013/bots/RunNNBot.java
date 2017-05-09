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

	ProcessStats pc=new ProcessStats();
	
	public RunNNBot(MarketData md) {
		super(md, "RunNNBot");
		
		initialize();
	}

	public void initialize()
	{
		DataWindowsSizes.init();
		CategoriesParameters.COLLECT=false;
		//System.out.println("############################################ : "+CategoriesParameters.COLLECT);
		root=new Root(0);
		CategoryNode.printIDs(root);
		System.out.println("############################################ : "+CategoriesParameters.COLLECT);
		//CategoryNode.buildDirectories(root);
		
		//Vector<CategoryNode> cnv=CategoryNode.getAncestorsById(root,500);
		
		//for(CategoryNode cn:cnv)
		//	System.out.print(cn.getPath()+"\\");
		
	}

	public void newMarket(MarketData md)
	{
	
		setMd(md);
		
	}

	public boolean isProfitableCat()
	{
		
		return true;
	}
	
	int predict=0;
	int execute=0;
	public void update()
	{
		if(getMinutesToStart()==0) return;
		
		updateRunnerCategoryData();
		
		if(rcdv!=null && rcdv.size()>0)
		{
			predict++;
			execute++;
			//RunnerCategoryData rcd=rcdv.get(0);
			for(RunnerCategoryData rcd:rcdv)
			{	
				//Vector<CategoryNode> cat= rcd.getCat();
				//int idcat=cat.get(6).getIdStart();
				//if(pc.isProfitableCat(idcat))
				{
					if(predict==1)
					{
						int result=rcd.predict();
						System.out.println("result for "+rcd.getRd().getName()+" : "+result);
					}		
							
					if(execute==60)
						rcd.executePredictions();
							
				}
			}
			if(execute==60)
				execute=0;
			if(predict==1)
				predict=0;
				
				
		}
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
