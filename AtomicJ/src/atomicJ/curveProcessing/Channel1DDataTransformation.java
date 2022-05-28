package atomicJ.curveProcessing;

import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;

public interface Channel1DDataTransformation 
{
    public Channel1DData transform(Channel1DData channel);
    public Point1DData transformPointChannel(Point1DData channel);
}
