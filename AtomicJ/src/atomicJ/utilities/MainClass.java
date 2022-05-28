package atomicJ.utilities;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.SinusoidalChannel1DData;

public class MainClass 
{
    public static void main(String[] args)
    {

        double[][] vals = {{10, 6}, {9, 18}, {8, 2}, {7, 7}, {6, 16}, {5, 11}, {4, 17}, {3, 
            2}, {2, 15}, {1, 19}, {0, 6}};



        double[] ys = new double[] {12, 4, 0, 10, 3, 18, 6, 11, 15, 12, 5, 7, 2, 6, 20, 1, 10, 15, 10, 
                0, 9, 13, 0, 2, 3, 11, 3, 6, 10, 0, 18, 17, 7, 12, 16, 9, 20, 11, 3, 
                11};
        double amplitude = 1;
        double angleFactor = 1./30.;
        int initIndex = 0;
        double phaseShift = 0;
        double xShift =0.1;

        SinusoidalChannel1DData ch2 = new SinusoidalChannel1DData(ys, amplitude, angleFactor, initIndex, phaseShift, xShift, Quantities.TIME_SECONDS, Quantities.FORCE_NANONEWTONS);
        FlexibleChannel1DData ch = new FlexibleChannel1DData(ch2.getPoints(), Quantities.TIME_SECONDS, Quantities.FORCE_NANONEWTONS, SortedArrayOrder.DESCENDING);

        ArrayUtilities.print(ch2.getPoints());
        System.out.println(ch.getIndexCountBoundedBy(0.2,0.8));

        System.out.println(ch2.getIndexCountBoundedBy(0.2,0.8));
    }
}   
