package org.kevoree.brain.test;


import org.kevoree.brain.learning.KmeansLearning;
import org.kevoree.brain.util.StatLibrary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by assaa_000 on 8/12/2014.
 */
public class TestKmeans {
    public static void main (String[] args){
        KmeansLearning gkl = new KmeansLearning(2,100);
        String csvFile = "D:\\workspace\\Github\\kevoree-brain\\org.kevoree.brain.learning\\src\\main\\resources\\test.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<Double[]> x=new ArrayList<Double[]>();
        ArrayList<Integer> y= new ArrayList<Integer>();

        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] values = line.split(cvsSplitBy);

                Double[] px= new Double[1];
                px[0]= Double.parseDouble(values[0]);
                Integer py= Integer.parseInt(values[1]);
                x.add(px);
                y.add(py);
            }

        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }

        for(int i=0;i<(int)(x.size()*0.9);i++){
            gkl.addTrainingSet(x.get(i),y.get(i));
        }

        ArrayList<Object[]> xTest= new ArrayList<Object[]>();
        ArrayList<Integer> yTest= new ArrayList<Integer>();

        for(int i=(int)(x.size()*0.9);i<x.size();i++){
            xTest.add(x.get(i));
            yTest.add(y.get(i));
        }



        long startTime = System.nanoTime();
        for(int i=0; i<1;i++)
            try {
                gkl.train();
            } catch (Exception e) {
                System.out.println("Exception in train "+e.getMessage());
                e.printStackTrace();
            }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        //gkl.testAccuracy();
        System.out.println("Duration: "+(double)duration / 1000000000.0+" seconds");





        gkl.print();
        StatLibrary.testClassifier(xTest,yTest,gkl);

        for(int temp=0; temp<700; temp++){
            Object[] tst = new Object[1];
            tst[0]=new Double(temp);
            int result = gkl.evaluate(tst);
            if(result==0)
                System.out.print(temp + " , ");
        }
        System.out.println();








    }
}
