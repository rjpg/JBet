package TradeMechanisms.dutching;

import java.util.Vector;

import TradeMechanisms.TradeMechanism;
import TradeMechanisms.TradeMechanismListener;
import TradeMechanisms.close.ClosePosition;
import TradeMechanisms.open.OpenPosition;

public class Dutching extends TradeMechanism {
	
	private Vector<DutchingRunnerOptions> vdro=new Vector<DutchingRunnerOptions>();
	
	private Vector<OpenPosition> openTMs=new Vector<OpenPosition>();
	private Vector<ClosePosition> closeTMs=new Vector<ClosePosition>();
	
	private double globalStake=0;
	private int timeWaitOpen=10;
	
	public Dutching(Vector<DutchingRunnerOptions> vdroA,double globalStakeA, int timeWaitOpenA) {
		vdro=vdroA;
		globalStake=globalStakeA;
		timeWaitOpen=timeWaitOpenA;
	}
	
	
	@Override
	public void forceClose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forceCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getStatisticsFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatisticsValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clean() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTradeMechanismListener(TradeMechanismListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTradeMechanismListener(TradeMechanismListener listener) {
		// TODO Auto-generated method stub
		
	}

}
