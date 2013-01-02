package soccerModel;


public class ModelCore {
	
	public static int TABLE_MAX_GOALS=12;
		
	//double AH;
	//double home,away;
	
	//global Table
	double table[][];
	double totalTable=0;
	
	//Handicap
	double hcapHome[];
	double hcapAway[];
	double hcapSumHomeAway[];
	double hcapAVGScale[];
	
	
	//Total Goals
	public static int MAX_GOALS=7;
	double goalsOver[];
	double goalsUnder[];
	double goalsSumOverUnder[];
	double goalsAVGScale[];
	
	//Model Inputs
	double goalLine=2.5;
	double asianHcap=0;
	
	double gLineOver=1.8;
	double gLineUnder=1.8;
	
	double aHcapHome=1.8;
	double aHcapAway=1.8;
	
	double desAH=0;
	double desGL=0;
	
	
	public ModelCore() {
	
		initTable(2.5,2.5,1);
		
		calculateHandicap();
		
		calculateTotalGoals();
		/*
		System.out.println(cumputeNearHcap(1.0,2.5));
		System.out.println(cumputeNearTG(1.0,2.5));*/
		
		supremacy();
		
	}
	
	
	
	public double poisson(int k,double l)
	{
		
		int kfac=1;
		for(int i=k;i>0;i--)
		{
			kfac*=i;
		}
		
		double x= Math.exp(-l);
		
		x=x*Math.pow(l, k);
		
		if(k==0)
			return x;
		else
			return x/(double)kfac;
	}
	
	public void supremacy()
	{
		desAH=aHcapHome*(1/aHcapHome+1/aHcapAway);
		desGL=gLineOver*(1/gLineOver+1/gLineUnder);
		
		double sup=0;
		double tg=2.5;
		
		double nearHcap=cumputeNearHcap(hcapAVGScale[0],tg);
		int neari=0;
		double nearHcapaux=0;
		
		for(int i=1;i<hcapAVGScale.length;i++)
		{
			if(Math.abs(hcapAVGScale[i])<=tg)
			{
				nearHcapaux=cumputeNearHcap(hcapAVGScale[i],tg);
				double diff1=Math.abs(nearHcap-desAH);
				double diff2=Math.abs(nearHcapaux-desAH);
				if (diff1>diff2)
				{
					nearHcap=nearHcapaux;
					neari=i;
				}
			}
		}
		
		sup=hcapAVGScale[neari];
		
		System.out.println("sup found : "+sup);		
	}
	
	
	public double cumputeNearHcap(double sup,double totalGoals)
	{
		initTable(sup,totalGoals,1);
		
		calculateHandicap();
		
		//calculateTotalGoals();
		
		int indexMin=0;
		
		
		for(int i=0;i<hcapSumHomeAway.length;i++)
		{
			if(hcapSumHomeAway[i]<hcapSumHomeAway[indexMin])
			{
				indexMin=i;
			}
		}
		System.out.println("minimo sumHomeAway["+indexMin+"]="+hcapSumHomeAway[indexMin]);
		return hcapHome[indexMin];
		
	}
	
	public double cumputeNearTG(double sup,double totalGoals)
	{
		initTable(sup,totalGoals,1);
		
		calculateHandicap();
		
		calculateTotalGoals();
		
		int indexMin=0;
		
		
		for(int i=0;i<goalsSumOverUnder.length;i++)
		{
			if(goalsSumOverUnder[i]<goalsSumOverUnder[indexMin])
			{
				indexMin=i;
			}
		}
		
		System.out.println("minimo goalsSumOverUnder["+indexMin+"]="+goalsSumOverUnder[indexMin]);
		return goalsOver[indexMin];
		
	}
	
	public void initTable(double sup,double totalGoals, double drawFactor)
	{
		table=new double[TABLE_MAX_GOALS][TABLE_MAX_GOALS];
		
		double goalA[]=new double[TABLE_MAX_GOALS];
		double goalB[]=new double[TABLE_MAX_GOALS];
		
		double lA=(sup/2)+(totalGoals/2);
		double lB=(totalGoals/2)-(sup/2);
		
		System.out.println("lA: "+lA+"\nLB: "+lB);
		
		for(int i =0;i<TABLE_MAX_GOALS;i++)
		{
			goalA[i]=poisson(i,lA);
			goalB[i]=poisson(i,lB);
		}
		
		String s="";
		
		totalTable=0;
		
		for(int l=0;l<TABLE_MAX_GOALS;l++)
		{
			for(int c=0;c<TABLE_MAX_GOALS;c++)
			{
				if(c==l)
					table[l][c]=goalA[l]*goalB[c]*drawFactor;
				else
					table[l][c]=goalA[l]*goalB[c];
				
				s=s+" "+table[l][c];
				
				totalTable+=table[l][c];
			}
			s=s+"\n";
		}
		
		
		
		System.out.println(s);
		System.out.println("Total Table : "+totalTable);
	}
	
