package atomicJ.curveProcessing;

import java.util.List;

import atomicJ.data.Channel1DData;

public interface Channel1DMultiTransformation 
{
    public Channel1DData transform(List<Channel1DData> channel);
}
