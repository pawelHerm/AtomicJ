package atomicJ.analysis;

import atomicJ.data.Channel1DData;

public interface ForceEventEstimate 
{
    public ForceEventEstimate shiftMarkerStart(double z, double f);    
    public ForceEventEstimate shiftMarkerEnd(double z, double f);
    public Channel1DData getEventData();
    //returns force in nN
    public double getForceMagnitude();
}
