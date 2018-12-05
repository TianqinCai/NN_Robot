package xor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class NeurualNetWork implements NeuralNetInterface {

    // Constants
    private static final int MAX_HIDDEN_NEURONS = 100;
    private static final int MAX_INPUT_NEURONS = 20;
    private static final int MAX_OUTPUT_NEURONS = 20;

    private static final int TRAINING_SET_STATE_INDEX = 0;
    private static final int TRAINING_SET_ACTION_INDEX = 1;

    // only have one single hidden layer
    private static final int WEIGHTS_INPUT_HIDDEN_INDEX = 0;
    private static final int WEIGTHS_HIDDEN_OUTPUT_INDEX = 1;

    // Neural Network Parameters
    private int mNumInputs;
    private int mNumHidden;
    private int mNumOutputs;
    // private int argNumLayers;

    // Learning Rate and Momentum term
    private double mLearningRate;
    private double mMomentumTerm;

    // array to store input values to the neural network, including bias term
    private double[] mInputValues = new double[MAX_INPUT_NEURONS];

    // array to store input weights to the neurons of the hidden layer
    private static double[][] mInputHiddenWeights = new double[MAX_INPUT_NEURONS][MAX_HIDDEN_NEURONS];
    // array to store previous weights
    private static double[][] mPreviousInputHiddenWeights = new double[MAX_INPUT_NEURONS][MAX_HIDDEN_NEURONS];
    // array to store neuron values of the hidden layer
    private double[] mHiddenNeuronValues = new double[MAX_HIDDEN_NEURONS];
    // array to store neuron error of the hidden layer
    private double[] mHiddenNeuronErrors = new double[MAX_HIDDEN_NEURONS];

    // array to store the weights from hidden layer to output layer
    private static double[][] mHiddenOutputWeights = new double[MAX_HIDDEN_NEURONS][MAX_OUTPUT_NEURONS];
    // array to store previous weights
    private static double[][] mPreviousHiddenOutputWeights = new double[MAX_HIDDEN_NEURONS][MAX_OUTPUT_NEURONS];
    // variable for the value of output neuron
    private double[] mOutputValues = new double[MAX_OUTPUT_NEURONS];
    // variable for out neuron error
    private double[] mOutputNeuronErrors = new double[MAX_OUTPUT_NEURONS];
    // array to store unactivated values
    private double[] mHiddenUnactivatedValue = new double[MAX_HIDDEN_NEURONS];
    private double[] mOutputUnactivatedValue = new double[MAX_OUTPUT_NEURONS];

    //private member variables
    private double mArgA;
    private double mArgB;

    /**
            * Constructor. (Cannot be declared in an interface, but your implementation will need one)
            * @param argNumInputs The number of inputs in your input vector
 * @param argNumHidden The number of hidden neurons in your hidden layer. Only a single hidden layer is supported
 * @param argNumOutput Number of output neurons
 * @param argLearningRate The learning rate coefficient
 * @param argMomentumTerm The momentum coefficient
 * @param argA Integer lower bound of sigmoid used by the output neuron only.
            * @param argB Integer upper bound of sigmoid used by the output neuron only.
     *             **/

    public NeurualNetWork(
            int argNumInputs,
            int argNumHidden,
            int argNumOutput,
            double argLearningRate,
            double argMomentumTerm,
            double argA,
            double argB ){

        // add one for bias
        this.mNumInputs = argNumInputs + 1;
        //TODO: different: add one for bias
        this.mNumHidden = argNumHidden + 1;
        this.mNumOutputs = argNumOutput;
        this.mLearningRate = argLearningRate;
        this.mMomentumTerm = argMomentumTerm;
        this.mArgA = argA;
        this.mArgB = argB;

        zeroWeights();
        //Comment: It's a bad way to use three dimension to store NN, since input, hidden, output don't have same length
    }


    @Override
    /**
     * Return a bipolar sigmoid of the input X
     * @param x The input
     * @return f(x) = 1 / (1+e(-x))
     */
    public double sigmoid(double x) {
        return 1/(1+Math.exp(-x));
    }

    /**
     * This method implements the derivative of the sigmoid function
     * @param x The input
     * @return f'(x) = (1 / (1 + exp(-x)))(1 - (1 / (1 + exp(-x))))
     */
    public double derivativeSigmoid(double x){
        return sigmoid(x) * (1 - sigmoid(x));
    }

    @Override
    /**
     * This method implements a general sigmoid with asymptotes bounded by (a,b)
     * @param x The input
     * @return f(x) = （b-a） / (1 + e(-x)) - （-a）
     */
    public double customSigmoid(double x) {
        return ((mArgB-mArgA)/(1+Math.exp(-x)) + mArgA);
    }

    /**
     * This method implements the derivative of the custom sigmoid
     * @param x The input
     * @return f'(x) = (1 / (b - a))(customSigmoid(x) - a)(b - customSigmoid(x))
     */
    public double derivativeCustomSigmoid(double x){
        return (1.0/(mArgB - mArgA)) * (customSigmoid(x) - mArgA) * (mArgB - customSigmoid(x));
    }

    @Override
    /**
     * Initialize the weights to random values.
     * For say 2 inputs, the input vector is [0] & [1]. We add [2] for the bias.
     * Like wise for hidden units. For say 2 hidden units which are stored in an array.
     * [0] & [1] are the hidden & [2] the bias.
     * We also initialise the last weight change arrays. This is to implement the alpha term.
     */
    public void initializeWeights() {
        Random random = new Random();
        //weight from input x[i] to hidden layer h[j]
        int i, j;

        // initialize input-hidden neuron weights
        for(i = 0; i < mNumInputs; i++)
        {
            for(j = 0; j < mNumHidden; j++)
            {
                //ranging from -0.5 to 0.5
                mInputHiddenWeights[i][j] = random.nextDouble() - 0.5;
            }
        }

        // initialize hidden-output neuron weights
        for(i = 0; i < mNumHidden; i++)
        {
            for(j = 0; j < mNumOutputs; j++)
            {
                // initialize the output neuron weights
                mHiddenOutputWeights[i][j] = random.nextDouble() - 0.5;
            }
        }

        // Copy the initial weights into the delta tracking variables
        mPreviousInputHiddenWeights = mInputHiddenWeights.clone();
        mPreviousHiddenOutputWeights = mHiddenOutputWeights.clone();

    }

    @Override
    /**
     * Initialize the weights to 0.
     */
    public void zeroWeights() {
        int i, j;
        for(i = 0;i < mNumInputs;i++){
            for(j = 0;j < mNumHidden;j++){
                mInputHiddenWeights[i][j] = 0.0;
                mPreviousInputHiddenWeights[i][j] = 0.0;
            }
        }

        for(i = 0;i < mNumHidden;i++){
            for(j = 0; j< mNumOutputs; ++j){
                mHiddenOutputWeights[i][j] = 0.0;
                mPreviousHiddenOutputWeights[i][j] = 0.0;
            }
        }
    }

    private double calculateWeightDelta(double weightInput, double error, double currentWeight, double previousWeight){
        double momentum, learningTerm;
        momentum = mMomentumTerm * (currentWeight - previousWeight);
        learningTerm = mLearningRate * error * weightInput;
        return learningTerm + momentum;
    }

    private void updateWeight(){
        int h, o, i;
        double[][] newHiddenOutputWeights = new double[MAX_HIDDEN_NEURONS][MAX_OUTPUT_NEURONS];
        double[][] newInputHiddenWeights = new double[MAX_INPUT_NEURONS][MAX_HIDDEN_NEURONS];

        for(o = 0; o < mNumOutputs; o++){
            for(h = 0; h < mNumHidden; h++){
                newHiddenOutputWeights[h][o] = mHiddenOutputWeights[h][o] +
                        calculateWeightDelta(
                                mHiddenNeuronValues[h],
                                mOutputNeuronErrors[o],
                                mHiddenOutputWeights[h][o],
                                mPreviousHiddenOutputWeights[h][o]);
            }
        }

        for(h = 0; h < mNumHidden; h++){
            for(i = 0; i < mNumInputs; i++){
                newInputHiddenWeights[i][h] = mInputHiddenWeights[i][h] +
                        calculateWeightDelta(
                                mInputValues[i],
                                mHiddenNeuronErrors[i],
                                mInputHiddenWeights[i][h],
                                mPreviousInputHiddenWeights[i][h]);
            }
        }


    }

    @Override
    /**
     * @param X The input vector. An array of doubles.
     * @return The value returned by th LUT or NN for this input vector
     */
    public double[] outputFor(double[] X){
        int h, o, i, index;
        //set bias term
        //TODO: bias term for hidden forgotten
        mInputValues[0] = 1.0;

        //set input values
        for(index = 0; index < X.length; index++){
            mInputValues[index+1] = X[index];
        }

        //step forward and calculate hidden values
        //TODO: initialize unactivated to zero first
        for(h = 0; h < mNumHidden; h++){
            for( i = 0; i < mNumInputs; i++){
                mHiddenUnactivatedValue[h] += mInputValues[i]*mInputHiddenWeights[i][h];
            }
            //TODO: use unActivatedHiddenNeuronValues to store S before activation function: y=f(s), so that we can put it into derivative function
            // right now simply use y' = y(1-y) and not use S array to store intermediate result
            mHiddenNeuronValues[h] = customSigmoid(mHiddenUnactivatedValue[h]);
        }

        //Step forward and Calculate the output for the output neurons
        //TODO: initialize unactivated to zero first
        for(o = 0; o < mNumOutputs; o++){
            for(h = 0; h < mNumHidden; h++){
                mOutputUnactivatedValue[o] += mHiddenNeuronValues[h] * mHiddenOutputWeights[h][o];
            }
            mOutputValues[o] = customSigmoid(mOutputUnactivatedValue[o]);
        }

        return mOutputValues.clone();
    }

    private void calculateErrors(double[] expectedOutputs){
        int h, o, index;
        double sumError;

        //TODO: different: put hidden errors outside outputerror loop
        for(o = 0; o < mNumOutputs; o++){
            mOutputNeuronErrors[o] = (expectedOutputs[o] - mOutputValues[o]) * derivativeCustomSigmoid(mOutputUnactivatedValue[o]);
        }

        // calculate error signal for hidden unit
        for(h = 0; h < mNumHidden; h++){
            sumError = 0.0;
            for(index = 0; index < mNumOutputs; index++) {
                sumError += mOutputNeuronErrors[index] * mHiddenOutputWeights[h][index];
            }
            mHiddenNeuronErrors[h] = sumError * derivativeCustomSigmoid(mHiddenUnactivatedValue[h]);
        }
    }


    @Override
    /**
     * This method will tell the NN or the LUT the output
     * value that should be mapped to the given input vector. I.e.
     * the desired correct output value for an input.
     * @param X The input vector
     * @param argValue The new value to learn
     * @return The error in the output for that input vector
     */
    public double[] train(List<List<Double>> trainingSet) {

        double[] errors = new double[mNumOutputs];
        double[] inputs = new double[trainingSet.get(TRAINING_SET_STATE_INDEX).size()];
        double[] outputs = new double[trainingSet.get(TRAINING_SET_ACTION_INDEX).size()];
        int i;

        //get the inputs and outputs from trainingSet
        for(i = 0; i < inputs.length; i++){
            inputs[i] = trainingSet.get(TRAINING_SET_STATE_INDEX).get(i);
        }
        for(i = 0; i < outputs.length; i++){
            outputs[i] = trainingSet.get(TRAINING_SET_ACTION_INDEX).get(i);
        }

        outputFor(inputs);

        calculateErrors(outputs);

        updateWeight();

        for(i = 0; i < mNumOutputs; i++){
            errors[i] = outputs[i] - mOutputValues[i];
        }

        return errors;
    }

    public List<List<List<Double>>> getWeights(){
        List<List<List<Double>>> NNWeights = new ArrayList<>();
        List<List<Double>> inputHiddenWeights = new ArrayList<>();
        List<List<Double>> hiddenOutputWeights = new ArrayList<>();
        List<Double> subset;
        int i, h, o;

        for(h = 0; h < mNumHidden; h++){
            subset = new ArrayList<>();
            for(i = 0; i < mNumInputs; i++){
                subset.add(mInputHiddenWeights[i][h]);
            }
            inputHiddenWeights.add(subset);
        }
        NNWeights.add(inputHiddenWeights);

        //TODO: different: bias for hidden output weight
        for(o = 0; o < mNumOutputs; o++){
            subset = new ArrayList<>();
            for(h = 0; h < mNumHidden; h++){
                subset.add(mHiddenOutputWeights[h][o]);
            }
            hiddenOutputWeights.add(subset);
        }

        NNWeights.add(hiddenOutputWeights);

        return NNWeights;
    }

    public void setWeights(){
        
    }


    @Override
    /**
     * A method to write either a LUT or weights of an neural net to a file.
     * @param argFile of type File.
     */
    public void save(File argFile) {
        File output = new File("output.txt");
        try {
            boolean createNewFile = output.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(output));
            //TODO:put the epoch number and total error into the output file

        } catch (IOException e){
            System.out.println("IOException");
        }

    }

    public void saveWeight(File argFile) {
        File output = new File("Weights.txt");
        try {
            boolean createNewFile = output.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(output));
            //ToDO:put the weight into the output file
        } catch (IOException e){
            System.out.println("IOException");
        }

    }

    @Override
    /**
     * Loads the LUT or neural net weights from file. The load must of course
     * have knowledge of how the data was written out by the save method.
     * You should raise an error in the case that an attempt is being
     * made to load data into an LUT or neural net whose structure does not match
     * the data in the file. (e.g. wrong number of hidden neurons).
     * @param argFileName
     * @throws IOException
     */
    public void load(String argFileName) throws IOException {

    }

    public void loadLUT(String argFileName) throws IOException{
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(argFileName);
            Scanner inputScanner = new Scanner(fileInputStream);
            for (int i = 0; i < LUTHowManyState; i++) {
                for (int j = 0; j < LUTHowManyAction; j++) {
                    LUTTable[i][j] = customSigmoid(inputScanner.nextDouble());
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
