
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

package atomicJ.gui.profile;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.Map;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DStandard;
import atomicJ.data.Channel2D;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.StandardSample;
import atomicJ.data.units.Quantity;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.DistanceShapeFactors.DirectedPosition;
import atomicJ.resources.CrossSectionSettings;


public class ChannelSectionLine
{	
    private final Shape profileShape;

    private final Object key;
    private final String name;

    private final Quantity dQuantity;
    private final Quantity zQuantity;

    private final Channel2D channel;

    public ChannelSectionLine(Shape profileShape, Channel2D channel, Quantity dQuantity, Quantity zQuantity, Object key, String name)
    {
        this.profileShape = profileShape;
        this.dQuantity = dQuantity;
        this.zQuantity = zQuantity;
        this.key= key;
        this.name = name;
        this.channel = channel;
    }

    public Object getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public Quantity getDomainQuantity()
    {
        return dQuantity;
    }

    public Quantity getRangeQuantity()
    {
        return zQuantity;
    }

    public double getLength()
    {
        double length = DistanceShapeFactors.getLength(profileShape, 0);
        return length;
    }

    public Point2D getChannelPoint(double lengthPosition)
    {        
        DirectedPosition dirPos = DistanceShapeFactors.getDirectedPosition(profileShape, lengthPosition);
        Point2D p = dirPos.getPoint();

        return p;
    }

    public double getValue(double d, CrossSectionSettings settings)
    {
        Point2D channelpoint = getChannelPoint(d);

        double x = channelpoint.getX();
        double y = channelpoint.getY();

        InterpolationMethod2D interpolationMethod = settings.getInterpolationMethod();

        double z = interpolationMethod.getValue(channel.getChannelData(), x, y);

        return z;
    }

    public Map<String, QuantitativeSample> getCrossSectionaSamples(int pointCount, CrossSectionSettings settings)
    {
        Map<String, QuantitativeSample> samples = new LinkedHashMap<>();

        double[][] profilePoints = DistanceShapeFactors.getXYDTriples(profileShape, pointCount);

        double[] ds = new double[pointCount];
        double[] ys = new double[pointCount];

        InterpolationMethod2D interpolationMethod = settings.getInterpolationMethod();

        for(int i = 0; i<pointCount; i++)
        {
            double[] p = profilePoints[i];
            double x = p[0];
            double y = p[1];
            double d = p[2];
            double z = interpolationMethod.getValue(channel.getChannelData(), x, y);

            ds[i] = d;
            ys[i] = z;
        }


        QuantitativeSample DsSample = new StandardSample(ds, name + " x ", dQuantity.changeName(name + " x"),"","");
        QuantitativeSample YsSample = new StandardSample(ys, name + " y ", zQuantity.changeName(name + " y "),"","");

        samples.put(name + " x", DsSample);
        samples.put(name + " y", YsSample);

        return samples;
    }

    public Channel1D getCrossSection(CrossSectionSettings settings) 
    {	
        double[][] data = channel.getCrossSection(profileShape, settings);

        Channel1D crossSection = new Channel1DStandard(new FlexibleChannel1DData(data, dQuantity, zQuantity, SortedArrayOrder.ASCENDING),key.toString(),name);
        return crossSection;
    }
}
