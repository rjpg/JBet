package tensorflowTest;


import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;




public class HelloTF {
	public static void main(String[] args) throws Exception {
		SavedModelBundle bundle=SavedModelBundle.load("tfModels/dnn/ModelSave","serve");
		Session s = bundle.session();
		
		double[] inputDouble = {1.0,0.7982741870963959,1.0,-0.46270838239235024,0.040320274521029376,0.443451913224413,-1.0,1.0,1.0,-1.0,0.36689718911339564,-0.13577379160035796,-0.5162916256414466,-0.03373651520104648,1.0,1.0,1.0,1.0,0.786999801054777,-0.43856035121103853,-0.8199093927945158,1.0,-1.0,-1.0,-0.1134921695894473,-1.0,0.6420892436196663,0.7871737734493178,1.0,0.6501788845358409,1.0,1.0,1.0,-0.17586627413625022,0.8817194210401085};
		float [] inputfloat=new float[inputDouble.length];
		for(int i=0;i<inputfloat.length;i++)
		{
			inputfloat[i]=(float)inputDouble[i];
		}
		
		
//		TensorProto 
		
		//Tensor inputTensor = Tensor.create(new long[] {35}, FloatBuffer.wrap(inputfloat) );
		float[][] data= new float[1][35];
		data[0]=inputfloat;
		Tensor inputTensor=Tensor.create(data);
//		TensorProto.newBuilder();
		
		
		Tensor result = s.runner()
				.feed("dnn/input_from_feature_columns/input_from_feature_columns/concat", inputTensor)
				//.feed("input_example_tensor", inputTensor)
	            //.fetch("tensorflow/serving/classify")
	            .fetch("dnn/multi_class_head/predictions/probabilities")
				//.fetch("dnn/zero_fraction_3/Cast")
	            .run().get(0);
	
		
		 float[][] m = new float[1][5];
         float[][] vector = result.copyTo(m);
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