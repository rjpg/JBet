package correctscore;

import generated.exchange.BFExchangeServiceStub.BetTypeEnum;

import java.awt.Color;
import java.io.IOException;

import javax.swing.UIManager;

import scrapers.xscores.ScraperGoals;

import demo.util.Display;

import DataRepository.Utils;

public class LoaderCorrectScore {

	public static void main(String[] args)  throws Exception {
		
		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		 
		//System.out.println(BetTypeEnum.Factory.fromValue("L"));
		Utils.init();
		
		CorrectScoreMainFrame csmf=new CorrectScoreMainFrame();
	
		
		
		ScraperGoals sg=new ScraperGoals(); 
		sg.addUpdateScoresListener(csmf);
		csmf.writeMessageText(sg.getTargetURLString(), Color.BLACK);
		csmf.writeMessageText("Scraper is now polling", Color.BLUE);
		sg.startPolling();
	}
}
