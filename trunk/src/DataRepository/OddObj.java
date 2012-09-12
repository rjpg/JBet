package DataRepository;

public class OddObj {
	public double odd;
	public int index;
	
	public OddObj()
	{
		
	}
	
	public OddObj(double oddA,int indexA)
	{
		this();
		setOdd(oddA);
		setIndex(indexA);
	}
	
	public double getOdd() {
		return odd;
	}
	public void setOdd(double odd) {
		this.odd = odd;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		return ""+odd;
	}
	
}
