package bets;

import java.util.Vector;

public interface BetListener {
	public void betChange(Vector<BetData> bd);
}
