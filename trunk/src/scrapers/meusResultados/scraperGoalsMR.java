package scrapers.meusResultados;



import java.util.Vector;

import scrapers.GameScoreData;
import scrapers.UpdateScoresListener;


public class scraperGoalsMR {

	private String targetURLString = "http://xscores.com/soccer/soccer.jsp?sports=soccer&flag=sportData#.UdHHSfnVByU";
	private static String xpath = "//table/tr";

	private Vector<GameScoreData> gameScoreData=new Vector<GameScoreData>();
	
	private Vector<UpdateScoresListener> updateScoresListener=new Vector<UpdateScoresListener>();
	

	public scraperGoalsMR() {
		
	}
	
	public void addUpdateScoresListener(UpdateScoresListener l)
	{
		updateScoresListener.add(l);
	}

	public void removeUpdateScoresListener(UpdateScoresListener l)
	{
		updateScoresListener.remove(l);
	}

	
	
	public void refresh() {
		System.out.println("Entrei");
		//BasicConfigurator.configure(); 
		System.out.println("Entrei");
		
		
		String s=null;
		try {
		
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			// TODO: handle exception
		} 	
		System.out.println("site:"+s);
	}
	
	public String getTargetURLString() {
		return targetURLString;
		
	}

	
	public void UpdateListeners(GameScoreData gd)
	{
	
		for(UpdateScoresListener usl:updateScoresListener)
		{
			usl.scoreUpdated(gd);
		}
	}
	
	 private static final String TEST_URI = "http://meusresultados.com";
	
	   public static void main(String[] args) throws Exception {
	   
	    }
	   


}
