/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2015 by Pawe³ Hermanowicz
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

package atomicJ.readers.nanosurf;

import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.analysis.ForceCurveOrientation;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.ImageChannel;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.QuantityArray2DExpression;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.nanosurf.NIDDataset.NIDChannelType;

public class NIDGroup
{
    private static final String SPEC_FORWARD = "Spec forward";
    private static final String SPEC_BACKWARD = "Spec backward";

    static final String SCAN_FORWARD = "Scan forward";
    static final String SCAN_BACKWARD = "Scan backward";

    private final String name;
    private final String id;
    private final SortedMap<Integer, NIDChannel> channels;

    NIDGroup(String name, String id, SortedMap<Integer, NIDChannel> channels)
    {
        this.name = name;
        this.id = id;
        this.channels = channels;       
    }        

    public String getName()
    {
        return name;
    }

    public String getId()
    {
        return id;
    }

    public boolean isApproachBranchGroup()
    {
        return SPEC_FORWARD.equals(name);
    }

    public boolean isWithdrawBranchGroup()
    {
        return SPEC_BACKWARD.equals(name);
    }

    public boolean isScanTrace()
    {
        return SCAN_FORWARD.equals(name);
    }

    public boolean isScanRetrace()
    {
        return SCAN_BACKWARD.equals(name);
    }

    public ForceCurveBranch getForceCurveBranch()
    {
        ForceCurveBranch curveBranch = SPEC_FORWARD.equals(name) ? ForceCurveBranch.APPROACH : (SPEC_BACKWARD.equals(name) ? ForceCurveBranch.WITHDRAW : null);
        return curveBranch;
    }

    public NIDGroupData readInScanDataAndSkipOthers(FileChannel channel, ChannelFilter filter, SourceReadingState state) throws UserCommunicableException
    {
        List<ImageChannel> imageChannels = new ArrayList<>();

        for(NIDChannel ch : channels.values())
        {        
            if(state.isOutOfJob())
            {
                return NIDGroupData.getEmptyInstance();
            }

            if(ch.shouldBeAccepted(filter))
            {
                imageChannels.addAll(ch.readInScanDataAndSkipOthers(channel, state).getReadInImageChannels().values());
            }
            else
            {
                ch.skipData(channel);               
            }
        }

        return NIDGroupData.getScanOnlyInstance(imageChannels);
    }

    public NIDGroupData readInSpectroscopyDataAndSkipOthers(FileChannel channel, ChannelFilter filter, SourceReadingState state) throws UserCommunicableException
    {
        Map<String, QuantityArray2DExpression> rawData = new HashMap<>();


        for(NIDChannel ch : channels.values())
        {   
            if(state.isOutOfJob())
            {
                return NIDGroupData.getEmptyInstance();
            }
            if(ch.shouldBeAccepted(filter))
            {
                NIDChannelData channelData = ch.readInSpectroscopyDataAndSkipOthers(channel, state);

                rawData.putAll(channelData.getReadInSpectroscopyData());
            }
            else
            {
                ch.skipData(channel);
            }
        }

        List<Channel1DData> spData = convertRawSpectroscopyDataToChannels(rawData);

        return NIDGroupData.getSpectroscopyOnlyInstance(spData);
    }

    public NIDGroupData readInData(FileChannel channel, ChannelFilter filter, SourceReadingState state) throws UserCommunicableException
    {               
        Map<String, QuantityArray2DExpression> rawSpectroscopyData = new HashMap<>();
        List<ImageChannel> readInImageChannels = new ArrayList<>();

        for(NIDChannel ch: channels.values())
        {
            if(state.isOutOfJob())
            {
                return NIDGroupData.getEmptyInstance();
            }

            if(ch.shouldBeAccepted(filter))
            {
                NIDChannelData channelData = ch.readInData(channel, state);

                rawSpectroscopyData.putAll(channelData.getReadInSpectroscopyData());
                readInImageChannels.addAll(channelData.getReadInImageChannels().values());
            }
            else
            {
                ch.skipData(channel);
            }
        }

        List<Channel1DData> readInSpectroscopyData = convertRawSpectroscopyDataToChannels(rawSpectroscopyData);
        return new NIDGroupData(readInImageChannels, readInSpectroscopyData);
    }

    public int getCurveCount()
    {
        NIDChannel xCurveChannel = null;
        NIDChannel yCurveChannel = null;

        for(NIDChannel channel : channels.values())
        {
            if(channel.isCurveXChannel())
            {
                xCurveChannel = channel;
            }
            else if(channel.isCurveYChannel())
            {
                yCurveChannel = channel;
            }
        }

        if(xCurveChannel == null || yCurveChannel == null)
        {
            return 0;
        }

        int curveCount = Math.min(xCurveChannel.getLineCount(), yCurveChannel.getLineCount());

        return curveCount;
    }

