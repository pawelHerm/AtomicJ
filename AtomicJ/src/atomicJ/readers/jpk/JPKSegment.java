package atomicJ.readers.jpk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleFlatChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.jpk.JPKSpectroscopyReader.ScannerType;

import static atomicJ.readers.jpk.JPKSpectroscopyReader.*;

class JPKSegment
{
    private final int pointNumber;
    private final Properties segmentProperties;
    private final Properties sharedProperties;

    private final JPKSegmentType type;

    private final String scannerHeightChannel;

    private final Integer forceScanSeriesIndex;
    private final Object segmentKey;
    private final String path;

    private final JPKLinearConverter deflectionDistanceConverter;
    private final JPKLinearConverter deflectionForceConverter;
    private final JPKLinearConverter heightDistanceConverter;

    private final boolean containsHeightData;
    private final boolean containsDeflectionData;

    private final boolean distanceConverterDefined;
    private final boolean forceConverterDefined;

    private final double positionX;
    private final double positionY;

    private Channel1DData deflectionChannel;

    public JPKSegment(Object segmentKey, Integer forceScanSeriesIndex, String path, Properties sharedProperties, Properties segmentProperties)
    {
        this.segmentKey = segmentKey;
        this.forceScanSeriesIndex = forceScanSeriesIndex;
        this.path = path;
        this.sharedProperties = sharedProperties;
        this.segmentProperties = segmentProperties;

        String channelsProperty = segmentProperties.getProperty(KEY_CHANNEL_LIST);

        List<String> channels = Arrays.asList(channelsProperty.split("\\s+"));

        boolean containsMeasuredHeight = channels.contains(KEY_MEASURED_HEIGHT);
        boolean containsCapacitiveSensorHeightData = channels.contains(KEY_CAPACITIVE_SENSOR_HEIGHT);
        boolean containsStrainGaugeHeightData = channels.contains(KEY_STRAIN_GAUGE_HEIGHT);

        this.scannerHeightChannel = containsMeasuredHeight ? KEY_MEASURED_HEIGHT : (containsCapacitiveSensorHeightData ? KEY_CAPACITIVE_SENSOR_HEIGHT : (containsStrainGaugeHeightData ? KEY_STRAIN_GAUGE_HEIGHT : KEY_PREFIX_HEIGHT2));

        this.containsHeightData = channels.contains(scannerHeightChannel);
        this.containsDeflectionData = channels.contains(KEY_DEFLECTION);

        this.type = JPKSegmentType.valueOf(getProperty(KEY_PREFIX_SEGMENT_HEADER, KEY_LINKER_SEGMENT_HEADER, KEY_SUFFIX_SEGMENT_STYLE));
        this.pointNumber = Integer.valueOf(getProperty(KEY_PREFIX_SEGMENT_HEADER, KEY_LINKER_SEGMENT_HEADER, KEY_SUFFIX_SEGMENT_NUMBER_POINTS));

        this.distanceConverterDefined = containsDeflectionData && Boolean.valueOf(getProperty(KEY_PREFIX_DEFLECTION, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSION_DISTANCE_DEFINED));
        this.forceConverterDefined = containsDeflectionData && Boolean.valueOf(getProperty(KEY_PREFIX_DEFLECTION, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSION_FORCE_DEFINED));

        this.deflectionDistanceConverter = distanceConverterDefined ? getDistanceConverter(KEY_PREFIX_DEFLECTION) : JPKLinearConverter.getNullInstance();
        this.deflectionForceConverter = forceConverterDefined ? getForceConverter(KEY_PREFIX_DEFLECTION)  : JPKLinearConverter.getNullInstance();
        this.heightDistanceConverter = containsHeightData ? getCalibratedConverter(KEY_PREFIX_CHANNEL + scannerHeightChannel)  : JPKLinearConverter.getNullInstance();

        String activeScannerName = getProperty(KEY_PREFIX_SEGMENT_HEADER, KEY_LINKER_SEGMENT_HEADER, KEY_SUFFIX_ACTIVE_SCANNER);
        ScannerType activeScanner = ScannerType.getScannerType(activeScannerName);

        String xPositionKey = segmentProperties.containsKey(KEY_SCANNER_MAP_X_POSITION) ? KEY_SCANNER_MAP_X_POSITION : activeScanner.getXPositionKey();
        String yPositionKey = segmentProperties.containsKey(KEY_SCANNER_MAP_Y_POSITION) ? KEY_SCANNER_MAP_Y_POSITION : activeScanner.getYPositionKey();

        this.positionX = Double.valueOf(segmentProperties.getProperty(xPositionKey, "NaN"));
        this.positionY = Double.valueOf(segmentProperties.getProperty(yPositionKey, "NaN"));        
    }

