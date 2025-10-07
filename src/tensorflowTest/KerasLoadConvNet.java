package tensorflowTest;


import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;


public class KerasLoadConvNet {

	public static void main(String[] args) throws Exception {
		SavedModelBundle bundle=SavedModelBundle.load("tfModels/model_keras","serve");
		Session s = bundle.session();
		
		double[][][][] inputDouble = {{{{0.37671986791414125,0.28395908337619136,-0.0966095873607713,-1.0,0.06891621389763203,-0.09716678086712205,0.726029084013637},
			{4.984689881073479E-4,-0.30296253267499107,-0.16192917054985334,0.04820256230479658,0.4951319883569152,0.5269983894210499,-0.2560313828048315},
			{-0.3710980821053321,-0.4845867212612598,-0.8647234314469595,-0.6491591208322198,-1.0,-0.5004549422844073,-0.9880910165770813},
			{0.5540293108747256,0.5625990251930839,0.7420121698556554,0.5445551415657979,0.4644276850235627,0.7316976292340245,0.636690006814346},
			{0.16486621649984112,-0.0466018967678159,0.5261100063227044,0.6256168612312738,-0.544295484930702,0.379125782517193,0.6959368575211544}}}};
		
		//double[] inputDouble = {1.0,0.7982741870963959,1.0,-0.46270838239235024,0.040320274521029376,0.443451913224413,-1.0,1.0,1.0,-1.0,0.36689718911339564,-0.13577379160035796,-0.5162916256414466,-0.03373651520104648,1.0,1.0,1.0,1.0,0.786999801054777,-0.43856035121103853,-0.8199093927945158,1.0,-1.0,-1.0,-0.1134921695894473,-1.0,0.6420892436196663,0.7871737734493178,1.0,0.6501788845358409,1.0,1.0,1.0,-0.17586627413625022,0.8817194210401085};
		float [][][][] inputfloat=new float[1][1][5][7];
		for(int i=0;i<5;i++)
		{
			for(int x=0;x<7;x++)
				inputfloat[0][0][i][x]=(float)inputDouble[0][0][i][x];
		}
		
		
//		TensorProto 
		
		//Tensor inputTensor = Tensor.create(new long[] {35}, FloatBuffer.wrap(inputfloat) );
		//float[][] data= new float[1][35];
		//data[0]=inputfloat;
		Tensor inputTensor=Tensor.create(inputfloat);
		
		Tensor no_learning = Tensor.create(Boolean.FALSE);
		
		Tensor result = s.runner()
	            .feed("conv2d_1_input", inputTensor)
	            //.feed("dropout_1/keras_learning_phase", no_learning)
	            .fetch("activation_5/Softmax")
	            .run().get(0);
		
		
		 float[][] m = new float[1][5];
         float[][] vector = (float[][])result.copyTo(m);
         float maxVal = 0;
         int inc = 0;
         int predict = -1;
         for(float val : vector[0]) 
         {
        	 System.out.println(val+"  ");
        	 if(val > maxVal) {
        		 predict = inc;
        		 maxVal = val;
        	 }
        	 inc++;
         }
         System.out.println(predict);
	}
}
