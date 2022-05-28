package atomicJ.imageProcessing;

import atomicJ.data.Channel2DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public interface ImageInPlaceTransformation
{
    public boolean isIdentity();
    public void transform(Channel2DData channel);
    public void transform(Channel2DData channel, ROI roi, ROIRelativePosition position);
}
