package tfserving;



public class Prediction {
    
    private double original;
    private double denormalized;
    private int classified;

    // Constructor
    public Prediction(double original, double denormalized, int classified) {
        this.original = original;
        this.denormalized = denormalized;
        this.classified = classified;
    }

    // Getters and Setters
    public double getOriginal() {
        return original;
    }

    public void setOriginal(double original) {
        this.original = original;
    }

    public double getDenormalized() {
        return denormalized;
    }

    public void setDenormalized(double denormalized) {
        this.denormalized = denormalized;
    }

    public int getClassified() {
        return classified;
    }

    public void setClassified(int classified) {
        this.classified = classified;
    }

    @Override
    public String toString() {
        return "Prediction{" +
                "original=" + original +
                ", denormalized=" + denormalized +
                ", classified=" + classified +
                '}';
    }

}
