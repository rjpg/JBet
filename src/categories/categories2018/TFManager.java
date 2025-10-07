package categories.categories2018;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;

import categories.categories2013.CategoryNode;
import categories.categories2018.cattree.Liquidity;
import categories.categories2018.cattree.Root2018;

public class TFManager {

	
	public static Session TFsessions[]=null;
	
	public static void initialize()
	{
		
		
		Root2018 root=new Root2018(0);
		
		
		System.out.println("Cat max ID : "+root.getIdEnd());	
		//root.getAncestorsById(cat, id)

		TFsessions=new Session[root.getIdEnd()];
		
		for(int i=0;i<root.getIdEnd();i++)
		{
			System.out.print("id="+i+" ");
			Vector<CategoryNode> cnv=CategoryNode.getAncestorsById(root,i);
			System.out.println(Root2018.getAncestorsStringPath(cnv));
		
			System.out.println("Loading model");
			TFsessions[i]=loadNN(cnv);
			}
			System.out.println();
	}
		
	public static Session getModel(int id)
	{
		return TFsessions[id];
	}
	
	public static Session loadNN(Vector<CategoryNode> cat)
	{
		

		String catPath= CategoryNode.getAncestorsStringPath(cat)+"model_gru";
		File[] directories = new File(catPath).listFiles(File::isDirectory);
		
		if(directories==null)
		{
			System.out.println("Tensoflow model does not exists on : "+catPath);
			return null;
		}
		
		
		//catPath+="/saved_model.pbtxt";
		SavedModelBundle bundle= SavedModelBundle.load(catPath,"serve");
		Session TFsession = bundle.session();
		//File file = new File("/path/to/directory");
	/*	String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(File current, String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		System.out.println(Arrays.toString(directories));*/
		return TFsession;
	}
		

		
		//System.out.println("Bulinding directories in file system");
		//CategoryNode.buildDirectories(root);
	
	public static void main(String[] args) {
		initialize();
	}
	
	
}
