package scrapers.xscores;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import scrapers.GameScoreData;
import scrapers.UpdateScoresListener;



public class ScraperGoals {

	// THREAD
	private UpdateThread as;
	private Thread t;
	private boolean polling = false;
	protected int updateIntervalScraper = 3000;
	
	
	private String targetURLString = "http://www.xscores.com/soccer/soccer.jsp?sports=soccer&menu3=2&dt=09/08&flag=sportData";
	private static String xpath = "//table/tr";

	private Vector<GameScoreData> gameScoreData=new Vector<GameScoreData>();
	
	private Vector<UpdateScoresListener> updateScoresListener=new Vector<UpdateScoresListener>();
	
	public void addUpdateScoresListener(UpdateScoresListener l)
	{
		updateScoresListener.add(l);
	}

	public void removeUpdateScoresListener(UpdateScoresListener l)
	{
		updateScoresListener.remove(l);
	}

	
	public Document tidy(InputStream inputStrm) {
		Tidy tidy = new Tidy();
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		tidy.setShowErrors(0);
		//tidy.
		tidy.setWrapScriptlets(true);
		Document tidyDOM = tidy.parseDOM(inputStrm, null);
		return tidyDOM;
	}

	public void refresh() {
		
		System.out.println("Entrei");
		NodeList urlNodes = null;
		targetURLString="http://www.xscores.com/soccer/soccer.jsp?sports=soccer&menu3=2&dt=";
		
		//05/08&flag=sportData";
		
		Calendar now = Calendar.getInstance();
		int day=now.get(Calendar.DAY_OF_MONTH);
		int month=now.get(Calendar.MONTH);
		month++;  // ?? but it has to be...
		//System.out.println(month);
		
		if(day>9)
			targetURLString+=day+"/";
		else
			targetURLString+="0"+day+"/";
		
		if(month>9)
			targetURLString+=month+"&flag=sportData";
		else
			targetURLString+="0"+month+"&flag=sportData";
		//System.out.println("DAY:"+day);
		//System.out.println("Site:"+targetURLString);
		//targetURLString="http://www.xscores.com/soccer/soccer.jsp?sports=soccer&menu3=2&dt=10/08&flag=sportData";
		
		try {
			
			URL targetURL = new URL(targetURLString);
			URLConnection targetConnection = targetURL.openConnection();
			targetConnection.setDoOutput(true);
			targetConnection.setUseCaches(false);
			targetConnection.setAllowUserInteraction(false);
			targetConnection.setReadTimeout(2000);
			targetConnection.connect();
		
			// Post to output
			//System.out.println(targetConnection.getInputStream().available());
			//OutputStreamWriter out = new OutputStreamWriter(
			//		targetConnection.getOutputStream());
			//out.write("stst=" + keyword);
			//out.close();

			
			Document xmlResponse = tidy(targetConnection.getInputStream());
			System.out.println("Sai");
			//xmlResponse.getDoctype().
			//System.out.println(nodeToString(xmlResponse.getDocumentElement()));
			urlNodes = XPathAPI.selectNodeList(xmlResponse, "//tbody[@id=\"scoretable\"]");
			
			
			//System.out.println("urlNodes size:"+urlNodes.getLength());
			//System.out.println("------------------------");
			//System.out.println(nodeToString(urlNodes.item(0)));
			urlNodes=urlNodes.item(0).getChildNodes();
			
			
			//System.out.println("urlNodes size:"+urlNodes.getLength());
			
			String tA=null;
			String tB=null;
			int gA=0;
			int gB=0;
			
			for (int i = 1; i < urlNodes.getLength()-3; i++) {
				//System.out.println("####urlNodes size:"+urlNodes.item(i).getChildNodes().getLength());
				if(urlNodes.item(i).getChildNodes().getLength()==21)
				{
					//System.out.println(nodeToString(urlNodes.item(i).getChildNodes().item(6)));
					//System.out.print(urlNodes.item(i).getChildNodes().item(6).getChildNodes().item(0).getNodeValue());
					
					tA=urlNodes.item(i).getChildNodes().item(6).getChildNodes().item(0).getNodeValue();
					
					//System.out.print("  VS  ");
					////System.out.println(nodeToString(urlNodes.item(i).getChildNodes().item(10)));
					//System.out.print(urlNodes.item(i).getChildNodes().item(10).getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
					
					tB=urlNodes.item(i).getChildNodes().item(10).getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
					
					//System.out.println(" "+ urlNodes.item(i).getChildNodes().item(15).getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
					
					String score=urlNodes.item(i).getChildNodes().item(15).getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
					
					String goals[]=score.split("-");
					
					gA=Integer.parseInt(goals[0]);
					gB=Integer.parseInt(goals[1]);
										
				}
				else if(urlNodes.item(i).getChildNodes().getLength()==20)
				{
					//System.out.println(urlNodes.item(i).getChildNodes().item(5).getChildNodes().item(0).getNodeValue());
					//System.out.println("VS");
					//System.out.println(nodeToString(urlNodes.item(i).getChildNodes().item(9)));
					
					
					//System.out.print(urlNodes.item(i).getChildNodes().item(5).getChildNodes().item(0).getNodeValue());
					
					tA=urlNodes.item(i).getChildNodes().item(5).getChildNodes().item(0).getNodeValue();
					
					//System.out.print("  VS  ");
					////System.out.println(nodeToString(urlNodes.item(i).getChildNodes().item(10)));
					
					//System.out.print(urlNodes.item(i).getChildNodes().item(9).getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
					
					tB=urlNodes.item(i).getChildNodes().item(9).getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
					
					//System.out.println(" "+ urlNodes.item(i).getChildNodes().item(14).getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
					
					String score=urlNodes.item(i).getChildNodes().item(14).getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
					
					String goals[]=score.split("-");
					
					gA=Integer.parseInt(goals[0]);
					gB=Integer.parseInt(goals[1]);
				}
					
				
				//System.out.println(tA+" "+gA);
				//System.out.println(tB+" "+gB);
				
				boolean foundGame=false;
				boolean update=false;
				for(GameScoreData gd:gameScoreData)
				{
					if(tA.equals(gd.getTeamA())&&tB.equals(gd.getTeamB()) )
					{
						
						gd.setFoundInPLay(true);
						
						foundGame=true;
						if(gd.getActualGoalsA()!=gA)
						{
							gd.setActualGoalsA(gA);
							update=true;
						}
						if(gd.getActualGoalsB()!=gB)
						{
							
							gd.setActualGoalsB(gB);
							update=true;
						}
						
						if(update)
							UpdateListeners(gd);
					}
				}
				
				if(!foundGame)
				{
					if(tA!=null && tB!=null)
					{
						GameScoreData gsd=new GameScoreData(tA, tB, gA, gB);
						gsd.setFoundInPLay(true);
						gameScoreData.add(gsd);
					}
				}
				
				//System.out.println("------------------------");
			}
			
			
			//System.out.println(nodeToString(urlNodes.item(0)));
		} catch (Exception urle) {
			
			System.err.println("Error: " + urle.toString()+ "(Probably no live games to scrap)");
		}

		//clean finish games
		GameScoreData gsdArray[]=gameScoreData.toArray(new GameScoreData []{});
		for(int i=0;i<gsdArray.length;i++)
		{
			if(!gsdArray[i].isFoundInPLay())
				gameScoreData.remove(gsdArray[i]);
			else
				gsdArray[i].setFoundInPLay(false);
		}
		
		
		for(GameScoreData gsd: gameScoreData)
		{
			System.out.println("TeamA:"+gsd.getTeamA()+"-"+gsd.getActualGoalsA()+"("+gsd.getPrevGoalsA()+")\\n"+
					"TeamB:"+gsd.getTeamB()+"-"+gsd.getActualGoalsB()+"("+gsd.getPrevGoalsB()+")");
		}
		
		//return urlNodes;
	}
	
