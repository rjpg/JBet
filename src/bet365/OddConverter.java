package bet365;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import DataRepository.Utils;
import GUI.MessagePanel;

public class OddConverter {
	
	public int runners=3;
	public double bet365Ladder[]=new double[79];
	
	public JFrame frame;
	
	public OddSelector[] os;

	double odds[];
	
	double oddsFair[];
	
	public JLabel osLabel[];
	public JLabel osLabelFair[];

	public OddConverter(int runnersA) {
		runners=runnersA;
		initialize();
	}
	
	public void initialize()
	{
		loadLadderValues();
		
		os=new OddSelector[runners];

		odds=new double[runners];
		
		oddsFair=new double[runners];
		
		osLabel=new JLabel[runners];
		osLabelFair=new JLabel[runners];
				
		
		frame=new JFrame("Bet365 odd Converter");
		frame.setSize(640,480);
		JPanel aux=new JPanel();
		aux.setLayout(new GridLayout(1,runners));
		
		JPanel aux2[]=new JPanel[runners];
		
		for(int i=0;i<runners;i++)
		{
			osLabelFair[i]=new JLabel(""+oddsFair[i]);
			osLabel[i]=new JLabel(""+odds[i]);
			os[i]=new OddSelector(bet365Ladder, this);
			aux2[i]=new JPanel();
			aux2[i].setLayout(new BorderLayout());
			aux2[i].add(osLabelFair[i],BorderLayout.NORTH);
			aux2[i].add(os[i],BorderLayout.CENTER);
			aux2[i].add(osLabel[i],BorderLayout.SOUTH);
			
			
			aux.add(aux2[i]);
		}
	
		frame.setContentPane(aux);
		frame.setVisible(true);
	}
	
	
	public void loadLadderValues()
	{
		
		File f=new File("bet365odds.txt");
		BufferedReader input=null;
		try {
			input= new BufferedReader(new FileReader(f));
		} catch (Exception e) {
			e.printStackTrace();
		} 

		if(input==null)
		{
			System.err.println("File ("+f.getAbsolutePath()+") not processed");
			return;
		}
		
		
		String s;
		int i=0;
		try {
			while ((s=input.readLine()) != null)
			{
				
			
				bet365Ladder[i]=Double.parseDouble(s);
				i++;

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		try {
			input.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		for(i=0;i<bet365Ladder.length;i++)
		{
			System.out.println("bet365Ladder["+i+"]="+bet365Ladder[i]);
		}
		
	}
	
	public void newOdd(OddSelector osA,double odd)
	{
		
		for(int i=0;i<runners;i++)
		{
			if(osA==os[i])
			{
				System.out.println("new odd on "+ i+" :"+odd);
				osLabel[i].setText(""+odd);
				odds[i]=odd;
			}	
		}
		computeFairOdds();
	}
	
	public void computeFairOdds()
	{
		double totalProb=0;
		
		for(int i=0;i<runners;i++)
		{
			totalProb+=(1/odds[i]);
		}
		
		for(int i=0;i<runners;i++)
		{
			oddsFair[i]=(odds[i]*totalProb);
			
			osLabelFair[i].setText("("+Utils.nearValidOdd(oddsFair[i])+")"+"  "+oddsFair[i]);
		}
	}
	
	public static void main(String[] args) {
		Utils.init();
		new OddConverter(5);
	}
}
