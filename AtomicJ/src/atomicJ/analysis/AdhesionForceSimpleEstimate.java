package atomicJ.analysis;

import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Quantities;

public class AdhesionForceSimpleEstimate implements AdhesionForceEstimate
{
    private final double minZ;
    private final double minF;

    private final double maxZ;
    private final double maxF;

    public AdhesionForceSimpleEstimate(double minZ, double minF, double maxZ, double maxF)
    {
        this.minZ = minZ;
        this.minF = minF;
        this.maxZ = maxZ;
        this.maxF = maxF;
    }

    @Override
    public AdhesionForceEstimate shiftMarkerStart(double z, double f)
    {
        return new AdhesionForceSimpleEstimate(z, f, z, maxF);
    }

    @Override
    public AdhesionForceEstimate shiftMarkerEnd(double z, double f)
    {
        return new AdhesionForceSimpleEstimate(z, minF, z, f);
    }

    @Override
    public Channel1DData getMarkerData()
    {
        Channel1DData adhesionMarker = new FlexibleChannel1DData(new double[][] {{minZ, minF}, {maxZ, maxF}}, Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS, null);
        return adhesionMarker;
    }

    //returns adhesion force in nN
    @Override
    public double getAdhesionForce()
    {
        return Math.abs(maxF - minF);
    }
}
