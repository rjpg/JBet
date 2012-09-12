package DataRepository;

import generated.exchange.BFExchangeServiceStub.Bet;
import generated.exchange.BFExchangeServiceStub.BetCategoryTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetPersistenceTypeEnum;
import generated.exchange.BFExchangeServiceStub.BetStatusEnum;
import generated.exchange.BFExchangeServiceStub.BetTypeEnum;
import generated.exchange.BFExchangeServiceStub.CancelBets;
import generated.exchange.BFExchangeServiceStub.CancelBetsResult;
import generated.exchange.BFExchangeServiceStub.PlaceBets;
import generated.exchange.BFExchangeServiceStub.PlaceBetsResult;
import generated.exchange.BFExchangeServiceStub.UpdateBets;
import generated.exchange.BFExchangeServiceStub.UpdateBetsResult;

import java.awt.Color;
import java.util.Vector;

import main.Manager;
import main.Parameters;
import bots.Bot;
import demo.handler.ExchangeAPI;

public class SwingFrontLine implements TradeMecanism{

	
	static public final int UNDEFINED = -1;
	static public final int END = 0;
	static public final int START = 1;
	static public final int OPEN = 2;
	static public final int WAIT_OPEN = 3;
	static public final int WAIT_CLOSE = 4;
	static public final int CLOSE = 5;
	static public final int CLOSE_EMERGENCY = 6;
	static public final int EDGE = 7;
	static public final int WAIT_EDGE = 8;
	static public final int EXIT = 9;
	
	
	public int STATE= SwingFrontLine.UNDEFINED;
	
	protected int updateInterval = 300;
	 
	public MarketData marketData=null;
	public RunnersData rd=null;
	
	public int waitFramesNormal=100; 
	public int waitFramesEmergency=100; 
	public int direction=1;
	
	public int ticksUp=1;
	public int ticksDown=1;
	
	
	private int countFramesWaitNormal=0;
	private int countFramesWaitEmergency=0;
	
	public Bot bot=null; 
	
	private boolean processing=false; 
	
	//trade data
	public double entryOdd=0;
	public double exitOdd=0;
	
	public double stake=0.0;
	
	private long betIdBack=-1;
	private long betIdLay=-1;
	
	private double avgOddBack = -1;
	private double avgOddLay = -1;
	
	private double amountBack = 0;
	private double amountLay = 0;
	
	private double oddToCloseLay=0.;
	private double amountToCloseLay=0.;
	
	private double oddToCloseBack=0.;
	private double amountToCloseBack=0.;
	
	private Vector<OddData> oddDataCloseMatchedVector =new Vector<OddData>(); 
	
	//end tarde data 
	
	public SwingFrontLine(MarketData Market, RunnersData rdA, double stakeSize, double entryOddA,int waitFramesNormalA,int waitFramesEmergencyA, Bot botOwner,int directionA,int ticksUpA,int ticksDownA)
	{
		marketData=Market;
		rd=rdA;
		waitFramesNormal=waitFramesNormalA;
		waitFramesEmergency=waitFramesEmergencyA;
		bot=botOwner;
		
		entryOdd=entryOddA;
		stake=stakeSize;
		
		direction=directionA;
		ticksUp=ticksUpA;
		ticksDown=ticksDownA;
		
		initialize();
	}
	
	public void initialize()
	{
		
		bot.setInTrade(true);
		if(marketData.isInPlay())
		{
			writeMessageText("Did not enter Swing Becouse Market is inplay: exit", Color.RED);
			exit();
		}
		//16899726555
		//16899827848  16900001743
		/*long id=16899827848l;
		Bet b=getBet(id);
		printBet(b);*/
		
		setSTATE(SwingFrontLine.START);
		
		if(!Parameters.simulation)
		{
			as = new SwingThread();
			t = new Thread(as);
			t.start();
		}
		
		//marketData.addMarketChangeListener(this);
	}
	
	
	
	public void updateState()
	{
		if(Parameters.simulation)
		{
			countFramesWaitNormal++;
			double odd=0;
			if (rd != null) {
				
				if(direction<0)
					odd = rd.getDataFrames()
						.get(rd.getDataFrames().size() - 1).getOddBack();
				else
					odd = rd.getDataFrames()
					.get(rd.getDataFrames().size() - 1).getOddLay();
			}
			else
			{
				writeMessageText("ERROR : Runner is null ", Color.RED);
				clean();
				return;
			}
			
			writeMessageText("trading on " + rd.getName() + ": Odd:" + odd,
					Color.BLUE);
			
			if (direction > 0)
			{
				if (odd >= Utils.indexToOdd((Utils.oddToIndex(entryOdd)+ticksUp+1)))
				{
					writeMessageText("GREEN :" + (bot.getGreens() + 1), Color.GREEN);
					bot.setGreens(bot.getGreens() + 1);
					bot.tradeResults(rd, 1, direction, entryOdd, odd, stake,0,0,Math.abs(Utils.oddToIndex(odd)-1-Utils.oddToIndex(entryOdd)));
					//bot.tradeResults(rd, 1, direction, entryOdd, odd, stake, 0);
					this.clean();
					return;
				} else if(odd <= Utils.indexToOdd((Utils.oddToIndex(entryOdd)-ticksDown))) {
					writeMessageText("RED :" + (bot.getReds() + 1), Color.RED);
					bot.setReds(bot.getReds() + 1);
					bot.tradeResults(rd, -1, direction, entryOdd, odd, stake, 0, 0, Math.abs(Utils.oddToIndex(odd)-1-Utils.oddToIndex(entryOdd)));
					//bot.tradeResults(rd, -1, direction, entryOdd, odd, stake, 0);
					this.clean();
					return;
				}
			}
			else
			{
				if (odd <= Utils.indexToOdd((Utils.oddToIndex(entryOdd)-ticksDown-1)))
				{
					writeMessageText("GREEN :" + (bot.getGreens() + 1), Color.GREEN);
					bot.setGreens(bot.getGreens() + 1);
					bot.tradeResults(rd, 1, direction, entryOdd, odd, stake,0,0,Math.abs(Utils.oddToIndex(odd)+1-Utils.oddToIndex(entryOdd)));
					//bot.tradeResults(rd, 1, direction, entryOdd, odd, stake, 0);
					this.clean();
					return;
				} else if(odd >= Utils.indexToOdd((Utils.oddToIndex(entryOdd)+ticksUp))) {
					writeMessageText("RED :" + (bot.getReds() + 1), Color.RED);
					bot.setReds(bot.getReds() + 1);
					bot.tradeResults(rd, -1, direction, entryOdd, odd, stake, 0, 0, Math.abs(Utils.oddToIndex(odd)+1-Utils.oddToIndex(entryOdd)));
					//bot.tradeResults(rd, -1, direction, entryOdd, odd, stake, 0);
					this.clean();
					return;
				}
			}
				
			
	
			if (countFramesWaitNormal > waitFramesNormal) {
				
				if (direction > 0)
				{
					if (odd >entryOdd)
					{
						writeMessageText("GREEN :" + (bot.getGreens() + 1), Color.GREEN);
						bot.setGreens(bot.getGreens() + 1);
						bot.tradeResults(rd, 1, direction, entryOdd, odd, stake,0,0,Math.abs(Utils.oddToIndex(odd)-1-Utils.oddToIndex(entryOdd)));
						//bot.tradeResults(rd, 1, direction, entryOdd, odd, stake, 0);
						this.clean();
						return;
					} else if(odd <entryOdd) {
						writeMessageText("RED :" + (bot.getReds() + 1), Color.RED);
						bot.setReds(bot.getReds() + 1);
						bot.tradeResults(rd, -1, direction, entryOdd, odd, stake, 0, 0, Math.abs(Utils.oddToIndex(odd)+1-Utils.oddToIndex(entryOdd)));
						//bot.tradeResults(rd, -1, direction, entryOdd, odd, stake, 0);
						this.clean();
						return;
					}
				}
				else
				{
					if (odd < entryOdd)
					{
						writeMessageText("GREEN :" + (bot.getGreens() + 1), Color.GREEN);
						bot.setGreens(bot.getGreens() + 1);
						bot.tradeResults(rd, 1, direction, entryOdd, odd, stake,0,0,Math.abs(Utils.oddToIndex(odd)+1-Utils.oddToIndex(entryOdd)));
						//bot.tradeResults(rd, 1, direction, entryOdd, odd, stake, 0);
						this.clean();
						return;
					} 
					else if(odd >entryOdd)
					{
						writeMessageText("RED :" + (bot.getReds() + 1), Color.RED);
						bot.setReds(bot.getReds() + 1);
						bot.tradeResults(rd, -1, direction, entryOdd, odd, stake, 0, 0, Math.abs(Utils.oddToIndex(odd)-1-Utils.oddToIndex(entryOdd)));
						//bot.tradeResults(rd, -1, direction, entryOdd, odd, stake, 0);
						this.clean();
						return;
					}
				
				//clean();
				//return;
				}
			}
		}
		else
		{
			if(processing==false)
			{
				processing=true;
				switch (getSTATE()) {
		            case SwingFrontLine.UNDEFINED:  clean();       break;
		            case SwingFrontLine.START:  openTrade();      break;
		            case SwingFrontLine.WAIT_OPEN:  waitOpenTrade();      break;
		            case SwingFrontLine.WAIT_CLOSE:  waitCloseTrade();      break;
		            case SwingFrontLine.CLOSE: closeTrade();  break;
		            case SwingFrontLine.CLOSE_EMERGENCY:  closeEmergencyTrade(); break;
		            case SwingFrontLine.EDGE: processEdge();  break;
		            case SwingFrontLine.EXIT: exit();      break;
		            default: clean(); break;
				}
				processing=false;
			}
			 
		}
		 
	}
	
