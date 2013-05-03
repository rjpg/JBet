package DataRepository;

import bets.BetData;

public class OddData {
	public double odd =0;
	public double amount=0;
	//public int depth=0;  // nova API n�o vem 
	//public double totalBSPBackersStake=0;
	//public double totalBSPLayLiability=0;
	public int type = BetData.BACK; // optional 
	
	public OddData(double oddA,double amountA)
	{
		odd=oddA;
		amount=amountA;
		//depth=depthA;
		//totalBSPBackersStake=aTotalBSPBackersStake;
		//totalBSPLayLiability=atotalBSPLayLiability;
	}
	
	public OddData(double oddA,double amountA,int typeA)
	{
		odd=oddA;
		amount=amountA;
		type=typeA;
		//depth=depthA;
		//totalBSPBackersStake=aTotalBSPBackersStake;
		//totalBSPLayLiability=atotalBSPLayLiability;
	}
	
	public double getOdd() {
		return odd;
	}
	
	public void setOdd(double odd) {
		this.odd = odd;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
	@Override
	public String toString() {
		if(type==BetData.BACK)
			return amount+" @ "+odd+" (Back)";
		else
			return amount+" @ "+odd+" (LAY)";
	}
//	public int getDepth() {
//		return depth;
//	}
//	public void setDepth(int depth) {
//		this.depth = depth;
//	}
//	
//	public void setTotalBSPBackersStake(double atotalBSPBackersStake) {
//		totalBSPBackersStake = atotalBSPBackersStake;
//	}
//
//	public void setTotalBSPLayLiability(double atotalBSPLayLiability) {
//		totalBSPLayLiability = atotalBSPLayLiability;
//	}
//	
//	public double getTotalBSPBackersStake() {
//		return totalBSPBackersStake;
//	}
//
//	public double getTotalBSPLayLiability() {
//		return totalBSPLayLiability;
//	}

	
}
