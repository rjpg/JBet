package statistics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Statistics {

	public static BufferedWriter out=null;
	
	public static void init()
	{
		try {
		out = new BufferedWriter(new FileWriter("statistics.txt", true));
		} catch (IOException e) {
		e.printStackTrace();
		System.out.println("Error Open statistics.txt for writing");
		}
	}
	
	synchronized public static void writeStatistics(int raceEntryMinute, double raceEntryAmount, double horseEntryAmount, int raceRunners, boolean favorite,String favoriteName,long timeStamp, int redOrGreen, int entryUpDown, double entryOdd, double exitOdd,int ticksMoved ,double stake,double exitStake,double amountMade, int minutesToStart,String hourse,int neighbourDist, String neighbourHorse,double volumeIncBack,double volumeIncLay,int dayOfWeek )
	{
		try {
			out = new BufferedWriter(new FileWriter("statistics.txt", true));
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open statistics.txt for writing");
			}
		if(out==null)
		{
			System.err.println("could not open statistics.txt" );
			return;
		}
		
		String s=raceEntryMinute+" "+raceEntryAmount+" "+horseEntryAmount+" "+raceRunners+" "+favorite+" \""+favoriteName+"\" "+timeStamp+" "+redOrGreen+" "+entryUpDown+" "+entryOdd+" "+exitOdd+" "+ticksMoved+" "+stake+" "+exitStake+" "+amountMade+" "+minutesToStart+" \""+hourse+"\" "+neighbourDist+" \""+neighbourHorse+"\" "+volumeIncBack+" "+volumeIncLay+" "+dayOfWeek; 
		try {
			out.write(s);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			System.out.println("SaveFav:Error wrtting data to log file");
			e.printStackTrace();
		}
		
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
