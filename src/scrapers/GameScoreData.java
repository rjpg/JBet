package scrapers;

public class GameScoreData {
	private String teamA=null;
	private String teamB=null;
	
	private int prevGoalsA=0;
	private int prevGoalsB=0;
	
	private int actualGoalsA=0;
	private int actualGoalsB=0;
	
	private boolean mistakeState=false;
	
	protected boolean foundInPLay=false;

	public GameScoreData(String tA,String tB,int gA, int gB)
	{
		teamA=tA;
		teamB=tB;
		
		prevGoalsA=gA;
		prevGoalsB=gB;
		
		actualGoalsA=gA;
		actualGoalsB=gB;
	}
	
	public String getTeamA() {
		return teamA;
	}
	public void setTeamA(String teamA) {
		this.teamA = teamA;
	}
	public String getTeamB() {
		return teamB;
	}
	public void setTeamB(String teamB) {
		this.teamB = teamB;
	}
	public int getPrevGoalsA() {
		return prevGoalsA;
	}
	
	
	
	public int getPrevGoalsB() {
		return prevGoalsB;
	}
	
	
	
	public int getActualGoalsA() {
		return actualGoalsA;
	}
	
	public void setActualGoalsA(int actualGoalsAa) {
		if(this.actualGoalsA>= actualGoalsAa)
		{
			System.out.println("mistake Goals A in class < Goals A in argument");
			setMistakeState(true);
		}
		
		this.prevGoalsA=this.actualGoalsA;
		this.actualGoalsA = actualGoalsAa;
		
		this.prevGoalsB=this.actualGoalsB;
		
	}
	public int getActualGoalsB() {
		return actualGoalsB;
	}
	
	public void setActualGoalsB(int actualGoalsBa) {
		if(this.actualGoalsB>= actualGoalsBa)
		{
			System.out.println("mistake Goals B in class < Goals B in argument");
			setMistakeState(true);
		}
		
		this.prevGoalsB=this.actualGoalsB;
		this.actualGoalsB = actualGoalsBa;
		
		this.prevGoalsA=this.actualGoalsA;
	}
	

	public boolean isMistakeState() {
		return mistakeState;
	}

	public void setMistakeState(boolean mistakeState) {
		this.mistakeState = mistakeState;
	}
	
	public boolean isFoundInPLay() {
		return foundInPLay;
	}

	public void setFoundInPLay(boolean foundInPLay) {
		this.foundInPLay = foundInPLay;
	}
	
	public static void main(String[] args) throws Exception {
		GameScoreData gd=new GameScoreData("x", "y", 0, 0);
		gd.setActualGoalsB(1);
		
		System.out.println("-----------test----------");
		System.out.println("TeamA:"+gd.getTeamA()+"-"+gd.getActualGoalsA()+"("+gd.getPrevGoalsA()+")\\n"+
				"TeamB:"+gd.getTeamB()+"-"+gd.getActualGoalsB()+"("+gd.getPrevGoalsB()+") State Mistake:"+gd.isMistakeState());
		System.out.println("-----------test----------");
	}
	
}
