package xor;

import java.io.*;
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


//    public void buildLUT(int totalStates, int totalActions){
//        LUTTable = new double[totalStates][totalActions];
//    }

//    public void readLUT(String argFileName) throws IOException {
//        //Action1.csv;
//        //duplicate function, delete it later
//        loadLUT(argFileName);
//    }
//
//    public void setInputs() {
//
//        Input = new double[3072][4];
//
//        for(int state=0;state<3072;++state) {
//            Input[state][0] = (double)state % 2*2-1;
//            Input[state][1] = ((   Math.floor(((double)state / 2)) % 8 )     / (8 - 1))*2-1;
//            Input[state][2] = ((    Math.floor(((double)state / 2 / 3)) %  3)     / (3 - 1))*2-1;
//            Input[state][3] = ((    Math.floor(((double)state / 2 / 8 / 3)) % 8 )    / (8 - 1))*2-1;
//            //Input[state][4] = ((    Math.floor(((double)state / 2 / 53 / 8 / 3)) % 8)     / (8 - 1))*2-1;}
//
//        }
//    }

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

    public double outputFor(double[] X) {
        //step forward
        //first get initial input
        int i = 0, j = 0;
        for(i=0;i<argNumInputs;i++){
            S[0][i] = X[i];
            NeuronCell[0][i] = X[i];
        }
        //then add the bias term
        S[0][argNumInputs] = 1;
        NeuronCell[0][argNumInputs] = customSigmoid(S[0][argNumInputs]);
        S[1][argNumHidden] = 1;
        NeuronCell[1][argNumHidden] = customSigmoid(S[1][argNumInputs]);

        for(i=0;i<argNumHidden;i++){
            for(j=0;j<=argNumInputs;j++){
                //Wji : weigth from j to i
                WeightSum+=NeuronCell[0][j]*Weight[0][j][i];
            }
            //Sj = sigma(Wji * Xi)
            S[1][i]=WeightSum;
            NeuronCell[1][i]=(customSigmoid(WeightSum));
            //reset weigthsum
            WeightSum=0;
        }

        for(i = 0; i < argNumOutputs; i++){
            for(j = 0;j <= argNumHidden;j++){
                WeightSum += NeuronCell[1][j] * Weight[1][j][i];
            }
            NeuronCell[2][i]=customSigmoid(WeightSum);
            S[2][i]=WeightSum;
            WeightSum=0;
        }
        //if we only return 1 double, it means we only have one output, so actually we can write return NeuronCell[2][0]
        return NeuronCell[2][0];
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
    public double train(double[] X, double argValue) {

        outputFor(X);

        // for output unit
        // Ei = (Ci - yi)*yi*(1-yi)
        for(int i=0;i<argNumOutputs;i++){
            for(int j=0;j<argNumHidden;++j){
                //layerError[2][i] = (argValue-NeuronCell[2][i])*NeuronCell[2][i]*(1-NeuronCell[2][i]);
                layerError[2][i] = (argValue-NeuronCell[2][i])*0.5*(1-NeuronCell[2][i]*NeuronCell[2][i]);
            }
        }

        // for hidden unit
        // Ei = Sigma{Whi * Eh * yi * (1-yi)}
        for(int h = 0; h < argNumOutputs; h++) {
            for (int i = 0; i < argNumHidden; i++) {
                //layerError[1][i] = Weight[1][i][h] * layerError[2][h] * NeuronCell[1][i] * (1 - NeuronCell[1][i]);
                layerError[1][i] = Weight[1][i][h] * layerError[2][h] * 0.5 *  (1 - NeuronCell[1][i]* NeuronCell[1][i]);
            }
        }

        //Ej = Yj * (1-Yj) * Sigma(Eh * Whj)
        //We first calculate Sigma(Eh * Whj) term and store it into temp
        double temp=0;
        for(int h = 0; h < argNumOutputs; h++) {
            for (int j = 0; j < argNumHidden; j++) {
                temp += Weight[0][2][j] * layerError[1][j];
            }
        }
        //layerError[0][2]=temp*(1-NeuronCell[0][2])*NeuronCell[0][2];
        layerError[0][2]=temp*0.5*(1-NeuronCell[0][2]*NeuronCell[0][2]);

        //now we need to change the weight: Wji* = Wji + (Learning Rate)*(Layer Error)*(Xi)
        double Wji_With_Momentum=0;
        ///wight change
        for(int j=0;j<1;j++) {
            for (int i = 0; i <= argNumHidden; i++) {
                //Wji*
                Wji_With_Momentum = Weight[1][i][j] + argMomentumTerm * WeightChange[1][i][j] + argLearningRate * layerError[2][j] * NeuronCell[1][i];
                WeightChange[1][i][j] = Wji_With_Momentum - Weight[1][i][j];
                Weight[1][i][j] = Wji_With_Momentum;
            }
        }

        for(int i=0;i<=argNumInputs;i++){
            for(int j=0;j<argNumHidden;j++){
                Wji_With_Momentum=Weight[0][i][j]+argMomentumTerm*WeightChange[0][i][j]+argLearningRate*layerError[1][j]*NeuronCell[0][i];
                WeightChange[0][i][j]=Wji_With_Momentum-Weight[0][i][j];
                Weight[0][i][j]=Wji_With_Momentum;

            }
        }
        double error = 0.5 * Math.pow(NeuronCell[2][0]-argValue, 2);
        return error;
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
