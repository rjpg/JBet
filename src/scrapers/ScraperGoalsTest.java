package scrapers;

import java.awt.Color;

import javax.swing.UIManager;

import correctscore.UpdateScoresListener;


import DataRepository.Utils;

public class ScraperGoalsTest implements UpdateScoresListener {

	@Override
	public void scoreUpdated(GameScoreData gd) {
		
		System.out.println(gd.getTeamA()+":"+gd.getActualGoalsA()+"("+gd.getPrevGoalsA()+")  VS "+
				gd.getTeamB()+":"+gd.getActualGoalsB()+"("+gd.getPrevGoalsB()+")");
		
	}

public static void main(String[] args)  throws Exception {
		
		ScraperGoalsTest sgt=new ScraperGoalsTest();
		ScraperGoals sg=new ScraperGoals(); 

		sg.addUpdateScoresListener(sgt);
		System.out.println(sg.getTargetURLString());
		System.out.println("Scraper is now polling");
		sg.startPolling();
		
		
	}
}
