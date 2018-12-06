package NeuralNetWork;

import java.io.*;
import java.util.*;

public class TestBench {
    private static final int MAXIMUM_EPOCHS = 5000;
    private static final double CONVERGENCE_ERROR = 0.1;
    private static final int NUM_INPUTS = 4;
    private static final int NUM_HIDDEN_NEURONS = 100;
    private static final int NUM_OUTPUTS = 5;

    private static final double MIN_VAL = -0.5;
    private static final double MAX_VAL = 0.5;
    private static final double MOMENTUM = 0.2;
    private static final double LEARNING_RATE = 0.0001;

    // LUT file and properties
    private static final String LUT_FILE_NAME = "Action1.csv";
    private static File mLutFile;

    private static double[][][][][] LUTContent = new double[8][3][8][2][5];
    private static List<List<List<Double>>> totalTrainingSet = new ArrayList<>();
    private static List<List<Double>> trainingSet;
    private static List<Double> inputs;
    private static List<Double> outputs;
    private static NeurualNetWork NNObject;

    public static void main(String[] args) {
        //1.load
        //2.setTrainingSet
        //3.
        //NeurualNetWork NNObject;
        ArrayList<Double> results = new ArrayList<>();

        mLutFile = new File(LUT_FILE_NAME);
        loadLut(mLutFile);
        setTrainingSet();
        System.out.printf("LUT file has %d entires\n", totalTrainingSet.size());

        NNObject = new NeurualNetWork(NUM_INPUTS, NUM_HIDDEN_NEURONS, NUM_OUTPUTS, LEARNING_RATE, MOMENTUM, MIN_VAL, MAX_VAL);
        runTrials(NNObject, totalTrainingSet, CONVERGENCE_ERROR, MAXIMUM_EPOCHS, results);
        try {
            printTrialResults(results, "convergence.csv");
            List<List<List<Double>>> weightLog = NNObject.getWeights();
            System.out.println("Finished");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printTrialResults(ArrayList<Double> results, String fileName) throws IOException {
        int epoch;

        PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
        printWriter.printf("Epoch, Total Squared Error,\n");

        for(epoch = 0; epoch < results.size(); epoch++)
        {
            printWriter.printf("%d, %f,\n", epoch, results.get(epoch));
        }

        printWriter.flush();
        printWriter.close();
    }

    private static void runTrials(NeurualNetWork nnObject, List<List<List<Double>>> totalTrainingSet,  double convergenceError, int maxEpochs, ArrayList<Double> results)
    {
        System.out.println("Run Trials");
        results.clear();
        // Initialize weights for a new training session
        nnObject.initializeWeights();
        List<List<List<Double>>> weights = nnObject.getWeights();
        nnObject.setWeights(weights);
        // Attempt convergence
        attemptConvergence(
                nnObject, totalTrainingSet, convergenceError, maxEpochs, results);

    }

    private static void attemptConvergence(NeurualNetWork nnObject, List<List<List<Double>>> trainingSet, double convergenceError, int maxEpochs, ArrayList<Double> results) {
        double cummError, setCummError;
        int index, epoch, output;

        double[] errors;

        System.out.println("attemptConvergence");

        for (epoch = 0; epoch < maxEpochs; epoch++)
        {
            cummError = 0.0;
            for (index = 0; index < trainingSet.size(); index++)
            {
                setCummError = 0.0;
                errors = nnObject.train(trainingSet.get(index));
                for (output = 0; output < errors.length; output++)
                {
                    setCummError += errors[output] * errors[output];
                }
                setCummError /= 5;
                cummError += setCummError;
            }

            // RMS error
            cummError /= trainingSet.size();
            cummError = Math.sqrt(cummError);

            // Append the result to our list
            results.add(cummError);

            if (cummError < convergenceError)
            {
                break;
            }
        }
    }


    private static void setTrainingSet(){

        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        double d1, d2, d3, d4;
        for(int s1 = 0; s1 < 8; s1++){
            for(int s2 = 0; s2 < 3; s2++){
                for(int s3 = 0; s3 < 8; s3++){
                    for(int s4 = 0; s4 < 2; s4++){
                        inputs = new ArrayList<>();
                        outputs = new ArrayList<>();
                        trainingSet = new ArrayList<>();
                        d1 = -1+s1*0.125;
                        d2 = -1+s2;
                        d3 = -1+s3*0.125;
                        d4 = s4;
                        inputs.add(d1);
                        inputs.add(d2);
                        inputs.add(d3);
                        inputs.add(d4);
                        for(int a = 0; a < 5; a++){
                            outputs.add(customSigmoid(LUTContent[s1][s2][s3][s4][a]));
                        }
                        trainingSet.add(inputs);
                        trainingSet.add(outputs);
                        totalTrainingSet.add(trainingSet);
                    }
                }
            }
        }
    }

    private static void loadLut(File LutFile)
    {
        try {
            FileInputStream fileInputStream;
            fileInputStream = new FileInputStream(LutFile);
            Scanner inputScanner = new Scanner(fileInputStream);
            for(int s1 = 0; s1 < 8; s1++){
                for(int s2 = 0; s2 < 3; s2++){
                    for(int s3 = 0; s3 < 8; s3++){
                        for(int s4 = 0; s4 < 2; s4++){
                            for(int a = 0; a < 5; a++){
                                LUTContent[s1][s2][s3][s4][a] = inputScanner.nextDouble();
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static double customSigmoid(double x) {
        return ((1.0)/(1.1+Math.exp(-x)));
    }

}
