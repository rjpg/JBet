package GUI;

import java.awt.Component;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import bets.BetData;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;

public class MarketMainFrame extends JFrame  implements MarketChangeListener{
	
	private JPanel contentPane;
	public Vector<RunnerButton> vectorRdb=new Vector<RunnerButton>();
	
	
	public MarketData md;
	
	public MarketMainFrame( MarketData mdA) {
		super(mdA.getName());
		md=mdA;
		contentPane=new JPanel(new FlowLayout());
		this.setContentPane(contentPane);
		md.addMarketChangeListener(this);
		
		
		
	}





	public void MarketUpdate(MarketData md) {
		
		//System.out.println("updateeeee");	
		
		////////////////////////////// Frame with time to end ///////////////////////////
		Calendar c=md.getStart();
		Calendar now=Calendar.getInstance();
		
		
		
		//////////////////////////// Runners Visual components update ////////////////////
		if(md==null)
			return;
		
		
		
		for(RunnersData rd:md.getRunners())
		{
			boolean found=false;
			for(RunnerButton rdb:vectorRdb)
			{
				if(rdb.getRunnerData().getId()==rd.getId())
				{
					found=true;
					rdb.update();
					now=rdb.getRunnerData().getDataFrames().get(rdb.getRunnerData().getDataFrames().size()-1).getTimestamp();
				}
			}
			if(!found)
			{
				RunnerButton nrdb=new RunnerButton(rd);
				vectorRdb.add(nrdb);
				contentPane.add(nrdb);
				System.out.println("added "+ rd);
				contentPane.doLayout();
			}
		}
		
		
		long difmillis=c.getTimeInMillis()-now.getTimeInMillis();
		Calendar dif=Calendar.getInstance();
		dif.setTimeInMillis(difmillis);
		
		
		this.setTitle(md.getName()+dif.getTime().toString().split(" ")[3]);
		//////////////////////////////////////////////////////////////////////////////////
	
	}


	public void MarketChange(MarketData md) {
		clean();
		this.setTitle(md.getName());
		
		for(RunnersData rd:md.getRunners())
		{
				RunnerButton nrdb=new RunnerButton(rd);
				vectorRdb.add(nrdb);
				contentPane.add(nrdb);
				System.out.println("added "+ rd);
				contentPane.doLayout();
		
		}
		contentPane.doLayout();
		
	}
	
	public void clean()
	{
		System.out.println("Clean from MarkeMainFrame was called");
		
		for(RunnerButton rdb:vectorRdb)
		{
			rdb.clean();
		}
		vectorRdb.clear();
		
		for(Component comp:contentPane.getComponents())
		{
			if(comp instanceof RunnerButton )
			{
				RunnerButton rdb=(RunnerButton)comp;
				{
		
					rdb.clean();
					rdb.removeAll();
					rdb=null;
					
				}
			}
		}
		
		contentPane.removeAll();
		contentPane.doLayout();
		contentPane.repaint();
		Runtime r = Runtime.getRuntime();
		r.gc();
		r.freeMemory();
		r=null;
	}
	
	public RunnerFrame getRunnerFrame(int id)
	{
		RunnerFrame ret=null;
		for(Component comp:contentPane.getComponents())
		{
			if(comp instanceof RunnerButton )
			{
				RunnerButton rdb=(RunnerButton)comp;
				if(rdb.getRunnerData().getId()==id)
				{
					ret=rdb.getRunnerFrame();
				}
			}
		}
		
		return ret;
	}


	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		
		if(marketEventType==MarketChangeListener.MarketNew)
		{
			MarketChange(md);
			return;
		}
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			MarketUpdate(md);
			return;
		}
		//if(marketEventType==MarketChangeListener.MarketLive)
		//	MarketLive(md);
		
	}


/*	@Override
	public void MarketBetChange(MarketData md, BetData bd,
			int marketBetEventType) {
		// TODO Auto-generated method stub
		
	}
*/

	







}