    private List<Channel1DData> convertRawSpectroscopyDataToChannels(Map<String, QuantityArray2DExpression> rawData)
    {
        List<Channel1DData> curveBranches = new ArrayList<>();

        String zPosition = NIDChannelType.Z_POSITION.getName();
        QuantityArray2DExpression xChannelExpression = rawData.get(zPosition);

        //        String zSensor = NIDChannelType.USER_INPUT_3.getName();
        //        xChannelExpression = (xChannelExpression != null) ? xChannelExpression : rawData.get(zSensor); 

        QuantityArray2DExpression yChannelExpression = rawData.get(NIDChannelType.Z_CONTROLLER_INPUT.getName());

        if(xChannelExpression == null || yChannelExpression == null)
        {
            return curveBranches;
        }

        double[][] xChannelData = xChannelExpression.getValues();
        Quantity xQuantity = xChannelExpression.getQuantity();

        double[][] yChannelData = yChannelExpression.getValues();
        Quantity yQuantity = yChannelExpression.getQuantity();

        int curveCount = Math.min(xChannelData.length,yChannelData.length);

        for(int i = 0; i<curveCount;i++)
        {
            double[] xData = xChannelData[i];
            double[] yData = yChannelData[i];

            int pointCount = Math.min(xData.length, yData.length);
            double[][] points = new double[pointCount][];

            for(int j = 0; j<pointCount;j++)
            {
                points[j] = new double[] {xData[j], yData[j]};
            }

            ForceCurveBranch branch = getForceCurveBranch();
            double[][] correctedPoints = ForceCurveOrientation.LEFT.correctOrientation(points, branch);

            Channel1DData channelData = new FlexibleChannel1DData(correctedPoints, xQuantity, yQuantity, branch.getDefaultXOrderForLeftHandSideContactOrientation());
            curveBranches.add(channelData);
        }

        return curveBranches;
    }



    /**
     * Nanosurf saves it's spectroscopy data in a slalom mode (see below). This 
     * function corrects the channels so that they're displayed in proper order
     * 
     * Left picture in which order they are scanned, right picture how atomicJ initial
     * thought it was 
     * 
     * # # # #               # # # #
     * # # # #               # # # # 
     * 7     4        <=>    4     7    
     * # # # #               # # # # 
     * 0     3               0     3
     * 
     * @param curveBranches A list of all curves collected for the measurement
     * @return A slalom corrected list of all curves
     */

    //ok, so I changed  the method   buildSpectroscopySources(Map<String, MultiMap<ForceCurveBranch, Channel1DData>>, File )
    //to achieve the same effect in a bit faster way, changing  "grid.getPointFlattenedWithFullReturn(i)" to source.setRecordingPoint(grid.getPointFlattenedBackedAndForth(i));
    //Pawel

    private List<Channel1DData> correctSlalomMode(List<Channel1DData> curveBranches)
    {
        // Calculate number of lines and rows assuming a quadratic grid shape
        int numCurves = curveBranches.size();
        int sqrt = (int) Math.sqrt(numCurves);

        // Get individual lines
        List<List<Channel1DData>> linesList = new ArrayList<>();
        for(int i=0; i < sqrt; i++) 
        {
            List<Channel1DData> lineCurves = new ArrayList<>();
            lineCurves.addAll(curveBranches.subList(i*sqrt, i*sqrt + sqrt));

            linesList.add(lineCurves);
        }

        // Invert every second line
        for(int i=0; i < linesList.size(); i++) 
        {
            if( (i % 2) == 1) 
            {
                Collections.reverse(linesList.get(i));
            }
        }

        curveBranches.clear();
        for(int i=0; i < linesList.size(); i++) {
            curveBranches.addAll(linesList.get(i));
        }
        return curveBranches;
    }

    public double[] reverseArray(double[] xData) 
    {
        double[] temp = new double[xData.length];
        int iterator = 0;
        for(int k=xData.length - 1; k >= 0; k--) {
            temp[iterator] = xData[k];
            iterator++;
        }

        return temp;
    }
    public int getReadableElementCount()
    {
        int elementCount = 0;

        for(NIDChannel channel : channels.values())
        {
            elementCount += channel.getReadableElementCount();
        }

        return elementCount;
    }

    public int getSpectroscopyElementCount()
    {
        int elementCount = 0;

        for(NIDChannel channel : channels.values())
        {
            elementCount += channel.getSpectroscopyElementCount();
        }

        return elementCount;
    }

    public int getImageElementCount()
    {
        int elementCount = 0;

        for(NIDChannel channel : channels.values())
        {
            elementCount += channel.getImageElementCount();
        }

        return elementCount;
    }
}