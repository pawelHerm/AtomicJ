package atomicJ.analysis;

import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;

public class AdhesionEventEstimate implements ForceEventEstimate
{
    private static final double TOLERANCE = 1e-9;

    private static final AdhesionEventEstimate EMPTY_INSTANCE = new AdhesionEventEstimate(Double.NaN, Double.NaN, Double.NaN, Double.NaN);

    private final double adhesionF;
    private final double adhesionZ;

    private final double liftOffZ;
    private final double liftOffF;

    public AdhesionEventEstimate(double adhesionF, double adhesionZ, double liftOffZ, double liftOffF)
    {
        this.adhesionF = adhesionF;
        this.adhesionZ = adhesionZ;
        this.liftOffZ = liftOffZ;
        this.liftOffF = liftOffF;
    }

    public static AdhesionEventEstimate getEmptyInstance()
    {
        return EMPTY_INSTANCE;
    }

    @Override
    public double getForceMagnitude()
    {                       
        double adhesionForce = Math.max(-TOLERANCE, liftOffF - adhesionF);

        return adhesionForce;
    }

    public Point1DData getLiftOffPoint()
    {
        return new Point1DData(liftOffZ, liftOffF, Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS);
    }

    public Point1DData getAdhesionPoint()
    {
        return new Point1DData(adhesionZ, adhesionF, Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS);
    }

    @Override
    public ForceEventEstimate shiftMarkerStart(double z, double f)
    {
        return new ForceEventSimpleEstimate(z, f, z, liftOffF);
    }

    @Override
    public ForceEventEstimate shiftMarkerEnd(double z, double f)
    {
        return new ForceEventSimpleEstimate(z, adhesionF, z, f);
    }

    @Override
    public Channel1DData getEventData()
    {
        double[][] markerData = new double[][] {{adhesionZ, adhesionF}, {adhesionZ, liftOffF}};
        Channel1DData adhesionMarker = new FlexibleChannel1DData(markerData, Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS, null);

        return adhesionMarker;
    }
}
