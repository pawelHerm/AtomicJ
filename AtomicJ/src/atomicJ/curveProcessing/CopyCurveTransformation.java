package atomicJ.curveProcessing;

import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public class CopyCurveTransformation implements Channel1DDataInROITransformation
{
    @Override
    public Channel1DData transform(Channel1DData channel)
    {
        return channel.getCopy();
    }

    @Override
    public Channel1DData transform(Channel1DData channel, ROI roi,
            ROIRelativePosition position) 
    {
        return channel.getCopy();
    }

    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        return channel.getCopy();
    }
}
