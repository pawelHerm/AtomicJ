package atomicJ.imageProcessing;

import gnu.trove.list.TDoubleList;

import java.util.List;

public interface SampleFunction 
{
    public double getValue(double[] line);
    public double getValue(List<Double> line);
    public double getValue(TDoubleList line);
}
