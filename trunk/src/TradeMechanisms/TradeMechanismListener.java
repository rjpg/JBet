package TradeMechanisms;

import java.awt.Color;

public interface TradeMechanismListener {
	void tradeMechanismChangeState(TradeMechanism tm,int state);
	void tradeMechanismEnded(TradeMechanism tm,int state);
	void tradeMechanismMsg(TradeMechanism tm,String msg,Color color);
}
