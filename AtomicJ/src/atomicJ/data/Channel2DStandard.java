
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

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Map;

import org.jfree.data.Range;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.Channel2DDataTransformation;
import atomicJ.resources.CrossSectionSettings;

public class Channel2DStandard implements Channel2D
{ 
    private Channel2DData channelData;

    private final ChannelMetadata channelMetadata;
    private String channelInfo;

    private final String identifier;

    private int duplicationCount = 0;

    public Channel2DStandard(Channel2DData channelData, String identifier) 
    {
        this(channelData, "", identifier);
    }

    public Channel2DStandard(Channel2DData channelData, String channelInfo, String identifier) 
    {
        this.identifier = identifier;
        this.channelData = channelData;

        this.channelInfo = channelInfo;
        this.channelMetadata = DummyChannelMetadata.getInstance();
    }

    public Channel2DStandard(Channel2DData channelData, String channelInfo, String identifier, ChannelMetadata channelMetadata) 
    {
        this.identifier = identifier;
        this.channelData = channelData;

        this.channelInfo = channelInfo;
        this.channelMetadata = channelMetadata;
    }

    public Channel2DStandard(Channel2DStandard channelOld) 
    {
        this(channelOld, channelOld.identifier);
    }

    public Channel2DStandard(Channel2DStandard channelOld, String identifierNew) 
    {
        this.identifier = identifierNew;
        this.channelData = channelOld.channelData.getCopy();
        this.channelInfo = channelOld.channelInfo;
        this.channelMetadata = channelOld.channelMetadata.copyIfNeccesary();
    }

    @Override
    public ChannelMetadata getMetadata()
    {
        return channelMetadata;
    }

    @Override
    public Channel2DStandard getCopy()
    {
        return new Channel2DStandard(this);
    }

    @Override
    public Channel2DStandard duplicate()
    {
        String identifierNew = identifier + " (" + String.valueOf(duplicationCount + 1) + ")";
        this.duplicationCount = duplicationCount + 1;

        return new Channel2DStandard(this, identifierNew);
    }

    @Override
    public Channel2DStandard duplicate(String identifierNew)
    {
        this.duplicationCount = duplicationCount + 1;
        return new Channel2DStandard(this, identifierNew);
    }

    @Override
    public Rectangle2D getDataArea()
    {
        return channelData.getDataArea();        
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
    public Quantity getZQuantity() 
    {
        return channelData.getZQuantity();
    }

    @Override
    public double getDataDensity()
    {
        return channelData.getDataDensity();
    }

    @Override
    public Channel2DData getChannelData() 
    {
        return channelData;
    }

    @Override
    public GridChannel2DData getDefaultGridding()
    {
        return channelData.getDefaultGridding();
    }

    @Override
    public void setChannelData(Channel2DData channelData) 
    {
        this.channelData = channelData;
    }

    @Override
    public boolean transform(Channel2DDataTransformation tr) 
    {        
        this.channelData = tr.transform(channelData);

        return true;      
    }

    @Override
    public boolean transform(Channel2DDataInROITransformation tr, ROI roi, ROIRelativePosition position) 
    {
        this.channelData = tr.transform(channelData, roi, position);
        return true;
    }

    @Override
    public double[][] getPoints()
    {
        return channelData.getPoints();
    }

    @Override
    public double getZ(int item)
    {
        return channelData.getZ(item);
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
    public double getValue(Point2D dataPoint)
    {
        return channelData.getValue(dataPoint);
    }

    @Override
    public boolean isWithinDataDomain(Point2D dataPoint)
    {
        return channelData.isWithinDataDomain(dataPoint);
    }

    @Override
    public int getItemCount()
    {
        return channelData.getItemCount();
    }

    @Override
    public String getIdentifier() 
    {
        return identifier;
    }

    @Override
    public double[] getXCoordinatesCopy()
    {
        return channelData.getXCoordinatesCopy();
    }

    @Override
    public double[] getYCoordinatesCopy()
    {
        return channelData.getYCoordinatesCopy();
    }

    @Override
    public double[] getZCoordinatesCopy()
    {
        return channelData.getZCoordinatesCopy();
    }

    @Override
    public QuantitativeSample getXSample() 
    {
        return channelData.getXSample();
    }

    @Override
    public QuantitativeSample getYSample() 
    {
        return channelData.getYSample();
    }

    @Override
    public QuantitativeSample getZSample() 
    {
        return channelData.getZSample(channelInfo);
    }

    public  QuantitativeSample getROISample(ROI roi, String sampleKeyTail)
    {
        return channelData.getROISample(roi, ROIRelativePosition.INSIDE, sampleKeyTail);
    }

    @Override
    public Map<Object, QuantitativeSample> getROISamples(Collection<ROI> rois, String sampleKeyTail)
    {
        return channelData.getROISamples(rois, ROIRelativePosition.INSIDE, sampleKeyTail);
    }

    @Override
    public double[][] getXYZView() 
    {
        return channelData.getXYZView();
    }

    @Override
    public Range getXRange()
    {
        return channelData.getXRange();
    }

    @Override
    public Range getYRange()
    {
        return channelData.getYRange();
    }

    @Override
    public Range getZRange() 
    {
        return  channelData.getZRange();
    }

    @Override
    public Range getAutomaticZRange()
    {
        return channelData.getAutomaticZRange();
    }

    @Override
    public ChannelDomainIdentifier getDomainIdentifier()
    {
        return channelData.getDomainIdentifier();
    }

    @Override
    public void setChannelInfo(String info)
    {
        this.channelInfo = info;
    }

    @Override
    public String getChannelInfo() 
    {
        return channelInfo;
    }

    @Override
    public double getXDataDensity() 
    {
        return channelData.getXDataDensity();
    }

    @Override
    public double getYDataDensity() 
    {
        return channelData.getYDataDensity();
    }

    @Override
    public double[][] getCrossSection(Shape profile, CrossSectionSettings settings) 
    {  
        return channelData.getCrossSection(profile, settings);
    }

    @Override
    public double[] getProfileValues(Shape profile, CrossSectionSettings settings)
    {        
        return channelData.getProfileValues(profile, settings);
    }

    @Override
    public Grid2D getDefaultGriddingGrid()
    {
        return channelData.getDefaultGriddingGrid();
    }

    @Override
    public boolean isInterpolationPreparationNecessary(InterpolationMethod2D interpolationMethod)
    {
        return channelData.isInterpolationPreparationNecessary(interpolationMethod);
    }

    @Override
    public void prepareForInterpolationIfNecessary(InterpolationMethod2D interpolation)
    {
        this.channelData.prepareForInterpolationIfNecessary(interpolation);
    }
}