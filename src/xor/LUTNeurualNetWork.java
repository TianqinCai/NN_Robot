package xor;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class LUTNeurualNetWork extends NeurualNetWork {
    /**
     * Constructor. (Cannot be declared in an interface, but your implementation will need one)
     *
     * @param argNumInputs    The number of inputs in your input vector
     * @param argNumHidden    The number of hidden neurons in your hidden layer. Only a single hidden layer is supported
     * @param argNumOutput    Number of output neurons
     * @param argLearningRate The learning rate coefficient
     * @param argMomentumTerm The momentum coefficient
     * @param argA            Integer lower bound of sigmoid used by the output neuron only.
     * @param argB            Integer upper bound of sigmoid used by the output neuron only.
     **/
    public Map<Integer, Integer> states;
    public List<String> actions;
    private int TotalStateNumber;
    private int StateCategories;
    private int TotalActionNumber;
    public double[][] StateTable;
    private double[][][][][] LUTTable;
    public LUTNeurualNetWork(Map states, List actions, int argNumInputs, int argNumHidden, int argNumOutput,
                             double argLearningRate, double argMomentumTerm, double argA, double argB)
    {
        //how to get input numbers before putting states and actions in?
        super(argNumInputs, argNumHidden, argNumOutput, argLearningRate, argMomentumTerm, argA, argB);
        this.states = states;
        this.actions = actions;
        this.StateCategories = argNumInputs;
        this.setTotalStateNumber();
        this.setTotalActionNumber();
        //TODO:better way to create LUTTable
        this.LUTTable = new double[8][3][8][2][8];
    }

    private void setTotalStateNumber(){
        TotalStateNumber = 1;
        for(int i : states.values()){
            TotalStateNumber *= i;
        }

    }

    private void setTotalActionNumber(){
        TotalActionNumber = 0;
        for(String s: actions){
            TotalActionNumber++;
        }
    }

    //initializing the state LUTTable
    public void setStateTable(){
        //initializing TotalStateNumber and TotalActionNumber:
        StateTable = new double[TotalStateNumber][TotalActionNumber];
        for(int i = 0; i < TotalStateNumber;++i) {
            //对于以前的代码：
            //1.为什么第三层是除3而不是除8
            //2. 为什么需要循环到384*8 而不是384
//                StateTable[i][0] = (double) i % 2 * 2 - 1;
//                StateTable[i][1] = ((Math.floor(((double) i / 2)) % 8) / (8 - 1)) * 2 - 1;
//                StateTable[i][2] = ((Math.floor(((double) i / 2 / 3)) % 3) / (3 - 1)) * 2 - 1;
//                StateTable[i][3] = ((Math.floor(((double) i / 2 / 8 / 3)) % 8) / (8 - 1)) * 2 - 1;

            for (int j = 0; j < StateCategories; ++j) {
                int lastLevel = states.get(states.size()-j-1);
                //put the 1D state into 2D stateTable
                //stateTable[i][j] means input i's jth state, like input i is the 13th state, which is heading: 1, tarD: 1, tarB: 0, isAiming: 1
                //stateTable[13][2] is 0, which is tarB of the 13th state
                //in this way, we transfer the 1D state number to 4 inputs,
                // combine with other 2 inputs(bias and action) we got the whole input

                //换句话说，主要作用是让Input匹配上LUT LUTTable, 现在LUT LUTTable 的存储方式是外循环384 states， 内循环8 action，每个
                //state-action对 对应一个q-value，也就是output
                //Input 只要能匹配上output就行
                StateTable[i][j] = ( Math.floor((double) i/ factorialHelper(j)) % lastLevel
                        / (lastLevel - 1)) * states.get(states.size()-1) - 1;
                //System.out.println("Number " + i + " State: i,j: " + i + ", " + j + ", value: "+ StateTable[i][j]);
            }
        }
    }

    private int factorialHelper(int n){
        int res = 1;
        if(n==0)
            return res;
        for(int i = n; i>0;i--){
            //zero based
            res *= states.get(states.keySet().size()-i);
        }
        return res;
    }

    public void loadLUT(String filename){
        //get the LUTTable from data file
        File LutFile = new File(filename);
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(LutFile);
            Scanner inputScanner = new Scanner(fileInputStream);
            for(int s1 = 0; s1 < 8; s1++){
                for(int s2 = 0; s2 < 3; s2++){
                    for(int s3 = 0; s3 < 8; s3++){
                        for(int s4 = 0; s4 < 2; s4++){
                            for(int a = 0; a < TotalActionNumber; a++){
                                //TODO: how to get better convergence?
                                this.LUTTable[s1][s2][s3][s4][a] = customSigmoid((inputScanner.nextDouble()+10));
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

    public double trainNN(){
        double totalError = 0;
        for(int s1 = 0; s1 < 8; s1++){
            for(int s2 = 0; s2 < 3; s2++){
                for(int s3 = 0; s3 < 8; s3++){
                    for(int s4 = 0; s4 < 2; s4++){
                        for(int a = 0; a < TotalActionNumber; a++){
                            double[] input = new double[5];
                            //TODO: better way to set input array
                            input[0] = s1;
                            input[1] = s2;
                            input[2] = s3;
                            input[3] = s4;
                            input[4] = a;
                            //totalError+=train(input, LUTTable[s1][s2][s3][s4][a]);
                        }
                    }
                }
            }
        }
        return totalError;
    }

    public void saveWeights(){
        File NNWeights = new File("NNWeights.txt");
        try {
            //TODO: find a way to store the weights
            BufferedWriter out3 = new BufferedWriter(new FileWriter(NNWeights));
//            for(int i = 0; i < Weight.length; i++) {
//                for(int j = 0; j < Weight[0].length; j++) {
//                    for(int k = 0; k < Weight[0][0].length;k++) {
////                        System.out.println(i+" " + j + " " + k + " ");
////                        System.out.println(Weight[i][j][k]);
//                        out3.write(String.valueOf(Weight[i][j][k])+" ");
//                    }
//                }
//            }
            out3.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
