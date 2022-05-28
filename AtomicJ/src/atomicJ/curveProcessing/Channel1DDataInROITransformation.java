package atomicJ.curveProcessing;

import atomicJ.data.Channel1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public interface Channel1DDataInROITransformation extends Channel1DDataTransformation
{
    public Channel1DData transform(Channel1DData channel, ROI roi, ROIRelativePosition position);
}
