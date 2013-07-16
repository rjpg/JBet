package main;

import bfapi.handler.ExchangeAPI.Exchange;
import categories.CategoriesManager;
import statistics.Statistics;
import generated.exchange.BFExchangeServiceStub.Market;
import DataRepository.MarketData;
import DataRepository.Utils;
import GUI.MarketMainFrame;




public class loader {

	
	
	
	public static void main(String[] args)  throws Exception {
		Utils.init();
		Statistics.init();
		
		
		CategoriesManager.init();
		CategoriesManager.loadRawAMFromFile();
		CategoriesManager.processAMCatIntervals();
		
		
		new Manager();
		
		
		/*Calendar now = Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(1279974600000L);
		System.out.println("today day"+calendar.get(Calendar.DAY_OF_MONTH)+"--"+calendar.get(Calendar.HOUR)+":"+calendar.get(Calendar.MINUTE));
		
		/*
        calendar.set(Calendar.MINUTE,55);
        calendar.set(Calendar.HOUR,2);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

		
		System.out.println("now   "+now.getTimeInMillis());
		System.out.println("today "+calendar.getTimeInMillis());
		System.out.println("today day"+calendar.get(Calendar.DAY_OF_MONTH));
		int min=calendar.get(Calendar.MINUTE);
		System.out.println("min"+calendar.get(Calendar.MINUTE));
		calendar.set(Calendar.MINUTE,min+10);
		System.out.println("min+10 "+calendar.get(Calendar.MINUTE));

		 File f = new File("1.txt");
		    System.out.println
		      (f + (f.exists()? " is found " : " is missing "));
		    
		    try {
		    	BufferedWriter out = new BufferedWriter(new FileWriter("1.txt", true));
		    	out.write("aString");
		    	out.newLine();
		    	out.close();
		    	} catch (IOException e) {
	    	} 

*/
	}
	
	
	

}
