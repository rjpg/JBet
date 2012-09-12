package nextGoal;

import DataRepository.Utils;

public class LoaderNextGoal {
	public static void main(String[] args)  throws Exception {
		Utils.init();
		new InterfaceNextGoal();
	}
}
