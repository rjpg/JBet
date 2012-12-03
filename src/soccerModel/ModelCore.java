package soccerModel;

import nextGoal.BFDataInit;
import nextGoal.BetListCS;
import DataRepository.Utils;

public class ModelCore {
	
	public static int MAX_GOALS=12;
	
	double table[][];
	
	double AH;
	double home,away;
	
	
	public ModelCore() {
	
		initTable(0.0,2.5,1);
		
		calculateHandicap();
	}
	
	public double poisson(int k,double l)
	{
		
		int kfac=1;
		for(int i=k;i>0;i--)
		{
			kfac*=i;
		}
		
		double e=2.71828;
		
		double x=Math.pow(e, -l);
		
		x=x*Math.pow(l, k);
		
		if(k==0)
			return x;
		else
			return x/(double)kfac;
	}
	
	
	
	public void initTable(double sup,double totalGoals, double drawFactor)
	{
		table=new double[MAX_GOALS][MAX_GOALS];
		
		double goalA[]=new double[MAX_GOALS];
		double goalB[]=new double[MAX_GOALS];
		
		double lA=(sup/2)+(totalGoals/2);
		double lB=(totalGoals/2)-(sup/2);
		
		for(int i =0;i<MAX_GOALS;i++)
		{
			goalA[i]=poisson(i,lA);
			goalB[i]=poisson(i,lB);
		}
		
		String s="";
		for(int l=0;l<MAX_GOALS;l++)
		{
			for(int c=0;c<MAX_GOALS;c++)
			{
				if(c==l)
					table[l][c]=goalA[l]*goalB[c]*drawFactor;
				else
					table[l][c]=goalA[l]*goalB[c];
				
				s=s+" "+table[l][c];
			}
			s=s+"\n";
		}
		
		System.out.println(s);
	}
	
	public void calculateHandicap()
	{
		double ret[]=new double[(MAX_GOALS*2)+1];
		for(int i=0;i<(MAX_GOALS*2)+1;i++)
		{
			if(i==MAX_GOALS)
			{
				for(int x=0;x<MAX_GOALS;x++)
					ret[i]+=table[x][x];
			}
			else if(i<MAX_GOALS)
			{
				int ref=MAX_GOALS-i+1;
				
				int steps=MAX_GOALS-ref;
				
				for(int x=0;x<steps;x++)
				{
					ret[i]+=table[ref-1+x][x];
				}
			}
			else if(i>MAX_GOALS)
			{
				int ref=i-MAX_GOALS+1;
				
				int steps=MAX_GOALS-ref;
				
				for(int x=0;x<steps;x++)
				{
					ret[i]+=table[x][ref-1+x];
				}
			}
			
			System.out.println("ret["+i+"]="+ret[i]);
		}
		
		
		
		double auxA[]=new double[25];
		double auxB[]=new double[25];
		
		double scale1[]=new double[25];
		double scale2[]=new double[25];
		
		double stepAux=-3.0;
		for(int i=0;i<25;i+=2)
		{
			scale1[i]=stepAux;
			if(i+1<25)
				scale1[i+1]=stepAux;
			
			scale2[i]=stepAux;
			stepAux+=0.5;
			if(i+1<25)
				scale2[i+1]=stepAux;
			
			System.out.println("scale1["+i+"]="+scale1[i]+"scale2["+i+"]="+scale2[i]);
			
		}
		
		for(int i=0;i<25;i++)
			System.out.println("scale1["+i+"]="+scale1[i]+"scale2["+i+"]="+scale2[i]);
	
		
	}
	
	public static void main(String[] args)  throws Exception {
		ModelCore mc=new ModelCore();
		
	}
}
