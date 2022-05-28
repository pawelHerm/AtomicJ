package atomicJ.curveProcessing;

import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public class NullCurveTransformation implements Channel1DDataInROITransformation 
{

    private static final NullCurveTransformation INSTANCE = new NullCurveTransformation();

    private NullCurveTransformation()
    {}

    public static NullCurveTransformation getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        return channel;
    }

    @Override
    public Channel1DData transform(Channel1DData channel) 
    {
        return channel;
    }

    @Override
    public Channel1DData transform(Channel1DData channel, ROI roi,
            ROIRelativePosition position) {
        return channel;
    }

}
