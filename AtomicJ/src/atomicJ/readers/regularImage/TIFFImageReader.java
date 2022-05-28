
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.readers.regularImage;


import java.io.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.Map.Entry;

import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.tiff.IFD;
import loci.formats.tiff.IFDList;
import loci.formats.tiff.PhotoInterp;
import loci.formats.tiff.TiffParser;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.readers.IntArrayReaderType;
import atomicJ.sources.ImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.MultiMap;


public class TIFFImageReader extends RegularImageReader
{  
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"tif","tiff", "TIF","TIFF"};
    private static final String DESCRIPTION = "TIFF image (.tif, .tiff, .TIF, .TIFF)";

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
    public List<ImageSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalSpectroscopySourceException
    {       
        try(RandomAccessInputStream in = new RandomAccessInputStream(f.getAbsolutePath()))
        {
            TiffParser parser = new TiffParser(in);
            IFDList ifds = parser.getNonThumbnailIFDs();

            if(ifds.isEmpty())
            {
                return Collections.emptyList();
            }

            int imageCount = ifds.size();

            IFD firstIFD = ifds.get(0);

            double firstXResolution = firstIFD.getXResolution();
            double firstYResolution = firstIFD.getYResolution();


            ChannelProvider[] img = new ChannelProvider[imageCount];

            for(int i = 0; i < imageCount; i++)
            {
                IFD ifd = ifds.get(i);

                ByteOrder byteOrder = ifd.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

                PhotoInterp photInterp = ifd.getPhotometricInterpretation();

                boolean indexed = PhotoInterp.RGB_PALETTE.equals(photInterp);

                int width = (int) ifd.getImageWidth();
                int height = (int) ifd.getImageLength();

                int sampleCount = ifd.getSamplesPerPixel();

                int bitFormat = ifd.getIFDIntValue(IFD.SAMPLE_FORMAT);
                int[] bytesPerSample = ifd.getBytesPerSample();
                int[] extraSamplesReadIn = ifd.getIFDIntArray(IFD.EXTRA_SAMPLES);
                int[] extraSamples = (extraSamplesReadIn != null) ? extraSamplesReadIn : new int[] {};

                TIFFPhotometricInterpretation photometricInterpretation = TIFFPhotometricInterpretation.get(photInterp.getCode());
                List<String> channelNames = getChannelNames(photometricInterpretation, extraSamples);

                double xResolutionReadin = ifd.getXResolution();
                double yResolutionReadIn = ifd.getYResolution();

                //x and y resolution are sometimes only written in the first IFD, but they apply to all images
                double xResolution = (Double.isNaN(xResolutionReadin) || xResolutionReadin <= 0) ? firstXResolution : xResolutionReadin;
                double yResolution = (Double.isNaN(yResolutionReadIn) || yResolutionReadIn <= 0) ? firstYResolution : yResolutionReadIn;

                UnitExpression xLength = (Double.isNaN(xResolution) || xResolution <= 0) ? new UnitExpression(xResolution*width, Units.MICRO_METER_UNIT) : null;
                UnitExpression yLength = (Double.isNaN(yResolution) || yResolution <= 0) ? new UnitExpression(yResolution*height, Units.MICRO_METER_UNIT) : null;

                if(indexed)
                {
                    List<IntArrayReaderType> sampleReaders = new ArrayList<>();

                    for(int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++)
                    {
                        sampleReaders.add(TIFFSampleType.get(bytesPerSample[sampleIndex], bitFormat).getIntReader());
                    }

                    int imageByteCount = width*height*IntArrayReaderType.countBytes(sampleReaders);                              
                    byte[] imageBytes = parser.getSamples(ifd, new byte[imageByteCount]);

                    double[][] palette = getLookupTable(parser, ifd);
                    img[i] = new IndexedChannelProvider(imageBytes, byteOrder, false, sampleReaders, photometricInterpretation.getColorSpaceAsReadByBioFormats(), channelNames, palette, height, width, xLength, yLength);       
                }
                else
                {
                    List<DoubleArrayReaderType> sampleReaders = new ArrayList<>();

                    for(int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++)
                    {
                        sampleReaders.add(TIFFSampleType.get(bytesPerSample[sampleIndex], bitFormat).getDoubleReader());
                    }

                    int imageByteCount = width*height*DoubleArrayReaderType.countBytes(sampleReaders);                              
                    byte[] imageBytes = parser.getSamples(ifd, new byte[imageByteCount]);

                    img[i] = new WrappedBytesChannelProvider(imageBytes, byteOrder, false, sampleReaders, photometricInterpretation.getColorSpaceAsReadByBioFormats(), channelNames, height, width, xLength, yLength);

                }
            } 

            return readImages(f, readingDirectives, img);
        } 
        catch (IOException | FormatException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading in the image");
        }  
    }

    public List<String> getChannelNames(TIFFPhotometricInterpretation photometricInterpretation, int[] extraSamples)
    {
        List<String> channelNames = new ArrayList<>();

        List<String> standardChannelNames = photometricInterpretation.getChannelNamesAsReadByBioFormats();
        channelNames.addAll(standardChannelNames);

        int standardChannelsCount = standardChannelNames.size();
        int channelCount = standardChannelsCount + extraSamples.length;

        MultiMap<TIFFExtraChannelType, TIFFExtraChannelType> extraChannelTypes = new MultiMap<>();

        for(int i = 0; i<extraSamples.length; i++)
        {
            TIFFExtraChannelType channelType = TIFFExtraChannelType.getExtraChannelType(extraSamples[i]);
            extraChannelTypes.put(channelType, channelType);
        }

        int channelIndex = standardChannelsCount;

        for(Entry<TIFFExtraChannelType, List<TIFFExtraChannelType>> entry : extraChannelTypes.entrySet())
        {
            List<TIFFExtraChannelType> channels = entry.getValue();

            for(int i = 0; i<channels.size();i++)
            {
                TIFFExtraChannelType ch = channels.get(i);
                channelNames.add(ch.getExtraChannelName(channelIndex++, channelCount, i, channels.size()));
            }
        }       

        return channelNames;
    }

    public double[][] getLookupTable(TiffParser tiffParser, IFD ifd) throws FormatException, IOException 
    {
        int[] bits = ifd.getBitsPerSample();

        int expectedColorMapLength = 3*(int)Math.pow(2, bits[0]);

        int[] colorMap = tiffParser.getColorMap(ifd);
        if (colorMap == null || colorMap.length < expectedColorMapLength)
        {
            return null;
        }

        double[][] table = new double[3][colorMap.length / 3];

        for (int i = 0, next = 0; i < 3; i++) 
        {
            for (int j = 0; j < table[0].length; j++) 
            {
                table[i][j] = (colorMap[next++] & 0xffff);
            }
        }
        return table;
    }
}