    public boolean isWellBuiltSpectroscopyChannel()
    {
        boolean wellBuilt = containsHeightData && containsDeflectionData;
        return wellBuilt;
    }

    public double getPositionX()
    {
        return positionX;
    }

    public double getPositionY()
    {
        return positionY;
    }

    public double getSpringConstant()
    {
        //we multiply by 1e-3 to convert to SI 
        double springConstant = forceConverterDefined ? 1e-3*deflectionForceConverter.getMultiplier() : Double.NaN;
        return springConstant;
    }

    public double getSensitivity()
    {
        double sensitivity = distanceConverterDefined ? deflectionDistanceConverter.getMultiplier() : Double.NaN;
        return sensitivity;
    }

    public Channel1DData getDeflectionChannel()
    {   
        return deflectionChannel;
    }

    private Channel1DData buildDeflectionBranch(double[] heightData, double[] deflectionData)
    {        
        double[] convertedHeightData = heightDistanceConverter.convert(heightData);
        double[] convertedDistanceData = deflectionDistanceConverter.convert(deflectionData);

        Quantity yQuantity = distanceConverterDefined ? Quantities.DEFLECTION_MICRONS : Quantities.DEFLECTION_VOLTS;
        return new FlexibleFlatChannel1DData(convertedHeightData, convertedDistanceData, Quantities.DISTANCE_MICRONS, yQuantity, type.getDefaultXOrder());
    }

    protected void readInData(ZipFile zipFile) throws UserCommunicableException
    {
        double[] heightData = containsHeightData ? readInChannel(zipFile, KEY_PREFIX_CHANNEL + scannerHeightChannel, KEY_PREFIX_CHANNEL + scannerHeightChannel + KEY_DATA) : new double[] {};
        double[] deflectionData = containsDeflectionData ? readInChannel(zipFile, KEY_PREFIX_DEFLECTION, KEY_PREFIX_DEFLECTION_DATA)  : new double[] {};

        this.deflectionChannel = buildDeflectionBranch(heightData, deflectionData);
    } 

    //CONVERT TO BASE
    private JPKLinearConverter getEncodingConverter(String channelKeyPrefix)
    {
        String unit = getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_ENCODER_SCALING_UNIT).trim();
        double prefixValue = UnitUtilities.getPrefixValueForUnit(unit).getConversion();

