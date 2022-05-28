package atomicJ.readers.jpk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import atomicJ.gui.UserCommunicableException;


class JPKSegmentData
{
    private final Integer forceScanSeriesIndex;
    private final Object segmentKey;

    private final ZipEntry entry;
    private final ZipFile zipFile;
    private final Properties sharedProperties;

    JPKSegmentData(Integer forceScanSeriesIndex, Object segmentKey, ZipEntry entry, ZipFile zipFile, Properties sharedProperties)
    {
        this.forceScanSeriesIndex = forceScanSeriesIndex;
        this.segmentKey = segmentKey;
        this.entry = entry;
        this.zipFile = zipFile;
        this.sharedProperties = sharedProperties;
    }

    protected JPKSegment readInSegmentFromPropertiesEntry()
    {
        String entryName = entry.getName();

        JPKSegment segment = null;
        try(InputStream entryInputStream = zipFile.getInputStream(entry))
        {            
            Properties segmentProperties = JPKSpectroscopyReader.loadProperties(entryInputStream);

            String segmentRootPath = JPKSpectroscopyReader.getZipDirectoryName(entryName);
            segment = new JPKSegment(segmentKey, forceScanSeriesIndex, segmentRootPath, sharedProperties, segmentProperties);

            segment.readInData(zipFile);

        }
        catch(IOException | UserCommunicableException ex)
        {
            ex.printStackTrace();
        }   

        return segment;
    }

    Integer getForceScanSeriesIndex()
    {
        return forceScanSeriesIndex;
    }

    private Object getSegmentKey()
    {
        return segmentKey;
    }

    private ZipEntry getZipEntry()
    {
        return entry;
    }
}