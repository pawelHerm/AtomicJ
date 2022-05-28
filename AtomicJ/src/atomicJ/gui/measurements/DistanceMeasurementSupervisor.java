package atomicJ.gui.measurements;

import java.awt.Cursor;


public interface DistanceMeasurementSupervisor 
{
    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement);
    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement);
    public void requestCursorChange(Cursor cursor);
}
