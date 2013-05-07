package TradeMechanisms.dutching;

import java.util.Vector;

import TradeMechanisms.close.ClosePosition;
import TradeMechanisms.open.OpenPosition;
import bets.BetData;
import DataRepository.OddData;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class DutchingRunnerOptions {

	private RunnersData rd=null;

	// open vars
	private double oddOpenInfo=-1;
			
	// close type and also open type (in case of not completed open)
	private int timeHoldForceClose=0;
	
	// dynamic vars
	private OddData oddData=null; // to process global dutching
	
	private OpenPosition open=null;
	private ClosePosition close=null;
	
	public DutchingRunnerOptions(RunnersData runnerA,double oddOpenInfoA) 
	{
		rd=runnerA;
		oddOpenInfo=oddOpenInfoA;
	}
	
	public DutchingRunnerOptions(RunnersData runnerA,int timeHoldForceCloseA) 
	{
		rd=runnerA;
		timeHoldForceClose=timeHoldForceCloseA;
			
	}

	
	public boolean isOpen() {
		if(oddOpenInfo==-1)
			return false;
		else
			return true;
	}

	public int getTimeHoldForceClose() {
		return timeHoldForceClose;
	}

	public void setTimeHoldForceClose(int timeHoldForceClose) {
		this.timeHoldForceClose = timeHoldForceClose;
	}

	public RunnersData getRd() {
		return rd;
	}

	public void setRd(RunnersData rd) {
		this.rd = rd;
	}

	public double getOddOpenInfo() {
		return oddOpenInfo;
	}

	public void setOddOpenInfo(double oddOpenInfoA) {
		this.oddOpenInfo = oddOpenInfoA;
	}

	protected OddData getOddData() {
		return oddData;
	}

	protected void setOddData(OddData oddData) {
		this.oddData = oddData;
	}

	protected OpenPosition getOpen() {
		return open;
	}

	protected void setOpen(OpenPosition open) {
		this.open = open;
	}

	protected ClosePosition getClose() {
		return close;
	}

	protected void setClose(ClosePosition close) {
		this.close = close;
	}
	
	protected Vector<BetData> getMatchedInfo()
	{
		Vector<BetData> ret=new Vector<BetData>();
		
		if(open!=null)
		{
			for(BetData bd:open.getMatchedInfo())
				ret.add(bd);
		}
		
		if(close!=null)
		{
			for(BetData bd:close.getMatchedInfo())
				ret.add(bd);
		}
		
		return ret;
	}
	
	protected Vector<OddData> getMatchedInfoOddData()
	{
		Vector<BetData> vbd=getMatchedInfo();
		
		Vector<OddData> ret=new Vector<OddData>();
		
		for(BetData bd:vbd)
			ret.add(bd.getOddDataMatched());
			
		return ret;
	}
	
	protected double getActualOdd()
	{
		double actualOdd=0;
		actualOdd=Utils.getOddBackFrame(getRd(), 0);
		if(actualOdd==0)
			actualOdd=1.01;
		
		return actualOdd;
	}
	
}
