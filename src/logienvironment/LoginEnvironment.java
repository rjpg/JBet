package logienvironment;

import generated.exchange.BFExchangeServiceStub.GetAccountFundsResp;
import bfapi.handler.ExchangeAPI;
import bfapi.handler.GlobalAPI;
import bfapi.handler.ExchangeAPI.Exchange;
import demo.util.APIContext;
import demo.util.Display;


public class LoginEnvironment {

	public String username=null;
	public String password=null;
	
	public Exchange selectedExchange=Exchange.UK;
	public APIContext apiContext=new APIContext();
	
	public GetAccountFundsResp funds=null;
	
	public LoginEnvironment() {
	
	}
	
	public Exchange getSelectedExchange() {
		return selectedExchange;
	}

	public APIContext getApiContext() {
		return apiContext;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public int login()
	{
		try {
			GlobalAPI.login(apiContext, username, password);
		
		} catch (Exception e) {
			Display.showException("*** Failed to Login : ", e);
			return -1;
		}
		
		return 0;
	}
	
	public int  logout()
	{
		try {
			GlobalAPI.logout(apiContext);
		} catch (Exception e) {
			// If we can't log out for any reason, there's not a lot to do.
			Display.showException("Failed to Logout : ", e);
			return -1;
		}
		
		return 0;
	}
	
	public int refreshAccountFunds() {
		try {
			funds = ExchangeAPI.getAccountFunds(getSelectedExchange(), getApiContext());
		} catch (Exception e) {
			Display.showException("*** Failed to Refresh Account Funds", e);
			return -1;
		}
		return 0;
		
	}
	
	public double getBalance()
	{
		return funds.getBalance();
	}
	
	public double getAvailBalance()
	{
		return funds.getAvailBalance();
	}
	
	public double getCreditLimit()
	{
		return funds.getCreditLimit();
	}
	
	public double getCurrentBetfairPoints()
	{
		return funds.getCurrentBetfairPoints();
	}
	
	public double getExposure()
	{
		return funds.getExposure();
	}
	
	public double getExposureLimit()
	{
		return funds.getExpoLimit();
	}
	
	

}
