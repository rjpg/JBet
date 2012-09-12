package correctscore;

import java.awt.Color;

import javax.swing.UIManager;

import DataRepository.Utils;

public class LoaderDataFavorite {
	public static void main(String[] args)  throws Exception {
		
		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		 
		//System.out.println(BetTypeEnum.Factory.fromValue("L"));
		Utils.init();
		
		CorrectScoreMainFrame csmf=new CorrectScoreMainFrame();
		csmf.refreshMatchOddFile();
	}
}
