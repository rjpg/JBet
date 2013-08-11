package bet365;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import DataRepository.Utils;

public class OddSelector extends JPanel{
	JLabel ladderLabels[];

	double bet365Ladder[];
	OddConverter moc;
	
	public OddSelector(double bet365LadderA[],OddConverter mocA) {
		bet365Ladder=bet365LadderA;
		moc=mocA;
		initialize();
	}
	
	
	public void initialize()
	{
		
		JPanel oddsPanel=new JPanel();
		oddsPanel.setLayout(new GridLayout(bet365Ladder.length,1));
		
		oddsPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY.darker()));
		
		ladderLabels=new JLabel[bet365Ladder.length];
		    
		for(int i=0;i<bet365Ladder.length;i++)
		{
			final int x=i;
			ladderLabels[i]=new JLabel(""+bet365Ladder[i]);
			ladderLabels[i].setHorizontalAlignment(JLabel.CENTER );
			ladderLabels[i].setBorder(BorderFactory.createLoweredBevelBorder());
			oddsPanel.add(ladderLabels[i]);
			ladderLabels[i].addMouseListener(new MouseListener() {
				
				@Override
				public void mouseClicked(MouseEvent e) {
					
					OddSelector.this.warnMOConvertorNewOdd(bet365Ladder[x]);
					OddSelector.this.deselect();
					OddSelector.this.ladderLabels[x].setBackground(Color.BLUE.brighter().brighter());
				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
		}
		
		JScrollPane jScrollPane = new JScrollPane();
		jScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		jScrollPane.setViewportView(oddsPanel);
		this.setLayout(new BorderLayout());
		this.add(jScrollPane,BorderLayout.CENTER);
	}
	
	public void deselect()
	{
		for(int i=0;i<bet365Ladder.length;i++)
		{
			ladderLabels[i].setBackground(Color.LIGHT_GRAY);
		}
	}
	
	public void warnMOConvertorNewOdd(double odd)
	{
		moc.newOdd(this, odd);
	}
	
}
