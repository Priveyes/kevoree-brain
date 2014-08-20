package org.kevoree.brain.learning.livelearning;

import org.kevoree.brain.api.classifier.LiveLearning;

import java.util.Random;

/**
 * Created by Ace Shooting on 8/19/2014.
 */
public class LinearRegressionLive implements LiveLearning {

    private int featuresize;
    private double[] weights; //weigths to learn
    private double cont1=1;
    private double const2=2000;
    private int iteration=100;
    private double alpha = cont1/const2;//learning rate

    private int counter=0;
    private Random random = new Random();



    @Override
    public void initialize(Object[] params) {
        featuresize= (Integer) params[0];
        weights=new double[featuresize+1];
        for(int i=0; i<featuresize+1;i++){
            weights[i]=random.nextDouble();
        }
    }

    @Override
    public void feed(double[] features, double result) {
        for(int j=0; j<iteration;j++) {
            double h = calculate(features);
            double err = -alpha * (h - result);
            for (int i = 0; i < featuresize; i++) {
                weights[i] = weights[i] + err * features[i];
            }
            weights[featuresize] = weights[featuresize] + err;
        }
        counter++;
     //  alpha = cont1/(counter+const2);

    }

    @Override
    public double calculate(double[] feature) {
        double result=0;
        for(int i=0;i<feature.length;i++){
            result+=weights[i]*feature[i];
        }
        result+=weights[featuresize];
        return result;
    }

    @Override
    public void reset() {

    }

    @Override
    public void print() {
        for(double d: weights){System.out.println(d);}
    }

}