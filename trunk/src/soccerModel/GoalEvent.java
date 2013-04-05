package soccerModel;

public class GoalEvent {

		
		public int timeSegment = 0;
		

		public boolean ab=false;
		
		public int a=0;
		public int b=0;
		
		public GoalEvent(int segment,boolean abA,int aA,int bA)
		{
			timeSegment=segment;
			ab=abA;
			a=aA;
			b=bA;
		}

		
		public int getTimeSegment() {
			return timeSegment;
		}

		public void setTimeSegment(int timeSegment) {
			this.timeSegment = timeSegment;
		}

		public boolean isAb() {
			return ab;
		}

		public void setAb(boolean ab) {
			this.ab = ab;
		}

		public int getA() {
			return a;
		}

		public void setA(int a) {
			this.a = a;
		}

		public int getB() {
			return b;
		}

		public void setB(int b) {
			this.b = b;
		}
}
