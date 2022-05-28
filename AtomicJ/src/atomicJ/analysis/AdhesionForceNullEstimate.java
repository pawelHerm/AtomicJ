package atomicJ.analysis;

import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Quantities;

public class AdhesionForceNullEstimate implements ForceEventEstimate
{
    private static final AdhesionForceNullEstimate INSTANCE = new AdhesionForceNullEstimate();
    private AdhesionForceNullEstimate()
    {}

    public static AdhesionForceNullEstimate getInstance()
    {
        return INSTANCE;
    }

    @Override
    public ForceEventEstimate shiftMarkerStart(double z, double f)
    {
        return INSTANCE;
    }

    @Override
    public ForceEventEstimate shiftMarkerEnd(double z, double f)
    {
        return INSTANCE;
    }

    @Override
    public Channel1DData getEventData()
    {
        Channel1DData adhesionMarker = new FlexibleChannel1DData(new double[][] {}, Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS, null);
        return adhesionMarker;
    }

    //returns adhesion force in nN
    @Override
    public double getForceMagnitude()
    {
        return Double.NaN;
    }
}
