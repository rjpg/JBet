package bets;

import java.util.Calendar;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class BetManagerSim extends BetManager implements MarketChangeListener{

	public static long IN_PLAY_DELAY=0000;
	
	public Vector<BetData> bets=new Vector<BetData>();
	private static Semaphore sem=new Semaphore(1,true);
	
	private boolean polling = false;
	
	public BetManagerSim(MarketData mdA) {
		super(mdA);
		
		getMd().addMarketChangeListener(this);
		
	}

	@Override
	public Vector<BetData> getBets() {
		
		return bets;
		
	}

	@Override
	public Vector<BetData> getBetsByRunner(RunnersData rd) {
		Vector<BetData> ret=new Vector<BetData>();
		boolean found=false;
		for(BetData bd:bets)
		{
			if(bd.getRd()==rd)
			{
				found=true;
				ret.add(bd);
			}
		}
		if(found) return ret; else return null;
	}

	@Override
	public BetData getBetById(long ID) {
		for(BetData bd:bets)
		{
			if(bd.getBetID()!=null && bd.getBetID()==ID)
				return bd;
		}
		return null;
	}

	@Override
	public void placeBet(BetData bet) {
		Vector<BetData> place=new Vector<BetData>();
		place.add(bet);
		placeBets(place);
	}

	
	@Override
	public void placeBets(final Vector<BetData> place) {
		
		for(BetData bd:place)
		{
			bd.setState(BetData.PLACING, BetData.PLACE);
			
		}
		
		  Thread placeBetsThread = new Thread(){
	            public void run(){
	                try {
	                	                   
	                    Thread.sleep(BetManagerSim.IN_PLAY_DELAY);
	                    placeBetsThread(place);
	                   
	                    
	                } catch (InterruptedException ex) {
	                   
	                }
	            }
	        };
	      
	        placeBetsThread.start();
	       
	        
		
	}
	
	public void placeBetsThread(Vector<BetData> place) {
		
		BetData[] bds=place.toArray(new BetData[]{});
		
		for(int i=0;i<bds.length;i++)
		{
	
			if(bds[i].getType()==BetData.BACK)
			{
				bds[i].setEntryAmount(Utils.getAmountLayOddFrame(bds[i].getRd(), bds[i].getOddRequested(), 0));
				bds[i].setEntryVolume(Utils.getVolumeFrame(bds[i].getRd(), 0, bds[i].getOddRequested()));
				
				if(bds[i].getEntryAmount()<0)
				{
					//System.out.println("ODD Back:"+Utils.getOddBackFrame(bds[i].getRd(), 0));
					if(Utils.getOddBackFrame(bds[i].getRd(), 0)>bds[i].getOddRequested())
					{
						
						double odd=Utils.getOddBackFrame(bds[i].getRd(), 0);
						Vector<Double> amounts=new Vector<Double>();
						Vector<Double> ODDs=new Vector<Double>();
						double acum=0;
						
						while(odd>=bds[i].getOddRequested() && acum<bds[i].getAmount())
						{
							
							
							if(odd==bds[i].getOddRequested())
							{
								double am=bds[i].getAmount()-acum;
								if(am!=0)
								{
									amounts.add(am);
									ODDs.add(odd);
								}
								acum+=bds[i].getAmount()-acum;
								
								//System.out.println("odd-----"+odd+ "         am ---------- "+am);
								
								if(odd!=1.01)
									odd=Utils.indexToOdd(Utils.oddToIndex(odd)-1);
								else
									odd=1;
								
							}
							else
							{
								
								double am=Utils.getAmountBackOddFrame(bds[i].getRd(), odd, 0);
								if(am+acum>bds[i].getAmount())
								{
									am=bds[i].getAmount()-acum;
								}
								
								if(am!=0)
								{
									amounts.add(am);
									ODDs.add(odd);
								}
								acum+=am;
								
								//System.out.println("odd-----"+odd+ "         am ---------- "+am);
								
								odd=Utils.indexToOdd(Utils.oddToIndex(odd)-1);
								
							}
							
							
							
						}
						
						
						
						bds[i].setMatchedAmount(bds[i].getAmount());
						bds[i].setOddMached(Utils.calculateOddAverage(ODDs.toArray(new Double[]{}), amounts.toArray(new Double[]{})));
						//bds[i].setOddMached(bds[i].getOddRequested());
						bds[i].setTimestampFinalState(bds[i].getRd().getMarketData().getCurrentTime());
						bds[i].setState(BetData.MATCHED, BetData.PLACE);
					}
					else // can only be equal becouse bds[i].getEntryAmount() is negative
					{
						if((bds[i].getEntryAmount()*(-1))>=bds[i].getAmount()) // Available amount bigger or equal then bet amount - Mached
						{
							
							bds[i].setMatchedAmount(bds[i].getAmount());
							bds[i].setOddMached(bds[i].getOddRequested());
							bds[i].setTimestampFinalState(bds[i].getRd().getMarketData().getCurrentTime());
							bds[i].setState(BetData.MATCHED, BetData.PLACE);
						}
						else // Available amount smaller then bet amount - Partial Matched
						{
							
							bds[i].setMatchedAmount(Utils.convertAmountToBF(bds[i].getEntryAmount()*(-1)));
							bds[i].setOddMached(bds[i].getOddRequested());
							
							bds[i].setLastAvailableAmount(bds[i].getEntryAmount()*(-1));
							bds[i].setLastVolumeUpdate(bds[i].getEntryVolume());
							
							bds[i].setState(BetData.PARTIAL_MATCHED, BetData.PLACE);
							
							
						}
					}
					
				}
				else
				{
					bds[i].setLastVolumeUpdate(bds[i].getEntryVolume());
					bds[i].setState(BetData.UNMATCHED, BetData.PLACE);
					
				}
				
			}
			else //Lay
			{
				
				bds[i].setEntryAmount(Utils.getAmountBackOddFrame(bds[i].getRd(), bds[i].getOddRequested(), 0));
				//System.out.println("Entry Amount Back:"+Utils.getAmountBackOddFrame(bds[i].getRd(), bds[i].getOddRequested(), 0));
				bds[i].setEntryVolume(Utils.getVolumeFrame(bds[i].getRd(), 0, bds[i].getOddRequested()));
				
				if(bds[i].getEntryAmount()<0)
				{
					//System.out.println("ODD Lay:"+Utils.getOddLayFrame(bds[i].getRd(), 0));
					if(Utils.getOddLayFrame(bds[i].getRd(), 0)<bds[i].getOddRequested())
					{
						double odd=Utils.getOddLayFrame(bds[i].getRd(), 0);
						Vector<Double> amounts=new Vector<Double>();
						Vector<Double> ODDs=new Vector<Double>();
						double acum=0;
						
						while(odd<=bds[i].getOddRequested() && acum<bds[i].getAmount())
						{
							
							
							if(odd==bds[i].getOddRequested())
							{
								
								double am=bds[i].getAmount()-acum;
								if(am!=0)
								{
									amounts.add(am);
									ODDs.add(odd);
								}
								
								acum+=bds[i].getAmount()-acum;
								
								//System.out.println("odd-----"+odd+ "         am ---------- "+am);
								
								if(odd!=1000)
									odd=Utils.indexToOdd(Utils.oddToIndex(odd)+1);
								else
									odd=1001;
							}
							else
							{
								
								double am=Utils.getAmountLayOddFrame(bds[i].getRd(), odd, 0);
								if(am+acum>bds[i].getAmount())
								{
									am=bds[i].getAmount()-acum;
								}
								if(am!=0)
								{
									amounts.add(am);
									ODDs.add(odd);
								}
								acum+=am;
								
								//System.out.println("odd-----"+odd+ "         am ---------- "+am);
								
								odd=Utils.indexToOdd(Utils.oddToIndex(odd)+1);
							}
							
							
							
						}
						
						
						bds[i].setMatchedAmount(bds[i].getAmount());
						
						bds[i].setOddMached(Utils.calculateOddAverage(ODDs.toArray(new Double[]{}), amounts.toArray(new Double[]{})));
						//bds[i].setOddMached(bds[i].getOddRequested());
						bds[i].setTimestampFinalState(bds[i].getRd().getMarketData().getCurrentTime());
						bds[i].setState(BetData.MATCHED, BetData.PLACE);
					}
					else // can only be equal becouse bds[i].getEntryAmount() is negative
					{
						if((bds[i].getEntryAmount()*(-1))>=bds[i].getAmount()) // Available amount bigger or equal then bet amount - Mached
						{
							
							bds[i].setMatchedAmount(bds[i].getAmount());
							bds[i].setOddMached(bds[i].getOddRequested());
							bds[i].setTimestampFinalState(bds[i].getRd().getMarketData().getCurrentTime());
							bds[i].setState(BetData.MATCHED, BetData.PLACE);
						}
						else // Available amount smaller then bet amount - Partial Matched
						{
							
							bds[i].setMatchedAmount(Utils.convertAmountToBF(bds[i].getEntryAmount()*(-1)));
							bds[i].setOddMached(bds[i].getOddRequested());
							
							bds[i].setLastAvailableAmount(bds[i].getEntryAmount()*(-1));
							bds[i].setLastVolumeUpdate(bds[i].getEntryVolume());
							
							bds[i].setState(BetData.PARTIAL_MATCHED, BetData.PLACE);
							
							
						}
					}
				}
				else
				{
					
					bds[i].setLastVolumeUpdate(bds[i].getEntryVolume());
					bds[i].setState(BetData.UNMATCHED, BetData.PLACE);
				}
				
				
				
			}
			
			bds[i].setEntryVolume(Utils.getVolumeFrame(bds[i].getRd(), 0, bds[i].getOddRequested()));
			
			try {
				sem.acquire();
				bets.add(bds[i]);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sem.release();
			
			bds[i].setTimestampPlace(bds[i].getRd().getMarketData().getCurrentTime());
		
			
		}
		
		
	}

	@Override
	public int cancelBet(BetData bet) {
		Vector<BetData> place=new Vector<BetData>();
		place.add(bet);
		return cancelBets(place);
	}

	@Override
	public int cancelBets(Vector<BetData> cancelBets) {
		
		boolean someCanceled=false;
		boolean someNotCanceled=false;
		
		for(BetData bd:cancelBets)
		{
			if(BetUtils.isBetFinalState(bd.getState()))
			{
				someNotCanceled=true;
				continue;
			}
			
			if(bd.getState()==BetData.PLACING)
			{
				someNotCanceled=true;
				continue;
			}
			
			if(bd.getMatchedAmount()>0)
				if(bd.getMatchedAmount()>=bd.getAmount())
				{
					bd.setState(BetData.MATCHED, BetData.CANCEL);
					someCanceled=true;
				}
				else
				{
					bd.setState(BetData.PARTIAL_CANCELED, BetData.CANCEL);
					someCanceled=true;
				}
			else
			{
				bd.setState(BetData.CANCELED, BetData.CANCEL);
				someCanceled=true;
			}
			
			
		}
		
		if(!someCanceled)
			return -1;
		
		if(someNotCanceled)
			return -2;
		
		return 0;
	}

	@Override
	public void startPolling() {
		polling=true;
		
	}

	@Override
	public void stopPolling() {
		polling=false;
		
	}
	
	@Override
	public boolean isPolling() {

		return polling;	
	}

	
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketUpdate)
		{
			if(polling)
				refresh();
		}
		
	}

	
	private boolean isBetsToProcess()
	{
		boolean ret=false;
		for(BetData b:bets)
		{
			if(b.getState()==BetData.UNMATCHED || 
					b.getState()==BetData.PARTIAL_MATCHED || 
					b.getState()==BetData.BET_IN_PROGRESS ||
					b.getState()==BetData.CANCEL_WAIT_UPDATE)
				ret=true;
		}
		return ret;
	}
	
	
	private void refresh()
	{
		if(!isBetsToProcess())
			return;
		//System.out.println("Estou a processar");
		
		for(BetData bd:bets)
		{
		
			if(bd.getState()==BetData.PARTIAL_MATCHED || bd.getState()==BetData.UNMATCHED ) //|| bd.getState()==BetData.CANCEL_WAIT_UPDATE)
			{
				if(bd.getType()==BetData.BACK)
				{
					double oddBack=Utils.getOddBackFrame(bd.getRd(), 0);
					
					if(oddBack>bd.getOddRequested())
					{
					
						bd.setMatchedAmount(bd.getAmount());
						bd.setOddMached(bd.getOddRequested());
						bd.setState(BetData.MATCHED, BetData.SYSTEM);
					}
					else if(oddBack==bd.getOddRequested())
					{
						bd.setEntryAmount(0);
						//System.out.println("equal : "+oddBack);
						double amountBack=Utils.getAmountBackOddFrame(bd.getRd(), oddBack, 0);
						
						double amountIncrease=amountBack-bd.getLastAvailableAmount();
						if(amountIncrease<0)
							amountIncrease=0;
						
						double volumeIncrease=Utils.getVolumeFrame(bd.getRd(), 0, oddBack)-bd.getLastVolumeUpdate();
						if(volumeIncrease<=0)
						{
							volumeIncrease=0;
						}
						else
						{
							amountIncrease+=volumeIncrease/2;
						}
							
						
						double missingAmount=bd.getAmount()-bd.getMatchedAmount();
						
						
						
						if(amountIncrease>=missingAmount)
						{
							bd.setMatchedAmount(bd.getAmount());
							bd.setOddMached(oddBack);
							bd.setState(BetData.MATCHED, BetData.SYSTEM);
						}
						else
						{
							bd.setMatchedAmount(bd.getMatchedAmount()+amountIncrease);
							bd.setOddMached(oddBack);
							
							bd.setLastAvailableAmount(Utils.getAmountBackOddFrame(bd.getRd(), oddBack, 0));
							bd.setLastVolumeUpdate(Utils.getVolumeFrame(bd.getRd(), 0, oddBack));
							
							bd.setState(BetData.PARTIAL_MATCHED, BetData.SYSTEM);
						}
						
						
						
					}
					else // oddBack<bd.getOddRequested()
					{
						double missingAmount=bd.getAmount()-bd.getMatchedAmount();
						
						double volumeIncrease=Utils.getVolumeFrame(bd.getRd(), 0, bd.getOddRequested())-bd.getLastVolumeUpdate();
						
						double amountIncrease=0;
						
						if(volumeIncrease<=0)
						{
							volumeIncrease=0;
							bd.setLastAvailableAmount(0);
							continue;
						}
						else
						{
							amountIncrease=volumeIncrease/2;
						}
						
						
						//System.out.println("amountIncrease : "+amountIncrease);
						//System.out.println("bd.getEntryAmount() : "+bd.getEntryAmount());
						if(amountIncrease>bd.getEntryAmount())
						{
							double someMatchedAm=amountIncrease-bd.getEntryAmount();
							
							
							if(someMatchedAm>=missingAmount)
							{
								bd.setMatchedAmount(bd.getAmount());
								bd.setOddMached(bd.getOddRequested());
								bd.setState(BetData.MATCHED, BetData.SYSTEM);
							}
							else
							{
								bd.setEntryAmount(0);
								bd.setMatchedAmount(bd.getMatchedAmount()+someMatchedAm);
								bd.setOddMached(bd.getOddRequested());
								
								bd.setLastVolumeUpdate(Utils.getVolumeFrame(bd.getRd(), 0, bd.getOddRequested()));
								bd.setLastAvailableAmount(0);
								
								bd.setState(BetData.PARTIAL_MATCHED, BetData.SYSTEM);
							}
								
						}
						else
						{
							bd.setEntryAmount(bd.getEntryAmount()-amountIncrease);
							bd.setLastVolumeUpdate(Utils.getVolumeFrame(bd.getRd(), 0, bd.getOddRequested()));
							bd.setLastAvailableAmount(0);
						}
					}
				}
				else
				{
					
					double oddLay=Utils.getOddLayFrame(bd.getRd(), 0);
					
					if(oddLay<bd.getOddRequested())
					{
					
						bd.setMatchedAmount(bd.getAmount());
						bd.setOddMached(bd.getOddRequested());
						bd.setState(BetData.MATCHED, BetData.SYSTEM);
					}
					else if(oddLay==bd.getOddRequested())
					{
						//System.out.println("equal : "+oddLay);
						bd.setEntryAmount(0);
						
						double amountLay=Utils.getAmountLayOddFrame(bd.getRd(), oddLay, 0);
						
						double amountIncrease=amountLay-bd.getLastAvailableAmount();
						if(amountIncrease<0)
							amountIncrease=0;
						
						double volumeIncrease=Utils.getVolumeFrame(bd.getRd(), 0, oddLay)-bd.getLastVolumeUpdate();
						if(volumeIncrease<=0)
						{
							volumeIncrease=0;
						}
						else
						{
							amountIncrease+=volumeIncrease/2;
						}
							
						
						double missingAmount=bd.getAmount()-bd.getMatchedAmount();
						
						
						
						if(amountIncrease>=missingAmount)
						{
							bd.setMatchedAmount(bd.getAmount());
							bd.setOddMached(oddLay);
							bd.setState(BetData.MATCHED, BetData.SYSTEM);
						}
						else
						{
							bd.setMatchedAmount(bd.getMatchedAmount()+amountIncrease);
							bd.setOddMached(oddLay);
							
							bd.setLastAvailableAmount(Utils.getAmountLayOddFrame(bd.getRd(), oddLay, 0));
							bd.setLastVolumeUpdate(Utils.getVolumeFrame(bd.getRd(), 0, oddLay));
							
							bd.setState(BetData.PARTIAL_MATCHED, BetData.SYSTEM);
						}
						
						
						
					}
					else // oddBack<bd.getOddRequested()
					{
						double missingAmount=bd.getAmount()-bd.getMatchedAmount();
						
						double volumeIncrease=Utils.getVolumeFrame(bd.getRd(), 0, bd.getOddRequested())-bd.getLastVolumeUpdate();
						
						double amountIncrease=0;
						
						if(volumeIncrease<=0)
						{
							volumeIncrease=0;
							bd.setLastAvailableAmount(0);
							continue;
						}
						else
						{
							amountIncrease=volumeIncrease/2;
						}
						
						
						//System.out.println("amountIncrease : "+amountIncrease);
						//System.out.println("bd.getEntryAmount() : "+bd.getEntryAmount());
						
						if(amountIncrease>bd.getEntryAmount())
						{
							double someMatchedAm=amountIncrease-bd.getEntryAmount();
							
							
							if(someMatchedAm>=missingAmount)
							{
								bd.setMatchedAmount(bd.getAmount());
								bd.setOddMached(bd.getOddRequested());
								bd.setState(BetData.MATCHED, BetData.SYSTEM);
							}
							else
							{
								bd.setEntryAmount(0);
								bd.setMatchedAmount(bd.getMatchedAmount()+someMatchedAm);
								bd.setOddMached(bd.getOddRequested());
								
								bd.setLastVolumeUpdate(Utils.getVolumeFrame(bd.getRd(), 0, bd.getOddRequested()));
								bd.setLastAvailableAmount(0);
								
								bd.setState(BetData.PARTIAL_MATCHED, BetData.SYSTEM);
							}
								
						}
						else
						{
							bd.setEntryAmount(bd.getEntryAmount()-amountIncrease);
							bd.setLastVolumeUpdate(Utils.getVolumeFrame(bd.getRd(), 0, bd.getOddRequested()));
							bd.setLastAvailableAmount(0);
						}
					}
					
					
				}
			}
		}
	}
	
	@Override
	public void clean() {
		
		
		getMd().removeMarketChangeListener(this);
		
		for(BetData bd:bets)
		{
			if(!BetUtils.isBetFinalState(bd.getState()))
				bd.setState(BetData.UNMONITORED, BetData.SYSTEM);
			
		}
		bets.clear();
		bets=null;
		md=null;
		
	}

	@Override
	public void setPollingInterval(int milis) {
		// Nothing to do update in simulation is always BetManager.SYNC_MARKET_DATA_UPDATE  
		
	}


}
