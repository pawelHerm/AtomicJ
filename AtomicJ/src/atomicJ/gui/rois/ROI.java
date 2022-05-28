
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

package atomicJ.gui.rois;

import java.awt.Shape;
import java.awt.geom.Point2D;

import atomicJ.data.ArraySupport2D;
import atomicJ.gui.Identifiable;
import atomicJ.gui.ShapeFactors;


public interface ROI extends Identifiable
{
    public Shape getROIShape();
    public boolean contains(Point2D p);
    public Object getKey();
    public String getLabel();
    public void setLabel(String labelNew);
    public ShapeFactors getShapeFactors(double flatness);
    public boolean equalsUpToStyle(ROI that);

    public int getPointsInsideCountUpperBound(ArraySupport2D grid);
    public int getPointsOutsideCountUpperBound(ArraySupport2D grid);
    public int getPointCountUpperBound(ArraySupport2D grid, ROIRelativePosition position);

    public void addPoints(ArraySupport2D grid, ROIRelativePosition position, GridPointRecepient recepient);
    public void addPointsInside(ArraySupport2D grid, GridPointRecepient recepient);
    public void addPointsOutside(ArraySupport2D grid, GridPointRecepient recepient);
    //imageMinRow, imageMaxRow, imageMinColumn and imageMaxColumn inclusive
    public void dividePoints(ArraySupport2D grid, int imageMinRow, int imageMaxRow,int imageMinColumn, int imageMaxColumn,
            GridBiPointRecepient recepient);
    public ROI copy();
    public ROI getRotatedCopy(double angle, double anchorX, double anchorY);
}
