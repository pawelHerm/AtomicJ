package atomicJ.readers.jpk;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.utilities.IOUtilities;


import loci.common.RandomAccessInputStream;
import loci.formats.tiff.IFD;

public class JPKQIImageReader extends JPKImageReader
{
    private static final String QI_DATA_EXTENSION = "jpk-qi-data";
    private static final String QI_IMAGE_EXTENSION = "jpk-qi-image";
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {QI_IMAGE_EXTENSION, QI_DATA_EXTENSION};
    private static final String DESCRIPTION = "JPK QI image";


    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    @Override
    public List<ImageSource> readSources(File f, SourceReadingDirectives readingDirective) throws UserCommunicableException 
    {     
        String extension = IOUtilities.getExtension(f);
        List<ImageSource> sources = QI_DATA_EXTENSION.equals(extension) ? readFromZipEntry(f) : super.readSources(f, readingDirective);

        return sources;
    }     

    private List<ImageSource> readFromZipEntry(File f) throws UserCommunicableException 
    {
        try
        {
            RandomAccessInputStream in = new RandomAccessInputStream(IOUtilities.getZipEntryBytes(f, QI_IMAGE_EXTENSION));

            return readSourcesFromInputStream(f, in); 
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading in the image");
        }  
    }

    public static boolean containsQIImageEntry(ZipFile zipFile)
    {
        boolean containsEntry = IOUtilities.containsEntryByExtension(zipFile, QI_IMAGE_EXTENSION);
        return containsEntry;
    }

    @Override
    protected String getChannelName(IFD ifd,String channelType)
    {
        String channelInfo = (String) ifd.get(TAG_CHANNEL_INFO);
        Map<String, String> curvesInforProperties = convertToMap(channelInfo, "\n", ":");

        String keySegmentStyle = curvesInforProperties.get(KEY_SEGMENT_STYLE);

        String channelName = (keySegmentStyle == null) ? channelType : channelType + " " + keySegmentStyle;

        return channelName;
    }
}