	//--------------- Process States -------------------
	
	
	

	private void openTrade()
	{
		writeMessageText("#### OPEN ####", Color.BLACK);
		double actualOddEntry;
		if(direction<0)
			actualOddEntry = getRunnerOddBak();
		else
			actualOddEntry = getRunnerOddLay();
		
		if(this.entryOdd!=actualOddEntry)
		{
			writeMessageText("openTrade():Odd move before open trade: OddEntry" + this.entryOdd+" != ActualOdd"+actualOddEntry, Color.RED);
			setSTATE(SwingFrontLine.EXIT);
			return;
		}
		
		if(direction<0)
		{
			betIdBack=placeBackBet(actualOddEntry,stake);
			if(betIdBack==-1)
			{
				writeMessageText("openTrade():Some error Placing Back:EXIT!!", Color.RED);
				setSTATE(SwingFrontLine.EXIT);
				return;
			}
			else
			{
				if(amountBack>0)
				{
					writeMessageText("openTrade():Some Amount Back Macthed: OK :WAIT_OPEN:Call waitOpenTrade()", Color.GREEN);
					setSTATE(SwingFrontLine.WAIT_OPEN);
					waitOpenTrade();
					return;
				}
				else
				{
					writeMessageText("openTrade(): OK :WAIT_OPEN", Color.GREEN);
					setSTATE(SwingFrontLine.WAIT_OPEN);
					return;
				}
				//writeMessageText("openTrade(): OK :WAIT_OPEN", Color.GREEN);
				//setSTATE(Swing.WAIT_OPEN);
				//return;
			}
		}
		else
		{
			betIdLay=placeLayBet(actualOddEntry,stake);
			if(betIdLay==-1)
			{
				writeMessageText("openTrade():Some error Placing Lay:EXIT!!", Color.RED);
				setSTATE(SwingFrontLine.EXIT);
				return;
			}
			else
			{
				if(amountLay>0)
				{
					writeMessageText("openTrade():Some Amount Lay Macthed: OK :WAIT_OPEN:Call waitOpenTrade()", Color.GREEN);
					setSTATE(SwingFrontLine.WAIT_OPEN);
					waitOpenTrade();
					return;
				}
				else
				{
					writeMessageText("openTrade(): OK :WAIT_OPEN", Color.GREEN);
					setSTATE(SwingFrontLine.WAIT_OPEN);
					return;
				}			
				//writeMessageText("openTrade(): OK :WAIT_OPEN", Color.GREEN);
				//setSTATE(Swing.WAIT_OPEN);
				//return;
			}
		}	
	}
	
	private void waitOpenTrade()
	{
		writeMessageText("#### WAIT OPEN ####", Color.BLACK);
		if(direction<0)
		{
			// predict Odd down
			Bet betBack=getBet(betIdBack);
			if(betBack==null)
			{
				writeMessageText("waitOpenTrade(): could not refresh back bet id="+betIdBack+" : Wait for next frame", Color.RED);
				return;
			}
			
			printBet(betBack);
			
			amountBack=betBack.getMatchedSize();
			avgOddBack=betBack.getAvgPrice();
			
			if(amountBack!=0 )
			{
				if(amountBack<Utils.convertAmountToBF(stake))
				{
					if(cancelBetID(betIdBack)==-1)
					{
						writeMessageText("waitOpenTrade():Warning:Some error canceling bet:"+betIdBack+" : !Continue!", Color.ORANGE);
					}
					betBack=getBet(betIdBack);
					if(betBack==null)
					{
						writeMessageText("waitOpenTrade(): could not refresh back bet id="+betIdBack, Color.RED);
						return;
					}
					printBet(betBack);	
					
					if(betBack.getBetStatus()!=BetStatusEnum.C && (betBack.getBetStatus()==BetStatusEnum.M && betBack.getMatchedSize() != betBack.getRequestedSize()) )
					{
						writeMessageText("waitOpenTrade(): Bet is not Canceled and/or completed Matched id="+betIdBack+"wait for next Frame", Color.RED);
						return;
					}
					
					amountBack=betBack.getMatchedSize();
					avgOddBack=betBack.getAvgPrice();
				}
				
				oddToCloseLay=Utils.indexToOdd(Utils.oddToIndex(entryOdd)-ticksDown);
				amountToCloseLay=Utils.closeAmountLay(avgOddBack, amountBack, oddToCloseLay);
				
				if(Utils.indexToOdd(Utils.oddToIndex(entryOdd)+ticksUp)<=getRunnerOddBak())
				{
					oddToCloseLay=getRunnerOddBak();
					amountToCloseLay=Utils.closeAmountLay(avgOddBack, amountBack, oddToCloseLay);
					writeMessageText("waitOpenTrade():Odd went on UNPredicted direction", Color.RED);
				}
				
				writeMessageText("Placing Lay "+amountToCloseLay+"@"+oddToCloseLay, Color.BLUE);
				betIdLay=placeLayBet(oddToCloseLay,amountToCloseLay);
				
				//*********************************************
				//Bet aux=getBet(betIdLay);
				//if(aux==null)
				//{
				//	writeMessageText("waitOpenTrade(): could not refresh back bet id="+betIdBack, Color.RED);
				//	return;
				//}
				//printBet(aux);
				//*********************************************
				
				if(betIdLay==-1)
				{
					writeMessageText("waitOpenTrade():Some error Placing Lay (close trade):Wait Next Frame!!", Color.RED);
					//setSTATE(Swing.EXIT);
					return;
				}
				else
				{
					writeMessageText("waitOpenTrade():Ok Placing Lay (close trade)id"+betIdLay+":WAIT_CLOSE", Color.GREEN);
					setSTATE(SwingFrontLine.WAIT_CLOSE);
					return;
				}
				
			}
			else
			{
				if(entryOdd>getRunnerOddBak())
				{
					if(cancelBetID(betIdBack)==-1)
					{
						writeMessageText("waitOpenTrade():Warning:2 Some error canceling Back bet:"+betIdBack+ " !!Continue!!", Color.ORANGE);
					}
					betBack=getBet(betIdBack);
					if(betBack==null)
					{
						writeMessageText("waitOpenTrade():2 could not refresh back bet id="+betIdBack+" : wait for next Frame", Color.RED);
						return;
					}
					printBet(betBack);		
					
					if(betBack.getBetStatus()!=BetStatusEnum.C && (betBack.getBetStatus()==BetStatusEnum.M && betBack.getMatchedSize()!= betBack.getRequestedSize()))
					{
						
						writeMessageText("waitOpenTrade():2 Bet is not Canceled or not completed Matched id="+betIdBack+" : wait for next Frame", Color.RED);
						return;
					}
					
					
					amountBack=betBack.getMatchedSize();
					avgOddBack=betBack.getAvgPrice();
					
					if(amountBack==0)
					{
						setSTATE(SwingFrontLine.EXIT);
						return;
					}
					else
					{
						
						oddToCloseLay=Utils.indexToOdd(Utils.oddToIndex(entryOdd)-ticksDown);
						amountToCloseLay=Utils.closeAmountLay(avgOddBack, amountBack, oddToCloseLay);
						
						if(Utils.indexToOdd(Utils.oddToIndex(entryOdd)+ticksUp)<=getRunnerOddBak())
						{
							oddToCloseLay=getRunnerOddBak();
							amountToCloseLay=Utils.closeAmountLay(avgOddBack, amountBack, oddToCloseLay);
							writeMessageText("waitOpenTrade()3:Odd went on UNPredicted direction", Color.RED);
						}
						
						writeMessageText("Placing Lay "+amountToCloseLay+"@"+oddToCloseLay, Color.BLUE);
						betIdLay=placeLayBet(oddToCloseLay,amountToCloseLay);
						
						if(betIdLay==-1)
						{
							writeMessageText("waitOpenTrade()3:Some error Placing Lay (close trade): Wait For Next Frame", Color.RED);
							//setSTATE(Swing.EXIT);
							return;
						}
						else
						{
							writeMessageText("waitOpenTrade()3:Ok Placing Lay (close trade)id"+betIdLay+":WAIT_CLOSE", Color.GREEN);
							setSTATE(SwingFrontLine.WAIT_CLOSE);
							return;
						}
					}
				}
			}
		}
		else
		{
			// predict Odd UP
			Bet betLay=getBet(betIdLay);
			if(betLay==null)
			{
				writeMessageText("waitOpenTrade(): could not refresh Lay bet id="+betIdLay, Color.RED);
				return;
			}
			
			printBet(betLay);
			
			amountLay=betLay.getMatchedSize();
			avgOddLay=betLay.getAvgPrice();
			
			if(amountLay!=0)
			{
				if(amountLay<Utils.convertAmountToBF(stake))
				{
					if(cancelBetID(betIdLay)==-1)
					{
						writeMessageText("waitOpenTrade():Warning:Some error canceling Lay bet:"+betIdLay, Color.ORANGE);
					}
					betLay=getBet(betIdLay);
					if(betLay==null)
					{
						writeMessageText("waitOpenTrade(): could not refresh Lay bet id="+betIdLay, Color.RED);
						return;
					}
					printBet(betLay);
					if(betLay.getBetStatus()!=BetStatusEnum.C && (betLay.getBetStatus()==BetStatusEnum.M && betLay.getMatchedSize()!= betLay.getRequestedSize()))
					{
						writeMessageText("waitOpenTrade(): Bet is not Canceled or not Completed Macthed id="+betIdLay+" : wait for next Frame", Color.RED);
						return;
					}
					amountLay=betLay.getMatchedSize();
					avgOddLay=betLay.getAvgPrice();
				}
				
				oddToCloseBack=Utils.indexToOdd(Utils.oddToIndex(entryOdd)+ticksUp);
				amountToCloseBack=Utils.closeAmountBack(avgOddLay, amountLay, oddToCloseBack);
				
				if(Utils.indexToOdd(Utils.oddToIndex(entryOdd)-ticksDown)>=getRunnerOddLay())
				{
					oddToCloseBack=getRunnerOddLay();
					amountToCloseBack=Utils.closeAmountBack(avgOddLay, amountLay, oddToCloseBack);
					writeMessageText("waitOpenTrade():Odd went on UNPredicted direction", Color.RED);
				}
				
				writeMessageText("Placing Back "+amountToCloseBack+"@"+oddToCloseBack, Color.BLUE);
				betIdBack=placeBackBet(oddToCloseBack,amountToCloseBack);
				
				//*********************************************
				//Bet aux=getBet(betIdBack);
				//if(aux==null)
				//{
				//	writeMessageText("waitOpenTrade(): could not refresh back bet id="+betIdBack, Color.RED);
				//	return;
				//}
				//printBet(aux);
				//*********************************************
				
				if(betIdBack==-1)
				{
					writeMessageText("waitOpenTrade()3:Some error Placing Back (close trade): Wait For Next Frame", Color.RED);
					//setSTATE(Swing.EXIT);
					return;
				}
				else
				{
					writeMessageText("waitOpenTrade()3:Ok Placing Back (close trade)id"+betIdBack+":WAIT_CLOSE", Color.GREEN);
					setSTATE(SwingFrontLine.WAIT_CLOSE);
					return;
				}
				
			}
			else
			{
				if(entryOdd<getRunnerOddLay())
				{
					if(cancelBetID(betIdLay)==-1)
					{
						writeMessageText("waitOpenTrade():Warning:2 Some error canceling bet:"+betIdLay, Color.ORANGE);
					}
					betLay=getBet(betIdLay);
					if(betLay==null)
					{
						writeMessageText("waitOpenTrade():2 could not refresh Lay bet id="+betIdLay, Color.RED);
						return;
					}
					printBet(betLay);		
					
					if(betLay.getBetStatus()!=BetStatusEnum.C && (betLay.getBetStatus()==BetStatusEnum.M && betLay.getMatchedSize()!= betLay.getRequestedSize()) )
					{
						writeMessageText("waitOpenTrade()3: Bet is not Canceled or not completed Matched id="+betIdLay+"wait for next Frame", Color.RED);
						return;
					}
					amountLay=betLay.getMatchedSize();
					avgOddLay=betLay.getAvgPrice();
					
					if(amountLay==0)
					{//lost opportunity
						setSTATE(SwingFrontLine.EXIT);
						return;
					}
					else
					{
						oddToCloseBack=Utils.indexToOdd(Utils.oddToIndex(entryOdd)+ticksUp);
						amountToCloseBack=Utils.closeAmountBack(avgOddLay, amountLay, oddToCloseBack);
						
						if(Utils.indexToOdd(Utils.oddToIndex(entryOdd)-ticksDown)>=getRunnerOddLay())
						{
							oddToCloseBack=getRunnerOddLay();
							amountToCloseBack=Utils.closeAmountBack(avgOddBack, amountBack, oddToCloseBack);
							writeMessageText("waitOpenTrade():Odd went on UNPredicted direction", Color.RED);
						}
						
						writeMessageText("Placing Back "+amountToCloseBack+"@"+oddToCloseBack, Color.BLUE);
						betIdBack=placeBackBet(oddToCloseBack,amountToCloseBack);
						
						if(betIdBack==-1)
						{
							writeMessageText("waitOpenTrade()2:Some error Placing Back (close trade):Wait for Next Frame", Color.RED);
							//setSTATE(Swing.EXIT);
							return;
						}
						else
						{
							writeMessageText("waitOpenTrade()2:Ok Placing Back (close trade)id"+betIdBack+":WAIT_CLOSE", Color.GREEN);
							setSTATE(SwingFrontLine.WAIT_CLOSE);
							return;
						}
					}
				}
			}	
		}

		//setSTATE(Swing.EXIT);
	}
	