	public String getTargetURLString() {
		return targetURLString;
		
	}

	public void UpdateListeners(GameScoreData gd)
	{
		/*
		for(GameScoreData gsd: gameScoreData)
		{
			System.out.println("TeamA:"+gsd.getTeamA()+"-"+gsd.getActualGoalsA()+"("+gsd.getPrevGoalsA()+")\\n"+
					"TeamB:"+gsd.getTeamB()+"-"+gsd.getActualGoalsB()+"("+gsd.getPrevGoalsB()+")");
		}
		*/
		//System.out.println("------------------------");
		//System.out.println("TeamA:"+gd.getTeamA()+"-"+gd.getActualGoalsA()+"("+gd.getPrevGoalsA()+")\\n"+
		//		"TeamB:"+gd.getTeamB()+"-"+gd.getActualGoalsB()+"("+gd.getPrevGoalsB()+")");
		//System.out.println("------------------------");
		
		for(UpdateScoresListener usl:updateScoresListener)
		{
			usl.scoreUpdated(gd);
		}
	}
	
	public ScraperGoals()
	{
		targetURLString="http://www.xscores.com/soccer/soccer.jsp?sports=soccer&menu3=2&dt=";
		
		//05/08&flag=sportData";
		
		Calendar now = Calendar.getInstance();
		int day=now.get(Calendar.DAY_OF_MONTH);
		int month=now.get(Calendar.MONTH);
		month++;  // ?? but it has to be...
		//System.out.println(month);
		
		if(day>9)
			targetURLString+=day+"/";
		else
			targetURLString+="0"+day+"/";
		
		if(month>9)
			targetURLString+=month+"&flag=sportData";
		else
			targetURLString+="0"+month+"&flag=sportData";
		
		
		startPolling();
		//System.out.println(list.toString());
	}
	
