package atomicJ.readers.nanoscope;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.IntArrayReaderType;


public class NanoscopeData extends NanoscopeHeaderSection
{  
    //image offset in bytes
    private static final String DATA_OFFSET = "data offset";

    //image length in bytes
    private static final String DATA_LENGTH = "data length";

    private static final String BYTES_PER_PIXEL = "bytes/pixel";

    //image name is in quotes and/or in square brackets
    private static final String IMAGE_DATA = "image data";

    private static final String Z_SCALE = "z scale";

    final static Pattern referenceFieldPattern = Pattern.compile("\\@[0-9]*+:(.*+)");
    final static Pattern squareBracketsPattern = Pattern.compile("\\[(.*?)\\]");
    final static Pattern regularBracketsPattern = Pattern.compile("\\((.*?)\\)");
    final static Pattern quotationsPattern = Pattern.compile("\"(.*?)\"");
    final static Pattern numberPattern = Pattern.compile("([-0-9]++[0-9.Ee-]*+)");

    private int dataOffset = -1;
    private int specifiedDataLength = -1;
    private int bytesPerPixel = -1;

    private String imageName = "Unknown";

    private UnitExpression hardZScale;
    private UnitExpression hardZValue;

    private String softZScaleReference;
    private UnitExpression softZScale;

    private NanoscopeVersion version = NanoscopeVersion.getNullInstance();

    public NanoscopeData(String... identifier)
    {
        super(identifier);
    }

    public NanoscopeVersion getNanoscopeVersion()
    {
        return version;
    }

    public void setNanoscopeVersion(NanoscopeVersion version)
    {
        this.version = version;
    }

    public String getDataName()
    {
        return imageName;
    }

    public int getSpecifiedDataLength()
    {
        return specifiedDataLength;
    }

    public int getDataOffset()
    {
        return dataOffset;
    }

    public int getBytesPerPixel()
    {
        return bytesPerPixel;
    }

    public int getNumberOfBytesToSkipPerPoint()
    {        
        int toSkip = (!version.isEarlierThan(9, 2)) ? Math.max(0, 4 - bytesPerPixel): 0;
        return toSkip;
    }

    public DoubleArrayReaderType getDoubleReader()
    {
        return NanoscopeDataType.get(bytesPerPixel).getDoubleReader();
    }

    public IntArrayReaderType getIntReader()
    {
        return NanoscopeDataType.get(bytesPerPixel).getIntReader();
    }

    public long getErrorValue()
    {
        return NanoscopeDataType.get(bytesPerPixel).getErrorValue();
    }

    public UnitExpression getHardZScale()
    {
        return hardZScale;
    }

    public UnitExpression getHardZValue()
    {
        return hardZValue;
    }

    public UnitExpression getSoftZScale()
    {
        return softZScale;
    }

    public UnitExpression getZScale()
    {        
        return softZScale.multiply(hardZScale);
    }

    public UnitExpression getZScaleBasedOnHardValueAndBytePerPixel()
    {
        return softZScale.multiply(hardZValue.multiply(1./Math.pow(2, 8*bytesPerPixel)));
    }

    public boolean isFullySpecified()
    {        
        boolean fullySpecified = true;
        fullySpecified = fullySpecified && softZScale != null && hardZScale != null;
        fullySpecified = fullySpecified && dataOffset >= 0 && specifiedDataLength >= 0 && bytesPerPixel >= 0;

        return fullySpecified;
    }

    private void parseAndSetBytesPerPixel(String dataBytesPerPixelField)
    {
        this.bytesPerPixel = parseIntValue(dataBytesPerPixelField);          
    }

    protected void parseAndSetZScale(String zScale)
    {                             
        this.softZScaleReference = extractSoftScaleReference(zScale);                     
        this.hardZScale = extractHardScale(zScale);  
        this.hardZValue = extractHardValueNew(zScale);   

        if(softZScaleReference == null) //if softZScaleReference is not null, we cannot calculate soft Z Scale without reference fields
        {
            setSoftZScale(new UnitExpression(1, SimplePrefixedUnit.getNullInstance()));
        }
    }

    //uses external designation if provided
    //otherwise uses internal designation
    //if no designation found, returns 'Unknown' 

    //BOTH IMAGES AND CURVES
    public void parseAndSetImageDataEntry(String imageData)
    {
        if(imageData == null)
        {
            return;
        }

        Matcher externalDesignationMatcher = quotationsPattern.matcher(imageData);

        if(externalDesignationMatcher.find())
        {
            imageName = externalDesignationMatcher.group(1).trim();
        }
        else
        {
            Matcher internalDesignationMatcher = squareBracketsPattern.matcher(imageData);
            if(internalDesignationMatcher.find())
            {
                imageName = internalDesignationMatcher.group(1).trim();
            }
        }    
    }

    public void readInReferenceFields(List<String> referenceFields)
    {
        if(softZScaleReference == null)
        {
            return;
        }

        String softScaleField = findReferenceField(softZScaleReference, referenceFields);

        if(softScaleField != null)
        {
            setSoftZScale(extractSoftScale(softScaleField));
        }
    }

    protected void setSoftZScale(UnitExpression softZScale)
    {
        this.softZScale = softZScale;;
    }

    @Override
    public void readField(String fieldRaw)
    {              
        String field = dropPrefices(fieldRaw);
        String fieldLowerCase = field.toLowerCase(); //Nanoscope fields in the text header have inconsistent case, e.g. the files use Scan Size or Scan size, depending on the file version
        //so we check against field converted to lower case

        if(fieldLowerCase.startsWith(DATA_OFFSET))
        {
            this.dataOffset = parseIntValue(field);     
        }
        else if(fieldLowerCase.startsWith(DATA_LENGTH))
        {
            this.specifiedDataLength = parseIntValue(field);              
        }
        else if(fieldLowerCase.startsWith(BYTES_PER_PIXEL))
        {
            parseAndSetBytesPerPixel(field);
        }       
        else if(fieldLowerCase.startsWith(Z_SCALE))
        {            
            parseAndSetZScale(field);
        }
        else if(fieldLowerCase.startsWith(IMAGE_DATA))
        {           
            parseAndSetImageDataEntry(field);
        }        
    }

    private static enum NanoscopeDataType
    {
        INT16(DoubleArrayReaderType.INT16, IntArrayReaderType.INT16, Short.MIN_VALUE), INT32(DoubleArrayReaderType.INT32, IntArrayReaderType.INT32, Integer.MIN_VALUE), INT64(DoubleArrayReaderType.INT64, IntArrayReaderType.INT64, Long.MIN_VALUE);

        private final DoubleArrayReaderType doubleReader;
        private final IntArrayReaderType intReader;
        private final long errorValue;

        NanoscopeDataType(DoubleArrayReaderType reader, IntArrayReaderType intReader, long errorValue)
        {
            this.doubleReader = reader;
            this.intReader = intReader;
            this.errorValue = errorValue;
        }

        public int getByteCount()
        {
            return doubleReader.getByteSize();
        }

        public DoubleArrayReaderType getDoubleReader()
        {
            return doubleReader;
        }

        public IntArrayReaderType getIntReader()
        {
            return intReader;
        }

        public long getErrorValue()
        {
            return errorValue;
        }

        public static NanoscopeDataType get(int byteCount)
        {
            for(NanoscopeDataType dataType : NanoscopeDataType.values())
            {
                if(dataType.getByteCount() == byteCount)
                {
                    return dataType;
                }
            }
            throw new IllegalArgumentException("No NanoscopeDataType known for the byte count " + byteCount);
        }
    }
}