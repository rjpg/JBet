package horses;

public class HorsesUtils {

	
	public static String raceType[]={"5f","6f","7f","1m","1m1f","1m2f","1m3f","1m4f","1m5f","1m6f","1m7f","2m","2m1f","2m2f","2m3f","2m4f","2m5f","2m6f","2m7f","3m","3m1f","3m2f","3m3f","3m4f","3m5f","3m6f","3m7f","4m", "4m1f"};
	public static int timeInSeconds[]={60, 74 , 88 , 104, 122  , 140  ,  158 , 176  , 194  ,  212 ,  230 ,248 , 266  , 284  , 302  , 320  , 338  , 356  , 374  , 392, 410  , 428  ,  446 , 464 ,  482  , 500  , 518 , 536, 554};		
	
	public static int getTimeRaceInSeconds(String type)
	{
		
		for(int i =0;i<HorsesUtils.raceType.length;i++)
		{
			String field0=type.split(" ")[0];
			if(field0.equals(raceType[i]))
				return timeInSeconds[i];
		}
		return -1;
	}
	
	public static void main(String[] args) {
		for(int i =0;i<HorsesUtils.raceType.length;i++)
		{
			System.out.println("index : "+i+" raceType["+i+"]="+raceType[i]+" timeInSeconds["+i+"]="+timeInSeconds[i]);
		}
	}
}
