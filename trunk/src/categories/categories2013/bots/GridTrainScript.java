package categories.categories2013.bots;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import categories.categories2013.CategoryNode;
import categories.categories2013.Root;

public class GridTrainScript {

	public static void main(String[] args) {
		
		Root root=new Root(0);
		
		File fcmds=new File("trainScripts/cmds.sh");
		String ss="#!/bin/sh\ncd trainCmd";
		
		BufferedWriter outs=null;
		
		
		
		try {
			outs = new BufferedWriter(new FileWriter(fcmds, true));
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error Open "+fcmds+" for writing");
			}
		if(outs==null)
		{
			System.err.println("could not open "+fcmds);
			return;
		}
		
		try {
			outs.write(ss);
			outs.newLine();
			outs.flush();
		} catch (IOException e) {
			System.out.println("Error wrtting data to file - "+fcmds);
			e.printStackTrace();
		}
		
		for(int i=0;i<648;i++)
		{
			
			Vector<CategoryNode> cat=CategoryNode.getAncestorsById(root,i);
			String fileName=CategoryNode.getAncestorsStringPath(cat)+"nn-train-data.egb";
			
			
			
			File file = new File(fileName);
			if(file.exists()) {
				
				File fcmd=new File("trainScripts/train-"+i+".sh");
				String s="#!/bin/sh\n";
				s+="cd train\n";
				s+="/usr/java/jdk1.7.0_21/bin/java -jar -Xms1024m trainSave.jar "+i+"\n";
				
				BufferedWriter out=null;
				
				try {
					out = new BufferedWriter(new FileWriter(fcmd, true));
					} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Error Open "+fcmd+" for writing");
					}
				if(out==null)
				{
					System.err.println("could not open "+fcmd);
					return;
				}
				
				try {
					out.write(s);
					out.newLine();
					out.flush();
				} catch (IOException e) {
					System.out.println("Error wrtting data to file - "+fileName);
					e.printStackTrace();
				}
				
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				String cmd="qsub -o train"+i+".o -e train"+i+".e train-"+i+".sh";
				
				try {
					outs.write(cmd);
					outs.newLine();
					outs.flush();
				} catch (IOException e) {
					System.out.println("Error wrtting data to file - "+fileName);
					e.printStackTrace();
				}
				
				System.out.println("qsub train-"+i+".sh");
			}
			else
			{
				//System.out.println("File Not found in "+fileName);
			}
			
		}
		
		try {
			outs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
