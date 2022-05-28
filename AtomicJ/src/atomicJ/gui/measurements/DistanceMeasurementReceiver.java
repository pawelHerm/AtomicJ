package atomicJ.gui.measurements;


public interface DistanceMeasurementReceiver 
{
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement);
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement);
}
