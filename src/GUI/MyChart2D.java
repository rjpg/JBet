package GUI;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import main.Parameters;

import aw.gui.chart.Chart2D;
import aw.gui.chart.Trace2DLtd;
import aw.util.Range;

public class MyChart2D extends Chart2D {
	
	private Hashtable<String, Trace2DLtd> traces = new Hashtable<String, Trace2DLtd>();

	private int maxPoints = Parameters.CHART_FRAMES;
	
	private Calendar startTime;
	private Calendar endTime;
	
	long frame=0;
	
	public MyChart2D() {
		super();
		
		//startTime=Calendar.getInstance();
		
		this.setGridY(true);
		this.setScaleY(true);
		this.setGridX(true);
		this.setScaleX(true);
		
		//setXRangeTimeNowTo (startTime.getTimeInMillis());
		
	}
	
	public MyChart2D(Calendar endA)
	{
		super();
		
		startTime=Calendar.getInstance();
		this.setGridY(true);
		this.setScaleY(true);
		this.setGridX(true);
		this.setScaleX(true);
		
		endTime=endA;
	}
	
	
	public void setXRange (double min,double max)
	{
		this.setScaleX(false);
		setForceXRange(new Range(min,max));
	}

	public void setXRangeTimeNowTo (double max)
	{
		setXRange (startTime.getTimeInMillis(),max);
	}
	
	public void setYRange (double min,double max)
	{
		this.setScaleY(false);
		setForceYRange(new Range(min,max));
	}
	
	public void addValue(String traceName, double x, double y,Color c) {
		
		
		
		Trace2DLtd tmp = traces.get(traceName);
		if (tmp == null) {
			tmp = new Trace2DLtd(maxPoints);
			tmp.setName(traceName);
			tmp.setColor(c);
			this.addTrace(tmp);
			traces.put(traceName, tmp);			
		}
		tmp.setColor(c);
		//traces.put(traceName, tmp);
		
		//if(y>20) y=20;
		
		tmp.addPoint(x,y);
		
		
	}
	
	public void removeTrace(String traceName) {
		if (traces.containsKey(traceName)) {
			this.removeTrace(traces.get(traceName));
			traces.remove(traceName);
		}
	}
	
	public void setMaxPoints(int n)
	{
		Enumeration<String> e = traces.keys();
		while(e.hasMoreElements())
		{
			String key=e.nextElement();
			
			Trace2DLtd tdl=traces.get(key);
			tdl.setMaxSize(n);
		}
	}
	
	public static void main(String[] args)
	{
		JFrame frame=new JFrame();
		
		MyChart2D chart=new MyChart2D();
		
		final MyChart2D chart2=new MyChart2D();
		
		JButton b=new JButton("resize");
		b.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				chart2.setMaxPoints(30);
			}
		});
		
		JButton b2=new JButton("resize2");
		b2.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				chart2.setMaxPoints(10);
			}
		});
		
		JPanel panel=new JPanel();
		frame.setSize(500,500);
		panel.setLayout(new GridLayout(2,1));
		panel.add(chart);
		panel.add(chart2);
		panel.add(b);
		panel.add(b2);
		frame.setContentPane(panel);
		frame.setVisible(true);
		
		Random generator = new Random( 19580427 );
		
		double odd=0;
		double odd1=0;
		double odd2=0;
		double money1=0;
		double money2=0;
		while (true) {
			 
			odd = (generator.nextDouble() * 1)-0.5;
			
			odd1+=odd;
			chart.addValue("hourse-1", System.currentTimeMillis(), odd1, Color.RED);
			odd = (generator.nextDouble() * 1)-0.5;
			odd2+=odd;
			chart.addValue("hourse-2", System.currentTimeMillis(), odd2, Color.BLUE);
			
			odd = (generator.nextDouble() * 10)-5;
			money1+=odd;
			chart2.addValue("money-1", System.currentTimeMillis(), money1, Color.RED);
			odd = (generator.nextDouble() * 10)-5;
			money2+=odd;
			chart2.addValue("money-2", System.currentTimeMillis(), money2, Color.BLUE);
			
			
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//sleep for 1000 ms
				
		}
	}
	
}
