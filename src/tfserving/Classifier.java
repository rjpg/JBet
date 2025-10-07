package tfserving;


import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class Classifier {
    public static List<Prediction> processPredictions(JSONArray ar) {
        List<Prediction> predictions = new ArrayList<>();
        double min = -735.5000000000016;
        double max = 769.2999999999961;

        for (int i = 0; i < ar.length(); i++) {
            double originalValue = ar.getDouble(i);
            
            //originalValue*=1.6; // disperse 10%
            
            // Denormalizing the value
            double denormalizedValue = ((originalValue + 1) * (max - min) / 2) + min;
            
            denormalizedValue *= 3.0; 
            
            // Classifying the value
            int classifiedValue = classifyValue(denormalizedValue);
            
            // Create a new Prediction object and add it to the list
            Prediction prediction = new Prediction(originalValue, denormalizedValue, classifiedValue);
            predictions.add(prediction);
        }
        
        return predictions;
    }

    // Classification logic
    private static int classifyValue(double value) {
        if (value < -266.90000000000236) {
            return 0;
        } else if (value > -266.90000000000236 && value < -75.5000000000025) {
            return 1;
        } else if (value > -75.5000000000025 && value < 86.19999999999732) {
            return 2;
        } else if (value > 86.19999999999732 && value < 287.4999999999968) {
            return 3;
        } else if (value > 287.4999999999968) {
            return 4;
        }
        return -1; // default case, shouldn't occur if ranges are correct
    }
    
}
