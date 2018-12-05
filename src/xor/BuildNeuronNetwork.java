package xor;

import java.io.File;
import java.io.IOException;
import static java.time.Clock.system;
import java.util.Random;
import java.util.Scanner;

public class BuildNeuronNetwork{
    
double NeuronCell[][];
double NeuronCellIn[][];//Input, output and hidden layer are counted here
double Weight[][][]; //Weight on every layer
double WeightChange[][][];
double layerError[][];
int layerStructure[];
int argNumInputs;
int argNumHidden;
int argNumOutput;
int argHiddenlayerNumber;
double argLearningRate;
double argMomentumTerm;
double argA;
double argB;
static int HIDDENLAYER=4;
static int NUMBEROFHIDDENLAYERS=1;
static int NUMBEROFINPUT=2;
static int NUMBEROFOUTPUT=1;
static double MINRANGE=-0.5;
static double MAXRANGE=0.5;
double WeightSum,WeightSum2;
double Error,TotalError;
/*

  public double[][] layer;//神经网络各层节点
    public double[][] layerErr;//神经网络各节点误差
    public double[][][] layer_weight;//各层节点权重
    public double[][][] layer_weight_delta;//各层节点权重动量
    public double mobp;//动量系数
    public double rate;//学习系数
*/


public BuildNeuronNetwork() {
    
    
    
    //input arg here
    /*
    layerStructure[0]=2;
    layerStructure[1]=4;
    layerStructure[2]=1;
zeroWeights(Weight, argNumInputs, argNumHidden,argNumOutput);

       
        NeuronCell = new double[layerStructure.length][];
        layerError = new double[layerStructure.length][];
        Weight = new double[layerStructure.length-1][][];
        WeightChange = new double[layerStructure.length-1][][];
        Random random = new Random();
        for(int l=0;l<layerStructure.length;l++){
            NeuronCell[l]=new double[layerStructure[l]];
            layerError[l]=new double[layerStructure[l]];
            if(l+1<layerStructure.length){
                Weight[l]=new double[layerStructure[l]+1][layerStructure[l+1]];
                WeightChange[l]=new double[layerStructure[l]+1][layerStructure[l+1]];
                for(int j=0;j<layerStructure[l]+1;j++)
                    for(int i=0;i<layerStructure[l+1];i++)
                        Weight[l][j][i]=1-random.nextDouble()*2;}
        }*/
argNumInputs=2;
 argNumHidden=4;
 argNumOutput=1;
    NeuronCell=new double[3][];
    NeuronCell[0]=new double[3];
    NeuronCell[0][2]=1;
    NeuronCell[1]=new double[5];
    NeuronCell[2]=new double[1];
    NeuronCellIn=new double[3][];
    NeuronCellIn[0]=new double[3];
    NeuronCellIn[0][2]=1;
    NeuronCellIn[1]=new double[5];
    NeuronCellIn[2]=new double[1];
    layerError=new double[3][];
    layerError[0]=new double[3];
    layerError[1]=new double[5];
    layerError[2]=new double[1];
    Weight=new double[2][][];
    Weight[0]=new double[3][4];
    Weight[1]=new double[5][1];
    WeightChange=new double[2][][];
    WeightChange[0]=new double[3][4];
    WeightChange[1]=new double[5][1];
    WeightSum=0;
    Error=0;
    TotalError=0;
    
 Random random = new Random();
      for(int i=0;i<3;i++){
         for(int j=0;j<4;++j){
                        Weight[0][i][j]=random.nextDouble()-0.5;
            }
      
      }
      
      for(int i=0;i<4;i++){
         for(int j=0;j<1;++j){
                        Weight[1][i][j]=random.nextDouble()-0.5;
            }}
    
}

/*


BpDeep(int[] layernum, double rate, double mobp){
        this.mobp = mobp;
        this.rate = rate;
        layer = new double[layernum.length][];
        layerErr = new double[layernum.length][];
        layer_weight = new double[layernum.length][][];
        layer_weight_delta = new double[layernum.length][][];
       
*/

   /* 
    public void InputBasicInformation(){
    System.out.println("Please enter input number");
      Scanner input = new Scanner(System.in);
      argNumInputs = input.nextInt();
      System.out.println("Please enter output number");
      argNumOutput = input.nextInt();
      System.out.println("Please enter number of hidden layers");
      argNumHidden = input.nextInt();
      while(argNumHidden>=5||argNumHidden<=0){
      System.out.println("Please enter number of hidden layers");
      argNumHidden = input.nextInt();}
      
      
    
    
    };
    */
    
    
       
   
    public double sigmoid(double x){
    /*
* This method implements a general sigmoid with asymptotes bounded by (a,b)
* @param x The input
* @return f(x) = b_minus_a / (1 + e(-x)) - minus_a
*/
    double a=0,b=0; 
    return 1/(1+java.lang.Math.exp(-x));
    }
 
