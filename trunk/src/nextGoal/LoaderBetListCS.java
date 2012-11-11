package nextGoal;

import DataRepository.Utils;

public class LoaderBetListCS {

	public static void main(String[] args)  throws Exception {
		Utils.init();
		BFDataInit bfdata=new BFDataInit();
		new BetListCS(0,0,bfdata);
		new BetListCS(743,0,bfdata);
	}
}
