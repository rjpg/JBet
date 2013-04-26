package demo.handler;

import generated.exchange.BFExchangeServiceStub;
import generated.exchange.BFExchangeServiceStub.APIErrorEnum;
import generated.exchange.BFExchangeServiceStub.APIRequestHeader;
import generated.exchange.BFExchangeServiceStub.APIResponseHeader;
import generated.exchange.BFExchangeServiceStub.ArrayOfCancelBets;
import generated.exchange.BFExchangeServiceStub.ArrayOfPlaceBets;
import generated.exchange.BFExchangeServiceStub.ArrayOfUpdateBets;
import generated.exchange.BFExchangeServiceStub.Bet;
import generated.exchange.BFExchangeServiceStub.BetStatusEnum;
import generated.exchange.BFExchangeServiceStub.BetsOrderByEnum;
import generated.exchange.BFExchangeServiceStub.CancelBets;
import generated.exchange.BFExchangeServiceStub.CancelBetsE;
import generated.exchange.BFExchangeServiceStub.CancelBetsErrorEnum;
import generated.exchange.BFExchangeServiceStub.CancelBetsReq;
import generated.exchange.BFExchangeServiceStub.CancelBetsResp;
import generated.exchange.BFExchangeServiceStub.CancelBetsResult;
import generated.exchange.BFExchangeServiceStub.GetAccountFunds;
import generated.exchange.BFExchangeServiceStub.GetAccountFundsErrorEnum;
import generated.exchange.BFExchangeServiceStub.GetAccountFundsReq;
import generated.exchange.BFExchangeServiceStub.GetAccountFundsResp;
import generated.exchange.BFExchangeServiceStub.GetBet;
import generated.exchange.BFExchangeServiceStub.GetBetErrorEnum;
import generated.exchange.BFExchangeServiceStub.GetBetReq;
import generated.exchange.BFExchangeServiceStub.GetBetResp;
import generated.exchange.BFExchangeServiceStub.GetCompleteMarketPricesCompressed;
import generated.exchange.BFExchangeServiceStub.GetCompleteMarketPricesCompressedReq;
import generated.exchange.BFExchangeServiceStub.GetCompleteMarketPricesCompressedResp;
import generated.exchange.BFExchangeServiceStub.GetCompleteMarketPricesErrorEnum;
import generated.exchange.BFExchangeServiceStub.GetMUBets;
import generated.exchange.BFExchangeServiceStub.GetMUBetsErrorEnum;
import generated.exchange.BFExchangeServiceStub.GetMUBetsReq;
import generated.exchange.BFExchangeServiceStub.GetMUBetsResp;
import generated.exchange.BFExchangeServiceStub.GetMarket;
import generated.exchange.BFExchangeServiceStub.GetMarketErrorEnum;
import generated.exchange.BFExchangeServiceStub.GetMarketPricesCompressed;
import generated.exchange.BFExchangeServiceStub.GetMarketPricesCompressedReq;
import generated.exchange.BFExchangeServiceStub.GetMarketPricesCompressedResp;
import generated.exchange.BFExchangeServiceStub.GetMarketPricesErrorEnum;
import generated.exchange.BFExchangeServiceStub.GetMarketReq;
import generated.exchange.BFExchangeServiceStub.GetMarketResp;
import generated.exchange.BFExchangeServiceStub.GetMarketTradedVolume;
import generated.exchange.BFExchangeServiceStub.GetMarketTradedVolumeCompressed;
import generated.exchange.BFExchangeServiceStub.GetMarketTradedVolumeCompressedErrorEnum;
import generated.exchange.BFExchangeServiceStub.GetMarketTradedVolumeCompressedReq;
import generated.exchange.BFExchangeServiceStub.GetMarketTradedVolumeCompressedResp;
import generated.exchange.BFExchangeServiceStub.GetMarketTradedVolumeErrorEnum;
import generated.exchange.BFExchangeServiceStub.GetMarketTradedVolumeReq;
import generated.exchange.BFExchangeServiceStub.GetMarketTradedVolumeResp;
import generated.exchange.BFExchangeServiceStub.MUBet;
import generated.exchange.BFExchangeServiceStub.Market;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.PlaceBetsE;
import generated.exchange.BFExchangeServiceStub.PlaceBetsErrorEnum;
import generated.exchange.BFExchangeServiceStub.PlaceBetsReq;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResp;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResult;
import generated.exchange.BFExchangeServiceStub.SortOrderEnum;
import generated.exchange.BFExchangeServiceStub.UpdateBets;
import generated.exchange.BFExchangeServiceStub.UpdateBetsE;
import generated.exchange.BFExchangeServiceStub.UpdateBetsErrorEnum;
import generated.exchange.BFExchangeServiceStub.UpdateBetsReq;
import generated.exchange.BFExchangeServiceStub.UpdateBetsResp;
import generated.exchange.BFExchangeServiceStub.UpdateBetsResult;
import generated.exchange.BFExchangeServiceStub.VolumeInfo;

