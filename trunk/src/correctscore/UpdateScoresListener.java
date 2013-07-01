package correctscore;

import scrapers.GameScoreData;

public interface UpdateScoresListener {
	public void scoreUpdated(GameScoreData gsd);
}