	private void waitCloseTrade() {
		writeMessageText("#### WAIT CLOSE ####", Color.BLACK);
		
		if(direction<0)
		{	
			if(Utils.indexToOdd(Utils.oddToIndex(entryOdd)+ticksUp)<=getRunnerOddBak()) // Go after
			{
				writeMessageText("waitCloseTrade(): entryOdd<getRunnerOddBak() ("+entryOdd+"+ticksUp("+ticksUp+") = "+Utils.indexToOdd(Utils.oddToIndex(entryOdd)+ticksUp)+" <="+getRunnerOddBak()+") : CLOSE", Color.RED);
				setSTATE(SwingFrontLine.CLOSE);
				return;
			}
			
			Bet betLay=getBet(betIdLay);
			if(betLay==null)
			{
				writeMessageText("waitCloseTrade(): could not refresh Lay bet id="+betIdLay + " : Wait For Next Frame", Color.RED);
				return;
			}
			printBet(betLay);	
			
			if(Utils.convertAmountToBF(amountToCloseLay) <= betLay.getMatchedSize()) //End Total Matched
			{
				writeMessageText("waitCloseTrade(): Close Lay bet id="+betIdLay+" completed matched: EDGE", Color.GREEN);
				OddData ad=new OddData(betLay.getAvgPrice(), betLay.getMatchedSize());
				oddDataCloseMatchedVector.add(ad);
				setSTATE(SwingFrontLine.EDGE);
				processEdge();
				return;
			}
			
			
			
			countFramesWaitNormal++;
			if (countFramesWaitNormal > waitFramesNormal) {
				writeMessageText("waitCloseTrade(): Wait complete (Frames reach "+countFramesWaitNormal+") : CLOSE", Color.ORANGE);
				setSTATE(SwingFrontLine.CLOSE);
				return;
			}
			writeMessageText("waitCloseTrade(): (Frames "+countFramesWaitNormal+")",Color.BLUE);
		}
		else
		{
			if(Utils.indexToOdd(Utils.oddToIndex(entryOdd)-ticksDown)>=getRunnerOddLay()) // Go after
			{
				writeMessageText("waitCloseTrade(): entryOdd>getRunnerOddLay() ("+entryOdd+"-ticksDown"+"<"+getRunnerOddLay()+") : CLOSE", Color.RED);
				setSTATE(SwingFrontLine.CLOSE);
				return;
			}
			
			Bet betBack=getBet(betIdBack);
			if(betBack==null)
			{
				writeMessageText("waitCloseTrade(): could not refresh Back bet id="+betIdLay + " : Wait For Next Frame", Color.RED);
				return;
			}
			printBet(betBack);	
			
			
			
			if(Utils.convertAmountToBF(amountToCloseBack) <= betBack.getMatchedSize()) //End Total Matched
			{
				writeMessageText("waitCloseTrade(): Close Back bet id="+betIdLay+" completed matched: EDGE", Color.GREEN);
				OddData ad=new OddData(betBack.getAvgPrice(), betBack.getMatchedSize());
				oddDataCloseMatchedVector.add(ad);
				setSTATE(SwingFrontLine.EDGE);
				processEdge();
				return;
			}
			
			countFramesWaitNormal++;
			if (countFramesWaitNormal > waitFramesNormal) {
				writeMessageText("waitCloseTrade(): Wait complete (Frames reach "+countFramesWaitNormal+") : CLOSE", Color.ORANGE);
				setSTATE(SwingFrontLine.CLOSE);
				return;
			}
			writeMessageText("waitCloseTrade(): (Frames "+countFramesWaitNormal+")",Color.BLUE);
		}
		
		
	}
	
