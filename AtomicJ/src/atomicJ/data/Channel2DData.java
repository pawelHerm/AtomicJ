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
import atomicJ.resources.CrossSectionSettings;

public interface Channel2DData
{
    public Channel2DData getCopy(); //ok
    public Quantity getXQuantity();//ok
    public Quantity getYQuantity();//ok
    public Quantity getZQuantity(); //ok
    public double[][] getPoints();//ok
    public double[][] getPointsCopy();//ok
    public boolean isEmpty(); //ok
    public int getItemCount();  //ok 
    public double getDataDensity();
    public double getX(int item); //ok
    public double getY(int item); //ok
    public double getZ(int item); //ok
    public double getXDataDensity();
    public double getYDataDensity();
    public boolean isWithinDataDomain(Point2D dataPoint);
    public Rectangle2D getDataArea();

    public Range getXRange();      
    public Range getYRange();
    public Range getZRange();
    public Range getAutomaticZRange();

    public double[] getXCoordinates();
    public double[] getYCoordinates();
    public double[] getZCoordinates();
    public double[] getXCoordinatesCopy();
    public double[] getYCoordinatesCopy();
    public double[] getZCoordinatesCopy();
    public QuantitativeSample getXSample();
    public QuantitativeSample getYSample();
    public QuantitativeSample getZSample(String nameTag) ;

    public double[] getPoint(int item);
    public double getValue(Point2D dataPoint);
    public double[][] getXYZView();

    public Grid2D getDefaultGriddingGrid();
    //copies the original channel only if necessary
    public GridChannel2DData getDefaultGridding();
    //copies the original channel only if necessary
    public GridChannel2DData getGridding(Grid2D gridNew);

    public ChannelDomainIdentifier getDomainIdentifier();

    public double[] getProfileValues(Shape profile, CrossSectionSettings settings);
    public double[][] getCrossSection(Shape profile, CrossSectionSettings settings);

    public double[] getROIData(ROI roi, ROIRelativePosition position);
    public QuantitativeSample getROISample(ROI roi, ROIRelativePosition position, String sampleKeyTail);
    public Map<Object, QuantitativeSample> getROISamples(Collection<ROI> rois, ROIRelativePosition position, String sampleKeyTail);
    public boolean isInterpolationPreparationNecessary(InterpolationMethod2D interpolationMethod);
    public void prepareForInterpolationIfNecessary(InterpolationMethod2D interpolation);
}