    public double customSigmoid(double x){
    /*
* Initialize the weights to random values.
* For say 2 inputs, the input vector is [0] & [1]. We add [2] for the bias.
* Like wise for hidden units. For say 2 hidden units which are stored in an array.
* [0] & [1] are the hidden & [2] the bias.
* We also initialise the last weight change arrays. This is to implement the alpha term.
*/
    
    return 0;
    }
    
    public void initializeWeights(){
     Random random = new Random();
      for(int i=0;i<2;i++){
         for(int j=0;j<4;++j){
                        Weight[0][i][j]=1;
            }
      
      }
      
      for(int i=0;i<4;i++){
         for(int j=0;j<1;++j){
                        Weight[1][i][j]=random.nextDouble();
            }}
    }
 
    public void load(String argFileName) throws IOException{
    
    }

  

    public double outputFor(int a,int b) {
         ;
         NeuronCell[0][0]=a;
         NeuronCell[0][1]=b;
         NeuronCell[0][2]=sigmoid(NeuronCellIn[0][2]);////////////
         NeuronCellIn[0][0]=a;
         NeuronCellIn[0][1]=b;
         NeuronCellIn[0][2]=1;
        NeuronCell[1][4]=sigmoid(NeuronCellIn[1][4]);/////////////
        for(int i=0;i<4;i++){
          
         for(int j=0;j<3;++j){
                      WeightSum+=NeuronCell[0][j]*Weight[0][j][i];
                      
            }
         NeuronCellIn[1][i]=WeightSum;
      NeuronCell[1][i]=(sigmoid(WeightSum));
      WeightSum=0;
      }
       
      
      for(int i=0;i<1;i++){
         for(int j=0;j<5;++j){
                        WeightSum+=NeuronCell[1][j]*Weight[1][j][i];
            }
         //if(WeightSum>0)NeuronCell[2][0]=1;
         //else NeuronCell[2][0]=0;
     NeuronCell[2][0]=sigmoid(WeightSum);
     NeuronCellIn[2][0]=WeightSum;
      WeightSum=0;
      }
      NeuronCell[1][4]=sigmoid(NeuronCellIn[1][4]);
      NeuronCellIn[1][4]=1;
       return NeuronCell[2][0];
    }

 

    public double train(double argValue) {
       /* 
        
            for(int j=0;j<layer[l].length;j++){
                double z=layer_weight[l-1][layer[l-1].length][j];
                for(int i=0;i<layer[l-1].length;i++){
                    layer[l-1][i]=l==1?in[i]:layer[l-1][i];
                    z+=layer_weight[l-1][i][j]*layer[l-1][i];
                }
                layer[l][j]=1/(1+Math.exp(-z));
            }
        
        return layer[layer.length-1];*/
        
       
       ///////BPdelta
       for(int i=0;i<1;i++){
         for(int j=0;j<4;++j){
                 layerError[2][0]   =(argValue-NeuronCell[2][0])*NeuronCell[2][0]*(1-NeuronCell[2][0]);
                 
            }
      
      }
       
       for(int i=0;i<4;i++){
                
     layerError[1][i]=Weight[1][i][0]*layerError[2][0]*NeuronCell[1][i]*(1-NeuronCell[1][i]);
      }
       double buffer1=0;
       
                for(int j=0;j<4;j++){ buffer1+=Weight[0][2][j]*layerError[1][j] ;      }
     layerError[0][2]=buffer1*NeuronCell[0][2]*(1-NeuronCell[0][2]);
      
      double buffer=0;
      ///wight change
       for(int i=0;i<5;i++){
          
         
             buffer=Weight[1][i][0]+0*WeightChange[1][i][0]+0.2*layerError[2][0]*NeuronCell[1][i];
             WeightChange[1][i][0]=buffer-Weight[1][i][0];
             Weight[1][i][0]=buffer;
                  
     
      }
       
      
       for(int i=0;i<3;i++){
          
         for(int j=0;j<4;++j){
             buffer=Weight[0][i][j]+0*WeightChange[0][i][j]+0.2*layerError[1][j]*NeuronCell[0][i];
             WeightChange[0][i][j]=buffer-Weight[0][i][j];
             Weight[0][i][j]=buffer;
                      
            
     
     
      }}
       
      
     /* for(int i=0;i<1;i++){
         for(int j=0;j<4;++j){
                       buffer=Weight[1][j][i]+0.9*WeightChange[1][j][i]+0.1*layerError[2][i];
             WeightChange[1][j][i]=buffer-Weight[1][j][i];
             Weight[1][j][i]=buffer;
            }
      
      }*/
      
      
        
        
       return 0;
        
    }

 
      public void save(File argFile) {
       
    }


     public void NeuralNet() {
        
        
    }
    
    
}