	private void closeTrade() {
		writeMessageText("#### CLOSE ####", Color.BLACK);
		
		if(direction<0)
		{
			// predict Odd down
			Bet betLay=getBet(betIdLay);
			if(betLay==null)
			{
				writeMessageText("closeTrade(): could not refresh Lay bet id="+betIdLay + " : Wait For Next Frame", Color.RED);
				return;
			}
			printBet(betLay);	
			
			if(Utils.convertAmountToBF(amountToCloseLay) <= betLay.getMatchedSize()) //End Total Matched
			{
				writeMessageText("closeTrade(): Close Lay bet id="+betIdLay+" completed matched: EDGE", Color.GREEN);
				OddData ad=new OddData(betLay.getAvgPrice(), betLay.getMatchedSize());
				oddDataCloseMatchedVector.add(ad);
				setSTATE(SwingFrontLine.EDGE);
				processEdge();
				return;
			}
			writeMessageText("CloseTrade(): Close Lay bet id="+betIdLay+" Not completed matched ...", Color.ORANGE);
			
			if(betLay.getPrice()<getRunnerOddBak()) // Go after
			{
				writeMessageText("CloseTrade(): betLay.getPrice()<getRunnerOddBak() : Go after...", Color.RED);
				if(cancelBetID(betIdLay)==-1)
				{
					writeMessageText("CloseTrade():Warning:2 Some error canceling Lay bet:"+betIdLay, Color.ORANGE);
				}
				betLay=getBet(betIdLay);
				if(betLay==null)
				{
					writeMessageText("CloseTrade(): Could not refresh Lay bet id="+betIdLay, Color.RED);
					return;
				}
				printBet(betLay);		
				
				if(betLay.getBetStatus()!=BetStatusEnum.C && (betLay.getBetStatus()==BetStatusEnum.M && betLay.getMatchedSize()!= betLay.getRequestedSize()) )
				{
					writeMessageText("CloseTrade(): Bet is not Canceled and not completed Matched id="+betIdLay+"wait for next Frame", Color.RED);
					return;
				}
				
				OddData odaux=new OddData(betLay.getAvgPrice(),betLay.getMatchedSize());
				oddDataCloseMatchedVector.add(odaux);
				
				if(Utils.convertAmountToBF(amountToCloseLay) <= betLay.getMatchedSize())
				{
					setSTATE(SwingFrontLine.EDGE);
					processEdge();
					return;
				}
				else
				{
					
					double pricesArray[]=new double[oddDataCloseMatchedVector.size()];
					double sizetArray[]=new double[oddDataCloseMatchedVector.size()];
					
					double tolalAmount = 0; 
					int i=0;
					
					for ( OddData od:oddDataCloseMatchedVector)
					{
						pricesArray[i]=od.getOdd();
						sizetArray[i]=od.getAmount();
						tolalAmount+=od.getAmount();
						i++;
					}
					
					double oddAVG=Utils.calculateOddAverage(pricesArray, sizetArray);
					
					double desgaste = Utils.closeAmountBack(oddAVG, tolalAmount, avgOddBack);
					
					double amountAux=amountBack-desgaste;
					
					double oddToCloseLayAux=getRunnerOddBak();
					double amountToCloseLayAux=Utils.closeAmountLay(avgOddBack, amountAux, oddToCloseLayAux);
					
					if(Utils.convertAmountToBF(amountToCloseLayAux)<=0.00)
					{
						writeMessageText("CloseTrade(): Lay Bet Amount reach 0.00 : EXIT", Color.RED);
						setSTATE(SwingFrontLine.EXIT);
						return;
					}
					
					writeMessageText("Placing New Lay "+amountToCloseLayAux+"@"+oddToCloseLayAux, Color.BLUE);
					betIdLay=placeLayBet(oddToCloseLayAux,amountToCloseLayAux);
					
					if(betIdLay==-1)
					{
						writeMessageText("CloseTrade():Some error Placing New Lay (close trade): Wait For Next Frame", Color.RED);
						//setSTATE(Swing.EXIT);
						return;
					}
					else
					{
						writeMessageText("CloseTrade():Ok Placing New Lay (close trade)id"+betIdLay+": Continue CLOSE", Color.GREEN);
						
						//Now the bet is placed I can Actualize Global Vars!!!
						oddToCloseLay=oddToCloseLayAux;
						amountToCloseLay=amountToCloseLayAux;
						
						setSTATE(SwingFrontLine.CLOSE);
						return;
					}
				}
			}
			
			countFramesWaitEmergency++;
			if (countFramesWaitEmergency > waitFramesEmergency) {
				writeMessageText("waitCloseTrade(): Wait complete (Frames reach "+countFramesWaitEmergency+") : EMERGENCY", Color.RED);
				setSTATE(SwingFrontLine.CLOSE_EMERGENCY);
				return;
			}
			writeMessageText("CloseTrade(): (Frames to go EMERGENCY : "+countFramesWaitEmergency+")",Color.BLUE);
			
		}
		else
		{
			// predict Odd down
			Bet betBack=getBet(betIdBack);
			if(betBack==null)
			{
				writeMessageText("CloseTrade(): could not refresh Back bet id="+betIdBack + " : Wait For Next Frame", Color.RED);
				return;
			}
			printBet(betBack);	
			
			if(Utils.convertAmountToBF(amountToCloseBack) <= betBack.getMatchedSize()) //End Total Matched
			{
				writeMessageText("CloseTrade(): Close Back bet id="+betIdBack+" completed matched: EDGE", Color.GREEN);
				OddData ad=new OddData(betBack.getAvgPrice(), betBack.getMatchedSize());
				oddDataCloseMatchedVector.add(ad);
				setSTATE(SwingFrontLine.EDGE);
				processEdge();
				return;
			}
			writeMessageText("CloseTrade(): Close Back bet id="+betIdBack+" Not completed matched ...", Color.ORANGE);
			
			if(betBack.getPrice()>getRunnerOddLay()) // Go after
			{
				writeMessageText("CloseTrade(): betBack.getPrice()>getRunnerOddLay() : Go after...", Color.RED);
				if(cancelBetID(betIdBack)==-1)
				{
					writeMessageText("CloseTrade():Warning:2 Some error canceling Back bet:"+betIdBack, Color.ORANGE);
				}
				betBack=getBet(betIdBack);
				if(betBack==null)
				{
					writeMessageText("CloseTrade(): Could not refresh Back bet id="+betIdBack, Color.RED);
					return;
				}
				printBet(betBack);		
				
				if(betBack.getBetStatus()!=BetStatusEnum.C && (betBack.getBetStatus()==BetStatusEnum.M && betBack.getMatchedSize()!= betBack.getRequestedSize()) )
				{
					writeMessageText("CloseTrade(): Back Bet is not Canceled and not completed Matched id="+betIdBack+"wait for next Frame", Color.RED);
					return;
				}
				
				OddData odaux=new OddData(betBack.getAvgPrice(),betBack.getMatchedSize());
				oddDataCloseMatchedVector.add(odaux);
				
				if(Utils.convertAmountToBF(amountToCloseBack) <= betBack.getMatchedSize())
				{
					setSTATE(SwingFrontLine.EDGE);
					processEdge();
					return;
				}
				else
				{
					double pricesArray[]=new double[oddDataCloseMatchedVector.size()];
					double sizetArray[]=new double[oddDataCloseMatchedVector.size()];
					
					double tolalAmount = 0; 
					int i=0;
					
					for ( OddData od:oddDataCloseMatchedVector)
					{
						pricesArray[i]=od.getOdd();
						sizetArray[i]=od.getAmount();
						tolalAmount+=od.getAmount();
						i++;
					}
					
					double oddAVG=Utils.calculateOddAverage(pricesArray, sizetArray);
					
					double desgaste = Utils.closeAmountLay(oddAVG, tolalAmount, avgOddLay);
					
					double amountAux=amountLay-desgaste;
					
					double oddToCloseBackAux=getRunnerOddLay();
					double amountToCloseBackAux=Utils.closeAmountBack(avgOddLay, amountAux, oddToCloseBackAux);
					
					if(Utils.convertAmountToBF(amountToCloseBackAux)<=0.00)
					{
						writeMessageText("CloseTrade(): Back Bet Amount reach 0.00 : EXIT", Color.RED);
						setSTATE(SwingFrontLine.EXIT);
						return;
					}
					
					writeMessageText("Placing New Back "+amountToCloseBackAux+"@"+oddToCloseBackAux, Color.BLUE);
					betIdBack=placeBackBet(oddToCloseBackAux,amountToCloseBackAux);
					
					if(betIdBack==-1)
					{
						writeMessageText("CloseTrade():Some error Placing New Back (close trade): Wait For Next Frame", Color.RED);
						//setSTATE(Swing.EXIT);
						return;
					}
					else
					{
						writeMessageText("CloseTrade():Ok Placing New Back (close trade)id"+betIdBack+": Continue CLOSE", Color.GREEN);
						
						//Now the bet is placed I can Actualize Global Vars!!!
						oddToCloseBack=oddToCloseBackAux;
						amountToCloseBack=amountToCloseBackAux;
						
						setSTATE(SwingFrontLine.CLOSE);
						return;
					}
				}
			}
			
			countFramesWaitEmergency++;
			if (countFramesWaitEmergency > waitFramesEmergency) {
				writeMessageText("waitCloseTrade(): Wait complete (Frames reach "+countFramesWaitEmergency+") : EMERGENCY", Color.RED);
				setSTATE(SwingFrontLine.CLOSE_EMERGENCY);
				return;
			}
			writeMessageText("CloseTrade(): (Frames to go EMERGENCY : "+countFramesWaitEmergency+")",Color.BLUE);	
		}
	}
	
