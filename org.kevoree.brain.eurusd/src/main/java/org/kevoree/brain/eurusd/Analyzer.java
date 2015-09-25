package org.kevoree.brain.eurusd;

import org.kevoree.brain.util.TimeStamp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.TreeMap;

/**
 * Created by assaad on 06/02/15.
 */
public class Analyzer {


    public static Profiler load(TreeMap<Long, Double> eurUsd){
        Profiler profiler = new Profiler();
        long starttime;
        long endtime;
        double res;

        starttime = System.nanoTime();
        String csvFile = "/Users/assaad/work/github/eurusd/newEurUsd.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";


        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator 2000.05.30,17:35
                String[] values = line.split(cvsSplitBy);
                Long timestamp = Long.parseLong(values[0]);
                Double val = Double.parseDouble(values[1]);
                profiler.feed(val);
                eurUsd.put(timestamp, val);
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        endtime = System.nanoTime();
        res = ((double) (endtime - starttime)) / (1000000000);
        System.out.println("Loaded :" + eurUsd.size() + " values in " + res + " s!");
        System.out.println("Min: " + profiler.getMin() + " Max: " + profiler.getMax() + " Avg: " + profiler.getAverage());



        return profiler;

    }


    public static double getPerc(Profiler profiler, double[] acchist, double val){
        return acchist[profiler.position(val)];
    }


    public static double getVal(Profiler profiler, double[] acchist, double percent){
        for(int i=0; i< profiler.getMaxInt();i++){
            if(acchist[i]>=percent){
                return profiler.value(i);
            }
        }
        return -1;
    }


    public static void calc(boolean print, long initTimeStamp,long finalTimeStamp, int degradeFactor, Profiler profiler, TreeMap<Long, Double> eurUsd, double eurval, double moneyeur, double moneydol){
        int[] histogram = new int[profiler.getMaxInt()];
        int counter=0;
        for(long i=initTimeStamp; i<finalTimeStamp;i+=degradeFactor){
            //double val = pt.fastReconstruct(i);
            double val=eurUsd.get(eurUsd.floorKey(i));
            histogram[profiler.position(val)]++;
            counter++;
        }


        int max=0;
        int acc=0;
        //System.out.println("Range: " + profiler.getMaxInt());
        for (int i = 0; i < profiler.getMaxInt(); i++) {
            if(histogram[i]>max){
                max=histogram[i];
            }
            acc+=histogram[i];
        }

        int aggregations=0;

        double [] hist=new double[profiler.getMaxInt()];
        double [] acchist=new double[profiler.getMaxInt()];


        try {
            if(print) {
                FileWriter outFile = new FileWriter("result.csv");
                PrintWriter out = new PrintWriter(outFile);
                out.println("eur,hist,acc");
                for (int i = 0; i < profiler.getMaxInt(); i++) {
                    aggregations += histogram[i];
                    hist[i] = ((double) (histogram[i] * 100)) / max;
                    acchist[i] = ((double) (aggregations * 100)) / acc;
                    out.println(String.format("%.4f", profiler.value(i)) + "," + String.format("%.6f", hist[i]) + "," + String.format("%.6f", acchist[i]));
                }
                out.close();
            }
            else{
                for (int i = 0; i < profiler.getMaxInt(); i++) {
                    aggregations += histogram[i];
                    hist[i] = ((double) (histogram[i] * 100)) / max;
                    acchist[i] = ((double) (aggregations * 100)) / acc;
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }


        System.out.println("At 00%: " +String.format( "%.4f", getVal(profiler, acchist, 0.00001)));
        System.out.println("At 25%: "+String.format("%.4f", getVal(profiler, acchist, 25)));
        System.out.println("At 50%: "+String.format("%.4f", getVal(profiler, acchist, 50)));
        System.out.println("At 75%: "+String.format("%.4f", getVal(profiler, acchist, 75)));
        System.out.println("At100%: "+String.format("%.4f", getVal(profiler, acchist, 100)));

        double perc=getPerc(profiler, acchist, eurval);
        System.out.println("Euro level at "+eurval+" is: "+String.format( "%.2f",perc)+" %");



        double tot=moneyeur*eurval+moneydol;
        double neweur = (100-perc)*tot/(100*eurval);
        double newdol=tot-neweur*eurval;

        if(neweur>moneyeur) {
            System.out.println("[BUY] "+String.format("%.4f",neweur-moneyeur)+" eur");
        }
        else
        {
            System.out.println("[SELL] "+String.format("%.4f",moneyeur-neweur)+" eur");
        }

        System.out.println("Equilibre eur: "+String.format("%.4f",neweur));
        System.out.println("Equilibre dol: "+String.format("%.4f",newdol));

    }

    public static void main(String[] args) {

        int degradeFactor = 60000;

        TreeMap<Long, Double> eurUsd = new TreeMap<Long, Double>();
        Profiler profiler =load(eurUsd);


        Long initTimeStamp = TimeStamp.getTimeStamp(2000, 5, 30, 17, 27);
        Long finalTimeStamp = eurUsd.floorKey(TimeStamp.getTimeStamp(2050, 1, 1, 1, 1));

        double eurval=eurUsd.get(finalTimeStamp);
        System.out.println("last value: "+eurval);
        double moneyeur=3806.68;
        double moneydol=17151;

        System.out.println();
        System.out.println("From 2000:");
        calc(false, initTimeStamp, finalTimeStamp, degradeFactor, profiler, eurUsd, eurval, moneyeur, moneydol);


        System.out.println();
        System.out.println("From 2010:");
        initTimeStamp = TimeStamp.getTimeStamp(2010, 1, 1, 00, 0);
        finalTimeStamp = TimeStamp.getTimeStamp(2015, 01, 31, 23, 59);
        calc(false,initTimeStamp,finalTimeStamp,degradeFactor, profiler,eurUsd,eurval,moneyeur,moneydol);
        System.out.println("Done!");


    }



}
