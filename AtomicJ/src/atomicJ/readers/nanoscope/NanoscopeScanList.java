package atomicJ.readers.nanoscope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atomicJ.data.Grid2D;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;

public class NanoscopeScanList extends NanoscopeHeaderSection
{   
    //hex-edition tests revealed that Nanoscope Analysis software
    //takes into acccount the fields "Sync Distance QNM" and "Sync Distance" , but ignores "Sync Distance New" 
    //field "\Sync Distance QNM" has precedence over "\Sync Distance"
    //if there is neither "\Sync Distance QNM", nor "\Sync Distance", then Nanoscope reads file as if ramp was not sinusoidal, but regular

    //if there is a "\Sync Distance ..." field, but the "\Peak Force Amplitude" field is missing,
    //then Nanoscope tries to read in the file, but the Z range is zero. However, when there is neither
    //"\Sync Distance QNM", nor "\Sync Distance", then the lack of "\Peak Force Amplitude" has no bearing,
    //as Nanoscope assumes that the force curve is a regular curve with a linear ramp

    //if the fields "\Peak Force Engage Amplitude" or "\Peak Force Engage Setpoints" are missing,
    //then the force curve is read by Nanoscope properly with sinusoidal ramp,
    //so these two fields has no bearing on the force curve data interpretation

    //Nanoscope fields in the text header have inconsistent case, e.g. the files use Scan Size or Scan size, depending on the file version
    //so we check against field converted to lower case

    private static final String IDENTIFIER = "\\*Ciao scan list";

    private static final String RELATIVE_FIELD_PREFIX = "\\@";

    private static final String SAMPLES_PER_LINE = "samps/line";

    private static final String LINES = "lines";
    private static final String FORCE_DATA_POINTS = "force data points";

    private static final String SCAN_SIZE = "scan size"; 

    private static final String X_OFFSET = "x offset";
    private static final String Y_OFFSET = "y offset";

    private static final String SYNC_DISTANCE = "sync distance"; //in the files (.spm, .pfc) I saw this field was under the header \*Ciao scan list
    private static final String SYNC_DISTANCE_QNM = "sync distance qnm"; //in the files (.spm, .pfc) I saw this field was under the header \*Ciao scan list
    private static final String SAMPLE_POINTS = "sample points";
    private static final String PEAK_FORCE_AMPLITUDE = "peak force amplitude";
    private static final String PEAK_FORCE_CAPTURE = "peakforce capture";
    private static final String PEAK_FORCE_CAPTURE_ALLOWED = "allow";

    private double peakForceAmplitude = Double.NaN;
    private double syncDistance = Double.NaN;
    private double syncDistanceQNM = Double.NaN;
    private int samplePoints = -1;
    private boolean peakForceCaptureAllowed = false;

    private int forceDataPoints = -1;

    private UnitExpression scanSize = new UnitExpression(1, SimplePrefixedUnit.getNullInstance());
    private UnitExpression xOffset = new UnitExpression(0, SimplePrefixedUnit.getNullInstance());
    private UnitExpression yOffset = new UnitExpression(0, SimplePrefixedUnit.getNullInstance());

    private int sampsPerLine = - 1;
    private int lineCount = -1;


    private final List<String> referenceFields = new ArrayList<>();

    public NanoscopeScanList()
    {
        super(IDENTIFIER);
    }

    public double getSyncDistance()
    {        
        return syncDistance;
    }

    public double getSyncDistanceQNM()
    {
        return syncDistanceQNM;
    }

    public double getSyncDistanceForAnalysis()
    {
        double distanceForAnalysis = !Double.isNaN(syncDistanceQNM) ? syncDistanceQNM : syncDistance;

        return distanceForAnalysis;
    }

    public double getPeakForceAmplitude()    
    {
        return peakForceAmplitude;
    }

    public boolean isPeakForceCaptureAllowed()
    {
        return peakForceCaptureAllowed;
    }

    public int getSamplePoints()
    {
        return samplePoints;
    }

    public UnitExpression getScanSize()
    {
        return scanSize;
    }

    public UnitExpression getXOffset()
    {        
        return xOffset;
    }

    public UnitExpression getYOffset()
    {
        return yOffset;
    }

    public int getSamplesPerLine()
    {
        return sampsPerLine;
    }

