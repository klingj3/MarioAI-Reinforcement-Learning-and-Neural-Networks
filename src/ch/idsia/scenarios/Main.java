package ch.idsia.scenarios;

import ch.idsia.agents.controllers.QLearningAgent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.MarioCustomSystemOfValues;
import ch.idsia.tools.CmdLineOptions;

import java.io.*;
import java.io.FileWriter;
import java.util.Scanner;

import java.util.ArrayList;
import java.util.HashMap;

// Comment below refers to just the basic structure of this file, all content as well as referenced QLearningAgent was done
// by John Klingelhofer
/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey at idsia dot ch Date: Mar 17, 2010 Time: 8:28:00 AM
 * Package: ch.idsia.scenarios
 */
public final class Main {

    public static void write(double[] a, String filename) {
        try {
            Writer wr = new FileWriter(filename);
            for (int i = 0; i < a.length; i++) {
                wr.write(Double.toString(a[i]) + " ");
            }
            wr.close();
        } catch (IOException e) {

        }
    }

    private static int nb2i(boolean b){
        if (b)
            return 0;
        return 1;
    }

    public static double[] fileTodouble(String filename) {
        double[] ret = new double[1500];
        try {
            int i = 0;
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextDouble() && i < ret.length) {
                double temp = scanner.nextDouble();
                ret[i++] = temp;
            }
        } catch (IOException e) {

        }
        return ret;
    }

    public static void main(String[] args) {

        int generations = 500;

        //Establishing junk.
        final String argsString = "-vis on -fps 100 -tl 200 -ld 0 -ag ch.idsia.agents.controllers.QLearningAgent";
        final CmdLineOptions cmdLineOptions = new CmdLineOptions(argsString);
        final BasicTask basicTask = new BasicTask(cmdLineOptions);

        cmdLineOptions.setVisualization(true);

        //MODIFY THESE FOR EXPERIMENTAITON
        int numSeeds = 1;
        double epsilon = 0.3;
        double learningRate = 0.1;
        double discountFactor = 0.6;
        boolean visualizeOccasionally = true;
        String learningType = "QLEARNING"; //Must be SARSA or QLEARNING
        //END OF MODIFYABLE!

        QLearningAgent agent = new QLearningAgent(learningType);
        agent.setEpsilon(epsilon);
        agent.setLearning(learningRate);
        agent.setDiscount(discountFactor);
        cmdLineOptions.setAgent(agent);
        basicTask.reset(cmdLineOptions);

        HashMap<Integer, ArrayList<Double>> hm;

        Integer seeds[] = new Integer[numSeeds];
        for (int i = 0; i < numSeeds; i++){
            if (numSeeds == 1)
                seeds[i] = 6;
            else
                seeds[i] = (int)(Math.random()*Integer.MAX_VALUE);
        }

        float generationAverages[] = new float[generations];
        float cumulativeAverage = 0;
        float maximumToDate = 0;

        for (int i = 0; i < generations; ++i) {
            generationAverages[i] = 0;
            for (int s = 0; s < numSeeds; s++) {
                cmdLineOptions.setLevelRandSeed(seeds[s]);
                if (visualizeOccasionally && (i+1)%100 == 0){
                    cmdLineOptions.setVisualization(true);
                }
                else{
                    cmdLineOptions.setVisualization(false);
                }
                basicTask.reset(cmdLineOptions);
                basicTask.runOneEpisode();
                float tempVal = basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness();
                generationAverages[i] += tempVal;
            }
            hm = agent.getHashMap();
            agent = new QLearningAgent(learningType, hm, learningRate, discountFactor, epsilon);
            cmdLineOptions.setAgent(agent);
            generationAverages[i] = generationAverages[i]/numSeeds;
            cumulativeAverage += generationAverages[i];
            maximumToDate = Math.max(maximumToDate, generationAverages[i]);
            if (i < 30)
                System.out.println(i + "\t" + generationAverages[i] + "\t" + cumulativeAverage/(i+1) + "\t" + cumulativeAverage/(i+1)+ "\t" + maximumToDate);
            else{
                float temp = 0;
                for (int k = i-30; k<i; k++){
                    temp += generationAverages[k];
                }
                System.out.println(i + "\t" + generationAverages[i] + "\t" + temp/(30) + "\t" + cumulativeAverage/(i+1) + "\t" + maximumToDate);
            }
        }

//        write(bestEver, "best.txt");


        //System.out.println("Average fitness is " + average / totalCount + "\n");
        System.exit(0);

    }
}