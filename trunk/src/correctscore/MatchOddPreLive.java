package correctscore;

public class MatchOddPreLive {

		private String  teams=null;
		private long idBF = 0;
		private double oddBackA=0;
		private double oddBackB=0;
		private double oddLayA=0;
		private double oddLayB=0;
		
		public MatchOddPreLive(long id, String name,double oddBackAa,double oddBackBa,double  oddLayAa, double oddLayBa)
		{
			teams=name;
			idBF = id;
			oddBackA=oddBackAa;
			oddBackB=oddBackBa;
			oddLayA=oddLayAa;
			oddLayB=oddLayBa;
		}
		
		public MatchOddPreLive(String s)
		{
			parse(s);
		}
		
		public int parse(String s)
		{
			String fields[]=s.split("%");
			
			for (int i=0;i<fields.length;i++)
			{
				System.out.println("fields["+i+"]="+fields[i]);
			}
			
			if (fields.length!=6)
			{
				System.out.println("something wrong parcing matchOdd :"+s);
				return -1;
			}
			
			teams=fields[0];
			idBF = Long.parseLong(fields[1]);
			oddBackA=Double.parseDouble(fields[2]);
			oddBackB=Double.parseDouble(fields[3]);
			oddLayA=Double.parseDouble(fields[4]);
			oddLayB=Double.parseDouble(fields[5]);
			
			
			return 0;
		}
		
		public String makeString()
		{
			String ret=null;
			if (teams==null)
				ret="none%"+new Long(idBF).toString()+"%"+oddBackA+"%"+oddBackB+"%"+oddLayA+"%"+oddLayB;
			else
				ret=teams+"%"+new Long(idBF).toString()+"%"+oddBackA+"%"+oddBackB+"%"+oddLayA+"%"+oddLayB;
			
			return ret;
				
		}
		
		public String getTeams() {
			return teams;
		}

		public void setTeams(String teams) {
			this.teams = teams;
		}

		public long getIdBF() {
			return idBF;
		}

		public void setIdBF(long idBF) {
			this.idBF = idBF;
		}

		public double getOddBackA() {
			return oddBackA;
		}

		public void setOddBackA(double oddBackA) {
			this.oddBackA = oddBackA;
		}

		public double getOddBackB() {
			return oddBackB;
		}

		public void setOddBackB(double oddBackB) {
			this.oddBackB = oddBackB;
		}

		public double getOddLayA() {
			return oddLayA;
		}

		public void setOddLayA(double oddLayA) {
			this.oddLayA = oddLayA;
		}

		public double getOddLayB() {
			return oddLayB;
		}

		public void setOddLayB(double oddLayB) {
			this.oddLayB = oddLayB;
		}
		public static void main(String[] args) throws Exception {
			MatchOddPreLive mopl1=new MatchOddPreLive(12,"x v y",2,3,4,5);
			System.out.println("mopl1 makestring="+mopl1.makeString());
			
			MatchOddPreLive mopl2=new MatchOddPreLive(mopl1.makeString());
			System.out.println("mopl2 makestring="+mopl2.makeString());
		}
}