	private void closeEmergencyTrade() {
		
		writeMessageText("#### CLOSE EMERGENCY ####", Color.BLACK);
		
		if(direction<0)
		{
			// predict Odd down
			Bet betLay=getBet(betIdLay);
			if(betLay==null)
			{
				writeMessageText("closeEmergencyTrade(): could not refresh Lay bet id="+betIdLay + " : Wait For Next Frame", Color.RED);
				return;
			}
			printBet(betLay);	
			
			if(Utils.convertAmountToBF(amountToCloseLay) <= betLay.getMatchedSize()) //End Total Matched
			{
				writeMessageText("closeEmergencyTrade(): Close Lay bet id="+betIdLay+" completed matched: EDGE", Color.GREEN);
				OddData ad=new OddData(betLay.getAvgPrice(), betLay.getMatchedSize());
				oddDataCloseMatchedVector.add(ad);
				setSTATE(SwingFrontLine.EDGE);
				processEdge();
				return;
			}
			writeMessageText("closeEmergencyTrade(): Close Lay bet id="+betIdLay+" Not completed matched ...", Color.ORANGE);
			
			if(betLay.getPrice()<getRunnerOddLay()) // Go after
			{
				writeMessageText("closeEmergencyTrade()): betLay.getPrice()<getRunnerOddLay() : Go after...", Color.RED);
				if(cancelBetID(betIdLay)==-1)
				{
					writeMessageText("closeEmergencyTrade():Warning:2 Some error canceling Lay bet:"+betIdLay, Color.ORANGE);
				}
				betLay=getBet(betIdLay);
				if(betLay==null)
				{
					writeMessageText("closeEmergencyTrade(): Could not refresh Lay bet id="+betIdLay, Color.RED);
					return;
				}
				printBet(betLay);		
				
				if(betLay.getBetStatus()!=BetStatusEnum.C && (betLay.getBetStatus()==BetStatusEnum.M && betLay.getMatchedSize()!= betLay.getRequestedSize()) )
				{
					writeMessageText("closeEmergencyTrade(): Bet is not Canceled and not completed Matched id="+betIdLay+"wait for next Frame", Color.RED);
					return;
				}
				
				OddData odaux=new OddData(betLay.getAvgPrice(),betLay.getMatchedSize());
				oddDataCloseMatchedVector.add(odaux);
				
				if(Utils.convertAmountToBF(amountToCloseLay) <= betLay.getMatchedSize())
				{
					setSTATE(SwingFrontLine.EDGE);
					processEdge();
					return;
				}
				else
				{
					double pricesArray[]=new double[oddDataCloseMatchedVector.size()];
					double sizetArray[]=new double[oddDataCloseMatchedVector.size()];
					
					double tolalAmount = 0; 
					int i=0;
					
					for ( OddData od:oddDataCloseMatchedVector)
					{
						pricesArray[i]=od.getOdd();
						sizetArray[i]=od.getAmount();
						tolalAmount+=od.getAmount();
						i++;
					}
					
					double oddAVG=Utils.calculateOddAverage(pricesArray, sizetArray);
					
					double desgaste = Utils.closeAmountBack(oddAVG, tolalAmount, avgOddBack);
					
					double amountAux=amountBack-desgaste;
					
					double oddToCloseLayAux=getRunnerOddLay();
					double amountToCloseLayAux=Utils.closeAmountLay(avgOddBack, amountAux, oddToCloseLayAux);
					
					if(Utils.convertAmountToBF(amountToCloseLayAux)<=0.00)
					{
						writeMessageText("closeEmergencyTrade(): Lay Bet Amount reach 0.00 : EXIT", Color.RED);
						setSTATE(SwingFrontLine.EXIT);
						return;
					}
					
					writeMessageText("Placing New Lay "+amountToCloseLayAux+"@"+oddToCloseLayAux, Color.BLUE);
					betIdLay=placeLayBet(oddToCloseLayAux,amountToCloseLayAux);
					
					if(betIdLay==-1)
					{
						writeMessageText("closeEmergencyTrade():Some error Placing New Lay (close trade): Wait For Next Frame", Color.RED);
						//setSTATE(Swing.EXIT);
						return;
					}
					else
					{
						writeMessageText("closeEmergencyTrade():Ok Placing New Lay (close trade)id"+betIdLay+": Continue CLOSE_EMERGENCY", Color.GREEN);
						
						//Now the bet is placed I can Actualize Global Vars!!!
						oddToCloseLay=oddToCloseLayAux;
						amountToCloseLay=amountToCloseLayAux;
						
						setSTATE(SwingFrontLine.CLOSE_EMERGENCY);
						return;
					}
				}
			}
		}
		else
		{
			// predict Odd UP
			
			
			Bet betBack=getBet(betIdBack);
			if(betBack==null)
			{
				writeMessageText("closeEmergencyTrade()2: could not refresh Back bet id="+betIdBack + " : Wait For Next Frame", Color.RED);
				return;
			}
			printBet(betBack);	
			
			if(Utils.convertAmountToBF(amountToCloseBack) <= betBack.getMatchedSize()) //End Total Matched
			{
				writeMessageText("closeEmergencyTrade()2: Close Back bet id="+betIdBack+" completed matched: EDGE", Color.GREEN);
				OddData ad=new OddData(betBack.getAvgPrice(), betBack.getMatchedSize());
				oddDataCloseMatchedVector.add(ad);
				setSTATE(SwingFrontLine.EDGE);
				processEdge();
				return;
			}
			writeMessageText("CloseTrade(): Close Back bet id="+betIdBack+" Not completed matched ...", Color.ORANGE);
			
			if(betBack.getPrice()>getRunnerOddBak()) // Go after
			{
				writeMessageText("closeEmergencyTrade()2: betBack.getPrice()>getRunnerOddBack() : Go after...", Color.RED);
				if(cancelBetID(betIdBack)==-1)
				{
					writeMessageText("closeEmergencyTrade()2:Warning:2 Some error canceling Back bet:"+betIdBack, Color.ORANGE);
				}
				betBack=getBet(betIdBack);
				if(betBack==null)
				{
					writeMessageText("closeEmergencyTrade()2: Could not refresh Back bet id="+betIdBack, Color.RED);
					return;
				}
				printBet(betBack);		
				
				if(betBack.getBetStatus()!=BetStatusEnum.C && (betBack.getBetStatus()==BetStatusEnum.M && betBack.getMatchedSize()!= betBack.getRequestedSize()) )
				{
					writeMessageText("closeEmergencyTrade()2: Back Bet is not Canceled and not completed Matched id="+betIdBack+"wait for next Frame", Color.RED);
					return;
				}
				
				OddData odaux=new OddData(betBack.getAvgPrice(),betBack.getMatchedSize());
				oddDataCloseMatchedVector.add(odaux);
				
				if(Utils.convertAmountToBF(amountToCloseBack) <= betBack.getMatchedSize())
				{
					setSTATE(SwingFrontLine.EDGE);
					processEdge();
					return;
				}
				else
				{
					double pricesArray[]=new double[oddDataCloseMatchedVector.size()];
					double sizetArray[]=new double[oddDataCloseMatchedVector.size()];
					
					double tolalAmount = 0; 
					int i=0;
					
					for ( OddData od:oddDataCloseMatchedVector)
					{
						pricesArray[i]=od.getOdd();
						sizetArray[i]=od.getAmount();
						tolalAmount+=od.getAmount();
						i++;
					}
					
					double oddAVG=Utils.calculateOddAverage(pricesArray, sizetArray);
					
					double desgaste = Utils.closeAmountLay(oddAVG, tolalAmount, avgOddLay);
					
					double amountAux=amountLay-desgaste;
					
					double oddToCloseBackAux=getRunnerOddBak();
					double amountToCloseBackAux=Utils.closeAmountBack(avgOddLay, amountAux, oddToCloseBackAux);
					
					if(Utils.convertAmountToBF(amountToCloseBackAux)<=0.00)
					{
						writeMessageText("closeEmergencyTrade()2: Back Bet Amount reach 0.00 : EXIT", Color.RED);
						setSTATE(SwingFrontLine.EXIT);
						return;
					}
					
					writeMessageText("Placing New Back "+amountToCloseBackAux+"@"+oddToCloseBackAux, Color.BLUE);
					betIdBack=placeBackBet(oddToCloseBackAux,amountToCloseBackAux);
					
					if(betIdBack==-1)
					{
						writeMessageText("closeEmergencyTrade()2:Some error Placing New Back (close trade): Wait For Next Frame", Color.RED);
						//setSTATE(Swing.EXIT);
						return;
					}
					else
					{
						writeMessageText("closeEmergencyTrade()2:Ok Placing New Back (close trade)id"+betIdBack+": Continue EMERGENCY", Color.GREEN);
						
						//Now the bet is placed I can Actualize Global Vars!!!
						oddToCloseBack=oddToCloseBackAux;
						amountToCloseBack=amountToCloseBackAux;
						
						setSTATE(SwingFrontLine.CLOSE_EMERGENCY);
						return;
					}
				}
			}
		}	
	}
	
	
	public void processEdge(){
		writeMessageText("#### EDGE ####", Color.BLACK);
		
		if(direction<0)
		{
			// predict Odd down
			double pricesArray[]=new double[oddDataCloseMatchedVector.size()];
			double sizetArray[]=new double[oddDataCloseMatchedVector.size()];
			
			double tolalAmount = 0; 
			int i=0;
			
			for ( OddData od:oddDataCloseMatchedVector)
			{
				pricesArray[i]=od.getOdd();
				sizetArray[i]=od.getAmount();
				tolalAmount+=od.getAmount();
				i++;
			}
			
			double oddAVG=Utils.calculateOddAverage(pricesArray, sizetArray);
			
			
			
			double desgaste = Utils.closeAmountBack(oddAVG, tolalAmount, avgOddBack);
			
			double amountAux=amountBack-desgaste;
			
			double oddToCloseLayAux=getRunnerOddLay();
			double amountToCloseLayAux=Utils.closeAmountLay(avgOddBack, amountAux, oddToCloseLayAux);
			
			if(oddAVG<avgOddBack)
			{
				writeMessageText("processEdge(): avgOddLay < avgOddBack ("+oddAVG+"<"+avgOddBack+") : GREEN", Color.GREEN);
				writeMessageText("processEdge(): Profit :" + (tolalAmount+amountToCloseLayAux) +"(Lay) - "+ amountBack+"(Back) = "+ ((tolalAmount+amountToCloseLayAux)-amountBack), Color.GREEN);
				writeMessageText("processEdge(): GREENS :" + (bot.getGreens() + 1), Color.GREEN);
				bot.setGreens(bot.getGreens() + 1);
				bot.setAmountGreen(bot.getAmountGreen() + Utils.convertAmountToBF(((tolalAmount+amountToCloseLayAux)-amountBack)));
				bot.tradeResults(rd, 1, direction, avgOddBack, oddAVG, amountBack, tolalAmount+amountToCloseLayAux ,Utils.convertAmountToBF(((tolalAmount+amountToCloseLayAux)-amountBack)), Math.abs(Utils.oddToIndex(Utils.nearValidOdd(oddAVG))-Utils.oddToIndex(avgOddBack)));
				//bot.tradeResults(rd, 1, direction, avgOddBack, oddAVG, amountBack, Utils.convertAmountToBF(((tolalAmount+amountToCloseLayAux)-amountBack)));
			}
			
			if(oddAVG>avgOddBack)
			{
				writeMessageText("processEdge(): avgOddLay > avgOddBack ("+oddAVG+">"+avgOddBack+"): RED", Color.RED);
				writeMessageText("processEdge(): Loss :" + (tolalAmount+amountToCloseLayAux) +"(Lay) - "+ amountBack+"(Back) = "+ ((tolalAmount+amountToCloseLayAux)-amountBack), Color.RED);
				writeMessageText("processEdge(): REDS :" + (bot.getReds() + 1), Color.RED);
				bot.setReds(bot.getReds() + 1);
				bot.setAmountRed(bot.getAmountRed() + Utils.convertAmountToBF(((tolalAmount+amountToCloseLayAux)-amountBack)));
				bot.tradeResults(rd, -1, direction, avgOddBack, oddAVG, amountBack,tolalAmount+amountToCloseLayAux ,Utils.convertAmountToBF(((tolalAmount+amountToCloseLayAux)-amountBack)), Math.abs(Utils.oddToIndex(Utils.nearValidOdd(oddAVG))-Utils.oddToIndex(avgOddBack)));
				//bot.tradeResults(rd, -1, direction, avgOddBack, oddAVG, amountBack, Utils.convertAmountToBF(((tolalAmount+amountToCloseLayAux)-amountBack)));
			}
			
			if(oddAVG==avgOddBack)
			{
				writeMessageText("processEdge(): avgOddLay == avgOddBack ("+oddAVG+"=="+avgOddBack+"): NO PROFIT OR LOSS", Color.DARK_GRAY);
			}
			
			if(Utils.convertAmountToBF(amountToCloseLayAux)<=0.00)
			{
				writeMessageText("processEdge(): Back Bet Amount reach 0.00 No need to do Edge: EXIT", Color.GREEN);
				setSTATE(SwingFrontLine.EXIT);
				return;
			}
			
			writeMessageText("Placing New Lay "+amountToCloseLayAux+"@"+oddToCloseLayAux, Color.BLUE);
			betIdLay=placeLayBet(oddToCloseLayAux,amountToCloseLayAux);
			
			
			
			if(betIdLay==-1)
			{
				writeMessageText("processEdge():Some error Placing New Lay (close trade): Wait For Next Frame", Color.RED);
				//setSTATE(Swing.EXIT);
				return;
			}
			else
			{
				writeMessageText("processEdge():Ok Placing New Lay (close trade)id"+betIdLay+": Done: EXIT", Color.GREEN);
				//Now the bet is placed I can Actualize Global Vars!!!
				oddToCloseLay=oddToCloseLayAux;
				amountToCloseLay=amountToCloseLayAux;
				setSTATE(SwingFrontLine.EXIT);
				return;
			}
		}
		else
		{
			// predict Odd up
			
			double pricesArray[]=new double[oddDataCloseMatchedVector.size()];
			double sizetArray[]=new double[oddDataCloseMatchedVector.size()];
			
			double tolalAmount = 0; 
			int i=0;
			
			for ( OddData od:oddDataCloseMatchedVector)
			{
				pricesArray[i]=od.getOdd();
				sizetArray[i]=od.getAmount();
				tolalAmount+=od.getAmount();
				i++;
			}
			
			double oddAVG=Utils.calculateOddAverage(pricesArray, sizetArray);
			
						
			double desgaste = Utils.closeAmountLay(oddAVG, tolalAmount, avgOddLay);
			double amountAux=amountLay-desgaste;
			
			double oddToCloseBackAux=getRunnerOddBak();
			double amountToCloseBackAux=Utils.closeAmountBack(avgOddLay, amountAux, oddToCloseBackAux);
			
			if(oddAVG>avgOddLay)
			{
				writeMessageText("processEdge(): avgOddBak > avgOddLay ("+oddAVG+">"+avgOddLay+") : GREEN", Color.GREEN);
				writeMessageText("processEdge(): Profit :" + amountLay +"(Lay) - "+ (tolalAmount+amountToCloseBackAux) +"(Back) = "+ (amountLay-(tolalAmount+amountToCloseBackAux)), Color.GREEN);
				writeMessageText("processEdge(): GREENS :" + (bot.getGreens() + 1), Color.GREEN);
				bot.setGreens(bot.getGreens() + 1);
				bot.setAmountGreen(bot.getAmountGreen() + Utils.convertAmountToBF((amountLay-(tolalAmount+amountToCloseBackAux))));
				
				bot.tradeResults(rd, 1, direction, avgOddLay, oddAVG, amountLay,tolalAmount+amountToCloseBackAux ,Utils.convertAmountToBF((amountLay-(tolalAmount+amountToCloseBackAux))),Math.abs(Utils.oddToIndex(Utils.nearValidOdd(oddAVG))-Utils.oddToIndex(avgOddLay)));
				//bot.tradeResults(rd, 1, direction, avgOddLay, oddAVG, amountLay, Utils.convertAmountToBF((amountLay-(tolalAmount+amountToCloseBackAux))));
			}
			
			if(oddAVG<avgOddLay)
			{
				writeMessageText("processEdge(): avgOddBack < avgOddLay ("+oddAVG+"<"+avgOddLay+"): RED", Color.RED);
				writeMessageText("processEdge(): Loss :" + amountLay +"(Lay) - "+ (tolalAmount+amountToCloseBackAux) +"(Back) = "+ (amountLay-(tolalAmount+amountToCloseBackAux)), Color.RED);
				writeMessageText("processEdge(): REDS :" + (bot.getReds() + 1), Color.RED);
				bot.setReds(bot.getReds() + 1);
				bot.setAmountRed(bot.getAmountRed() + Utils.convertAmountToBF((amountLay-(tolalAmount+amountToCloseBackAux))));
				
				bot.tradeResults(rd, -1, direction, avgOddLay, oddAVG, amountLay, tolalAmount+amountToCloseBackAux ,Utils.convertAmountToBF((amountLay-(tolalAmount+amountToCloseBackAux))),Math.abs(Utils.oddToIndex(Utils.nearValidOdd(oddAVG))-Utils.oddToIndex(avgOddLay)));
				//bot.tradeResults(rd, -1, direction, avgOddLay, oddAVG, amountLay, Utils.convertAmountToBF((amountLay-(tolalAmount+amountToCloseBackAux))));
			}
			
			if(oddAVG==avgOddLay)
			{
				writeMessageText("processEdge(): avgOddLay == avgOddLay ("+oddAVG+"=="+avgOddLay+"): NO PROFIT OR LOSS", Color.DARK_GRAY);
			}
			
			if(Utils.convertAmountToBF(amountToCloseBackAux)<=0.00)
			{
				writeMessageText("processEdge()2: Back Bet Amount reach 0.00 No need to do Edge: EXIT", Color.GREEN);
				setSTATE(SwingFrontLine.EXIT);
				return;
			}
			
			writeMessageText("Placing New Back "+amountToCloseBackAux+"@"+oddToCloseBackAux, Color.BLUE);
			betIdBack=placeBackBet(oddToCloseBackAux,amountToCloseBackAux);
			
			if(betIdBack==-1)
			{
				writeMessageText("processEdge()2:Some error Placing New Back (close trade): Wait For Next Frame", Color.RED);
				//setSTATE(Swing.EXIT);
				return;
			}
			else
			{
				writeMessageText("processEdge()2:Ok Placing New Back (close trade)id"+betIdBack+": Done: EXIT", Color.GREEN);
				
				//Now the bet is placed I can Actualize Global Vars!!!
				oddToCloseBack=oddToCloseBackAux;
				amountToCloseBack=amountToCloseBackAux;
				setSTATE(SwingFrontLine.EXIT);
				return;
			}
		}
		
		
	}
	
