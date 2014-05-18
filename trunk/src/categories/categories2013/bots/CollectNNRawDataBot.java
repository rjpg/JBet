package categories.categories2013.bots;

import java.awt.Color;
import java.util.Vector;

import categories.categories2013.CategoriesParameters;
import categories.categories2013.CategoryNode;
import categories.categories2013.Root;
import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
import bots.Bot;

public class CollectNNRawDataBot extends Bot {

	
	Root root=null;
	
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
		DataWindowsSizes.init();
		CategoriesParameters.COLLECT=true;
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

	//int framesMinute=0;
	//int minute=0;
	public void update()
	{
		/*int minuteAux=getMinutesToStart();
		
		if(minuteAux!=minute)
		{
			System.out.println("Frames minute : "+framesMinute);
			minute=minuteAux;
			framesMinute=0;
		}
		else
			framesMinute++;
		*/
		
		
		updateRunnerCategoryData();
		
		if(rcdv!=null && rcdv.size()>0)
		{
			RunnerCategoryData rcd=rcdv.get(0);
			//for(RunnerCategoryData rcd:rcdv)
			{
				System.out.println("Runner "+rcd.getRd().getName()+" inputs:");
				Vector<Double> inputs=rcd.generateNNInputs();
				if(inputs!=null)
				{
					//for(int i=0;i<270;i++)
					//	System.out.print(Utils.getOddLayFrame(rcd.getRd(), i)+"("+i+") ");
					//System.out.println();
					for(Double v:inputs)
					{
						System.out.print(v+" ");
					}
				}
				else
					System.out.print("No inputs generated");
				System.out.println("");
				
				System.out.println("output : " + rcd.generateNNOutput());
			}
		}
		
	//	RunnersData rd=getMd().getRunners().get(10);
	//	if(Utils.isValidWindow(rd, 27, 0))
		{
//			UtilsCollectData.getAmountOfferVariationBackDepthWindow(rd, 1, 9,4);
//			System.out.println("AVG Back : "+UtilsCollectData.getAmountOfferVariationAVGBackDepthWindow(rd, 0, 9,4));
//			UtilsCollectData.getAmountOfferVariationLayDepthWindow(rd, 1, 9,4);
//			System.out.println("AVG Lay : "+UtilsCollectData.getAmountOfferVariationAVGLayDepthWindow(rd, 0, 9,4));

//			System.out.println("Volume Variation Lay :"+UtilsCollectData.getAmountMatchedVariationFrame(rd, 0, Utils.indexToOdd(Utils.oddToIndex(Utils.getOddBackFrame(rd, 0)))  ));

//			System.out.println("Volume Variation depth : "+UtilsCollectData.getAmountMatchedVariationAxisFrame(rd,0,4));
			
//			System.out.println("Volume Variation depth AVG window : "+UtilsCollectData.getAmountMatchedVariationAVGAxisWindow(rd,0,9,4));
//			System.out.println("Volume Variation depth AVG window : "+UtilsCollectData.getAmountMatchedVariationAVGAxisWindow(rd,9,18,4));
//			System.out.println("");
			
//			System.out.println("For Lay :"+UtilsCollectData.getAmountOfferVariationOddFrame(rd, 0, Utils.indexToOdd(Utils.oddToIndex(Utils.getOddBackFrame(rd, 0))+1)  ));
//			System.out.println("For Back :"+UtilsCollectData.getAmountOfferVariationOddFrame(rd, 0, Utils.getOddBackFrame(rd, 0)));
//			
//			System.out.println("For Back depth:"+UtilsCollectData.getAmountOfferVariationBackDepthFrame(rd,0,2));
//			System.out.println("For Lay depth:"+UtilsCollectData.getAmountOfferVariationLayDepthFrame(rd,0,2));
		}
		
//		if(rcdv!=null)
//			for(RunnerCategoryData rcd:rcdv)
//			{
//				System.out.println("Odd Variation on last 80 frames is :"+rcd.generateNNOutput()+" for "+rcd.getRd().getName());
//			}
				
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
		
		if(! Utils.isValidWindow(getMd().getRunners().get(0), CategoriesParameters.FRAMES_PREDICTION+2, 0))
			return;
		
		int timeOffSet=0;
		if(CategoriesParameters.COLLECT)
		{
			timeOffSet=CategoriesParameters.MINUTES_PREDICTION;
		}

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
