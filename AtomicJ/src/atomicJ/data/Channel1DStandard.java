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

package atomicJ.data;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.Range;

import atomicJ.curveProcessing.Channel1DDataInROITransformation;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.Validation;

public class Channel1DStandard implements Channel1D
{
    private Channel1DData channelData;
    private final String name;
    private final String identifier;

    private ModificationConstraint1D modificationConstraint;
    private final ChannelGroupTag groupTag;

    public Channel1DStandard(Channel1DData channelData, String identifier)
    {
        this(channelData, identifier, identifier.toString());
    }

    public Channel1DStandard(Channel1DData channelData, String identifier, String name)
    {
        this.channelData = Validation.requireNonNullParameterName(channelData, "channelData");
        this.identifier = Validation.requireNonNullParameterName(identifier, "identifier");
        this.name = name;
        this.groupTag = null;
    }

    public Channel1DStandard(Channel1DData channelData, String identifier, String name, ModificationConstraint1D modificationConstraint)
    {
        this(channelData, identifier, name, modificationConstraint, null);
    }

    public Channel1DStandard(Channel1DData channelData, String identifier, String name, ModificationConstraint1D modificationConstraint, ChannelGroupTag groupTag)
    {
        this.channelData = Validation.requireNonNullParameterName(channelData, "channelData");
        this.identifier = Validation.requireNonNullParameterName(identifier, "identifier");
        this.name = name;
        this.modificationConstraint = modificationConstraint;
        this.groupTag = groupTag;
    }

    @Override
    public Channel1DStandard getCopy()
    {
        Channel1DData dataCopy = channelData.getCopy();
        Channel1DStandard channelCopy = new Channel1DStandard(dataCopy, this.identifier, this.name);

        return channelCopy;
    }

    @Override
    public Channel1DStandard getCopy(String identifierNew, String nameNew)
    {
        Channel1DData dataCopy = channelData.getCopy();
        Channel1DStandard channelCopy = new Channel1DStandard(dataCopy, identifierNew, nameNew);

        return channelCopy;
    }

    @Override
    public Channel1DStandard getCopy(double scale)
    {
        Channel1DData dataCopy = channelData.getCopy(scale);
        Channel1DStandard channelCopy = new Channel1DStandard(dataCopy, this.identifier, this.name);

        return channelCopy;
    }

    @Override
    public Channel1DStandard getCopy(double scale, String identifierNew, String nameNew)
    {
        Channel1DData dataCopy = channelData.getCopy(scale);
        Channel1DStandard channelCopy = new Channel1DStandard(dataCopy, identifierNew, nameNew);

        return channelCopy;
    }

    @Override
    public boolean isPointPositionConstrained()
    {        
        return (modificationConstraint != null);
    }

    @Override
    public Point2D constrain(Map<String, Channel1D> allChannels, int itemIndex, Point2D dataPoint)
    {
        Point2D constrainedPoint = (modificationConstraint != null) ? modificationConstraint.getValidItemPosition(this, allChannels, itemIndex, dataPoint) : dataPoint;
        return constrainedPoint;
    }

    @Override
    public boolean isValidPosition(Channel1D channel, Map<String, Channel1D> allChannels, int itemIndex, Point2D dataPoint)
    {
        boolean valid = (modificationConstraint != null) ? modificationConstraint.isValidPosition(channel, allChannels, itemIndex, dataPoint) : true;
        return valid;
    }

    @Override
    public boolean transform(Channel1DDataTransformation tr) 
    {
        this.channelData = tr.transform(channelData);

        return true;
    }

    @Override
    public boolean transform(Channel1DDataInROITransformation tr, ROI roi, ROIRelativePosition position) 
    {
        this.channelData = tr.transform(channelData, roi, position);
        return true;
    }

    @Override
    public String getIdentifier()
    {
        return identifier;
    }

    @Override
    public ChannelGroupTag getGroupTag()
    {
        return groupTag;
    }

    @Override
    public Channel1DData getChannelData() 
    {
        return channelData;
    }