	public void exit()
	{
		writeMessageText("#### EXIT ####", Color.BLACK);
		writeMessageText("Exit Swing ", Color.BLUE);
		clean();
	}
	
	
	//--------------- END Process States -------------------
	
	private void printBet(Bet b)
	{
		writeMessageText("------------Print Bet---------",Color.BLACK);
		if(b==null)
		{
			writeMessageText("Print Bet : Bet is NULL"+b.getBetId(),Color.BLACK);
		}
		else
		{
			writeMessageText("Bet Id:"+b.getBetId(),Color.BLACK);
			writeMessageText("Bet state:"+b.getBetStatus(),Color.BLACK);
			writeMessageText("Remaing size:"+b.getRemainingSize(),Color.BLACK);
			writeMessageText("Matched size:"+b.getMatchedSize(),Color.BLACK);
			writeMessageText("Avg Price:"+b.getAvgPrice(),Color.BLACK);
			writeMessageText("Price:"+b.getPrice(),Color.BLACK);
			writeMessageText("Requested Size:"+b.getRequestedSize(),Color.BLACK);
			writeMessageText("Market Name:"+b.getMarketName(),Color.BLACK);
			writeMessageText("Bet Type:"+b.getBetType(),Color.BLACK);
			writeMessageText("Original Price:"+b.getPrice(),Color.BLACK);	
		}
		writeMessageText("-------------------------------------",Color.BLACK);
	}
	
