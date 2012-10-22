package DataRepository;

import generated.exchange.BFExchangeServiceStub.Runner;

public class RunnerObj {
	
	Runner runner;

	public RunnerObj(Runner runnerA)
	{
		runner=runnerA;
	}
	
	public Runner getRunner() {
		return runner;
	}

	public void setRunner(Runner runner) {
		this.runner = runner;
	}
	
	@Override
	public String toString() {
		if(runner!=null)
			return runner.getName();
		else
			return null;
	}
}
