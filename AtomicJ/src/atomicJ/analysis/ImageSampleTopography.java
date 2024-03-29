/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe� Hermanowicz
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

package atomicJ.analysis;

import java.awt.geom.Point2D;

import atomicJ.data.Channel2D;
import atomicJ.gui.SampleTopography;

public class ImageSampleTopography implements SampleTopography
{
    private final Channel2D topographyChannel;

    public ImageSampleTopography(Channel2D topographyChannel)
    {
        this.topographyChannel = topographyChannel;
    }

    @Override
    public double getHeight(double x, double y) 
    {
        return topographyChannel.getValue(new Point2D.Double(x,y));
    }
}