    @Override
    public void setChannelData(Channel1DData channelData)
    {
        this.channelData = Validation.requireNonNullParameterName(channelData, "channelData");
    }

    @Override
    public double[][] getPoints()
    {
        return channelData.getPoints();
    }

    @Override
    public double[][] getPointsCopy() {
        return channelData.getPointsCopy();
    }

    @Override
    public double[][] getPointsCopy(double scale) 
    {
        return channelData.getPointsCopy(scale);
    }

    @Override
    public boolean isEmpty()
    {
        return channelData.isEmpty();
    }

    @Override
    public Quantity getXQuantity()
    {
        return channelData.getXQuantity();
    }

    @Override
    public Quantity getYQuantity()
    {
        return channelData.getYQuantity();
    }

    @Override
    public int getItemCount()
    {
        return channelData.getItemCount();
    }

    @Override
    public double getX(int item)
    {
        return channelData.getX(item);
    }

    @Override
    public double getY(int item)
    {
        return channelData.getY(item);
    }

    @Override
    public double[] getXCoordinates() {
        return channelData.getXCoordinatesCopy();
    }

    @Override
    public double[] getYCoordinates()
    {
        return channelData.getYCoordinatesCopy();
    }

    @Override
    public double[][] getXYView() 
    {
        return channelData.getXYViewCopy();
    }

    @Override
    public boolean isDicrete()
    {
        return (channelData.getItemCount() == 1);
    }

    @Override
    public Map<String, QuantitativeSample> getSamples()
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        double[][] data = channelData.getPoints();

        if(data != null && data.length>0)
        {
            int dataCount = data.length;

            double[] dataXs = new double[dataCount];
            double[] dataYs = new double[dataCount];

            for(int i = 0; i<dataCount; i++)
            {
                double[] p = data[i];

                dataXs[i] = p[0];
                dataYs[i] = p[1];
            }

            QuantitativeSample approachXsSample = new StandardSample(dataXs, identifier + " X", getXQuantity().changeName(name + " X"), name + " X");
            QuantitativeSample approachYsSample = new StandardSample(dataYs, identifier + " Y", getYQuantity().changeName(name + " Y"), name + " Y");

            samples.put(identifier + " X", approachXsSample );
            samples.put(identifier + " Y", approachYsSample );
        }

        return samples;
    }

    @Override
    public List<? extends Channel1D> getChannels()
    {
        return Collections.singletonList(this);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public double getXMinimum() 
    {
        return channelData.getXMinimum();
    }

    @Override
    public double getXMaximum()
    {
        return channelData.getXMaximum();
    }

    @Override
    public Range getXRange()
    {
        return channelData.getXRange();
    }

    @Override
    public double getYMinimum()
    {
        return channelData.getYMinimum();
    }

    @Override
    public double getYMaximum() 
    {
        return channelData.getYMaximum();
    }

    @Override
    public Range getYRange()
    {
        return channelData.getYRange();
    }

    @Override
    public Range getYRange(Range xRange)
    {
        return channelData.getYRange(xRange);
    }

    @Override
    public IndexRange getIndexRangeBoundedBy(double lowerBound, double upperBound) 
    {       
        return channelData.getIndexRangeBoundedBy(lowerBound, upperBound);
    }

    //also includes points for which x == lowerBound or x == upperBound
    @Override
    public int getIndexCountBoundedBy(double lowerBound, double upperBound)
    {
        return channelData.getIndexCountBoundedBy(lowerBound, upperBound);
    }

    @Override
    public double getXMinimumIncludingErrors()
    {
        return channelData.getXMinimumIncludingErrors();
    }

    @Override
    public double getXMaximumIncludingErrors()
    {
        return channelData.getXMaximumIncludingErrors();
    }

    @Override
    public double getYMinimumIncludingErrors()
    {
        return channelData.getYMinimumIncludingErrors();
    }

    @Override
    public double getYMaximumIncludingErrors()
    {
        return channelData.getYMaximumIncludingErrors();
    }

    @Override
    public Range getYRangeIncludingErrors(Range xRange)
    {
        return channelData.getYRangeIncludingErrors(xRange);
    }
}

