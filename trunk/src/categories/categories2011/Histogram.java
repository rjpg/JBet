package categories.categories2011;

public class Histogram {

	private int intervals[];
	
	private int size=0;
	private double sizeDouble=0;
	public double max=1000;
	public double min=-1000;
	public double precision=10.;
	
	public double norma=0.;
	
	public double nvalues=0;
	
	public Histogram(double mina, double maxa, double precisiona)
	{
		max=maxa;
		min=mina;
		precision=precisiona;
		
		size=(int) ((max-min)/precision);
		sizeDouble= ((max-min)/precision);
		intervals = new int[size+1];
		
		norma=((max-min));
		
		System.out.println("Size:"+(size+1));
	}
	
	public void addValue(double v)
	{
		//System.out.println((((v-min)*sizeDouble)/norma));
		int index=(int) Math.round(((v-min)*sizeDouble)/norma);

		if(index<intervals.length && index>=0)
		{
			intervals[index]++;
			nvalues++;
		}
	}
	
	public int[] getIntervals() {
		return intervals;
	}
	
	public double getMaxFiltred(double percent)
	{
		int acum=0;
		double end=((percent*nvalues)/100);
		//System.out.println("end:"+end);
		int i=0;
		for(i=0;acum<end;i++)
		{
			acum+=intervals[i];
		}
		
		
		return (precision*(i-1))+min;
	}
	
	public double getMinFiltred(double percent)
	{
		double acum=0;
		double end=((percent*nvalues)/100);
		//System.out.println("end:"+end);
		int i=0;
		for(i=intervals.length-1;acum<end;i--)
		{
			acum+=(double)intervals[i];
		}
		
		
		return (precision*(i+1))+min;
	}

	
	public static void main(String[] args)  throws Exception {
		Histogram h=new Histogram(-10, 10, 2);
		
		h.addValue(-10.4);
		
		h.addValue(5.7);
		h.addValue(5.7);
		h.addValue(5.7);
		h.addValue(5.7);
		h.addValue(5.7);
		h.addValue(5.7);
		h.addValue(5.7);
	//	h.addValue(5.7);
		

		
		for(int i=0; i<h.getIntervals().length;i++)
		{
			System.out.println("v["+i+"]="+h.getIntervals()[i]);
		}
		
		System.out.println(h.getMinFiltred(85));
		
	}
	
}
