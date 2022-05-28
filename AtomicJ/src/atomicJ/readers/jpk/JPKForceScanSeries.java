package atomicJ.readers.jpk;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Properties;

import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.MultiMap;


public class JPKForceScanSeries 
{
    private final static String KEY_POSITION_X = "force-scan-series.header.position.x";
    private final static String KEY_POSITION_Y = "force-scan-series.header.position.y";

    private double positionX; // x - coordinate of position in microns
    private double positionY; //y - coordinate of position in microns

    private final String pathName;
    private final String shortName;
    private final String longName;

    private final MultiMap<JPKSegmentType, JPKSegment> segments = new MultiMap<>();

    public JPKForceScanSeries(String shortName, String longName, String pathName)
    {
        this.pathName = pathName;
        this.shortName = shortName;
        this.longName = longName;
        this.positionX = Double.NaN;
        this.positionY = Double.NaN;
    }

    public JPKForceScanSeries(String shortName, String longName, String pathName, Properties scanSeriesProperties)
    {
        this.pathName = pathName;
        this.shortName = shortName;
        this.longName = longName;;
        this.positionX = Double.valueOf(scanSeriesProperties.getProperty(KEY_POSITION_X));
        this.positionY = Double.valueOf(scanSeriesProperties.getProperty(KEY_POSITION_Y));  
    }

    public void addSegment(JPKSegment segment)
    {        
        if(segments.isEmpty())
        {
            this.positionX = 1e6*segment.getPositionX();
            this.positionY = 1e6*segment.getPositionY();            
        }

        this.segments.put(segment.getSegmentType(), segment);
    }

    public void addSegments(List<JPKSegment> segments)
    {
        for(JPKSegment segment :segments)
        {
            addSegment(segment);
        }
    }

    private JPKSegment getFirstSegment(JPKSegmentType segmentType)
    {
        List<JPKSegment> segmentsForType = segments.get(segmentType);
        boolean segmentForTypeAdded = segmentsForType != null && !segmentsForType.isEmpty();
        JPKSegment segmentForType = segmentForTypeAdded ? segmentsForType.get(0) : null;

        return segmentForType;       
    }

    private Point2D getRecordingPoint()
    {
        boolean pointKnown = !(Double.isNaN(positionX) || Double.isNaN(positionY));

        Point2D.Double point = pointKnown ? new Point2D.Double(positionX, positionY) : null;

        return point;
    }

    public SimpleSpectroscopySource getSpectroscopySource()
    {              
        JPKSegment firstApproachSegment = getFirstSegment(JPKSegmentType.extend);
        JPKSegment firstWithdrawSegment = getFirstSegment(JPKSegmentType.retract);

        JPKSegment segmentForSensitivityAndSpringConstantQueries = (firstApproachSegment != null) ? firstApproachSegment : firstWithdrawSegment;

        double sensitivity = segmentForSensitivityAndSpringConstantQueries.getSensitivity();
        double springConstant = segmentForSensitivityAndSpringConstantQueries.getSpringConstant();

        Channel1DData approachData = (firstApproachSegment == null)  ? FlexibleChannel1DData.getEmptyInstance(firstWithdrawSegment.getDeflectionChannel()) : firstApproachSegment.getDeflectionChannel();
        Channel1DData withdrawData = (firstWithdrawSegment == null) ? FlexibleChannel1DData.getEmptyInstance(firstApproachSegment.getDeflectionChannel()) : firstWithdrawSegment.getDeflectionChannel();

        StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(pathName, shortName, longName, approachData, withdrawData);

        source.setSensitivity(sensitivity);
        source.setSpringConstant(springConstant);

        source.setRecordingPoint(getRecordingPoint());

        return source;
    }
}
