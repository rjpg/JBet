package nextGoal;

public class Score {
	public int goalA=0;
	public int goalB=0;
	
	public String stringResult= "0 - 0";
	
	public double oddBack=1000;
	public double oddLay=1.01;
	
	public Score()
	{
		
	}
	
	public Score(int a,int b)
	{
		setGoals(a, b);
	}
	
	public Score(int a,int b,double oddBackA,double oddLayA)
	{
		this(a,b);
		setOdds(oddBackA,oddLayA);
	}
	
	public void setOdds(double oddBackA,double oddLayA)
	{
		oddBack=oddBackA;
		oddLay=oddLayA;
	}
	
	public void setGoals(int a, int b)
	{
		goalA=a;
		goalB=b;
		if(a==-1 || b==-1 || a>3 || b>3)
			stringResult="Any Uncoted";
		else
			stringResult=goalA+" - "+goalB;
	}
	
	@Override
	public String toString() {
		
		return stringResult;
	}
	
	public Score getNextScoreA()
	{
		return new Score(goalA+1,goalB);
	}
	
	public Score getNextScoreB()
	{
		return new Score(goalA,goalB+1);
	}
	
	public Score getSimetricScore()
	{
		return new Score(goalB,goalA);
	}
	
}
