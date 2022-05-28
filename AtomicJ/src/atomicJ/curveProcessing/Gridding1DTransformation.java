package atomicJ.curveProcessing;

import atomicJ.analysis.InterpolationMethod1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public class Gridding1DTransformation implements Channel1DDataInROITransformation
{ 
    private final int columnCount;
    private final InterpolationMethod1D interpolationMethod;

    public Gridding1DTransformation(int columnCount, InterpolationMethod1D settings)
    {
        this.columnCount = columnCount;
        this.interpolationMethod = settings;
    }

    @Override
    public GridChannel1DData transform(Channel1DData channelData, ROI roi, ROIRelativePosition position)
    {
        return interpolationMethod.getGriddedData(channelData, columnCount);
    }

    @Override
    public Channel1DData transform(Channel1DData channelData)
    {
        return interpolationMethod.getGriddedData(channelData, columnCount);
    }

    @Override
    public Point1DData transformPointChannel(Point1DData channelData)
    {
        return channelData;
    }
}
