package TradeMechanisms.dutching;

import DataRepository.OddData;
import DataRepository.RunnersData;

public class DutchingRunnerOptions {

	private boolean open=false;
	private RunnersData rd=null;

	// open type
	private OddData oddDataInfo=null;
			
	// close type and also open type (in case of not completed open)
	private int timeHoldForceClose=10;
	
		
	public DutchingRunnerOptions(RunnersData runnerA,OddData oddDataInfoA,int timeWaitOpenA,int timeHoldForceCloseA) 
	{
		open=true;
		rd=runnerA;
		oddDataInfo=oddDataInfoA;
		timeHoldForceClose=timeHoldForceCloseA;
			
	}
	
	public DutchingRunnerOptions(RunnersData runnerA,int timeHoldForceCloseA,boolean forceCloseA) 
	{
		open=true;
		rd=runnerA;
		timeHoldForceClose=timeHoldForceCloseA;
			
	}

	
	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
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

	public OddData getOddDataInfo() {
		return oddDataInfo;
	}

	public void setOddDataInfo(OddData oddDataInfo) {
		this.oddDataInfo = oddDataInfo;
	}


	
}
