package soccerModel;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JFrame;

import org.apache.axis2.jaxws.description.xml.handler.HomeType;

import GUI.MyChart2D;


public class ModelCore {
	
	public static int TABLE_MAX_GOALS=12;
		
	//double AH;
	//double home,away;
	
	//global Table
	double table[][];
	double totalTable=0;
	
	double drawFactor=1;
	
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
	double asianHcap=0.75;
	
	double gLineOver=2.0;
	double gLineUnder=2.0;
	
	double aHcapHome=2.0;
	double aHcapAway=2.0;
	
	double desAH=0;
	double desGL=0;
	
	
	
	//------------------------ time
	
	double totalGoals=0;
	double supremacy=0;
	
	int segments=90; 
	
	
	
	//Model parameters 
	
	
	double halfTimeFactor1Parmeter=0.45;
	
	double totalGoalsSegments[]=new double[segments*2+1];
	double supremacySegments[]=new double[segments*2+1];
	
	double halfTimeFactor1rates[]=new double[segments];
	
	double halfTimeFactor2rates[]=new double[segments];
	
	public ModelCore() {
	
		initTable(2.5,2.5,drawFactor);
		
		calculateHandicap();
		
		calculateTotalGoals();
		/*
		System.out.println(cumputeNearHcap(1.0,2.5));
		System.out.println(cumputeNearTG(1.0,2.5));*/
		
		supremacy();
		
		calculateHalfTimeRates1();
		calculateHalfTimeRates2();
		
		printMatchOdds();
		
		calculateTotalGoalsSegments();
		
		calculateSupremacySegments();
		
		printMatchOddsSegments();
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
	
	
	
	
	public void calculateHalfTimeRates1()
	{
		
		double rate=Math.pow((halfTimeFactor1Parmeter/0.001),(1./89.));
		System.out.println("Rate="+rate);
		halfTimeFactor1rates[0]=0.001;
		System.out.println("halfTimeFactor1rates["+0+"]="+halfTimeFactor1rates[0]);
		for(int i=1;i<segments;i++)
		{
			
			halfTimeFactor1rates[i]=halfTimeFactor1rates[i-1]*rate;
			System.out.println("halfTimeFactor1rates["+i+"]="+halfTimeFactor1rates[i]);
		}
		
		
	}
	
	public void calculateHalfTimeRates2()
	{
		
		double rate=1./Math.pow(halfTimeFactor1Parmeter,(1./90.));
		System.out.println("Rate2="+rate);
		halfTimeFactor2rates[0]=halfTimeFactor1rates[segments-1]*rate;
		System.out.println("halfTimeFactor2rates[0]="+halfTimeFactor2rates[0]);
		for(int i=1;i<segments;i++)
		{
			
			halfTimeFactor2rates[i]=halfTimeFactor2rates[i-1]*rate;
			System.out.println("halfTimeFactor2rates["+i+"]="+halfTimeFactor2rates[i]);
		}
		
		System.out.println("halfTimeFactor2rates[90]="+halfTimeFactor2rates[segments-1]*rate);
	}
	
	public void calculateTotalGoalsSegments()
	{
		totalGoalsSegments[0]=totalGoals;
		
		System.out.println("totalGoalsSegments[0]="+totalGoalsSegments[0]);
		
		for(int i=1;i<segments+1;i++)
		{
			totalGoalsSegments[i]=totalGoals*(1.-halfTimeFactor1rates[i-1]);
			System.out.println("totalGoalsSegments["+i+"]="+totalGoalsSegments[i] +"   ("+totalGoals+"*(1-"+halfTimeFactor1rates[i-1]+"))");
			
		}	
		
		System.out.println("--------------------");
		
		for(int i=0;i<segments;i++)
		{
			totalGoalsSegments[i+segments+1]=totalGoals*(1.-halfTimeFactor2rates[i]);
			System.out.println("totalGoalsSegments["+(i+segments+1)+"]="+totalGoalsSegments[i+segments+1]+ "   ("+totalGoals+"*(1-"+halfTimeFactor2rates[i]+"))");
		}	
	}
	
	public void calculateSupremacySegments()
	{
		supremacySegments[0]=supremacy;
		
		System.out.println("supremacySegments[0]="+supremacySegments[0]);
		
		for(int i=1;i<segments+1;i++)
		{
			supremacySegments[i]=supremacy*(1.-halfTimeFactor1rates[i-1]);
			System.out.println("supremacySegments[0]="+supremacySegments[i]+ "   ("+supremacy+"*(1-"+halfTimeFactor1rates[i-1]+"))");
		}	
		
		System.out.println("--------------------");
		
		for(int i=0;i<segments;i++)
		{
			supremacySegments[i+segments+1]=supremacy*(1.-halfTimeFactor2rates[i]);
			System.out.println("supremacySegments["+(i+segments+1)+"]="+supremacySegments[i+segments+1]+ "   ("+supremacy+"*(1-"+halfTimeFactor2rates[i]+"))");
		}	
	}
	
	
	
	public void supremacy()
	{
		desAH=aHcapHome*(1/aHcapHome+1/aHcapAway);
		desGL=gLineOver*(1/gLineOver+1/gLineUnder);

		double sup=0;
		double tg=2.5;
		
		
	
		/// afinar alterando a sup
		
		///
		///
		for(int it=0;it<3;it++)
		{
		
		//---------------------------- CAlcular desGL mudando tg
		int indexRefGL=0;
		
		while (goalsAVGScale[indexRefGL]!=goalLine) indexRefGL++;
		
		
		System.out.println("Found : goalsAVGScale["+indexRefGL+"]="+goalsAVGScale[indexRefGL]);
		
		int start=0;
		
		while (goalsAVGScale[start]<Math.abs(sup)) start++;
		
		initTable(sup,goalsAVGScale[start],drawFactor);
		calculateTotalGoals();
		
		double nearGL=goalsOver[indexRefGL];
		int nearGLi=start;
		double nearGLaux=0;
		
		
		
		for(int i=start+1;i<goalsAVGScale.length;i++)
		{
				initTable(sup,goalsAVGScale[i],drawFactor);
			
				calculateTotalGoals();
			
				nearGLaux=goalsOver[indexRefGL];
				
				double diff1=Math.abs(nearGL-desGL);
				double diff2=Math.abs(nearGLaux-desGL);
				if (diff1>diff2)
				{
					nearGL=nearGLaux;
					nearGLi=i;
				}
			
		}
		
		tg=goalsAVGScale[nearGLi];
		
		System.out.println("tg found : "+tg+" over: "+nearGL+" desGL: "+desGL);
		
		
		// refinar tg (argumento goalsAVGScale[i])
		
		double stepTG=0.125;
		double limitTG=0.00000001;
		
		double tgaux=tg;
		
		int dir=1;
		
		while (stepTG>limitTG && nearGL!=desGL)
		{
			tgaux=tg;
			
			if(dir>0)
			{
				tgaux+=stepTG;
				if(tgaux<Math.abs(sup))
				{
					tgaux-=(stepTG*2);
					dir=-1;
				}
			}
			else
			{
				tgaux-=stepTG;
				if(tgaux<Math.abs(sup))
				{
					tgaux+=(stepTG*2);
					dir=1;
				}
				
			}
			
			initTable(sup,tgaux,drawFactor);
			
			calculateTotalGoals();
		
			nearGLaux=goalsOver[indexRefGL];
			
			double diff1=Math.abs(nearGL-desGL);
			double diff2=Math.abs(nearGLaux-desGL);
			
			if (diff1>diff2)
			{
				nearGL=nearGLaux;
				tg=tgaux;
			}
			else
			{
				stepTG/=2;
				dir*=-1;
				//System.out.println("###################################################");
			}
			
		}
				
		//---------------------------- CAlcular desAH mudando sup
		
		int indexRefAH=0;
		
		while (hcapAVGScale[indexRefAH]!=asianHcap) indexRefAH++;
		
		int startAH=0;
		
		while (Math.abs(hcapAVGScale[startAH])>tg) startAH++;
		
		initTable(hcapAVGScale[startAH],tg,drawFactor);
		calculateHandicap();
		
		double nearHcap=hcapHome[indexRefAH];
		int nearAHi=startAH;
		double nearHcapaux=0;
		
		for(int i=startAH+1;i<hcapAVGScale.length;i++)
		{
			if(Math.abs(hcapAVGScale[i])<=tg)
			{
				initTable(hcapAVGScale[i],tg,drawFactor);
				calculateHandicap();
				nearHcapaux=hcapHome[indexRefAH];
				
				double diff1=Math.abs(nearHcap-desAH);
				double diff2=Math.abs(nearHcapaux-desAH);
				if (diff1>diff2)
				{
					nearHcap=nearHcapaux;
					nearAHi=i;
				}
			}
		}
		
		sup=hcapAVGScale[nearAHi];
		
		System.out.println("sup found : "+sup+" home: "+nearHcap+" desAH: "+desAH);	
		
		double stepAH=0.125;
		double limitAH=0.000001;
		
		double ahaux=sup;
		
		dir=1;
		
		while (stepAH>limitAH && nearHcap!=desAH)
		{
			ahaux=sup;
			
			if(dir>0)
			{
				ahaux+=stepAH;
				if(tg<Math.abs(ahaux))
				{
					ahaux-=(stepAH*2);
					dir=-1;
				}
			}
			else
			{
				ahaux-=stepAH;
				if(tg<Math.abs(ahaux))
				{
					ahaux+=(stepAH*2);
					dir=1;
				}
				
			}
			
			initTable(ahaux,tg,drawFactor);
			
			calculateHandicap();
		
			nearHcapaux=hcapHome[indexRefAH];
			
			double diff1=Math.abs(nearHcap-desAH);
			double diff2=Math.abs(nearHcapaux-desAH);
			if (diff1>diff2)
			{
				nearHcap=nearHcapaux;
				sup=ahaux;
			}
			else
			{
				stepAH/=2;
				dir*=-1;
			}
			
			
		}
		
		System.out.println("sup found : "+sup+" home: "+nearHcap+" desAH: "+desAH+"      tg found : "+tg+" goalsOver"+nearGL+" desGL: "+desGL);	
		}
		
		System.out.println("FINAL sup found : "+sup+"      tg found : "+tg);
		
		supremacy=sup;
		totalGoals=tg;
		
		initTable(supremacy,totalGoals,drawFactor);
		
		printTable();
		
		
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
		System.err.println("TABLE sup: "+sup+"   tg:"+totalGoals);
		
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
	
	public void printTable()
	{
		
		String s="";
		
		double totalTable2=0;
		for(int l=0;l<TABLE_MAX_GOALS;l++)
		{
			for(int c=0;c<TABLE_MAX_GOALS;c++)
			{
				
				s=s+" "+table[c][l];
				
				totalTable2+=table[c][l];
			}
			s=s+"\n";
		}
		
		
		
		System.out.println(s);
		System.out.println("Total Table : "+totalTable2);
	}
	
	
	public void printMatchOddsSegments()
	{
		
		JFrame frame=new JFrame();
		
		MyChart2D chart=new MyChart2D();
		
		frame.setSize(500,500);
		
		frame.setContentPane(chart);
		frame.setVisible(true);
		
		for(int i=0;i<segments+1;i++)
		{
			
			initTable(supremacySegments[i],totalGoalsSegments[i],drawFactor);
			System.out.println("Segment :"+i+"       (Minute :"+(i)/2.+")");
			printMatchOdds();
			
			//chart.addValue("A", i, sumA, Color.RED);
			//chart.addValue("D", i, sumDraw, Color.GREEN);
			//chart.addValue("B", i, sumB, Color.BLUE);
			chart.addValue("Actual", i, 1/actual, Color.YELLOW);
			
		}	
		
		System.out.println("--------------------");
		
		for(int i=0;i<segments;i++)
		{
			initTable(supremacySegments[i+segments+1],totalGoalsSegments[i+segments+1],drawFactor);
			//totalGoalsSegments[i+segments+1]=totalGoals*(1.-halfTimeFactor2rates[i]);
			System.out.println("Segment :"+(i+segments+1)+"       (Minute :"+(i+segments+1)/2.+")");
			printMatchOdds();
			//chart.addValue("A", i+segments+1, sumA, Color.RED);
			//chart.addValue("D", i+segments+1, sumDraw, Color.GREEN);
			//chart.addValue("B", i+segments+1, sumB, Color.BLUE);
			chart.addValue("Actual", i+segments+1, 1/actual, Color.YELLOW);
		}	
		
		/*for(int i=0;i<segments;i++)
		{
			chart.addValue("X", i, 0.7, Color.CYAN);
		}*/
		
		
		
	}
	
	double sumB=0;
	double sumDraw=0;
	double sumA=0;
	
	double actual=0;
	public void printMatchOdds()
	{
		sumB=0;
		sumDraw=0;
		sumA=0;
		//double sumB=0;
		for(int l=1;l<TABLE_MAX_GOALS;l++)
		{
			for(int c=0;c<l;c++)
			{
				
				sumB+=table[c][l];
			}
		
		}
		
	
		
		//double sumDraw=0;
		for(int l=0;l<TABLE_MAX_GOALS;l++)
		{
			sumDraw+=table[l][l];
			//System.out.println("sumDraw:"+table[l][l]);
		}
		
		
		
		//double sumA=0;
		for(int l=0;l<TABLE_MAX_GOALS-1;l++)
		{
			for(int c=l+1;c<TABLE_MAX_GOALS;c++)
			{
				
				sumA+=table[c][l];
			}
		
		}
		actual = table[0][0];
		
		if(actual<0.05) actual=0.05;
		
		System.out.println("A:"+1/sumA);
		System.out.println("Draw:"+1/sumDraw);
		System.out.println("B:"+1/sumB);
		System.out.println("ACtual:"+(1/actual));
		
	}
	
	public void initTable(double sup,double totalGoals, double drawFactor)
	{
		//System.err.println("TABLE sup: "+sup+"   tg:"+totalGoals);
		
		
		table=new double[TABLE_MAX_GOALS][TABLE_MAX_GOALS];
		
		double goalA[]=new double[TABLE_MAX_GOALS];
		double goalB[]=new double[TABLE_MAX_GOALS];
		
		double lA=(sup/2)+(totalGoals/2);
		double lB=(totalGoals/2)-(sup/2);
		
		//System.out.println("lA: "+lA+"\nLB: "+lB);
		
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
		
		
		
		//System.out.println(s);
		//System.out.println("Total Table : "+totalTable);
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
			
		//	System.out.println("ret["+i+"]="+ret[i]);
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
		
		//System.out.println("ret["+MAX_GOALS+"]="+ret[MAX_GOALS]);
		
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
		
		//for(int i=0;i<21;i++)
		//	System.out.println("scale1["+i+"]="+scale1[i]+"scale2["+i+"]="+scale2[i]);
		
		
		goalsAVGScale=new double[21];
		
		for(int i=0;i<scale1.length;i++)
		{
			goalsAVGScale[i]=(scale1[i]+scale2[i])/2;
		//	System.out.println("goalsAVGScale["+i+"]="+goalsAVGScale[i]);
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
			
		//	System.out.println("auxA["+i+"]="+auxA[i]+"        auxB["+i+"]="+auxB[i]);
		}
		
		goalsOver=new double[21];
		goalsUnder=new double[21];
		goalsSumOverUnder=new double[21];
		
		for(int i=0;i<goalsOver.length;i++)
		{
			goalsOver[i]=(auxB[i]/auxA[i])+1;
			goalsUnder[i]=(auxA[i]/auxB[i])+1;
			
			goalsSumOverUnder[i]=goalsOver[i]+goalsUnder[i];
			
			//System.out.println("goalsAVGScale["+i+"]="+goalsAVGScale[i]+"     goalsOver["+i+"]="+goalsOver[i]+"        goalsUnder["+i+"]="+goalsUnder[i]+"    goalsSumOverUnder["+i+"]="+goalsSumOverUnder[i]);
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
			//System.out.println("ret["+auxCorrect+"]="+ret[auxCorrect]);
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
			
			//System.out.println("scale1["+i+"]="+scale1[i]+"scale2["+i+"]="+scale2[i]);
			
		}
		
		//for(int i=0;i<25;i++)
		//	System.out.println("scale1["+i+"]="+scale1[i]+"scale2["+i+"]="+scale2[i]);
	
		hcapAVGScale=new double[25];
		
		for(int i=0;i<scale1.length;i++)
		{
			hcapAVGScale[i]=(scale1[i]+scale2[i])/2;
			//System.out.println("hcapAVGScale["+i+"]="+hcapAVGScale[i]);
		}
		
		int auxIndex[]=new int[(TABLE_MAX_GOALS*2)-1];
		
		auxIndex[0]=11;
		for(int i=1;i<auxIndex.length;i++)
		{
			auxIndex[i]=auxIndex[i-1]-1;
			
			//System.out.println("auxIndex["+i+"]="+auxIndex[i]);
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
			//System.out.println("auxA["+i+"]="+auxA[i]);
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
			//System.out.println("auxB["+i+"]="+auxB[i]);
		}
		
		hcapHome=new double[auxA.length];
		hcapAway=new double[auxA.length];
		
		hcapSumHomeAway=new double[auxA.length];
		
		for(int i=0;i<hcapHome.length;i++)
		{
			hcapHome[i]=(auxB[i]/auxA[i])+1;
			hcapAway[i]=(auxA[i]/auxB[i])+1;
			
			hcapSumHomeAway[i]=hcapHome[i]+hcapAway[i];
			
		//	System.out.println("hcapAVGScale["+i+"]="+hcapAVGScale[i]+"     home["+i+"]="+hcapHome[i]+ "  away["+i+"]="+hcapAway[i]+"    sumHomeAway["+i+"]="+hcapSumHomeAway[i]);
		}
		
	}
	
	public static void main(String[] args)  throws Exception {
		ModelCore mc=new ModelCore();
		
	}
}