    public int getLineCount()
    {
        return lineCount;
    }

    public int getForceDataPointCount()
    {
        return forceDataPoints;
    }

    public List<String> getReferenceFields()
    {
        return Collections.unmodifiableList(referenceFields);
    }

    public Grid2D getGrid()
    {
        int dataLengthX = sampsPerLine;
        int dataLengthY = lineCount;


        Quantity xQuantity = Quantities.DISTANCE_MICRONS;
        Quantity yQuantity = Quantities.DISTANCE_MICRONS;

        double xConversion = scanSize.getUnit().getConversionFactorTo(xQuantity.getUnit());
        double yConversion = scanSize.getUnit().getConversionFactorTo(yQuantity.getUnit());

        double xIncrement = xConversion*scanSize.getValue()/(dataLengthX - 1);
        double yIncrement = yConversion*scanSize.getValue()/(dataLengthY - 1);

        Grid2D grid = new Grid2D(xIncrement, yIncrement, 0, 0, dataLengthY, dataLengthX, xQuantity, yQuantity);

        return grid;
    }

    public boolean isFullySpecified()
    {
        boolean fullySpecified = lineCount >= 0 && sampsPerLine >= 0;

        return fullySpecified;
    }

    @Override
    public void readField(String fieldRaw)
    {        
        if(fieldRaw.startsWith(RELATIVE_FIELD_PREFIX))
        {
            referenceFields.add(fieldRaw);  
            return;
        }

        String field = dropPrefices(fieldRaw);
        String fieldLowerCase = field.toLowerCase(); //Nanoscope fields in the text header have inconsistent case, e.g. the files use Scan Size or Scan size, depending on the file version
        //so we check against field converted to lower case

        if(fieldLowerCase.startsWith(SCAN_SIZE + ":"))
        {            
            //if the scanSizeField contains only one numeric value, then we assume that the scan size
            //is a square
            UnitExpression readIn = parseUnitExpression(field);

            if(readIn != null)
            {
                this.scanSize = readIn.derive(Units.MICRO_METER_UNIT);
            }
        }    
        else if(fieldLowerCase.startsWith(X_OFFSET + ":"))
        {     

            UnitExpression readIn = parseUnitExpression(field);

            if(readIn != null)
            {
                this.xOffset = readIn.derive(Units.MICRO_METER_UNIT);   
            }
        }   
        else if(fieldLowerCase.startsWith(Y_OFFSET + ":"))
        {
            UnitExpression readIn = parseUnitExpression(field);

            if(readIn != null)
            {
                this.yOffset = readIn.derive(Units.MICRO_METER_UNIT);   
            }
        }  
        else if(fieldLowerCase.startsWith(SAMPLES_PER_LINE + ":"))
        {
            this.sampsPerLine = parseIntValue(field);
        }
        else if(fieldLowerCase.startsWith(FORCE_DATA_POINTS))
        {
            this.forceDataPoints = parseIntValue(field);
        }
        else if(fieldLowerCase.startsWith(LINES + ":"))
        {
            this.lineCount = parseIntValue(field);
        }
        if(fieldLowerCase.startsWith(PEAK_FORCE_AMPLITUDE + ":"))
        {
            //the value of the peak force amplitude is in nanometers, we want microns, so we divide by 1000
            this.peakForceAmplitude = parseDoubleValue(field)/1000.;     
        }
        else if(fieldLowerCase.startsWith(SYNC_DISTANCE + ":"))
        {
            this.syncDistance = parseDoubleValue(field);            
        }
        else if(fieldLowerCase.startsWith(SYNC_DISTANCE_QNM + ":"))
        {
            this.syncDistanceQNM = parseDoubleValue(field);            
        }
        else if(fieldLowerCase.startsWith(SAMPLE_POINTS + ":"))
        {
            this.samplePoints = parseIntValue(field);
        }
        else if(fieldLowerCase.startsWith(PEAK_FORCE_CAPTURE + ":"))
        {
            this.peakForceCaptureAllowed = PEAK_FORCE_CAPTURE_ALLOWED.equals(parseStringValue(fieldLowerCase));//we use fieldLowerCase here on purpose, in case Bruker changes "Allow" to "allow"
        }
    }
}
