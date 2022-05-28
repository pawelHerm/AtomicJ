package atomicJ.gui.rois;

import java.io.Serializable;

public interface ROIComponentSerializationProxy extends Serializable 
{
    public ROIComponent recreateOriginalObject(ROIStyle style, Integer unionKey);
}
