package atomicJ.imageProcessing;

import atomicJ.data.Channel2DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public interface Channel2DDataInROITransformation extends Channel2DDataTransformation
{
    public Channel2DData transform(Channel2DData channel, ROI roi, ROIRelativePosition position);
}
