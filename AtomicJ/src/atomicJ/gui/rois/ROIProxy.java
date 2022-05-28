package atomicJ.gui.rois;

import java.io.Serializable;


public interface ROIProxy extends Serializable
{
    public ROIDrawable recreateOriginalObject(ROIStyle roiStyle, Integer key);
}
