package atomicJ.gui.rois;

import java.util.Map;

public interface ROIReceiver 
{
    public void setROIs(Map<Object, ROIDrawable> rois);
    public void addOrReplaceROI(ROIDrawable roi);
    public void removeROI(ROIDrawable roi);
    public void changeROILabel(Object roiKey, String labelOld, String labelNew);
}
