
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
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.swing.SwingUtilities;

import atomicJ.data.ChannelMetadata;
import atomicJ.data.Coordinate4D;
import atomicJ.data.Quantities;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.StandardChannelMetadata;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;

public abstract class RegularImageReader extends AbstractSourceReader<ImageSource>
{  
    protected List<ImageSource> readImages(File f, SourceReadingDirectives readingDirectives, ChannelProvider ... channelProviders) throws UserCommunicableException
    {
        int channelProviderCount = channelProviders.length;
        boolean multipleImages = channelProviderCount > 0;

        List<ImageSource> sources = new ArrayList<>();

        boolean compatible = ImageInterpretationModel.areCompatible(Arrays.asList(channelProviders));
        if(compatible)
        {
            ChannelProvider firstImage = channelProviders[0];

            boolean roisAvailable = checkIfReadInROIsAvailable(Arrays.asList(channelProviders));
            boolean colorGradientsAvailable = checkIfReadInColorGradientsAvailable(Arrays.asList(channelProviders));

            final ImageInterpretationModel model = ImageInterpretationModel.getInstance(firstImage, channelProviderCount, roisAvailable, colorGradientsAvailable);           

            if(!waitAndGetUserImageInterpretation(model).isApproved())
            {
                return Collections.emptyList();   
            }

            for(int i = 0; i<channelProviderCount; i++)
            {
                if(readingDirectives.isCanceled())
                {
                    return Collections.emptyList();
                }
                String suffix = multipleImages ? " (" + Integer.toString(i) + ")": "";

                ChannelProvider image = channelProviders[i];
                sources.addAll(readImage(f, image, model, suffix));
            }
        }else
        {
            for(int i = 0; i < channelProviderCount; i++)
            {
                if(readingDirectives.isCanceled())
                {
                    return Collections.emptyList();
                }

                ChannelProvider channelProvider = channelProviders[i];

                boolean roisAvailable = checkIfReadInROIsAvailable(Collections.singletonList(channelProvider));
                boolean colorGradientsAvailable = checkIfReadInColorGradientsAvailable(Collections.singletonList(channelProvider));

                final ImageInterpretationModel model = ImageInterpretationModel.getInstance(channelProvider, 1, roisAvailable, colorGradientsAvailable);           

                if(!waitAndGetUserImageInterpretation(model).isApproved())
                {
                    return Collections.emptyList();   
                }
                String suffix = multipleImages ? " (" + Integer.toString(i) + ")": "";

                sources.addAll(readImage(f, channelProvider, model, suffix));
            }
        }

        return sources;
    }

    private ImageInterpretationModel waitAndGetUserImageInterpretation(final ImageInterpretationModel model) throws UserCommunicableException
    {
        Runnable runnable = new Runnable() 
        {               
            @Override
            public void run() {
                ImageInterpretationDialog dialog = new ImageInterpretationDialog(AtomicJ.getApplicationFrame(),model);
                dialog.setVisible(true);
            }
        };

        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);     
        }

        return model;
    }

    protected List<ImageSource> readImage(File f, ChannelProvider channelProvider, ImageInterpretationModel model, String suffix) throws UserCommunicableException
    {
        UnitExpression scaledWidth = new UnitExpression(model.getImageWidth(), model.getImageWidthUnit()).derive(Units.MICRO_METER_UNIT);
        UnitExpression scaledHeight = new UnitExpression(model.getImageHeight(), model.getImageHeightUnit()).derive(Units.MICRO_METER_UNIT);

        int columnCount = model.getImageColumnCount();
        int rowCount = model.getImageRowCount();

        double xIncrement = scaledWidth.getValue()/(columnCount - 1);
        double yIncrement = scaledHeight.getValue()/(rowCount - 1);

        Grid2D grid = new Grid2D(xIncrement, yIncrement, 0, 0, rowCount, columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

        String longName = f.getAbsolutePath() + suffix;
        String shortName = f.getName() + suffix;


        double[][][] bands = channelProvider.getChannelData(model);

        List<String> channelNames = model.getChannelNames();

        List<Quantity> zQuantities = model.getZQuatities();
        List<ImageChannel> imageChannels = new ArrayList<>();

        Channel2DSourceMetadata metadata = channelProvider.getImageMetadata();    
        metadata.setUseReadInROIs(model.isReadInROIs());

        for(int i = 0; i<bands.length; i++)
        {
            String channelName = channelNames.get(i);

            Coordinate4D coordinates = model.isCombineChannels() ? channelProvider.getCombinedChannelCoordinates() : channelProvider.getChannelCoordinates(i);
            metadata.registerChannelCoordinates(channelName, coordinates);

            ChannelMetadata channelMetadata = new StandardChannelMetadata(coordinates);
            ImageChannel channel = new ImageChannel(bands[i], grid, zQuantities.get(i), channelName, false, channelMetadata);

            imageChannels.add(channel);
        }

        ImageSource source = new StandardImageSource(metadata,f, shortName, longName);
        source.setChannels(imageChannels);

        List<ImageSource> sourceFiles = Collections.singletonList(source);

        return sourceFiles;
    }

    public static boolean checkIfReadInROIsAvailable(List<ChannelProvider> channelProviders)
    {
        boolean available = false;

        for(ChannelProvider ch : channelProviders)
        {
            Channel2DSourceMetadata metadata = ch.getImageMetadata();
            available = available || metadata.isReadInROIsAvailable();
            if(available)
            {
                break;
            }
        }

        return available;
    }

    public static boolean checkIfReadInColorGradientsAvailable(List<ChannelProvider> channelProviders)
    {
        boolean available = false;

        for(ChannelProvider ch : channelProviders)
        {
            Channel2DSourceMetadata metadata = ch.getImageMetadata();
            available = available || metadata.isReadInColorGradientAvailable();
            if(available)
            {
                break;
            }
        }

        return available;
    }
}