	public void calculateTotalGoals()
	{
		//double goalsOver[];
		//double goalsUnder[];
		//double goalsSumOverUnder[];
		//double goalsAVGScale;
		
		double ret[]=new double[MAX_GOALS+1];
		
		for(int i=0;i<MAX_GOALS;i++)
		{
			for(int j=0;j<=i;j++)
			{
				ret[i]+=table[j][i-j];
				ret[i]/=totalTable;
			}
			
			System.out.println("ret["+i+"]="+ret[i]);
		}
		
		/*
		for(int i=MAX_GOALS;i<TABLE_MAX_GOALS;i++)
		{
			for(int j=0;j<=i;j++)
			{
				ret[MAX_GOALS]+=table[j][i-j];
			}
		}
	
		//ret[MAX_GOALS]/=totalTable;
		 * */
		 
		
		// prova dos 9 
		for(int i=0;i<MAX_GOALS;i++)
		{
			ret[MAX_GOALS]+=ret[i];
		}
		
		ret[MAX_GOALS]=totalTable-ret[MAX_GOALS];
		
		ret[MAX_GOALS]/=totalTable;
		
		System.out.println("ret["+MAX_GOALS+"]="+ret[MAX_GOALS]);
		
		double scale1[]=new double[21];
		double scale2[]=new double[21];
		
		double stepAux=0.5;
		for(int i=0;i<=20;i+=2)
		{
			scale1[i]=stepAux;
			if(i+1<20)
				scale1[i+1]=stepAux;
			
			scale2[i]=stepAux;
			stepAux+=0.5;
			if(i+1<20)
				scale2[i+1]=stepAux;
			
			//System.out.println("scale1["+i+"]="+scale1[i]+"scale2["+i+"]="+scale2[i]);
			
		}
		
		for(int i=0;i<21;i++)
			System.out.println("scale1["+i+"]="+scale1[i]+"scale2["+i+"]="+scale2[i]);
		
		
		goalsAVGScale=new double[21];
		
		for(int i=0;i<scale1.length;i++)
		{
			goalsAVGScale[i]=(scale1[i]+scale2[i])/2;
			System.out.println("goalsAVGScale["+i+"]="+goalsAVGScale[i]);
		}
		
		
		double auxA[]=new double[21];
		
		double auxB[]=new double[21];
		
		
		for(int i=0;i<auxA.length;i++)
		{
			double acum1A=0;
			double acum2A=0;
			
			double acum1B=0;
			double acum2B=0;
			
			for(int x=0;x<=MAX_GOALS;x++)
			{
				if(x>scale1[i])
				{
					acum1A+=ret[x];
					//System.out.println("somou ret["+x+"]="+ret[x]);
				}
				
				if(x>scale2[i])
				{
					acum2A+=ret[x];
				}
				
			}
			
			for(int x=0;x<=MAX_GOALS;x++)
			{
				if(x<scale1[i])
				{
					acum1B+=ret[x];
					//System.out.println("somou ret["+x+"]="+ret[x]);
				}
				
				if(x<scale2[i])
				{
					acum2B+=ret[x];
				}
				
			}

			
			auxA[i]=(acum1A+acum2A)/2;
			auxB[i]=(acum1B+acum2B)/2;
			
			System.out.println("auxA["+i+"]="+auxA[i]+"        auxB["+i+"]="+auxB[i]);
		}
		
		goalsOver=new double[21];
		goalsUnder=new double[21];
		goalsSumOverUnder=new double[21];
		
		for(int i=0;i<goalsOver.length;i++)
		{
			goalsOver[i]=(auxB[i]/auxA[i])+1;
			goalsUnder[i]=(auxA[i]/auxB[i])+1;
			
			goalsSumOverUnder[i]=goalsOver[i]+goalsUnder[i];
			
			System.out.println("goalsAVGScale["+i+"]="+goalsAVGScale[i]+"     goalsOver["+i+"]="+goalsOver[i]+"        goalsUnder["+i+"]="+goalsUnder[i]+"    goalsSumOverUnder["+i+"]="+goalsSumOverUnder[i]);
		}
		
		
	}
	
	
	public void calculateHandicap()
	{
		double ret[]=new double[(TABLE_MAX_GOALS*2)-1];
		
		int auxCorrect=0;
		for(int i=1;i<ret.length+1;i++)
		{
			if(i==TABLE_MAX_GOALS)
			{
				for(int x=0;x<TABLE_MAX_GOALS;x++)
					ret[auxCorrect]+=table[x][x];
			}
			else if(i<TABLE_MAX_GOALS)
			{
				int ref=TABLE_MAX_GOALS-i+1;
				
				int steps=TABLE_MAX_GOALS-ref;
				
				for(int x=0;x<steps;x++)
				{
					ret[auxCorrect]+=table[ref-1+x][x];
				}
			}
			else if(i>TABLE_MAX_GOALS)
			{
				int ref=i-TABLE_MAX_GOALS+1;
				
				int steps=TABLE_MAX_GOALS-ref;
				
				for(int x=0;x<steps;x++)
				{
					ret[auxCorrect]+=table[x][ref-1+x];
				}
			}
			
			ret[auxCorrect]/=totalTable;
			System.out.println("ret["+auxCorrect+"]="+ret[auxCorrect]);
			auxCorrect++;
			
			
		}
		
		
		
		
		
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
	
		hcapAVGScale=new double[25];
		
		for(int i=0;i<scale1.length;i++)
		{
			hcapAVGScale[i]=(scale1[i]+scale2[i])/2;
			System.out.println("hcapAVGScale["+i+"]="+hcapAVGScale[i]);
		}
		
		int auxIndex[]=new int[(TABLE_MAX_GOALS*2)-1];
		
		auxIndex[0]=11;
		for(int i=1;i<auxIndex.length;i++)
		{
			auxIndex[i]=auxIndex[i-1]-1;
			
			System.out.println("auxIndex["+i+"]="+auxIndex[i]);
		}
		
		double auxA[]=new double[25];
		
		
		for(int i=0;i<auxA.length;i++)
		{
			double x=scale1[i]*(-1);
			double acum=0;
			for(int j=0;j<auxIndex.length;j++)
			{
				if(auxIndex[j]>x)
				{
					acum+=ret[j];
					//System.out.println("soma: ret["+j+"]: " +ret[j] );
				}
			}
			auxA[i]=acum;
			//System.out.println("scale1["+i+"]*(-1) :"+x+"    auxA["+i+"]="+auxA[i]);
			
			x=scale2[i]*(-1);
			acum=0;
			for(int j=0;j<auxIndex.length;j++)
			{
				if(auxIndex[j]>x)
				{
					acum+=ret[j];
				}
			}
			auxA[i]+=acum;
			
			auxA[i]=auxA[i]/2;
			System.out.println("auxA["+i+"]="+auxA[i]);
		}
		
		double auxB[]=new double[25];
		
		for(int i=0;i<auxA.length;i++)
		{
			double x=scale1[i]*(-1);
			double acum=0;
			for(int j=0;j<auxIndex.length;j++)
			{
				if(auxIndex[j]<x)
				{
					acum+=ret[j];
					//System.out.println("soma: ret["+j+"]: " +ret[j] );
				}
			}
			auxB[i]=acum;
			//System.out.println("scale1["+i+"]*(-1) :"+x+"    auxA["+i+"]="+auxA[i]);
			
			x=scale2[i]*(-1);
			acum=0;
			for(int j=0;j<auxIndex.length;j++)
			{
				if(auxIndex[j]<x)
				{
					acum+=ret[j];
				}
			}
			auxB[i]+=acum;
			
			auxB[i]=auxB[i]/2;
			System.out.println("auxB["+i+"]="+auxB[i]);
		}
		
		hcapHome=new double[auxA.length];
		hcapAway=new double[auxA.length];
		
		hcapSumHomeAway=new double[auxA.length];
		
		for(int i=0;i<hcapHome.length;i++)
		{
			hcapHome[i]=(auxB[i]/auxA[i])+1;
			hcapAway[i]=(auxA[i]/auxB[i])+1;
			
			hcapSumHomeAway[i]=hcapHome[i]+hcapAway[i];
			
			System.out.println("hcapAVGScale["+i+"]="+hcapAVGScale[i]+"     home["+i+"]="+hcapHome[i]+ "  away["+i+"]="+hcapAway[i]+"    sumHomeAway["+i+"]="+hcapSumHomeAway[i]);
		}
		
	}
	
	public static void main(String[] args)  throws Exception {
		ModelCore mc=new ModelCore();
		
	}
}
