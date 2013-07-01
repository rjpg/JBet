package scrapers.meusResultados;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import javax.xml.xpath.*;

import java.util.Vector;
import java.util.logging.*;

import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.domimpl.DocumentNotificationListener;
import org.lobobrowser.html.domimpl.HTMLDocumentImpl;
import org.lobobrowser.html.domimpl.NodeImpl;
import org.lobobrowser.html.parser.*;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import scrapers.GameScoreData;
import scrapers.UpdateScoresListener;


public class scraperGoalsMR implements DocumentNotificationListener {

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
	
	 private static final String TEST_URI = "http://lobobrowser.org";
	
	   public static void main(String[] args) throws Exception {
	        // Disable most Cobra logging.
		   
		   scraperGoalsMR sgmr=new scraperGoalsMR();
		    
		   // Disable most Cobra logging.
	        Logger.getLogger("org.lobobrowser").setLevel(Level.WARNING);
	        UserAgentContext uacontext = new SimpleUserAgentContext();
	        // In this case we will use a standard XML document
	        // as opposed to Cobra's HTML DOM implementation.
	        UserAgentContext context = new SimpleUserAgentContext();
	        DocumentBuilderImpl builder = new DocumentBuilderImpl(context);
	        
	        URL url = new URL(TEST_URI);
	        InputStream in = url.openConnection().getInputStream();
	        try {
	            Reader reader = new InputStreamReader(in, "ISO-8859-1");
	            //Document document = builder.newDocument();
	            InputSourceImpl inputSource = new InputSourceImpl(reader, TEST_URI);
	            Document d = builder.parse(inputSource);
	            
	            // see http://sourceforge.net/p/xamj/patches/40/
	            
	            HTMLDocumentImpl document = (HTMLDocumentImpl) d;
	            
	           // HTMLDocumentImpl document = (HTMLDocumentImpl) builder.createDocument(inputSource); 
	           // HTMLDocumentImpl document2 = (HTMLDocumentImpl) document;
	            
	          //  document2.addDocumentNotificationListener(sgmr);
	           
	            // Here is where we use Cobra's HTML parser.            
	            HtmlParser parser = new HtmlParser(uacontext, document);
	            
	            System.out.println("will start");
	            parser.parse(reader);
	            // Now we use XPath to locate "a" elements that are
	            // descendents of any "html" element.
	            XPath xpath = XPathFactory.newInstance().newXPath();
	            NodeList nodeList = (NodeList) xpath.evaluate("html//a", document, XPathConstants.NODESET);
	            int length = nodeList.getLength();
	            for(int i = 0; i < length; i++) {
	                Element element = (Element) nodeList.item(i);
	                System.out.println("## Anchor # " + i + ": " + element.getAttribute("href"));
	            }
	        } finally {
	            in.close();
	        }
	    }
	   
	 
	@Override
	public void allInvalidated() {
		System.out.println("allInvalidated");
		
	}

	@Override
	public void externalScriptLoading(NodeImpl arg0) {
		System.out.println("externalScriptLoading");
		
	}

	@Override
	public void invalidated(NodeImpl arg0) {
		System.out.println("externalScriptLoading");
		
	}

	@Override
	public void lookInvalidated(NodeImpl arg0) {
		System.out.println("lookInvalidated");
		
	}

	@Override
	public void nodeLoaded(NodeImpl arg0) {
		System.out.println("nodeLoaded");
		
	}

	@Override
	public void positionInvalidated(NodeImpl arg0) {
		System.out.println("positionInvalidated");
		
	}

	@Override
	public void sizeInvalidated(NodeImpl arg0) {
		System.out.println("sizeInvalidated");
		
	}

	@Override
	public void structureInvalidated(NodeImpl arg0) {
		System.out.println("structureInvalidated");
		
	}

}
