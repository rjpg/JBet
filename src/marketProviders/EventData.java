package marketProviders;

import java.util.Calendar;

public class EventData {

	public Calendar startTime;
	
	
	int matchOddsId;
	int underOverId[]=new int[8];
	int correctScoreId;
	
	public EventData(Calendar start) {
		startTime=start;
	}
	
	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public int getMatchOddsId() {
		return matchOddsId;
	}

	public void setMatchOddsId(int matchOddsId) {
		this.matchOddsId = matchOddsId;
	}

	public int[] getUnderOverId() {
		return underOverId;
	}

	public void setUnderOverId(int index, int underOverId) {
		this.underOverId[index] = underOverId;
	}

	public int getCorrectScoreId() {
		return correctScoreId;
	}

	public void setCorrectScoreId(int correctScoreId) {
		this.correctScoreId = correctScoreId;
	}

	
}