	//--------------- place/update/cancel bets -------------
	private Bet getBet(long id)
	{
		Bet gb=null;
		int attempts = 0;
		while (attempts < 3 && gb == null) {
			try {
				gb =ExchangeAPI.getBet(marketData.getSelectedExchange(), Manager.apiContext,id);
			} catch (Exception e) {
				writeMessageText(e.getMessage(), Color.RED);
				if(e.getMessage().contains(new String("EVENT_SUSPENDED"))  || e.getMessage().contains(new String("BET_IN_PROGRESS")) )
				{
					writeMessageText("ExchangeAPI.getBet Returned NULL: Market is supended | Bet in progress",Color.BLUE);
					attempts--;
				}				
				writeMessageText("ExchangeAPI.getBet Returned NULL:Attempt :"+attempts, Color.RED);
			}
			attempts++;
		}
		if(gb==null)
		{
			writeMessageText("Failed to get Bet: ExchangeAPI.getBet return null ",Color.RED);
			return null;
		}
		
		return gb;
	}
	
	
	private long placeLayBet(double odd,double sizeA)
	{
		Double size=Utils.convertAmountToBF(sizeA);
		writeMessageText("placing Lay Bet:"+size+" @ "+odd, Color.magenta);
		long id=0;
		if(size<2.0)
		{
			long idOrig=placeLayBetAux(1.01, 2.0);
			if(idOrig==-1)
			{
				writeMessageText("ERROR placing Orig Low Back Bet (2.0@1000)", Color.RED);
				return -1;
			}
			
			long idNewSize=changeOrigLowBetSize(idOrig,size,1.01);
			
			if(idNewSize==-1)
			{
				writeMessageText("ERROR changing Size Orig Low Back Bet ("+size+"@1000)", Color.BLUE);
				return -1;
			}
			
			if(cancelBetID(idOrig)==-1)
			{
				writeMessageText("ERROR canceling Low Back Bet ("+idOrig+")", Color.RED);
			}	
			
			id=changeOrigLowBetPrice(idNewSize,odd,size,1.01);
			
			if(id==-1)
			{
				writeMessageText("ERROR changing price Low Bet ("+idNewSize+")", Color.RED);
				return -1;
			}
			
			
		}
		else
		{
			id=placeLayBetAux(odd, size);
		}
		return id;
	}
	
	
	
	private long placeBackBet(double odd,double sizeA)
	{
		
		Double size=Utils.convertAmountToBF(sizeA);
		writeMessageText("placing Back Bet:"+size+" @ "+odd, Color.magenta);
		long id=0;
		if(size<2.0)
		{
			writeMessageText("placing Orig Low Back Bet (2.0@1000)", Color.BLUE);
			long idOrig=placeBackBetAux(1000, 2.0);
			if(idOrig==-1)
			{
				writeMessageText("ERROR placing Orig Low Back Bet (2.0@1000)", Color.RED);
				return -1;
			}
			
			long idNewSize=changeOrigLowBetSize(idOrig,size,1000);
			
			if(idNewSize==-1)
			{
				writeMessageText("ERROR changing Size Orig Low Back Bet ("+size+"@1000)", Color.RED);
				return -1;
			}
			
			if(cancelBetID(idOrig)==-1)
			{
				writeMessageText("ERROR canceling Low Back Bet ("+idOrig+")", Color.RED);
			}
			
			id=changeOrigLowBetPrice(idNewSize,odd,size,1000);
			
			if(id==-1)
			{
				writeMessageText("ERROR changing price Low Bet ("+idNewSize+")", Color.RED);
				return -1;
			}
						
		}
		else
		{
			id=placeBackBetAux(odd, size);
		}
		return id;
	}
	
	
	private long cancelBetID(long betID) {
		
			CancelBets canc = new CancelBets();
			canc.setBetId(betID);
			
			// We can ignore the array here as we only sent in one bet.
			CancelBetsResult betResult=null;
			int attempts = 0;
			while (attempts < 3 && betResult == null) {
				try {
					betResult = ExchangeAPI.cancelBets(marketData.getSelectedExchange(),Manager.apiContext, new CancelBets[] {canc})[0];
				} catch (Exception e) {
					writeMessageText(e.getMessage(), Color.RED);
					if(e.getMessage().contains(new String("EVENT_SUSPENDED"))  || e.getMessage().contains(new String("BET_IN_PROGRESS")) )
					{
						writeMessageText("ExchangeAPI.cancelBets Returned NULL: Market is supended | Bet in progress",Color.BLUE);
						attempts--;
					}				
					writeMessageText("ExchangeAPI.cancelBets Returned NULL: bet not canceled :Attempt :"+attempts, Color.RED);
					e.printStackTrace();
				}
				attempts++;
			}
			
			if(betResult==null)
			{
				writeMessageText("Failed to cancel bet: ExchangeAPI.cancelBets return null ",Color.RED);
				return -1;
			}
			
			if (betResult.getSuccess()) {
				writeMessageText("Bet "+betResult.getBetId()+" cancelled.",Color.BLUE);
			} else {
				writeMessageText("Failed to cancel bet: Problem was: "+betResult.getResultCode(),Color.RED);
				return -1;
			}
			
		return betResult.getBetId();
	}
	
	public long changeOrigLowBetSize(long id,double size,double oldOdd)
	{
		long newSizeLowLayBetId=0;
		writeMessageText("changeOrigLowBetSize() id:"+id+"  Size:"+size,Color.BLUE);
		UpdateBets upd = new UpdateBets(); 
		upd.setBetId(id);
		upd.setOldBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setOldPrice(oldOdd);
		upd.setOldSize(2.0);
		upd.setNewBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setNewPrice(oldOdd);
		upd.setNewSize(Utils.convertAmountToBF(2.0+size));
		
		UpdateBetsResult betResult=null;
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
				writeMessageText("ExchangeAPI.updateBets (Low Orig Update) Attempt :"+attempts, Color.BLUE);
			try {
				betResult = ExchangeAPI.updateBets(marketData.getSelectedExchange(),
						 Manager.apiContext, new UpdateBets[] {upd})[0];
			} catch (Exception e) {
				writeMessageText(e.getMessage(), Color.RED);
				if(e.getMessage().contains(new String("EVENT_SUSPENDED"))  || e.getMessage().contains(new String("BET_IN_PROGRESS")) )
				{
					writeMessageText("ExchangeAPI.updateBets Returned NULL: Market is supended | Bet in progress",Color.BLUE);
					attempts--;
				}				
				writeMessageText("ExchangeAPI.updateBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
				e.printStackTrace();
			}
			attempts++;
		}
		
		if(betResult==null)
		{
			writeMessageText("ExchangeAPI.updateBets Returned NULL: No bets placed", Color.RED);
			return -1;
		}
		
		if (betResult.getSuccess()) {
			writeMessageText("Bet "+betResult.getBetId()+" New Bet ID:"+betResult.getNewBetId() +" updated. New bet is "+betResult.getNewSize() +" @ "+betResult.getNewPrice(),Color.BLUE);
			newSizeLowLayBetId=betResult.getNewBetId();
		} else {
			writeMessageText("changeOrigLowBetSize() - Failed to update bet: Problem was: "+betResult.getResultCode(),Color.RED);
			return -1;
		}
		
		return newSizeLowLayBetId;
	}
	
