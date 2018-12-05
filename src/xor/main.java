package xor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main {
    public static void main(String[] args) {
//        NeurualNetWork neurualNetWork = new NeurualNetWork(2, 4, 1, 0.2, 0.0, -0.5,0.5);
//        neurualNetWork.initializeWeights();
//        double[][] input = {{1,1},{1,0},{0,1},{0,0}};
//        double[][] input2 = {{1,1},{1,-1},{-1,1},{-1,-1}};
//        double[] output = {0,1,1,0};
//        double[] output2 = {-1,1,1,-1};
//        double error = Double.MAX_VALUE;
//        int epoch = 0, counter = 0;
        File outFile = new File("output.txt");
        try {
            boolean createNewFile = outFile.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
            //put the epoch number and total error into the output file
//            while(error >= 0.05){
//                error = 0;
//                for(int i = 0; i < 4; i++){
//                    error += neurualNetWork.train(input[i],output[i]);
//                }
//                epoch++;
//                out.write(String.valueOf(error));
//                out.write("\r\n");
//            }
            double avg = 0;
            int beyond_bound = 0, min_epoch = 10000, max_epoch = 0;

            //1. Read inputs:
            //把3072种inputs组合换成4种state，1种action作为输入
            //normalize the inputs(state action combination) to
            //4种state: Heading 8种， tarDis 3种， TarBearing 8 种， IsAiming 2 种，乘起来等于384
            //用stateTable记录每个state的Heading,tarD, TarB, IsAiming分别对应的什么

            Map<Integer, Integer> states = new HashMap<>();
            states.put(0, 8);
            states.put(1, 3);
            states.put(2, 8);
            states.put(3,2);
            List<String> actions = new ArrayList();
            actions.add("1");
            actions.add("2");
            actions.add("3");
            actions.add("4");
            actions.add("5");
            actions.add("6");
            actions.add("7");
            actions.add("8");

            LUTNeurualNetWork lutNeurualNetWork = new LUTNeurualNetWork(states, actions, states.size(), 60, 1,
                    0.2,0.1,-0.5,0.5);
            lutNeurualNetWork.initializeWeights();
            lutNeurualNetWork.loadLUT("C:\\Users\\Tianqin\\eclipse-workspace2\\592P3\\bin\\RflRobot\\RflRobot\\RFLROBOCODE.data\\Action1.csv");
            double totalError;
            do {
                totalError = lutNeurualNetWork.trainNN();
                System.out.println(totalError);
            } while(totalError>0.03);

            lutNeurualNetWork.saveWeights();


//            double[][] StateTable = new double[3072][4];
//            double[][] StateTable = new double[384][4];
//
//            for(int state=0;state<384;++state) {
//                StateTable[state][0] = (double)state % 2*2-1;
//                StateTable[state][1] = ((   Math.floor(((double)state / 2)) % 8 )     / (8 - 1))*2-1;
//                StateTable[state][2] = ((    Math.floor(((double)state / 2 / 8)) %  3)     / (3 - 1))*2-1;
//                StateTable[state][3] = ((    Math.floor(((double)state / 2 / 8 / 3)) % 8 )    / (8 - 1))*2-1;
//                //Input[state][4] = ((    Math.floor(((double)state / 2 / 53 / 8 / 3)) % 8)     / (8 - 1))*2-1;}
////                System.out.println("Previous Version: Number: "+state +" " + StateTable[state][0] + " " + StateTable[state][1]
////                    +" " + StateTable[state][2] + " " + StateTable[state][3]);
//                //System.out.println(lutNeurualNetWork.factorialHelper(2));
//            }
//
//            NeurualNetWork neurualNetWork = new NeurualNetWork(384*8, 60, 1, 0.2, 0.0, -0.5,0.5);
//            neurualNetWork.initializeWeights();
//
//            for(int k = 0; k < 400; k++) {
//
//                double[][] input = {{1,1},{1,0},{0,1},{0,0}};
//                double[][] input2 = {{1,1},{1,-1},{-1,1},{-1,-1}};
//                double[] output = {0,1,1,0};
//                double[] output2 = {-1,1,1,-1};
//                double error = Double.MAX_VALUE;
//                int epoch = 0, counter = 0;
//
//                for (epoch = 0; epoch < 10000; epoch++) {
//                    error = 0;
//                    for (int i = 0; i < 4; i++) {
//                        error += neurualNetWork.train(input2[i], output2[i]);
//                    }
////                    out.write(String.valueOf(error));
////                    out.write("\r\n");
//
//                    if (error < 0.05){
//                        if(epoch<min_epoch){
//                            min_epoch = epoch;
//                        }
//                        if(epoch>max_epoch){
//                            max_epoch = epoch;
//                        }
//                        avg+=epoch;
//                        break;
//                    }
//                    if(epoch==9999){
//                        beyond_bound++;
//                    }
//                }
//
//
//            }
//            avg /=400;
//            out.write(String.valueOf(avg));
//            out.write("\r\n");
//            out.write(String.valueOf(min_epoch));
//            out.write("\r\n");
//            out.write(String.valueOf(max_epoch));
//            out.write("\r\n");
//            out.write(String.valueOf(beyond_bound));
//            out.write("\r\n");
//            out.flush();
//            out.close();
        } catch (IOException e){
            System.out.println("IOException");
        }


    }

}
