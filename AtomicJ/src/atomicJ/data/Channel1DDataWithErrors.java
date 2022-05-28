package atomicJ.data;

import org.jfree.data.Range;

import atomicJ.curveProcessing.ErrorBarType;

public interface Channel1DDataWithErrors extends Channel1DData
{
    public ErrorBarType getErrorType();
    public double getErrorValue(int item);
    public double getYPlusError(int item);
    public double[] getYCoordinatesPlusErrorCopy();
    public double[] getYCoordinatesMinusErrorCopy();
    public double getYMinusError(int item);
    public Range getYRangeForDataWithErrors();
}
