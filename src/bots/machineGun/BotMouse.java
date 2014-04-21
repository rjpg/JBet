package bots.machineGun;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import DataRepository.MarketChangeListener;
import DataRepository.MarketData;

public class BotMouse implements MarketChangeListener,NativeKeyListener{
    public static final int DELAY = 50000; 
	     
    public static boolean pressed=false;
    
    public static int x;
    public static int y;
	    
    public static Robot robot2;
    
    public MarketData md=null;
    
    public JFrame frame=null;
    
    public JCheckBox checkBox=new JCheckBox("verify market Activation");
	    
    public BotMouse() {
    	 try {
             GlobalScreen.registerNativeHook();
     }
     catch (NativeHookException ex) {
             System.err.println("There was a problem registering the native hook.");
             System.err.println(ex.getMessage());

             System.exit(1);
     }

     //Construct the example object and initialze native hook.
     GlobalScreen.getInstance().addNativeKeyListener(this);
     
    	frame = new JFrame();
    	frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    	
    	JButton blockMouseButton=new JButton("block Mouse");
    	blockMouseButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BotMouse.this.x=MouseInfo.getPointerInfo().getLocation().x;
				BotMouse.this.y=MouseInfo.getPointerInfo().getLocation().y;
				BotMouse.this.pressed=!BotMouse.this.pressed;
			}
		});
		
    	
    	frame.setLayout(new BorderLayout());
    	frame.add(blockMouseButton,BorderLayout.CENTER);
    	checkBox.setSelected(true);
    	frame.add(checkBox,BorderLayout.SOUTH);
    	frame.setSize(400, 100);
    	frame.setLocation(0,500);
    	frame.setVisible(true);
    	
    	
   	 try {
			robot2 = new Robot();
			robot2.mouseMove(10,10);
			
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	
   	 Thread t  = new Thread()
     {
         public void run() {
        	 for(;;){ 
        	       
                 
             	if(pressed)
             	{
             		 robot2.mouseMove(x,y);
             		 robot2.mousePress( InputEvent.BUTTON1_MASK );
             		 robot2.mouseRelease( InputEvent.BUTTON1_MASK );
             		 try {
     					Thread.sleep(50);
     				} catch (InterruptedException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				}
             		
             	}
             	else
             	{
             		try {
     					Thread.sleep(1000);
     				} catch (InterruptedException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				}
             	}
                
            }
         }
     };
   	 
     t.start();
       
	}
    
    public void setMarket(MarketData mda)
    {
    	md=mda;
    	md.addMarketChangeListener(this);
    	frame.setTitle(mda.getEventName()+" "+mda.getName());
    }
    
    private void startPress()
    {
    	if(checkBox.isSelected())
	    	if(md!=null)
	    	{
	    		md.setUpdateInterval(200);
	    		md.startPolling();
	    	}
    	pressed=true;
    }
    
    private void stopPress()
    {
    	if(md!=null)
    	{
    		md.stopPolling();
    	}
    	pressed=false;
    }
    
	@Override
	public void MarketChange(MarketData md, int marketEventType) {
		
		System.out.println("Market Update State = "+md.getState());
		if(marketEventType==MarketChangeListener.MarketUpdate)
		{
			if(md.getState()==MarketData.ACTIVE)
			{
				System.out.println("Market is active Stop Press");
				stopPress();
			}
		}
		
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

        if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
        	System.out.println("Key Pressed: Escape (STOP)");
               stopPress();
        }
        
        if (e.getKeyCode() == NativeKeyEvent.VK_F8) {
        	
        	      	
        	this.x=MouseInfo.getPointerInfo().getLocation().x;
			this.y=MouseInfo.getPointerInfo().getLocation().y;
        	System.out.println("Key Pressed: F8 (start)");
             startPress();
        }
		
		
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
