package xor;

import static java.lang.System.out;

public class XOR {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int pattern[][] = new int[4][2];
        pattern[0][0]=-1;
        pattern[0][1]=-1;
        pattern[1][0]=-1;
        pattern[1][1]=1;
        pattern[2][0]=1;
        pattern[2][1]=-1;
        pattern[3][0]=1;
        pattern[3][1]=1;
        int arg[]=new int[4];
        double argrec[]=new double[4];
        arg[0]=0;
        arg[1]=1;
        arg[2]=1;
        arg[3]=0;
        
        BuildNeuronNetwork NN1=new BuildNeuronNetwork();
        //NN1.initializeWeights();
        int epoch=0;
        
        for(epoch=0;epoch<=50000;++epoch){
            for(int i=0;i<=3;++i){
       argrec[i]= NN1.outputFor(pattern[i][0],pattern[i][1]);
        NN1.train(arg[i]);
        
            
            }
            
            
        
        }
      //  for(int i=0;i<=3;++i){
        //argrec[i]=NN1.outputFor(pattern[i][0],pattern[i][1]);
        
        
            
        //    }
        int i=0;
        
        
         
        
        
    }
    
    
    
    
}
