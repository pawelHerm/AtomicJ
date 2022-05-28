package atomicJ.statistics;

import java.util.Arrays;

import atomicJ.utilities.MultipleSelector;

public class QuantileSelector
{
    private final double[] sortedData;
    private final double[] quantileProbabilities;
    private final double[] quantiles;
    private final int[] ranks;


    public QuantileSelector(double[] data, double[] quantileProbabilities)
    {
        this.quantileProbabilities = quantileProbabilities;
        this.sortedData = Arrays.copyOf(data, data.length);

        int pCount  = quantileProbabilities.length;
        this.ranks = new int[pCount*2];
        this.quantiles = new double[pCount];        

        buildQuantiles(data, quantileProbabilities);
    }

    private void buildQuantiles(double[] data, double[] ps)
    {
        int n = data.length;
        int pCount = ps.length;

        Arrays.fill(quantiles, Double.NaN);

        if(n==0)
        {
            return;
        }

        int bottom = -1;
        int top = -1;

        for(int i = 0; i<pCount; i++)
        {
            double p = ps[i];
            double k = n*p + 0.5;

            int kfloor = (int)Math.floor(k) - 1;
            int kceil = (int)Math.ceil(k) - 1;        

            if(kfloor >= 0 && kceil < n)
            {
                bottom = bottom < 0 ? 2*i : bottom;
                top = 2*i + 1;
            }

            ranks[2*i] = kfloor;
            ranks[2*i + 1] = kceil;
        }

        if(bottom < 0 || top < 0){
            return;
        }

        MultipleSelector.sortSmallest(sortedData, ranks, bottom, top);

        for(int i = 0; i<pCount; i++)
        {
            int rankIndex = 2*i;

            if(rankIndex >= bottom && rankIndex + 1 <= top)
            {
                int rankFloor = ranks[rankIndex];
                int rankCeil = ranks[rankIndex + 1];

                quantiles[i] = 0.5*(sortedData[rankFloor] + sortedData[rankCeil]);
            }
        }            
    }

    public double[] getQuantileProbabilities()
    {
        return quantileProbabilities;
    }

    public double[] getQuantiles()
    {
        return quantiles;
    }

    public double[] getSortedData()
    {
        return sortedData;
    }

    public int[] getRanks()
    {
        return ranks;
    }
}