import java.util.Vector;
import java.util.concurrent.Semaphore;

import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import demo.util.APIContext;
import demo.util.InflatedCompleteMarketPrices;
import demo.util.InflatedMarketPrices;
import demo.util.RunnerTradedVolumeCompressed;

public class ExchangeAPI {
	
	//BoundedSemapho semaphore = new BoundedSemaphore(1);

	private static Semaphore sem=new Semaphore(1,true);
	
	// There are 2 Betfair exchanges, so a stub connection is needed for each
	public static enum Exchange {UK, AUS};
	private static BFExchangeServiceStub stub_UK;
	private static BFExchangeServiceStub stub_AUS;

	// This stub is used to make all requests to the Betfair Exchange API
	// The exchange API is used to place bets and query markets.
	public static BFExchangeServiceStub getStub(Exchange exch) throws Exception {
		switch (exch) {

			case UK: 
				// Lazy load the Exchange service stub generated by AXIS.
				if (stub_UK == null) {
					stub_UK = new BFExchangeServiceStub("https://api.betfair.com/exchange/v5/BFExchangeService");
			        
					ServiceContext context2 =stub_UK._getServiceClient().getServiceContext();
					MultiThreadedHttpConnectionManager connManager2 = (MultiThreadedHttpConnectionManager)context2.getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
					connManager2 = new MultiThreadedHttpConnectionManager();
					context2.setProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER, connManager2);
					connManager2.getParams().setMaxTotalConnections(100);
					connManager2.getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 100);
					
					// You may set up the connection parameters of the stub here if necessary
			        // For example: Wait 20 seconds for a response from the API
					stub_UK._getServiceClient().getOptions().setTimeOutInMilliSeconds(20 * 1000); 
					stub_UK._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.MC_ACCEPT_GZIP, "true");
					stub_UK._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.MC_GZIP_RESPONSE, "true");
					
				}
				return stub_UK;
			
			case AUS:
				// Lazy load the Exchange service stub generated by AXIS.
				if (stub_AUS == null) {
					stub_AUS = new BFExchangeServiceStub("https://api-au.betfair.com/exchange/v5/BFExchangeService");
			        
			        // You may set up the connection parameters of the stub here if necessary
			        // For example: Wait 20 seconds for a response from the API
					stub_AUS._getServiceClient().getOptions().setTimeOutInMilliSeconds(20 * 1000); 
					stub_AUS._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.MC_ACCEPT_GZIP, "true");
					stub_AUS._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.MC_GZIP_RESPONSE, "true");
				}
				return stub_AUS;
		}

		// Should never happen 
		throw new RuntimeException("Unable to get stub for exchange "+exch);
	}
	
	// Save the data from the request header into the context
	public static void setHeaderDataToContext(APIContext context, APIResponseHeader header) {
		context.setToken(header.getSessionToken()); // May be updated in each call.
		context.setLastCall(header.getTimestamp().getTime());
	}
	
	
	// Get the request header to add to the request
	public static APIRequestHeader getHeader(String token) {
        APIRequestHeader header = new APIRequestHeader();
        // The header must have the session context.getToken() attached.
        header.setSessionToken(token);
        return header;
	}

	// Get the account funds for the exchange
	public static GetAccountFundsResp getAccountFunds(Exchange exch, APIContext context) throws Exception {
		// Create a request object
        GetAccountFundsReq request = new GetAccountFundsReq();
        request.setHeader(getHeader(context.getToken()));
        
        // Create the GetAccountFunds message and attach the request to it.
        GetAccountFunds msg = new GetAccountFunds();
        msg.setRequest(request);
        
        // Send the request to the Betfair Exchange Service.
        GetAccountFundsResp resp=null;
        sem.acquire();
        try {
             resp = getStub(exch).getAccountFunds(msg).getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
        sem.release();
        context.getUsage().addCall("getAccountFunds");
        
        if(resp==null)
		{
			throw new IllegalArgumentException("Failed to retrieve data:  getStub(exch).getAccountFunds(msg).getResult() return null");
		}
        
        // Check the response code, and throw and exception if login failed
        if (resp.getErrorCode() != GetAccountFundsErrorEnum.OK)
        {
        	throw new IllegalArgumentException("Failed to retrieve account funds: "+resp.getErrorCode() + " Minor Error:"+resp.getMinorErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
        }

        // Transfer the response data back to the API context
        setHeaderDataToContext(context, resp.getHeader());
        
        return resp;
	}
	
	// Get the account funds for the exchange
	public static Market getMarket(Exchange exch, APIContext context, int marketId) throws Exception {
		// Create a request object
        GetMarketReq request = new GetMarketReq();
        request.setHeader(getHeader(context.getToken()));
        
        // Set the parameters
        request.setMarketId(marketId);
        
        // Create the GetMarket message and attach the request to it.
        GetMarket msg = new GetMarket();
        msg.setRequest(request);
        
        // Send the request to the Betfair Exchange Service.
        GetMarketResp resp = null;
        sem.acquire();
        try {
            resp = getStub(exch).getMarket(msg).getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
        sem.release();
        context.getUsage().addCall("getMarket");
        
        if(resp==null)
		{
			throw new IllegalArgumentException("Failed to retrieve data:  getStub(exch).getMarket(msg).getResult() return null");
		}
        
        
        // Check the response code, and throw and exception if call failed
        if (resp.getErrorCode() != GetMarketErrorEnum.OK)
        {
        	throw new IllegalArgumentException("Failed to retrieve data: "+resp.getErrorCode() + " Minor Error:"+resp.getMinorErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
        	
        }
        // Transfer the response data back to the API context
        setHeaderDataToContext(context, resp.getHeader());

        return resp.getMarket();
	}
							   
	public static VolumeInfo[] getMarketTradedVolume(Exchange exch, APIContext context, int marketId, int runnerId) throws Exception {
		GetMarketTradedVolumeReq request =new GetMarketTradedVolumeReq();
		request.setHeader(getHeader(context.getToken()));
		request.setMarketId(marketId);
		request.setSelectionId(runnerId);
		//request.setCurrencyCode(param)
		GetMarketTradedVolume msg= new GetMarketTradedVolume();
		msg.setRequest(request);
		
		GetMarketTradedVolumeResp resp = null;
		sem.acquire();
		try {
			resp = getStub(exch).getMarketTradedVolume(msg).getResult();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		sem.release();
		
		context.getUsage().addCall("getMarketTradedVolume");
		
		if(resp==null)
		{
			throw new IllegalArgumentException("Failed to retrieve data: getStub(exch).getMarketTradedVolume(msg).getResult() return null");
		}
		
		if (resp.getErrorCode() != GetMarketTradedVolumeErrorEnum.OK && resp.getHeader().getErrorCode() != APIErrorEnum.OK)
	    {
	        	throw new IllegalArgumentException("Failed to retrieve data: "+resp.getErrorCode() + " Minor Error:"+resp.getMinorErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
	        	
	    }
		
		setHeaderDataToContext(context, resp.getHeader());
		if(resp.getPriceItems()!=null)
			return resp.getPriceItems().getVolumeInfo();
		else 
			return new VolumeInfo[]{};
		
	}
	
	public static Vector<RunnerTradedVolumeCompressed> getMarketTradedVolumeCompressed(Exchange exch, APIContext context, int marketId) throws Exception {
		GetMarketTradedVolumeCompressedReq request =new GetMarketTradedVolumeCompressedReq();
		request.setHeader(getHeader(context.getToken()));
		request.setMarketId(marketId);
		request.setCurrencyCode("EUR");
		
		GetMarketTradedVolumeCompressed msg = new  GetMarketTradedVolumeCompressed();
		msg.setRequest(request);
		
		GetMarketTradedVolumeCompressedResp resp = null;
		sem.acquire();
		try {
			resp = getStub(exch).getMarketTradedVolumeCompressed(msg).getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sem.release();
		context.getUsage().addCall("getMarketTradedVolumeCompressed");
		
		if(resp==null)
		{
			throw new IllegalArgumentException("Failed to retrieve data: getStub(exch).getMarketTradedVolumeCompressed(msg).getResult() return null");
		}
		
		if (resp.getErrorCode() != GetMarketTradedVolumeCompressedErrorEnum.OK )//&& resp.getHeader().getErrorCode() != APIErrorEnum.OK)
	    {
	        	throw new IllegalArgumentException("Failed to retrieve data: "+resp.getErrorCode() + " Minor Error:"+resp.getMinorErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
	        	
	    }
		
		setHeaderDataToContext(context, resp.getHeader());
		//System.out.println(resp.getTradedVolume());
		Vector<RunnerTradedVolumeCompressed> ret=new  Vector<RunnerTradedVolumeCompressed>();
		if(resp.getTradedVolume()!=null)
		{
			//String completeS=resp.getTradedVolume();
			//String[] tradedVolume=completeS.substring(1, completeS.length()).split(":");
			
			String[] tradedVolume=resp.getTradedVolume().split(":");
			
			for(int i=1;i<tradedVolume.length;i++)
			{
				RunnerTradedVolumeCompressed rtvc=new RunnerTradedVolumeCompressed(tradedVolume[i]);
				ret.add(rtvc);
			}
		}
		return ret;
	}
	
	
	
	// Get the compressed version of the market prices data and re-inflate it.
	public static InflatedMarketPrices getMarketPrices(Exchange exch, APIContext context, int marketId) throws Exception {
		// Create a request object
		GetMarketPricesCompressedReq request = new GetMarketPricesCompressedReq();
        request.setHeader(getHeader(context.getToken()));
        
        // Set the parameters
        request.setMarketId(marketId);

        // Create the message and attach the request to it.
        GetMarketPricesCompressed msg = new GetMarketPricesCompressed();
        msg.setRequest(request);
        
        // Send the request to the Betfair Exchange Service.
        
        GetMarketPricesCompressedResp resp = null;
        sem.acquire();
        try {
        	resp = getStub(exch).getMarketPricesCompressed(msg).getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
        sem.release();
        context.getUsage().addCall("getMarketPricesCompressed");
        
        if(resp==null)
		{
			throw new IllegalArgumentException("Failed to retrieve data: getStub(exch).getMarketPricesCompressed(msg).getResult() return null");
		}

        // Check the response code, and throw and exception if call failed
        if (resp.getErrorCode() != GetMarketPricesErrorEnum.OK)
        {
        	throw new IllegalArgumentException("Failed to retrieve data: "+resp.getErrorCode() + " Minor Error:"+resp.getMinorErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
        	
        }
        // Transfer the response data back to the API context
        setHeaderDataToContext(context, resp.getHeader());

        
        return new InflatedMarketPrices(resp.getMarketPrices());
	}
	
	public static InflatedCompleteMarketPrices getCompleteMarketPrices(Exchange exch, APIContext context, int marketId) throws Exception {
		// create a request object
		GetCompleteMarketPricesCompressedReq request = new GetCompleteMarketPricesCompressedReq();
		request.setHeader(getHeader(context.getToken()));
		
		// Set the parameters
		request.setMarketId(marketId);
		
		// Create the message and attach the request to it.
		GetCompleteMarketPricesCompressed msg = new GetCompleteMarketPricesCompressed();
		msg.setRequest(request);
		
		// Send the request to the Betfair Exchange Service.
		GetCompleteMarketPricesCompressedResp resp = null;
		sem.acquire();
		try {
			resp = getStub(exch).getCompleteMarketPricesCompressed(msg).getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sem.release();
		context.getUsage().addCall("getCompleteMarketPricesCompressed");
		
		if(resp==null)
		{
			throw new IllegalArgumentException("Failed to retrieve data: getStub(exch).getCompleteMarketPricesCompressed(msg).getResult() return null");
		}
		
		// Check the response code and throw an exception if the call failed 
		if (resp.getErrorCode() != GetCompleteMarketPricesErrorEnum.OK) {
			throw new IllegalArgumentException("Failed to retrieve data: "+resp.getErrorCode() + " Minor Error:"+resp.getMinorErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
		}
		
		// Transfer the response data back to the API Context
		setHeaderDataToContext(context, resp.getHeader());
		
		return new InflatedCompleteMarketPrices(resp.getCompleteMarketPrices());
	}
	
	
	// Get all matched and unmatched bets on the market
	public static MUBet[] getMUBets(Exchange exch, APIContext context, int marketId) throws Exception {
		
		// Create a request object
		GetMUBetsReq request = new GetMUBetsReq();
		request.setHeader(getHeader(context.getToken()));
		
        // Set the parameters
		if (marketId > 0) {
			request.setMarketId(marketId);
		}
        request.setBetStatus(BetStatusEnum.MU);
        request.setSortOrder(SortOrderEnum.ASC);
        request.setOrderBy(BetsOrderByEnum.BET_ID);
        request.setRecordCount(100);
        request.setStartRecord(0);

        // Create the message and attach the request to it.
        GetMUBets msg = new GetMUBets();
        msg.setRequest(request);

        // Send the request to the Betfair Exchange Service.
        GetMUBetsResp resp = null;
        sem.acquire();
        try {
            resp = getStub(exch).getMUBets(msg).getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
        sem.release();
        context.getUsage().addCall("getMUBets");
        
        if(resp==null)
		{
			throw new IllegalArgumentException("Failed to retrieve data: getStub(exch).getMUBets(msg).getResult() return null");
		}
        
        // Check the response code, and throw and exception if call failed
        if ((resp.getErrorCode() != GetMUBetsErrorEnum.OK) &&
        		(resp.getErrorCode() != GetMUBetsErrorEnum.NO_RESULTS))
        {
        	throw new IllegalArgumentException("Failed to retrieve data: "+resp.getErrorCode() + " Minor Error:"+resp.getMinorErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
        	
        }
        // Transfer the response data back to the API context
        setHeaderDataToContext(context, resp.getHeader());

        //GetBetResp gbr = new GetBetResp();
       // gbr.getBet().
        
        if (resp.getErrorCode() == GetMUBetsErrorEnum.NO_RESULTS) {
        	return new MUBet[0];
        } else {
        	return resp.getBets().getMUBet();
        }
	}
	
	
	// Place some bets on the market
	public static PlaceBetsResult[] placeBets(Exchange exch, APIContext context, PlaceBets[] bets) throws Exception {
		
		
		

		
		// Create a request object
		PlaceBetsReq request = new PlaceBetsReq();
		request.setHeader(getHeader(context.getToken()));
		
        // Set the parameters
        ArrayOfPlaceBets betsArray = new ArrayOfPlaceBets();
        betsArray.setPlaceBets(bets);
        request.setBets(betsArray);
        // Create the message and attach the request to it.
        PlaceBetsE msg = new PlaceBetsE();
        msg.setRequest(request);

        // Send the request to the Betfair Exchange Service.
        PlaceBetsResp resp =null;
        //sem.acquire();
        try {
        	resp = getStub(exch).placeBets(msg).getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
        //sem.release();
        context.getUsage().addCall("placeBets");
        
        if(resp==null)
		{
			throw new IllegalArgumentException("Failed to retrieve data: getStub(exch).placeBets(msg).getResult() return null");
		}
        
        // Check the response code, and throw and exception if call failed
        if (resp.getErrorCode() != PlaceBetsErrorEnum.OK)
        {
        	throw new IllegalArgumentException("Failed to retrieve data: "+resp.getErrorCode() + " Minor Error:"+resp.getMinorErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
        	
        }

        // Transfer the response data back to the API context
        setHeaderDataToContext(context, resp.getHeader());

        return resp.getBetResults().getPlaceBetsResult();
	}
	
	// Update a bet on the market
	public static UpdateBetsResult[] updateBets(Exchange exch, APIContext context, UpdateBets[] bets) throws Exception {
		
		// Create a request object
		UpdateBetsReq request = new UpdateBetsReq();
		request.setHeader(getHeader(context.getToken()));
		
        // Set the parameters
        ArrayOfUpdateBets betsArray = new ArrayOfUpdateBets();
        betsArray.setUpdateBets(bets);
        request.setBets(betsArray);

        // Create the message and attach the request to it.
        UpdateBetsE msg = new UpdateBetsE();
        msg.setRequest(request);

        // Send the request to the Betfair Exchange Service.
        UpdateBetsResp resp = null;
        //sem.acquire();
        try {
            resp = getStub(exch).updateBets(msg).getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
        //sem.release();
        context.getUsage().addCall("updateBets");
        
        if(resp==null)
		{
			throw new IllegalArgumentException("Failed to retrieve data: getStub(exch).updateBets(msg).getResult() return null");
		}
        
        // Check the response code, and throw and exception if call failed
        if (resp.getErrorCode() != UpdateBetsErrorEnum.OK)
        {
        	throw new IllegalArgumentException("Failed to retrieve data: "+resp.getErrorCode() + " Minor Error:"+resp.getMinorErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
        	
        }

        // Transfer the response data back to the API context
        setHeaderDataToContext(context, resp.getHeader());

        return resp.getBetResults().getUpdateBetsResult();
	}
	
	// Cancel a bet on the market
	public static CancelBetsResult[] cancelBets(Exchange exch, APIContext context, CancelBets[] bets) throws Exception {
		
		// Create a request object
		CancelBetsReq request = new CancelBetsReq();
		request.setHeader(getHeader(context.getToken()));
		
        // Set the parameters
        ArrayOfCancelBets betsArray = new ArrayOfCancelBets();
        betsArray.setCancelBets(bets);
        request.setBets(betsArray);

        // Create the message and attach the request to it.
        CancelBetsE msg = new CancelBetsE();
        msg.setRequest(request);

        // Send the request to the Betfair Exchange Service.
        CancelBetsResp resp = null;
        sem.acquire();
        try {
            resp = getStub(exch).cancelBets(msg).getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
        sem.release();
        context.getUsage().addCall("cancelBets");
        
        if(resp==null)
		{
			throw new IllegalArgumentException("Failed to retrieve data: getStub(exch).cancelBets(msg).getResult() return null");
		}
        
        
        // Check the response code, and throw and exception if call failed
        if (resp.getErrorCode() != CancelBetsErrorEnum.OK)
        {
        	throw new IllegalArgumentException("Failed to retrieve data: "+resp.getErrorCode() + " Minor Error:"+resp.getMinorErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
        	
        }

        // Transfer the response data back to the API context
        setHeaderDataToContext(context, resp.getHeader());

        return resp.getBetResults().getCancelBetsResult();
	}
	
	public static String getAllMarkets()
	{
		return null;
	}
	
	
	public static Bet getBet(Exchange exch, APIContext context, long betId) throws Exception 
	{
		// Create a request object
		GetBetReq  request = new GetBetReq();
		request.setHeader(getHeader(context.getToken()));
		
		// Set the parameters
		request.setBetId(betId);
		
		// Create the message and attach the request to it.
		GetBet msg=new GetBet();
		msg.setRequest(request);
		
		// Send the request to the Betfair Exchange Service.
		GetBetResp resp = null;
		sem.acquire();
		try {
			resp = getStub(exch).getBet(msg).getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
        sem.release();
		context.getUsage().addCall("getBet");
		
		if(resp==null)
		{
			throw new IllegalArgumentException("Failed to retrieve data: getStub(exch).getBet(msg).getResult() return null");
		}
		
        // Check the response code, and throw and exception if call failed
        if (resp.getErrorCode() != GetBetErrorEnum.OK)
        {
        	throw new IllegalArgumentException("Failed to retrieve data: "+resp.getErrorCode() + " Minor Error:"+resp.getErrorCode()+ " Header Error:"+resp.getHeader().getErrorCode());
        	
        }

        // Transfer the response data back to the API context
        setHeaderDataToContext(context, resp.getHeader());
		
		return resp.getBet();
	}
	
}
