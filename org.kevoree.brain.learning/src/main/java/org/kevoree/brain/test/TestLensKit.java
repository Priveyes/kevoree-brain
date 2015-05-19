package org.kevoree.brain.test;

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer;
import org.grouplens.lenskit.baseline.UserMeanBaseline;
import org.grouplens.lenskit.baseline.UserMeanItemScorer;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.SimpleFileRatingDAO;
import org.grouplens.lenskit.knn.item.ItemItemScorer;
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.kevoree.brain.learning.livelearning.Recommender.LearningVector;
import org.kevoree.brain.learning.livelearning.Recommender.Rating;
import org.kevoree.brain.learning.livelearning.Recommender.Recommender;
import org.kevoree.brain.learning.livelearning.Recommender.User;
import org.kevoree.brain.util.Histogram;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;

/**
 * Created by assaad on 19/05/15.
 */
public class TestLensKit {


    public static Recommender getRec(){
        LearningVector.setParameters(0.0001, 0.001, 10, 50);

        String dir="/Users/assaad/work/github/kevoree-brain/org.kevoree.brain.learning/src/main/resources/Movielens/";

        String csvfile="movies.csv";
        String line = "";
        String cvsSplitBy = ",";

        Recommender recommender=new Recommender();
        long starttime;
        long endtime;
        double result;



        starttime= System.nanoTime();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dir + csvfile));
            while ((line = br.readLine()) != null) {

                String[] vals = line.split(cvsSplitBy);
                recommender.addProduct(vals[0],vals[1]);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        endtime= System.nanoTime();
        result= ((double)(endtime-starttime))/(1000000000);
        System.out.println("Loaded: "+recommender.getProducts().size()+" movies in "+result+" s");



        // int total=21063128;
        int total=1000209;

        csvfile="ratings.csv";
        starttime= System.nanoTime();
        int counter=0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(dir + csvfile));
            while ((line = br.readLine()) != null) {
                String[] vals = line.split(cvsSplitBy);
                recommender.addRating(vals[0], vals[1], Double.parseDouble(vals[2]), Long.parseLong(vals[3]), false);
                counter++;
                if(counter%(total/20)==0){
                    System.out.println(new DecimalFormat("##.##").format(((double) (counter * 100)) / total) + "%");
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        endtime= System.nanoTime();
        result= ((double)(endtime-starttime))/(1000000000);
        recommender.displayStats();
        System.out.println("Model created in "+result+" s");
        //recommender.getAverageError();
        return recommender;
    }


    public static boolean test=true;

    public static double testErr(Recommender kevoree, LenskitRecommender lens){
        double avg=0;
        double variance=0;
        int count=0;
        double err;
        // ArrayList<Double> errors = new ArrayList<Double>(ratingCounter);
        double[] errors= new double[kevoree.getRatingCounter()];

        int i=0;

        RatingPredictor pred = lens.getRatingPredictor();

        for(String k: kevoree.getUsers().keySet()) {
            User user = kevoree.getUsers().get(k);
            for(String prod: user.getRatings().keySet()){
                Rating rating= user.getRatings().get(prod);
                err=pred.predict(Long.parseLong(k),Long.parseLong(prod))-rating.getValue();
                if(test){
                    test=false;
                    System.out.println("predicted: "+ err+rating.getValue());
                }
                errors[i] =err;
                i++;
                avg+=Math.abs(err);
                variance+=err*err;
                count++;
            }
        }
        if(count!=0){
            avg=avg/count;
            variance=Math.sqrt(variance/count-avg*avg);
        }
        //System.out.println(count);
        Histogram.calcHistogramArray(errors, errors,errors,1000, "lenskit.csv");

        System.out.println("Average error: "+avg);
        System.out.println("STD: "+variance);
        return avg;
    }

    public static void main(String[] args) {
        LenskitConfiguration config = new LenskitConfiguration();

        // Use item-item CF to score items
        config.bind(ItemScorer.class).to(ItemItemScorer.class);

        // let's use personalized mean rating as the baseline/fallback predictor.
        // 2-step process:
        // First, use the user mean rating as the baseline scorer
        config.bind(BaselineScorer.class, ItemScorer.class).to(UserMeanItemScorer.class);

        // Second, use the item mean rating as the base for user means
        config.bind(UserMeanBaseline.class, ItemScorer.class).to(ItemMeanRatingItemScorer.class);

        // and normalize ratings by baseline prior to computing similarities
        config.bind(UserVectorNormalizer.class).to(BaselineSubtractingUserVectorNormalizer.class);

        config.bind(EventDAO.class).to(new SimpleFileRatingDAO(new File("/Users/assaad/work/github/kevoree-brain/org.kevoree.brain.learning/src/main/resources/Movielens/ratings.csv"), ","));

        long starttime;
        long endtime;
        double result;

        try {
            starttime= System.nanoTime();
            LenskitRecommender rec = LenskitRecommender.build(config);
            endtime= System.nanoTime();
            result= ((double)(endtime-starttime))/(1000000000);
            System.out.println("Trained in: "+result+" s");

            Recommender kevoree= getRec();
            testErr(kevoree,rec);

        } catch (RecommenderBuildException e) {
            e.printStackTrace();
        }



    }
}