        double multiplier = prefixValue*Double.valueOf(getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_ENCODER_SCALING_MULTIPLIER));
        double offset = prefixValue*Double.valueOf(getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_ENCODER_SCALING_OFFSET));

        JPKLinearConverter converter = new JPKLinearConverter(multiplier, offset);

        return converter;
    }

    //CONVERTS TO FORCE IN NANONEWTONS   - EXPECT DATA DISTANCE IN MICRONS     
    private JPKLinearConverter getForceConverter(String channelKeyPrefix)
    {
        String unit = getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSION_FORCE_UNIT).trim();
        double prefixValue = 1e3*UnitUtilities.getPrefixValueForUnit(unit).getConversion();

        double multiplier = prefixValue*Double.valueOf(getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSION_FORCE_MULTIPLIER));
        double offset = prefixValue*Double.valueOf(getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSION_FORCE_OFFSET));

        JPKLinearConverter converter = new JPKLinearConverter(multiplier, offset);

        return converter;
    }

    //CONVERTS TO DISTANCE IN MICROMETERS
    private JPKLinearConverter getDistanceConverter(String channelKeyPrefix)
    {        
        String baseSlot = getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSIONS_BASE).trim();

        String unit = getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSION_DISTANCE_UNIT).trim();
        double prefixValue = 1e6*UnitUtilities.getPrefixValueForUnit(unit).getConversion();

        String desiredSlot = "distance";

        JPKLinearConverter converter = concatenateConverters(channelKeyPrefix, desiredSlot, baseSlot);
        converter = converter.multiply(prefixValue);

        return converter;
    }

    private JPKLinearConverter concatenateConverters(String channelKeyPrefix, String desiredSlot, String baseSlot)
    {
        String currentSlot = desiredSlot;
        JPKLinearConverter converter = new JPKLinearConverter(1, 0);

        List<JPKLinearConverter> converters= new ArrayList<>();

        while(!currentSlot.equals(baseSlot))
        {
            String multiplierKey = KEY_SUFFIX_CONVERSIONS_ROOT + "." + currentSlot + KEY_SUFFIX_SCALING_MULTIPLIER;
            String offsetKey = KEY_SUFFIX_CONVERSIONS_ROOT + "." + currentSlot + KEY_SUFFIX_SCALING_OFFSET;

            double multiplier = Double.valueOf(getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, multiplierKey));
            double offset = Double.valueOf(getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, offsetKey));

            JPKLinearConverter currentConverter = new JPKLinearConverter(multiplier, offset);

            converters.add(currentConverter);
            String baseSlotKey = KEY_SUFFIX_CONVERSIONS_ROOT + "." + currentSlot + KEY_SUFFIX_BASE_CALIBRATION_SLOT;
            currentSlot = getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, baseSlotKey).trim();                    
        }   

        //the order of converters is important! do not reverse
        for(int i = converters.size(); i>0;i--)
        {
            converter = converter.compose(converters.get(i - 1));
        }

        return converter;
    }

    //CONVERTS TO HEIGHT IN MICROMETERS
    private JPKLinearConverter getCalibratedConverter(String channelKeyPrefix)
    {

        String baseSlot = getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSIONS_BASE).trim();
        String defaultSlot = getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSIONS_DEFAULT).trim();



        String defaultConversionUnitKey = KEY_SUFFIX_CONVERSIONS_ROOT + "." + defaultSlot + KEY_SUFFIX_SCALING_UNIT;

        String unit = getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, defaultConversionUnitKey).trim();
        double prefixValue = 1e6*UnitUtilities.getPrefixValueForUnit(unit).getConversion();

        JPKLinearConverter converter = concatenateConverters(channelKeyPrefix, defaultSlot, baseSlot);
        converter = converter.multiply(prefixValue);

        return converter;
    }

    //CONVERTS TO HEIGHT IN MICROMETERS
    private JPKLinearConverter getDefaultConverter(String channelKeyPrefix)
    {
        String unit = getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSION_NOMINAL_UNIT).trim();

        double prefixValue = 1e6*UnitUtilities.getPrefixValueForUnit(unit).getConversion();

        double multiplier = prefixValue*Double.valueOf(getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSION_NOMINAL_MULTIPLIER));
        double offset = prefixValue*Double.valueOf(getProperty(channelKeyPrefix, KEY_LINKER_CHANNEL, KEY_SUFFIX_CONVERSION_NOMINAL_OFFSET));


        JPKLinearConverter converter = new JPKLinearConverter(multiplier, offset);

        return converter;
    }

    private double[] readInChannel(ZipFile zipFile, String channelKeyPrefixShort, String channelKeyPrefixLong)
            throws UserCommunicableException
    {
        String dataRelativePath = getProperty(channelKeyPrefixLong, KEY_LINKER_CHANNEL, KEY_SUFFIX_FILE_NAME);
        String dataAbsolutePath = path + dataRelativePath;

        String encoderType = getProperty(channelKeyPrefixShort, KEY_LINKER_CHANNEL, KEY_SUFFIX_ENCODER_TYPE);
        String dataTypeString = getProperty(channelKeyPrefixShort,KEY_LINKER_CHANNEL, KEY_SUFFIX_DATA_TYPE);
        JPKDataType dataType = JPKDataType.getDataType(dataTypeString);

        ZipEntry dataEntry = zipFile.getEntry(dataAbsolutePath);

        if(dataEntry == null)
        {
            throw new UserCommunicableException("Error occured while reading the file");
        }

        if(dataType.isEncoderRequired())
        {
            JPKEncodedDataReader reader = JPKEncodedDataReader.getDataReader(encoderType);
            JPKLinearConverter encodingConverter = getEncodingConverter(channelKeyPrefixShort);
            return reader.readIndData(zipFile, dataEntry, pointNumber, encodingConverter);
        }
        else
        {
            return new JPKFloatDataReader().readIndData(zipFile, dataEntry, pointNumber);
        }
    }

    private String getProperty(String prefix, String linker, String suffix)
    {
        String segmentPropertiesKey = segmentProperties.containsKey(prefix + suffix) ? prefix + suffix : prefix + KEY_DATA + suffix;

        String value = segmentProperties.getProperty(segmentPropertiesKey);   

        if(value == null)
        {                         
            String linkingKey = prefix + "." + linker + "*";

            String linkingValue = segmentProperties.getProperty(linkingKey);

            if(linkingValue != null)
            {
                String sharedPropertiesKey = linker + linkingValue + suffix;                    
                value = sharedProperties.getProperty(sharedPropertiesKey);               
            }
        }

        return value;
    }

    public Integer getForceScanSeriesIndex()
    {
        return forceScanSeriesIndex;
    }

    JPKSegmentType getSegmentType()
    {
        return type;
    }
}