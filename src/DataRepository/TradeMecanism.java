package DataRepository;

import java.awt.Color;

public interface TradeMecanism {
	public void forceClose();
	public int getState();
	public void writeMsgTM(String s, Color c);
}
