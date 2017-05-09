package marketProviders.nextPreLiveMo;

import java.util.Calendar;

public class EventData {

	String eventName=null;
	
	public Calendar startTime;
		
	int matchOddsId;
	boolean matchOddsTurnInPlay=true;

	int overUnderId[]=new int[9];
	boolean overUnderTurnInPlay[]=new boolean[9];
	
	int correctScoreId;
	boolean correctScoreTurnInPlay=true;
	
	public EventData(String name) {
		eventName=name;
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

	public int[] getOverUnderId() {
		return overUnderId;
	}

	public void setOverUnderId(int index, int underOverId) {
		this.overUnderId[index] = underOverId;
	}

	public int getCorrectScoreId() {
		return correctScoreId;
	}

	public void setCorrectScoreId(int correctScoreId) {
		this.correctScoreId = correctScoreId;
	}


	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	public boolean isMatchOddsTurnInPlay() {
		return matchOddsTurnInPlay;
	}

	public void setMatchOddsTurnInPlay(boolean matchOddsTurnInPlay) {
		this.matchOddsTurnInPlay = matchOddsTurnInPlay;
	}

	public boolean isOverUnderTurnInPlay(int index) {
		return overUnderTurnInPlay[index];
	}

	public void setOverUnderTurnInPlay(int index,boolean overUnderTurnInPlay) {
		this.overUnderTurnInPlay[index] = overUnderTurnInPlay;
	}
	public boolean isCorrectScoreTurnInPlay() {
		return correctScoreTurnInPlay;
	}

	public void setCorrectScoreTurnInPlay(boolean correctScoreTurnInPlay) {
		this.correctScoreTurnInPlay = correctScoreTurnInPlay;
	}

}