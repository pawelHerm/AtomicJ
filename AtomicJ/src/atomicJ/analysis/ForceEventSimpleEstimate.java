package atomicJ.analysis;

import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Quantities;

public class ForceEventSimpleEstimate implements ForceEventEstimate
{
    private final double minZ;
    private final double minF;

    private final double maxZ;
    private final double maxF;

    public ForceEventSimpleEstimate(double minZ, double minF, double maxZ, double maxF)
    {
        this.minZ = minZ;
        this.minF = minF;
        this.maxZ = maxZ;
        this.maxF = maxF;
    }

    @Override
    public ForceEventEstimate shiftMarkerStart(double z, double f)
    {
        return new ForceEventSimpleEstimate(z, f, z, maxF);
    }

    @Override
    public ForceEventEstimate shiftMarkerEnd(double z, double f)
    {
        return new ForceEventSimpleEstimate(z, minF, z, f);
    }

    @Override
    public Channel1DData getEventData()
    {
        Channel1DData adhesionMarker = new FlexibleChannel1DData(new double[][] {{minZ, minF}, {maxZ, maxF}}, Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS, null);
        return adhesionMarker;
    }

    //returns adhesion force in nN
    @Override
    public double getForceMagnitude()
    {
        return Math.abs(maxF - minF);
    }
}
