package GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import main.Parameters;


import DataRepository.RunnersData;

public class RunnerButton extends JButton{
	
	public RunnersData runnerData;
	public RunnerFrame runnerFrame;
	
	public RunnerButton(RunnersData rdA) {
		super(rdA.getName()+":"+rdA.getDataFrames().get(rdA.getDataFrames().size()-1).getOddBack());
		runnerData=rdA;
		
		this.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				runnerFrame.setVisible(true);
			}
		});
		runnerFrame=new RunnerFrame(runnerData);
	}

	public RunnerFrame getRunnerFrame() {
		return runnerFrame;
	}

	public RunnersData getRunnerData() {
		return runnerData;
	}
	
	public void setRunnerData(RunnersData runnerData) {
		this.runnerData = runnerData;
		
	}
	
	
	public void update()
	{
		this.setText(runnerData.getName()+":"+runnerData.getDataFrames().get(runnerData.getDataFrames().size()-1).getOddBack());
		runnerFrame.update();
	}
	
	
	public void clean()
	{
		//if(Parameters.log)
		if(runnerFrame!=null)
		{
			runnerFrame.dispose();
			runnerFrame.clean();
			runnerFrame=null;
		}
		if(runnerData!=null)
		{
			
			//runnerData.clean();
			runnerData=null;
		}
	}

	
}
