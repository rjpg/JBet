package categories.categories2013;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;
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
			if(getMinutesToStart()>8 && getMinutesToStart()<=10)
			{
				for(RunnersData rd:getMd().getRunners())
				{
					Vector<CategoryNode> cat=CategoryNode.getAncestorsByRunner(root, rd);
					if(cat==null)
						System.out.println(rd.getName()+" has no category");
					else
					{
						System.out.println(rd.getName()+" category id (start):"+cat.get(cat.size()-1).getIdStart()+" (end):"+cat.get(cat.size()-1).getIdEnd()+" path : "+CategoryNode.getAncestorsStringPath(cat));
						writeLiquidity(rd,cat);
					}
					
				}
				farWritten=true;
			}
				
			
		}
		else if (!mediumWritten)
		{
			if(getMinutesToStart()>3 && getMinutesToStart()<=4)
			{
				for(RunnersData rd:getMd().getRunners())
				{
					Vector<CategoryNode> cat=CategoryNode.getAncestorsByRunner(root, rd);
					if(cat==null)
						System.out.println(rd.getName()+" has no category");
					else
					{
						System.out.println(rd.getName()+" category id (start):"+cat.get(cat.size()-1).getIdStart()+" (end):"+cat.get(cat.size()-1).getIdEnd()+" path : "+CategoryNode.getAncestorsStringPath(cat));
						writeLiquidity(rd,cat);
					}
					
				}
				mediumWritten=true;
			}
		}else if(!nearWritten)
		{
			if(getMinutesToStart()>=0 && getMinutesToStart()<=1)
			{
				for(RunnersData rd:getMd().getRunners())
				{
					Vector<CategoryNode> cat=CategoryNode.getAncestorsByRunner(root, rd);
					if(cat==null)
						System.out.println(rd.getName()+" has no category");
					else
					{
						System.out.println(rd.getName()+" category id (start):"+cat.get(cat.size()-1).getIdStart()+" (end):"+cat.get(cat.size()-1).getIdEnd()+" path : "+CategoryNode.getAncestorsStringPath(cat));
						writeLiquidity(rd,cat);
					}
					
				}
				nearWritten=true;
			}
		}
	}
	
	//pathroot\shortLenhgt\favorite\beginingDay\mediumRunners\farFromBegining\lowOdd\highLiquidity\
	
	public void reset()
	{
		farWritten=false;
		mediumWritten=false;
		nearWritten=false;
	}
	
	public void writeLiquidity(RunnersData rd,Vector<CategoryNode> cat)
	{
		String fileName=cat.get(0).getPath();;
		for(int i=1;i<cat.size()-2;i++)
			fileName+="/"+cat.get(i).getPath();
		
		fileName+="/liquitidyFile.txt";
		
		System.out.println("writing to file : "+fileName);
		
		BufferedWriter out=null;
			
		try {
			out = new BufferedWriter(new FileWriter(fileName, true));
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open "+fileName+" for writing");
			}
		if(out==null)
		{
			System.err.println("could not open "+fileName);
			return;
		}
		
		try {
			out.write(Utils.getMatchedAmount(rd, 0)+"");
			out.newLine();
			out.flush();
		} catch (IOException e) {
			System.out.println(fileName+":Error wrtting data to log file");
			e.printStackTrace();
		}
		
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
