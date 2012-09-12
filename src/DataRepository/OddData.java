package DataRepository;

public class OddData {
	public double odd =0;
	public double amount=0;
	//public int depth=0;  // nova API não vem 
	//public double totalBSPBackersStake=0;
	//public double totalBSPLayLiability=0;
	
	public OddData(double oddA,double amountA)
	{
		odd=oddA;
		amount=amountA;
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
