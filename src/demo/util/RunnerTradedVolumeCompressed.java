package demo.util;

import java.util.Hashtable;

public class RunnerTradedVolumeCompressed {
	public int id=0;
	Hashtable<Double, Double> volumeLadder=new Hashtable<Double, Double>(); //Odd-Volume
	
	public RunnerTradedVolumeCompressed(String runnerLine) {
		//System.out.println(runnerLine);
		String[] tradedVolume=runnerLine.split("\\|");
		//System.out.println("id-string:"+tradedVolume[3]);
		id=Integer.parseInt(tradedVolume[0].split("~")[0]);
		//System.out.println(id);
		for(int i=1;i<tradedVolume.length;i++)
		{
			String oddVol[]=tradedVolume[i].split("~");
			double odd=Double.parseDouble(oddVol[0]);
			double vol=Double.parseDouble(oddVol[1]);
			//System.out.println("ID:"+id+" odd:"+odd+" Vol"+vol);
			volumeLadder.put(odd, vol);
			
			//ret.add(rtvc);
		}
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Hashtable<Double, Double> getVolumeLadder() {
		return volumeLadder;
	}

	public void setVolumeLadder(Hashtable<Double, Double> volumeLadder) {
		this.volumeLadder = volumeLadder;
	}
	
	
}