	public long changeOrigLowBetPrice(long id,double price,double size,double holdOdd)
	{
		long newPriceLowLayBetId=-1;
		
		writeMessageText("changeOrigLowBet id:"+id+"  Size:"+size+" hold Odd:"+holdOdd,Color.BLUE);
		UpdateBets upd = new UpdateBets(); 
		upd.setBetId(id);
		upd.setOldBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setOldPrice(holdOdd);
		upd.setOldSize(size);
		upd.setNewBetPersistenceType(BetPersistenceTypeEnum.NONE);
		upd.setNewPrice(price);
		upd.setNewSize(size);
		
		UpdateBetsResult betResult=null;
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
				writeMessageText("ExchangeAPI.updateBets (Low Orig Update Price) Attempt :"+attempts, Color.BLUE);
			try {
				betResult = ExchangeAPI.updateBets(marketData.getSelectedExchange(),
						 Manager.apiContext, new UpdateBets[] {upd})[0];
			} catch (Exception e) {
				writeMessageText(e.getMessage(), Color.RED);
				if( e.getMessage().contains(new String("EVENT_SUSPENDED")) || e.getMessage().contains(new String("BET_IN_PROGRESS")) )
				{
					writeMessageText("ExchangeAPI.placeBets Returned NULL: Market is supended",Color.BLUE);
					attempts--;
				}
				// TODO Auto-generated catch block
				writeMessageText("ExchangeAPI.updateBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (betResult.getSuccess()) {
			writeMessageText("Bet "+betResult.getBetId()+" New Bet ID:"+betResult.getNewBetId() +" updated. New bet is "+betResult.getNewSize() +" @ "+betResult.getNewPrice(),Color.BLUE);
			 newPriceLowLayBetId=betResult.getNewBetId();
		} else {
			writeMessageText("changeOrigLowLayBetPrice() - Failed to update bet: Problem was: "+betResult.getResultCode(),Color.RED);
			return -1;
		}
		
		return newPriceLowLayBetId;
	}
	
	private long placeBackBetAux(double odd,double size)
	{
		long id=0;
		PlaceBets bet=createPlaceBackBet(odd,size);
		PlaceBets[] bets=new PlaceBets[1];
		bets[0]=bet;
		PlaceBetsResult[] betResult=null;
		
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
			writeMessageText("ExchangeAPI.placeBets(Back) Attempt :"+attempts, Color.BLUE);
			try {
				betResult=ExchangeAPI.placeBets(marketData.getSelectedExchange(),  Manager.apiContext, bets);
			} catch (Exception e) {
				writeMessageText(e.getMessage(), Color.RED);
				if(e.getMessage().contains(new String("EVENT_SUSPENDED")))
				{
					writeMessageText("ExchangeAPI.placeBets Returned NULL: Market is supended | Bet in progress",Color.BLUE);
					attempts--;
				}
				e.printStackTrace();
				writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
			}
			attempts++;
		}
		
		if(betResult==null)
		{
			writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed", Color.RED);
			return -1;
		}
		else
		{
			if(betResult.length!=1)
			{
				writeMessageText("ExchangeAPI.placeBets Returned !=1 lenght: Debug Bet!!", Color.RED);
				return -1;
			}
			
			if (betResult[0].getSuccess()) {
				writeMessageText("Bet Id:" + betResult[0].getBetId()
							+ " placed("+size+"@"+odd+") : Matched " + betResult[0].getSizeMatched()
							+ "@"
							+ betResult[0].getAveragePriceMatched(),Color.GREEN);
				
				if( betResult[0].getSizeMatched()>0)
				{
					avgOddBack = betResult[0].getAveragePriceMatched();
					amountBack = betResult[0].getSizeMatched();
				}
				
				id=betResult[0].getBetId();
			} else{
			
				writeMessageText("Failed to place bet: Problem was: "
							+ betResult[0].getResultCode(),Color.RED);
				return -1;
			}
		}
		return id;
	}
	
	public PlaceBets createPlaceBackBet(double odd,double size)
	{
		writeMessageText("Create Back PLACE BETS:"+size+"@"+odd, Color.BLUE);
		PlaceBets bet = new PlaceBets();
		bet.setMarketId(marketData.getSelectedMarket().getMarketId());
		bet.setSelectionId(rd.getId());
		bet.setBetCategoryType(BetCategoryTypeEnum.E);
		bet.setBetPersistenceType(BetPersistenceTypeEnum.NONE);
		bet.setBetType(BetTypeEnum.Factory.fromValue("B"));
		bet.setPrice(odd);
		bet.setSize(size);
		return bet;
	}
	
	
	private long placeLayBetAux(double odd,double size)
	{
		long id=0;
		PlaceBets bet=createPlaceLayBet(odd,size);
		PlaceBets[] bets=new PlaceBets[1];
		bets[0]=bet;
		PlaceBetsResult[] betResult=null;
		
		int attempts = 0;
		while (attempts < 3 && betResult == null) {
			writeMessageText("ExchangeAPI.placeBets(Lay) Attempt :"+attempts, Color.BLUE);
			try {
				betResult=ExchangeAPI.placeBets(marketData.getSelectedExchange(),  Manager.apiContext, bets);
			} catch (Exception e) {
				writeMessageText(e.getMessage(), Color.RED);
				if(e.getMessage().contains(new String("EVENT_SUSPENDED")))
				{
					writeMessageText("ExchangeAPI.placeBets Returned NULL: Market is supended | Bet in progress",Color.BLUE);
					attempts--;
				}
				e.printStackTrace();
				writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed:Attempt :"+attempts, Color.RED);
			}
			attempts++;
		}
		
		if(betResult==null)
		{
			writeMessageText("ExchangeAPI.placeBets Returned NULL: No bets placed", Color.RED);
			return -1;
		}
		else
		{
			if(betResult.length!=1)
			{
				writeMessageText("ExchangeAPI.placeBets Returned !=1 lenght: Debug Bet!!", Color.RED);
				return -1;
			}
			
			if (betResult[0].getSuccess()) {
				writeMessageText("Bet Id:" + betResult[0].getBetId()
							+ " placed("+size+"@"+odd+") : Matched " + betResult[0].getSizeMatched()
							+ " matched @ "
							+ betResult[0].getAveragePriceMatched(),Color.GREEN);
				if( betResult[0].getSizeMatched()>0)
				{
					avgOddLay = betResult[0].getAveragePriceMatched();
					amountLay = betResult[0].getSizeMatched();
				}
				id=betResult[0].getBetId();
			} else{
			
				writeMessageText("Failed to place bet: Problem was: "
							+ betResult[0].getResultCode(),Color.RED);
				return -1;
			}
		}
		return id;
	}
	public PlaceBets createPlaceLayBet(double odd,double size)
	{
		writeMessageText("Create Lay PLACE BETS:"+size+"@"+odd, Color.BLUE);
		PlaceBets bet = new PlaceBets();
		bet.setMarketId(marketData.getSelectedMarket().getMarketId());
		bet.setSelectionId(rd.getId());
		bet.setBetCategoryType(BetCategoryTypeEnum.E);
		bet.setBetPersistenceType(BetPersistenceTypeEnum.NONE);
		bet.setBetType(BetTypeEnum.Factory.fromValue("L"));
		bet.setPrice(odd);
		bet.setSize(size);
		return bet;
	}
	
	//--------------- place/update/cancel bets END ------------- 
	
	private double getRunnerOddBak()
	{
		return rd.getDataFrames()
		.get(rd.getDataFrames().size() - 1).getOddBack();
	}
	
	private double getRunnerOddLay()
	{
		return rd.getDataFrames()
		.get(rd.getDataFrames().size() - 1).getOddLay();
	}
	
	public int getRunner_ID() {
		return rd.getId();
	}
	
	
	
	public int getSTATE() {
		return STATE;
	}

	private void setSTATE(int sTATE) {
		STATE = sTATE;
	}
	
	private void writeMessageText(String s,Color c)
	{
		if(bot!=null)
			bot.writeMsg(s, c);
		//System.out.println(s);
	}
	
	public void clean()
	{
		setSTATE(SwingFrontLine.END);
		//marketData.removeMarketChangeListener(this);
		if(!Parameters.simulation)
		{
			as.stopRequest();
			t=null;
		}
		if (bot!=null)
			bot.setInTrade(false);
		bot=null;
		marketData=null;
		if(oddDataCloseMatchedVector!=null)
			oddDataCloseMatchedVector.clear();
		oddDataCloseMatchedVector=null;
	}
	
	 private SwingThread as;
	 private volatile Thread t;
	 
	 public class SwingThread extends Object implements Runnable {
		 private volatile boolean stopRequested;
		 private Thread runThread;

		 public void run() {
			 runThread = Thread.currentThread();
			 stopRequested = false;

			 while (!stopRequested) {
				 updateState();
				 try {
					 Thread.sleep(updateInterval);
				 }
				 catch (Exception e) {
					 // e.printStackTrace();
				 }
			 }
		 }

        public void stopRequest() {
           	stopRequested = true;
            
            if (runThread != null) {
            	runThread.interrupt();
            }
        }
	 }
 
	 public static void main(String[] args)  throws Exception 
	 {
		 long x=-1;
		 if(x==-1)
			 System.out.println("reconheço -1");		 
	 }
	 
	 @Override
		public void forceClose() {
			countFramesWaitNormal=waitFramesNormal;
			countFramesWaitEmergency=waitFramesEmergency;
			
		}

		@Override
		public int getState() {
			return STATE;
		}
	
		@Override
		public void writeMsgTM(String s, Color c) {
			writeMessageText(s, c);
			
		}
}
