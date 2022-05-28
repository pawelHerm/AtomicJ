
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


public interface Channel2D extends Channel
{
    public Channel2D getCopy();
    public Channel2D duplicate();
    public Channel2D duplicate(String identifierNew);

    public String getChannelInfo();
    public void setChannelInfo(String info);

    public ChannelMetadata getMetadata();

    public Quantity getZQuantity();

    public double getDataDensity();
    public double getXDataDensity();
    public double getYDataDensity();
    public Grid2D getDefaultGriddingGrid();

    public Range getZRange();
    public Range getAutomaticZRange();
    public boolean isWithinDataDomain(Point2D dataPoint);

    public double getZ(int item);
    public double getValue(Point2D dataPoint);

    public double[][] getPoints();
    public double[] getXCoordinatesCopy();
    public double[] getYCoordinatesCopy();
    public double[] getZCoordinatesCopy();
    public double[][] getXYZView();

    public boolean transform(Channel2DDataTransformation tr);
    public boolean transform(Channel2DDataInROITransformation tr, ROI roi, ROIRelativePosition position);

    public GridChannel2DData getDefaultGridding();

    public Channel2DData getChannelData();
    public void setChannelData(Channel2DData data);

    public Rectangle2D getDataArea();
    public ChannelDomainIdentifier getDomainIdentifier();

    public QuantitativeSample getXSample();
    public QuantitativeSample getYSample();
    public QuantitativeSample getZSample();

    public Map<Object, QuantitativeSample> getROISamples(Collection<ROI> rois, String sampleKeyTail);
    public double[][] getCrossSection(Shape profile, CrossSectionSettings settings);
    public double[] getProfileValues(Shape profile, CrossSectionSettings settings);
    public boolean isInterpolationPreparationNecessary(InterpolationMethod2D interpolationMethod);
    public void prepareForInterpolationIfNecessary(InterpolationMethod2D interpolation);
}