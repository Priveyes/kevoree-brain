import com.ritolaaudio.simplewavio.Utils;
import org.kevoree.brain.util.PolynomialCompressor;
import org.kevoree.brain.util.Prioritization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by duke on 8/21/14.
 */
public class HelloWave {

    public static void main(String[] args) throws IOException {
        double maxerr = 0;

        double temp = 0;

        int timeOrigine = 0;
        int degradeFactor = 100;
        double toleratedError = 0.4;
        int maxDegree = 10;
        PolynomialCompressor pt = new PolynomialCompressor(timeOrigine, degradeFactor, toleratedError, maxDegree);
        pt.setContinous(true);
        pt.setPrioritization(Prioritization.LOWDEGREES);
        float[][] inputAudio = new float[0][];
        try {
            inputAudio = Utils.WAVToFloats(new File("D:\\workspace\\Github\\kevoree-brain\\wave\\Mogo.wav"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int sizetoRead = inputAudio.length;
        int time = 0;
        for (int i = 0; i < sizetoRead; i++) {
            float[] frame = inputAudio[i];
            if(frame.length > 1){
                frame[1] = 0;
            }
            pt.feed(time, frame[0]);
            time++;
        }//end for(frames)
        pt.finalsave();

        float[][] cloned = new float[sizetoRead][inputAudio[0].length];
        float[][] original = new float[sizetoRead][inputAudio[0].length];
        float[][] error = new float[sizetoRead][inputAudio[0].length];
        time = 0;
        int ind = 0;



        for (int i = 0; i < sizetoRead; i++) {
            float[] frame = inputAudio[i];
            float[] errfile= new float[inputAudio[i].length];

            for(int j=0; j<frame.length;j++){
                original[i][j] = frame[j];
            }


            if(frame.length > 1){
                frame[1] = 0;
            }
            try {
                if (ind < pt.origins.size() -1) {
                    if (time >= pt.origins.get(ind + 1)) {
                        ind++;
                    }
                }
                double h = PolynomialCompressor.reconstruct(time, pt.origins.get(ind), pt.w.get(ind), degradeFactor);
                double err = Math.abs(h - frame[0]);
                if (err > maxerr) {
                    maxerr = err;
                }
                temp = temp + err;

                frame[0] = new Float(h);
                errfile[0]=new Float(err);

                cloned[i] = frame;
                error[i]=errfile;
            } catch (Exception e) {
                e.printStackTrace();
            }
            time++;
        }
        Utils.floatsToWAV(cloned, new File("output.wav"), 44100);
        Utils.floatsToWAV(original, new File("original.wav"), 44100);
        Utils.floatsToWAV(error, new File("error.wav"), 44100);
        FileWriter outFile2;
        try {
            outFile2 = new FileWriter("polynome.txt");
            PrintWriter out2 = new PrintWriter(outFile2);
            int total = 0;

            for (int i = 0; i < pt.origins.size(); i++) {
                double[] dd = pt.w.get(i);
                total += dd.length;
                for (double d : dd) {
                    out2.write(d+"");
                }
                out2.println();
            }
            out2.close();
            System.out.println("Read "+sizetoRead+" time variables");
            System.out.println("Number of polynomials: " + pt.w.size());

            System.out.println("Number of double: " + total + " Disk compression: " + ((double) (sizetoRead - total) * 100) / (sizetoRead) + " %");
            System.out.println("Average degrees of the polynomials: " + ((double) total / pt.w.size() - 1));

            temp = temp / sizetoRead;
            System.out.println("Maximum error " + maxerr);
            System.out.println("Average error " + temp);
        } catch (IOException ex) {
        }


    }

}
