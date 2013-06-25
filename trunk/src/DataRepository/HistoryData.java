package DataRepository;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

public class HistoryData {
	
	public Calendar timestamp;
	
	int state=MarketData.ACTIVE;
	
	double oddLay=0;
	double amountLay=0;
	
	double oddBack=0;
	double amountBack=0;
	
	//double weightmoneyLay=0; 
	//double weightmoneyBack=0;
	
	double matchedAmount=0;
	double lastMatchet=0;
	
	public Vector<OddData> layPrices = null;
	public Vector<OddData> backPrices = null;
	
	public Hashtable<Double, Double> volume=null;
		
	public HistoryData(Calendar timeA, double oddLayA,double amountLayA, double oddBackA,double amountBackA,/* double weightmoneyLayA, double weightmoneyBackA,*/ double matchedAmountA, double lastMatchetA, Vector<OddData> layPricesA, Vector<OddData> backPricesA,int stateA)
	{
		timestamp=timeA;
		
		oddLay=oddLayA;
		amountLay=amountLayA;
		
		oddBack=oddBackA;
		amountBack=amountBackA;
		
		matchedAmount=matchedAmountA;
		lastMatchet=lastMatchetA;
		
		//weightmoneyLay=weightmoneyLayA; 
		//weightmoneyBack=weightmoneyBackA;
		
		layPrices = layPricesA;
		backPrices = backPricesA;
		
		state=stateA;
	}
	
	public Calendar getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}
	
	public double getOddLay() {
		return oddLay;
	}
	public void setOddLay(double odd) {
		this.oddLay = odd;
	}
	
	public double getAmountLay() {
		return amountLay;
	}
	
	public void setAmountLay(double amount) {
		this.amountLay = amount;
	}
	
	public double getOddBack() {
		return oddBack;
	}

	public void setOddBack(double oddBack) {
		this.oddBack = oddBack;
	}

	public double getAmountBack() {
		return amountBack;
	}

	public void setAmountBack(double amountBack) {
		this.amountBack = amountBack;
	}
	
	public double getMatchedAmount() {
		return matchedAmount;
	}
	
	public void setMatchedAmount(double matchedAmount) {
		this.matchedAmount = matchedAmount;
	}
	
	public double getLastMatchet() {
		return lastMatchet;
	}
	
	public void setLastMatchet(double lastMatchet) {
		this.lastMatchet = lastMatchet;
	}
	/*
	public double getWeightmoneyLay() {
		return weightmoneyLay;
	}

	public double getWeightmoneyBack() {
		return weightmoneyBack;
	}

	public void setWeightmoneyLay(double weightmoneyLay) {
		this.weightmoneyLay = weightmoneyLay;
	}

	public void setWeightmoneyBack(double weightmoneyBack) {
		this.weightmoneyBack = weightmoneyBack;
	}
	*/
	public Vector<OddData> getLayPrices() {
		return layPrices;
	}

	public Vector<OddData> getBackPrices() {
		return backPrices;
	}

	public Hashtable<Double, Double> getVolume() {
		return volume;
	}

	public void setVolume(Hashtable<Double, Double> volume) {
		this.volume = volume;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public void clean()
	{
		//System.out.println("foi chamado o clean na folha History Data");
		timestamp=null;
		layPrices.clear();
		layPrices = null;
		backPrices.clear();
		backPrices = null;
		volume.clear();
		volume=null;
	}
}
