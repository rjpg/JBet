package bets;

import java.util.Calendar;
import java.util.Vector;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;
import DataRepository.RunnersData;
import DataRepository.Utils;

public class BetManagerSim extends BetManager implements MarketChangeListener{

	public Vector<BetData> bets=new Vector<BetData>();
	
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
	public int placeBet(BetData bet) {
		Vector<BetData> place=new Vector<BetData>();
		place.add(bet);
		return placeBets(place);
	}

	@Override
	public int placeBets(Vector<BetData> place) {
		
		BetData[] bds=place.toArray(new BetData[]{});
		
		for(int i=0;i<bds.length;i++)
		{
	
			if(bds[i].getType()==BetData.BACK)
			{
				bds[i].setEntryAmount(Utils.getAmountLayOddFrame(bds[i].getRd(), bds[i].getOddRequested(), 0));
				
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
						
						
						bds[i].setState(BetData.MATCHED, BetData.PLACE);
						bds[i].setMatchedAmount(bds[i].getAmount());
						bds[i].setOddMached(Utils.calculateOddAverage(ODDs.toArray(new Double[]{}), amounts.toArray(new Double[]{})));
						bds[i].setTimestampFinalState(Calendar.getInstance());
					}
					else // can only be equal becouse bds[i].getEntryAmount() is negative
					{
						if((bds[i].getEntryAmount()*(-1))>=bds[i].getAmount()) // Available amount bigger or equal then bet amount - Mached
						{
							bds[i].setState(BetData.MATCHED, BetData.PLACE);
							bds[i].setMatchedAmount(bds[i].getAmount());
							bds[i].setOddMached(bds[i].getOddRequested());
							bds[i].setTimestampFinalState(Calendar.getInstance());
						}
						else // Available amount smaller then bet amount - Partial Matched
						{
							bds[i].setState(BetData.PARTIAL_MATCHED, BetData.PLACE);
							bds[i].setMatchedAmount(Utils.convertAmountToBF(bds[i].getEntryAmount()*(-1)));
							bds[i].setOddMached(bds[i].getOddRequested());
							
						}
					}
					
				}
				else
				{
					bds[i].setState(BetData.UNMATCHED, BetData.PLACE);
					
				}
				
			}
			else //Lay
			{
				
				bds[i].setEntryAmount(Utils.getAmountBackOddFrame(bds[i].getRd(), bds[i].getOddRequested(), 0));
				//System.out.println("Entry Amount Back:"+Utils.getAmountBackOddFrame(bds[i].getRd(), bds[i].getOddRequested(), 0));
				
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
						
						bds[i].setState(BetData.MATCHED, BetData.PLACE);
						bds[i].setMatchedAmount(bds[i].getAmount());
						
						bds[i].setOddMached(Utils.calculateOddAverage(ODDs.toArray(new Double[]{}), amounts.toArray(new Double[]{})));
						bds[i].setTimestampFinalState(Calendar.getInstance());
					}
					else // can only be equal becouse bds[i].getEntryAmount() is negative
					{
						if((bds[i].getEntryAmount()*(-1))>=bds[i].getAmount()) // Available amount bigger or equal then bet amount - Mached
						{
							bds[i].setState(BetData.MATCHED, BetData.PLACE);
							bds[i].setMatchedAmount(bds[i].getAmount());
							bds[i].setOddMached(bds[i].getOddRequested());
							bds[i].setTimestampFinalState(Calendar.getInstance());
						}
						else // Available amount smaller then bet amount - Partial Matched
						{
							bds[i].setState(BetData.PARTIAL_MATCHED, BetData.PLACE);
							bds[i].setMatchedAmount(Utils.convertAmountToBF(bds[i].getEntryAmount()*(-1)));
							bds[i].setOddMached(bds[i].getOddRequested());
							
							
						}
					}
				}
				else
				{
					bds[i].setState(BetData.UNMATCHED, BetData.PLACE);
				}
				
				
				
			}
			
			bds[i].setEntryVolume(Utils.getVolumeFrame(bds[i].getRd(), 0, bds[i].getOddRequested()));
			
			bets.add(bds[i]);
			bds[i].setTimestampPlace(Calendar.getInstance());
		
			
			//if()
		}
		
		
		
		
		return 0;
	}

	@Override
	public int cancelBet(BetData bet) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int cancelBets(Vector<BetData> cancelBets) {
		// TODO Auto-generated method stub
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
	public void MarketChange(MarketData md, int marketEventType) {
		if( marketEventType==MarketChangeListener.MarketUpdate)
		{
			refresh();
		}
		
	}

	private void refresh()
	{
		
	}
	


}
