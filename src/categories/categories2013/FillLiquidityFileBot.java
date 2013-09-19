package categories.categories2013;

import java.awt.Color;
import java.util.Vector;

import org.omg.CORBA.INITIALIZE;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import bots.Bot;

public class FillLiquidityFileBot extends Bot{

	boolean farWritten=false;
	boolean mediumWritten=false;
	boolean nearWritten=false;

	
	Root root=new Root(0);
	
	public FillLiquidityFileBot(MarketData md) {
		super(md, "FillLiquidityFileBot");
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
		if(!farWritten)
		{
			if(getMinutesToStart()>8 && getMinutesToStart()<10)
			{
				for(RunnersData rd:getMd().getRunners())
				{
					Vector<CategoryNode> cat=CategoryNode.getAncestorsByRunner(root, rd);
					if(cat==null)
						System.out.println(rd.getName()+" has no category");
					else
						System.out.println(rd.getName()+" category id :"+CategoryNode.getAncestorsStringPath(cat));
					
				}
				farWritten=true;
			}
				
			
		}
		else if (!mediumWritten)
		{
			
		}else if(!nearWritten)
		{
			
		}
	}
	
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketNew)
			newMarket(md);
			
		if(marketEventType==MarketChangeListener.MarketUpdate)
			update();			
		
	}

	@Override
	public void writeMsg(String s, Color c) {
		System.out.println("Bot "+getName()+" Msg :"+s);
		
	}

}
