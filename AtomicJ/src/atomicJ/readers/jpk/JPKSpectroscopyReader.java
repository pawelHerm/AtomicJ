package atomicJ.readers.jpk;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import atomicJ.data.Quantities;
import atomicJ.data.Grid2D;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.FileReadingPack;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.SourceReadingStateMonitored;
import atomicJ.readers.SourceReadingStateMute;
import atomicJ.readers.ReadingPack;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapGridSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.IOUtilities;


public class JPKSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final double TOLERANCE = 1e-12;

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"jpk-force","jpk-force-map", "jpk-qi-data", "out"};
    private static final String DESCRIPTION = "JPK force curve file (.jpk-force, .jpk-force-map, .jpk-qi-data, .out)";

    static final String ZIP_ENTRY_PATH_SEPARATOR = "/";
    static final String SYSTEM_FILE_SEPARATOR = System.getProperty("file.separator");

    //extensions
    private static final String PROPERTIES = "properties";
    private static final String JPK_FORCE = "jpk-force";
    private static final String JPK_FORCE_MAP = "jpk-force-map";
    private static final String JPK_QI_DATA = "jpk-qi-data";

    //directory types

    static final String SEGMENTS = "segments";
    static final String INDEX = "index";
    static final String SHARED_DATA = "shared-data";

    //key linkers

    static final String KEY_LINKER_CHANNEL = "lcd-info.";    
    static final String KEY_LINKER_SEGMENT_HEADER = "force-segment-header-info.";

    //segment properties

    static final String KEY_CHANNEL_LIST = "channels.list";

    static final String KEY_SCANNER_MAP_X_POSITION = "force-segment-header.position.x";
    static final String KEY_SCANNER_MAP_Y_POSITION = "force-segment-header.position.y";

    static final String KEY_PREFIX_SEGMENT_HEADER = "force-segment-header";

    static final String KEY_SUFFIX_SEGMENT_STYLE = ".settings.segment-settings.style";
    static final String KEY_SUFFIX_SEGMENT_NUMBER_POINTS = ".num-points";

    static final String KEY_SUFFIX_ACTIVE_SCANNER = ".environment.xy-scanner-position-map.xy-scanners.active-xy-scanner.name";

    //deflection data properties

    static final String KEY_DEFLECTION_DATA_FILE = "channel.vDeflection.data.file.name";

    static final String KEY_PREFIX_DEFLECTION = "channel.vDeflection";  
    static final String KEY_PREFIX_DEFLECTION_DATA = "channel.vDeflection.data";
    static final String KEY_DEFLECTION_CHANNEL_INFO = "channel.vDeflection.lcd-info.*";
    static final String KEY_DEFLECTION = "vDeflection";

    //height data properties

    static final String KEY_PREFIX_CHANNEL = "channel.";
    static final String KEY_PREFIX_HEIGHT2 = "height";
    static final String KEY_MEASURED_HEIGHT = "measuredHeight";
    static final String KEY_CAPACITIVE_SENSOR_HEIGHT = "capacitiveSensorHeight";
    static final String KEY_STRAIN_GAUGE_HEIGHT = "strainGaugeHeight";

    static final String KEY_DATA = ".data";
    static final String KEY_CHANNEL_INFO = "channel.capacitiveSensorHeight.lcd-info.*";

    //channel properties key suffices

    static final String KEY_SUFFIX_FILE_NAME = ".file.name";
    static final String KEY_SUFFIX_DATA_TYPE = ".type";
    static final String KEY_SUFFIX_ENCODER_TYPE = ".encoder.type";
    static final String KEY_SUFFIX_ENCODER_SCALING_TYPE = ".encoder.scaling.type";
    static final String KEY_SUFFIX_ENCODER_SCALING_STYLE = ".encoder.scaling.style";
    static final String KEY_SUFFIX_ENCODER_SCALING_OFFSET = ".encoder.scaling.offset";
    static final String KEY_SUFFIX_ENCODER_SCALING_MULTIPLIER = ".encoder.scaling.multiplier";
    static final String KEY_SUFFIX_ENCODER_SCALING_UNIT = ".encoder.scaling.unit.unit";  

    //conversion properties key suffices

    static final String KEY_SUFFIX_CONVERSION_FORCE_DEFINED = ".conversion-set.conversion.force.defined";
    static final String KEY_SUFFIX_CONVERSION_FORCE_OFFSET = ".conversion-set.conversion.force.scaling.offset";
    static final String KEY_SUFFIX_CONVERSION_FORCE_MULTIPLIER = ".conversion-set.conversion.force.scaling.multiplier";
    static final String KEY_SUFFIX_CONVERSION_FORCE_UNIT = ".conversion-set.conversion.force.scaling.unit.unit";
    static final String KEY_SUFFIX_CONVERSION_FORCE_CALIBRATION_SLOT = ".conversion-set.conversion.force.base-calibration-slot";

    static final String KEY_SUFFIX_CONVERSION_DISTANCE_DEFINED = ".conversion-set.conversion.distance.defined";
    static final String KEY_SUFFIX_CONVERSION_DISTANCE_OFFSET = ".conversion-set.conversion.distance.scaling.offset";
    static final String KEY_SUFFIX_CONVERSION_DISTANCE_MULTIPLIER = ".conversion-set.conversion.distance.scaling.multiplier";
    static final String KEY_SUFFIX_CONVERSION_DISTANCE_UNIT = ".conversion-set.conversion.distance.scaling.unit.unit";
    static final String KEY_SUFFIX_CONVERSION_DISTANCE_CALIBRATION_SLOT = ".conversion-set.conversion.distance.base-calibration-slot";

    static final String KEY_SUFFIX_CONVERSION_CALIBRATED_OFFSET = ".conversion-set.conversion.calibrated.scaling.offset";
    static final String KEY_SUFFIX_CONVERSION_CALIBRATED_MULTIPLIER = ".conversion-set.conversion.calibrated.scaling.multiplier";
    static final String KEY_SUFFIX_CONVERSION_CALIBRATED_UNIT = ".conversion-set.conversion.calibrated.scaling.unit.unit";
    static final String KEY_SUFFIX_CONVERSION_CALIBRATED_CALIBRATION_SLOT = ".conversion-set.conversion.calibrated.base-calibration-slot";

    static final String KEY_SUFFIX_CONVERSION_NOMINAL_OFFSET = ".conversion-set.conversion.nominal.scaling.offset";
    static final String KEY_SUFFIX_CONVERSION_NOMINAL_MULTIPLIER = ".conversion-set.conversion.nominal.scaling.multiplier";
    static final String KEY_SUFFIX_CONVERSION_NOMINAL_UNIT = ".conversion-set.conversion.nominal.scaling.unit.unit";
    static final String KEY_SUFFIX_CONVERSION_NOMINAL_CALIBRATION_SLOT = ".conversion-set.conversion.nominal.base-calibration-slot";

    static final String KEY_SUFFIX_BASE_CALIBRATION_SLOT = ".base-calibration-slot";
    static final String KEY_SUFFIX_SCALING_MULTIPLIER = ".scaling.multiplier";
    static final String KEY_SUFFIX_SCALING_OFFSET = ".scaling.offset";
    static final String KEY_SUFFIX_SCALING_UNIT = ".scaling.unit.unit";

    static final String KEY_SUFFIX_CONVERSIONS_ROOT = ".conversion-set.conversion";
    static final String KEY_SUFFIX_CONVERSIONS_BASE = ".conversion-set.conversions.base";
    static final String KEY_SUFFIX_CONVERSIONS_DEFAULT = ".conversion-set.conversions.default";


    private static final String FILE_GENERAL_PROPERTIES = "header.properties";

    //force map/qi grid pattern

    private static final String KEY_MAP_PREFIX = "force-scan-map";
    private static final String KEY_QI_PREFIX = "quantitative-imaging-map";

    private static final String KEY_SUFFIX_PATTERN_BACK_AND_FORTH = ".position-pattern.back-and-forth";
    private static final String KEY_SUFFIX_PATTERN_TYPE = ".position-pattern.type";   
    private static final String VALUE_POSITION_PATTERN_GRID = "grid-position-pattern"; 

    private static final String KEY_SUFFIX_GRID_VLENGTH = ".position-pattern.grid.vlength";
    private static final String KEY_SUFFIX_GRID_ULENGTH = ".position-pattern.grid.ulength";
    private static final String KEY_SUFFIX_GRID_JLENGTH = ".position-pattern.grid.jlength";
    private static final String KEY_SUFFIX_GRID_ILENGTH = ".position-pattern.grid.ilength";
    private static final String KEY_SUFFIX_GRID_X_CENTER = ".position-pattern.grid.xcenter";
    private static final String KEY_SUFFIX_GRID_Y_CENTER = ".position-pattern.grid.ycenter";
    private static final String KEY_SUFFIX_GRID_ROTATION_ANGLE = ".position-pattern.grid.theta";

    private final JPKOutSpectroscopyReader outReader = new JPKOutSpectroscopyReader();

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        return filter.accept(f);       
    }

    @Override
    public List<SimpleSpectroscopySource> readSources(File file, SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {
        try
        {
            String fileName = file.getName();
            String extension = IOUtilities.getExtension(fileName);

            if(JPK_FORCE.equals(extension))
            {
                return readInSingleSources(file, readingDirective);
            }
            else if(JPK_FORCE_MAP.equals(extension))
            {
                return readInMapSources(file, readingDirective);
            }     
            else if(JPK_QI_DATA.equals(extension))
            {
                return readInQuantitativeImagingSources(file, readingDirective);
            }
            else if(outReader.accept(file))
            {
                return outReader.readSources(file, readingDirective);
            }
            else
            {
                throw new UserCommunicableException("Error occured while reading the file");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
    }


    public static void printProperties(Properties properties)
    {
        System.out.println("-------------------------------------");

        for(Entry<Object, Object> entry : properties.entrySet())
        {
            Object key = entry.getKey();
            Object value = entry.getValue();

            System.out.println( key + "  " + value );
        }
    }

    public List<SimpleSpectroscopySource> readInMapSources(File file, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {
        return readInMapLikeSources(file, KEY_MAP_PREFIX, readingDirective);
    }

    public List<SimpleSpectroscopySource> readInQuantitativeImagingSources(File file, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {
        return readInMapLikeSources(file, KEY_QI_PREFIX, readingDirective);
    }

    private List<SimpleSpectroscopySource> readInMapLikeSources(File file, String prefixKey, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        try(ZipFile zipFile = new ZipFile(file))
        {                  
            Properties sharedProperties = getSharedProperties(zipFile);
            Properties generalProperties = getGeneralProperties(zipFile);

            String positionPatternType = generalProperties.getProperty(prefixKey + KEY_SUFFIX_PATTERN_TYPE);

            boolean isGrid = VALUE_POSITION_PATTERN_GRID.equals(positionPatternType);

            if(!isGrid)
            {
                throw new UserCommunicableException("Error occured while reading the file. Non-grid JPK force maps not supported");
            }

            Grid2D grid = readInGrid(generalProperties, prefixKey);

            double rotationAngle = Double.valueOf(generalProperties.getProperty(prefixKey + KEY_SUFFIX_GRID_ROTATION_ANGLE));

            int n = grid.getItemCount();

            SourceReadingState state  = n > 10 ? new SourceReadingStateMonitored(n, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) :
                new SourceReadingStateMute(n);

            try{
                List<JPKForceScanSeries> allForceScanSeries = readInMultipleForceScanSeries(zipFile, grid, sharedProperties, readingDirective, state);

                for(JPKForceScanSeries forceScanSeries : allForceScanSeries)
                {
                    SimpleSpectroscopySource source = forceScanSeries.getSpectroscopySource();
                    sources.add(source);          
                }

                boolean containsQIImageEntry = JPKQIImageReader.containsQIImageEntry(zipFile);

                ReadingPack<ImageSource> readingPack = null;
                if(containsQIImageEntry)
                {
                    List<File> imageFiles = Collections.singletonList(file);
                    JPKQIImageReader imageReader = new JPKQIImageReader();
                    readingPack = new FileReadingPack<>(imageFiles, imageReader);  
                }

                boolean containsImageEntry = JPKImageReader.containsStandardImageEntry(zipFile);
                if(containsImageEntry)
                {
                    List<File> imageFiles = Collections.singletonList(file);
                    JPKImageReader imageReader = new JPKImageReader();
                    readingPack = new FileReadingPack<>(imageFiles, imageReader);  
                }

                if(Math.abs(rotationAngle)>TOLERANCE)
                {
                    correctRecordingPositionsForRotationAngle(rotationAngle, grid.getXCenter(), grid.getYCenter(), sources);
                }

                MapGridSource mapSource = new MapGridSource(file, sources, grid);               
                mapSource.setMapAreaImageReadingPack(readingPack);

                zipFile.close();
            }
            catch(Exception e)
            {
                state.setOutOfJob();
                throw e;
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the fil", e);
        }

        return sources;
    }

    private void correctRecordingPositionsForRotationAngle(double angle, double xCentre, double yCentre, List<SimpleSpectroscopySource> sources)
    {
        for(SimpleSpectroscopySource source : sources)
        {
            Point2D sourceRecordingPoint = source.getRecordingPoint();
            if(sourceRecordingPoint == null)
            {
                continue;
            }
            double x = sourceRecordingPoint.getX();
            double y = sourceRecordingPoint.getY();

            double cosine = Math.cos(-angle);
            double sine = Math.sin(-angle);

            double xNew = xCentre + (x - xCentre)*cosine + (-y + yCentre)*sine;
            double yNew = yCentre + (y -yCentre)*cosine + (x - xCentre)*sine;

            source.setRecordingPoint(new Point2D.Double(xNew, yNew));
        }
    }

    public List<SimpleSpectroscopySource> readInSingleSources(File file, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        try(ZipFile zipFile = new ZipFile(file))
        {      
            Properties sharedProperties = getSharedProperties(zipFile);
            JPKForceScanSeries forceScanSeries = readInForceScanSeries(zipFile, sharedProperties);  
            SimpleSpectroscopySource source = forceScanSeries.getSpectroscopySource();
            sources.add(source);

            zipFile.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return sources;
    }

    private JPKForceScanSeries readInForceScanSeries(ZipFile zipFile, Properties sharedProperties)
    {
        List<JPKSegment> segments = new ArrayList<>();

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();
            if(entry.isDirectory())
            {
                continue;
            }
            String entryName = entry.getName();
            String[] pathElements = entryName.split(ZIP_ENTRY_PATH_SEPARATOR);
            int pathElementCount = pathElements.length;

            int segmentsKeyWordPosition = Arrays.asList(pathElements).indexOf(SEGMENTS);
            boolean isSegment = segmentsKeyWordPosition >-1;

            if(!(isSegment && pathElementCount > (segmentsKeyWordPosition + 1)))
            {
                continue;
            }

            Object segmentKey = pathElements[segmentsKeyWordPosition + 1];
            String entryExtension = IOUtilities.getExtension(entryName);
            boolean isPropertiesEntry = PROPERTIES.equals(entryExtension);

            if(isPropertiesEntry)
            {
                JPKSegmentData segmentData = new JPKSegmentData(-1, segmentKey, entry, zipFile, sharedProperties);
                JPKSegment segment = segmentData.readInSegmentFromPropertiesEntry();
                segments.add(segment);
            }     
        }

        String longName = zipFile.getName();
        String shortName = getFileBareNameDirectoryName(longName);

        JPKForceScanSeries scanSeries = new JPKForceScanSeries(shortName, longName, longName);
        scanSeries.addSegments(segments);

        return scanSeries;
    }

    private List<JPKForceScanSeries> readInMultipleForceScanSeries(ZipFile zipFile, Grid2D grid, Properties sharedProperties, atomicJ.readers.SourceReadingDirectives readingDirective, SourceReadingState state)
    {                   
        Map<Integer, JPKForceScanSeries> scanSeriesUnderConstruction = new LinkedHashMap<>();
        String path = zipFile.getName();

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while(entries.hasMoreElements())
        {            
            ZipEntry entry = entries.nextElement();
            JPKSegmentData segmentData = getSegmentData(zipFile, entry, sharedProperties);

            if(segmentData == null)
            { 
                continue;
            }  

            Integer scanSeriesIndex = segmentData.getForceScanSeriesIndex();
            JPKForceScanSeries scanSeries = scanSeriesUnderConstruction.get(scanSeriesIndex);
            if(scanSeries == null)
            {
                scanSeries = getJPKForceScanSeries(path, scanSeriesIndex, grid);
                scanSeriesUnderConstruction.put(scanSeriesIndex, scanSeries);

                if(readingDirective.isCanceled())
                {
                    state.setOutOfJob();
                }
                if(state.isOutOfJob())
                {
                    return Collections.emptyList();
                }

                state.incrementAbsoluteProgress(); 
            }

            JPKSegment segment = segmentData.readInSegmentFromPropertiesEntry();            
            scanSeries.addSegment(segment);
        }

        List<JPKForceScanSeries> builtScanSeries = new ArrayList<>(scanSeriesUnderConstruction.values());       

        return builtScanSeries;
    }

    private JPKForceScanSeries getJPKForceScanSeries(String path, Integer index, Grid2D grid)
    {       
        String longName = path +  " (" + index + ")";
        String shortName = getFileBareNameDirectoryName(path) +  " (" + index + ")";
        JPKForceScanSeries scanSeries = new JPKForceScanSeries(shortName, longName, path);

        return scanSeries;
    }

    private JPKSegmentData getSegmentData(ZipFile zipFile, ZipEntry entry, Properties sharedProperties)
    {
        JPKSegmentData segmentData = null;

        if(entry.isDirectory())
        {
            return segmentData;
        }

        String entryName = entry.getName();
        String[] pathElements = entryName.split(ZIP_ENTRY_PATH_SEPARATOR);
        int pathElementCount = pathElements.length;

        int indexKeyWorkPosition = Arrays.asList(pathElements).indexOf(INDEX);
        boolean isMapPart = indexKeyWorkPosition>-1;

        if(!(isMapPart && pathElementCount > (indexKeyWorkPosition + 1)))
        {
            return segmentData;
        }

        int segmentsKeyWordPosition = Arrays.asList(pathElements).indexOf(SEGMENTS);
        boolean isSegment = segmentsKeyWordPosition >-1;

        Integer index = Integer.valueOf(pathElements[indexKeyWorkPosition + 1]);

        if(!(isSegment && pathElementCount > (segmentsKeyWordPosition + 1)))
        {
            return null;
        }

        String segmentKey = pathElements[segmentsKeyWordPosition + 1];
        String entryExtension = IOUtilities.getExtension(entryName);
        boolean isPropertiesEntry = PROPERTIES.equals(entryExtension);

        if(isPropertiesEntry)
        {
            segmentData = new JPKSegmentData(index, segmentKey, entry, zipFile, sharedProperties);
        }

        return segmentData;
    }

    private Grid2D readInGrid(Properties generalMapProperties, String keyPrefix)
    {            
        double centerX = 1e6*Double.valueOf(generalMapProperties.getProperty(keyPrefix + KEY_SUFFIX_GRID_X_CENTER));
        double centerY = 1e6*Double.valueOf(generalMapProperties.getProperty(keyPrefix + KEY_SUFFIX_GRID_Y_CENTER));

        int rowCount = Integer.valueOf(generalMapProperties.getProperty(keyPrefix + KEY_SUFFIX_GRID_JLENGTH));
        int columnCount = Integer.valueOf(generalMapProperties.getProperty(keyPrefix + KEY_SUFFIX_GRID_ILENGTH));

        double flankedWidth = 1e6*Double.valueOf(generalMapProperties.getProperty(keyPrefix + KEY_SUFFIX_GRID_ULENGTH));
        double flankedHeight = 1e6*Double.valueOf(generalMapProperties.getProperty(keyPrefix + KEY_SUFFIX_GRID_VLENGTH));

        double incrementX = flankedWidth/(columnCount);
        double incrementY = flankedHeight/(rowCount);

        double width = incrementX * (columnCount - 1); 
        double height = incrementY * (rowCount - 1); 

        double originX = centerX - 0.5*width;
        double originY = centerY - 0.5*height;       

        Grid2D grid = new Grid2D(incrementX, incrementY, originX, originY, rowCount, columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

        return grid;
    }

    private Properties getGeneralProperties(ZipFile zipFile)
    {
        Properties generalProperties = null;
        ZipEntry entry = zipFile.getEntry(FILE_GENERAL_PROPERTIES);

        try(InputStream entryInputStream = zipFile.getInputStream(entry))
        {
            generalProperties = loadProperties(entryInputStream);

        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }      

        return generalProperties;
    }

    private Properties getSharedProperties(ZipFile zipFile)
    {
        Properties sharedProperties = null;

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();
            if(entry.isDirectory())
            {
                continue;
            }

            String entryName = entry.getName();
            String[] pathElements = entryName.split(ZIP_ENTRY_PATH_SEPARATOR);
            int pathElementCount = pathElements.length;

            if(pathElementCount<2)
            {
                continue;
            }

            boolean isSharedData = SHARED_DATA.equals(pathElements[0]);

            if(!isSharedData)
            {
                continue;
            }

            String entryExtension = IOUtilities.getExtension(entryName);
            boolean isPropertiesFile = PROPERTIES.equals(entryExtension);

            if(isPropertiesFile)
            {                
                try(InputStream entryInputStream = zipFile.getInputStream(entry))
                {
                    sharedProperties = loadProperties(entryInputStream);

                }
                catch(IOException ex)
                {
                    ex.printStackTrace();
                }
            }          
        }

        return sharedProperties;
    }

    static String getZipDirectoryName(String zipName)
    {
        int n = zipName.lastIndexOf(ZIP_ENTRY_PATH_SEPARATOR) + 1;

        String extension = n>0 ? zipName.substring(0, n) : "";
        return extension;
    }

    private static String getFileBareNameDirectoryName(String path)
    {        
        int n = path.lastIndexOf(SYSTEM_FILE_SEPARATOR) + 1;

        String name = n > 0 ? path.substring(n, path.length()) : path;
        int extensionPosition = name.lastIndexOf('.');
        String bareName = extensionPosition>0 ? name.substring(0, extensionPosition) : name;

        return bareName;
    }

    static Properties loadProperties(InputStream in)
    {
        Properties properties = new Properties();

        try 
        {
            properties.load(in);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return properties;
    }

    public static enum ScannerType
    {
        SAMPLE("sample-scanner","force-segment-header.environment.xy-scanner-position-map.xy-scanner.sample-scanner.position.x","force-segment-header.environment.xy-scanner-position-map.xy-scanner.sample-scanner.position.y"), TIP("tip-scanner","force-segment-header.environment.xy-scanner-position-map.xy-scanner.tip-scanner.position.x","force-segment-header.environment.xy-scanner-position-map.xy-scanner.tip-scanner.position.y");

        private final String scannerName;
        private final String xPositionKey;
        private final String yPositionKey;

        ScannerType(String scannerName, String xPositionKey, String yPositionKey)
        {
            this.scannerName = scannerName;
            this.xPositionKey = xPositionKey;
            this.yPositionKey = yPositionKey;
        }

        public String getXPositionKey()
        {
            return xPositionKey;
        }

        public String getYPositionKey()
        {
            return yPositionKey;
        }

        public static ScannerType getScannerType(String scannerName)
        {
            for(ScannerType type : ScannerType.values())
            {
                if(Objects.equals(type.scannerName, scannerName))
                {
                    return type;
                }
            }

            return TIP;
        }
    }
}
