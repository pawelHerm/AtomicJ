
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2014 by Paweł Hermanowicz
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

package atomicJ.readers.asylum;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;

import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;


public class AsylumImageReader extends AbstractSourceReader<ImageSource>
{  
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"ibw", "bwav"};
    private static final String DESCRIPTION = "Asylum image (.ibw, .bwav)";

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

    public boolean canBeImage(File f)
    {
        boolean canBeImage = false;

        try(FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath());) 
        {
            ByteBuffer versionBuffer = FileInputUtilities.readBytesToBuffer(channel, 2);

            IgorFileType fileType = new IgorFileType(versionBuffer);

            // reading the bin header
            ByteBuffer binHeaderBuffer = ByteBuffer.allocate(fileType.getBinHeaderSize());            
            binHeaderBuffer.put(versionBuffer.rewind());

            FileInputUtilities.readBytes(channel, binHeaderBuffer);
            binHeaderBuffer.flip();

            IgorBinaryHeader binHeader = fileType.readInBinHeader(binHeaderBuffer);

            WaveHeader waveHeader = fileType.readInWaveHeader(FileInputUtilities.readBytesToBuffer(channel, fileType.getWaveHeaderSize(), fileType.getByteOrder()));

            canBeImage = waveHeader.canBeImage();
        } catch (IOException | UserCommunicableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return canBeImage;
    }

    @Override
    public List<ImageSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {           
        List<ImageSource> sources = new ArrayList<>();

        try(FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath());) 
        {
            ByteBuffer versionBuffer = FileInputUtilities.readBytesToBuffer(channel, 2);

            IgorFileType fileType = new IgorFileType(versionBuffer);

            // reading the bin header
            ByteBuffer binHeaderBuffer = ByteBuffer.allocate(fileType.getBinHeaderSize());            
            binHeaderBuffer.put(versionBuffer.rewind());

            FileInputUtilities.readBytes(channel, binHeaderBuffer);
            binHeaderBuffer.flip();

            IgorBinaryHeader binHeader = fileType.readInBinHeader(binHeaderBuffer);        

            WaveHeader waveHeader = fileType.readInWaveHeader(FileInputUtilities.readBytesToBuffer(channel, fileType.getWaveHeaderSize(), fileType.getByteOrder()));

            double[][][] channelData = waveHeader.readInRowByRow(FileInputUtilities.readBytesToBuffer(channel, waveHeader.getWaveDataByteCount(), fileType.getByteOrder()));

            //DEPENDENCY FORMULA

            FileInputUtilities.readBytesToBuffer(channel, binHeader.getDependencyFormulaSize(), fileType.getByteOrder());

            //WAVE NOTE

            IgorWaveNote waveNote = binHeader.readInWaveNote(FileInputUtilities.readBytesToBuffer(channel, binHeader.getWaveNoteSize(), fileType.getByteOrder()));

            //OPTIONAL EXTENDED DATA UNITS

            String extendedDataUnit = binHeader.readInExtendedDataUnit(FileInputUtilities.readBytesToBuffer(channel, binHeader.getExtendedDataUnitSize(), fileType.getByteOrder()));

            //OPTIONAL EXTENDED DIMENSION UNITS

            List<String> extendedDimUnits = binHeader.readInExtendedDimensionUnits(FileInputUtilities.readBytesToBuffer(channel, binHeader.getTotalExtendedDimensionUnitSize(), fileType.getByteOrder()));


            //OPTIONAL DIMENSION LABELS

            List<String> dimensionLabels = binHeader.readInDimensionLabels(FileInputUtilities.readBytesToBuffer(channel, binHeader.getTotalDimensionLabelSize(), fileType.getByteOrder()));

            List<String> channelLabels = IgorUtilities.extractChannelNames(dimensionLabels.get(2), waveHeader.getLayerCount());

            List<ImageChannel> imageChannels = new ArrayList<>();

            Grid2D grid = waveHeader.getGrid();

            for(int i = 0; i<waveHeader.getLayerCount(); i++)
            {
                double[][] data = channelData[i];
                String channelLabel = channelLabels.get(i);

                AsylumChannel asylumLabel = new AsylumChannel(channelLabel);

                ImageChannel ch = new ImageChannel(data, grid, asylumLabel.getQuantity(), asylumLabel.getLabel(),  asylumLabel.isTrace());

                imageChannels.add(ch);
            }

            ImageSource sourceFile = new StandardImageSource(f);
            sourceFile.setChannels(imageChannels);   

            sources.add(sourceFile);

        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 

        return sources;        
    }
}

