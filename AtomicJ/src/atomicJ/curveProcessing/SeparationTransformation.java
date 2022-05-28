package atomicJ.curveProcessing;

import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.SinusoidalChannel1DData;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public class SeparationTransformation implements Channel1DDataInROITransformation
{   
    private final static String SEPARATION_SQUANTITY_NAME = "Separation";
    private final double xContact;
    private final double yContact;
    private final UnitExpression yQuantityToXQuantityConversionExpression;

    //separation is negative when the tip indents the sample
    public SeparationTransformation(double xContact, double yContact, UnitExpression yQuantityToXQuantityConversionExpression)
    {
        this.xContact = xContact;
        this.yContact = yContact;
        this.yQuantityToXQuantityConversionExpression = yQuantityToXQuantityConversionExpression;
    }

    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        Quantity xQuantity = channel.getXQuantity();
        Quantity yQuantity = channel.getYQuantity();

        PrefixedUnit xUnit = xQuantity.getUnit();
        PrefixedUnit yUnit = yQuantity.getUnit();

        UnitExpression conversionExpressionAfterMultiplication = yQuantityToXQuantityConversionExpression.multiply(yUnit);

        if(!conversionExpressionAfterMultiplication.isCompatible(xUnit))
        {
            return channel;
        }

        double yQuantityToXQuantityConversionFactorValue =
                conversionExpressionAfterMultiplication.getValue(xUnit);

        double deltaX = -channel.getX() + xContact;//it is "plus XContact", because in AtomicJ smaller x coordinates are when the tip is closer o the sample
        double deltaY = (channel.getY() - yContact);

        double separation = yQuantityToXQuantityConversionFactorValue*deltaY - deltaX;

        Point1DData channelNew = new Point1DData(separation, deltaY, getSeparationXQuantity(xQuantity), yQuantity);    
        return channelNew;
    }

    private Quantity getSeparationXQuantity(Quantity originalXQuantity)
    { 
        PrefixedUnit xUnit = originalXQuantity.getUnit();

        Quantity derivativeQuantity = new UnitQuantity(SEPARATION_SQUANTITY_NAME, xUnit);

        return derivativeQuantity;
    }

    @Override
    public Channel1DData transform(Channel1DData channel) 
    {     
        if(channel instanceof GridChannel1DData)
        {
            return transformGridChannel((GridChannel1DData)channel);
        }

        if(channel instanceof SinusoidalChannel1DData)
        {
            return transformPeakForceChannel((SinusoidalChannel1DData)channel);
        }

        if(channel instanceof Point1DData)
        {
            return transformPointChannel((Point1DData)channel);
        }

        Quantity xQuantity = channel.getXQuantity();
        Quantity yQuantity = channel.getYQuantity();


        PrefixedUnit xUnit = xQuantity.getUnit();
        PrefixedUnit yUnit = yQuantity.getUnit();

        UnitExpression conversionExpressionAfterMultiplication = yQuantityToXQuantityConversionExpression.multiply(yUnit);

        if(!conversionExpressionAfterMultiplication.isCompatible(xUnit))
        {
            return channel;
        }

        double yQuantityToXQuantityConversionFactorValue = conversionExpressionAfterMultiplication.getValue(xUnit);

        double[][] points = channel.getPoints();
        int n = points.length;
        double[][] separationVsYPoints = new double[n][];

        for(int i = 0; i<n; i++)
        {
            double[] p = points[i];
            double deltaX = -p[0] + xContact;//it is "plus XContact", because in AtomicJ smaller x coordinates are when the tip is closer o the sample
            double deltaY = p[1] - yContact;
            separationVsYPoints[i] = new double[] {yQuantityToXQuantityConversionFactorValue*deltaY - deltaX, deltaY};
        }

        FlexibleChannel1DData channelData = new FlexibleChannel1DData(separationVsYPoints, getSeparationXQuantity(xQuantity), yQuantity, channel.getXOrder());
        return channelData;
    }

    public Channel1DData transformGridChannel(GridChannel1DData channel) 
    {   
        Grid1D grid = channel.getGrid();

        double[] dataOriginal = channel.getData();
        int n = dataOriginal.length;

        double[][] separationVsYPoints = new double[n][];

        double increment = grid.getIncrement();
        double origin = grid.getOrigin();

        Quantity xQuantity = channel.getXQuantity();
        Quantity yQuantity = channel.getYQuantity();

        PrefixedUnit xUnit = xQuantity.getUnit();
        PrefixedUnit yUnit = yQuantity.getUnit();

        UnitExpression conversionExpressionAfterMultiplication = yQuantityToXQuantityConversionExpression.multiply(yUnit);

        if(!conversionExpressionAfterMultiplication.isCompatible(xUnit))
        {
            return channel;
        }

        double yQuantityToXQuantityConversionFactorValue = conversionExpressionAfterMultiplication.getValue(xUnit);

        for(int i = 0; i<n; i++)
        {            
            double deltaX = -(i*increment + origin) + xContact;//it is "plus XContact", because in AtomicJ smaller x coordinates are when the tip is closer o the sample
            double deltaY = dataOriginal[i] - yContact;
            separationVsYPoints[i] = new double[] {yQuantityToXQuantityConversionFactorValue*deltaY - deltaX, deltaY};
        }

        FlexibleChannel1DData channelData = new FlexibleChannel1DData(separationVsYPoints, getSeparationXQuantity(xQuantity), yQuantity, channel.getXOrder());
        return channelData;
    }

    public Channel1DData transformPeakForceChannel(SinusoidalChannel1DData channel) 
    {   
        double[] dataOriginal = channel.getData();

        int n = dataOriginal.length;
        double[][] separationVsYPoints = new double[n][];

        Quantity xQuantity = channel.getXQuantity();
        Quantity yQuantity = channel.getYQuantity();

        PrefixedUnit xUnit = xQuantity.getUnit();
        PrefixedUnit yUnit = yQuantity.getUnit();

        UnitExpression conversionExpressionAfterMultiplication = yQuantityToXQuantityConversionExpression.multiply(yUnit);

        if(!conversionExpressionAfterMultiplication.isCompatible(xUnit))
        {
            return channel;
        }

        double yQuantityToXQuantityConversionFactorValue = conversionExpressionAfterMultiplication.getValue(xUnit);

        for(int i = 0; i<n; i++)
        {            
            double deltaX = -channel.getX(i) + xContact;//it is "plus XContact", because in AtomicJ smaller x coordinates are when the tip is closer o the sample
            double deltaY = dataOriginal[i] - yContact;
            separationVsYPoints[i] = new double[] {yQuantityToXQuantityConversionFactorValue*deltaY - deltaX, deltaY};
        }

        FlexibleChannel1DData channelData = new FlexibleChannel1DData(separationVsYPoints, getSeparationXQuantity(xQuantity), yQuantity, channel.getXOrder());
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
