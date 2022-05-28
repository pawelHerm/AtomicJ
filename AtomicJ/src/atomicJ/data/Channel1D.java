/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
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

package atomicJ.data;

import java.awt.geom.Point2D;
import java.util.Map;

import org.jfree.data.Range;

import atomicJ.curveProcessing.Channel1DDataInROITransformation;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public interface Channel1D extends TransformableData1D<Channel1D>, Channel
{
    public Channel1D getCopy();

    @Override
    public Channel1D getCopy(double scale);
    public Channel1D getCopy(String identifierNew, String nameNew);
    public Channel1D getCopy(double scale, String identifierNew, String nameNew);
    @Override
    public boolean transform(Channel1DDataTransformation tr);
    public boolean transform(Channel1DDataInROITransformation tr, ROI roi, ROIRelativePosition position);

    public boolean isDicrete();
    public double[] getXCoordinates();
    public double[] getYCoordinates();
    public double[][] getXYView();
    public double[][] getPoints();
    public double[][] getPointsCopy();
    public double[][] getPointsCopy(double scale);

    public double getXMinimum();
    public double getXMaximum();
    public double getYMinimum();
    public double getYMaximum();

    public double getXMinimumIncludingErrors();
    public double getXMaximumIncludingErrors();    
    public double getYMinimumIncludingErrors();  
    public double getYMaximumIncludingErrors();
    public Range getYRangeIncludingErrors(Range xRange);

    public Range getYRange(Range xRange);

    public Channel1DData getChannelData();
    public void setChannelData(Channel1DData channelData);
    public boolean isPointPositionConstrained();
    public Point2D constrain(Map<String, Channel1D> allChannels, int itemIndex, Point2D dataPoint);
    public boolean isValidPosition(Channel1D channel, Map<String, Channel1D> allChannels, int itemIndex, Point2D dataPoint);

    public Map<String, QuantitativeSample> getSamples();

    public IndexRange getIndexRangeBoundedBy(double lowerBound, double upperBound);
    //also includes points for which x == lowerBound or x == upperBound
    public int getIndexCountBoundedBy(double lowerBound, double upperBound);
    public ChannelGroupTag getGroupTag();
}