	public Vector<GameScoreData> getGamesScoreData() {
		return gameScoreData;
	}

	
//-----------------------thread -------------------------------------
	
	public class UpdateThread extends Object implements Runnable {
		private volatile boolean stopRequested;

		private Thread runThread;

		public void run() {
			runThread = Thread.currentThread();
			stopRequested = false;
			
			while (!stopRequested) {
				try {
					refresh(); /// connect and get the data
				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
			
				
				try {
					Thread.sleep(updateIntervalScraper);
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}

		public void stopRequest() {
			stopRequested = true;

			if (runThread != null) {
				runThread.interrupt();

				// suspend()stop();
			}
		}
	}
	
	
	public void startPolling() {
		if (polling)
			return;
		as = new UpdateThread();
		t = new Thread(as);
		t.start();

		polling = true;
		/*
		 * timer = new javax.swing.Timer(UPDATE_INTERVAL, new ActionListener() {
		 * public void actionPerformed(ActionEvent e) { for(NeptusJoy
		 * nj:JoysList) nj.poll(); System.out.println("Fiz poll"); } });
		 */
	}

	public void stopPolling() {
		if (!polling)
			return;
		as.stopRequest();
		polling = false;

	}
	
	//---------------------------------------------------
	
	
	
	
	public static void main(String[] args) throws Exception {
		
		//final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
		//  final HtmlPage page = webClient.getPage("http://www.xscores.com/soccer/soccer.jsp?sports=soccer&flag=sportData");
		// HtmlPage page = webClient.getPage("http://www.futbol24.com/Live/");
		//  assertEquals("HtmlUnit - Welcome to HtmlUnit", page.getTitleText());
		
		//    String pageAsXml = page.asXml();
		//	System.out.println(pageAsXml);
		
		GameScoreData gd=new GameScoreData("x", "y", 1, 0);
		gd.setActualGoalsA(2);
		
		System.out.println("-----------test----------");
		System.out.println("TeamA:"+gd.getTeamA()+"-"+gd.getActualGoalsA()+"("+gd.getPrevGoalsA()+")\\n"+
				"TeamB:"+gd.getTeamB()+"-"+gd.getActualGoalsB()+"("+gd.getPrevGoalsB()+")");
		System.out.println("-----------test----------");
		
		new ScraperGoals();
	}

	   public static String nodeToString(Node node) {
	        StringWriter sw = new StringWriter();
	        try {
	            Transformer t = TransformerFactory.newInstance().newTransformer();
	            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	            t.setOutputProperty(OutputKeys.INDENT, "yes");
	            t.transform(new DOMSource(node), new StreamResult(sw));
	        }
	        catch (TransformerException te) {
	            System.out.println("nodeToString Transformer Exception");
	        }
	        return sw.toString();
	    }
}
