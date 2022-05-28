package atomicJ.curveProcessing;

import java.util.Arrays;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.SinusoidalChannel1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.GeometryUtilities;
import atomicJ.utilities.MathUtilities;

public class Translate1DTransformation implements Channel1DDataInROITransformation
{   
    private static final double TOLERANCE = 1e-15;

    private final double dX;
    private final double dY;

    private final boolean isXTranslated;
    private final boolean isYTranslated;

    public Translate1DTransformation(double xTranslate, double yTranslate)
    {
        this.dX = xTranslate;
        this.dY = yTranslate;

        this.isXTranslated = !MathUtilities.equalWithinTolerance(xTranslate, 0, TOLERANCE);
        this.isYTranslated = !MathUtilities.equalWithinTolerance(yTranslate, 0, TOLERANCE);
    }

    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        if(!isXTranslated && !isYTranslated)
        {
            return channel;
        }

        double xNew = channel.getX() + dX;
        double yNew = channel.getY() + dY;

        Point1DData channelNew = new Point1DData(xNew, yNew, channel.getXQuantity(), channel.getYQuantity());    
        return channelNew;
    }

    @Override
    public Channel1DData transform(Channel1DData channel) 
    {     
        if(!isXTranslated && !isYTranslated)
        {
            return channel;
        }

        if(channel instanceof GridChannel1DData)
        {
            return translateGridChannel((GridChannel1DData)channel);
        }

        if(channel instanceof SinusoidalChannel1DData)
        {
            return translatePeakForceChannel((SinusoidalChannel1DData)channel);
        }

        if(channel instanceof Point1DData)
        {
            return transformPointChannel((Point1DData)channel);
        }

        double[][] points = channel.getPoints();
        double[][] translatedPoints = null;

        if(isXTranslated && isYTranslated)
        {
            translatedPoints = GeometryUtilities.translatePointsXY(points, dX, dY);
        }
        else if(isXTranslated)
        {
            translatedPoints = GeometryUtilities.translatePointsX(points, dX);
        }
        else if(isYTranslated)
        {
            translatedPoints = GeometryUtilities.translatePointsY(points, dY);
        }

        FlexibleChannel1DData channelData = new FlexibleChannel1DData(translatedPoints, channel.getXQuantity(), channel.getYQuantity(), channel.getXOrder());
        return channelData;
    }

    public GridChannel1DData translateGridChannel(GridChannel1DData channel) 
    {   
        if(!isXTranslated && !isYTranslated)
        {
            return channel.getCopy();
        }

        Grid1D gridOriginal = channel.getGrid();

        Grid1D gridTranslated = new Grid1D(gridOriginal.getIncrement(), gridOriginal.getOrigin() + dX, gridOriginal.getIndexCount(), gridOriginal.getQuantity());

        double[] dataOriginal = channel.getData();

        double[] dataTranslated = isYTranslated ? MathUtilities.add(dataOriginal, dY) : Arrays.copyOf(dataOriginal, dataOriginal.length);

        GridChannel1DData channelData = new GridChannel1DData(dataTranslated, gridTranslated, channel.getYQuantity());

        return channelData;
    }

    public SinusoidalChannel1DData translatePeakForceChannel(SinusoidalChannel1DData channel) 
    {   
        if(!isXTranslated && !isYTranslated)
        {
            return channel.getCopy();
        }

        double[] dataOriginal = channel.getData();
        double[] dataTranslated = isYTranslated ? MathUtilities.add(dataOriginal, dY) : Arrays.copyOf(dataOriginal, dataOriginal.length);

        SinusoidalChannel1DData channelData = new SinusoidalChannel1DData(dataTranslated, channel.getAmplitude(), 
                channel.getAngleFactor(), channel.getInitIndex(), channel.getPhaseShift(), 
                channel.getXShift() + dX, channel.getXQuantity(), channel.getYQuantity());

        return channelData;
    }

    @Override
    public Channel1DData transform(Channel1DData channel, ROI roi, ROIRelativePosition position) 
    {        
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channel);
        }

        GridChannel1DData gridChannel = (channel instanceof GridChannel1DData) ? (GridChannel1DData)channel : GridChannel1DData.convert(channel);
        Grid1D grid = gridChannel.getGrid();
        double[] data = gridChannel.getData();

        int columnCount = grid.getIndexCount();

        double[] transformed = new double[columnCount];

        return null;
    }
}
