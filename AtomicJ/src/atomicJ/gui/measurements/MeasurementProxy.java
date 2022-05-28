package atomicJ.gui.measurements;

import java.io.Serializable;


public interface MeasurementProxy extends Serializable
{
    public DistanceMeasurementDrawable recreateOriginalObject(DistanceMeasurementStyle style, Integer key);
}
