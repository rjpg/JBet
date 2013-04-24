package DataRepository;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BFChart extends JPanel{

	int marketId=0;
	int runnerId=0;
	RunnersData rd=null;
	BufferedImage img=null;
	String imgURL=null;

	public BFChart(RunnersData rdA) {
		this(rdA.getMarketData().getId(),rdA.getId());
		
		rd=rdA;
	
		
	}

	public BFChart(int mId, int rId) {
		super();

		

		marketId=mId;
		runnerId=rId;
		
		imgURL="http://uk.sportsiteexweb.betfair.com/betting/LoadRunnerInfoChartAction.do?marketId="+marketId+"&selectionId="+runnerId;
		
		try {
			refresh();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	public void refresh() throws IOException{
		img = ImageIO.read(new URL(imgURL));
	}

	protected void paintComponent(Graphics g){
		Graphics g2 = g.create();
		g2.drawImage(img, (getWidth()-img.getWidth())/2, (getHeight()-img.getHeight())/2, img.getWidth(), img.getHeight(), null);
		g2.dispose();
	}

	public Dimension getPreferredSize(){
		return new Dimension(img.getWidth(), img.getHeight());
	}

	public static void main(String[] args)
	{

		String imgURL="http://uk.sportsiteexweb.betfair.com/betting/LoadRunnerInfoChartAction.do?marketId=109151826&selectionId=6520441";

		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(new BFChart(109151826,6520441));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

}
