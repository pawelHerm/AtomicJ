package atomicJ.analysis;

import atomicJ.data.Channel1DData;

public interface AdhesionForceEstimate 
{
    public AdhesionForceEstimate shiftMarkerStart(double z, double f);    
    public AdhesionForceEstimate shiftMarkerEnd(double z, double f);
    public Channel1DData getMarkerData();
    //returns adhesion force in nN
    public double getAdhesionForce();
}
