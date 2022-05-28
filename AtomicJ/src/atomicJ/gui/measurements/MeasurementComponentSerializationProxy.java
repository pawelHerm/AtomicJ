package atomicJ.gui.measurements;

import java.io.Serializable;

public interface MeasurementComponentSerializationProxy extends Serializable 
{
    public MeasurementComponent recreateOriginalObject(DistanceMeasurementStyle style, Integer unionKey);
}
